<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
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
  X,
  List,
  Trash2,
  Upload,
  Filter,
  Table as TableIcon,
  Eye,
  RotateCcw
} from "lucide-vue-next";
import { layDanhSachNhanVien } from "../../../services/nhan-vien.js";
import {
  layLichLamViec,
  phanCa,
  xoaLichLamViec,
  xepCaTuDong,
  datLaiLichLamViec,
} from "../../../services/lich-lam.js";
import { showSuccess, showError, showConfirm } from "../../../utils/alert.js";
import { getDisplayErrorMessage } from "../../../utils/error-message.js";
import { exportRowsToExcel } from "../../../utils/export-excel.js";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import { useAdminSession } from "../../../composable/useAdminSession.js";
import LichLamViecNhanVien from "./LichLamViecNhanVien.vue";
import { useRealtime } from "../../../composables/useRealtime.js";

const route = useRoute();
const router = useRouter();

const { adminSession } = useAdminSession();
const laAdmin = computed(() => adminSession.value.vaiTro === "Quản lý" || adminSession.value.vaiTro === "Quản trị viên" || adminSession.value.vaiTro === "Admin");

import { layDanhSachCaLam, normalizeShiftName } from "../../../services/ca-lam.js";

const SHIFT_COLORS = [
  { mau: "bg-emerald-500", muaNhat: "bg-emerald-50 border-emerald-200 text-emerald-700", nut: "border-emerald-500 text-emerald-600 hover:bg-emerald-50" },
  { mau: "bg-orange-400", muaNhat: "bg-orange-50 border-orange-200 text-orange-700", nut: "border-orange-400 text-orange-600 hover:bg-orange-50" },
  { mau: "bg-violet-400", muaNhat: "bg-violet-50 border-violet-200 text-violet-700", nut: "border-violet-400 text-violet-600 hover:bg-violet-50" },
  { mau: "bg-blue-500", muaNhat: "bg-blue-50 border-blue-200 text-blue-700", nut: "border-blue-500 text-blue-600 hover:bg-blue-50" },
  { mau: "bg-rose-500", muaNhat: "bg-rose-50 border-rose-200 text-rose-700", nut: "border-rose-500 text-rose-600 hover:bg-rose-50" },
  { mau: "bg-amber-500", muaNhat: "bg-amber-50 border-amber-200 text-amber-700", nut: "border-amber-500 text-amber-600 hover:bg-amber-50" },
  { mau: "bg-teal-500", muaNhat: "bg-teal-50 border-teal-200 text-teal-700", nut: "border-teal-500 text-teal-600 hover:bg-teal-50" }
];

const DS_CA = ref([]);
const TAT_CA = ref([]);

function mauOnDinhTheoCa(id) {
  const key = String(id || "ca");
  let hash = 0;
  for (let index = 0; index < key.length; index++) {
    hash = ((hash << 5) - hash + key.charCodeAt(index)) | 0;
  }
  return SHIFT_COLORS[Math.abs(hash) % SHIFT_COLORS.length];
}

async function taiDanhSachCa() {
  try {
    const list = await layDanhSachCaLam();
    if (list.length > 0) {
      const mapped = list.map((c) => {
        const colorSet = mauOnDinhTheoCa(c.id);
        return {
          id: c.id,
          nhan: normalizeShiftName(c.id, c.ten),
          gio: `${c.gioBatDau} - ${c.gioKetThuc}`,
          mau: colorSet.mau,
          muaNhat: colorSet.muaNhat,
          nut: colorSet.nut,
          gioBatDau: c.gioBatDau,
          trangThai: Boolean(c.trangThai),
        };
      });
      mapped.sort((a, b) => {
        const tA = a.gioBatDau || "00:00";
        const tB = b.gioBatDau || "00:00";
        return tA.localeCompare(tB);
      });
      TAT_CA.value = mapped;
      DS_CA.value = mapped.filter((ca) => ca.trangThai);
    } else {
      TAT_CA.value = [];
      DS_CA.value = [];
    }
  } catch (e) {
    console.error("Không thể tải danh sách ca làm việc", e);
  }
}

const { subscribeTopic } = useRealtime();
let boDemDongBoLich = null;

function lenLichDongBoCaVaLich(delay = 100) {
  if (!laAdmin.value) return;
  if (boDemDongBoLich) clearTimeout(boDemDongBoLich);
  boDemDongBoLich = setTimeout(async () => {
    boDemDongBoLich = null;
    await taiDanhSachCa();
    await taiDuLieuLich();
  }, delay);
}

subscribeTopic('/topic/admin/lich-lam-viec', () => {
  lenLichDongBoCaVaLich();
});

function dongBoKhiQuayLaiTab() {
  if (document.visibilityState === 'visible') {
    lenLichDongBoCaVaLich(0);
  }
}

// ───────── Chế độ xem & Tuần hiện tại ─────────
const calendarMode = ref('tuan'); // 'ngay', 'tuan', 'thang'
const ngayHienTai = ref(new Date());

const ngayLocSelect = computed({
  get() {
    return formatISODate(ngayHienTai.value);
  },
  set(val) {
    if (val) {
      ngayHienTai.value = new Date(val);
    }
  }
});

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

const cacNgayTrongThang = computed(() => {
  const year = ngayHienTai.value.getFullYear();
  const month = ngayHienTai.value.getMonth();
  const firstDayOfMonth = new Date(year, month, 1);
  const lastDayOfMonth = new Date(year, month + 1, 0);

  // Thứ 2 đầu tiên trên grid
  const startDate = new Date(firstDayOfMonth);
  const startDay = startDate.getDay(); // 0=CN, 1=T2...
  const diffStart = startDay === 0 ? -6 : 1 - startDay;
  startDate.setDate(startDate.getDate() + diffStart);

  // Chủ nhật cuối cùng trên grid
  const endDate = new Date(lastDayOfMonth);
  const endDay = endDate.getDay();
  if (endDay !== 0) {
    endDate.setDate(endDate.getDate() + (7 - endDay));
  }

  const weeks = [];
  let currentWeek = [];
  const currentDate = new Date(startDate);

  while (currentDate <= endDate) {
    currentWeek.push(new Date(currentDate));
    if (currentWeek.length === 7) {
      weeks.push(currentWeek);
      currentWeek = [];
    }
    currentDate.setDate(currentDate.getDate() + 1);
  }

  return weeks;
});

function laCaDangHoatDong(caId) {
  if (!caId) return false;
  return DS_CA.value.some((c) => String(c.id).toLowerCase() === String(caId).toLowerCase());
}

