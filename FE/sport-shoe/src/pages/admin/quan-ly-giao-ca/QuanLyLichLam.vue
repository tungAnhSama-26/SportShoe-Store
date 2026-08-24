<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  ArrowLeft,
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Download,
  MoreHorizontal,
  Plus,
  Shuffle,
  Users,
  Clock,
  X,
} from "lucide-vue-next";
import { layDanhSachNhanVien } from "../../../services/nhan-vien.js";
import {
  layLichLamViec,
  phanCa,
  xoaLichLamViec,
  xepCaTuDong,
} from "../../../services/lich-lam.js";
import { showSuccess, showError, showConfirm } from "../../../utils/alert.js";
import { getDisplayErrorMessage } from "../../../utils/error-message.js";
import { exportRowsToExcel } from "../../../utils/export-excel.js";
import { khoangGioGiaoNhau, taoGoiYCaTiepTheo, tinhThoiLuongCa } from "../../../utils/ca-lam.js";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import TimePicker24h from "../../../components/common/TimePicker24h.vue";

const route = useRoute();
const router = useRouter();

const MAX_NHAN_VIEN_MOI_CA = 3;

import { layDanhSachCaLam, normalizeShiftName } from "../../../services/ca-lam.js";

const SHIFT_COLORS = [
  { mau: "bg-emerald-500", muaNhat: "bg-emerald-50 border-emerald-200 text-emerald-700" },
  { mau: "bg-orange-400", muaNhat: "bg-orange-50 border-orange-200 text-orange-700" },
  { mau: "bg-violet-400", muaNhat: "bg-violet-50 border-violet-200 text-violet-700" },
  { mau: "bg-blue-400", muaNhat: "bg-blue-50 border-blue-200 text-blue-700" },
  { mau: "bg-rose-400", muaNhat: "bg-rose-50 border-rose-200 text-rose-700" },
  { mau: "bg-amber-400", muaNhat: "bg-amber-50 border-amber-200 text-amber-700" },
  { mau: "bg-teal-400", muaNhat: "bg-teal-50 border-teal-200 text-teal-700" }
];

const DS_CA = ref([
  {
    id: "sang",
    nhan: "Ca sáng",
    gio: "08:00 - 12:00",
    mau: "bg-emerald-500",
    muaNhat: "bg-emerald-50 border-emerald-200 text-emerald-700",
  },
  {
    id: "chieu",
    nhan: "Ca chiều",
    gio: "12:00 - 17:00",
    mau: "bg-orange-400",
    muaNhat: "bg-orange-50 border-orange-200 text-orange-700",
  },
  {
    id: "toi",
    nhan: "Ca tối",
    gio: "17:00 - 22:00",
    mau: "bg-violet-400",
    muaNhat: "bg-violet-50 border-violet-200 text-violet-700",
  },
]);

async function taiDanhSachCa() {
  try {
    const list = await layDanhSachCaLam();
    const activeList = list.filter(c => c.trangThai);
    if (activeList.length > 0) {
      DS_CA.value = activeList.map((c, idx) => {
        const colorSet = SHIFT_COLORS[idx % SHIFT_COLORS.length];
        return {
          id: c.id,
          nhan: normalizeShiftName(c.id, c.ten),
          gio: `${c.gioBatDau} - ${c.gioKetThuc}`,
          mau: colorSet.mau,
          muaNhat: colorSet.muaNhat
        };
      });
    } else {
      DS_CA.value = [];
    }
  } catch (e) {
    console.error("Không thể tải danh sách ca làm việc", e);
  }
}

// ───────── Tuần hiện tại ─────────
const ngayHienTai = ref(new Date());

function dauTuan(d) {
  const nd = new Date(d);
  const day = nd.getDay(); // 0=CN
  const diff = day === 0 ? -6 : 1 - day;
  nd.setDate(nd.getDate() + diff);
  nd.setHours(0, 0, 0, 0);
  return nd;
}

const ngayDauTuan = computed(() => dauTuan(ngayHienTai.value));

const cacNgayTrongTuan = computed(() => {
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(ngayDauTuan.value);
    d.setDate(d.getDate() + i);
    return d;
  });
});

const NHAN_TUAN = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"];

function formatNgay(d) {
  return `${String(d.getDate()).padStart(2, "0")}/${String(d.getMonth() + 1).padStart(2, "0")}`;
}

function formatTuanHienThi() {
  const dau = cacNgayTrongTuan.value[0];
  const cuoi = cacNgayTrongTuan.value[6];
  const format = (d) =>
    `${String(d.getDate()).padStart(2, "0")} tháng ${d.getMonth() + 1}, ${d.getFullYear()}`;
  return `${format(dau)} – ${format(cuoi)}`;
}

function tuanTruoc() {
  const d = new Date(ngayHienTai.value);
  d.setDate(d.getDate() - 7);
  ngayHienTai.value = d;
}

function tuanSau() {
  const d = new Date(ngayHienTai.value);
  d.setDate(d.getDate() + 7);
  ngayHienTai.value = d;
}

function homNay() {
  ngayHienTai.value = new Date();
}

// Màu avatar theo vai trò
const MAU_VAI_TRO = {
  1: "bg-primary",
  2: "bg-emerald-500",
};
function mauNenNV(vaiTro) {
  return MAU_VAI_TRO[vaiTro] ?? "bg-slate-400";
}

// Tạo viết tắt từ họ tên
function taoVietTat(hoTen) {
  const parts = (hoTen ?? "").trim().split(/\s+/);
  if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
  return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}

// Lịch demo theo vai trò (lưu vì BE chưa có bảng lịch)
function taoLichMock(vaiTro) {
  if (vaiTro === 1) return ["sang", "sang", "sang", null, "sang", null, null];
  if (vaiTro === 2)
    return [null, "chieu", "chieu", "chieu", null, "chieu", null];
  return ["toi", "toi", null, "toi", null, null, "toi"];
}

const dangTai = ref(false);
const loiTrang = ref("");
const danhSachNV = ref([]);

