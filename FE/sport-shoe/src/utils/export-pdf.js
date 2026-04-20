function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function buildCell(value, tag = "td") {
  return `<${tag}>${escapeHtml(value)}</${tag}>`;
}

export function printRowsToPdf({ title, subtitle = "", columns = [], rows = [], filename = "document" }) {
  if (!columns.length || !rows.length) {
    return false;
  }

  const popup = window.open("", "_blank", "noopener,noreferrer,width=1100,height=800");
  if (!popup) {
    throw new Error("Trình duyệt đang chặn cửa sổ in PDF.");
  }

  const headerRow = `<tr>${columns.map((column) => buildCell(column.label, "th")).join("")}</tr>`;
  const bodyRows = rows
    .map((row, index) => {
      const cells = columns.map((column) => {
        const value = typeof column.value === "function"
          ? column.value(row, index)
          : row?.[column.key];
        return buildCell(value);
      });
      return `<tr>${cells.join("")}</tr>`;
    })
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
            margin: 32px;
            color: #0f172a;
          }
          .header {
            margin-bottom: 20px;
          }
          h1 {
            margin: 0 0 6px;
            font-size: 26px;
          }
          .subtitle {
            margin: 0;
            color: #475569;
            font-size: 14px;
          }
          table {
            width: 100%;
            border-collapse: collapse;
            font-size: 13px;
          }
          th,
          td {
            border: 1px solid #dbeafe;
            padding: 10px 12px;
            text-align: left;
            vertical-align: top;
          }
          th {
            background: #f8fafc;
            font-weight: 700;
          }
          @page {
            size: A4 landscape;
            margin: 14mm;
          }
        </style>
      </head>
      <body>
        <div class="header">
          <h1>${escapeHtml(title)}</h1>
          ${subtitle ? `<p class="subtitle">${escapeHtml(subtitle)}</p>` : ""}
        </div>
        <table>
          <thead>${headerRow}</thead>
          <tbody>${bodyRows}</tbody>
        </table>
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
