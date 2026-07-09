import test from 'node:test';
import assert from 'node:assert/strict';
import { buildThuChiRecord, filterThuChiRecords } from '../src/services/thu-chi.js';

test('buildThuChiRecord stores shiftId and staff default status', () => {
  const record = buildThuChiRecord(
    {
      loaiPhieu: 'CHI',
      hangMuc: 'Trả tiền ship',
      soTien: 100000,
      hinhThuc: 'Tiền mặt',
      nguoiNhan: 'Anh thợ',
      ghiChu: 'Sửa cửa',
    },
    {
      user: { id: 7, hoTen: 'Nhân viên A', vaiTro: 'Nhân viên' },
      role: 'Nhân viên',
      shiftId: 'GC123',
    },
  );

  assert.equal(record.shiftId, 'GC123');
  assert.equal(record.trangThai, 'Đã xác nhận');
  assert.equal(record.nguoiTaoId, 7);
  assert.equal(record.loaiPhieu, 'CHI');
});

test('filterThuChiRecords restricts staff view to their own shift records', () => {
  const records = [
    buildThuChiRecord({ loaiPhieu: 'THU', hangMuc: 'Thu khác', soTien: 50000 }, { user: { id: 7, hoTen: 'Nhân viên A', vaiTro: 'Nhân viên' }, role: 'Nhân viên', shiftId: 'GC123' }),
    buildThuChiRecord({ loaiPhieu: 'THU', hangMuc: 'Thu khác', soTien: 60000 }, { user: { id: 8, hoTen: 'Nhân viên B', vaiTro: 'Nhân viên' }, role: 'Nhân viên', shiftId: 'GC123' }),
    buildThuChiRecord({ loaiPhieu: 'CHI', hangMuc: 'Chi khác', soTien: 70000 }, { user: { id: 7, hoTen: 'Nhân viên A', vaiTro: 'Nhân viên' }, role: 'Nhân viên', shiftId: 'GC999' }),
  ];

  const visible = filterThuChiRecords(records, {}, 'Nhân viên', 7, 'GC123');

  assert.equal(visible.length, 1);
  assert.equal(visible[0].nguoiTaoId, 7);
  assert.equal(visible[0].shiftId, 'GC123');
});
