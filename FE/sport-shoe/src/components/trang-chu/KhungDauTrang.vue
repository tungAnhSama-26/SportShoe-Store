<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";
import logoChinh from "../../assets/logo/delete-background-logo.png";
import { gioHangStore } from "../../stores/gio-hang";
import { layKhachId, layThongTinKhach } from "../../services/gio-hang";
import { logout } from "../../services/auth";
import { layTatCaSanPham } from "../../services/san-pham";
import { dinhDangTienViet } from "../../utils/dinhDangTien";
import { API_BASE_URL } from "../../services/api-client";
import { useAdminSession } from "../../composable/useAdminSession";
import { LayoutDashboard } from "lucide-vue-next";

const apiOrigin = API_BASE_URL.replace(/\/api\/v1\/?$/, "");

defineProps({
  thuongHieu: {
    type: String,
    required: true,
  },
});

const router = useRouter();
const dangCuon = ref(false);
const menuMo = ref(false);
const menuTaiKhoanMo = ref(false);
const daDangNhap = ref(Boolean(layKhachId()));
const thongTinKhach = ref(layThongTinKhach());

const { adminSession, avatarUrl: adminAvatarUrl } = useAdminSession();
const laAdminHayNhanVien = computed(() => !!adminSession.value.id);

function avatarTuTen(ten) {
  return "https://ui-avatars.com/api/?name=" + encodeURIComponent(ten || "KH")
    + "&background=B82220&color=ffffff&size=128";
}

// URL avatar ưu tiên admin/staff, sau đó đến khách hàng.
const finalAvatarUrl = computed(() => {
  if (laAdminHayNhanVien.value) {
    return adminAvatarUrl.value;
  }
  if (!daDangNhap.value) return null;
  const anh = (thongTinKhach.value?.hinhAnh || "").trim();
  if (anh) {
    if (/^(https?:|data:|blob:)/i.test(anh)) return anh;
    return anh.startsWith("/") ? apiOrigin + anh : apiOrigin + "/" + anh;
  }
  return avatarTuTen(thongTinKhach.value?.hoTen);
});

// Ảnh hỏng (vd đường dẫn cũ không tồn tại) -> dùng avatar chữ cái.
function loiAvatar(e) {
  const duPhong = avatarTuTen(thongTinKhach.value?.hoTen);
  if (e.target.src !== duPhong) e.target.src = duPhong;
}

// ===== Tìm kiếm sản phẩm =====
const hienTimKiem = ref(false);
const tuKhoa = ref("");
const dsSanPham = ref([]);
const dangTaiSP = ref(false);
const oTim = ref(null);

function boDauTim(s) {
  return String(s || "").normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase().trim();
}

// Gợi ý: sản phẩm có tên khớp/giống từ khóa (tối đa 6).
const ketQuaTim = computed(() => {
  const tk = boDauTim(tuKhoa.value);
  if (!tk) return [];
  return dsSanPham.value.filter((sp) => boDauTim(sp.ten).includes(tk)).slice(0, 6);
});

async function taiSanPhamNeuCan() {
  if (dsSanPham.value.length || dangTaiSP.value) return;
  dangTaiSP.value = true;
  try {
    dsSanPham.value = await layTatCaSanPham();
  } catch {
    dsSanPham.value = [];
  } finally {
    dangTaiSP.value = false;
  }
}

function moTimKiem() {
  hienTimKiem.value = !hienTimKiem.value;
  if (hienTimKiem.value) {
    taiSanPhamNeuCan();
    nextTick(() => oTim.value?.focus());
  }
}

function dongTimKiem() {
  hienTimKiem.value = false;
}

// Click 1 gợi ý -> mở chi tiết sản phẩm.
function chonSanPham(id) {
  dongTimKiem();
  tuKhoa.value = "";
  router.push(`/khachhang/san-pham/${id}`);
}

// Enter / "Xem tất cả" -> trang sản phẩm lọc theo tên.
function timKiem() {
  const tk = tuKhoa.value.trim();
  dongTimKiem();
  router.push(tk ? `/khachhang/san-pham?q=${encodeURIComponent(tk)}` : "/khachhang/san-pham");
}

function toggleTaiKhoan() {
  menuTaiKhoanMo.value = !menuTaiKhoanMo.value;
}

