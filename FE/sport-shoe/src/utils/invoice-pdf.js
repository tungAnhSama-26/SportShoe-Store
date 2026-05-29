function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function isMeaningfulText(value) {
  const text = String(value ?? "").trim();
  const normalized = text.toLowerCase();
  return Boolean(text)
    && text !== "-"
    && normalized !== "không áp dụng"
    && normalized !== "không có"
    && normalized !== "chưa cập nhật";
}

function buildInfoItem(label, value, options = {}) {
  if (options.hideIfEmpty && !isMeaningfulText(value)) {
    return "";
  }

  const displayValue = isMeaningfulText(value) ? value : options.fallback || "Không có";
  const wideClass = options.wide ? " info-item-wide" : "";

  return `
    <div class="info-item${wideClass}">
      <span class="info-label">${escapeHtml(label)}</span>
      <span class="info-value">${escapeHtml(displayValue)}</span>
    </div>
  `;
}

function buildMoneyRow(label, value, formatCurrency, options = {}) {
  const className = options.total ? "money-row money-row-total" : "money-row";
  const valueClass = options.discount ? "money-value money-value-discount" : "money-value";
  const prefix = options.discount ? "- " : options.plus ? "+ " : "";

  return `
    <div class="${className}">
      <span>${escapeHtml(label)}</span>
      <span class="${valueClass}">${escapeHtml(prefix + formatCurrency(value || 0))}</span>
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
  const phiVanChuyen = Number(invoice.phiVanChuyen || 0);
  const giamGia = Number(invoice.giamGia || 0);
  const tongCanTra = tongTienHang + phiVanChuyen - giamGia;
  const hasVoucher = isMeaningfulText(invoice.voucher);
  const createdAt = invoice.ngayTao ? formatDate(invoice.ngayTao) : "Không có";
  const printedAt = formatDate(new Date().toISOString());
  const invoiceCode = invoice.maHoaDon || filename || "SPORTSHOE";
  const status = invoice.trangThai || "Chưa cập nhật";

  const itemRows = items
    .map(
      (item, index) => `
        <tr>
          <td class="cell-center">${index + 1}</td>
          <td>
            <span class="product-name">${escapeHtml(item.tenSanPham || "-")}</span>
            <span class="product-meta">${escapeHtml([item.mauSac, item.kichCo].filter(Boolean).join(" / ") || item.phanLoai || "-")}</span>
          </td>
          <td class="cell-center tabular">${escapeHtml(item.soLuong || 0)}</td>
          <td class="cell-money tabular">${escapeHtml(formatCurrency(item.donGia || 0))}</td>
          <td class="cell-money tabular">${escapeHtml(formatCurrency(item.thanhTien || 0))}</td>
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
          :root {
            --brand: #B82220;
            --brand-dark: #8f1716;
            --ink: #0f172a;
            --muted: #64748b;
            --line: #e2e8f0;
            --soft: #f8fafc;
            --soft-red: #fff1f2;
          }

          * {
            box-sizing: border-box;
            -webkit-print-color-adjust: exact;
            print-color-adjust: exact;
          }

          body {
            margin: 0;
            background: #f1f5f9;
            color: var(--ink);
            font-family: "Inter", "Be Vietnam Pro", "Segoe UI", Arial, sans-serif;
            font-size: 13px;
            line-height: 1.45;
          }

          .invoice-page {
            width: 210mm;
            min-height: 297mm;
            margin: 0 auto;
            background: #ffffff;
            padding: 18mm;
          }

          .hero {
            border: 1px solid #fecaca;
            border-radius: 18px;
            overflow: hidden;
            margin-bottom: 16px;
          }

          .hero-top {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            background: var(--brand);
            color: #ffffff;
            padding: 14px 18px;
          }

          .brand {
            display: flex;
            align-items: center;
            gap: 0;
          }

          .brand-name {
            margin: 0;
            font-size: 18px;
            font-weight: 850;
          }

          .brand-subtitle {
            margin: 3px 0 0;
            color: #fee2e2;
            font-size: 11px;
          }

          .hero-code {
            text-align: right;
          }

          .hero-code-label {
            display: block;
            color: #fee2e2;
            font-size: 10px;
            font-weight: 700;
            text-transform: uppercase;
          }

          .hero-code-value {
            display: block;
            margin-top: 4px;
            font-size: 15px;
            font-weight: 500;
            letter-spacing: 0;
          }

          .hero-body {
            display: grid;
            grid-template-columns: 1.3fr 0.7fr;
            gap: 12px;
            background: #fff7f7;
            padding: 13px 18px;
          }

          .invoice-title {
            margin: 0;
            font-size: 26px;
            line-height: 1.1;
            font-weight: 900;
            color: var(--brand-dark);
          }

          .printed-at {
            margin: 8px 0 0;
            color: var(--muted);
            font-size: 12px;
          }

          .status-pill {
            justify-self: end;
            align-self: start;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            border-radius: 999px;
            background: #ffffff;
            border: 1px solid #fecaca;
            padding: 7px 11px;
            color: var(--brand);
            font-weight: 500;
          }

          .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: var(--brand);
          }

          .section {
            border: 1px solid var(--line);
            border-radius: 16px;
            padding: 16px;
            margin-bottom: 14px;
            break-inside: avoid;
          }

          .section-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            margin-bottom: 12px;
            padding-bottom: 10px;
            border-bottom: 1px solid var(--line);
          }

          .section-title {
            margin: 0;
            color: var(--ink);
            font-size: 15px;
            font-weight: 850;
          }

          .section-note {
            color: var(--muted);
            font-size: 12px;
            font-weight: 500;
          }

          .info-grid {
            display: grid;
            grid-template-columns: minmax(0, 1fr);
            gap: 8px;
          }

          .info-item {
            display: grid;
            grid-template-columns: 128px minmax(0, 1fr);
            gap: 14px;
            align-items: start;
            min-height: 26px;
          }

          .info-item-wide {
            grid-column: 1 / -1;
          }

          .info-label {
            color: var(--muted);
            font-weight: 650;
          }

          .info-value {
            color: var(--ink);
            font-weight: 500;
            word-break: break-word;
          }

          table {
            width: 100%;
            table-layout: fixed;
            border-collapse: separate;
            border-spacing: 0;
            overflow: hidden;
            border: 1px solid var(--line);
            border-radius: 14px;
            font-size: 12px;
          }

          th,
          td {
            padding: 11px 12px;
            text-align: left;
            vertical-align: top;
            border-bottom: 1px solid var(--line);
          }

          th {
            background: var(--soft-red);
            color: var(--brand-dark);
            font-size: 11px;
            font-weight: 850;
          }

          th:nth-child(1),
          th:nth-child(3) {
            text-align: center;
          }

          th:nth-child(4),
          th:nth-child(5) {
            text-align: right;
          }

          tbody tr:nth-child(even) td {
            background: var(--soft);
          }

          tbody tr:last-child td {
            border-bottom: 0;
          }

          .product-name {
            display: block;
            color: var(--ink);
            font-weight: 500;
          }

          .product-meta {
            display: block;
            margin-top: 3px;
            color: var(--muted);
            font-size: 11px;
          }

          .cell-type {
            color: var(--muted);
          }

          .cell-center {
            text-align: center;
          }

          .cell-money {
            text-align: right;
            white-space: nowrap;
          }

          .tabular {
            font-variant-numeric: tabular-nums;
          }

          .summary-layout {
            display: block;
          }

          .thanks {
            margin-top: 14px;
            border-radius: 16px;
            background: var(--soft);
            border: 1px solid var(--line);
            padding: 16px;
            color: var(--muted);
          }

          .thanks strong {
            display: block;
            margin-bottom: 6px;
            color: var(--ink);
            font-size: 15px;
          }

          .summary-card {
            width: 100%;
            margin-left: 0;
            border-radius: 16px;
            border: 1px solid #fecaca;
            background: #fffafa;
            padding: 16px;
          }

          .money-row {
            display: flex;
            justify-content: space-between;
            gap: 14px;
            padding: 8px 0;
            color: var(--muted);
          }

          .money-value {
            color: var(--ink);
            font-weight: 500;
            font-variant-numeric: tabular-nums;
            white-space: nowrap;
          }

          .money-value-discount {
            color: #059669;
          }

          .money-row-total {
            margin-top: 8px;
            padding-top: 14px;
            border-top: 1px solid #fecaca;
            color: var(--brand);
            font-size: 16px;
            font-weight: 900;
          }

          .money-row-total .money-value {
            color: var(--brand);
            font-size: 18px;
          }

          @media print {
            body {
              background: #ffffff;
            }

            .invoice-page {
              width: auto;
              min-height: auto;
              margin: 0;
              padding: 0;
            }
          }

          @page {
            size: A4;
            margin: 12mm;
          }
        </style>
      </head>
      <body>
        <main class="invoice-page">
          <section class="hero">
            <div class="hero-top">
              <div class="brand">
                <div>
                  <p class="brand-name">SportShoe</p>
                  <p class="brand-subtitle">Giày thể thao chính hãng</p>
                </div>
              </div>
              <div class="hero-code">
                <span class="hero-code-label">Mã hóa đơn</span>
                <span class="hero-code-value">${escapeHtml(invoiceCode)}</span>
              </div>
            </div>
            <div class="hero-body">
              <div>
                <h1 class="invoice-title">Hóa đơn bán hàng</h1>
                <p class="printed-at">Ngày tạo: ${escapeHtml(createdAt)} · In lúc: ${escapeHtml(printedAt)}</p>
              </div>
              <div class="status-pill">
                <span class="status-dot"></span>
                ${escapeHtml(status)}
              </div>
            </div>
          </section>

          <section class="section">
            <div class="section-header">
              <h2 class="section-title">Thông tin hóa đơn</h2>
              <span class="section-note">${escapeHtml(invoice.loaiDon || "Chưa cập nhật")}</span>
            </div>
            <div class="info-grid">
              ${buildInfoItem("Nhân viên", invoice.maNhanVien || invoice.tenNhanVien || "Chưa gán")}
              ${buildInfoItem("Khách hàng", invoice.tenKhachHang, { hideIfEmpty: true })}
              ${buildInfoItem("Email", invoice.email, { hideIfEmpty: true })}
              ${buildInfoItem("Số điện thoại", invoice.soDienThoai, { hideIfEmpty: true })}
              ${buildInfoItem("Địa chỉ", invoice.diaChi, { wide: true, fallback: "Mua tại quầy" })}
              ${buildInfoItem("Ghi chú", invoice.ghiChu, { wide: true, hideIfEmpty: true })}
            </div>
          </section>

          <section class="section">
            <div class="section-header">
              <h2 class="section-title">Danh sách sản phẩm</h2>
              <span class="section-note">${items.length} sản phẩm</span>
            </div>
            <table>
              <colgroup>
                <col style="width: 48px" />
                <col />
                <col style="width: 96px" />
                <col style="width: 132px" />
                <col style="width: 142px" />
              </colgroup>
              <thead>
                <tr>
                  <th>STT</th>
                  <th>Sản phẩm</th>
                  <th>Số lượng</th>
                  <th>Đơn giá</th>
                  <th>Thành tiền</th>
                </tr>
              </thead>
              <tbody>
                ${itemRows || '<tr><td colspan="5" class="cell-center">Không có sản phẩm</td></tr>'}
              </tbody>
            </table>
          </section>

          <section class="summary-layout">
            <aside class="summary-card">
              <div class="section-header">
                <h2 class="section-title">Tổng kết thanh toán</h2>
              </div>
              ${buildMoneyRow("Tổng tiền hàng", tongTienHang, formatCurrency)}
              ${phiVanChuyen > 0 ? buildMoneyRow("Phí vận chuyển", phiVanChuyen, formatCurrency, { plus: true }) : ""}
              ${hasVoucher ? buildInfoItem("Mã giảm giá", invoice.voucher, { wide: true }) : ""}
              ${giamGia > 0 ? buildMoneyRow("Số tiền được giảm", giamGia, formatCurrency, { discount: true }) : ""}
              ${buildMoneyRow("Tổng thanh toán", tongCanTra, formatCurrency, { total: true })}
            </aside>

            <div class="thanks">
              <strong>Cảm ơn quý khách!</strong>
              Hóa đơn được phát hành bởi SportShoe. Vui lòng kiểm tra thông tin sản phẩm và tổng thanh toán trước khi rời quầy.
            </div>
          </section>
        </main>

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
