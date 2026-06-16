const fs = require('fs');
const path = require('path');

const baseDir = 'C:\\Users\\tunga\\Desktop\\SportShoe-Store\\BE\\server\\src\\main\\java\\com\\example\\server';
const reposDir = path.join(baseDir, 'repository');
const servicesDir = path.join(baseDir, 'core', 'admin', 'quanLyDanhMuc');

const attributes = [
    ['ChatLieuGiay', 'Ten', 'String'],
    ['CoGiay', 'Ten', 'String'],
    ['CongNgheDem', 'Ten', 'String'],
    ['DeGiay', 'Ten', 'String'],
    ['KichCo', 'GiaTri', 'String'],
    ['LoaiGiay', 'Ten', 'String'],
    ['MauSac', 'Ten', 'String'],
    ['ThuongHieu', 'Ten', 'String'],
    ['TrongLuong', 'GiaTri', 'Integer']
];

for (const [entity, field, type] of attributes) {
    const repoFile = path.join(reposDir, `${entity}Repository.java`);
    if (fs.existsSync(repoFile)) {
        let content = fs.readFileSync(repoFile, 'utf8');
        const findMethod = field === 'GiaTri' && entity === 'TrongLuong' 
            ? `Optional<${entity}> findByGiaTri(Integer giaTri);` 
            : `Optional<${entity}> findBy${field}IgnoreCase(${type} ${field.toLowerCase()});`;
            
        if (!content.includes('java.util.Optional')) {
            content = content.replace('public interface', 'import java.util.Optional;\n\npublic interface');
        }
        
        const methodSignature = findMethod.split('(')[0];
        if (!content.includes(methodSignature)) {
            content = content.replace(/}\s*$/, `    ${findMethod}\n}`);
            fs.writeFileSync(repoFile, content);
            console.log(`Updated ${entity}Repository`);
        }
    }
}