function formatISODate(d) {
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

async function taiDuLieuLich() {
  if (danhSachNV.value.length === 0) return;
  const tuNgay = formatISODate(cacNgayTrongTuan.value[0]);
  const denNgay = formatISODate(cacNgayTrongTuan.value[6]);
  try {
    const lichData = await layLichLamViec(tuNgay, denNgay);
    danhSachNV.value.forEach((nv) => {
      nv.lich = cacNgayTrongTuan.value.map((date) => {
        const dateStr = formatISODate(date);
        return lichData.filter(
          (l) => String(l.nhanVienId) === String(nv.id) && l.ngay === dateStr,
        ).map((item) => ({ id: item.id, ca: item.ca }));
      });
      nv.tongGio = nv.lich.flat().reduce((tong, item) => tong + soGioCa(item.ca), 0);
      nv.overtime = nv.tongGio > 20 ? nv.tongGio - 20 : 0;
    });
  } catch (e) {
    console.error(e);
    showError(getDisplayErrorMessage(e, "Không thể tải dữ liệu lịch làm việc"));
  }
}

async function taiNhanVien() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    await taiDanhSachCa();
    const ds = await layDanhSachNhanVien({ trangThai: 1 });
    danhSachNV.value = ds.map((nv) => ({
      id: String(nv.id),
      ma: nv.ma ?? "",
      ten: nv.hoTen ?? "",
      vieTat: taoVietTat(nv.hoTen ?? ""),
      chucVu: nv.tenVaiTro ?? "—",
      vaiTro: Number(nv.vaiTro) === 1 ? 1 : 2,
      hinhAnh: nv.hinhAnh ?? "",
      mauNen: mauNenNV(Number(nv.vaiTro) === 1 ? 1 : 2),
      lich: Array.from({ length: 7 }, () => []),
      tongGio: 0,
      overtime: 0,
      gioiHanOT: 5,
    }));
    await taiDuLieuLich();
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(
      e,
      "Không thể tải danh sách nhân viên",
    );
  } finally {
    dangTai.value = false;
  }
}

onMounted(taiNhanVien);

watch(ngayDauTuan, async () => {
  dangTai.value = true;
  await taiDuLieuLich();
  dangTai.value = false;
});

// ───────── Bộ lọc vai trò ─────────
const boLocVaiTro = ref(0); // 0 = tất cả
const dsVaiTro = [
  { value: 0, label: "Tất cả" },
  { value: 1, label: "Admin" },
  { value: 2, label: "Nhân viên" },
];

const employeeIdFilter = computed(() =>
  route.params.id ? String(route.params.id) : null,
);

const danhSachLocVaiTro = computed(() => {
  let list = danhSachNV.value;
  if (employeeIdFilter.value) {
    list = list.filter((nv) => nv.id === employeeIdFilter.value);
  }
  if (boLocVaiTro.value === 0) {
    return list;
  }
  return list.filter((nv) => nv.vaiTro === boLocVaiTro.value);
});

// ───────── Computed cho Board View ─────────
const lichBoard = computed(() => {
  return cacNgayTrongTuan.value.map((ngay, ngayIndex) => {
    return {
      ngay: ngay,
      ngayStr: formatISODate(ngay),
      thu: NHAN_TUAN[ngayIndex],
      cas: DS_CA.value.map((caInfo) => {
        const nhanViens = danhSachLocVaiTro.value.filter((nv) =>
          nv.lich[ngayIndex]?.some((item) => item.ca.toLowerCase() === caInfo.id.toLowerCase())
        );
        return {
          ...caInfo,
          nhanViens,
        };
      }),
    };
  });
});

// ───────── Hiển thị modal Chi tiết ca (Modal 1) ─────────
const showModalChiTietCa = ref(false);
const currentChiTietCa = ref(null);

function xemChiTietCa(day, ca) {
  currentChiTietCa.value = { day, ca };
  showModalChiTietCa.value = true;
}

// ───────── Hiển thị modal Thêm nhân viên (Modal 2) ─────────
const showModalThemCa = ref(false);
const chonNhanVienId = ref("");
const chonNgayVal = ref("");
const chonCaVal = ref("sang");

// ───────── Hiển thị modal Thêm ca làm mới (Modal 3) ─────────
const showModalTaoCa = ref(false);
const goiYCaTiepTheo = ref(null);
const formTaoCa = ref({
  tenCa: "",
  gioBatDau: "",
  gioKetThuc: "",
  moTa: ""
});

function tachGioCa(ca) {
  const [gioBatDau = "", gioKetThuc = ""] = String(ca.gio || "")
    .split("-")
    .map((item) => item.trim());
  return {
    id: ca.id,
    ten: ca.nhan,
    gioBatDau,
    gioKetThuc,
    trangThai: true,
  };
}

function moModalTaoCa() {
  goiYCaTiepTheo.value = taoGoiYCaTiepTheo(DS_CA.value.map(tachGioCa));
  formTaoCa.value = {
    tenCa: "",
    gioBatDau: goiYCaTiepTheo.value?.gioBatDau ?? "",
    gioKetThuc: goiYCaTiepTheo.value?.gioKetThuc ?? "",
    moTa: ""
  };
  showModalTaoCa.value = true;
}

function huyTaoCa() {
  showModalTaoCa.value = false;
  goiYCaTiepTheo.value = null;
}

function luuTaoCa() {
  if (!formTaoCa.value.tenCa) {
    showError("Vui lòng nhập tên ca làm.");
    return;
  }
  if (!formTaoCa.value.gioBatDau || !formTaoCa.value.gioKetThuc) {
    showError("Vui lòng chọn thời gian bắt đầu và kết thúc.");
    return;
  }
  if (formTaoCa.value.gioBatDau === formTaoCa.value.gioKetThuc) {
    showError("Giờ kết thúc không được trùng với giờ bắt đầu.");
    return;
  }

  const idMoi = "ca_" + Date.now();
  DS_CA.value.push({
    id: idMoi,
    nhan: formTaoCa.value.tenCa,
    gio: `${formTaoCa.value.gioBatDau} - ${formTaoCa.value.gioKetThuc}`,
    mauHex: "#475569",
  });

  showSuccess("Thêm ca làm mới thành công!");
  showModalTaoCa.value = false;
  goiYCaTiepTheo.value = null;
}

