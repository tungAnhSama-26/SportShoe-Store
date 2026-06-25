const fs = require('fs');
const file = 'c:/Users/tunga/Desktop/SportShoe-Store/FE/sport-shoe/src/features/ban-hang-tai-quay/useBanHangTaiQuay.js';
let content = fs.readFileSync(file, 'utf8');

// 1. Add to extraction from usePosCart
content = content.replace(
  /    giamSoLuong\r?\n  } = usePosCart\({/,
  `    giamSoLuong,\n    xoaSanPham,\n    capNhatSoLuong\n  } = usePosCart({`
);

// 2. Add to return of useBanHangTaiQuay
content = content.replace(
  /    giamSoLuong,\r?\n    dongChiTietSanPham,/,
  `    giamSoLuong,\n    xoaSanPham,\n    capNhatSoLuong,\n    dongChiTietSanPham,`
);

fs.writeFileSync(file, content, 'utf8');
console.log("Fixed useBanHangTaiQuay.js returns again!");
