function sanitizeDigits(value) {
  return String(value ?? '').replace(/\D/g, '')
}
function formatNumber(value) {
  return Number(value || 0).toLocaleString('vi-VN')
}
function simulate(inputStr) {
  let val = ''
  for (let char of inputStr) {
    val = val + char
    val = formatNumber(Number(sanitizeDigits(val)))
  }
  return val
}
console.log(simulate('24000'))
