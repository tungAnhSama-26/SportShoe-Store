<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, Banknote, CircleX, History, Package, Plus, Search, ShoppingCart, Star, Trash2, Truck } from "lucide-vue-next";
import { capNhatSanPhamHoaDon, capNhatTrangThaiHoaDon, layChiTietHoaDon } from "../../../services/hoa-don";
import { timSanPhamTaiQuay, type SanPhamTaiQuay } from "../../../services/ban-hang-tai-quay";

const route = useRoute();
const router = useRouter();
const hoaDon = ref(null);
const dangTai = ref(false);
const loiTrang = ref("");
const dangCapNhat = ref(false);
const thongBao = ref("");

// Modals state
const hienModalXacNhan = ref(false);
const hienModalLichSu = ref(false);
const hienModalSanPham = ref(false);

const trangThaiMoiXacNhan = ref(null);
const ghiChuXacNhan = ref("");

// Product update state
const tuKhoaSanPham = ref("");
const ketQuaTimKiem = ref<SanPhamTaiQuay[]>([]);
const dangTimKiem = ref(false);
const danhSachSanPhamUpdate = ref<Array<{ chiTietId: number; tenSanPham: string; soLuong: number; giaBan: number; maBienThe: string }>>([]);

function dinhDangTien(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND", maximumFractionDigits: 0 }).format(value || 0);
}

function dinhDangNgay(ngay: string) {
  return new Intl.DateTimeFormat("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" }).format(new Date(ngay));
}

function dinhDangGio(ngay: string) {
  return new Intl.DateTimeFormat("vi-VN", { hour: "2-digit", minute: "2-digit", second: "2-digit" }).format(new Date(ngay));
}


const cacBuoc = [
  { id: 1, key: "Chờ xác nhận", ten: "Chờ Xác Nhận", icon: ShoppingCart },
  { id: 2, key: "Đã xác nhận", ten: "Đã Xác Nhận Thông Tin\nThanh Toán", icon: Banknote },
  { id: 3, key: "Chờ vận chuyển", ten: "Chờ Lấy Vận Chuyển", icon: Package },
  { id: 4, key: "Vận chuyển", ten: "Vận Chuyển", icon: Truck },
  { id: 5, key: "Đã hoàn thành", ten: "Đã Hoàn Thành", icon: Star },
];

const buocHienTai = computed(() => {
  if (!hoaDon.value) return 0;
  return { "Chờ xác nhận": 1, "Đã xác nhận": 2, "Chờ vận chuyển": 3, "Vận chuyển": 4, "Đã hoàn thành": 5, Hủy: 0 }[hoaDon.value.trangThai];
});

const tongTienHang = computed(() => hoaDon.value?.sanPham.reduce((tong, sp) => tong + sp.thanhTien, 0) ?? 0);
const tongKhachCanTra = computed(() => (hoaDon.value ? tongTienHang.value + hoaDon.value.phiVanChuyen - hoaDon.value.giamGia : 0));

async function taiChiTiet() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    hoaDon.value = await layChiTietHoaDon(Number(route.params.id));
  } catch (error) {
    loiTrang.value = error instanceof Error ? error.message : "Không thể tải chi tiết hóa đơn";
  } finally {
    dangTai.value = false;
  }
}

function openModalXacNhan(trangThai) {
  trangThaiMoiXacNhan.value = trangThai;
  ghiChuXacNhan.value = "";
  hienModalXacNhan.value = true;
}

async function handleXacNhanTrangThai() {
  if (!hoaDon.value || !trangThaiMoiXacNhan.value || dangCapNhat.value) return;

  dangCapNhat.value = true;
  try {
    const payload = {
      trangThai: trangThaiMoiXacNhan.value,
      ghiChu: ghiChuXacNhan.value,
    };
    hoaDon.value = await capNhatTrangThaiHoaDon(hoaDon.value.id, payload);
    thongBao.value = `Cập nhật sang trạng thái ${trangThaiMoiXacNhan.value} thành công.`;
    hienModalXacNhan.value = false;
  } catch (error) {
    alert(error instanceof Error ? error.message : "Lỗi cập nhật trạng thái");
  } finally {
    dangCapNhat.value = false;
  }
}

