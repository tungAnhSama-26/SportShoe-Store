const http = require('http');

const loginData = JSON.stringify({
    username: 'admin@gmail.com', // Change this if different
    password: '123'
});

const req = http.request({
    hostname: 'localhost',
    port: 8080,
    path: '/api/v1/auth/admin/login',
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(loginData)
    }
}, res => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
        const json = JSON.parse(data);
        if (json.data && json.data.token) {
            const token = json.data.token;
            // Now fetch the hoa-don API
            http.get({
                hostname: 'localhost',
                port: 8080,
                path: '/api/v1/admin/hoa-don?loaiDon=' + encodeURIComponent('Tại quầy') + '&tuNgay=2026-06-23&denNgay=2026-06-23',
                headers: {
                    'Authorization': 'Bearer ' + token
                }
            }, res2 => {
                let data2 = '';
                res2.on('data', chunk => data2 += chunk);
                res2.on('end', () => {
                    console.log('STATUS:', res2.statusCode);
                    console.log('RESPONSE:', data2);
                });
            });
        } else {
            console.log('LOGIN FAILED:', data);
        }
    });
});
req.write(loginData);
req.end();
