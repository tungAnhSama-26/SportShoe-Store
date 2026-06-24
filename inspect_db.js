const sql = require("mssql");

const dbConfig = {
  user: "sa",
  password: "Thuy@123456",
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
    
    const res = await sql.query("SELECT id, CAST(ngay_tao AS VARCHAR) as raw_ngay_tao, ngay_tao FROM thanh_toan WHERE id = 2001");
    console.log(res.recordset);
    
  } catch (err) {
    console.error("Error: ", err);
  } finally {
    await sql.close();
  }
}

run();
