<script setup>
import { ref } from "vue";
import { showSuccess } from "../utils/alert";
import logoChinh from "../assets/logo/delete-background-logo.png";
import ChatBox from "../components/common/ChatBox.vue";
import KhungDauTrang from "../components/trang-chu/KhungDauTrang.vue";
import { cuaHangUngDung } from "../stores/ung-dung";

const emailBanTin = ref("");

function xuLyDangKyBanTin() {
  if (emailBanTin.value) {
    showSuccess("Đăng ký nhận bản tin thành công!", "Cảm ơn bạn");
    emailBanTin.value = "";
  }
}

const nhomLienKet = [
  {
    tieuDe: "Mua sắm",
    lienKet: [
      { nhan: "Hàng mới", den: { name: "hang-moi" } },
      { nhan: "Nam", den: { name: "nam" } },
      { nhan: "Nữ", den: { name: "nu" } },
      { nhan: "Trẻ em", den: { name: "tre-em" } },
      { nhan: "Giảm giá", den: { name: "giam-gia" } },
    ],
  },
  {
    tieuDe: "Hỗ trợ",
    lienKet: [
      { nhan: "Liên hệ", den: { name: "lien-he" } },
      { nhan: "Thông tin giao hàng", den: { name: "thong-tin-giao-hang" } },
      { nhan: "Hướng dẫn chọn size", den: { name: "huong-dan-chon-size" } },
      { nhan: "FAQ", den: { name: "faq" } },
    ],
  },
  {
    tieuDe: "Khám phá",
    lienKet: [
      { nhan: "Trang chủ", den: { name: "trang-chu" } },
      { nhan: "Danh mục", den: { name: "danh-muc" } },
      { nhan: "Sản phẩm", den: { name: "san-pham" } },
      { nhan: "Sản phẩm nổi bật", den: { name: "noi-bat" } },
      { nhan: "Giới thiệu", den: { name: "gioi-thieu" } },
    ],
  },
  {
    tieuDe: "Mua sắm & đơn hàng",
    lienKet: [
      { nhan: "Giỏ hàng", den: { name: "gio-hang" } },
      { nhan: "Đơn hàng của tôi", den: { name: "don-hang" } },
      { nhan: "Theo dõi đơn hàng", den: { name: "tra-cuu-don" } },
      { nhan: "Đánh giá", den: { name: "danh-gia-cong-khai" } },
      { nhan: "Gợi ý giày", den: { name: "goi-y-giay" } },
    ],
  },
  {
    tieuDe: "Tài khoản",
    lienKet: [
      { nhan: "Đăng nhập", den: { name: "login" } },
      { nhan: "Đăng ký", den: { name: "register" } },
      { nhan: "Quên mật khẩu", den: { name: "forgot-password" } },
      { nhan: "Thông tin cá nhân", den: { name: "client-profile" } },
    ],
  },
];
</script>

<template>
  <div class="min-h-screen overflow-x-hidden bg-white">
    <KhungDauTrang :thuong-hieu="cuaHangUngDung.thuongHieu" />
    <div class="pt-[72px]">
      <router-view />
    </div>

    <ChatBox />

    <footer class="border-t border-[#dadad3] bg-white px-6 py-12 text-[#62625b] lg:px-10">
      <div class="mx-auto grid max-w-7xl gap-10 sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-[1.3fr_repeat(5,0.8fr)_1.2fr]">
        <div>
          <RouterLink
            :to="{ name: 'trang-chu' }"
            class="inline-flex rounded-2xl border border-[#dadad3] bg-[#f6f6f3] p-3"
          >
            <img
              :src="logoChinh"
              :alt="cuaHangUngDung.thuongHieu"
              class="h-16 w-auto object-contain"
            />
          </RouterLink>
          <p class="mt-4 max-w-sm text-xs leading-6 text-[#62625b]">
            Điểm đến dành cho những ai yêu giày chất lượng. Tự tin bước vào phong cách của riêng bạn.
          </p>
          <div class="mt-6 flex items-center gap-4 text-[#91918c]">
            <a href="https://www.facebook.com/profile.php?id=61588337362695" target="_blank" rel="noopener noreferrer" aria-label="Facebook" title="Facebook" class="transition-colors hover:text-primary">
              <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z" />
              </svg>
            </a>
            <a href="#" aria-label="Instagram" title="Instagram" class="transition-colors hover:text-primary">
              <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect width="20" height="20" x="2" y="2" rx="5" ry="5" />
                <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z" />
                <line x1="17.5" x2="17.51" y1="6.5" y2="6.5" />
              </svg>
            </a>
            <a href="#" aria-label="Twitter" title="Twitter" class="transition-colors hover:text-primary">
              <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 4s-.7 2.1-2 3.4c1.6 10-9.4 17.3-18 11.6 2.2.1 4.4-.6 6-2C3 15.5.5 9.6 3 5c2.2 2.6 5.6 4.1 9 4-.9-4.2 4-6.6 7-3.8 1.1 0 3-1.2 3-1.2z" />
              </svg>
            </a>
            <a href="https://www.tiktok.com/@pekor266?_r=1&_t=ZS-98dFglK6GZh" target="_blank" rel="noopener noreferrer" aria-label="TikTok" title="TikTok" class="transition-colors hover:text-primary">
              <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 12a4 4 0 1 0 4 4V4a5 5 0 0 0 5 5" />
              </svg>
            </a>
          </div>
        </div>

        <div v-for="nhom in nhomLienKet" :key="nhom.tieuDe">
          <p class="text-sm font-semibold text-black">{{ nhom.tieuDe }}</p>
          <nav class="mt-4 space-y-2 text-xs text-[#62625b]" :aria-label="nhom.tieuDe">
            <RouterLink
              v-for="lienKet in nhom.lienKet"
              :key="lienKet.nhan"
              :to="lienKet.den"
              class="block transition-colors hover:text-primary"
            >
              {{ lienKet.nhan }}
            </RouterLink>
          </nav>
        </div>

        <div>
          <p class="text-sm font-semibold text-black">Bản tin</p>
          <p class="mt-4 text-xs leading-6 text-[#62625b]">
            Đăng ký để nhận ưu đãi và cập nhật mới nhất.
          </p>
          <form @submit.prevent="xuLyDangKyBanTin" class="mt-4 flex gap-2">
            <input
              type="email"
              v-model="emailBanTin"
              required
              placeholder="Email của bạn"
              aria-label="Email đăng ký nhận bản tin"
              class="w-full min-w-0 rounded-2xl border border-[#91918c] bg-white px-3 py-2 text-xs text-black outline-none placeholder:text-[#91918c] focus:border-black focus:ring-2 focus:ring-[#435ee5]"
            />
            <button
              type="submit"
              class="rounded-2xl bg-primary px-4 py-2 text-xs font-semibold text-white transition-all hover:bg-primary-hover"
            >
              Đăng ký
            </button>
          </form>
        </div>
      </div>

      <div class="mx-auto mt-10 max-w-7xl border-t border-[#dadad3] pt-6 text-[11px] text-[#91918c]">
        <p>© 2026 SportShoe. Đã đăng ký mọi quyền.</p>
      </div>
    </footer>
  </div>
</template>
