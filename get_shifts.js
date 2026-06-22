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
    const result1 = await sql.query("SELECT * FROM ca_lam");
    console.log("Shifts in ca_lam:");
    console.log(result1.recordset);

    const result2 = await sql.query("SELECT TOP 10 * FROM lich_lam_viec ORDER BY id DESC");
    console.log("Latest schedules in lich_lam_viec:");
    console.log(result2.recordset);
  } catch (err) {
    console.error("Error: ", err);
  } finally {
    await sql.close();
  }
}

run();
