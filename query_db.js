const { Connection, Request } = require('tedious');

const config = {
  server: 'localhost',
  authentication: {
    type: 'default',
    options: {
      userName: 'sa',
      password: 'Yoasobi123.'
    }
  },
  options: {
    database: 'sport_shoe',
    encrypt: false,
    trustServerCertificate: true
  }
};

const connection = new Connection(config);

connection.on('connect', err => {
  if (err) {
    console.error(err);
  } else {
    executeStatement();
  }
});

connection.connect();

function executeStatement() {
  const request = new Request("SELECT dgs.id, dgs.giay_chi_tiet_id, dgs.trang_thai, dg.ma, dg.kich_hoat FROM dot_giam_gia_san_pham dgs JOIN dot_giam_gia dg ON dgs.dot_giam_gia_id = dg.id JOIN giay_chi_tiet gct ON dgs.giay_chi_tiet_id = gct.id JOIN giay g ON gct.giay_id = g.id WHERE g.ma = 'SP20'", function(err, rowCount, rows) {
    if (err) {
      console.error(err);
    }
    connection.close();
  });

  request.on('row', function(columns) {
    const row = {};
    columns.forEach(function(column) {
      row[column.metadata.colName] = column.value;
    });
    console.log(row);
  });

  connection.execSql(request);
}
