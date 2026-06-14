const sql = require('mssql');

const config = {
    user: 'sa',
    password: 'TungAnh@123456',
    server: 'localhost',
    database: 'giay',
    options: {
        encrypt: true,
        trustServerCertificate: true
    }
};

async function run() {
    try {
        let pool = await sql.connect(config);
        
        let deleteImages = await pool.request().query(`
            DELETE FROM hinh_anh_giay WHERE giay_chi_tiet_id IN (SELECT id FROM giay_chi_tiet WHERE giay_id IN (SELECT id FROM giay WHERE ten LIKE N'%Giày Chạy Bộ Nam Nike Air Zoopegasus 41 - Xanh Lá%'))
        `);
        console.log('HinhAnhGiay rows deleted:', deleteImages.rowsAffected);

        let deleteChiTiet = await pool.request().query(`
            DELETE FROM giay_chi_tiet WHERE giay_id IN (SELECT id FROM giay WHERE ten LIKE N'%Giày Chạy Bộ Nam Nike Air Zoopegasus 41 - Xanh Lá%')
        `);
        console.log('GiayChiTiet rows deleted:', deleteChiTiet.rowsAffected);

        let deleteGiay = await pool.request().query(`
            DELETE FROM giay WHERE ten LIKE N'%Giày Chạy Bộ Nam Nike Air Zoopegasus 41 - Xanh Lá%'
        `);
        console.log('Giay rows deleted:', deleteGiay.rowsAffected);
        
        sql.close();
    } catch (err) {
        console.error(err);
        sql.close();
    }
}

run();
