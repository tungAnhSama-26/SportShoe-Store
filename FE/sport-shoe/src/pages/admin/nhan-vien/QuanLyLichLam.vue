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
  X,
  List,
  Trash2,
  Upload,
  Search,
  SlidersHorizontal,
  Table as TableIcon,
  Eye
} from "lucide-vue-next";
import { layDanhSachNhanVien } from "../../../services/nhan-vien.js";
import {
  layLichLamViec,
  phanCa,
  xepCaTuDong,
} from "../../../services/lich-lam.js";
import { showSuccess, showError, showConfirm } from "../../../utils/alert.js";
import { getDisplayErrorMessage } from "../../../utils/error-message.js";
import { exportRowsToExcel } from "../../../utils/export-excel.js";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import { useAdminSession } from "../../../composable/useAdminSession.js";
import LichLamViecNhanVien from "./LichLamViecNhanVien.vue";

const route = useRoute();
const router = useRouter();

const { adminSession } = useAdminSession();
const laAdmin = computed(() => adminSession.value.vaiTro === "Quản trị viên" || adminSession.value.vaiTro === "Admin");

const MAX_NHAN_VIEN_MOI_CA = 3;

import { layDanhSachCaLam } from "../../../services/ca-lam.js";

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
    gioBatDau: "08:00",
  },
  {
    id: "chieu",
    nhan: "Ca chiều",
    gio: "12:00 - 17:00",
    mau: "bg-orange-400",
    muaNhat: "bg-orange-50 border-orange-200 text-orange-700",
    gioBatDau: "12:00",
  },
  {
    id: "toi",
    nhan: "Ca tối",
    gio: "17:00 - 22:00",
    mau: "bg-violet-400",
    muaNhat: "bg-violet-50 border-violet-200 text-violet-700",
    gioBatDau: "17:00",
  },
]);

