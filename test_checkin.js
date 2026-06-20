const http = require('http');
const req = http.request('http://localhost:8080/api/admin/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' }
}, (res) => {
  let body = '';
  res.on('data', d => body += d);
  res.on('end', () => {
    const json = JSON.parse(body);
    console.log("Login res:", json);
    if (!json.data || !json.data.token) return;
    const token = json.data.token;
    
    // Now call check-in
    const checkInReq = http.request('http://localhost:8080/api/admin/cham-cong/check-in', {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      }
    }, (res2) => {
      let body2 = '';
      res2.on('data', d => body2 += d);
      res2.on('end', () => {
        console.log("Checkin status:", res2.statusCode);
        console.log("Checkin res:", body2);
      });
    });
    checkInReq.write(JSON.stringify({ nhanVienId: json.data.id }));
    checkInReq.end();
  });
});
req.write(JSON.stringify({ email: "admin@example.com", matKhau: "admin123" }));
req.end();