// Product Update Logic
watch(hienModalSanPham, (val) => {
  if (val && hoaDon.value) {
    danhSachSanPhamUpdate.value = (hoaDon.value.sanPham || []).map(sp => ({
      chiTietId: sp.id, // Ensure this matches back-end expected chiTietId
      tenSanPham: sp.tenSanPham,
      soLuong: sp.soLuong,
      giaBan: sp.donGia,
      maBienThe: sp.phanLoai || ''
    }));
  }
});

async function timKiemSanPham() {
  if (!tuKhoaSanPham.value.trim()) {
    ketQuaTimKiem.value = [];
    return;
  }
  dangTimKiem.value = true;
  try {
    ketQuaTimKiem.value = await timSanPhamTaiQuay(tuKhoaSanPham.value);
  } finally {
    dangTimKiem.value = false;
  }
}

function themSanPham(sp: SanPhamTaiQuay) {
  const existing = danhSachSanPhamUpdate.value.find(i => i.chiTietId === sp.chiTietId);
  if (existing) {
    existing.soLuong++;
  } else {
    danhSachSanPhamUpdate.value.push({
      chiTietId: sp.chiTietId,
      tenSanPham: sp.tenSanPham,
      soLuong: 1,
      giaBan: sp.giaBan,
      maBienThe: `${sp.mauSac} - ${sp.kichCo}`
    });
  }
}

function removeSanPham(id: number) {
  danhSachSanPhamUpdate.value = danhSachSanPhamUpdate.value.filter(i => i.chiTietId !== id);
}

async function handleSaveSanPham() {
  if (!hoaDon.value || dangCapNhat.value) return;
  dangCapNhat.value = true;
  try {
    const payload = {
      items: danhSachSanPhamUpdate.value.map(i => ({ chiTietId: i.chiTietId, soLuong: i.soLuong }))
    };
    hoaDon.value = await capNhatSanPhamHoaDon(hoaDon.value.id, payload);
    thongBao.value = "Cập nhật sản phẩm hóa đơn thành công.";
    hienModalSanPham.value = false;
  } catch (error) {
    alert(error instanceof Error ? error.message : "Lỗi cập nhật sản phẩm");
  } finally {
    dangCapNhat.value = false;
  }
}

onMounted(taiChiTiet);
</script>

