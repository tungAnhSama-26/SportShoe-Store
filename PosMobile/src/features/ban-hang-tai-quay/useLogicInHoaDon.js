import { useCallback } from 'react';
import * as Print from 'expo-print';
import { showToastSuccess, showError } from '../../utils/alert';

export function useLogicInHoaDon() {
  const xuLyInHoaDonTaiQuay = useCallback(async ({
    hoaDonChoDaChon,
    cartItems,
    phiVanChuyen,
    tienGiam,
    tongTien,
    khachCanTra,
    tenKhachHangHienThi,
    soDienThoaiKhachHangHienThi
  }) => {
    if (!hoaDonChoDaChon) return;

    showToastSuccess(`Đang chuẩn bị in hóa đơn ${hoaDonChoDaChon.ma}...`);

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

    const invoiceHtml = `
      <div class="pos-invoice">
        <style>
          @media print {
            body { -webkit-print-color-adjust: exact; }
          }
          .pos-invoice {
            width: 100%;
            max-width: 148mm;
            margin: 0 auto;
            overflow: hidden;
            border: 1px solid #fecaca;
            border-radius: 6px;
            background: #ffffff;
            color: #0f172a;
            font-family: "Inter", "Segoe UI", Arial, sans-serif;
            font-size: 12px;
            line-height: 1.45;
          }

          .pos-header {
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            gap: 16px;
            background: #c52220;
            color: #ffffff;
            padding: 14px 16px;
          }

          .pos-brand {
            margin: 0;
            font-size: 18px;
            font-weight: 900;
          }

          .pos-subtitle,
          .pos-code-label {
            margin: 4px 0 0;
            color: #fee2e2;
            font-size: 10px;
          }

          .pos-code {
            text-align: right;
          }

          .pos-code strong {
            display: block;
            margin-top: 4px;
            font-size: 14px;
          }

          .pos-title {
            display: flex;
            justify-content: space-between;
            gap: 14px;
            padding: 14px 16px;
            border-bottom: 1px solid #fecaca;
            background: #fff7f7;
          }

          .pos-title h1 {
            margin: 0;
            color: #991b1b;
            font-size: 24px;
            line-height: 1.1;
          }

          .pos-title p {
            margin: 7px 0 0;
            color: #64748b;
            font-size: 11px;
          }

          .pos-badge {
            align-self: flex-start;
            border: 1px solid #fecaca;
            background: white;
            color: #dc2626;
            padding: 4px 8px;
            font-size: 11px;
            font-weight: bold;
            border-radius: 4px;
          }

          .pos-customer {
            padding: 14px 16px;
            background: #ffffff;
          }

          .pos-customer p {
            margin: 0 0 6px;
            display: flex;
            gap: 8px;
          }

          .pos-customer p:last-child {
            margin: 0;
          }

          .pos-customer strong {
            color: #334155;
            width: 70px;
            flex-shrink: 0;
          }

          .pos-table-wrapper {
            padding: 0 16px;
            margin-bottom: 16px;
          }

          .pos-table {
            width: 100%;
            border-collapse: collapse;
          }

          .pos-table th {
            text-align: left;
            padding: 8px 6px;
            border-bottom: 2px solid #cbd5e1;
            color: #475569;
            font-weight: 700;
            font-size: 11px;
            white-space: nowrap;
          }

          .pos-table td {
            padding: 10px 6px;
            border-bottom: 1px dashed #e2e8f0;
            vertical-align: top;
          }

          .pos-table td strong {
            display: block;
            color: #0f172a;
            font-size: 13px;
          }

          .pos-table td span {
            display: block;
            margin-top: 4px;
            color: #64748b;
            font-size: 11px;
          }

          .cell-center { text-align: center; }
          .cell-money { text-align: right; white-space: nowrap; }
          .pos-table th.cell-money, .pos-table th.cell-center { text-align: right; }
          .pos-table th.cell-center { text-align: center; }

          .pos-summary {
            background: #f8fafc;
            padding: 14px 16px;
            border-top: 2px solid #e2e8f0;
          }

          .pos-summary h2 {
            margin: 0 0 12px;
            font-size: 13px;
            color: #475569;
            text-transform: uppercase;
            letter-spacing: 0.05em;
          }

          .money-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 8px;
            font-size: 13px;
            color: #334155;
          }

          .money-row.discount {
            color: #16a34a;
          }

          .money-row.money-total {
            margin-top: 12px;
            padding-top: 12px;
            border-top: 1px solid #cbd5e1;
            font-size: 16px;
            color: #0f172a;
          }

          .money-row.money-total strong {
            color: #dc2626;
            font-size: 18px;
          }

          .pos-thanks {
            text-align: center;
            padding: 16px;
            background: #f1f5f9;
            color: #64748b;
            font-size: 11px;
            border-top: 1px dashed #cbd5e1;
          }

          .pos-thanks strong {
            display: block;
            color: #0f172a;
            font-size: 13px;
            margin-bottom: 4px;
          }
        </style>

        <header class="pos-header">
          <div>
            <h2 class="pos-brand">GIÀY VIỆT STORE</h2>
            <p class="pos-subtitle">123 Đường Bán Giày, Hà Nội</p>
            <p class="pos-subtitle">ĐT: 0123.456.789</p>
          </div>
          <div class="pos-code">
            <p class="pos-code-label">MÃ HÓA ĐƠN</p>
            <strong>${hoaDonChoDaChon.ma}</strong>
          </div>
        </header>

        <section class="pos-title">
          <div>
            <h1>Phiếu thanh toán</h1>
            <p>${new Date().toLocaleString("vi-VN")}</p>
          </div>
          <div class="pos-badge">ĐÃ THANH TOÁN</div>
        </section>

        <section class="pos-customer">
          <p><strong>Khách hàng:</strong> <span>${tenKhachHangHienThi || "-"}</span></p>
          <p><strong>SĐT:</strong> <span>${soDienThoaiKhachHangHienThi || "-"}</span></p>
        </section>

        <div class="pos-table-wrapper">
          <table class="pos-table">
            <thead>
              <tr>
                <th style="width: 30px;">#</th>
                <th>Sản phẩm</th>
                <th class="cell-center" style="width: 40px;">SL</th>
                <th class="cell-money">Đơn giá</th>
                <th class="cell-money">Thành tiền</th>
              </tr>
            </thead>
            <tbody>
              ${rowsHtml}
            </tbody>
          </table>
        </div>

        <section class="pos-summary">
          <h2>Tổng kết thanh toán</h2>
          <div class="money-row">
            <span>Tổng tiền hàng</span>
            <strong>${tongTien.toLocaleString("vi-VN")} đ</strong>
          </div>
          ${deliveryFeeRow}
          ${discountRow}
          <div class="money-row money-total">
            <span>Khách cần trả</span>
            <strong>${khachCanTra.toLocaleString("vi-VN")} đ</strong>
          </div>
        </section>

        <footer class="pos-thanks">
          <strong>Cảm ơn quý khách!</strong>
          Hóa đơn được phát hành bởi SportShoe. Vui lòng kiểm tra sản phẩm và tổng thanh toán trước khi rời quầy.
        </footer>
      </div>
    `;

    try {
      await Print.printAsync({
        html: invoiceHtml,
      });
    } catch (err) {
      showError(`Có lỗi xảy ra khi in: ${err.message}`);
    }
  }, []);

  return {
    xuLyInHoaDonTaiQuay
  };
}