function nhanVienCoLichTrongNgay(d) {
  const dateStr = formatISODate(d);
  const result = [];
  danhSachLocVaiTro.value.forEach(nv => {
    (nv.lich[dateStr] || []).forEach((lich) => {
      if (lich.ca && laCaDangHoatDong(lich.ca)) {
        result.push({ nv, ca: lich.ca, id: lich.id });
      }
    });
  });
  return result;
}

const NHAN_TUAN = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"];

function formatNgay(d) {
  return `${String(d.getDate()).padStart(2, "0")}/${String(d.getMonth() + 1).padStart(2, "0")}`;
}

function formatTuanHienThi() {
  if (calendarMode.value === 'ngay') {
    return `${String(ngayHienTai.value.getDate()).padStart(2, "0")} tháng ${ngayHienTai.value.getMonth() + 1}, ${ngayHienTai.value.getFullYear()}`;
  } else if (calendarMode.value === 'thang') {
    return `Tháng ${ngayHienTai.value.getMonth() + 1} Năm ${ngayHienTai.value.getFullYear()}`;
  }
  const dau = cacNgayTrongTuan.value[0];
  const cuoi = cacNgayTrongTuan.value[6];
  const format = (d) =>
    `${String(d.getDate()).padStart(2, "0")} tháng ${d.getMonth() + 1}, ${d.getFullYear()}`;
  return `${format(dau)} – ${format(cuoi)}`;
}

function tuanTruoc() {
  const d = new Date(ngayHienTai.value);
  if (calendarMode.value === 'ngay') d.setDate(d.getDate() - 1);
  else if (calendarMode.value === 'thang') d.setMonth(d.getMonth() - 1);
  else d.setDate(d.getDate() - 7);
  ngayHienTai.value = d;
}

function tuanSau() {
  const d = new Date(ngayHienTai.value);
  if (calendarMode.value === 'ngay') d.setDate(d.getDate() + 1);
  else if (calendarMode.value === 'thang') d.setMonth(d.getMonth() + 1);
  else d.setDate(d.getDate() + 7);
  ngayHienTai.value = d;
}