<template>
  <div class="space-y-6 pb-20">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <button type="button" @click="router.push({ name: 'admin-hoa-don' })" class="mb-3 inline-flex items-center gap-2 rounded-xl bg-white px-3 py-1.5 text-xs font-medium text-slate-500 shadow-sm ring-1 ring-slate-200 transition hover:text-rose-500">
          <ArrowLeft class="h-3.5 w-3.5" />
          Quay lại
        </button>
        <h1 class="text-2xl text-slate-800">Chi tiết hóa đơn</h1>
      </div>
    </div>

    <div v-if="dangTai" class="rounded-[24px] border border-slate-200 bg-white p-10 text-center text-sm text-slate-400 shadow-sm">Đang tải chi tiết hóa đơn...</div>
    <div v-else-if="loiTrang || !hoaDon" class="rounded-[24px] border border-slate-200 bg-white p-10 text-center shadow-sm">
      <h2 class="text-2xl font-bold text-slate-800">Không tìm thấy hóa đơn</h2>
      <p class="mt-3 text-sm text-slate-400">{{ loiTrang || "Hóa đơn không tồn tại." }}</p>
    </div>

    <template v-else>
      <!-- Stepper and Actions -->
      <section class="rounded-[32px] border border-slate-200 bg-white p-8 shadow-sm">
        <h2 class="mb-10 text-sm font-bold text-slate-800">Trạng Thái Đơn Hàng</h2>
        <div class="relative flex items-start justify-between px-8">
          <!-- Thanh xám nền -->
          <div class="absolute left-[5rem] right-[5rem] top-10 z-0 h-[6px] bg-[#a6a6a6] rounded-full"></div>
          <!-- Thanh xanh lá tiến trình -->
          <div class="absolute left-[5rem] top-10 z-10 h-[6px] bg-[#16a34a] rounded-full transition-all duration-500" :style="{ width: `calc((100% - 10rem) * ${(buocHienTai - 1) / 4})` }"></div>
          
          <div v-for="buoc in cacBuoc" :key="buoc.id" class="flex flex-col items-center gap-4 w-40 z-20">
            <div class="relative flex h-20 w-20 items-center justify-center rounded-full border-[6px] bg-white transition-all duration-500" :class="buoc.id <= buocHienTai ? 'border-[#16a34a] text-[#16a34a] scale-110' : 'border-[#a6a6a6] text-[#636363]'">
              <component :is="buoc.icon" class="h-8 w-8 stroke-[2.5]" />
            </div>
            <div class="text-center h-10">
              <p class="text-xs font-semibold whitespace-pre-line leading-tight transition-colors" :class="buoc.id <= buocHienTai ? 'text-slate-800' : 'text-slate-500'">{{ buoc.ten }}</p>
            </div>
          </div>
        </div>

        <!-- Action Buttons and Notification -->
        <div class="mt-12 flex items-center justify-between">
          <div class="flex items-center gap-4">
            <button v-if="buocHienTai < 5 && hoaDon.trangThai !== 'Hủy' && hoaDon.trangThai !== 'Đã hoàn thành'" @click="openModalXacNhan(cacBuoc[buocHienTai].key as any)" class="rounded-full bg-[#FE3B4B] px-6 py-2.5 text-sm font-bold text-white shadow-md transition hover:bg-rose-600">
              Chuyển sang {{ cacBuoc[buocHienTai].ten.toLowerCase() }}
            </button>
            <button v-if="buocHienTai === 3" @click="openModalXacNhan('Hủy')" class="rounded-full bg-[#909090] px-6 py-2.5 text-sm font-bold text-white shadow-md transition hover:bg-slate-500">
              Hủy đơn và hoàn tiền
            </button>
            <div v-if="thongBao" class="rounded-full bg-emerald-50 px-6 py-2.5 text-xs font-bold text-emerald-600 border border-emerald-100">{{ thongBao }}</div>
            <div v-if="hoaDon.trangThai === 'Hủy'" class="flex items-center gap-3 rounded-full bg-rose-50 px-6 py-2.5 text-sm font-bold text-rose-600 border border-rose-100">
              <CircleX class="h-5 w-5" />
              Hóa đơn này đã bị hủy.
            </div>
          </div>
          <button @click="hienModalLichSu = true" class="inline-flex items-center gap-2 rounded-full bg-[#FE3B4B] px-6 py-2.5 text-sm font-bold text-white shadow-md transition hover:bg-rose-600 active:scale-95">
            <History class="h-4 w-4" />
            LỊCH SỬ HÓA ĐƠN
          </button>
        </div>
      </section>

      <section class="space-y-6 mt-6">
        <!-- Tracking Table -->
        <div class="rounded-xl border border-slate-200 bg-[#fbfbfb] p-6 shadow-sm overflow-hidden">
          <h2 class="mb-5 text-lg font-bold text-slate-800">Lịch sử thanh toán</h2>
          <div class="overflow-x-auto rounded-lg border border-slate-200">
            <table class="w-full text-sm text-left bg-white">
              <thead class="bg-slate-200/50">
                <tr class="border-b border-slate-200 text-xs font-bold text-slate-800">
                  <th class="py-3 px-4">STT</th>
                  <th class="py-3 px-4">Mã giao dịch</th>
                  <th class="py-3 px-4">Loại giao dịch</th>
                  <th class="py-3 px-4">Phương thức thanh toán</th>
                  <th class="py-3 px-4">Trạng thái thanh toán</th>
                  <th class="py-3 px-4">Thời gian</th>
                  <th class="py-3 px-4">Tổng tiền</th>
                  <th class="py-3 px-4">Ghi chú</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="(item, index) in hoaDon.lichSuThanhToan" :key="item.id" class="group">
                  <td class="py-3 px-4 font-bold text-slate-600">{{ index + 1 }}</td>
                  <td class="py-3 px-4 font-bold text-slate-800">{{ item.maGiaoDich || '-' }}</td>
                  <td class="py-3 px-4 text-slate-600">{{ item.loaiGiaoDich }}</td>
                  <td class="py-3 px-4 text-slate-600">{{ item.phuongThucThanhToan }}</td>
                  <td class="py-3 px-4 font-bold text-emerald-600">{{ item.trangThaiThanhToan }}</td>
                  <td class="py-3 px-4">
                    <div class="text-slate-800 text-xs font-bold">{{ dinhDangGio(item.thoiGian) }}</div>
                    <div class="text-slate-500 text-xs">{{ dinhDangNgay(item.thoiGian) }}</div>
                  </td>
                  <td class="py-3 px-4 font-bold text-rose-500">{{ dinhDangTien(item.tongTien) }}</td>
                  <td class="py-3 px-4 text-slate-600">{{ item.ghiChu || '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Sidebar Info (Thông tin đơn hàng) -->
        <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="mb-5 text-lg text-slate-800">Thông tin đơn hàng</h2>
          <div class="space-y-2 text-sm">
            <div class="flex">
              <span class="w-32 font-semibold text-slate-800">Mã đơn hàng:</span>
              <span class="text-slate-600 w-64">{{ hoaDon.maHoaDon }}</span>
              <span class="w-40 font-semibold text-slate-800">Nhân viên:</span>
              <span class="font-medium text-sky-600">{{ hoaDon.tenNhanVien || "Chưa gán" }}</span>
            </div>
            <div class="flex items-center">
              <span class="w-32 font-semibold text-slate-800">Trạng thái <span class="text-slate-400">:</span></span>
              <span class="w-64 font-medium text-rose-500">{{ hoaDon.trangThai }}</span>
              <span class="w-40 font-semibold text-slate-800">Khách hàng:</span>
              <span class="font-medium text-sky-600">{{ hoaDon.tenKhachHang }}</span>
            </div>
            <div class="flex items-center">
              <span class="w-32 font-semibold text-slate-800">Loại <span class="text-slate-400">:</span></span>
              <span class="w-64 font-medium text-emerald-500">{{ hoaDon.loaiDon }}</span>
              <span class="w-40 font-semibold text-slate-800">Số điện thoại:</span>
              <span class="text-sky-600">{{ hoaDon.soDienThoai }}</span>
            </div>
            <div class="flex mt-1">
              <span class="w-32 font-semibold text-slate-800">Email</span>
              <span class="ml-2 font-medium text-slate-800">{{ hoaDon.email }}</span>
            </div>
            <div class="flex mt-1">
              <span class="w-32 font-semibold text-slate-800">Địa chỉ:</span>
              <span class="ml-2 w-full max-w-2xl font-medium text-slate-800">{{ hoaDon.diaChi }}</span>
            </div>
            <div class="flex mt-1">
              <span class="w-32 font-semibold text-slate-800">Ghi chú</span>
              <span class="text-slate-600 ml-2">{{ hoaDon.ghiChu || '' }}</span>
            </div>
          </div>
        </div>

        <!-- Product Table and Totals -->
        <div class="rounded-xl border border-slate-200 bg-[#fbfbfb] p-6 shadow-sm overflow-hidden flex flex-col">
          <h2 class="mb-5 text-lg font-bold text-slate-800">Thông tin sản phẩm đã mua</h2>
          
          <div class="overflow-x-auto rounded-lg border border-slate-200 border-2 border-[#1E88E5]">
            <table class="w-full text-sm text-left bg-white">
              <thead class="bg-slate-200/50">
                <tr class="border-b border-slate-200 text-xs font-bold text-slate-800">
                  <th class="py-3 px-4 border-r border-slate-200">STT</th>
                  <th class="py-3 px-4 border-r border-slate-200">Ảnh sản phẩm</th>
                  <th class="py-3 px-4 border-r border-slate-200">Sản phẩm</th>
                  <th class="py-3 px-4 border-r border-slate-200">Màu sắc</th>
                  <th class="py-3 px-4 border-r border-slate-200 text-center">Số lượng</th>
                  <th class="py-3 px-4 border-r border-slate-200">Thời gian</th>
                  <th class="py-3 px-4 border-r border-slate-200 text-center">Đơn giá</th>
                  <th class="py-3 px-4 text-center">Tổng tiền</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="(item, index) in hoaDon.sanPham" :key="item.id" class="group">
                  <td class="py-4 px-4 font-bold text-slate-600 border-r border-slate-100 text-center">{{ index + 1 }}</td>
                  <td class="py-4 px-4 border-r border-slate-100">
                    <img :src="item.hinhAnh || 'https://via.placeholder.com/64x64?text=Shoe'" class="h-14 w-14 object-cover" />
                  </td>
                  <td class="py-4 px-4 border-r border-slate-100 text-slate-800 font-bold capitalize">
                    {{ item.tenSanPham }}
                  </td>
                  <td class="py-4 px-4 border-r border-slate-100 text-slate-600">
                    {{ item.mauSac }}
                  </td>
                  <td class="py-4 px-4 border-r border-slate-100 text-slate-800 font-bold text-center">
                    {{ item.soLuong }}
                  </td>
                  <td class="py-4 px-4 border-r border-slate-100">
                    <div class="text-slate-800 text-xs font-bold">{{ dinhDangGio(hoaDon.ngayTao) }}</div>
                    <div class="text-slate-500 text-xs">{{ dinhDangNgay(hoaDon.ngayTao) }}</div>
                  </td>
                  <td class="py-4 px-4 border-r border-slate-100 font-bold text-rose-500 text-center">{{ dinhDangTien(item.donGia) }}</td>
                  <td class="py-4 px-4 text-center font-bold text-rose-500">{{ dinhDangTien(item.thanhTien) }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="self-end mt-10 w-full max-w-lg pr-4">
            <div class="space-y-4 text-sm font-bold">
              <div class="flex items-center">
                <span class="text-slate-800 w-64">Tổng tiền hàng :</span>
                <span class="text-rose-500 flex-1 text-right">{{ dinhDangTien(tongTienHang) }}</span>
              </div>
              <div class="flex items-center">
                <span class="text-slate-800 w-64">Phí vận chuyển :</span>
                <span class="text-rose-500 flex-1 text-right">{{ dinhDangTien(hoaDon.phiVanChuyen) }}</span>
              </div>
              <div class="flex items-center">
                <span class="text-slate-800 w-64">Voucher áp dụng :</span>
                <span class="text-emerald-500 flex-1 text-right">{{ hoaDon.voucher !== 'Không áp dụng' ? hoaDon.voucher : '' }}</span>
              </div>
              <div class="flex items-center">
                <span class="text-slate-800 w-64">Số tiền được giảm :</span>
                <span class="text-rose-500 flex-1 text-right">{{ dinhDangTien(hoaDon.giamGia) }}</span>
              </div>
              <div class="flex items-center">
                <span class="text-slate-800 w-64">Tổng tiền khách hàng cần thanh toán :</span>
                <span class="text-emerald-600 flex-1 text-right">{{ dinhDangTien(tongKhachCanTra) }}</span>
              </div>
              <div class="flex items-center">
                <span class="text-slate-800 w-64">Tiền khách đã thanh toán:</span>
                <span class="text-emerald-600 flex-1 text-right">{{ dinhDangTien(tongKhachCanTra) }}</span>
              </div>
            </div>
            <div class="mt-8 flex justify-end">
              <button v-if="hoaDon.trangThai === 'Chờ xác nhận'" @click="hienModalSanPham = true" class="rounded-lg bg-[#FE3B4B] px-6 py-2.5 text-sm font-bold text-white shadow-md hover:bg-rose-600 transition">
                Cập nhật hóa đơn
              </button>
            </div>
          </div>
        </div>
      </section>
    </template>

    <!-- Modals -->
    <!-- Status Confirmation Modal -->
    <div v-if="hienModalXacNhan" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-md overflow-hidden rounded-[32px] bg-white shadow-2xl animate-in zoom-in-95 duration-200">
        <div class="p-8">
          <h3 class="text-xl font-bold text-slate-800">Xác nhận thông tin thanh toán</h3>
          <p class="mt-2 text-sm text-slate-400">Cập nhật đơn hàng sang trạng thái <span class="font-bold text-slate-800">{{ trangThaiMoiXacNhan }}</span></p>
          <div class="mt-6 text-sm">
            <label class="mb-2 block text-xs font-bold uppercase tracking-widest text-slate-400">Ghi chú xác nhận</label>
            <textarea v-model="ghiChuXacNhan" rows="4" placeholder="Nhập ghi chú cho bước này..." class="w-full rounded-2xl border border-slate-200 bg-slate-50 p-4 outline-none focus:border-rose-500 focus:bg-white transition-all"></textarea>
          </div>
          <div class="mt-8 flex gap-3">
            <button @click="hienModalXacNhan = false" class="flex-1 rounded-2xl bg-slate-100 py-3.5 text-sm font-bold text-slate-500 hover:bg-slate-200 transition">Đóng</button>
            <button @click="handleXacNhanTrangThai" :disabled="dangCapNhat" class="flex-1 rounded-2xl bg-rose-500 py-3.5 text-sm font-bold text-white shadow-lg shadow-rose-100 hover:bg-rose-600 transition disabled:opacity-50">{{ dangCapNhat ? 'Đang lưu...' : 'Xác nhận' }}</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Details History Modal -->
    <div v-if="hienModalLichSu" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-2xl overflow-hidden rounded-[40px] bg-white shadow-2xl animate-in slide-in-from-bottom-5 duration-300">
        <div class="border-b border-slate-100 p-8 flex items-center justify-between bg-slate-50/50">
          <div>
            <h3 class="text-xl font-black text-slate-800 uppercase tracking-tight">Lịch sử hóa đơn</h3>
            <p class="text-xs font-bold text-slate-400 mt-1 uppercase tracking-widest">Chi tiết các bước xác nhận</p>
          </div>
          <button @click="hienModalLichSu = false" class="rounded-full bg-white p-2 text-slate-400 shadow-sm ring-1 ring-slate-200 hover:text-rose-500"><CircleX class="h-6 w-6" /></button>
        </div>
        <div class="max-h-[60vh] overflow-y-auto p-8">
          <div class="space-y-8 relative">
            <div class="absolute left-6 top-3 bottom-3 w-0.5 bg-slate-100"></div>
            <div v-for="log in hoaDon?.lichSuHoaDon" :key="log.id" class="relative pl-14 group">
              <div class="absolute left-4 top-1 z-10 h-4 w-4 rounded-full border-4 border-white bg-rose-500 shadow-[0_0_0_4px_rgba(244,63,94,0.1)]"></div>
              <div class="flex items-start justify-between">
                <div>
                  <p class="text-sm font-black text-slate-800 uppercase">{{ log.trangThai }}</p>
                  <p class="mt-0.5 text-[10px] font-bold text-slate-400">{{ dinhDangNgay(log.ngayTao) }}</p>
                </div>
                <div class="text-right">
                  <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Nhân viên xác nhận</p>
                  <p class="text-xs font-black text-sky-600 uppercase mt-0.5">{{ log.tenNhanVien }}</p>
                </div>
              </div>
              <div v-if="log.ghiChu" class="mt-3 rounded-2xl bg-slate-50 p-4 text-xs font-bold leading-relaxed text-slate-500 border border-slate-100 group-hover:border-slate-200 transition-colors">{{ log.ghiChu }}</div>
            </div>
            <div v-if="!hoaDon?.lichSuHoaDon?.length" class="text-center py-10 text-slate-400 text-sm font-bold">Chưa có lịch sử xác nhận.</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Product Update Modal -->
    <div v-if="hienModalSanPham" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-4xl overflow-hidden rounded-[40px] bg-white shadow-2xl animate-in zoom-in-95 duration-200">
        <div class="grid lg:grid-cols-2">
          <!-- Search Product -->
          <div class="border-r border-slate-100 p-8">
            <h3 class="text-xl font-black text-slate-800 uppercase tracking-tight">Tìm sản phẩm</h3>
            <div class="relative mt-6">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input v-model="tuKhoaSanPham" @input="timKiemSanPham" type="text" placeholder="Tên sản phẩm, SKU..." class="h-12 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none focus:border-rose-500 focus:bg-white transition-all shadow-inner" />
            </div>
            <div class="mt-6 max-h-[400px] space-y-3 overflow-y-auto pr-2 custom-scrollbar">
              <div v-if="dangTimKiem" class="py-10 text-center text-xs font-bold text-slate-400 uppercase tracking-widest">Đang tìm kiếm...</div>
              <div v-else-if="ketQuaTimKiem.length === 0" class="py-10 text-center text-xs font-bold text-slate-400 uppercase tracking-widest">Nhập từ khóa để tìm sản phẩm</div>
              <div v-for="sp in ketQuaTimKiem" :key="sp.chiTietId" @click="themSanPham(sp)" class="cursor-pointer rounded-2xl border border-slate-100 p-4 transition hover:border-rose-300 hover:bg-rose-50/30 group">
                <div class="flex items-center gap-4">
                  <div class="flex-1">
                    <p class="text-sm font-black text-slate-800 line-clamp-1 capitalize group-hover:text-rose-600 transition-colors">{{ sp.tenSanPham }}</p>
                    <div class="mt-1 flex gap-2 text-[10px] font-bold uppercase text-slate-400">
                      <span>{{ sp.mauSac }}</span>
                      <span>{{ sp.kichCo }}</span>
                      <span class="text-sky-500">Tồn: {{ sp.soLuongTon }}</span>
                    </div>
                  </div>
                  <div class="text-right">
                    <p class="text-sm font-black text-rose-500">{{ dinhDangTien(sp.giaBan) }}</p>
                    <p class="text-[9px] font-bold text-slate-400 uppercase">Chọn</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Current Items Selection -->
          <div class="flex flex-col bg-slate-50/50 p-8">
            <h3 class="text-xl font-black text-slate-800 uppercase tracking-tight">Giỏ hàng cập nhật</h3>
            <div class="mt-6 flex-1 space-y-4 overflow-y-auto pr-2 custom-scrollbar max-h-[400px]">
              <div v-for="item in danhSachSanPhamUpdate" :key="item.chiTietId" class="flex items-center gap-4 rounded-2xl bg-white p-4 shadow-sm group">
                <div class="flex-1">
                  <p class="text-sm font-bold text-slate-800 line-clamp-1 capitalize">{{ item.tenSanPham }}</p>
                  <p class="text-[10px] text-slate-400 font-bold uppercase">{{ item.maBienThe }}</p>
                </div>
                <div class="flex items-center gap-3">
                  <input v-model.number="item.soLuong" type="number" min="1" class="h-9 w-16 rounded-xl border border-slate-200 bg-slate-50 text-center text-sm font-bold outline-none focus:border-rose-500" />
                  <button @click="removeSanPham(item.chiTietId)" class="rounded-xl bg-rose-100 p-2 text-rose-500 transition hover:bg-rose-200"><Trash2 class="h-4 w-4" /></button>
                </div>
              </div>
              <div v-if="danhSachSanPhamUpdate.length === 0" class="py-20 text-center text-xs font-bold text-slate-400 uppercase tracking-widest">Chưa có sản phẩm nào</div>
            </div>
            <div class="mt-8 border-t border-slate-200 pt-6">
              <div class="flex items-center justify-between font-black text-slate-800 uppercase tracking-tight">
                <span>Tổng tiền hàng</span>
                <span class="text-rose-500">{{ dinhDangTien(danhSachSanPhamUpdate.reduce((t, i) => t + i.giaBan * i.soLuong, 0)) }}</span>
              </div>
              <div class="mt-6 flex gap-3">
                <button @click="hienModalSanPham = false" class="flex-1 rounded-2xl bg-white py-3.5 text-sm font-bold text-slate-500 shadow-sm ring-1 ring-slate-200 hover:bg-slate-50 transition">Đóng</button>
                <button @click="handleSaveSanPham" :disabled="dangCapNhat" class="flex-1 rounded-2xl bg-slate-800 py-3.5 text-sm font-bold text-white shadow-xl hover:bg-slate-900 transition disabled:opacity-50">Lưu thay đổi</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #cbd5e1; }
</style>

<style scoped>
/* Smooth transitions */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