async function taiDanhSachCa() {
  try {
    const list = await layDanhSachCaLam();
    const activeList = list.filter(c => c.trangThai);
    if (activeList.length > 0) {
      const mapped = activeList.map((c, idx) => {
        const colorSet = SHIFT_COLORS[idx % SHIFT_COLORS.length];
        return {
          id: c.id,
          nhan: c.ten,
          gio: `${c.gioBatDau} - ${c.gioKetThuc}`,
          mau: colorSet.mau,
          muaNhat: colorSet.muaNhat,
          gioBatDau: c.gioBatDau,
        };
      });
      mapped.sort((a, b) => {
        const tA = a.gioBatDau || "00:00";
        const tB = b.gioBatDau || "00:00";
        return tA.localeCompare(tB);
      });
      DS_CA.value = mapped;
    }
  } catch (e) {
    console.error("Không thể tải danh sách ca làm việc", e);
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

function nhanVienCoLichTrongNgay(d) {
  const dateStr = formatISODate(d);
  const result = [];
  danhSachLocVaiTro.value.forEach(nv => {
    if (nv.lich[dateStr]) {
      result.push({ nv, ca: nv.lich[dateStr] });
    }
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

function homNay() {
  ngayHienTai.value = new Date();
}

// Khi thay đổi mode, tải lại dữ liệu vì chu kỳ thời gian thay đổi
watch(calendarMode, () => {
  if (laAdmin.value) {
    taiDuLieuLich();
  }
});

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
      nv.trangThaiChamCong = {};
      let countCa = 0;
      
      const lichNhanVien = lichData.filter(l => String(l.nhanVienId) === String(nv.id));
      lichNhanVien.forEach(l => {
        nv.lich[l.ngay] = l.ca;
        nv.trangThaiChamCong[l.ngay] = l.trangThaiChamCong;
        countCa++;
      });

      nv.tongGio = countCa * 4;
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
      trangThaiChamCong: {},
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
  if (laAdmin.value) {
    taiNhanVien();
  }
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
        const nhanViens = danhSachLocVaiTro.value.filter(
          (nv) => nv.lich[ngayStr] && nv.lich[ngayStr].toLowerCase() === caInfo.id.toLowerCase()
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
  const todayStr = formatISODate(new Date());

  danhSachLocVaiTro.value.forEach((nv) => {
    cacNgayTrongTuan.value.forEach((d) => {
      const ngayStr = formatISODate(d);
      const caId = nv.lich[ngayStr];
      if (caId && ngayStr <= todayStr) {
        const caInfo = layThongTinCa(caId);
        result.push({
          nv,
          caId,
          caInfo,
          ngay: d,
          ngayStr,
        });
      }
    });
  });
  // Sắp xếp theo ngày giảm dần, rồi đến giờ ca làm bắt đầu sớm hơn lên trước
  result.sort((a, b) => {
    if (a.ngayStr !== b.ngayStr) return b.ngayStr.localeCompare(a.ngayStr);
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

// ───────── Hiển thị modal Thêm nhân viên (Modal 2) ─────────
const showModalThemCa = ref(false);
const chonNhanVienId = ref("");
const chonNgayVal = ref("");
const chonCaVal = ref("sang");

// Giờ bắt đầu của mỗi ca
const GIO_BAT_DAU_CA = computed(() => {
  const map = {};
  DS_CA.value.forEach(c => {
    const startHourStr = c.gio.split("-")[0].trim().split(":")[0];
    map[c.id] = parseInt(startHourStr, 10) || 8;
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
    const gioBatDau = GIO_BAT_DAU_CA.value[caId];
    if (gioBatDau !== undefined && now.getHours() >= gioBatDau) return true;
  }

  return false;
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
  const defaultCaId = DS_CA.value[0]?.id || 'sang';
  const isPast = laCaDaKhoa(ngay, defaultCaId);

  if (isPast) {
    // Ngày quá khứ: mở modal xem danh sách nhân viên nhóm theo ca
    const thuIdx = (ngay.getDay() + 6) % 7;
    const caGroups = DS_CA.value.map(caInfo => {
      const nhanViens = danhSachLocVaiTro.value.filter(nv => nv.lich[ngayStr] === caInfo.id);
      return { ...caInfo, nhanViens };
    }).filter(g => g.nhanViens.length > 0);

    xemNgayData.value = {
      ngay,
      ngayStr,
      thu: NHAN_TUAN[thuIdx],
      caGroups
    };
    showModalXemNgay.value = true;
    return;
  }

  chonNgayVal.value = ngayStr;
  chonCaVal.value = DS_CA.value[0]?.id || "sang";
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
    const defaultCaId = DS_CA.value[0]?.id || 'sang';
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
  if (!currentChiTietCa.value && !isThemCaThang.value) {
    return danhSachLocVaiTro.value;
  }
  
  if (currentChiTietCa.value) {
    const { ca } = currentChiTietCa.value;
    return danhSachLocVaiTro.value.filter(
      (nv) => !ca.nhanViens.some((nvInCa) => nvInCa.id === nv.id)
    );
  }

  // Nếu là Thêm Ca Tháng: Lọc những nhân viên CHƯA có ca trong ngày đó
  const ngayCheck = chonNgayVal.value;
  return danhSachLocVaiTro.value.filter(nv => !nv.lich[ngayCheck]);
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
    if (ca.nhanViens.length + danhSachChonNhanVienIds.value.length > MAX_NHAN_VIEN_MOI_CA) {
      showError(`Ca này chỉ còn nhận thêm tối đa ${MAX_NHAN_VIEN_MOI_CA - ca.nhanViens.length} nhân viên.`);
      return;
    }
  } else {
    ngayStr = chonNgayVal.value;
    caId = chonCaVal.value;
    
    // Đếm số người đã có trong ca này
    const dem = danhSachLocVaiTro.value.filter(nv => nv.lich[ngayStr] === caId).length;
    if (dem + danhSachChonNhanVienIds.value.length > MAX_NHAN_VIEN_MOI_CA) {
      showError(`Ca này chỉ còn nhận thêm tối đa ${MAX_NHAN_VIEN_MOI_CA - dem} nhân viên.`);
      return;
    }
  }

  dangTai.value = true;
  try {
    const promises = danhSachChonNhanVienIds.value.map(nvId => 
      phanCa({
        nhanVienId: nvId,
        ngay: ngayStr,
        ca: caId,
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
    await phanCa({
      nhanVienId: nhanVien.id,
      ngay: day.ngayStr,
      ca: null,
    });
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
    await phanCa({
      nhanVienId: nv.id,
      ngay: ngayStr,
      ca: null,
    });
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

function tenCaXuatExcel(ca) {
  const thongTinCa = layThongTinCa(ca);
  return thongTinCa ? `${thongTinCa.nhan} (${thongTinCa.gio})` : "Nghỉ";
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
  if (found) return found;
  return {
    id: id,
    nhan: id,
    gio: "—",
    mau: "bg-slate-400",
    muaNhat: "bg-slate-50 border-slate-200 text-slate-700"
  };
}

</script>

<template>
  <LichLamViecNhanVien v-if="!laAdmin" />
  
  <div v-else class="schedule-page space-y-5">

    <!-- ───── HEADER TÙY CHỌN ───── -->
    <div class="bg-white rounded-[16px] border border-slate-200 p-5 shadow-sm">
      <div class="flex items-center gap-2 mb-4 text-slate-700">
        <SlidersHorizontal class="w-4 h-4" />
        <span class="font-bold text-[15px]">Bộ lọc</span>
      </div>
      <div class="flex flex-wrap items-center justify-between gap-4">
        <!-- Filters -->
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2">
            <span class="text-[13px] font-medium text-slate-700">Nhân viên <span class="text-rose-500"></span></span>
            <div class="relative w-48">
              <input v-model="timKiemNhanVien" type="text" placeholder="Tìm kiếm nhân viên..." class="w-full h-9 pl-3 pr-3 text-[13px] border border-slate-200 rounded-lg focus:outline-none focus:border-rose-400 transition" />
            </div>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-[13px] font-medium text-slate-700">Ca làm:</span>
            <div class="relative w-40">
              <select v-model="boLocCaLam" class="w-full h-9 px-3 text-[13px] border border-slate-200 rounded-lg focus:outline-none focus:border-rose-400 transition bg-white cursor-pointer">
                <option value="">Tất cả ca</option>
                <option v-for="ca in DS_CA" :key="ca.id" :value="ca.id">{{ ca.nhan }}</option>
              </select>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-[13px] font-medium text-slate-700">Ngày làm:</span>
            <div class="relative flex items-center h-9 border border-slate-200 rounded-lg bg-white overflow-hidden focus-within:border-rose-400 transition">
              <CalendarDays class="absolute left-3 w-4 h-4 text-slate-400 pointer-events-none" />
              <input type="date" v-model="ngayLocSelect" class="w-36 h-full pl-9 pr-2 text-[13px] text-slate-700 outline-none bg-transparent cursor-pointer" />
            </div>
          </div>
        </div>
        
        <!-- Actions -->
        <div class="flex items-center gap-2">
          <!-- Xếp ca tự động (Duy trì chức năng cũ) -->
          <button @click="xepCaDong" class="h-9 px-4 flex items-center gap-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 text-[13px] font-medium transition shadow-sm">
            <Shuffle class="w-4 h-4" />
            <span>Xếp ca tự động</span>
          </button>
          <!-- Tải template button (red) -->
          <button @click="xuatExcel" class="h-9 px-4 flex items-center gap-2 rounded-lg bg-rose-500 hover:bg-rose-600 text-white text-[13px] font-medium transition shadow-sm">
            <Download class="w-4 h-4" />
            <span>Tải template</span>
          </button>
          <!-- Import Excel button (dark navy) -->
          <button class="h-9 px-4 flex items-center gap-2 rounded-lg bg-slate-900 hover:bg-slate-800 text-white text-[13px] font-medium transition shadow-sm">
            <Upload class="w-4 h-4" />
            <span>Import Excel</span>
          </button>
          <!-- Thêm mới lịch làm việc -->
          <button @click="moModalThemCaTuHeader" class="h-9 px-4 flex items-center gap-2 rounded-lg bg-rose-500 hover:bg-rose-600 text-white text-[13px] font-medium transition shadow-sm">
            <Plus class="w-4 h-4" />
            <span>Thêm mới lịch làm việc</span>
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
                    <button @click="xemChiTietCa({ ngay: item.ngay, ngayStr: item.ngayStr, thu: '' }, item.caInfo)" class="w-8 h-8 inline-flex items-center justify-center rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-50 transition shadow-sm" title="Xem chi tiết">
                      <Eye class="w-4 h-4" />
                    </button>
                    <button @click="xoaCaTuBang(item.nv, item.ngayStr, item.caInfo)" class="w-8 h-8 inline-flex items-center justify-center rounded-lg border border-slate-200 text-slate-600 hover:bg-rose-50 hover:text-rose-500 hover:border-rose-200 transition shadow-sm" title="Xóa ca">
                      <Trash2 class="w-4 h-4" />
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
                <button @click="homNay" class="ml-2 h-8 px-3 rounded-md border border-slate-200 text-[13px] font-medium text-slate-600 hover:bg-slate-50 transition shadow-sm">
                  Hôm nay
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
                       :class="ngay.getMonth() !== ngayHienTai.getMonth() ? 'bg-slate-50/50' : 'bg-white hover:bg-slate-50/70'"
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
                              :class="[
                                item.ca === 'sang' ? 'bg-emerald-500' :
                                item.ca === 'chieu' ? 'bg-orange-400' : 'bg-violet-500'
                              ]"
                              :title="`${item.nv.ten} - Ca ${item.ca}`">
                            {{ item.nv.vieTat }}
                         </div>
                         <div v-if="nhanVienCoLichTrongNgay(ngay).length > 4" class="w-6 h-6 rounded-full bg-slate-200 flex items-center justify-center text-[10px] font-bold text-slate-600 shadow-sm ring-1 ring-white">
                           +{{ nhanVienCoLichTrongNgay(ngay).length - 4 }}
                         </div>
                      </div>
                      <!-- Nút + -->
                      <div class="w-7 h-7 rounded-full flex items-center justify-center shadow-md transition hover:scale-110"
                           :class="laCaDaKhoa(ngay, 'sang') ? 'bg-slate-400' : 'bg-rose-500 hover:bg-rose-600'">
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
                        :class="calendarMode === 'ngay' && day.ngayStr !== formatISODate(ngayHienTai) ? 'opacity-20 cursor-not-allowed pointer-events-none' : 'hover:bg-slate-50'">
                      
                      <div v-if="calendarMode === 'tuan' || day.ngayStr === formatISODate(ngayHienTai)" class="flex flex-col gap-1.5 min-h-[50px]">
                        
                        <!-- Thẻ tóm tắt ca làm -->
                        <div class="rounded-[10px] p-2 border transition flex flex-col items-center justify-center shadow-sm h-full" :class="caInfo.muaNhat">
                          <div style="font-size: 13px;" class="font-bold">{{ caInfo.nhan }}</div>
                          <div style="font-size: 10px;" class="opacity-70 mt-0.5">{{ caInfo.gio }}</div>
                          <div style="font-size: 12px;" class="mt-1 font-medium text-slate-700">
                            {{ day.cas.find(c => c.id === caInfo.id)?.nhanViens.length || 0 }}/{{ MAX_NHAN_VIEN_MOI_CA }} Nhân viên
                          </div>
                          <button @click="xemChiTietCa(day, day.cas.find(c => c.id === caInfo.id))" style="font-size: 12px;" class="mt-2 px-3 py-1 rounded-full border bg-white font-medium transition hover:shadow-sm" :class="[
                              caInfo.id === 'sang' ? 'border-emerald-500 text-emerald-600 hover:bg-emerald-50' : 
                              caInfo.id === 'chieu' ? 'border-orange-400 text-orange-600 hover:bg-orange-50' : 
                              'border-violet-400 text-violet-600 hover:bg-violet-50'
                            ]">
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
                  <div v-if="nv.trangThaiChamCong && nv.trangThaiChamCong[cacNgayTrongTuan.findIndex(d => formatISODate(d) === currentChiTietCa.day.ngayStr)]" :class="['text-[10px] font-bold mt-0.5', nv.trangThaiChamCong[cacNgayTrongTuan.findIndex(d => formatISODate(d) === currentChiTietCa.day.ngayStr)] === 'check in' ? 'text-blue-500' : 'text-emerald-500']">
                    {{ nv.trangThaiChamCong[cacNgayTrongTuan.findIndex(d => formatISODate(d) === currentChiTietCa.day.ngayStr)] === 'check in' ? 'Đã check in' : 'Đã check out' }}
                  </div>
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

            <div v-for="group in xemNgayData.caGroups" :key="group.id" class="rounded-xl border overflow-hidden" :class="[
              group.id === 'sang' ? 'border-emerald-200' : group.id === 'chieu' ? 'border-orange-200' : 'border-violet-200'
            ]">
              <!-- Ca Header -->
              <div class="flex items-center gap-2 px-4 py-2.5" :class="[
                group.id === 'sang' ? 'bg-emerald-50 text-emerald-700' : group.id === 'chieu' ? 'bg-orange-50 text-orange-700' : 'bg-violet-50 text-violet-700'
              ]">
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
