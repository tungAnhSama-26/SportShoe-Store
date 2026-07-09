const ROLE_ADMIN = 'Quản trị viên';
const ROLE_STAFF = 'Nhân viên';

function normalizeRole(role) {
  if (!role) return ROLE_STAFF;
  const normalized = String(role).trim().toUpperCase();
  if (normalized === '1' || normalized === 'ADMIN' || normalized === 'ROLE_ADMIN') return ROLE_ADMIN;
  if (normalized === '2' || normalized === 'STAFF' || normalized === 'EMPLOYEE' || normalized === 'ROLE_STAFF') return ROLE_STAFF;
  return role;
}

function isAdminRole(role) {
  return normalizeRole(role) === ROLE_ADMIN;
}

function buildThuChiRecord(formData, context = {}) {
  const role = normalizeRole(context.role || context.user?.vaiTro);
  const shiftId = context.shiftId || context.activeShift?.id || context.activeShift?.ma || '';
  const createdById = context.user?.id ?? context.nguoiTaoId ?? '';

  return {
    id: formData.id || `tmp-${Date.now()}`,
    loaiPhieu: formData.loaiPhieu || 'THU',
    maPhieu: formData.maPhieu || '',
    thoiGian: formData.thoiGian || new Date().toISOString(),
    hạngMục: formData.hangMuc || formData.hạngMục || '',
    soTien: Number(formData.soTien || 0),
    hinhThuc: formData.hinhThuc || 'Tiền mặt',
    nguoiNhan: formData.nguoiNhan || '',
    ghiChu: formData.ghiChu || '',
    nguoiTaoId: createdById,
    nguoiTaoTen: context.user?.hoTen || context.nguoiTaoTen || '',
    shiftId,
    trangThai: role === ROLE_ADMIN ? (formData.trangThai || 'Đã duyệt') : 'Đã xác nhận',
    canEdit: isAdminRole(role),
    canDelete: isAdminRole(role),
  };
}

function filterThuChiRecords(records = [], filters = {}, role, userId, shiftId) {
  const normalizedRole = normalizeRole(role);
  const normalizedUserId = String(userId ?? '');
  const normalizedShiftId = String(shiftId ?? '');

  return records.filter((record) => {
    if (isAdminRole(normalizedRole)) {
      return true;
    }

    const matchesUser = String(record.nguoiTaoId || '') === normalizedUserId;
    const matchesShift = String(record.shiftId || '') === normalizedShiftId;
    return matchesUser && matchesShift;
  });
}

export {
  buildThuChiRecord,
  filterThuChiRecords,
  isAdminRole,
  normalizeRole,
};
