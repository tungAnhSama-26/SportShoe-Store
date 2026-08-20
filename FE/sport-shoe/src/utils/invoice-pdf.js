import logoGhn from "../constants/logoGhn";
import logoSportShoe from "../assets/logo/logoPhu.png";
import { dinhDangDiaChi } from "./dia-chi";

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
    && normalized !== "[object object]"
    && normalized !== "không áp dụng"
    && normalized !== "không có"
    && normalized !== "chưa cập nhật";
}

function getInvoiceAddress(invoice) {
  const raw = invoice?.diaChi ?? invoice?.diaChiGiaoHang;
  if (!raw) return "";
  if (typeof raw === "object") {
    return dinhDangDiaChi(raw);
  }
  const str = String(raw).trim();
  return str.toLowerCase() === "[object object]" ? "" : str;
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
  const address = normalizePlainText(getInvoiceAddress(invoice));
  const hasShippingFee = Number(invoice?.phiVanChuyen || 0) > 0;
  const hasDeliveryAddress = isMeaningfulText(address) && address !== "mua tai quay";
  const isDeliveryOrder = Boolean(invoice?.giaoHang) || hasShippingFee || hasDeliveryAddress;

  if (isDeliveryOrder) {
    return false;
  }

  const orderType = normalizePlainText(invoice?.loaiDon);
  return ["cua hang", "offline", "tai cua hang", "tai quay"].includes(orderType)
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
  const invoiceQrUrl = buildInvoiceQrUrl(invoiceCode);
  const isStoreOrder = isStoreInvoice(invoice);
  const customerAddress = getInvoiceAddress(invoice);

  const employeeName = invoice.nguoiTao || invoice.tenNhanVien || "Thu ngân";

  if (isStoreOrder) {
    // ----------------------------------------------------
    // MẪU 1: HÓA ĐƠN BÁN TẠI QUẦY (BILL NHIỆT 80MM / K80)
    // ----------------------------------------------------
    popup.document.open();
    popup.document.write(`
      <!doctype html>
      <html lang="vi">
        <head>
          <meta charset="UTF-8" />
          <title>${escapeHtml(filename)}</title>
          <link rel="preconnect" href="https://fonts.googleapis.com">
          <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
          <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
          <style>
            * {
              box-sizing: border-box;
              -webkit-print-color-adjust: exact;
              print-color-adjust: exact;
            }

            body {
              margin: 0;
              padding: 12px 0;
              background: #f1f5f9;
              color: #000000;
              font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
              font-size: 12px;
              line-height: 1.4;
              -webkit-font-smoothing: antialiased;
            }

            .receipt-wrapper {
              width: 80mm;
              max-width: 80mm;
              margin: 0 auto;
              background: #ffffff;
              padding: 12px 10px;
              border-radius: 4px;
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
            }

            .receipt-qr-top {
              text-align: center;
              margin-bottom: 8px;
            }

            .receipt-qr-top img {
              width: 80px;
              height: 80px;
              object-fit: contain;
              display: inline-block;
            }

            .receipt-header {
              text-align: center;
              margin-bottom: 4px;
            }

            .receipt-store-name {
              font-size: 16px;
              font-weight: 800;
              text-transform: uppercase;
              letter-spacing: 0.03em;
              margin: 0 0 3px;
              color: #000000;
            }

            .receipt-store-info {
              font-size: 10.5px;
              color: #333333;
              margin: 1px 0;
              line-height: 1.35;
            }

            .divider-dashed {
              border: none;
              border-top: 1px dashed #666666;
              margin: 8px 0;
            }

            .divider-solid {
              border: none;
              border-top: 1px solid #000000;
              margin: 5px 0;
            }

            .receipt-title-box {
              text-align: center;
              margin: 6px 0 8px;
            }

            .receipt-title {
              font-size: 15px;
              font-weight: 800;
              text-transform: uppercase;
              letter-spacing: 0.03em;
              margin: 0 0 3px;
              color: #000000;
            }

            .receipt-code {
              font-size: 12px;
              font-weight: 700;
              margin: 1px 0;
              color: #111111;
            }

            .receipt-date {
              font-size: 10.5px;
              color: #444444;
              margin: 1px 0;
            }

            .receipt-meta {
              font-size: 11px;
              margin: 6px 0;
            }

            .meta-row {
              display: flex;
              justify-content: space-between;
              margin: 2px 0;
            }

            .meta-label {
              color: #444444;
              font-weight: 500;
            }

            .meta-value {
              color: #000000;
              font-weight: 600;
              text-align: right;
            }

            .receipt-table {
              width: 100%;
              border-collapse: collapse;
              margin: 6px 0 4px;
              font-size: 11px;
            }

            .receipt-table thead th {
              border-top: 1px solid #000000;
              border-bottom: 1px solid #000000;
              padding: 4px 2px;
              font-weight: 700;
              color: #000000;
            }

            .receipt-table tbody td {
              padding: 5px 2px 3px;
              vertical-align: top;
            }

            .item-name {
              font-weight: 700;
              color: #000000;
              font-size: 11px;
              line-height: 1.3;
            }

            .item-variant {
              font-size: 10px;
              color: #555555;
              margin-top: 1px;
            }

            .cell-left { text-align: left; }
            .cell-center { text-align: center; }
            .cell-right { text-align: right; font-variant-numeric: tabular-nums; white-space: nowrap; }

            .receipt-summary {
              margin: 6px 0;
              font-size: 11.5px;
            }

            .summary-row {
              display: flex;
              justify-content: space-between;
              align-items: center;
              padding: 2px 0;
              color: #222222;
            }

            .summary-row.discount {
              color: #15803d;
            }

            .summary-row.total-row {
              font-size: 13.5px;
              font-weight: 800;
              padding-top: 5px;
              margin-top: 3px;
              border-top: 1px solid #000000;
              color: #000000;
            }

            .summary-row.total-row .total-amount {
              font-size: 15px;
              font-weight: 800;
            }

            .receipt-footer {
              text-align: center;
              margin-top: 10px;
              padding-top: 2px;
            }

            .thanks-text {
              font-weight: 700;
              font-size: 11.5px;
              margin: 0 0 2px;
              color: #000000;
            }

            .powered-text {
              font-size: 10px;
              color: #666666;
              margin: 0;
            }

            @media print {
              html, body {
                width: 100%;
                margin: 0;
                padding: 0;
                background: #ffffff;
              }

              .receipt-wrapper {
                width: 80mm !important;
                max-width: 80mm !important;
                margin: 0 auto !important;
                padding: 4px 6px !important;
                box-shadow: none !important;
                border-radius: 0 !important;
              }
            }

            @page {
              margin: 6mm auto;
            }
          </style>
        </head>
        <body>
          <main class="receipt-wrapper">
            <!-- 1. QR Code ở trên đầu -->
            <div class="receipt-qr-top">
              <img src="${escapeHtml(invoiceQrUrl)}" alt="QR Code Hóa đơn" />
            </div>

            <!-- 2. Thông tin cửa hàng -->
            <header class="receipt-header">
              <h1 class="receipt-store-name">SportShoe Store</h1>
              <p class="receipt-store-info">Đ/C: 163 Thái Hà, Đống Đa, Hà Nội</p>
              <p class="receipt-store-info">SĐT: 0965.852.782</p>
            </header>

            <hr class="divider-dashed" />

            <!-- 3. Tiêu đề Hóa đơn & Thông tin -->
            <section class="receipt-title-box">
              <h2 class="receipt-title">HOÁ ĐƠN THANH TOÁN</h2>
              <p class="receipt-code">Số: ${escapeHtml(invoiceCode)}</p>
              <p class="receipt-date">Ngày: ${escapeHtml(createdAt)}</p>
            </section>

            <section class="receipt-meta">
              <div class="meta-row">
                <span class="meta-label">Khách hàng:</span>
                <strong class="meta-value">${escapeHtml(getCustomerName(invoice))}</strong>
              </div>
              ${isMeaningfulText(getCustomerPhone(invoice)) ? `
                <div class="meta-row">
                  <span class="meta-label">SĐT:</span>
                  <strong class="meta-value">${escapeHtml(getCustomerPhone(invoice))}</strong>
                </div>
              ` : ""}
              <div class="meta-row">
                <span class="meta-label">Thu ngân:</span>
                <strong class="meta-value">${escapeHtml(employeeName)}</strong>
              </div>
              <div class="meta-row">
                <span class="meta-label">Hình thức:</span>
                <strong class="meta-value">Bán tại quầy</strong>
              </div>
            </section>

            <!-- 4. Bảng sản phẩm: Tên hàng | Đ.giá | SL | TT -->
            <table class="receipt-table">
              <thead>
                <tr>
                  <th class="cell-left">Tên hàng</th>
                  <th class="cell-right" style="width: 62px;">Đ.giá</th>
                  <th class="cell-center" style="width: 25px;">SL</th>
                  <th class="cell-right" style="width: 70px;">TT</th>
                </tr>
              </thead>
              <tbody>
                ${items.map((item) => {
                  const variantText = [item.mauSac, item.kichCo].filter(Boolean).join(" / ");
                  const itemTotal = Number(item.thanhTien || ((item.soLuong || 0) * (item.donGia || 0)));
                  return `
                    <tr>
                      <td class="cell-left">
                        <div class="item-name">${escapeHtml(item.tenSanPham || "-")}</div>
                        ${variantText ? `<div class="item-variant">(${escapeHtml(variantText)})</div>` : ""}
                      </td>
                      <td class="cell-right">${escapeHtml(formatCurrency(item.donGia || 0))}</td>
                      <td class="cell-center">${escapeHtml(item.soLuong || 0)}</td>
                      <td class="cell-right"><strong>${escapeHtml(formatCurrency(itemTotal))}</strong></td>
                    </tr>
                  `;
                }).join('')}
              </tbody>
            </table>

            <hr class="divider-solid" />

            <!-- 5. Tổng kết thanh toán (Chỉ 1 tổng thanh toán khi không có voucher/ship) -->
            <section class="receipt-summary">
              ${(phiVanChuyen > 0 || hasVoucher) ? `
                <div class="summary-row">
                  <span>Tổng tiền hàng</span>
                  <strong>${escapeHtml(formatCurrency(tongTienHang))}</strong>
                </div>
                ${phiVanChuyen > 0 ? `
                  <div class="summary-row">
                    <span>Phí giao hàng</span>
                    <strong>+ ${escapeHtml(formatCurrency(phiVanChuyen))}</strong>
                  </div>
                ` : ""}
                ${hasVoucher ? `
                  <div class="summary-row discount">
                    <span>Giảm giá (${escapeHtml(invoice.voucher)})</span>
                    <strong>- ${escapeHtml(formatCurrency(giamGia))}</strong>
                  </div>
                ` : ""}
              ` : ""}
              <div class="summary-row total-row">
                <span>Tổng cộng</span>
                <span class="total-amount">${escapeHtml(formatCurrency(tongCanTra))}</span>
              </div>
            </section>

            <hr class="divider-dashed" />

            <!-- 6. Chân trang -->
            <footer class="receipt-footer">
              <p class="thanks-text">Xin cám ơn, hẹn gặp lại quý khách!</p>
              <p class="powered-text">SportShoe Store</p>
            </footer>
          </main>

          <script>
            window.onload = () => {
              setTimeout(() => {
                window.focus();
                window.print();
              }, 300);
            };
            window.onafterprint = () => window.close();
          </script>
        </body>
      </html>
    `);
    popup.document.close();
    return true;
  }

  // ----------------------------------------------------
  // MẪU 2: HÓA ĐƠN MUA ONLINE / PHIẾU GIAO HÀNG (GHN / SPORTSHOE)
  // ----------------------------------------------------
  popup.document.open();
  popup.document.write(`
    <!doctype html>
    <html lang="vi">
      <head>
        <meta charset="UTF-8" />
        <title>${escapeHtml(filename)}</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
        <style>
          :root {
            --brand: #0f172a;
            --brand-dark: #020617;
            --ink: #0f172a;
            --muted: #64748b;
            --line: #e2e8f0;
            --radius: 8px;
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
            font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            font-size: 12px;
            line-height: 1.4;
            padding: 12px 0;
            -webkit-font-smoothing: antialiased;
          }

          .invoice-page {
            width: 100%;
            max-width: 440px;
            margin: 0 auto;
            background: #ffffff;
            padding: 0;
          }

          .shipping-invoice {
            overflow: hidden;
            border: 1px solid var(--line);
            border-radius: var(--radius);
            background: #ffffff;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
          }

          .top-band {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: #0f172a;
            color: #ffffff;
            padding: 10px 14px;
          }

          .brand-box {
            display: flex;
            flex-direction: column;
          }

          .brand-name {
            margin: 0;
            font-size: 16px;
            font-weight: 800;
            letter-spacing: -0.02em;
            color: #ffffff;
          }

          .brand-subtitle {
            margin: 2px 0 0;
            color: #94a3b8;
            font-size: 10px;
            font-weight: 500;
          }

          .code-box {
            text-align: right;
          }

          .code-label {
            display: block;
            color: #94a3b8;
            font-size: 9px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.05em;
          }

          .code-value {
            display: block;
            margin-top: 1px;
            font-size: 13px;
            font-weight: 800;
            letter-spacing: 0.02em;
            color: #38bdf8;
          }

          .invoice-heading {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: #f8fafc;
            border-bottom: 1px solid var(--line);
            padding: 10px 14px;
          }

          .invoice-title {
            margin: 0;
            color: #0f172a;
            font-size: 17px;
            line-height: 1.2;
            font-weight: 800;
            letter-spacing: -0.02em;
          }

          .printed-at {
            margin: 2px 0 0;
            color: var(--muted);
            font-size: 10px;
            font-weight: 500;
          }

          .order-type-badge {
            display: inline-block;
            padding: 3px 8px;
            background: #e2e8f0;
            color: #334155;
            font-size: 10px;
            font-weight: 700;
            border-radius: 12px;
            text-transform: uppercase;
            letter-spacing: 0.03em;
          }

          .route-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            border-bottom: 1px dashed var(--line);
          }

          .route-panel {
            min-height: auto;
            padding: 10px 12px;
          }

          .route-panel + .route-panel {
            border-left: 1px dashed var(--line);
          }

          .route-title {
            margin: 0 0 6px;
            color: #0f172a;
            font-size: 11.5px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.03em;
          }

          .address-line {
            display: grid;
            grid-template-columns: 48px minmax(0, 1fr);
            gap: 4px;
            margin-top: 3px;
            font-size: 11px;
            line-height: 1.35;
          }

          .address-line span {
            color: var(--muted);
            font-weight: 500;
          }

          .address-line strong {
            color: var(--ink);
            font-weight: 600;
            word-break: break-word;
          }

          .order-strip {
            display: grid;
            grid-template-columns: 1fr 110px;
            border-bottom: 1px dashed var(--line);
          }

          .shipping-brand-block {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            gap: 6px;
            min-height: auto;
            border-right: 1px dashed var(--line);
            padding: 8px 12px;
            text-align: center;
          }

          .shipping-brand-main {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
          }

          .shipping-brand-logo {
            width: 32px;
            height: 26px;
            object-fit: contain;
          }

          .shipping-brand-name {
            color: var(--brand);
            font-size: 20px;
            font-weight: 800;
            line-height: 1;
            letter-spacing: -0.02em;
          }

          .shipping-carrier {
            display: inline-flex;
            align-items: center;
            justify-content: center;
          }

          .shipping-carrier-logo {
            height: 16px;
            width: auto;
            object-fit: contain;
          }

          .order-meta {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            padding: 6px;
          }

          .invoice-qr {
            width: 65px;
            height: 65px;
            object-fit: contain;
          }

          .section {
            padding: 10px 14px;
            border-bottom: 1px solid var(--line);
          }

          .section-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 8px;
            margin-bottom: 8px;
          }

          .section-title {
            margin: 0;
            color: var(--ink);
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.03em;
          }

          .section-note {
            color: var(--muted);
            font-size: 10px;
            font-weight: 600;
          }

          .invoice-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 11px;
          }

          .invoice-table th {
            text-align: left;
            padding: 6px 4px;
            background: #f1f5f9;
            color: #475569;
            font-weight: 700;
            font-size: 10.5px;
            text-transform: uppercase;
            letter-spacing: 0.03em;
            border-top: 1px solid var(--line);
            border-bottom: 1px solid var(--line);
          }

          .invoice-table td {
            padding: 6px 4px;
            border-bottom: 1px solid #f1f5f9;
            vertical-align: top;
          }

          .invoice-table tbody tr:last-child td {
            border-bottom: none;
          }

          .product-name {
            font-weight: 700;
            color: #0f172a;
            font-size: 11.5px;
            line-height: 1.3;
          }

          .product-variant {
            margin-top: 1px;
            color: #64748b;
            font-size: 10px;
            font-weight: 500;
          }

          .payment-section {
            padding: 10px 14px;
            border-bottom: 1px solid var(--line);
            background: #ffffff;
          }

          .money-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
            padding: 4px 0;
            color: #475569;
            font-size: 11.5px;
            font-weight: 500;
          }

          .money-label-with-logo {
            display: inline-flex;
            align-items: center;
            gap: 6px;
          }

          .money-label-logo {
            height: 14px;
            width: auto;
            object-fit: contain;
          }

          .money-value {
            color: #0f172a;
            font-weight: 700;
            font-variant-numeric: tabular-nums;
            white-space: nowrap;
          }

          .money-value-discount {
            color: #16a34a;
          }

          .money-row-total {
            margin-top: 4px;
            padding: 8px 10px;
            background: #f8fafc;
            border-radius: 6px;
            border: 1px solid var(--line);
            color: #0f172a;
            font-size: 12.5px;
            font-weight: 700;
          }

          .money-row-total .money-value {
            color: #0f172a;
            font-size: 15px;
            font-weight: 800;
          }

          .thanks {
            padding: 10px 14px;
            color: var(--muted);
            font-size: 10.5px;
            text-align: center;
            background: #fafafa;
          }

          .thanks strong {
            display: block;
            margin-bottom: 2px;
            color: var(--ink);
            font-size: 12px;
            font-weight: 700;
          }

          @media print {
            html, body {
              width: 100%;
              margin: 0;
              padding: 0;
              background: #ffffff;
            }

            .invoice-page {
              width: 120mm !important;
              max-width: 120mm !important;
              margin: 0 auto !important;
              padding: 0 !important;
            }

            .shipping-invoice {
              box-shadow: none !important;
              border: 1px solid #cbd5e1 !important;
            }
          }

          @page {
            size: auto;
            margin: 6mm auto;
          }
        </style>
      </head>
      <body>
        <main class="invoice-page">
          <article class="shipping-invoice">
            <header class="top-band">
              <div class="brand-box">
                <span class="brand-name">SportShoe</span>
                <span class="brand-subtitle">Giày thể thao chính hãng</span>
              </div>
              <div class="code-box">
                <span class="code-label">Mã hóa đơn</span>
                <span class="code-value">${escapeHtml(invoiceCode)}</span>
              </div>
            </header>

            <section class="invoice-heading">
              <div>
                <h1 class="invoice-title">Hóa đơn bán hàng</h1>
                <p class="printed-at">Ngày tạo: ${escapeHtml(createdAt)} · In lúc: ${escapeHtml(printedAt)}</p>
              </div>
              <div>
                <span class="order-type-badge">Giao hàng</span>
              </div>
            </section>

            <section class="route-grid">
              <div class="route-panel">
                <h2 class="route-title">Từ: SportShoe Store</h2>
                ${buildAddressLine("Địa chỉ", SHOP_ADDRESS)}
                ${buildAddressLine("Ghi chú", invoice.ghiChu, { hideIfEmpty: true })}
              </div>
              <div class="route-panel">
                <h2 class="route-title">Đến: ${escapeHtml(getCustomerName(invoice))}</h2>
                ${buildAddressLine("SĐT", getCustomerPhone(invoice), { hideIfEmpty: true })}
                ${buildAddressLine("Địa chỉ", customerAddress, { fallback: "Mua tại quầy" })}
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
                <img class="invoice-qr" src="${escapeHtml(invoiceQrUrl)}" alt="QR" />
              </div>
            </section>

            <section class="section">
              <div class="section-header">
                <h2 class="section-title">Nội dung hàng</h2>
                <span class="section-note">Tổng số: ${items.length} món</span>
              </div>
              <table class="invoice-table">
                <thead>
                  <tr>
                    <th style="width: 28px; text-align: center;">#</th>
                    <th>Sản phẩm</th>
                    <th style="text-align: center; width: 44px;">SL</th>
                    <th style="text-align: right; width: 90px;">Đơn giá</th>
                    <th style="text-align: right; width: 100px;">Thành tiền</th>
                  </tr>
                </thead>
                <tbody>
                  ${items.map((item, idx) => {
                    const variantText = [item.mauSac, item.kichCo].filter(Boolean).join(" / ");
                    const itemTotal = Number(item.thanhTien || ((item.soLuong || 0) * (item.donGia || 0)));
                    return `
                      <tr>
                        <td style="text-align: center; color: var(--muted); font-weight: 600;">${idx + 1}</td>
                        <td>
                          <div class="product-name">${escapeHtml(item.tenSanPham || "-")}</div>
                          ${variantText ? `<div class="product-variant">Phân loại: ${escapeHtml(variantText)}</div>` : ""}
                        </td>
                        <td style="text-align: center; font-weight: 700;">${escapeHtml(item.soLuong || 0)}</td>
                        <td style="text-align: right; font-variant-numeric: tabular-nums;">${escapeHtml(formatCurrency(item.donGia || 0))}</td>
                        <td style="text-align: right; font-weight: 700; font-variant-numeric: tabular-nums;">${escapeHtml(formatCurrency(itemTotal))}</td>
                      </tr>
                    `;
                  }).join('')}
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
              Hóa đơn được phát hành bởi SportShoe. Vui lòng kiểm tra sản phẩm và tổng thanh toán khi nhận hàng.
            </footer>
          </article>
        </main>

        <script>
          window.onload = () => {
            setTimeout(() => {
              window.focus();
              window.print();
            }, 300);
          };
          window.onafterprint = () => window.close();
        </script>
      </body>
    </html>
  `);
  popup.document.close();
  return true;
}
