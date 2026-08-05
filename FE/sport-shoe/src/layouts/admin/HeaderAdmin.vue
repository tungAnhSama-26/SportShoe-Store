<script setup>
import { computed, ref, onMounted } from "vue";
import { ChevronDown, Home, LogOut, Menu, Moon, Sun, UserCog, UserRound, ArrowRightLeft, Bell, BellOff, Gift, AlertTriangle, Star, MessageSquare, Calendar, XCircle, RefreshCw, X, Settings2 } from "lucide-vue-next";
import { useRoute, useRouter } from "vue-router";
import { toggleSidebar } from "../../composable/useSidebar";
import { useDarkMode } from "../../composable/useDarkMode";
import { useAdminSession } from "../../composable/useAdminSession";
import { isAdminRole, logoutAdmin } from "../../services/auth";

import { useGiaoCa } from "../../composable/useGiaoCa";
import { useRealtime } from "../../composables/useRealtime";
import { layDanhSachThongBao, demThongBaoChuaDoc, docThongBao, docTatCaThongBao, xoaThongBao } from "../../services/thong-bao";

const route = useRoute();
const router = useRouter();
const { isDark, toggleDark } = useDarkMode();
const { adminSession, avatarUrl } = useAdminSession();
const FALLBACK_ADMIN_NAME = "Trần Vũ Tùng Anh";
const hienMenuTaiKhoan = ref(false);

const { activeShift, loadActiveShift } = useGiaoCa();

// States for Notification System
const dropdownThongBaoMo = ref(false);
const dsThongBao = ref([]);
const chuaDocCount = ref(0);
const loadingThongBao = ref(false);

const { subscribeTopic } = useRealtime();

async function taiThongBao() {
  loadingThongBao.value = true;
  try {
    const [count, pageData] = await Promise.all([
      demThongBaoChuaDoc(),
      layDanhSachThongBao(0, 15)
    ]);
    chuaDocCount.value = typeof count === 'number' ? count : 0;
    dsThongBao.value = pageData?.content || [];
  } catch (error) {
    console.error("Lỗi khi tải thông báo:", error);
  } finally {
    loadingThongBao.value = false;
  }
}

function toggleDropdownThongBao() {
  dropdownThongBaoMo.value = !dropdownThongBaoMo.value;
  if (dropdownThongBaoMo.value) {
    taiThongBao();
  }
}

async function markAllAsRead() {
  try {
    await docTatCaThongBao();
    dsThongBao.value.forEach(tb => tb.daDoc = true);
    chuaDocCount.value = 0;
  } catch (error) {
    console.error("Lỗi khi đánh dấu đọc tất cả:", error);
  }
}

async function handleNotificationClick(tb) {
  try {
    if (!tb.daDoc) {
      const res = await docThongBao(tb.id);
      if (res) {
        tb.daDoc = true;
        if (chuaDocCount.value > 0) chuaDocCount.value--;
      }
    }
  } catch (error) {
    console.error("Lỗi khi đọc thông báo:", error);
  }
  dropdownThongBaoMo.value = false;
  if (tb.link) {
    router.push(tb.link);
  }
}

async function deleteNotification(id, event) {
  if (event) {
    event.stopPropagation();
    event.preventDefault();
  }
  try {
    const item = dsThongBao.value.find(tb => tb.id === id);
    const wasUnread = item && !item.daDoc;

    await xoaThongBao(id);
    dsThongBao.value = dsThongBao.value.filter(tb => tb.id !== id);

    if (wasUnread && chuaDocCount.value > 0) {
      chuaDocCount.value--;
    }
  } catch (error) {
    console.error("Lỗi khi xóa thông báo:", error);
  }
}

function getIconComponent(loai) {
  switch (loai) {
    case "ORDER": return Gift;
    case "REVIEW": return Star;
    case "CHAT": return MessageSquare;
    case "STOCK": return AlertTriangle;
    case "SHIFT": return Calendar;
    case "REFUND": return RefreshCw;
    case "CANCEL": return XCircle;
    default: return Bell;
  }
}

