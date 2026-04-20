function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function buildCell(value, tag = "td") {
  return `<${tag} style="border:1px solid #dbeafe;padding:8px 12px;text-align:left;vertical-align:top;">${escapeHtml(value)}</${tag}>`;
}

export function exportRowsToExcel({ filename, sheetName = "Sheet1", columns = [], rows = [] }) {
  if (!rows.length || !columns.length) {
    return false;
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

  const html = `
    <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel">
      <head>
        <meta charset="UTF-8" />
        <meta name="ProgId" content="Excel.Sheet" />
        <meta name="Generator" content="Codex" />
        <title>${escapeHtml(sheetName)}</title>
      </head>
      <body>
        <table>
          ${headerRow}
          ${bodyRows}
        </table>
      </body>
    </html>
  `;

  const blob = new Blob([`\ufeff${html}`], {
    type: "application/vnd.ms-excel;charset=utf-8;",
  });

  const link = document.createElement("a");
  const safeName = filename.endsWith(".xls") ? filename : `${filename}.xls`;
  link.href = URL.createObjectURL(blob);
  link.download = safeName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(link.href);

  return true;
}
