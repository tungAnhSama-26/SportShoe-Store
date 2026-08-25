import test from 'node:test';
import assert from 'node:assert/strict';
import { khoangGioGiaoNhau, taoGoiYCaTiepTheo, tinhThoiLuongCa } from '../src/utils/ca-lam.js';

test('tu dong dien gio bat dau bang gio ket thuc cua ca truoc do', () => {
  const goiY = taoGoiYCaTiepTheo([
    { id: 'sang', ten: 'Ca sang', gioBatDau: '08:00', gioKetThuc: '12:00', trangThai: true },
    { id: 'chieu', ten: 'Ca chieu', gioBatDau: '13:00', gioKetThuc: '17:00', trangThai: true },
  ]);

  assert.equal(goiY.gioBatDau, '17:00');
  assert.equal(goiY.gioKetThuc, '21:00');
  assert.equal(goiY.tuCa.id, 'chieu');
});

test('giu thoi luong bang thoi luong ca truoc do', () => {
  const goiY = taoGoiYCaTiepTheo([
    { id: 'ca1', ten: 'Ca 1', gioBatDau: '07:30', gioKetThuc: '11:45', trangThai: true },
  ]);

  assert.equal(goiY.gioBatDau, '11:45');
  assert.equal(goiY.gioKetThuc, '16:00');
});

test('ca truoc 6 tieng thi ca moi cung tu dong 6 tieng', () => {
  const goiY = taoGoiYCaTiepTheo([
    { id: 'ca-test', ten: 'Ca test', gioBatDau: '06:00', gioKetThuc: '12:00', trangThai: true },
  ]);

  assert.equal(goiY.gioBatDau, '12:00');
  assert.equal(goiY.gioKetThuc, '18:00');
});

test('goi y ca qua dem khi ca tiep theo vuot qua nua dem', () => {
  const goiY = taoGoiYCaTiepTheo([
    { id: 'toi', ten: 'Ca toi', gioBatDau: '20:00', gioKetThuc: '23:00', trangThai: true },
  ]);

  assert.equal(goiY.tuCa.id, 'toi');
  assert.equal(goiY.gioBatDau, '23:00');
  assert.equal(goiY.gioKetThuc, '02:00');
});

test('dien gio mac dinh cho ca dau tien khi chua co ca truoc do', () => {
  const goiY = taoGoiYCaTiepTheo([]);

  assert.equal(goiY.tuCa, null);
  assert.equal(goiY.gioBatDau, '08:00');
  assert.equal(goiY.gioKetThuc, '12:00');
});

test('tinh dung thoi luong ca qua dem', () => {
  assert.equal(tinhThoiLuongCa('23:00', '06:00'), 7 * 60);
});

test('phat hien trung gio voi ca qua dem', () => {
  assert.equal(khoangGioGiaoNhau('23:00', '06:00', '05:00', '08:00'), true);
  assert.equal(khoangGioGiaoNhau('23:00', '06:00', '08:00', '12:00'), false);
});