function getIconBgClass(loai) {
  switch (loai) {
    case "ORDER": return "bg-emerald-50 dark:bg-emerald-500/10";
    case "REVIEW": return "bg-amber-50 dark:bg-amber-500/10";
    case "CHAT": return "bg-blue-50 dark:bg-blue-500/10";
    case "STOCK": return "bg-rose-50 dark:bg-rose-500/10";
    case "SHIFT": return "bg-indigo-50 dark:bg-indigo-500/10";
    case "REFUND":
    case "CANCEL": return "bg-rose-50 dark:bg-rose-500/10";
    default: return "bg-slate-50 dark:bg-slate-700/50";
  }
}

function getIconColorClass(loai) {
  switch (loai) {
    case "ORDER": return "text-emerald-600 dark:text-emerald-400";
    case "REVIEW": return "text-amber-600 dark:text-amber-400";
    case "CHAT": return "text-blue-600 dark:text-blue-400";
    case "STOCK": return "text-rose-600 dark:text-rose-400";
    case "SHIFT": return "text-indigo-600 dark:text-indigo-400";
    case "REFUND":
    case "CANCEL": return "text-rose-600 dark:text-rose-400";
    default: return "text-slate-500 dark:text-slate-400";
  }
}

function formatTimeAgo(dateStr) {
  if (!dateStr) return "";
  const now = new Date();
  const date = new Date(dateStr);
  const diffMs = now - date;
  const diffSec = Math.floor(diffMs / 1000);
  if (diffSec < 60) return "Vừa xong";
  const diffMin = Math.floor(diffSec / 60);
  if (diffMin < 60) return `${diffMin} phút trước`;
  const diffHr = Math.floor(diffMin / 60);
  if (diffHr < 24) return `${diffHr} giờ trước`;
  const diffDays = Math.floor(diffHr / 24);
  return `${diffDays} ngày trước`;
}

function playNotificationSound() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.frequency.setValueAtTime(880, ctx.currentTime);
    gain.gain.setValueAtTime(0.05, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.15);
    osc.start(ctx.currentTime);
    osc.stop(ctx.currentTime + 0.15);
  } catch (e) {
    console.error("Lỗi phát âm thanh thông báo", e);
  }
}

onMounted(() => {
  loadActiveShift();
  taiThongBao();

  // Subscribe to real-time notification topic
  subscribeTopic("/topic/admin/notifications", (msg) => {
    if (msg.type === "NEW_NOTIFICATION") {
      const tb = msg.payload;
      const laAdmin = adminSession.value.vaiTro === "Quản trị viên" || adminSession.value.vaiTro === "Admin";
      if (!laAdmin && tb.loai !== "ORDER" && tb.loai !== "REFUND" && tb.loai !== "CANCEL") {
        return; // Bỏ qua thông báo không phải hóa đơn đối với nhân viên
      }
      dsThongBao.value.unshift(tb);
      if (dsThongBao.value.length > 20) {
        dsThongBao.value.pop();
      }
      chuaDocCount.value++;
      playNotificationSound();
    } else if (msg.type === "ALL_READ") {
      dsThongBao.value.forEach(tb => tb.daDoc = true);
      chuaDocCount.value = 0;
    }
  });
});

