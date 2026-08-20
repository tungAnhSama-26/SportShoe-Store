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

  popup.document.open();
  popup.document.write(`
    <!doctype html>
    <html lang="vi">
      <head>
        <meta charset="UTF-8" />
        <title>${escapeHtml(filename)}</title>
        <style>
          :root {
            --brand: #000000;
            --brand-dark: #000000;
            --brand-soft: #f8fafc;
            --ink: #0f172a;
            --muted: #64748b;
            --line: #cbd5e1;
            --line-strong: #000000;
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
            background: #ffffff;
            color: var(--ink);
            font-family: Arial, Helvetica, sans-serif;
            font-size: 12px;
            line-height: 1.4;
            padding: 0;
          }

          .invoice-page {
            width: 100%;
            max-width: 480px;
            margin: 0 auto;
            background: #ffffff;
            padding: 0;
          }

          .shipping-invoice {
            overflow: hidden;
            border: 1px solid var(--line-strong);
            border-radius: var(--radius);
            background: #ffffff;
            box-shadow: none;
          }

          .top-band {
            display: grid;
            grid-template-columns: 1fr 245px;
            background: #000000;
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
            color: #cbd5e1;
            font-size: 11px;
          }

          .code-box {
            padding: 12px 16px;
            text-align: right;
          }

          .code-label {
            display: block;
            color: #cbd5e1;
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

          .invoice-heading {
            display: grid;
            grid-template-columns: 1fr auto;
            gap: 16px;
            align-items: start;
            background: #f8fafc;
            border-bottom: 1px solid var(--line);
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
            grid-template-columns: 80px minmax(0, 1fr);
          }

          .store-info-grid .address-line-wide {
            grid-column: 1 / -1;
          }

          .route-panel {
            min-height: 110px;
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
            grid-template-columns: 50px minmax(0, 1fr);
            gap: 6px;
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
            align-items: center;
            padding: 12px;
          }

          .invoice-qr {
            width: 90px;
            height: 90px;
            object-fit: contain;
          }

          .section {
            padding: 16px 18px;
            border-bottom: 1px solid var(--line);
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

          .shopee-content-list {
            font-size: 12px;
            line-height: 1.5;
            margin-top: 8px;
          }

          .payment-section {
            padding: 16px 18px;
            border-bottom: 1px solid var(--line);
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
            color: #000000;
          }

          .money-row-total {
            margin-top: 8px;
            padding-top: 13px;
            border-top: 1px solid #000000;
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
              font-size: 11px;
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

            .route-panel {
              min-height: auto !important;
              padding: 8px 12px !important;
            }

            .store-info-grid {
              gap: 4px 16px !important;
            }

            .address-line {
              margin-top: 4px !important;
            }

            .order-strip {
              grid-template-columns: 1fr 120px !important;
            }

            .shipping-brand-block {
              padding: 8px !important;
              min-height: auto !important;
            }

            .shipping-brand-name {
              font-size: 28px !important;
            }

            .shipping-brand-logo {
              width: 60px !important;
              height: 48px !important;
            }

            .order-meta {
              padding: 4px !important;
            }

            .invoice-qr {
              width: 72px !important;
              height: 72px !important;
              margin: 0 auto !important;
            }

            .section {
              padding: 8px 12px !important;
            }

            .section-header {
              margin-bottom: 6px !important;
            }

            .payment-section {
              padding: 8px 12px !important;
            }

            .money-row {
              padding: 4px 0 !important;
            }

            .money-row-total {
              margin-top: 4px !important;
              padding-top: 8px !important;
            }

            .thanks {
              padding: 6px 12px !important;
            }
          }

          @page {
            size: A4;
            margin: 6mm;
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
              </div>
            </header>

            <section class="invoice-heading">
              <div>
                <h1 class="invoice-title">Hóa đơn bán hàng</h1>
                <p class="printed-at">Ngày tạo: ${escapeHtml(createdAt)} · In lúc: ${escapeHtml(printedAt)}</p>
              </div>
            </section>

            ${isStoreOrder ? `
              <section class="section">
                <div class="section-header">
                  <h2 class="section-title">Thông tin hóa đơn</h2>
                </div>
                <div class="store-info-grid">
                  ${buildAddressLine("Khách hàng", getCustomerName(invoice), { hideIfEmpty: true })}
                  ${buildAddressLine("Số điện thoại", getCustomerPhone(invoice), { hideIfEmpty: true })}
                  ${buildAddressLine("Email", invoice.email, { hideIfEmpty: true })}
                  <div class="address-line address-line-wide">
                    <span>Địa chỉ</span>
                    <strong>${escapeHtml(isMeaningfulText(customerAddress) ? customerAddress : "Mua tại quầy")}</strong>
                  </div>
                  ${buildAddressLine("Ghi chú", invoice.ghiChu, { hideIfEmpty: true })}
                </div>
              </section>
            ` : `
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
            `}

            <section class="section">
              <div class="section-header">
                <h2 class="section-title">${isStoreOrder ? "Danh sách sản phẩm" : "Nội dung hàng"}</h2>
                <span class="section-note">Tổng số sản phẩm: ${items.length}</span>
              </div>
              <div class="shopee-content-list">
                ${items.map((item, idx) => `
                  <div style="margin-bottom: 6px; display: flex; align-items: flex-start; gap: 6px;">
                    <span>${idx + 1}.</span>
                    <div>
                      <strong>${escapeHtml(item.tenSanPham || "-")}</strong>
                      <span style="color: #475569; font-size: 11px;">
                        ${escapeHtml([item.mauSac, item.kichCo].filter(Boolean).join(" / ") ? `(${[item.mauSac, item.kichCo].filter(Boolean).join(" / ")})` : "")}
                      </span>
                      - Đơn giá: <strong>${escapeHtml(formatCurrency(item.donGia || 0))}</strong> - SL: <strong>${escapeHtml(item.soLuong || 0)}</strong>
                    </div>
                  </div>
                `).join('')}
              </div>
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
        </script>
      </body>
    </html>
  `);
  popup.document.close();
  return true;
}
