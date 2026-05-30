const fs = require('fs');
async function test() {
  const req = await fetch('http://localhost:8080/api/v1/auth/admin/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: '1' })
  });
  const res = await req.json();
  let token = res.data?.token || res.data;
  
  if(!token) { 
    console.log('Login failed:', res); 
    return;
  }
  console.log('Logged in successfully');
  
  const patchReq = await fetch('http://localhost:8080/api/v1/admin/san-pham/bien-the/1/trang-thai', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
    body: JSON.stringify({ kichHoat: 2 })
  });
  const patchRes = await patchReq.text();
  console.log('PATCH Response:', patchReq.status, patchRes);
}
test();
