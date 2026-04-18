function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function buildValueRow(label, value) {
  return `
    <div class="info-row">
      <span class="info-label">${escapeHtml(label)}</span>
      <span class="info-value">${escapeHtml(value)}</span>
    </div>
  `;
}

export function printInvoiceToPdf({
  invoice,
  formatCurrency,
  formatDate,
  filename = "hoa-don",
  targetWindow = null,
}) {
  if (!invoice) return false;

  const popup = targetWindow || window.open("", "_blank", "width=1100,height=800");
  if (!popup) {
    throw new Error("Trình duyệt đang chặn cửa sổ in PDF.");
  }

  const items = Array.isArray(invoice.sanPham) ? invoice.sanPham : [];
  const tongTienHang = items.reduce((sum, item) => sum + Number(item.thanhTien || 0), 0);
  const tongCanTra =
    tongTienHang +
    Number(invoice.phiVanChuyen || 0) -
    Number(invoice.giamGia || 0);

  const itemRows = items
    .map(
      (item, index) => `
        <tr>
          <td>${index + 1}</td>
          <td>${escapeHtml(item.tenSanPham || "-")}</td>
          <td>${escapeHtml(item.mauSac || item.phanLoai || "-")}</td>
          <td>${escapeHtml(item.soLuong || 0)}</td>
          <td>${escapeHtml(formatCurrency(item.donGia || 0))}</td>
          <td>${escapeHtml(formatCurrency(item.thanhTien || 0))}</td>
        </tr>
      `,
    )
    .join("");

  popup.document.open();
  popup.document.write(`
    <!doctype html>
    <html lang="vi">
      <head>
        <meta charset="UTF-8" />
        <title>${escapeHtml(filename)}</title>
        <style>
          body {
            font-family: "Be Vietnam Pro", "Segoe UI", sans-serif;
            margin: 28px;
            color: #0f172a;
          }
          .header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 24px;
            margin-bottom: 20px;
          }
          .title {
            font-size: 28px;
            font-weight: 800;
            margin: 0 0 6px;
          }
          .subtitle {
            margin: 0;
            color: #475569;
            font-size: 13px;
          }
          .card {
            border: 1px solid #e2e8f0;
            border-radius: 16px;
            padding: 16px 18px;
            margin-bottom: 18px;
          }
          .section-title {
            font-size: 15px;
            font-weight: 700;
            margin: 0 0 12px;
          }
          .info-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 10px 18px;
          }
          .info-row {
            display: flex;
            gap: 10px;
            font-size: 13px;
          }
          .info-label {
            width: 120px;
            color: #475569;
            font-weight: 600;
            flex-shrink: 0;
          }
          .info-value {
            font-weight: 500;
          }
          table {
            width: 100%;
            border-collapse: collapse;
            font-size: 12px;
          }
          th, td {
            border: 1px solid #dbeafe;
            padding: 9px 10px;
            text-align: left;
            vertical-align: top;
          }
          th {
            background: #f8fafc;
            font-weight: 700;
          }
          .summary {
            margin-left: auto;
            width: min(380px, 100%);
          }
          .summary-row {
            display: flex;
            justify-content: space-between;
            gap: 12px;
            padding: 7px 0;
            font-size: 13px;
          }
          .summary-row.total {
            border-top: 1px solid #e2e8f0;
            margin-top: 4px;
            padding-top: 12px;
            font-size: 15px;
            font-weight: 800;
            color: #dc2626;
          }
          @media print {
            body {
              margin: 14mm;
            }
          }
          @page {
            size: A4;
            margin: 14mm;
          }
        </style>
      </head>
      <body>
        <div class="header">
          <div>
            <h1 class="title">Hóa đơn bán hàng</h1>
            <p class="subtitle">In lúc ${escapeHtml(formatDate(new Date().toISOString()))}</p>
          </div>
        </div>

        <div class="card">
          <h2 class="section-title">Thông tin hóa đơn</h2>
          <div class="info-grid">
            ${buildValueRow("Mã hóa đơn", invoice.maHoaDon || "-")}
            ${buildValueRow("Ngày tạo", formatDate(invoice.ngayTao))}
            ${buildValueRow("Trạng thái", invoice.trangThai || "-")}
            ${buildValueRow("Loại đơn", invoice.loaiDon || "-")}
            ${buildValueRow("Nhân viên", invoice.tenNhanVien || "Chưa gán")}
            ${buildValueRow("Khách hàng", invoice.tenKhachHang || "-")}
            ${buildValueRow("Số điện thoại", invoice.soDienThoai || "-")}
            ${buildValueRow("Email", invoice.email || "-")}
            ${buildValueRow("Địa chỉ", invoice.diaChi || "-")}
            ${buildValueRow("Ghi chú", invoice.ghiChu || "-")}
          </div>
        </div>

        <div class="card">
          <h2 class="section-title">Sản phẩm</h2>
          <table>
            <thead>
              <tr>
                <th>STT</th>
                <th>Sản phẩm</th>
                <th>Phân loại</th>
                <th>Số lượng</th>
                <th>Đơn giá</th>
                <th>Thành tiền</th>
              </tr>
            </thead>
            <tbody>
              ${itemRows || '<tr><td colspan="6">Không có sản phẩm</td></tr>'}
            </tbody>
          </table>
        </div>

        <div class="card summary">
          <h2 class="section-title">Tổng kết</h2>
          <div class="summary-row">
            <span>Tổng tiền hàng</span>
            <strong>${escapeHtml(formatCurrency(tongTienHang))}</strong>
          </div>
          <div class="summary-row">
            <span>Phí vận chuyển</span>
            <strong>${escapeHtml(formatCurrency(invoice.phiVanChuyen || 0))}</strong>
          </div>
          <div class="summary-row">
            <span>Voucher áp dụng</span>
            <strong>${escapeHtml(invoice.voucher || "-")}</strong>
          </div>
          <div class="summary-row">
            <span>Số tiền được giảm</span>
            <strong>${escapeHtml(formatCurrency(invoice.giamGia || 0))}</strong>
          </div>
          <div class="summary-row total">
            <span>Tổng thanh toán</span>
            <strong>${escapeHtml(formatCurrency(tongCanTra))}</strong>
          </div>
        </div>

        <script>
          window.onload = () => {
            setTimeout(() => {
              window.focus();
              window.print();
            }, 200);
          };
          window.onafterprint = () => window.close();
        <\/script>
      </body>
    </html>
  `);
  popup.document.close();
  return true;
}
