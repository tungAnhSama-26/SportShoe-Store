import test from 'node:test';
import assert from 'node:assert/strict';
import {
  layCauHinhTrangThaiDonHang,
  layViTriTienTrinhDonHang,
} from '../src/utils/order-status.js';

test('map đúng vị trí các trạng thái trong tiến trình giao hàng', () => {
  assert.equal(layViTriTienTrinhDonHang(1), 1);
  assert.equal(layViTriTienTrinhDonHang(9), 2);
  assert.equal(layViTriTienTrinhDonHang(2), 3);
  assert.equal(layViTriTienTrinhDonHang(3), 4);
  assert.equal(layViTriTienTrinhDonHang(4), 5);
  assert.equal(layViTriTienTrinhDonHang(5), 6);
});

test('các trạng thái kết thúc bất thường dùng khối thông báo riêng', () => {
  for (const trangThai of [6, 7, 8, 10]) {
    const cauHinh = layCauHinhTrangThaiDonHang(trangThai);
    assert.equal(cauHinh.hienStepper, false);
    assert.ok(cauHinh.tieuDe);
    assert.ok(cauHinh.moTa);
  }
});

test('mã trạng thái không xác định không làm vỡ stepper', () => {
  assert.equal(layViTriTienTrinhDonHang(999), 0);
  assert.equal(layCauHinhTrangThaiDonHang(999).hienStepper, false);
});