const pageTitle = computed(() => {
  const titles = {
    'admin-thong-ke': 'Thống kê',
    'admin-phieu-giam-gia': 'Phiếu giảm giá',
    'admin-phieu-giam-gia-khach-hang': 'Phiếu giảm giá khách hàng',
    'admin-phieu-giam-gia-them': 'Thêm phiếu giảm giá',
    'admin-phieu-giam-gia-chi-tiet': 'Chi tiết phiếu giảm giá',
    'admin-phieu-giam-gia-khach-hang-them': 'Thêm phiếu giảm giá khách hàng',
    'admin-phieu-giam-gia-khach-hang-chi-tiet': 'Chi tiết phiếu giảm giá khách hàng',
    'admin-dot-giam-gia': 'Đợt giảm giá',
    'admin-dot-giam-gia-them': 'Thêm đợt giảm giá',
    'admin-dot-giam-gia-chi-tiet': 'Chi tiết đợt giảm giá',
    'admin-hoa-don': 'Hóa đơn',
    'admin-hoa-don-chi-tiet': 'Chi tiết đơn hàng',
    'admin-ban-hang': 'Bán hàng tại quầy',
    'admin-san-pham': 'Sản phẩm',
    'admin-san-pham-them': 'Thêm sản phẩm',
    'admin-chi-tiet-san-pham': 'Chi tiết sản phẩm',
    'admin-chi-tiet-san-pham-new': 'Chi tiết sản phẩm',
    'admin-bien-the-san-pham': 'Biến thể sản phẩm',
    'admin-bien-the-san-pham-them': 'Thêm biến thể sản phẩm',
    'admin-loai-giay': 'Loại giày',
    'admin-co-giay': 'Cổ giày',
    'admin-de-giay': 'Đế giày',
    'admin-chat-lieu-giay': 'Chất liệu giày',
    'admin-thuong-hieu': 'Thương hiệu',
    'admin-cong-nghe-dem': 'Công nghệ đệm',
    'admin-mau-sac': 'Màu sắc',
    'admin-kich-co': 'Kích cỡ',
    'admin-trong-luong': 'Trọng lượng',
    'admin-danh-gia': 'Quản lý đánh giá',
    'admin-chat': 'Hỗ trợ khách hàng trực tuyến',
    'admin-chatbot-config': 'Cấu hình Chatbot AI',
    'admin-nhan-vien': 'Nhân viên',
    'admin-nhan-vien-them': 'Thêm nhân viên',
    'admin-nhan-vien-lich-lam': 'Lịch làm việc',
    'admin-nhan-vien-chi-tiet': 'Chi tiết nhân viên',
    'admin-khach-hang': 'Khách hàng',
    'admin-khach-hang-them': 'Thêm khách hàng',
    'admin-khach-hang-chi-tiet': 'Chi tiết khách hàng',
    'admin-khach-hang-don-hang': 'Lịch sử mua hàng',
    'admin-lich-lam-viec': 'Lịch làm việc',
    'admin-lich-ca-lam': 'Lịch ca làm',
    'admin-ban-giao-ca': 'Bàn giao ca',
    'admin-thu-chi': 'Quản lý thu / chi',
    'admin-mo-ca': 'Mở ca làm việc',
    'admin-lich-su-hoat-dong': 'Lịch sử hoạt động',
    'admin-profile': 'Hồ sơ cá nhân',
    'nhanvien-profile': 'Hồ sơ cá nhân'
  };
  return route.meta?.title || titles[route.name] || 'Hệ thống Quản lý SportShoe';
});