watch(calendarMode, () => {
  if (laAdmin.value) {
    taiDuLieuLich();
  }
});

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
  
  const tuNgay = calendarMode.value === 'thang' 
    ? cacNgayTrongThang.value[0][0] 
    : cacNgayTrongTuan.value[0];
    
  const denNgay = calendarMode.value === 'thang' 
    ? cacNgayTrongThang.value[cacNgayTrongThang.value.length - 1][6] 
    : cacNgayTrongTuan.value[6];

  const tuStr = formatISODate(tuNgay);
  const denStr = formatISODate(denNgay);

  try {
    const lichData = await layLichLamViec(tuStr, denStr);
    danhSachNV.value.forEach((nv) => {
      // Xóa lịch cũ
      nv.lich = {};
      const lichNhanVien = lichData.filter(l => String(l.nhanVienId) === String(nv.id));
      lichNhanVien.forEach(l => {
        if (!nv.lich[l.ngay]) nv.lich[l.ngay] = [];
        nv.lich[l.ngay].push({ id: l.id, ca: l.caLamId ?? l.ca });
      });

      nv.tongGio = Object.values(nv.lich).flat().reduce(
        (tong, item) => tong + soGioCa(item.ca),
        0,
      );
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
    const ds = await layDanhSachNhanVien({ trangThai: 1, vaiTro: 2 });
    danhSachNV.value = ds.map((nv) => ({
      id: String(nv.id),
      ma: nv.ma ?? "",
      ten: nv.hoTen ?? "",
      vieTat: taoVietTat(nv.hoTen ?? ""),
      chucVu: nv.tenVaiTro ?? "—",
      vaiTro: Number(nv.vaiTro) === 1 ? 1 : 2,
      hinhAnh: nv.hinhAnh ?? "",
      mauNen: mauNenNV(Number(nv.vaiTro) === 1 ? 1 : 2),
      lich: {},
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

onMounted(() => {
  document.addEventListener('visibilitychange', dongBoKhiQuayLaiTab);
  window.addEventListener('focus', dongBoKhiQuayLaiTab);
  if (laAdmin.value) {
    taiNhanVien();
  }
});

onBeforeUnmount(() => {
  document.removeEventListener('visibilitychange', dongBoKhiQuayLaiTab);
  window.removeEventListener('focus', dongBoKhiQuayLaiTab);
  if (boDemDongBoLich) clearTimeout(boDemDongBoLich);
});

watch(() => ngayHienTai.value.getMonth(), async () => {
  if (laAdmin.value && calendarMode.value === 'thang') {
    dangTai.value = true;
    await taiDuLieuLich();
    dangTai.value = false;
  }
});

watch(ngayDauTuan, async () => {
  if (laAdmin.value && calendarMode.value !== 'thang') {
    dangTai.value = true;
    await taiDuLieuLich();
    dangTai.value = false;
  }
});

// ───────── Bộ lọc vai trò ─────────
const boLocVaiTro = ref(0); // 0 = tất cả
const dsVaiTro = [
  { value: 0, label: "Tất cả" },
  { value: 1, label: "Quản lý" },
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
    const ngayStr = formatISODate(ngay);
    let activeCas = DS_CA.value;
    if (boLocCaLam.value) {
      activeCas = DS_CA.value.filter(c => String(c.id).toLowerCase() === boLocCaLam.value.toLowerCase());
    }
    return {
      ngay: ngay,
      ngayStr: ngayStr,
      thu: NHAN_TUAN[ngayIndex],
      cas: activeCas.map((caInfo) => {
        const nhanViens = danhSachLocVaiTro.value.filter((nv) =>
          (nv.lich[ngayStr] || []).some((item) => item.ca.toLowerCase() === caInfo.id.toLowerCase())
        );
        return {
          ...caInfo,
          nhanViens,
        };
      }),
    };
  });
});

const lichBoardHienThi = computed(() => {
  if (calendarMode.value === 'ngay') {
    const todayStr = formatISODate(ngayHienTai.value);
    const dayData = lichBoard.value.find(d => d.ngayStr === todayStr);
    return dayData ? [dayData] : [];
  }
  return lichBoard.value;
});

// ───────── Chế độ xem & Bảng ─────────
const viewMode = ref('bang'); // 'bang' hoặc 'lich'

const danhSachCaLamBang = computed(() => {
  const result = [];
  const days = calendarMode.value === 'thang'
    ? cacNgayTrongThang.value.flat()
    : (calendarMode.value === 'ngay' ? [ngayHienTai.value] : cacNgayTrongTuan.value);

  danhSachLocVaiTro.value.forEach((nv) => {
    days.forEach((d) => {
      const ngayStr = formatISODate(d);
      (nv.lich[ngayStr] || []).forEach((lich) => {
        if (lich.ca) {
          const caInfo = layThongTinCa(lich.ca);
          result.push({
            nv,
            lichId: lich.id,
            caId: lich.ca,
            caInfo,
            ngay: d,
            ngayStr,
          });
        }
      });
    });
  });
  // Sắp xếp theo ngày tăng dần, rồi đến giờ ca làm bắt đầu sớm hơn lên trước
  result.sort((a, b) => {
    if (a.ngayStr !== b.ngayStr) return a.ngayStr.localeCompare(b.ngayStr);
    const gioA = a.caInfo?.gioBatDau || "00:00";
    const gioB = b.caInfo?.gioBatDau || "00:00";
    if (gioA !== gioB) return gioA.localeCompare(gioB);
    return a.nv.ten.localeCompare(b.nv.ten);
  });
  return result;
});

const timKiemNhanVien = ref("");
const boLocCaLam = ref("");

const danhSachCaLamBangLoc = computed(() => {
  let list = danhSachCaLamBang.value;
  if (timKiemNhanVien.value) {
    const keyword = timKiemNhanVien.value.toLowerCase();
    list = list.filter(item => 
      item.nv.ten.toLowerCase().includes(keyword) || 
      item.nv.ma.toLowerCase().includes(keyword)
    );
  }
  if (boLocCaLam.value) {
    list = list.filter(item => 
      String(item.caId).toLowerCase() === boLocCaLam.value.toLowerCase()
    );
  }
  return list;
});

// Phân trang cho view bảng
const trangHienTai = ref(1);
const soPhanTuMotTrang = ref(10);
const pageSizeOptions = [5, 10, 20, 50];

const tongSoTrang = computed(() => {
  return Math.ceil(danhSachCaLamBangLoc.value.length / soPhanTuMotTrang.value) || 1;
});

const danhSachCaLamBangLocPhanTrang = computed(() => {
  const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
  return danhSachCaLamBangLoc.value.slice(start, start + soPhanTuMotTrang.value);
});

watch(danhSachCaLamBangLoc, () => {
  trangHienTai.value = 1;
});

watch(soPhanTuMotTrang, () => {
  trangHienTai.value = 1;
});

// ───────── Hiển thị modal Chi tiết ca (Modal 1) ─────────
const showModalChiTietCa = ref(false);
const currentChiTietCa = ref(null);

function xemChiTietCa(day, ca) {
  currentChiTietCa.value = { day, ca };
  showModalChiTietCa.value = true;
}

function xemChiTietCaTuBang(item) {
  const day = lichBoard.value.find((ngay) => ngay.ngayStr === item.ngayStr) || {
    ngay: item.ngay,
    ngayStr: item.ngayStr,
    thu: NHAN_TUAN[(item.ngay.getDay() + 6) % 7] || "",
  };
  const ca = day.cas?.find((caInfo) => String(caInfo.id).toLowerCase() === String(item.caInfo.id).toLowerCase()) || {
    ...item.caInfo,
    nhanViens: danhSachLocVaiTro.value.filter(
      (nv) => (nv.lich[item.ngayStr] || []).some(
        (lich) => String(lich.ca).toLowerCase() === String(item.caInfo.id).toLowerCase(),
      ),
    ),
  };
  xemChiTietCa(day, ca);
}

// ───────── Hiển thị modal Thêm nhân viên (Modal 2) ─────────
const showModalThemCa = ref(false);
const chonNhanVienId = ref("");
const chonNgayVal = ref("");
const chonCaVal = ref("");

// Giờ bắt đầu của mỗi ca
const GIO_BAT_DAU_CA = computed(() => {
  const map = {};
  DS_CA.value.forEach(c => {
    const [gio, phut] = c.gio.split("-")[0].trim().split(":").map(Number);
    map[c.id] = Number.isFinite(gio) && Number.isFinite(phut) ? gio * 60 + phut : 8 * 60;
  });
  return map;
});

function laCaDaKhoa(ngay, caId) {
  if (!ngay) return false;
  const date = new Date(ngay);
  date.setHours(0, 0, 0, 0);
  const now = new Date();
  const today = new Date(now);
  today.setHours(0, 0, 0, 0);

  // Ngày quá khứ → khóa
  if (date < today) return true;

  // Ngày hôm nay → kiểm tra ca đã bắt đầu chưa
  if (date.getTime() === today.getTime() && caId) {
    const phutBatDau = GIO_BAT_DAU_CA.value[caId];
    const phutHienTai = now.getHours() * 60 + now.getMinutes();
    if (phutBatDau !== undefined && phutHienTai >= phutBatDau) return true;
  }

  return false;
}

function laNgayQuaKhu(ngay) {
  if (!ngay) return false;
  const date = new Date(ngay);
  date.setHours(0, 0, 0, 0);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return date < today;
}

// Computed: ca hiện tại trong modal có bị khóa không
const caHienTaiBiKhoa = computed(() => {
  if (!currentChiTietCa.value) return false;
  const { day, ca } = currentChiTietCa.value;
  return laCaDaKhoa(day.ngay, ca.id);
});

const danhSachChonNhanVienIds = ref([]);
const isThemCaThang = ref(false);

// Modal xem lịch theo ngày (quá khứ)
const showModalXemNgay = ref(false);
const xemNgayData = ref(null);

function moModalThemCaThang(ngay) {
  const ngayStr = formatISODate(ngay);
  const defaultCaId = DS_CA.value[0]?.id || '';
  const isPast = laCaDaKhoa(ngay, defaultCaId);

  if (isPast) {
    return;
  }

  chonNgayVal.value = ngayStr;
  chonCaVal.value = DS_CA.value[0]?.id || "";
  danhSachChonNhanVienIds.value = [];
  currentChiTietCa.value = null;
  isThemCaThang.value = true;
  showModalThemCa.value = true;
}

function moModalThemCa() {
  danhSachChonNhanVienIds.value = [];
  isThemCaThang.value = false;
  if (currentChiTietCa.value) {
    // Từ modal chi tiết ca — gán sẵn ngày và ca
    chonNgayVal.value = currentChiTietCa.value.day.ngayStr;
    chonCaVal.value = currentChiTietCa.value.ca.id;
  } else {
    const defaultCaId = DS_CA.value[0]?.id || '';
    const firstAvailableDay = cacNgayTrongTuan.value.find(d => !laCaDaKhoa(d, defaultCaId)) || cacNgayTrongTuan.value[0];
    chonNgayVal.value = formatISODate(firstAvailableDay);
    chonCaVal.value = defaultCaId;
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
  if (currentChiTietCa.value) {
    const { day, ca } = currentChiTietCa.value;
    return danhSachLocVaiTro.value.filter((nv) =>
      !nhanVienDaCoHoacChongCa(nv, day.ngayStr, ca.id)
    );
  }

  const ngayCheck = chonNgayVal.value;
  const caCheck = chonCaVal.value;
  if (!ngayCheck || !caCheck) return danhSachLocVaiTro.value;
  return danhSachLocVaiTro.value.filter((nv) =>
    !nhanVienDaCoHoacChongCa(nv, ngayCheck, caCheck)
  );
});

async function luuCa() {
  if (danhSachChonNhanVienIds.value.length === 0) {
    showError("Vui lòng chọn nhân viên!");
    return;
  }

  let ngayStr;
  let caId;

  if (currentChiTietCa.value) {
    const { day, ca } = currentChiTietCa.value;
    ngayStr = day.ngayStr;
    caId = ca.id;
  } else {
    ngayStr = chonNgayVal.value;
    caId = chonCaVal.value;
  }

  dangTai.value = true;
  try {
    const promises = danhSachChonNhanVienIds.value.map(nvId => 
      phanCa({
        nhanVienId: nvId,
        ngay: ngayStr,
        caLamId: caId,
      })
    );
    await Promise.all(promises);
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
    const lichCanXoa = (nhanVien.lich[day.ngayStr] || []).find(
      (lich) => lich.ca.toLowerCase() === ca.id.toLowerCase(),
    );
    if (!lichCanXoa?.id) throw new Error("Không tìm thấy lịch làm việc cần xóa");
    await xoaLichLamViec(lichCanXoa.id);
    showSuccess("Xóa ca làm việc thành công!");
    
    await taiDuLieuLich();
    
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

async function xoaCaTuBang(nv, ngayStr, caInfo) {
  const xacNhan = await showConfirm(
    `Bạn có chắc chắn muốn xóa ca làm việc của ${nv.ten} ngày ${formatNgay(new Date(ngayStr))}?`,
    "Xác nhận xóa ca",
  );
  if (!xacNhan) return;
  
  dangTai.value = true;
  try {
    const lichCanXoa = (nv.lich[ngayStr] || []).find(
      (lich) => lich.ca.toLowerCase() === caInfo.id.toLowerCase(),
    );
    if (!lichCanXoa?.id) throw new Error("Không tìm thấy lịch làm việc cần xóa");
    await xoaLichLamViec(lichCanXoa.id);
    showSuccess("Xóa ca làm việc thành công!");
    await taiDuLieuLich();
    
    const updatedDay = lichBoard.value.find(d => d.ngayStr === ngayStr);
    if (updatedDay) {
      const updatedCa = updatedDay.cas.find(c => c.id.toLowerCase() === caInfo.id.toLowerCase());
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
  const tomorrow = new Date();
  tomorrow.setHours(0, 0, 0, 0);
  tomorrow.setDate(tomorrow.getDate() + 1);
  const batDau = cacNgayTrongTuan.value.find((ngay) => ngay >= tomorrow);
  if (!batDau) {
    showError("Tuần đang xem không còn ngày tương lai để xếp ca.");
    return;
  }
  const ketThuc = cacNgayTrongTuan.value[6];
  const tuNgay = formatISODate(batDau);
  const denNgay = formatISODate(ketThuc);
  const xacNhan = await showConfirm(
    `Tự động tạo một nhân viên khung cho mỗi ca từ ${formatNgay(batDau)} đến ${formatNgay(ketThuc)}? Chỉ lịch tương lai trong phạm vi này bị ghi đè.`,
    "Xác nhận xếp ca tự động",
  );
  if (!xacNhan) return;
  dangTai.value = true;
  try {
    const result = await xepCaTuDong(tuNgay, denNgay);
    const canhBao = result?.soCaChuaCoNhanVien > 0
      ? ` Còn ${result.soCaChuaCoNhanVien} ca chưa có nhân viên do thiếu người.`
      : "";
    showSuccess(`Đã tạo ${result?.soLichDaTao ?? 0} lịch khung, mỗi ca một nhân viên.${canhBao}`);
    await taiDuLieuLich();
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể xếp ca tự động"));
  } finally {
    dangTai.value = false;
  }
}

const coNgayTuongLaiTrongTuan = computed(() => {
  const tomorrow = new Date();
  tomorrow.setHours(0, 0, 0, 0);
  tomorrow.setDate(tomorrow.getDate() + 1);
  return cacNgayTrongTuan.value.some((ngay) => ngay >= tomorrow);
});

async function datLaiLichTuongLai() {
  if (!coNgayTuongLaiTrongTuan.value) {
    showError("Tuần đang xem không còn ngày tương lai để đặt lại.");
    return;
  }
  const tomorrow = new Date();
  tomorrow.setHours(0, 0, 0, 0);
  tomorrow.setDate(tomorrow.getDate() + 1);
  const batDau = cacNgayTrongTuan.value.find((ngay) => ngay >= tomorrow);
  const ketThuc = cacNgayTrongTuan.value[6];
  const tuNgay = formatISODate(batDau);
  const denNgay = formatISODate(ketThuc);
  const xacNhan = await showConfirm(
    `Đặt lại lịch từ ${formatNgay(batDau)} đến ${formatNgay(ketThuc)}? Lịch hôm nay và quá khứ được giữ nguyên.`,
    "Xác nhận đặt lại lịch làm việc",
  );
  if (!xacNhan) return;
  dangTai.value = true;
  try {
    const result = await datLaiLichLamViec(tuNgay, denNgay);
    showSuccess(`Đã đặt lại ${result?.soLichDaXoa ?? 0} lịch làm việc trong tương lai.`);
    await taiDuLieuLich();
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể đặt lại lịch làm việc"));
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
        value: (row) => tenCaXuatExcel(row.lich[formatISODate(ngay)]),
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
  const found = TAT_CA.value.find((c) => c.id.toLowerCase() === id.toLowerCase());
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
  const [gioDau, phutDau] = batDau.split(":").map(Number);
  const [gioCuoi, phutCuoi] = ketThuc.split(":").map(Number);
  return Math.max(0, gioCuoi + phutCuoi / 60 - gioDau - phutDau / 60);
}

function caChongGio(caThuNhatId, caThuHaiId) {
  const caThuNhat = layThongTinCa(caThuNhatId);
  const caThuHai = layThongTinCa(caThuHaiId);
  if (!caThuNhat?.gio?.includes("-") || !caThuHai?.gio?.includes("-")) return false;

  const doiSangPhut = (giaTri) => {
    const [gio, phut] = giaTri.trim().split(":").map(Number);
    return gio * 60 + phut;
  };
  const [batDauMot, ketThucMot] = caThuNhat.gio.split("-").map(doiSangPhut);
  const [batDauHai, ketThucHai] = caThuHai.gio.split("-").map(doiSangPhut);
  return batDauMot < ketThucHai && batDauHai < ketThucMot;
}

function nhanVienDaCoHoacChongCa(nhanVien, ngayStr, caLamId) {
  return (nhanVien.lich[ngayStr] || []).some((lich) =>
    String(lich.ca).toLowerCase() === String(caLamId).toLowerCase()
      || caChongGio(lich.ca, caLamId)
  );
}

</script>

<template>
  <LichLamViecNhanVien v-if="!laAdmin" />
  
  <div v-else class="schedule-page space-y-5">

    <!-- ───── HEADER TÙY CHỌN ───── -->
    <div class="bg-white rounded-[16px] border border-slate-200 p-5 shadow-sm mb-6">
      <div class="flex items-center gap-3 mb-4">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
          <Filter class="h-5 w-5" />
        </div>
        <h2 class="admin-section-title">Bộ lọc</h2>
      </div>
      
      <div class="flex flex-wrap items-center gap-4">
        <!-- Filters -->
        <div class="flex flex-wrap items-center gap-4">
          <div class="flex items-center gap-2">
            <span class="text-[13px] font-medium text-slate-700">Nhân viên</span>
            <div class="relative w-40">
              <input v-model="timKiemNhanVien" type="text" placeholder="Tìm kiếm..." class="w-full h-9 pl-3 pr-3 text-[13px] border border-slate-200 rounded-lg focus:outline-none focus:border-rose-400 transition" />
            </div>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-[13px] font-medium text-slate-700">Ca làm:</span>
            <div class="relative w-32">
              <select v-model="boLocCaLam" class="w-full h-9 px-3 text-[13px] border border-slate-200 rounded-lg focus:outline-none focus:border-rose-400 transition bg-white cursor-pointer">
                <option value="">Tất cả ca</option>
                <option v-for="ca in DS_CA" :key="ca.id" :value="ca.id">{{ ca.nhan }}</option>
              </select>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-[13px] font-medium text-slate-700">Ngày làm:</span>
            <div class="relative flex items-center h-9 border border-slate-200 rounded-lg bg-white overflow-hidden focus-within:border-rose-400 transition">
              <input type="date" v-model="ngayLocSelect" class="w-32 h-full px-3 text-[13px] text-slate-700 outline-none bg-transparent cursor-pointer" />
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex flex-wrap items-center gap-2 ml-auto">
          <!-- Xếp ca tự động (Duy trì chức năng cũ) -->
          <button @click="xepCaDong" :disabled="!coNgayTuongLaiTrongTuan" :title="coNgayTuongLaiTrongTuan ? 'Tạo một lịch khung cho mỗi ca trong tương lai' : 'Không còn ngày tương lai để xếp ca'" class="h-9 px-3 flex items-center gap-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 text-[13px] font-medium transition shadow-sm disabled:opacity-45 disabled:cursor-not-allowed disabled:hover:bg-slate-100">
            <Shuffle class="w-4 h-4" />
            <span>Xếp tự động</span>
          </button>
          <button @click="datLaiLichTuongLai" :disabled="!coNgayTuongLaiTrongTuan" :title="coNgayTuongLaiTrongTuan ? 'Xóa lịch từ ngày mai đến cuối tuần đang xem' : 'Không còn ngày tương lai để đặt lại'" class="h-9 px-3 flex items-center gap-1.5 rounded-lg border border-rose-200 bg-white hover:bg-rose-50 text-rose-600 text-[13px] font-medium transition disabled:opacity-45 disabled:cursor-not-allowed disabled:hover:bg-white">
            <RotateCcw class="w-4 h-4" />
            <span>Đặt lại</span>
          </button>
          <!-- Tải template button (red) -->
          <button @click="xuatExcel" class="h-9 px-3 flex items-center gap-1.5 rounded-lg bg-rose-500 hover:bg-rose-600 text-white text-[13px] font-medium transition shadow-sm">
            <Download class="w-4 h-4" />
            <span>Tải mẫu</span>
          </button>
          <!-- Import Excel button (dark navy) -->
          <button class="h-9 px-3 flex items-center gap-1.5 rounded-lg bg-slate-900 hover:bg-slate-800 text-white text-[13px] font-medium transition shadow-sm">
            <Upload class="w-4 h-4" />
            <span>Import</span>
          </button>
          <!-- Thêm mới lịch làm việc -->
          <button @click="moModalThemCaTuHeader" class="h-9 px-3 flex items-center gap-1.5 rounded-lg bg-rose-500 hover:bg-rose-600 text-white text-[13px] font-medium transition shadow-sm">
            <Plus class="w-4 h-4" />
            <span>Thêm lịch</span>
          </button>
        </div>
      </div>
    </div>

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

    <!-- ───── CONTENT (BẢNG & LỊCH) ───── -->
    <div class="bg-white rounded-[16px] border border-slate-200 p-5 shadow-sm">
      <div class="flex flex-wrap items-center justify-between mb-5 gap-3">
        <div class="flex items-center gap-2 text-slate-800">
          <TableIcon v-if="viewMode === 'bang'" class="w-5 h-5" />
          <List v-else class="w-5 h-5" />
          <span class="font-bold text-[16px]">Danh sách lịch làm việc</span>
        </div>
        
        <!-- View Toggle -->
        <div class="flex items-center p-1 bg-slate-100 rounded-lg border border-slate-200">
          <button 
            @click="viewMode = 'bang'" 
            class="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-[13px] font-bold transition-all"
            :class="viewMode === 'bang' ? 'bg-rose-500 text-white shadow-sm' : 'text-slate-500 hover:text-slate-700'"
          >
            <TableIcon class="w-4 h-4" />
            <span>Bảng</span>
          </button>
          <button 
            @click="viewMode = 'lich'" 
            class="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-[13px] font-bold transition-all"
            :class="viewMode === 'lich' ? 'bg-rose-500 text-white shadow-sm' : 'text-slate-500 hover:text-slate-700'"
          >
            <CalendarDays class="w-4 h-4" />
            <span>Lịch</span>
          </button>
        </div>
      </div>

      <!-- TABLE VIEW -->
      <div v-if="viewMode === 'bang'">
        <div v-if="dangTai" class="py-10 text-center text-sm text-slate-400">
          Đang tải dữ liệu lịch làm việc...
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50/50 border-y border-slate-100">
                <th class="py-2.5 px-3 text-[11px] font-bold text-slate-500 tracking-wider w-12 text-center">STT</th>
                <th class="py-2.5 px-3 text-[11px] font-bold text-slate-500 tracking-wider">Mã nhân viên</th>
                <th class="py-2.5 px-3 text-[11px] font-bold text-slate-500 tracking-wider">Nhân viên</th>
                <th class="py-2.5 px-3 text-[11px] font-bold text-slate-500 tracking-wider">Ca làm</th>
                <th class="py-2.5 px-3 text-[11px] font-bold text-slate-500 tracking-wider text-center">Thời gian</th>
                <th class="py-2.5 px-3 text-[11px] font-bold text-slate-500 tracking-wider text-center">Ngày làm</th>
                <th class="py-2.5 px-3 text-[11px] font-bold text-slate-500 tracking-wider text-center w-24 whitespace-nowrap">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, idx) in danhSachCaLamBangLocPhanTrang" :key="idx" class="border-b border-slate-100 hover:bg-slate-50/50 transition">
                <td class="py-2.5 px-3 text-[12px] text-slate-600 text-center">{{ (trangHienTai - 1) * soPhanTuMotTrang + idx + 1 }}</td>
                <td class="py-2.5 px-3 text-[12px] text-slate-600">{{ item.nv.ma }}</td>
                <td class="py-2.5 px-3 text-[12px] font-medium text-slate-800">{{ item.nv.ten }}</td>
                <td class="py-2.5 px-3 text-[12px] text-slate-600">{{ item.caInfo.nhan }}</td>
                <td class="py-2.5 px-3 text-[12px] text-slate-600 text-center">{{ item.caInfo.gio }}</td>
                <td class="py-2.5 px-3 text-[12px] text-slate-600 text-center">{{ formatNgay(item.ngay) }}/{{ item.ngay.getFullYear() }}</td>
                <td class="py-2.5 px-3 text-center whitespace-nowrap">
                  <div class="flex items-center justify-center gap-2">
                    <button @click="xemChiTietCaTuBang(item)" :disabled="laCaDaKhoa(item.ngay, item.caId)" class="admin-table-action text-slate-600 hover:text-slate-900 disabled:opacity-35 disabled:cursor-not-allowed disabled:hover:text-slate-600" :title="laCaDaKhoa(item.ngay, item.caId) ? 'Lịch đã khóa' : 'Xem chi tiết'">
                      <Eye :size="14" />
                    </button>
                    <button @click="xoaCaTuBang(item.nv, item.ngayStr, item.caInfo)" :disabled="laCaDaKhoa(item.ngay, item.caId)" class="admin-table-action text-rose-500 hover:text-rose-700 disabled:opacity-35 disabled:cursor-not-allowed disabled:hover:text-rose-500" :title="laCaDaKhoa(item.ngay, item.caId) ? 'Lịch đã khóa, chỉ được xem' : 'Xóa ca'">
                      <Trash2 :size="14" />
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="danhSachCaLamBangLocPhanTrang.length === 0">
                <td colspan="7" class="py-10 text-center text-[13px] text-slate-500">
                  Không có dữ liệu ca làm việc phù hợp.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <AdminTableFooter
          :current-page="trangHienTai"
          :page-size="soPhanTuMotTrang"
          :page-size-options="pageSizeOptions"
          :total-items="danhSachCaLamBangLoc.length"
          :total-pages="tongSoTrang"
          compact
          show-refresh
          @refresh="taiNhanVien"
          @update:current-page="trangHienTai = $event"
          @update:page-size="soPhanTuMotTrang = $event"
        />
      </div>

      <!-- BOARD VIEW -->
      <div v-else>
          <!-- ── Bảng lịch ── -->
          <section class="schedule-board rounded-[16px] border border-slate-200 bg-white shadow-sm overflow-hidden">
            <!-- Thanh điều hướng Header -->
            <div class="flex flex-wrap items-center justify-between p-4 border-b border-slate-200">
              <!-- Điều hướng ngày/tuần -->
              <div class="flex items-center gap-3">
                <button @click="tuanTruoc" class="flex h-8 w-8 items-center justify-center rounded-md border border-slate-200 text-slate-600 hover:bg-slate-50 transition shadow-sm">
                  <ChevronLeft class="h-4 w-4" />
                </button>
                <h2 class="text-lg font-bold text-slate-800 min-w-[150px] text-center">
                  {{ formatTuanHienThi() }}
                </h2>
                <button @click="tuanSau" class="flex h-8 w-8 items-center justify-center rounded-md border border-slate-200 text-slate-600 hover:bg-slate-50 transition shadow-sm">
                  <ChevronRight class="h-4 w-4" />
                </button>
              </div>
          
              <!-- Toggle Ngày / Tuần / Tháng -->
              <div class="flex items-center rounded-md border border-slate-200 overflow-hidden bg-white shadow-sm">
                <button @click="calendarMode = 'ngay'" :class="calendarMode === 'ngay' ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-50'" class="px-5 py-1.5 text-[13px] font-bold transition">Ngày</button>
                <button @click="calendarMode = 'tuan'" :class="calendarMode === 'tuan' ? 'bg-slate-900 text-white' : 'text-slate-600 border-l border-r border-slate-200 hover:bg-slate-50'" class="px-5 py-1.5 text-[13px] font-bold transition">Tuần</button>
                <button @click="calendarMode = 'thang'" :class="calendarMode === 'thang' ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-50'" class="px-5 py-1.5 text-[13px] font-bold transition">Tháng</button>
              </div>
            </div>
          
            <!-- Loading -->
            <div v-if="dangTai" class="py-10 text-center text-sm text-slate-400">
              Đang tải dữ liệu lịch làm việc...
            </div>
            
            <!-- Bảng Lịch Tháng (Monthly Grid) -->
            <div v-else-if="calendarMode === 'thang'" class="w-full bg-white border-t border-l border-slate-200">
              <!-- Grid Header (Days of week) -->
              <div class="grid grid-cols-7 border-b border-slate-200 bg-slate-50">
                <div v-for="thu in ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN']" :key="thu" 
                     class="py-3 text-center text-[13px] font-bold text-slate-600 border-r border-slate-200">
                  {{ thu }}
                </div>
              </div>
              
              <!-- Grid Body (Weeks) -->
              <div class="flex flex-col">
                <div v-for="(tuan, tuanIdx) in cacNgayTrongThang" :key="'w-'+tuanIdx" class="grid grid-cols-7 border-b border-slate-200">
                  <div v-for="(ngay, ngayIdx) in tuan" :key="'d-'+ngayIdx" 
                       class="min-h-[110px] p-2 border-r border-slate-200 relative group transition cursor-pointer flex flex-col"
                       :class="[
                         ngay.getMonth() !== ngayHienTai.getMonth() ? 'bg-slate-50/50' : 'bg-white hover:bg-slate-50/70',
                         laNgayQuaKhu(ngay) ? 'schedule-past-cell cursor-not-allowed' : ''
                       ]"
                       @click="moModalThemCaThang(ngay)">
                    
                    <!-- Header ô: Ngày -->
                    <div class="flex justify-end mb-1">
                      <span class="text-[13px] font-medium" :class="[
                        formatISODate(ngay) === formatISODate(new Date()) ? 'bg-blue-600 text-white w-6 h-6 rounded-full flex items-center justify-center' : 
                        (ngay.getMonth() !== ngayHienTai.getMonth() ? 'text-slate-400' : 'text-slate-700')
                      ]">
                        {{ ngay.getDate() }}
                      </span>
                    </div>

                    <!-- Dữ liệu + Nút + (chỉ hiển thị khi hover) -->
                    <div class="flex-1 flex flex-col justify-center items-center opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                      <!-- Danh sách nhân viên có lịch -->
                      <div v-if="nhanVienCoLichTrongNgay(ngay).length > 0" class="flex flex-wrap gap-1 mb-2 justify-center">
                         <div v-for="(item, idx) in nhanVienCoLichTrongNgay(ngay).slice(0, 4)" :key="idx" 
                              class="w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-bold text-white shadow-sm ring-1 ring-white"
                              :class="layThongTinCa(item.ca)?.mau || 'bg-slate-400'"
                              :title="`${item.nv.ten} - ${layThongTinCa(item.ca)?.nhan || item.ca}`">
                            {{ item.nv.vieTat }}
                         </div>
                         <div v-if="nhanVienCoLichTrongNgay(ngay).length > 4" class="w-6 h-6 rounded-full bg-slate-200 flex items-center justify-center text-[10px] font-bold text-slate-600 shadow-sm ring-1 ring-white">
                           +{{ nhanVienCoLichTrongNgay(ngay).length - 4 }}
                         </div>
                      </div>
                      <!-- Nút + -->
                      <div class="w-7 h-7 rounded-full flex items-center justify-center shadow-md transition hover:scale-110"
                           :class="laCaDaKhoa(ngay, DS_CA[0]?.id) ? 'bg-slate-400' : 'bg-rose-500 hover:bg-rose-600'">
                        <Plus class="w-4 h-4 text-white" />
                      </div>
                    </div>

                  </div>
                </div>
              </div>
            </div>

            <!-- Bảng Ma trận (Matrix) -->
            <div v-else class="overflow-x-auto">
              <table class="w-full border-collapse min-w-[900px]">
                <tbody>
                  <!-- Hàng hiển thị chi tiết thứ & ngày -->
                  <tr>
                    <td class="w-24 min-w-[96px] border-b border-r border-slate-200 bg-slate-50 p-1.5 align-middle sticky left-0 z-20">
                      <div style="font-size: 12px;" class="font-bold text-slate-500 uppercase tracking-wider text-center">CA LÀM VIỆC</div>
                    </td>
                    <td v-for="day in lichBoard" :key="'td-'+day.ngayStr" class="border-b border-r border-slate-200 p-2 text-center bg-slate-50/50">
                      <div v-if="calendarMode === 'tuan' || (calendarMode === 'ngay' && day.ngayStr === formatISODate(ngayHienTai))">
                        <div style="font-size: 13px;" class="font-bold text-slate-800">{{ day.thu }}</div>
                        <div style="font-size: 12px;" class="text-slate-500">{{ formatNgay(day.ngay) }}/{{ day.ngay.getFullYear() }}</div>
                      </div>
                    </td>
                  </tr>
                  
                  <!-- Các hàng Ca làm -->
                  <tr v-for="caInfo in DS_CA" :key="caInfo.id">
                    <!-- Cột thông tin ca -->
                    <td class="w-24 min-w-[96px] border-b border-r border-slate-200 p-1.5 align-top text-center shadow-[2px_0_5px_-2px_rgba(0,0,0,0.1)] sticky left-0 z-20" :class="caInfo.muaNhat">
                      <div style="font-size: 13px;" class="font-bold">{{ caInfo.nhan }}</div>
                      <div style="font-size: 10px;" class="opacity-70 mt-0.5">{{ caInfo.gio }}</div>
                    </td>
                    <!-- Các cột ngày -->
                    <td v-for="day in lichBoard" :key="day.ngayStr + '-' + caInfo.id" 
                        class="border-b border-r border-slate-200 p-1.5 align-top min-w-[120px] transition group relative bg-white"
                        :class="[
                          calendarMode === 'ngay' && day.ngayStr !== formatISODate(ngayHienTai) ? 'opacity-20 cursor-not-allowed pointer-events-none' : 'hover:bg-slate-50',
                          laNgayQuaKhu(day.ngay) ? 'schedule-past-cell pointer-events-none cursor-not-allowed' : ''
                        ]">
                      
                      <div v-if="calendarMode === 'tuan' || day.ngayStr === formatISODate(ngayHienTai)" class="flex flex-col gap-1.5 min-h-[50px]">
                        
                        <!-- Thẻ tóm tắt ca làm -->
                        <div class="rounded-[10px] p-2 border transition flex flex-col items-center justify-center shadow-sm h-full" :class="caInfo.muaNhat">
                          <div style="font-size: 13px;" class="font-bold">{{ caInfo.nhan }}</div>
                          <div style="font-size: 10px;" class="opacity-70 mt-0.5">{{ caInfo.gio }}</div>
                          <div style="font-size: 12px;" class="mt-1 font-medium text-slate-700">
                            {{ day.cas.find(c => c.id === caInfo.id)?.nhanViens.length || 0 }} Nhân viên
                          </div>
                          <button @click="xemChiTietCa(day, day.cas.find(c => c.id === caInfo.id))" style="font-size: 12px;" class="mt-2 px-3 py-1 rounded-full border bg-white font-medium transition hover:shadow-sm" :class="caInfo.nut">
                            Xem thêm
                          </button>
                        </div>
                        
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
      </div>
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

          <!-- Footer: Nút Thêm (ẩn khi ca đã khóa) -->
          <div v-if="!caHienTaiBiKhoa" class="border-t border-slate-100 p-5 bg-slate-50">
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

    <!-- ======================= MODAL 2: THÊM MỚI LỊCH ======================= -->
    <Teleport to="body">
      <div v-if="showModalThemCa" class="fixed inset-0 z-[110] flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm" @click="huyThemCa"></div>
        <div class="relative w-full max-w-sm overflow-hidden rounded-[24px] bg-white shadow-xl animate-in zoom-in-95 duration-200">
          
          <!-- Header -->
          <div class="border-b border-slate-100 p-5 pb-4">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-bold text-slate-800">Thêm Mới Lịch</h3>
              <button @click="huyThemCa" class="rounded-full p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition">
                <X class="w-5 h-5" />
              </button>
            </div>
          </div>

          <!-- Body -->
          <div class="p-5 space-y-5">

            <!-- Chọn nhiều nhân viên -->
            <div class="space-y-1.5">
              <div class="flex justify-between items-end">
                <label class="text-sm font-bold text-slate-700">Nhân viên <span class="text-rose-500">*</span></label>
                <span class="text-xs text-slate-500 font-medium">Đã chọn: <span class="text-rose-500 font-bold">{{ danhSachChonNhanVienIds.length }}</span></span>
              </div>
              <div class="w-full max-h-48 overflow-y-auto border border-slate-200 rounded-xl bg-white p-2 space-y-1 custom-scrollbar">
                <div v-if="nhanVienKhaDung.length === 0" class="text-xs text-rose-500 p-4 text-center">
                  Không còn nhân viên nào khả dụng để thêm.
                </div>
                <label v-for="nv in nhanVienKhaDung" :key="nv.id" class="flex items-center gap-3 p-2 hover:bg-slate-50 rounded-lg cursor-pointer transition border border-transparent hover:border-slate-100">
                  <input type="checkbox" v-model="danhSachChonNhanVienIds" :value="nv.id" class="w-4 h-4 rounded text-rose-500 focus:ring-rose-500 border-slate-300">
                  <div class="flex items-center gap-2">
                     <img v-if="nv.hinhAnh" :src="nv.hinhAnh" class="w-7 h-7 rounded-full object-cover shadow-sm ring-1 ring-slate-200" />
                     <div v-else class="w-7 h-7 rounded-full flex items-center justify-center text-[10px] font-bold text-white shadow-sm ring-1 ring-slate-200" :class="nv.mauNen">
                        {{ nv.vieTat }}
                     </div>
                     <span class="text-sm font-medium text-slate-700">{{ nv.ten }}</span>
                  </div>
                </label>
              </div>
            </div>

            <!-- Ca làm việc -->
            <div class="space-y-1.5">
              <label class="text-sm font-bold text-slate-700">Ca làm việc <span class="text-rose-500">*</span></label>
              <select 
                v-model="chonCaVal" 
                class="w-full h-11 px-3 text-sm border border-slate-200 rounded-xl bg-white text-slate-700 focus:outline-none focus:border-slate-400 transition"
              >
                <option v-for="ca in DS_CA" :key="ca.id" :value="ca.id">
                  {{ ca.nhan }} ({{ ca.gio }})
                </option>
              </select>
            </div>

            <!-- Ngày làm -->
            <div class="space-y-1.5">
              <label class="text-sm font-bold text-slate-700">Ngày làm <span class="text-rose-500">*</span></label>
              <input 
                type="date" 
                v-model="chonNgayVal" 
                :min="formatISODate(new Date())"
                class="w-full h-11 px-3 text-sm border border-slate-200 rounded-xl bg-white text-slate-700 focus:outline-none focus:border-slate-400 transition"
              />
            </div>

          </div>

          <!-- Footer -->
          <div class="flex items-center gap-3 p-5 pt-0">
            <button @click="huyThemCa" class="flex-1 py-2.5 rounded-xl border border-slate-200 text-sm font-bold text-slate-600 hover:bg-slate-50 transition">
              Hủy
            </button>
            <button 
              @click="luuCa" 
              :disabled="danhSachChonNhanVienIds.length === 0 || dangTai" 
              class="flex-1 py-2.5 rounded-xl bg-gradient-to-r from-rose-500 to-rose-600 hover:from-rose-600 hover:to-rose-700 text-white text-sm font-bold transition disabled:opacity-50 disabled:cursor-not-allowed shadow-md hover:shadow-lg"
            >
              {{ dangTai ? 'Đang lưu...' : 'Thêm mới' }}
            </button>
          </div>

        </div>
      </div>
    </Teleport>

    <!-- ======================= MODAL 3: XEM LỊCH NGÀY (QUÁ KHỨ) ======================= -->
    <Teleport to="body">
      <div v-if="showModalXemNgay && xemNgayData" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm" @click="showModalXemNgay = false"></div>
        <div class="relative w-full max-w-md overflow-hidden rounded-[24px] bg-white shadow-xl animate-in zoom-in-95 duration-200">
          
          <!-- Header -->
          <div class="border-b border-slate-100 p-5 pb-4">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-bold text-slate-800">
                Lịch làm {{ xemNgayData.thu }} ({{ formatNgay(xemNgayData.ngay) }})
              </h3>
              <button @click="showModalXemNgay = false" class="rounded-full p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="text-xs text-slate-400 mt-1">Ngày đã qua — chỉ xem</div>
          </div>

          <!-- Body: Nhóm theo ca -->
          <div class="p-5 max-h-[60vh] overflow-y-auto space-y-4">
            <div v-if="xemNgayData.caGroups.length === 0" class="text-center py-6 text-sm text-slate-400">
              Không có nhân viên nào được phân ca trong ngày này.
            </div>

            <div v-for="group in xemNgayData.caGroups" :key="group.id" class="rounded-xl border overflow-hidden" :class="group.muaNhat">
              <!-- Ca Header -->
              <div class="flex items-center gap-2 px-4 py-2.5" :class="group.muaNhat">
                <div class="w-2.5 h-2.5 rounded-full" :class="group.mau"></div>
                <span class="text-sm font-bold">{{ group.nhan }}</span>
                <span class="text-xs opacity-70">{{ group.gio }}</span>
                <span class="ml-auto text-xs font-bold opacity-60">{{ group.nhanViens.length }} nhân viên</span>
              </div>

              <!-- Danh sách nhân viên -->
              <div class="divide-y divide-slate-100">
                <div v-for="nv in group.nhanViens" :key="nv.id" class="flex items-center gap-3 px-4 py-2.5 bg-white">
                  <img v-if="nv.hinhAnh" :src="nv.hinhAnh" class="w-8 h-8 rounded-full object-cover shadow-sm ring-1 ring-slate-200" />
                  <div v-else :class="['flex w-8 h-8 items-center justify-center rounded-full text-xs font-bold text-white shadow-sm ring-1 ring-slate-200', nv.mauNen]">
                    {{ nv.vieTat }}
                  </div>
                  <div>
                    <div class="text-sm font-medium text-slate-800">{{ nv.ten }}</div>
                    <div class="text-[11px] text-slate-400">{{ nv.ma }}</div>
                  </div>
                </div>
              </div>
            </div>

          </div>

          <!-- Footer -->
          <div class="border-t border-slate-100 p-4 bg-slate-50">
            <button @click="showModalXemNgay = false" class="w-full py-2.5 rounded-xl border border-slate-200 bg-white text-sm font-bold text-slate-600 hover:bg-slate-50 transition">
              Đóng
            </button>
          </div>

        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.schedule-past-cell {
  opacity: 0.52;
  background-color: rgb(248 250 252);
  background-image: repeating-linear-gradient(
    -45deg,
    transparent,
    transparent 8px,
    rgba(148, 163, 184, 0.12) 8px,
    rgba(148, 163, 184, 0.12) 10px
  );
}
</style>