function dangXuat() {
  logout();
  daDangNhap.value = false;
  thongTinKhach.value = null;
  gioHangStore.datSoLuong(0);
  menuTaiKhoanMo.value = false;
  menuMo.value = false;
  document.body.style.overflow = "";
  router.replace("/login");
  setTimeout(() => window.location.reload(), 100);
}

function capNhatTrangThaiCuon() {
  dangCuon.value = window.scrollY > 12;
}

function toggleMenu() {
  menuMo.value = !menuMo.value;
  if (menuMo.value) {
    document.body.style.overflow = "hidden";
  } else {
    document.body.style.overflow = "";
  }
}

onMounted(() => {
  daDangNhap.value = Boolean(layKhachId());
  thongTinKhach.value = layThongTinKhach();
  capNhatTrangThaiCuon();
  window.addEventListener("scroll", capNhatTrangThaiCuon, { passive: true });
  gioHangStore.lamMoi();
});

onUnmounted(() => {
  window.removeEventListener("scroll", capNhatTrangThaiCuon);
  document.body.style.overflow = "";
});
</script>

<template>
  <header
    :class="[
      'fixed inset-x-0 top-0 z-50 border-b transition-all duration-300',
      dangCuon
        ? 'border-primary/20 bg-white/95 shadow-primary/10 shadow-[0_10px_30px_0_var(--tw-shadow-color)] backdrop-blur'
        : 'border-slate-200/80 bg-white/88 backdrop-blur',
    ]"
  >
    <div class="mx-auto flex max-w-7xl items-center gap-4 px-6 py-3 lg:px-10">
      <!-- Mobile Menu Button -->
      <button @click="toggleMenu" class="flex text-slate-900 transition md:hidden" aria-label="Menu">
        <svg v-if="!menuMo" class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
        <svg v-else class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>

      <!-- Logo -->
      <router-link to="/" class="flex shrink-0 items-center gap-3">
        <img :src="logoChinh" :alt="thuongHieu" class="h-10 w-auto object-contain md:h-12" />
      </router-link>

      <!-- Mobile Cart -->
      <div class="flex items-center md:hidden">
        <router-link to="/khachhang/gio-hang" class="relative text-slate-900 transition hover:text-primary" aria-label="Giỏ hàng">
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="9" cy="20" r="1" />
            <circle cx="18" cy="20" r="1" />
            <path d="M3 4h2l2.4 10.2a1 1 0 0 0 1 .8h9.7a1 1 0 0 0 1-.8L21 7H7" />
          </svg>
          <span v-if="gioHangStore.soLuong" class="absolute -right-2 -top-2 flex h-4 min-w-4 items-center justify-center rounded-full bg-primary px-1 text-[10px] font-bold text-white shadow-sm shadow-primary/30">
            {{ gioHangStore.soLuong }}
          </span>
        </router-link>
      </div>

      <!-- Desktop Nav -->
      <nav class="hidden min-w-0 flex-1 items-center justify-center gap-5 whitespace-nowrap text-[12px] font-semibold leading-[1.35] text-slate-800 lg:gap-7 md:flex">
        <router-link :to="{ path: '/' }" class="shrink-0 transition hover:text-primary">Trang chủ</router-link>
        <router-link :to="{ path: '/', hash: '#bo-suu-tap' }" class="shrink-0 transition hover:text-primary">Danh mục</router-link>
        <router-link :to="{ path: '/khachhang/san-pham' }" class="shrink-0 transition hover:text-primary">Sản phẩm</router-link>
        <router-link :to="{ path: '/', hash: '#noi-bat' }" class="shrink-0 transition hover:text-primary">Nổi bật</router-link>
        <router-link :to="{ path: '/', hash: '#gia-tri' }" class="shrink-0 transition hover:text-primary">Giới thiệu</router-link>
        <router-link :to="{ path: '/khachhang/tra-cuu-don' }" class="shrink-0 transition hover:text-primary">Theo dõi đơn hàng</router-link>
        <router-link :to="{ path: '/khachhang/danh-gia' }" class="shrink-0 transition hover:text-primary">Đánh giá</router-link>
      </nav>

      <!-- Desktop Actions -->
      <div class="ml-auto hidden shrink-0 items-center gap-4 md:flex">
        <div class="relative">
          <button @click="moTimKiem" class="inline-flex shrink-0 items-center justify-center text-slate-900 transition hover:text-primary" aria-label="Tìm kiếm">
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="7" />
              <path d="m20 20-3.5-3.5" />
            </svg>
          </button>
          <div v-if="hienTimKiem" @click="dongTimKiem" class="fixed inset-0 z-40"></div>
          <div v-if="hienTimKiem" class="absolute right-0 z-50 mt-3 w-80 rounded-2xl border border-slate-100 bg-white p-3 shadow-xl">
            <div class="flex items-center gap-2 rounded-xl border border-slate-200 px-3 focus-within:border-primary">
              <svg class="h-4 w-4 text-slate-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" /></svg>
              <input ref="oTim" v-model="tuKhoa" @keyup.enter="timKiem" type="text" placeholder="Tìm sản phẩm theo tên..." class="h-10 flex-1 bg-transparent text-sm text-slate-800 outline-none" />
            </div>
            <div v-if="tuKhoa.trim()" class="mt-2 max-h-80 overflow-y-auto">
              <p v-if="dangTaiSP" class="px-2 py-3 text-center text-sm text-slate-400">Đang tải...</p>
              <p v-else-if="!ketQuaTim.length" class="px-2 py-3 text-center text-sm text-slate-400">Không tìm thấy sản phẩm.</p>
              <template v-else>
                <button
                  v-for="sp in ketQuaTim"
                  :key="sp.id"
                  @click="chonSanPham(sp.id)"
                  class="flex w-full items-center gap-3 rounded-xl px-2 py-2 text-left transition hover:bg-slate-50"
                >
                  <img :src="sp.hinhAnh" alt="" class="h-10 w-10 shrink-0 rounded-lg object-cover bg-slate-100" />
                  <span class="min-w-0 flex-1">
                    <span class="block truncate text-sm font-medium text-slate-800">{{ sp.ten }}</span>
                    <span class="text-xs font-bold text-primary">{{ dinhDangTienViet(sp.gia) }}</span>
                  </span>
                </button>
                <button @click="timKiem" class="mt-1 block w-full rounded-xl bg-slate-50 px-2 py-2 text-center text-xs font-bold text-primary transition hover:bg-slate-100">
                  Xem tất cả kết quả &rarr;
                </button>
              </template>
            </div>
          </div>
        </div>
        <div class="relative">
          <button @click="toggleTaiKhoan" class="inline-flex shrink-0 items-center justify-center text-slate-900 transition hover:text-primary" aria-label="Tài khoản">
            <img
              v-if="finalAvatarUrl"
              :src="finalAvatarUrl"
              @error="loiAvatar"
              alt="Avatar"
              class="h-7 w-7 rounded-full object-cover ring-1 ring-slate-200"
            />
            <svg v-else class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21a8 8 0 1 0-16 0" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </button>
          <div v-if="menuTaiKhoanMo" @click="menuTaiKhoanMo = false" class="fixed inset-0 z-40"></div>
          <div v-if="menuTaiKhoanMo" class="absolute right-0 z-50 mt-3 w-max min-w-[220px] overflow-hidden rounded-2xl border border-slate-100 bg-white py-2 shadow-xl">
            <template v-if="laAdminHayNhanVien">
              <router-link :to="adminSession.vaiTro === 'Quản trị viên' ? '/admin/thong-ke' : '/admin/ban-hang'" @click="menuTaiKhoanMo = false" class="flex items-center gap-2 px-4 py-2.5 text-sm font-medium text-primary transition hover:bg-slate-50 whitespace-nowrap">
                <LayoutDashboard class="h-4 w-4 shrink-0" />
                Chuyển sang giao diện quản lý
              </router-link>
              <div class="my-1 border-t border-slate-100"></div>
            </template>
            <template v-if="daDangNhap || laAdminHayNhanVien">
              <template v-if="daDangNhap">
                <router-link to="/khachhang/profile" @click="menuTaiKhoanMo = false" class="flex items-center gap-2 px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50">
                  <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21a8 8 0 1 0-16 0" />
                    <circle cx="12" cy="7" r="4" />
                  </svg>
                  Hồ sơ của bạn
                </router-link>
                <router-link to="/khachhang/don-hang" @click="menuTaiKhoanMo = false" class="flex items-center gap-2 px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50">
                  <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><path d="M3 6h18M16 10a4 4 0 0 1-8 0"/></svg>
                  Đơn hàng của bạn
                </router-link>
              </template>
              <button @click="dangXuat" class="flex w-full items-center gap-2 border-t border-slate-50 px-4 py-2.5 text-left text-sm font-medium text-rose-500 transition hover:bg-rose-50">
                <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/></svg>
                Đăng xuất
              </button>
            </template>
            <template v-else>
              <router-link to="/login" @click="menuTaiKhoanMo = false" class="block px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50">Đăng nhập</router-link>
              <router-link to="/register" @click="menuTaiKhoanMo = false" class="block border-t border-slate-50 px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50">Đăng ký</router-link>
            </template>
          </div>
        </div>
        <router-link to="/khachhang/gio-hang" class="relative inline-flex shrink-0 items-center justify-center text-slate-900 transition hover:text-primary" aria-label="Giỏ hàng">
          <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="9" cy="20" r="1" />
            <circle cx="18" cy="20" r="1" />
            <path d="M3 4h2l2.4 10.2a1 1 0 0 0 1 .8h9.7a1 1 0 0 0 1-.8L21 7H7" />
          </svg>
          <span v-if="gioHangStore.soLuong" class="absolute -right-2 -top-2 flex h-4 min-w-4 items-center justify-center rounded-full bg-primary px-1 text-[10px] font-bold text-white shadow-sm shadow-primary/30">
            {{ gioHangStore.soLuong }}
          </span>
        </router-link>
      </div>
    </div>

    <!-- Mobile Menu -->
    <div
      v-show="menuMo"
      class="absolute left-0 top-full z-50 flex h-[calc(100vh-60px)] w-full flex-col overflow-y-auto bg-white shadow-xl md:hidden"
    >
      <nav class="flex flex-col gap-6 p-6 text-base font-semibold text-slate-800">
        <router-link :to="{ path: '/' }" @click="toggleMenu" class="transition hover:text-primary">Trang chủ</router-link>
        <router-link :to="{ path: '/', hash: '#bo-suu-tap' }" @click="toggleMenu" class="transition hover:text-primary">Danh mục</router-link>
        <router-link :to="{ path: '/khachhang/san-pham' }" @click="toggleMenu" class="transition hover:text-primary">Sản phẩm</router-link>
        <router-link :to="{ path: '/', hash: '#noi-bat' }" @click="toggleMenu" class="transition hover:text-primary">Nổi bật</router-link>
        <router-link :to="{ path: '/', hash: '#gia-tri' }" @click="toggleMenu" class="transition hover:text-primary">Giới thiệu</router-link>
        <router-link to="/khachhang/tra-cuu-don" @click="toggleMenu" class="transition hover:text-primary">Theo dõi đơn hàng</router-link>
        <router-link to="/khachhang/danh-gia" @click="toggleMenu" class="transition hover:text-primary">Đánh giá</router-link>
        <router-link to="/khachhang/gio-hang" @click="toggleMenu" class="transition hover:text-primary">Giỏ hàng</router-link>
        <router-link v-if="laAdminHayNhanVien" :to="adminSession.vaiTro === 'Quản trị viên' ? '/admin/thong-ke' : '/admin/ban-hang'" @click="toggleMenu" class="transition text-primary hover:text-primary-hover">Chuyển sang giao diện quản lý</router-link>
        <template v-if="daDangNhap">
          <router-link to="/khachhang/profile" @click="toggleMenu" class="transition hover:text-primary">Hồ sơ của bạn</router-link>
          <router-link to="/khachhang/don-hang" @click="toggleMenu" class="transition hover:text-primary">Đơn hàng của bạn</router-link>
        </template>
        <button v-if="daDangNhap || laAdminHayNhanVien" @click="dangXuat" class="text-left text-rose-500 transition hover:text-rose-600">Đăng xuất</button>
        <template v-else>
          <router-link to="/login" @click="toggleMenu" class="transition hover:text-primary">Đăng nhập</router-link>
          <router-link to="/register" @click="toggleMenu" class="transition hover:text-primary">Đăng ký</router-link>
        </template>
      </nav>
      <div class="mb-8 flex items-center gap-6 border-t border-slate-100 px-6 pt-6">
        <button class="flex items-center gap-2 text-slate-900 transition hover:text-primary" aria-label="Tìm kiếm">
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="7" />
            <path d="m20 20-3.5-3.5" />
          </svg>
          <span class="text-sm font-semibold">Tìm kiếm</span>
        </button>
        <button class="flex items-center gap-2 text-slate-900 transition hover:text-primary" aria-label="Tài khoản">
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21a8 8 0 1 0-16 0" />
            <circle cx="12" cy="7" r="4" />
          </svg>
          <span class="text-sm font-semibold">Tài khoản</span>
        </button>
      </div>
    </div>
  </header>
</template>