const subRouteBreadcrumbs = {
  'admin-phieu-giam-gia-them': {
    parentPath: '/admin/phieu-giam-gia',
    parentTitle: 'Phiếu giảm giá',
    childTitle: 'Thêm phiếu giảm giá'
  },
  'admin-phieu-giam-gia-chi-tiet': {
    parentPath: '/admin/phieu-giam-gia',
    parentTitle: 'Phiếu giảm giá',
    childTitle: 'Chi tiết phiếu giảm giá'
  },
  'admin-phieu-giam-gia-khach-hang-them': {
    parentPath: '/admin/phieu-giam-gia-khach-hang',
    parentTitle: 'Phiếu giảm giá khách hàng',
    childTitle: 'Thêm phiếu giảm giá'
  },
  'admin-phieu-giam-gia-khach-hang-chi-tiet': {
    parentPath: '/admin/phieu-giam-gia-khach-hang',
    parentTitle: 'Phiếu giảm giá khách hàng',
    childTitle: 'Chi tiết phiếu giảm giá'
  },
  'admin-hoa-don-chi-tiet': {
    parentPath: '/admin/hoa-don',
    parentTitle: 'Hóa đơn',
    childTitle: 'Chi tiết hóa đơn'
  },
  'admin-san-pham': {
    parentPath: '',
    parentTitle: 'Quản lý sản phẩm',
    childTitle: 'Sản phẩm'
  },
  'admin-san-pham-them': {
    parentPath: '/admin/san-pham',
    parentTitle: 'Sản phẩm',
    childTitle: 'Thêm sản phẩm'
  },
  'admin-chi-tiet-san-pham': {
    parentPath: '/admin/san-pham',
    parentTitle: 'Sản phẩm',
    childTitle: 'Chi tiết sản phẩm'
  },
  'admin-chi-tiet-san-pham-new': {
    parentPath: '/admin/san-pham',
    parentTitle: 'Sản phẩm',
    childTitle: 'Chi tiết sản phẩm'
  },
  'admin-bien-the-san-pham': {
    parentPath: '',
    parentTitle: 'Quản lý sản phẩm',
    childTitle: 'Biến thể sản phẩm'
  },
  'admin-bien-the-san-pham-them': {
    parentPath: '/admin/bien-the-san-pham',
    parentTitle: 'Biến thể sản phẩm',
    childTitle: 'Thêm biến thể sản phẩm'
  },
  'admin-loai-giay': {
    parentPath: '',
    parentTitle: 'Thuộc tính',
    childTitle: 'Loại giày'
  },
  'admin-co-giay': {
    parentPath: '',
    parentTitle: 'Thuộc tính',
    childTitle: 'Cổ giày'
  },
  'admin-de-giay': {
    parentPath: '',
    parentTitle: 'Thuộc tính',
    childTitle: 'Đế giày'
  },
  'admin-chat-lieu-giay': {
    parentPath: '',
    parentTitle: 'Thuộc tính',
    childTitle: 'Chất liệu giày'
  },
  'admin-thuong-hieu': {
    parentPath: '',
    parentTitle: 'Thuộc tính',
    childTitle: 'Thương hiệu'
  },
  'admin-cong-nghe-dem': {
    parentPath: '',
    parentTitle: 'Thuộc tính',
    childTitle: 'Công nghệ đệm'
  },
  'admin-mau-sac': {
    parentPath: '',
    parentTitle: 'Thuộc tính',
    childTitle: 'Màu sắc'
  },
  'admin-kich-co': {
    parentPath: '',
    parentTitle: 'Thuộc tính',
    childTitle: 'Kích cỡ'
  },
  'admin-trong-luong': {
    parentPath: '',
    parentTitle: 'Thuộc tính',
    childTitle: 'Trọng lượng'
  },
  'admin-phieu-giam-gia': {
    parentPath: '',
    parentTitle: 'Quản lý giảm giá',
    childTitle: 'Phiếu giảm giá'
  },
  'admin-dot-giam-gia': {
    parentPath: '',
    parentTitle: 'Quản lý giảm giá',
    childTitle: 'Đợt giảm giá'
  },
  'admin-dot-giam-gia-them': {
    parentPath: '/admin/dot-giam-gia',
    parentTitle: 'Đợt giảm giá',
    childTitle: 'Thêm đợt giảm giá'
  },
  'admin-dot-giam-gia-chi-tiet': {
    parentPath: '/admin/dot-giam-gia',
    parentTitle: 'Đợt giảm giá',
    childTitle: 'Chi tiết đợt giảm giá'
  },
  'admin-dot-giam-gia-san-pham-them': {
    parentPath: '/admin/dot-giam-gia-san-pham',
    parentTitle: 'Đợt giảm giá sản phẩm',
    childTitle: 'Thêm đợt giảm giá'
  },
  'admin-dot-giam-gia-san-pham-chi-tiet': {
    parentPath: '/admin/dot-giam-gia-san-pham',
    parentTitle: 'Đợt giảm giá sản phẩm',
    childTitle: 'Chi tiết đợt giảm giá'
  },
  'admin-nhan-vien-them': {
    parentPath: '/admin/nhan-vien',
    parentTitle: 'Nhân viên',
    childTitle: 'Thêm nhân viên'
  },
  'admin-nhan-vien-chi-tiet': {
    parentPath: '/admin/nhan-vien',
    parentTitle: 'Nhân viên',
    childTitle: 'Chi tiết nhân viên'
  },
  'admin-nhan-vien-lich-lam': {
    parentPath: '/admin/nhan-vien',
    parentTitle: 'Nhân viên',
    childTitle: 'Lịch làm việc'
  },
  'admin-nhan-vien-lich-lam-chi-tiet': {
    parentPath: '/admin/nhan-vien-lich-lam',
    parentTitle: 'Lịch làm việc',
    childTitle: 'Chi tiết lịch làm việc'
  },
  'admin-lich-lam-viec': {
    parentPath: '',
    parentTitle: 'Quản lý lịch làm',
    childTitle: 'Lịch làm việc'
  },
  'admin-lich-ca-lam': {
    parentPath: '',
    parentTitle: 'Quản lý lịch làm',
    childTitle: 'Lịch ca làm'
  },
  'admin-lich-su-hoat-dong': {
    parentPath: '',
    parentTitle: 'Quản lý lịch làm',
    childTitle: 'Lịch sử hoạt động'
  },
  'admin-khach-hang-them': {
    parentPath: '/admin/khach-hang',
    parentTitle: 'Khách hàng',
    childTitle: 'Thêm khách hàng'
  },
  'admin-khach-hang-chi-tiet': {
    parentPath: '/admin/khach-hang',
    parentTitle: 'Khách hàng',
    childTitle: 'Chi tiết khách hàng'
  },
  'admin-khach-hang-don-hang': {
    parentPath: '/admin/khach-hang',
    parentTitle: 'Khách hàng',
    childTitle: 'Lịch sử mua hàng'
  }
};

