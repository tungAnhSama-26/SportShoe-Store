export function LogicInHoaDon() {
  let html2pdfLoader = null;

  async function loadHtml2Pdf() {
    if (!html2pdfLoader) {
      html2pdfLoader = import("html2pdf.js").then((module) => module.default ?? module);
    }
    return html2pdfLoader;
  }

  async function xuLyInHoaDonTaiQuay({
    hoaDonChoDaChon,
    cartItems,
    phiVanChuyen,
    tienGiam,
    tongTien,
    khachCanTra,
    tenKhachHangHienThi,
    soDienThoaiKhachHangHienThi,
    thongBaoThanhCong,
    thongBaoLoi
  }) {
    if (!hoaDonChoDaChon) return;

    thongBaoThanhCong.value = `Đang tạo PDF hóa đơn ${hoaDonChoDaChon.ma}...`;

    const rowsHtml = cartItems.map((item, index) => `
      <tr>
        <td>${index + 1}</td>
        <td>
          <strong>${item.tenSanPham}</strong>
          <span>${item.mauSac || "-"} / ${item.kichCo || "-"}</span>
        </td>
        <td class="cell-center">${item.soLuong}</td>
        <td class="cell-money">${item.giaBan.toLocaleString("vi-VN")} đ</td>
        <td class="cell-money">${(item.soLuong * item.giaBan).toLocaleString("vi-VN")} đ</td>
      </tr>
    `).join("");

    const deliveryFeeRow = phiVanChuyen > 0 ? `
      <div class="money-row">
        <span>Phí giao hàng</span>
        <strong>+ ${phiVanChuyen.toLocaleString("vi-VN")} đ</strong>
      </div>
    ` : "";

    const discountRow = tienGiam > 0 ? `
      <div class="money-row discount">
        <span>Giảm giá</span>
        <strong>- ${tienGiam.toLocaleString("vi-VN")} đ</strong>
      </div>
    ` : "";

    const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=128x128&margin=1&data=${encodeURIComponent(hoaDonChoDaChon.ma || "SPORTSHOE")}`;

    const invoiceHtml = `
      <div class="pos-receipt">
        <style>
          .pos-receipt {
            width: 80mm;
            margin: 0 auto;
            background: #ffffff;
            color: #000000;
            font-family: "Plus Jakarta Sans", "Inter", "Segoe UI", Arial, sans-serif;
            font-size: 11px;
            line-height: 1.35;
            padding: 8px 10px;
          }

          .pos-qr {
            text-align: center;
            margin-bottom: 8px;
          }

          .pos-qr img {
            width: 80px;
            height: 80px;
            object-fit: contain;
            display: inline-block;
          }

          .pos-header {
            text-align: center;
            margin-bottom: 6px;
          }

          .pos-brand {
            margin: 0 0 3px;
            font-size: 15px;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.02em;
            color: #000000;
          }

          .pos-store-info {
            margin: 2px 0;
            color: #333333;
            font-size: 10.5px;
          }

          .pos-divider-dashed {
            border: none;
            border-top: 1px dashed #555555;
            margin: 8px 0;
          }

          .pos-divider-solid {
            border: none;
            border-top: 1px solid #000000;
            margin: 6px 0;
          }

          .pos-title-box {
            text-align: center;
            margin: 6px 0 8px;
          }

          .pos-title-box h1 {
            margin: 0 0 3px;
            font-size: 14px;
            font-weight: 800;
            text-transform: uppercase;
            color: #000000;
          }

          .pos-title-box .pos-code {
            font-size: 12px;
            font-weight: 700;
            margin: 2px 0;
          }

          .pos-title-box .pos-date {
            font-size: 10.5px;
            color: #444444;
            margin: 2px 0;
          }

          .pos-meta {
            font-size: 11px;
            margin: 6px 0;
          }

          .pos-meta-row {
            display: flex;
            justify-content: space-between;
            margin: 2px 0;
          }

          .pos-meta-row span {
            color: #444444;
          }

          .pos-meta-row strong {
            color: #000000;
            font-weight: 600;
          }

          .pos-table {
            width: 100%;
            border-collapse: collapse;
            margin: 6px 0;
            font-size: 11px;
          }

          .pos-table th {
            text-align: left;
            padding: 4px 2px;
            border-top: 1px solid #000000;
            border-bottom: 1px solid #000000;
            font-weight: 700;
            color: #000000;
          }

          .pos-table td {
            padding: 5px 2px 3px;
            vertical-align: top;
          }

          .pos-table strong {
            display: block;
            color: #000000;
            font-size: 11px;
          }

          .pos-table span {
            display: block;
            margin-top: 1px;
            color: #555555;
            font-size: 10px;
          }

          .cell-center { text-align: center; }
          .cell-money { text-align: right; font-variant-numeric: tabular-nums; white-space: nowrap; }
          .pos-table th.cell-money { text-align: right; }
          .pos-table th.cell-center { text-align: center; }

          .pos-summary {
            margin: 6px 0;
            font-size: 11.5px;
          }

          .money-row {
            display: flex;
            justify-content: space-between;
            padding: 2px 0;
            color: #222222;
          }

          .money-row.discount {
            color: #15803d;
          }

          .money-row.money-total {
            margin-top: 4px;
            padding-top: 5px;
            border-top: 1px solid #000000;
            font-size: 13px;
            font-weight: 800;
            color: #000000;
          }

          .money-row.money-total strong {
            font-size: 14px;
            font-weight: 800;
          }

          .pos-thanks {
            text-align: center;
            margin-top: 10px;
            font-size: 10.5px;
            color: #444444;
          }

          .pos-thanks strong {
            display: block;
            color: #000000;
            font-size: 11.5px;
            margin-bottom: 2px;
          }
        </style>

        <div class="pos-qr">
          <img src="${qrUrl}" alt="QR Code Hóa đơn" />
        </div>

        <header class="pos-header">
          <h2 class="pos-brand">SportShoe Store</h2>
          <p class="pos-store-info">Đ/C: 163 Thái Hà, Đống Đa, Hà Nội</p>
          <p class="pos-store-info">SĐT: 0965.852.782</p>
        </header>

        <hr class="pos-divider-dashed" />

        <section class="pos-title-box">
          <h1>HOÁ ĐƠN THANH TOÁN</h1>
          <p class="pos-code">Số: ${hoaDonChoDaChon.ma}</p>
          <p class="pos-date">In lúc: ${(() => {
            const d = new Date();
            const datePart = new Intl.DateTimeFormat("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" }).format(d);
            const timePart = new Intl.DateTimeFormat("vi-VN", { hour: "2-digit", minute: "2-digit", second: "2-digit", hour12: false }).format(d);
            return `${timePart} Ngày ${datePart}`;
          })()}</p>
        </section>

        <section class="pos-meta">
          <div class="pos-meta-row">
            <span>Khách hàng:</span>
            <strong>${tenKhachHangHienThi || "Khách lẻ"}</strong>
          </div>
          ${soDienThoaiKhachHangHienThi ? `
            <div class="pos-meta-row">
              <span>SĐT:</span>
              <strong>${soDienThoaiKhachHangHienThi}</strong>
            </div>
          ` : ""}
          <div class="pos-meta-row">
            <span>Hình thức:</span>
            <strong>Bán tại quầy</strong>
          </div>
        </section>

        <table class="pos-table">
          <thead>
            <tr>
              <th>Tên hàng</th>
              <th class="cell-money" style="width: 55px;">Đ.giá</th>
              <th class="cell-center" style="width: 25px;">SL</th>
              <th class="cell-money" style="width: 65px;">TT</th>
            </tr>
          </thead>
          <tbody>
            ${rowsHtml}
          </tbody>
        </table>

        <hr class="pos-divider-solid" />

        <section class="pos-summary">
          ${(phiVanChuyen > 0 || tienGiam > 0) ? `
            <div class="money-row">
              <span>Tổng tiền hàng</span>
              <strong>${tongTien.toLocaleString("vi-VN")} đ</strong>
            </div>
            ${deliveryFeeRow}
            ${discountRow}
          ` : ""}
          <div class="money-row money-total">
            <span>Tổng cộng</span>
            <strong>${khachCanTra.toLocaleString("vi-VN")} đ</strong>
          </div>
        </section>

        <hr class="pos-divider-dashed" />

        <footer class="pos-thanks">
          <strong>Xin cám ơn, hẹn gặp lại quý khách!</strong>
          SportShoe Store
        </footer>
      </div>
    `;

    const opt = {
      margin: 4,
      filename: `HoaDon_${hoaDonChoDaChon.ma}.pdf`,
      image: { type: "jpeg", quality: 0.98 },
      html2canvas: { scale: 2, useCORS: true },
      jsPDF: { unit: "mm", format: [80, 200], orientation: "portrait" }
    };

    const html2pdf = await loadHtml2Pdf();
    html2pdf().set(opt).from(invoiceHtml).save().then(() => {
      thongBaoThanhCong.value = `Đã tải PDF hóa đơn ${hoaDonChoDaChon.ma}.`;
      setTimeout(() => { thongBaoThanhCong.value = ""; }, 3000);
    }).catch((err) => {
      thongBaoLoi.value = `Có lỗi xảy ra khi in PDF: ${err.message}`;
    });
  }

  return {
    xuLyInHoaDonTaiQuay
  };
}
