`const fs = require('fs');
const file = 'c:/Users/tunga/Desktop/SportShoe-Store/FE/sport-shoe/src/features/ban-hang-tai-quay/useBanHangTaiQuay.js';
let content = fs.readFileSync(file, 'utf8');

content = content.replace(/^\s*pageError\.value = "";\s*$/gm, '');
content = content.replace(/^\s*successMessage\.value = "";\s*$/gm, '');
content = content.replace(/^\s*setTimeout\(\(\) => \{ successMessage\.value = ""; \}, 3000\);\s*$/gm, '');
content = content.replace(/pageError\.value = error instanceof Error \? error\.message : "([^"]+)";/g, 'showError(error instanceof Error ? error.message : "$1");');
content = content.replace(/pageError\.value = (error instanceof Error[^;]+);/g, 'showError($1);');
content = content.replace(/pageError\.value = ([^;]+);/g, 'showError($1);');
content = content.replace(/successMessage\.value = ([^;]+);/g, 'showSuccess($1);');

content = content.replace(/watch\(pageError[\s\S]*?pageError\.value = "";\s*}\);\s*/g, '');
content = content.replace(/watch\(successMessage[\s\S]*?successMessage\.value = "";\s*}\);\s*/g, '');

content = content.replace(/\s*pageError,?\s*\n/g, '\n');
content = content.replace(/\s*successMessage,?\s*\n/g, '\n');
content = content.replace(/const pageError = ref\(""\);\s*\n/g, '');
content = content.replace(/const successMessage = ref\(""\);\s*\n/g, '');

fs.writeFileSync(file, content, 'utf8');
