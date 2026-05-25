const fs = require('fs')
const path = require('path')

const dir = 'C:/Users/tunga/OneDrive/Desktop/SportShoe-Store/FE/sport-shoe/src/pages/admin/danh-muc'
const files = fs.readdirSync(dir).filter(f => f.endsWith('.vue'))

files.forEach(file => {
    let content = fs.readFileSync(path.join(dir, file), 'utf8')
    
    // Add confirmation
    content = content.replace(/async function handleSave\(\) \{[\s\S]*?saving\.value = true/, match => {
        if (match.includes('confirm(')) return match;
        // Determine category name
        let catName = 'thuộc tính này';
        if (file === 'KichCo.vue') catName = 'kích cỡ này';
        else if (file === 'MauSac.vue') catName = 'màu sắc này';
        else if (file === 'ChatLieuGiay.vue') catName = 'chất liệu này';
        else if (file === 'CoGiay.vue') catName = 'cổ giày này';
        else if (file === 'CongNgheDem.vue') catName = 'công nghệ đệm này';
        else if (file === 'DeGiay.vue') catName = 'đế giày này';
        else if (file === 'LoaiGiay.vue') catName = 'loại giày này';
        else if (file === 'ThuongHieu.vue') catName = 'thương hiệu này';
        else if (file === 'TrongLuong.vue') catName = 'trọng lượng này';

        return match.replace('saving.value = true', `  if (!confirm(modalMode.value === 'add' ? 'Xác nhận thêm mới ${catName}?' : 'Xác nhận lưu thay đổi ${catName}?')) return\n\n  saving.value = true`);
    })

    // Add transition
    if (content.includes('<template #modal>')) {
        content = content.replace(/<Teleport to="body">\s*<div\s+v-if="showModal"/, '<Teleport to="body">\n        <Transition name="fade">\n          <div\n            v-if="showModal"');
        content = content.replace(/<\/div>\s*<\/Teleport>/, '</div>\n        </Transition>\n      </Teleport>');
        
        // Add style
        if (!content.includes('.fade-enter-active')) {
            content += `\n<style scoped>\n.fade-enter-active,\n.fade-leave-active {\n  transition:\n    opacity 0.2s ease,\n    transform 0.2s ease;\n}\n\n.fade-enter-from,\n.fade-leave-to {\n  opacity: 0;\n  transform: translateY(-8px);\n}\n</style>\n`;
        }
    }

    fs.writeFileSync(path.join(dir, file), content, 'utf8')
})
console.log("Done");