function taoNgayLocal(value) {
  if (value instanceof Date) {
    return new Date(value.getFullYear(), value.getMonth(), value.getDate());
  }
  if (typeof value === "string") {
    const match = value.match(/^(\d{4})-(\d{2})-(\d{2})$/);
    if (match) {
      return new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]));
    }
  }
  const date = new Date(value);
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function doiGioSangPhut(value) {
  if (!value || typeof value !== "string") return null;
  const [gio, phut] = value.trim().split(":").map(Number);
  if (!Number.isFinite(gio) || !Number.isFinite(phut)) return null;
  return gio * 60 + phut;
}

const THOI_GIAN_CA = computed(() => {
  const map = {};
  DS_CA.value.forEach((ca) => {
    const [batDauText = "", ketThucText = ""] = String(ca.gio || "")
      .split("-")
      .map((value) => value.trim());
    const batDau = doiGioSangPhut(batDauText);
    const ketThuc = doiGioSangPhut(ketThucText);
    if (batDau !== null && ketThuc !== null) {
      map[String(ca.id).toLowerCase()] = { batDau, ketThuc };
    }
  });
  return map;
});

function laCaDaKhoa(ngay, caId) {
  if (!ngay) return false;
  const date = taoNgayLocal(ngay);
  const now = new Date();
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  if (date < today) return true;
  if (date.getTime() !== today.getTime() || !caId) return false;

  const thoiGian = THOI_GIAN_CA.value[String(caId).toLowerCase()];
  if (!thoiGian) return false;

  const thoiDiemKetThuc = new Date(date);
  thoiDiemKetThuc.setHours(
    Math.floor(thoiGian.ketThuc / 60),
    thoiGian.ketThuc % 60,
    0,
    0,
  );
  if (thoiGian.ketThuc <= thoiGian.batDau) {
    thoiDiemKetThuc.setDate(thoiDiemKetThuc.getDate() + 1);
  }
  return now >= thoiDiemKetThuc;
}

function coTheThemNhanVienVaoCa(ngay, caId) {
  if (!ngay || !caId) return false;
  const thoiGian = THOI_GIAN_CA.value[String(caId).toLowerCase()];
  if (!thoiGian) return false;

  const date = taoNgayLocal(ngay);
  const thoiDiemKetThuc = new Date(date);
  thoiDiemKetThuc.setHours(
    Math.floor(thoiGian.ketThuc / 60),
    thoiGian.ketThuc % 60,
    0,
    0,
  );
  if (thoiGian.ketThuc <= thoiGian.batDau) {
    thoiDiemKetThuc.setDate(thoiDiemKetThuc.getDate() + 1);
  }

  const now = new Date();
  return now < thoiDiemKetThuc;
}

// Computed: ca hiện tại trong modal có bị khóa không
const caHienTaiBiKhoa = computed(() => {
  if (!currentChiTietCa.value) return false;
  const { day, ca } = currentChiTietCa.value;
  return laCaDaKhoa(day.ngay, ca.id);
});

const caHienTaiCoTheThemNhanVien = computed(() => {
  if (!currentChiTietCa.value) return false;
  const { day, ca } = currentChiTietCa.value;
  return coTheThemNhanVienVaoCa(day.ngay, ca.id);
});

function moModalThemCa() {
  chonNhanVienId.value = "";
  if (currentChiTietCa.value) {
    // Từ modal chi tiết ca — không cần check vì nút đã ẩn khi ca bị khóa
  } else {
    const firstAvailableDay = cacNgayTrongTuan.value.find(d =>
      DS_CA.value.some(ca => coTheThemNhanVienVaoCa(d, ca.id))
    ) || cacNgayTrongTuan.value[0];
    const caDangDienRa = DS_CA.value.find(ca => coTheThemNhanVienVaoCa(firstAvailableDay, ca.id));
    chonNgayVal.value = formatISODate(firstAvailableDay);
    chonCaVal.value = caDangDienRa?.id || DS_CA.value[0]?.id || 'sang';
  }
  showModalChiTietCa.value = false;
  showModalThemCa.value = true;
}

function moModalThemCaTuHeader() {
  currentChiTietCa.value = null;
  moModalThemCa();
}

function huyThemCa() {
  showModalThemCa.value = false;
  if (currentChiTietCa.value) {
    showModalChiTietCa.value = true;
  }
}

const nhanVienKhaDung = computed(() => {
  const ngayStr = currentChiTietCa.value?.day?.ngayStr || chonNgayVal.value;
  const caId = currentChiTietCa.value?.ca?.id || chonCaVal.value;
  if (!ngayStr || !caId) return danhSachLocVaiTro.value;
  return danhSachLocVaiTro.value.filter((nv) =>
    !nhanVienDaCoHoacChongCa(nv, ngayStr, caId)
  );
});

async function luuCa() {
  if (!chonNhanVienId.value) {
    showError("Vui lòng chọn nhân viên!");
    return;
  }

  let ngayStr;
  let caId;

  if (currentChiTietCa.value) {
    const { day, ca } = currentChiTietCa.value;
    ngayStr = day.ngayStr;
    caId = ca.id;
    if (ca.nhanViens.length >= MAX_NHAN_VIEN_MOI_CA) {
      showError(`Ca này đã đủ tối đa ${MAX_NHAN_VIEN_MOI_CA} nhân viên.`);
      return;
    }
  } else {
    ngayStr = chonNgayVal.value;
    caId = chonCaVal.value;
    // Đếm số lượng nhân viên đã xếp ca này vào ngày này
    const ngayIdx = cacNgayTrongTuan.value.findIndex(d => formatISODate(d) === ngayStr);
    if (ngayIdx >= 0) {
      const dem = danhSachLocVaiTro.value.filter((nv) =>
        nv.lich[ngayIdx]?.some((item) => item.ca.toLowerCase() === caId.toLowerCase())
      ).length;
      if (dem >= MAX_NHAN_VIEN_MOI_CA) {
        showError(`Ca này đã đủ tối đa ${MAX_NHAN_VIEN_MOI_CA} nhân viên.`);
        return;
      }
    }
  }

  if (!coTheThemNhanVienVaoCa(ngayStr, caId)) {
    showError("Chỉ có thể thêm nhân viên vào ca hiện tại hoặc ca tương lai.");
    return;
  }

  dangTai.value = true;
  try {
    await phanCa({
      nhanVienId: chonNhanVienId.value,
      ngay: ngayStr,
      caLamId: caId,
    });
    showSuccess("Thêm nhân viên vào ca thành công!");
    
    showModalThemCa.value = false;
    await taiDuLieuLich();
    
    if (currentChiTietCa.value) {
      const { day, ca } = currentChiTietCa.value;
      const updatedDay = lichBoard.value.find(d => d.ngayStr === day.ngayStr);
      if (updatedDay) {
        const updatedCa = updatedDay.cas.find(c => c.id.toLowerCase() === ca.id.toLowerCase());
        if (updatedCa) {
          currentChiTietCa.value = { day: updatedDay, ca: updatedCa };
          showModalChiTietCa.value = true;
        }
      }
    }
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể lưu ca làm việc"));
  } finally {
    dangTai.value = false;
  }
}

