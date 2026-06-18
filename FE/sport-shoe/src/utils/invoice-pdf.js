import logoGhn from "../constants/logoGhn";
import logoSportShoe from "../assets/logo/logoPhu.png";

const SHOP_ADDRESS = "Chung cư Sông Hồng, ngõ 163 Thái Hà, phường Láng Hạ, quận Đống Đa, thành phố Hà Nội";

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
    && text !== "—"
    && normalized !== "không áp dụng"
    && normalized !== "không có"
    && normalized !== "chưa cập nhật";
}

function buildAddressLine(label, value, options = {}) {
  if (options.hideIfEmpty && !isMeaningfulText(value)) {
    return "";
  }

  const displayValue = isMeaningfulText(value) ? value : options.fallback || "Không có";

  return `
    <div class="address-line">
      <span>${escapeHtml(label)}</span>
      <strong>${escapeHtml(displayValue)}</strong>
    </div>
  `;
}

function buildMoneyRow(label, value, formatCurrency, options = {}) {
  const className = options.total ? "money-row money-row-total" : "money-row";
  const valueClass = options.discount ? "money-value money-value-discount" : "money-value";
  const prefix = options.discount ? "- " : options.plus ? "+ " : "";
  const labelContent = options.logoSrc
    ? `<span class="money-label-with-logo"><span>${escapeHtml(label)}</span><img src="${escapeHtml(options.logoSrc)}" alt="${escapeHtml(options.logoAlt || "")}" class="money-label-logo" /></span>`
    : escapeHtml(label);

  return `
    <div class="${className}">
      <span>${labelContent}</span>
      <span class="${valueClass}">${escapeHtml(prefix + formatCurrency(value || 0))}</span>
    </div>
  `;
}

function buildSummaryTextRow(label, value, options = {}) {
  const valueClass = options.discount ? "money-value money-value-discount" : "money-value";

  return `
    <div class="money-row">
      <span>${escapeHtml(label)}</span>
      <span class="${valueClass}">${escapeHtml(value)}</span>
    </div>
  `;
}

function formatPercent(value) {
  return new Intl.NumberFormat("vi-VN", {
    maximumFractionDigits: 2,
  }).format(Number(value || 0));
}

function getVoucherDiscountText(invoice, actualDiscount, formatCurrency) {
  const type = Number(invoice?.loaiGiamGia);
  const configuredValue = Number(invoice?.giaTriGiamGia || 0);

  if (type === 1 && configuredValue > 0) {
    return `${formatPercent(configuredValue)}% (- ${formatCurrency(actualDiscount || 0)})`;
  }

  return `- ${formatCurrency(actualDiscount || configuredValue || 0)}`;
}

function getCustomerName(invoice) {
  return invoice?.tenKhachHang || invoice?.tenNguoiNhan || "Khách vãng lai";
}

function getCustomerPhone(invoice) {
  return invoice?.soDienThoai || invoice?.sdtNguoiNhan || "Không có";
}

function buildBarcodeText(code) {
  const raw = String(code || "SPORTSHOE").replace(/[^A-Za-z0-9]/g, "").slice(0, 18);
  return raw || "SPORTSHOE";
}

function buildInvoiceQrUrl(code) {
  const qrText = String(code || "SPORTSHOE").trim() || "SPORTSHOE";
  return `https://api.qrserver.com/v1/create-qr-code/?size=128x128&margin=1&data=${encodeURIComponent(qrText)}`;
}

function normalizePlainText(value) {
  return String(value ?? "")
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .trim();
}