const currentSubRoute = computed(() => {
  return subRouteBreadcrumbs[route.name] || null;
});

const profileName = computed(() => {
  const username = adminSession.value.tenTaiKhoan?.trim();
  const fullName = adminSession.value.hoTen?.trim();

  if (username && username !== "admin") {
    return fullName || username;
  }

  return FALLBACK_ADMIN_NAME;
});

const hasAvatar = computed(() => Boolean(adminSession.value.hinhAnh?.trim()));

function chuyenDenCapNhatThongTin() {
  hienMenuTaiKhoan.value = false;
  router.push(isAdminRole() ? "/admin/profile" : "/nhanvien/profile");
}

function chuyenDenTrangConfigChatbot() {
  hienMenuTaiKhoan.value = false;
  router.push("/admin/chatbot-config");
}

function chuyenDenTrangChu() {
  hienMenuTaiKhoan.value = false;
  router.push("/khachhang");
}

function chuyenDenCaLamViec() {
  router.push("/admin/ban-giao-ca");
}

function dangXuat() {
  logoutAdmin();
  hienMenuTaiKhoan.value = false;
  router.push("/admin/login");
}
</script>

<template>
  <header class="sticky top-0 z-50 border-b border-slate-200/80 bg-white/95 backdrop-blur dark:border-slate-700 dark:bg-slate-800/95">
    <div class="flex h-[74px] items-center justify-between gap-3 px-4 lg:px-6">
      <div class="flex items-center gap-4">
        <button
          type="button"
          @click="toggleSidebar"
          class="inline-flex h-11 w-11 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 transition hover:border-slate-300 hover:text-slate-700 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300 dark:hover:text-white"
        >
          <Menu class="h-5 w-5" />
        </button>
        <h1
          class="hidden md:block whitespace-nowrap font-bold text-slate-800 dark:text-slate-100"
          :class="currentSubRoute ? 'text-[20px]' : 'text-[28px]'"
        >
          <template v-if="currentSubRoute">
            <router-link
              v-if="currentSubRoute.parentPath"
              :to="currentSubRoute.parentPath"
              class="text-slate-400 hover:text-[#B82220] transition dark:text-slate-500 dark:hover:text-red-400"
              >{{ currentSubRoute.parentTitle }}</router-link
            >
            <span v-else class="text-slate-400 dark:text-slate-500">{{ currentSubRoute.parentTitle }}</span>
            <span class="mx-2 text-slate-300 dark:text-slate-600">/</span>
            <span>{{ currentSubRoute.childTitle }}</span>
          </template>
          <template v-else>
            {{ pageTitle }}
          </template>
        </h1>
      </div>

      <div class="flex items-center gap-3">
        <button
          type="button"
          @click="chuyenDenCaLamViec"
          class="inline-flex h-11 px-4 items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 text-slate-700 transition hover:border-slate-300 hover:bg-white dark:border-slate-700 dark:bg-slate-700 dark:text-slate-200 dark:hover:bg-slate-600 font-semibold text-sm shadow-sm"
        >
          <ArrowRightLeft class="h-4 w-4 text-[#B82220] dark:text-rose-400" />
          <span>Ca làm việc</span>
          <span
            class="h-2 w-2 rounded-full"
            :class="activeShift ? 'bg-emerald-500 animate-pulse' : 'bg-rose-500'"
          ></span>
        </button>

        <button
          type="button"
          @click="toggleDark"
          class="inline-flex h-11 w-11 items-center justify-center rounded-2xl border border-slate-200 bg-slate-50 text-slate-500 transition hover:border-slate-300 hover:bg-white hover:text-slate-700 dark:border-slate-700 dark:bg-slate-700 dark:text-slate-200 dark:hover:bg-slate-600"
        >
          <Sun v-if="!isDark" class="h-5 w-5" />
          <Moon v-else class="h-5 w-5" />
        </button>

        <!-- Notifications Dropdown -->
        <div class="relative">
          <button
            type="button"
            @click="toggleDropdownThongBao"
            class="relative inline-flex h-11 w-11 items-center justify-center rounded-2xl border border-slate-200 bg-slate-50 text-slate-500 transition hover:border-slate-300 hover:bg-white hover:text-slate-700 dark:border-slate-700 dark:bg-slate-700 dark:text-slate-200 dark:hover:bg-slate-600"
          >
            <Bell class="h-5 w-5" />
            <span
              v-if="chuaDocCount > 0"
              class="absolute -top-1 -right-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-[#B82220] px-1 text-[10px] font-bold text-white shadow-sm ring-2 ring-white dark:ring-slate-800 animate-pulse"
            >
              {{ chuaDocCount > 99 ? '99+' : chuaDocCount }}
            </span>
          </button>

          <div
            v-if="dropdownThongBaoMo"
            class="absolute right-0 top-[calc(100%+10px)] z-50 w-80 overflow-hidden rounded-2xl border border-slate-200 bg-white p-2 shadow-[0_18px_45px_rgba(15,23,42,0.16)] dark:border-slate-700 dark:bg-slate-800"
          >
            <div class="flex items-center justify-between border-b border-slate-100 dark:border-slate-700 px-3 py-2">
              <span class="text-sm font-bold text-slate-800 dark:text-slate-100">Thông báo</span>
              <button
                v-if="chuaDocCount > 0"
                @click="markAllAsRead"
                class="text-xs font-semibold text-[#B82220] hover:underline dark:text-rose-400"
              >
                Đọc tất cả
              </button>
            </div>

            <div class="max-h-80 overflow-y-auto divide-y divide-slate-50 dark:divide-slate-700/50 py-1">
              <div v-if="loadingThongBao" class="flex items-center justify-center py-6 text-xs text-slate-400">
                Đang tải...
              </div>
              <div v-else-if="dsThongBao.length === 0" class="flex flex-col items-center justify-center py-8 text-center">
                <BellOff class="h-8 w-8 text-slate-300 dark:text-slate-600 mb-2" />
                <span class="text-xs text-slate-400 dark:text-slate-500">Chưa có thông báo nào</span>
              </div>
              <template v-else>
                <div
                  v-for="tb in dsThongBao"
                  :key="tb.id"
                  @click="handleNotificationClick(tb)"
                  class="flex gap-3 items-start p-3 hover:bg-slate-50 dark:hover:bg-slate-700/50 cursor-pointer transition rounded-xl relative group"
                  :class="!tb.daDoc ? 'bg-slate-50/50 dark:bg-slate-700/20' : ''"
                >
                  <div
                    class="flex h-8 w-8 shrink-0 items-center justify-center rounded-xl"
                    :class="getIconBgClass(tb.loai)"
                  >
                    <component :is="getIconComponent(tb.loai)" class="h-4.5 w-4.5" :class="getIconColorClass(tb.loai)" />
                  </div>
                  <div class="space-y-1 flex-1 min-w-0">
                    <div class="flex items-start justify-between gap-1">
                      <p class="text-xs font-bold text-slate-800 dark:text-slate-100 truncate">
                        {{ tb.tieuDe }}
                      </p>
                      <span v-if="!tb.daDoc" class="h-1.5 w-1.5 shrink-0 rounded-full bg-[#B82220] mt-1.5"></span>
                    </div>
                    <p class="text-[11px] text-slate-500 dark:text-slate-400 line-clamp-2 leading-relaxed">
                      {{ tb.noiDung }}
                    </p>
                    <p class="text-[10px] text-slate-400 dark:text-slate-500">
                      {{ formatTimeAgo(tb.ngayTao) }}
                    </p>
                  </div>

                  <!-- Delete button (visible on hover) -->
                  <button
                    type="button"
                    @click.stop="deleteNotification(tb.id, $event)"
                    class="absolute top-2 right-2 p-1 rounded-lg text-slate-400 hover:text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10 opacity-0 group-hover:opacity-100 transition duration-200"
                    title="Xóa thông báo"
                  >
                    <X class="h-3.5 w-3.5" />
                  </button>
                </div>
              </template>
            </div>
          </div>
        </div>

        <div class="hidden h-8 w-px bg-slate-200 dark:bg-slate-700 sm:block"></div>

        <div class="relative">
          <button
            type="button"
            class="flex items-center gap-2 rounded-full border border-slate-200 bg-white px-2.5 py-1.5 text-left shadow-sm transition hover:border-slate-300 hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-800 dark:hover:bg-slate-700"
            @click="hienMenuTaiKhoan = !hienMenuTaiKhoan"
          >
            <div class="flex h-9 w-9 items-center justify-center overflow-hidden rounded-full bg-slate-100 text-slate-500 dark:bg-slate-700 dark:text-slate-200">
              <img
                :src="avatarUrl"
                :alt="profileName"
                class="h-full w-full object-cover"
              />
            </div>
            <span class="hidden max-w-[180px] truncate text-sm font-semibold text-slate-700 sm:block dark:text-slate-100">
              {{ profileName }}
            </span>
            <ChevronDown
              class="hidden h-4 w-4 text-slate-400 transition sm:block"
              :class="hienMenuTaiKhoan ? 'rotate-180' : ''"
            />
          </button>

          <div
            v-if="hienMenuTaiKhoan"
            class="absolute right-0 top-[calc(100%+10px)] z-50 w-56 overflow-hidden rounded-2xl border border-slate-200 bg-white p-2 shadow-[0_18px_45px_rgba(15,23,42,0.16)] dark:border-slate-700 dark:bg-slate-800"
          >
            <button
              type="button"
              class="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm font-semibold text-slate-700 transition hover:bg-slate-50 dark:text-slate-100 dark:hover:bg-slate-700"
              @click="chuyenDenCapNhatThongTin"
            >
              <UserCog class="h-4 w-4 text-slate-500 dark:text-slate-300" />
              Cập nhật thông tin
            </button>
            <button
              v-if="isAdminRole()"
              type="button"
              class="mt-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm font-semibold text-slate-700 transition hover:bg-slate-50 dark:text-slate-100 dark:hover:bg-slate-700"
              @click="chuyenDenTrangConfigChatbot"
            >
              <Settings2 class="h-4 w-4 text-slate-500 dark:text-slate-300" />
              Cấu hình Chatbot AI
            </button>
            <button
              type="button"
              class="mt-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm font-semibold text-primary transition hover:bg-primary/10 dark:text-primary dark:hover:bg-primary/20"
              @click="dangXuat"
            >
              <LogOut class="h-4 w-4" />
              Đăng xuất
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>