async function xoaCa(nhanVien) {
  if (!currentChiTietCa.value) return;
  const { day, ca } = currentChiTietCa.value;



  const xacNhan = await showConfirm(
    `Bạn có chắc chắn muốn xóa ca làm việc của ${nhanVien.ten} ngày ${formatNgay(day.ngay)}?`,
    "Xác nhận xóa ca",
  );
  if (!xacNhan) return;
  
  dangTai.value = true;
  try {
    const ngayIndex = cacNgayTrongTuan.value.findIndex((ngay) => formatISODate(ngay) === day.ngayStr);
    const lichCanXoa = nhanVien.lich[ngayIndex]?.find(
      (item) => item.ca.toLowerCase() === ca.id.toLowerCase(),
    );
    if (!lichCanXoa?.id) {
      throw new Error("Không tìm thấy lịch làm việc cần xóa");
    }
    await xoaLichLamViec(lichCanXoa.id);
    showSuccess("Xóa ca làm việc thành công!");
    
    await taiDuLieuLich();
    
    // Cập nhật lại currentChiTietCa
    const updatedDay = lichBoard.value.find(d => d.ngayStr === day.ngayStr);
    if (updatedDay) {
      const updatedCa = updatedDay.cas.find(c => c.id.toLowerCase() === ca.id.toLowerCase());
      if (updatedCa) {
        currentChiTietCa.value = { day: updatedDay, ca: updatedCa };
      }
    }
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể xóa ca làm việc"));
  } finally {
    dangTai.value = false;
  }
}

async function xepCaDong() {
  const tuNgay = formatISODate(cacNgayTrongTuan.value[0]);
  const denNgay = formatISODate(cacNgayTrongTuan.value[6]);
  const xacNhan = await showConfirm(
    `Bạn có chắc muốn tự động xếp ca cho tuần từ ngày ${formatNgay(cacNgayTrongTuan.value[0])} đến ${formatNgay(cacNgayTrongTuan.value[6])}? Các ca làm hiện tại trong tuần này sẽ bị ghi đè.`,
    "Xác nhận xếp ca tự động",
  );
  if (!xacNhan) return;
  dangTai.value = true;
  try {
    await xepCaTuDong(tuNgay, denNgay);
    showSuccess("Xếp ca tự động thành công!");
    await taiDuLieuLich();
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể xếp ca tự động"));
  } finally {
    dangTai.value = false;
  }
}

function tenCaXuatExcel(dsLich) {
  if (!Array.isArray(dsLich) || dsLich.length === 0) return "Nghỉ";
  return dsLich.map((item) => {
    const thongTinCa = layThongTinCa(item.ca);
    return thongTinCa ? `${thongTinCa.nhan} (${thongTinCa.gio})` : item.ca;
  }).join("; ");
}

function tenFileXuatExcel() {
  const tuNgay = formatISODate(cacNgayTrongTuan.value[0]);
  const denNgay = formatISODate(cacNgayTrongTuan.value[6]);
  return `lich-lam-viec-${tuNgay}_den_${denNgay}.xls`;
}

function xuatExcel() {
  const rows = danhSachLocVaiTro.value;
  if (!rows.length) {
    showError("Không có dữ liệu để xuất Excel.");
    return;
  }

  const exported = exportRowsToExcel({
    filename: tenFileXuatExcel(),
    sheetName: "Lịch làm việc",
    columns: [
      { label: "STT", value: (_row, index) => index + 1 },
      { label: "Nhân viên", value: (row) => row.hoTen },
      { label: "Vai trò", value: (row) => row.chucVu },
      ...cacNgayTrongTuan.value.map((ngay, index) => ({
        label: `${NHAN_TUAN[index]} ${formatNgay(ngay)}`,
        value: (row) => tenCaXuatExcel(row.lich[index]),
      })),
      { label: "Tổng giờ", value: (row) => `${row.tongGio}h` },
      {
        label: "Tăng ca",
        value: (row) => `${row.overtime}h / ${row.gioiHanOT}h`,
      },
    ],
    rows,
  });

  if (exported) {
    showSuccess("Xuất Excel thành công!");
  } else {
    showError("Không có dữ liệu để xuất Excel.");
  }
}

// ───────── Helpers ─────────
function layThongTinCa(id) {
  if (!id) return null;
  const found = DS_CA.value.find((c) => c.id.toLowerCase() === id.toLowerCase());
  if (found) {
    return {
      ...found,
      nhan: normalizeShiftName(found.id, found.nhan)
    };
  }
  return {
    id: id,
    nhan: normalizeShiftName(id, id),
    gio: "—",
    mau: "bg-slate-400",
    muaNhat: "bg-slate-50 border-slate-200 text-slate-700"
  };
}

function soGioCa(id) {
  const ca = layThongTinCa(id);
  if (!ca?.gio || !ca.gio.includes("-")) return 0;
  const [batDau, ketThuc] = ca.gio.split("-").map((value) => value.trim());
  return (tinhThoiLuongCa(batDau, ketThuc) ?? 0) / 60;
}

