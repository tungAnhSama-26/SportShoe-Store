const fs = require('fs');
const file = 'c:/Users/tunga/Desktop/SportShoe-Store/FE/sport-shoe/src/features/ban-hang-tai-quay/useBanHangTaiQuay.js';
let content = fs.readFileSync(file, 'utf8');

// 1. Remove manual resets
content = content.replace(/^\s*pageError\.value = "";\s*$/gm, '');
content = content.replace(/^\s*successMessage\.value = "";\s*$/gm, '');
content = content.replace(/^\s*setTimeout\(\(\) => \{ successMessage\.value = ""; \}, 3000\);\s*$/gm, '');

// 2. Replace assignments
content = content.replace(/pageError\.value = error instanceof Error \? error\.message : "([^"]+)";/g, 'showError(error instanceof Error ? error.message : "$1");');
content = content.replace(/pageError\.value = (error instanceof Error[^;]+);/g, 'showError($1);');
content = content.replace(/pageError\.value = ([^;]+);/g, 'showError($1);');
content = content.replace(/successMessage\.value = ([^;]+);/g, 'showSuccess($1);');

// 3. Remove watchers entirely (match them precisely)
const watchPageErrorRegex = /watch\(pageError, \(message\) => \{\s*if \(!message\) \{\s*return;\s*\}\s*showError\(message\);\s*\}\);\s*/;
content = content.replace(watchPageErrorRegex, '');

const watchSuccessMessageRegex = /watch\(successMessage, \(message\) => \{\s*if \(!message\) \{\s*return;\s*\}\s*showToastSuccess\(message\);\s*\}\);\s*/;
content = content.replace(watchSuccessMessageRegex, '');

// 4. Remove refs
content = content.replace(/const pageError = ref\(""\);\s*\n/, '');
content = content.replace(/const successMessage = ref\(""\);\s*\n/, '');
content = content.replace(/,\s*pageError\s*\n/g, '\n');
content = content.replace(/,\s*successMessage\s*\n/g, '\n');
content = content.replace(/\s*pageError,?\s*\n/g, '\n');
content = content.replace(/\s*successMessage,?\s*\n/g, '\n');

// 5. Update themBienTheDangChon
const oldThemBienThe = `  function themBienTheDangChon() {
    if (!selectedVariant.value) {
      showError("Vui lòng chọn màu sắc và kích cỡ phù hợp");
      return;
    }
    const success = themSanPham(selectedVariant.value, selectedQuantity.value);
    if (success) {
      dongChiTietSanPham();
      showToastSuccess(\`Đã thêm \${selectedQuantity.value} sản phẩm vào hóa đơn\`);
    }
  }`;

const newThemBienThe = `  function themBienTheDangChon() {
    if (!selectedVariant.value) {
      showError("Vui lòng chọn màu sắc và kích cỡ phù hợp");
      return;
    }
    const success = themSanPham(selectedVariant.value, selectedQuantity.value);
    if (success === "added") {
      dongChiTietSanPham();
      showSuccess(\`Đã thêm \${selectedQuantity.value} sản phẩm vào hóa đơn\`);
    } else if (success === "incremented") {
      dongChiTietSanPham();
      showSuccess(\`Đã cộng thêm \${selectedQuantity.value} sản phẩm vào giỏ hàng\`);
    } else if (success === "added_different_price") {
      dongChiTietSanPham();
      showWarning(\`Sản phẩm đã có trong giỏ hàng nhưng với giá khác, nên được thêm thành dòng mới.\`);
    }
  }`;

content = content.replace(oldThemBienThe, newThemBienThe);

// 6. Fix showToastSuccess to showSuccess
content = content.replace(/showToastSuccess/g, 'showSuccess');

fs.writeFileSync(file, content, 'utf8');