function isStoreInvoice(invoice) {
  const orderType = normalizePlainText(invoice?.loaiDon);
  const address = normalizePlainText(invoice?.diaChi);
  return ["cua hang", "offline", "tai cua hang"].includes(orderType)
    || address === "mua tai quay";
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
  const hasVoucher = isMeaningfulText(invoice.voucher) && giamGia > 0;
  const voucherDiscountText = hasVoucher ? getVoucherDiscountText(invoice, giamGia, formatCurrency) : "";
  const createdAt = invoice.ngayTao ? formatDate(invoice.ngayTao) : "Không có";
  const printedAt = formatDate(new Date().toISOString());
  const invoiceCode = invoice.maHoaDon || filename || "SPORTSHOE";
  const status = invoice.trangThai || "Chưa cập nhật";
  const barcodeText = buildBarcodeText(invoiceCode);
  const invoiceQrUrl = buildInvoiceQrUrl(invoiceCode);
  const isStoreOrder = isStoreInvoice(invoice);

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
            --brand: #c52220;
            --brand-dark: #991b1b;
            --brand-soft: #fff1f2;
            --ink: #0f172a;
            --muted: #64748b;
            --line: #dbe3ef;
            --line-strong: #f2b8b8;
            --soft: #f8fafc;
            --radius: 6px;
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
            font-size: 12px;
            line-height: 1.4;
          }

          .invoice-page {
            width: 210mm;
            min-height: 297mm;
            margin: 0 auto;
            background: #ffffff;
            padding: 12mm;
          }

          .shipping-invoice {
            overflow: hidden;
            border: 1px solid var(--line-strong);
            border-radius: var(--radius);
            background: #ffffff;
            box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
          }

          .top-band {
            display: grid;
            grid-template-columns: 1fr 245px;
            background: var(--brand);
            color: #ffffff;
          }

          .brand-box {
            padding: 15px 18px;
          }

          .brand-name {
            margin: 0;
            font-size: 19px;
            font-weight: 900;
            line-height: 1;
          }

          .brand-subtitle {
            margin: 6px 0 0;
            color: #fee2e2;
            font-size: 11px;
          }

          .code-box {
            padding: 12px 16px;
            text-align: right;
          }

          .code-label {
            display: block;
            color: #fee2e2;
            font-size: 10px;
            font-weight: 800;
            text-transform: uppercase;
          }

          .code-value {
            display: block;
            margin-top: 4px;
            font-size: 15px;
            font-weight: 800;
          }

          .fake-barcode {
            width: 196px;
            height: 38px;
            margin: 9px 0 0 auto;
            border-radius: 3px;
            background:
              repeating-linear-gradient(
                90deg,
                #ffffff 0 2px,
                transparent 2px 4px,
                #ffffff 4px 5px,
                transparent 5px 8px,
                #ffffff 8px 11px,
                transparent 11px 13px
              );
            opacity: 0.95;
          }

          .invoice-heading {
            display: grid;
            grid-template-columns: 1fr auto;
            gap: 16px;
            align-items: start;
            background: #fff7f7;
            border-bottom: 1px solid #fecaca;
            padding: 14px 18px;
          }

          .invoice-title {
            margin: 0;
            color: var(--brand-dark);
            font-size: 26px;
            line-height: 1.05;
            font-weight: 900;
          }

          .printed-at {
            margin: 8px 0 0;
            color: var(--muted);
            font-size: 11px;
          }

          .status-pill {
            display: inline-flex;
            align-items: center;
            gap: 7px;
            border: 1px solid #fecaca;
            border-radius: 999px;
            background: #ffffff;
            color: var(--brand);
            padding: 7px 12px;
            font-weight: 700;
          }

          .status-dot {
            width: 7px;
            height: 7px;
            border-radius: 999px;
            background: var(--brand);
          }

          .route-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            border-bottom: 1px dashed #94a3b8;
          }

          .store-info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 8px 24px;
          }

          .store-info-grid .address-line {
            grid-template-columns: 110px minmax(0, 1fr);
          }

          .store-info-grid .address-line-wide {
            grid-column: 1 / -1;
          }

          .route-panel {
            min-height: 138px;
            padding: 14px 16px;
          }

          .route-panel + .route-panel {
            border-left: 1px dashed #94a3b8;
          }

          .route-title {
            margin: 0 0 10px;
            color: var(--brand-dark);
            font-size: 15px;
            font-weight: 900;
          }

          .address-line {
            display: grid;
            grid-template-columns: 92px minmax(0, 1fr);
            gap: 10px;
            margin-top: 7px;
          }

          .address-line span {
            color: var(--muted);
            font-weight: 700;
          }

          .address-line strong {
            color: var(--ink);
            font-weight: 650;
            word-break: break-word;
          }

          .order-strip {
            display: grid;
            grid-template-columns: 1fr 168px;
            border-bottom: 1px dashed #94a3b8;
          }

          .shipping-brand-block {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            gap: 10px;
            min-height: 74px;
            border-right: 1px dashed #94a3b8;
            padding: 16px;
            text-align: center;
          }

          .shipping-brand-main {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 14px;
          }

          .shipping-brand-logo {
            width: 82px;
            height: 64px;
            object-fit: contain;
          }

          .shipping-brand-name {
            color: var(--brand);
            font-size: 38px;
            font-weight: 950;
            line-height: 1;
          }

          .shipping-carrier {
            display: inline-flex;
            align-items: center;
            justify-content: center;
          }

          .shipping-carrier-logo {
            height: 24px;
            width: auto;
            object-fit: contain;
          }

          .order-meta {
            display: flex;
            flex-direction: column;
            justify-content: center;
            gap: 5px;
            padding: 12px;
            text-align: center;
          }

          .invoice-qr {
            width: 82px;
            height: 82px;
            margin: 2px auto 6px;
            border: 1px solid var(--line);
            border-radius: var(--radius);
            background: #ffffff;
            object-fit: contain;
            padding: 5px;
          }

          .order-meta span {
            color: var(--muted);
            font-size: 10px;
            font-weight: 800;
            text-transform: uppercase;
          }

          .order-meta strong {
            color: var(--ink);
            font-size: 13px;
          }

          .section {
            padding: 16px 18px;
            border-bottom: 1px solid var(--line);
            break-inside: avoid;
          }

          .section-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            margin-bottom: 11px;
          }

          .section-title {
            margin: 0;
            color: var(--ink);
            font-size: 15px;
            font-weight: 900;
          }

          .section-note {
            color: var(--muted);
            font-size: 11px;
            font-weight: 700;
          }

          table {
            width: 100%;
            table-layout: fixed;
            border-collapse: separate;
            border-spacing: 0;
            overflow: hidden;
            border: 1px solid var(--line);
            border-radius: var(--radius);
            font-size: 11px;
          }

          th,
          td {
            padding: 10px 11px;
            text-align: left;
            vertical-align: top;
            border-bottom: 1px solid var(--line);
          }

          th {
            background: var(--brand-soft);
            color: var(--brand-dark);
            font-size: 10px;
            font-weight: 900;
            text-transform: uppercase;
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
            font-weight: 750;
          }

          .product-meta {
            display: block;
            margin-top: 3px;
            color: var(--muted);
            font-size: 10px;
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

          .payment-section {
            padding: 16px 18px;
            border-bottom: 1px solid var(--line);
            break-inside: avoid;
          }

          .money-row {
            display: flex;
            justify-content: space-between;
            gap: 16px;
            padding: 7px 0;
            color: var(--muted);
          }

          .money-label-with-logo {
            display: inline-flex;
            align-items: center;
            gap: 7px;
          }

          .money-label-logo {
            height: 15px;
            width: auto;
            object-fit: contain;
          }

          .money-value {
            color: var(--ink);
            font-weight: 800;
            font-variant-numeric: tabular-nums;
            white-space: nowrap;
          }

          .money-value-discount {
            color: #059669;
          }

          .money-row-total {
            margin-top: 8px;
            padding-top: 13px;
            border-top: 1px solid #fecaca;
            color: var(--brand);
            font-size: 16px;
            font-weight: 900;
          }

          .money-row-total .money-value {
            color: var(--brand);
            font-size: 19px;
          }

          .thanks {
            padding: 14px 18px 18px;
            color: var(--muted);
          }

          .thanks strong {
            display: block;
            margin-bottom: 6px;
            color: var(--ink);
            font-size: 14px;
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

            .shipping-invoice {
              box-shadow: none;
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
          <article class="shipping-invoice">
            <header class="top-band">
              <div class="brand-box">
                <p class="brand-name">SportShoe</p>
                <p class="brand-subtitle">Giày thể thao chính hãng</p>
              </div>
              <div class="code-box">
                <span class="code-label">Mã hóa đơn</span>
                <span class="code-value">${escapeHtml(invoiceCode)}</span>
                ${isStoreOrder ? "" : '<div class="fake-barcode" aria-hidden="true"></div>'}
              </div>
            </header>

            <section class="invoice-heading">
              <div>
                <h1 class="invoice-title">Hóa đơn bán hàng</h1>
                <p class="printed-at">Ngày tạo: ${escapeHtml(createdAt)} · In lúc: ${escapeHtml(printedAt)}</p>
              </div>
              <div class="status-pill">
                <span class="status-dot"></span>
                ${escapeHtml(status)}
              </div>
            </section>

            ${isStoreOrder ? `
              <section class="section">
                <div class="section-header">
                  <h2 class="section-title">Thông tin hóa đơn</h2>
                  <span class="section-note">${escapeHtml(invoice.loaiDon || "Cửa hàng")}</span>
                </div>
                <div class="store-info-grid">
                  ${buildAddressLine("Nhân viên", invoice.maNhanVien || invoice.tenNhanVien || "Chưa gán")}
                  ${buildAddressLine("Khách hàng", getCustomerName(invoice), { hideIfEmpty: true })}
                  ${buildAddressLine("Số điện thoại", getCustomerPhone(invoice), { hideIfEmpty: true })}
                  ${buildAddressLine("Email", invoice.email, { hideIfEmpty: true })}
                  <div class="address-line address-line-wide">
                    <span>Địa chỉ</span>
                    <strong>${escapeHtml(isMeaningfulText(invoice.diaChi) ? invoice.diaChi : "Mua tại quầy")}</strong>
                  </div>
                  ${buildAddressLine("Ghi chú", invoice.ghiChu, { hideIfEmpty: true })}
                </div>
              </section>
            ` : `
              <section class="route-grid">
                <div class="route-panel">
                  <h2 class="route-title">Từ: SportShoe Store</h2>
                  ${buildAddressLine("Địa chỉ", SHOP_ADDRESS)}
                  ${buildAddressLine("Nhân viên", invoice.maNhanVien || invoice.tenNhanVien || "Chưa gán")}
                  ${buildAddressLine("Loại đơn", invoice.loaiDon || "Chưa cập nhật")}
                  ${buildAddressLine("Ghi chú", invoice.ghiChu, { hideIfEmpty: true })}
                </div>
                <div class="route-panel">
                  <h2 class="route-title">Đến: ${escapeHtml(getCustomerName(invoice))}</h2>
                  ${buildAddressLine("SĐT", getCustomerPhone(invoice), { hideIfEmpty: true })}
                  ${buildAddressLine("Email", invoice.email, { hideIfEmpty: true })}
                  ${buildAddressLine("Địa chỉ", invoice.diaChi, { fallback: "Mua tại quầy" })}
                </div>
              </section>

              <section class="order-strip">
                <div class="shipping-brand-block">
                  <div class="shipping-brand-main">
                    <img class="shipping-brand-logo" src="${escapeHtml(logoSportShoe)}" alt="SportShoe" />
                    <span class="shipping-brand-name">SportShoe</span>
                  </div>
                  <div class="shipping-carrier">
                    <img class="shipping-carrier-logo" src="${escapeHtml(logoGhn)}" alt="GHN" />
                  </div>
                </div>
                <div class="order-meta">
                  <img class="invoice-qr" src="${escapeHtml(invoiceQrUrl)}" alt="QR hóa đơn ${escapeHtml(invoiceCode)}" crossorigin="anonymous" />
                  <span>QR hóa đơn</span>
                  <strong>${escapeHtml(invoiceCode)}</strong>
                  <span>Ngày đặt hàng</span>
                  <strong>${escapeHtml(createdAt)}</strong>
                </div>
              </section>
            `}

            <section class="section">
              <div class="section-header">
                <h2 class="section-title">${isStoreOrder ? "Danh sách sản phẩm" : "Nội dung hàng"}</h2>
                <span class="section-note">Tổng số sản phẩm: ${items.length}</span>
              </div>
              <table>
                <colgroup>
                  <col style="width: 48px" />
                  <col />
                  <col style="width: 92px" />
                  <col style="width: 130px" />
                  <col style="width: 138px" />
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

            <section class="payment-section">
              <div class="section-header">
                <h2 class="section-title">Tổng kết thanh toán</h2>
              </div>
              ${buildMoneyRow("Tổng tiền hàng", tongTienHang, formatCurrency)}
              ${phiVanChuyen > 0 ? buildMoneyRow("Phí vận chuyển", phiVanChuyen, formatCurrency, { plus: true, logoSrc: logoGhn, logoAlt: "GHN" }) : ""}
              ${hasVoucher ? buildSummaryTextRow("Mã giảm giá", invoice.voucher) : ""}
              ${hasVoucher ? buildSummaryTextRow("Giá trị giảm", voucherDiscountText, { discount: true }) : ""}
              ${buildMoneyRow("Tổng thanh toán", tongCanTra, formatCurrency, { total: true })}
            </section>

            <footer class="thanks">
              <strong>Cảm ơn quý khách!</strong>
              ${isStoreOrder
                ? "Hóa đơn được phát hành bởi SportShoe. Vui lòng kiểm tra thông tin sản phẩm và tổng thanh toán trước khi rời quầy."
                : "Hóa đơn được phát hành bởi SportShoe. Vui lòng kiểm tra thông tin sản phẩm và tổng thanh toán khi nhận hàng."}
            </footer>
          </article>
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