function caChongGio(caThuNhatId, caThuHaiId) {
  const caThuNhat = layThongTinCa(caThuNhatId);
  const caThuHai = layThongTinCa(caThuHaiId);
  if (!caThuNhat?.gio?.includes("-") || !caThuHai?.gio?.includes("-")) return false;

  const [batDauMot, ketThucMot] = caThuNhat.gio.split("-").map((value) => value.trim());
  const [batDauHai, ketThucHai] = caThuHai.gio.split("-").map((value) => value.trim());
  return khoangGioGiaoNhau(batDauMot, ketThucMot, batDauHai, ketThucHai);
}

function nhanVienDaCoHoacChongCa(nhanVien, ngayStr, caLamId) {
  const ngayIndex = cacNgayTrongTuan.value.findIndex((ngay) => formatISODate(ngay) === ngayStr);
  if (ngayIndex < 0) return false;
  return (nhanVien.lich[ngayIndex] || []).some((lich) =>
    String(lich.ca).toLowerCase() === String(caLamId).toLowerCase()
      || caChongGio(lich.ca, caLamId)
  );
}

function mauOvertimeBar(nv) {
  const pct = nv.overtime / nv.gioiHanOT;
  if (pct >= 0.9) return "bg-rose-500";
  if (pct >= 0.5) return "bg-orange-400";
  return "bg-emerald-500";
}

function phanTramOT(nv) {
  return Math.min((nv.overtime / nv.gioiHanOT) * 100, 100);
}

const nvTruc = computed(
  () => danhSachNV.value.filter((nv) => nv.lich.some((dsCa) => dsCa.length > 0)).length,
);
const caUnassigned = computed(
  () =>
    danhSachNV.value.filter((nv) => nv.lich.every((dsCa) => dsCa.length === 0)).length,
);
</script>

