function sanitizeDigits(value) {
  return String(value ?? '').replace(/\D/g, '')
}
function formatNumber(value) {
  return Number(value || 0).toLocaleString('vi-VN')
}

let displayValue = '24.000'
let selectionStart = 3 // user clicked after "24."
let inputStr = '24.1000' // user typed "1"

let digitsBeforeCursor = 0
for (let i = 0; i < selectionStart + 1; i++) { // selectionStart was 3, + 1 character typed
  if (/\d/.test(inputStr[i])) {
    digitsBeforeCursor++;
  }
}
console.log("digitsBeforeCursor", digitsBeforeCursor)

const rawDigits = sanitizeDigits(inputStr)
const formattedValue = formatNumber(Number(rawDigits))

let newCursor = 0;
let digitsSeen = 0;
for (let i = 0; i < formattedValue.length; i++) {
  if (digitsSeen === digitsBeforeCursor) break;
  if (/\d/.test(formattedValue[i])) digitsSeen++;
  newCursor++;
}
console.log("formattedValue", formattedValue)
console.log("newCursor", newCursor)
