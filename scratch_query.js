const sql = require("mssql");

const dbConfig = {
  user: "sa",
  password: "Huy@123456",
  server: "localhost",
  database: "giay",
  options: {
    encrypt: true,
    trustServerCertificate: true,
  },
};

async function run() {
  try {
    await sql.connect(dbConfig);
    console.log("Connected to database successfully!");
    
    const hdRes = await sql.query(`
      SELECT id, ma, trang_thai, ghi_chu 
      FROM hoa_don 
      WHERE ma = 'HDMRCA7SNS109'
    `);
    console.log("HOA DON:", hdRes.recordset);
    
    if (hdRes.recordset.length > 0) {
      const hdId = hdRes.recordset[0].id;
      const vcRes = await sql.query(`
        SELECT id, hoa_don_id, ghi_chu, ly_do_giao_hang_that_bai 
        FROM van_chuyen 
        WHERE hoa_don_id = ${hdId}
      `);
      console.log("VAN CHUYEN:", vcRes.recordset);
      
      const lsRes = await sql.query(`
        SELECT id, trang_thai, ghi_chu, ngay_tao 
        FROM lich_su_hoa_don 
        WHERE hoa_don_id = ${hdId}
        ORDER BY ngay_tao DESC
      `);
      console.log("LICH SU HOA DON:", lsRes.recordset);
    }
  } catch (err) {
    console.error("Error: ", err);
  } finally {
    await sql.close();
  }
}

run();