<template>

  

  <div class="schedule-page space-y-5">

    <!-- ───── HEADER ───── -->
    <section class="flex flex-wrap items-center gap-3">
      <!-- <button
        @click="router.push({ name: 'admin-nhan-vien' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>

      <div class="flex-1">
        <h1 class="text-[26px] font-bold tracking-tight text-slate-900">
          Quản lý lịch làm việc
        </h1>
        <p class="text-sm text-slate-400">
          Phân ca và theo dõi giờ làm cho nhân viên
        </p>
      </div> -->

      <!-- Tuần hiển thị -->
      <div
        class="flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm"
      >
        <CalendarDays class="h-4 w-4 text-slate-400" />
        {{ formatTuanHienThi() }}
      </div>

      <button @click="xepCaDong" class="admin-btn-soft gap-2">
        <Shuffle class="h-4 w-4" /> Xếp ca tự động
      </button>
      <button @click="xuatExcel" class="admin-btn-soft gap-2">
        <Download class="h-4 w-4" /> Xuất Excel
      </button>
      <button @click="moModalThemCaTuHeader" class="admin-btn-primary gap-2 bg-indigo-500 hover:bg-indigo-600 text-white">
        <Users class="h-4 w-4" /> Phân ca
      </button>
      <button @click="moModalTaoCa" class="admin-btn-primary gap-2">
        <Plus class="h-4 w-4" /> Thêm ca mới
      </button>
    </section>

    <!-- Alert when viewing a specific employee's schedule -->
    <div
      v-if="employeeIdFilter" class="flex items-center justify-between rounded-2xl bg-violet-50 p-4 text-sm font-semibold text-violet-700">
      <div class="flex items-center gap-2">
        <CalendarDays class="h-5 w-5 text-violet-500" />
        <span>Đang hiển thị lịch làm việc của nhân viên:
          <span class="font-bold text-violet-900">{{
            danhSachLocVaiTro[0]?.ten || "Đang tải..."
          }}</span></span>
      </div>
      <button
        @click="router.push({ name: 'admin-nhan-vien-lich-lam' })"
        class="text-xs bg-white hover:bg-violet-100 text-violet-700 px-3 py-1.5 rounded-xl border border-violet-200 transition shadow-sm">
        Xem tất cả nhân viên
      </button>
    </div>

    <!-- ───── CONTENT ───── -->

    <div class="grid gap-5 2xl:grid-cols-[minmax(0,1fr)_320px]">

      <!-- ── Bảng lịch ── -->
      <section class="schedule-board rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">


        <!-- Thanh điều hướng tuần -->
        <div class="mb-5 flex flex-wrap items-center gap-3">
          <h2 class="flex-1 text-base font-bold text-slate-800">
            Bảng lịch làm việc theo tuần
          </h2>

          <!-- Lọc vai trò -->
          <div class="flex items-center gap-2 text-sm text-slate-500">
            <span class="font-medium">Vai trò:</span>
            <select
              v-model="boLocVaiTro"
              class="rounded-xl border border-slate-200 bg-slate-50 px-3 py-1.5 text-sm outline-none focus:border-primary/50 focus:ring-2 focus:ring-primary/10 transition"
            >
              <option v-for="vt in dsVaiTro" :key="vt.value" :value="vt.value">
                {{ vt.label }}
              </option>
            </select>
          </div>

          <button
            @click="tuanTruoc"
            class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition"
          >
            <ChevronLeft class="h-4 w-4" />
          </button>
          <button
            @click="homNay"
            class="rounded-xl bg-primary px-4 py-1.5 text-sm font-semibold text-white transition hover:bg-primary-hover shadow-sm"
          >
            Hôm nay
          </button>
          <button
            @click="tuanSau"
            class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition"
          >
            <ChevronRight class="h-4 w-4" />
          </button>
        </div>

        <div
          v-if="loiTrang"
          class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600"
        >
          {{ loiTrang }}
        </div>

        <!-- Bảng lịch (Board View) -->
        <div v-if="dangTai" class="py-10 text-center text-sm text-slate-400">
          Đang tải dữ liệu lịch làm việc...
        </div>
        <div v-else-if="lichBoard.length === 0" class="py-10 text-center text-sm text-slate-400">
          Không có dữ liệu.
        </div>
        <div v-else class="overflow-x-auto pb-4">
          <div class="flex gap-4 min-w-[900px]">
            <div v-for="day in lichBoard" :key="day.ngayStr" class="flex-1 flex flex-col gap-3 min-w-[140px]">
              <!-- Day Header -->
              <div class="text-center bg-slate-50 rounded-xl py-3 border border-slate-100" :class="{ 'ring-2 ring-primary bg-primary/5': day.ngay.toDateString() === new Date().toDateString() }">
                <div class="font-bold text-slate-700 text-sm">{{ day.thu }}</div>
                <div class="text-xs font-normal text-slate-500 mt-0.5">{{ formatNgay(day.ngay) }}</div>
              </div>

              <!-- Shift Cards -->
              <div v-for="ca in day.cas" :key="ca.id" class="rounded-[16px] p-3 border transition hover:shadow-md flex flex-col" :class="ca.muaNhat" :style="ca.mauHex ? { backgroundColor: ca.mauHex + '15', borderColor: ca.mauHex + '40', color: ca.mauHex } : {}">
                <div class="font-bold text-sm">{{ ca.nhan }}</div>
                <div class="text-[11px] opacity-80 mt-0.5">{{ ca.gio }}</div>
                <div class="mt-2 text-xs font-semibold">
                  {{ ca.nhanViens.length }}/{{ MAX_NHAN_VIEN_MOI_CA }} Nhân viên
                </div>
                <button 
                  @click="xemChiTietCa(day, ca)" 
                  class="mt-3 mx-auto px-4 py-1.5 rounded-full border bg-white text-[11px] font-bold transition hover:shadow-sm"
                  :class="[
                    ca.id === 'sang' ? 'border-emerald-500 text-emerald-600 hover:bg-emerald-50' : 
                    ca.id === 'chieu' ? 'border-orange-400 text-orange-600 hover:bg-orange-50' : 
                    ca.id === 'toi' ? 'border-violet-400 text-violet-600 hover:bg-violet-50' : ''
                  ]"
                  :style="ca.mauHex ? { borderColor: ca.mauHex, color: ca.mauHex } : {}"
                >
                  Xem thêm
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ── Sidebar ── -->
      <aside class="space-y-4">
        <!-- Overtime tracker -->
        <div
          class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm"
        >
          <div class="mb-4 flex items-center justify-between">
            <h3 class="text-sm font-bold text-slate-800">Theo dõi tăng ca</h3>
            <button class="text-slate-400 hover:text-slate-600">
              <MoreHorizontal class="h-4 w-4" />
            </button>
          </div>

          <div class="space-y-4">
            <div v-for="nv in danhSachNV" :key="nv.id" class="space-y-1.5">
              <div class="flex items-center justify-between text-sm">
                <span class="font-semibold text-slate-700">{{ nv.ten }}</span>
                <span
                  :class="[
                    'text-xs font-bold',
                    nv.overtime >= nv.gioiHanOT * 0.9
                      ? 'text-rose-500'
                      : nv.overtime === 0
                        ? 'text-slate-400'
                        : 'text-emerald-600',
                  ]"
                >
                  {{ nv.overtime }}h / {{ nv.gioiHanOT }}h
                </span>
              </div>
              <div class="h-2 w-full overflow-hidden rounded-full bg-slate-100">
                <div
                  :class="[
                    'h-full rounded-full transition-all duration-500',
                    mauOvertimeBar(nv),
                  ]"
                  :style="{ width: phanTramOT(nv) + '%' }"></div>
              </div>
            </div>
          </div>

          <!-- Cảnh báo -->
          <div
            v-if="danhSachNV.some((nv) => nv.overtime >= nv.gioiHanOT * 0.9)"
            class="mt-4 rounded-xl bg-rose-50 px-3 py-2.5 text-xs text-rose-700"
          >
            <span class="font-bold">Lưu ý:</span>
            {{
              danhSachNV
                .filter((nv) => nv.overtime >= nv.gioiHanOT * 0.9)
                .map((nv) => nv.ten)
                .join(", ")
            }}
            sắp vượt giới hạn tăng ca hàng tuần. Vui lòng xem xét lại lịch trực
            chủ nhật.
          </div>
        </div>

        <!-- Thống kê nhanh -->
        <div class="grid grid-cols-2 gap-3">
          <div class="rounded-[20px] bg-emerald-50 p-4 text-center">
            <div
              class="mx-auto mb-2 flex h-9 w-9 items-center justify-center rounded-2xl bg-emerald-100 text-emerald-600"
            >
              <Users class="h-5 w-5" />
            </div>
            <p class="text-xs font-semibold text-emerald-600">Nhân viên trực</p>
            <p class="mt-1 text-2xl font-bold text-emerald-700">
              {{ nvTruc }} / {{ tongNV }}
            </p>
          </div>
          <div class="rounded-[20px] bg-primary-light p-4 text-center">
            <div
              class="mx-auto mb-2 flex h-9 w-9 items-center justify-center rounded-2xl bg-primary/10 text-primary"
            >
              <CalendarDays class="h-5 w-5" />
            </div>
            <p class="text-xs font-semibold text-primary">
              Nhân viên chưa phân công
            </p>
            <p class="mt-1 text-2xl font-bold text-primary">
              {{ String(caUnassigned).padStart(2, "0") }}
            </p>
          </div>
        </div>

        <!-- Phân loại ca -->
        <div
          class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm"
        >
          <h3 class="mb-3 text-sm font-bold text-slate-800">Phân loại ca</h3>
          <div class="space-y-2.5">
            <div
              v-for="ca in DS_CA"
              :key="ca.id"
              class="flex items-center gap-3 text-sm"
            >
              <div :class="['h-3.5 w-3.5 rounded-sm', ca.mau]" :style="ca.mauHex ? { backgroundColor: ca.mauHex } : {}" />
              <span class="font-semibold text-slate-700">{{ ca.nhan }}</span>
              <span class="text-slate-400">({{ ca.gio }})</span>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- ======================= MODAL 1: CHI TIẾT CA ======================= -->
    <Teleport to="body">
      <div v-if="showModalChiTietCa && currentChiTietCa" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm" @click="showModalChiTietCa = false"></div>
        <div class="relative w-full max-w-md overflow-hidden rounded-[24px] bg-white shadow-xl animate-in zoom-in-95 duration-200">
          
          <!-- Header -->
          <div class="border-b border-slate-100 p-5 pb-4">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-bold text-slate-800">
                Danh sách {{ currentChiTietCa.ca.nhan }} - {{ currentChiTietCa.day.thu }} ({{ formatNgay(currentChiTietCa.day.ngay) }})
              </h3>
              <button @click="showModalChiTietCa = false" class="rounded-full p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition">
                <X class="w-5 h-5" />
              </button>
            </div>
          </div>

          <!-- Body: Danh sách nhân viên -->
          <div class="p-5 max-h-[50vh] overflow-y-auto space-y-3">
            <div v-if="currentChiTietCa.ca.nhanViens.length === 0" class="text-center py-6 text-sm text-slate-400">
              Chưa có nhân viên nào trong ca này.
            </div>
            <div v-else v-for="nv in currentChiTietCa.ca.nhanViens" :key="nv.id" class="flex items-center justify-between p-3 rounded-xl border border-slate-100 bg-slate-50/50">
              <div class="flex items-center gap-3">
                <img v-if="nv.hinhAnh" :src="nv.hinhAnh" class="w-10 h-10 rounded-full object-cover shadow-sm ring-1 ring-slate-200" />
                <div v-else :class="['flex w-10 h-10 items-center justify-center rounded-full text-sm font-bold text-white shadow-sm ring-1 ring-slate-200', nv.mauNen]">
                  {{ nv.vieTat }}
                </div>
                <div>
                  <div class="text-sm font-semibold text-slate-800">{{ nv.ten }} <span class="text-slate-400 font-normal">({{ nv.ma }})</span></div>
                </div>
              </div>
              <button v-if="!caHienTaiBiKhoa" @click="xoaCa(nv)" title="Xóa khỏi ca" class="p-2 text-rose-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition border border-transparent hover:border-rose-100">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/><line x1="10" x2="10" y1="11" y2="17"/><line x1="14" x2="14" y1="11" y2="17"/></svg>
              </button>
            </div>
          </div>

          <!-- Footer: Nút Thêm (chỉ hiện khi ca đang diễn ra) -->
          <div v-if="caHienTaiCoTheThemNhanVien" class="border-t border-slate-100 p-5 bg-slate-50">
            <button 
              @click="moModalThemCa" 
              class="w-full flex justify-center items-center gap-2 py-2.5 bg-white border border-slate-200 shadow-sm text-sm font-bold text-slate-700 rounded-xl hover:bg-slate-50 transition"
            >
              <Plus class="w-4 h-4" /> Thêm nhân viên
            </button>
          </div>

        </div>
      </div>
    </Teleport>

    <!-- ======================= MODAL 2: THÊM NHÂN VIÊN VÀO CA ======================= -->
    <Teleport to="body">
      <div v-if="showModalThemCa" class="fixed inset-0 z-[110] flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm" @click="huyThemCa"></div>
        <div class="relative w-full max-w-sm overflow-hidden rounded-[24px] bg-white shadow-xl animate-in zoom-in-95 duration-200">
          
          <!-- Header -->
          <div class="border-b border-slate-100 p-5 pb-4">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-bold text-slate-800">Thêm nhân viên vào ca</h3>
              <button @click="huyThemCa" class="rounded-full p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition">
                <X class="w-5 h-5" />
              </button>
            </div>
          </div>

          <!-- Body -->
          <div class="p-5 space-y-5">
            
            <!-- Info Ca -->
            <div v-if="currentChiTietCa">
              <div class="flex items-center gap-2 mb-1">
                <div class="w-3 h-3 rounded bg-orange-400" :class="currentChiTietCa.ca.id === 'sang' ? 'bg-emerald-500' : currentChiTietCa.ca.id === 'toi' ? 'bg-violet-400' : 'bg-orange-400'"></div>
                <span class="font-bold text-emerald-800" :class="currentChiTietCa.ca.id === 'sang' ? 'text-emerald-700' : currentChiTietCa.ca.id === 'toi' ? 'text-violet-700' : 'text-orange-700'">{{ currentChiTietCa.ca.nhan }}</span>
              </div>
              <div class="text-sm text-slate-500">
                {{ currentChiTietCa.ca.gio }} • {{ currentChiTietCa.day.thu }}, {{ formatNgay(currentChiTietCa.day.ngay) }}
              </div>
            </div>
            <div v-else class="space-y-4">
              <!-- Select Ngay -->
              <div class="space-y-1.5">
                <label class="text-sm font-bold text-slate-700">Ngày làm việc</label>
                <select 
                  v-model="chonNgayVal" 
                  class="w-full h-11 px-3 text-sm border border-slate-200 rounded-xl bg-slate-50/50 text-slate-700 focus:outline-none focus:border-primary/50 focus:ring-4 focus:ring-primary/10 transition"
                >
                  <option 
                    v-for="(ngay, idx) in cacNgayTrongTuan" 
                    :key="idx" 
                    :value="formatISODate(ngay)"
                    :disabled="!coTheThemNhanVienVaoCa(ngay, chonCaVal)"
                  >
                    {{ NHAN_TUAN[idx] }} ({{ formatNgay(ngay) }}){{ !coTheThemNhanVienVaoCa(ngay, chonCaVal) ? ' - Ca đã kết thúc' : '' }}
                  </option>
                </select>
              </div>
              
              <!-- Select Ca -->
              <div class="space-y-1.5">
                <label class="text-sm font-bold text-slate-700">Ca làm việc</label>
                <select 
                  v-model="chonCaVal" 
                  class="w-full h-11 px-3 text-sm border border-slate-200 rounded-xl bg-slate-50/50 text-slate-700 focus:outline-none focus:border-primary/50 focus:ring-4 focus:ring-primary/10 transition"
                >
                  <option
                    v-for="ca in DS_CA"
                    :key="ca.id"
                    :value="ca.id"
                    :disabled="!coTheThemNhanVienVaoCa(chonNgayVal, ca.id)"
                  >
                    {{ ca.nhan }} ({{ ca.gio }}){{ !coTheThemNhanVienVaoCa(chonNgayVal, ca.id) ? ' - Ca đã kết thúc' : '' }}
                  </option>
                </select>
              </div>
            </div>

            <!-- Hiện tại -->
            <div v-if="currentChiTietCa" class="bg-emerald-50/50 border border-emerald-100 rounded-xl p-3 flex items-center gap-2 text-sm text-emerald-700">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-emerald-500"><circle cx="12" cy="12" r="10"/><line x1="12" x2="12" y1="8" y2="12"/><line x1="12" x2="12.01" y1="16" y2="16"/></svg>
              Hiện tại: <span class="font-bold">{{ currentChiTietCa.ca.nhanViens.length }}/{{ MAX_NHAN_VIEN_MOI_CA }}</span> nhân viên
            </div>

            <!-- Chọn nhân viên -->
            <div class="space-y-1.5">
              <label class="text-sm font-bold text-slate-700">Nhân viên</label>
              <select 
                v-model="chonNhanVienId" 
                class="w-full h-11 px-3 text-sm border border-slate-200 rounded-xl bg-slate-50/30 text-slate-700 focus:outline-none focus:border-emerald-400 focus:ring-4 focus:ring-emerald-50 transition"
              >
                <option value="" disabled>Chọn nhân viên...</option>
                <option v-for="nv in nhanVienKhaDung" :key="nv.id" :value="nv.id">
                  {{ nv.ten }}
                </option>
              </select>
              <div v-if="nhanVienKhaDung.length === 0" class="text-xs text-rose-500 mt-1">
                Không còn nhân viên nào  để thêm.
              </div>
            </div>

          </div>

          <!-- Footer -->
          <div class="flex items-center gap-3 p-5 pt-0">
            <button @click="huyThemCa" class="flex-1 py-2.5 rounded-xl border border-slate-200 text-sm font-bold text-slate-600 hover:bg-slate-50 transition">
              Hủy
            </button>
            <button 
              @click="luuCa" 
              :disabled="!chonNhanVienId || dangTai || !coTheThemNhanVienVaoCa(currentChiTietCa?.day?.ngayStr || chonNgayVal, currentChiTietCa?.ca?.id || chonCaVal)" 
              class="flex-1 py-2.5 rounded-xl bg-rose-400 hover:bg-rose-500 text-white text-sm font-bold transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {{ dangTai ? 'Đang lưu...' : 'Thêm vào ca' }}
            </button>
          </div>

        </div>
      </div>
    </Teleport>
    <!-- ======================= MODAL 3: THÊM CA LÀM MỚI ======================= -->
    <Teleport to="body">
      <div v-if="showModalTaoCa" class="fixed inset-0 z-[120] flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm" @click="huyTaoCa"></div>
        <div class="relative w-full max-w-[500px] overflow-hidden rounded-[16px] bg-white shadow-xl animate-in zoom-in-95 duration-200">
          
          <!-- Header -->
          <div class="p-6 pb-4">
            <div class="flex items-center justify-between">
              <h3 class="text-[18px] font-bold text-slate-800">Thêm mới Ca làm việc</h3>
              <button @click="huyTaoCa" class="text-slate-400 hover:text-slate-600 transition">
                <X class="w-5 h-5" />
              </button>
            </div>
          </div>

          <!-- Body -->
          <div class="p-6 pt-0 space-y-5">
            
            <div class="space-y-1.5">
              <label class="text-[14px] font-medium text-slate-700">Tên ca <span class="text-rose-500">*</span></label>
              <input 
                v-model="formTaoCa.tenCa" 
                type="text" 
                class="w-full h-11 px-3 text-[14px] border border-slate-200 rounded-xl focus:outline-none focus:border-slate-400 transition placeholder-slate-400"
                placeholder="VD: Ca Sáng, Ca Chiều..."
              />
            </div>

            <div v-if="goiYCaTiepTheo" class="p-3 bg-blue-50 border border-blue-100 rounded-xl flex items-start gap-2.5">
              <Clock class="h-4 w-4 text-blue-500 mt-0.5 shrink-0" />
              <p class="text-[13px] text-blue-700 leading-snug">
                <template v-if="goiYCaTiepTheo.tuCa">
                  Gợi ý ca tiếp theo sau "{{ goiYCaTiepTheo.tuCa.ten }}": {{ goiYCaTiepTheo.gioBatDau }} - {{ goiYCaTiepTheo.gioKetThuc }}.
                </template>
                <template v-else>
                  Gợi ý ca đầu tiên: {{ goiYCaTiepTheo.gioBatDau }} - {{ goiYCaTiepTheo.gioKetThuc }}.
                </template>
              </p>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-[14px] font-medium text-slate-700">Giờ bắt đầu</label>
                <div class="w-full h-11 border border-slate-200 rounded-xl focus-within:border-slate-400 transition bg-white overflow-hidden">
                  <TimePicker24h v-model="formTaoCa.gioBatDau" />
                </div>
              </div>
              
              <div class="space-y-1.5">
                <label class="text-[14px] font-medium text-slate-700">Giờ kết thúc</label>
                <div class="w-full h-11 border border-slate-200 rounded-xl focus-within:border-slate-400 transition bg-white overflow-hidden">
                  <TimePicker24h v-model="formTaoCa.gioKetThuc" />
                </div>
              </div>
            </div>

            <div class="space-y-1.5">
              <label class="text-[14px] font-medium text-slate-700">Mô tả</label>
              <textarea 
                v-model="formTaoCa.moTa" 
                rows="4"
                class="w-full p-3 text-[14px] border border-slate-200 rounded-xl focus:outline-none focus:border-slate-400 transition placeholder-slate-400 resize-none"
                placeholder="Ghi chú thêm về ca làm việc..."
              ></textarea>
            </div>
            
          </div>

          <!-- Footer -->
          <div class="flex items-center justify-end gap-3 p-6 pt-2">
            <button @click="huyTaoCa" class="px-6 py-2.5 rounded-xl border border-slate-200 bg-white text-[14px] font-medium text-slate-700 hover:bg-slate-50 transition shadow-sm">
              Hủy bỏ
            </button>
            <button 
              @click="luuTaoCa" 
              class="px-6 py-2.5 rounded-xl bg-gradient-to-r from-rose-500 to-slate-900 text-white text-[14px] font-medium transition shadow-sm hover:opacity-90"
            >
              Thêm mới
            </button>
          </div>

        </div>
      </div>
    </Teleport>
  </div>
</template>
