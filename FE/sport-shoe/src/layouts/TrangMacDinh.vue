<script setup>
import logoChinh from "../assets/logo/delete-background-logo.png";
import KhungDauTrang from "../components/trang-chu/KhungDauTrang.vue";
import { cuaHangUngDung } from "../stores/ung-dung";
</script>

<template>
  <div class="min-h-screen overflow-x-hidden bg-white">
    <KhungDauTrang :thuong-hieu="cuaHangUngDung.thuongHieu" />
    <div class="pt-[72px]">
      <router-view />
    </div>

    <footer class="bg-[#080808] px-6 py-12 text-white lg:px-10">
      <div class="mx-auto grid max-w-7xl gap-10 md:grid-cols-[1.4fr_0.7fr_0.7fr_1fr]">
        <div>
          <a href="#trang-chu" class="inline-flex rounded-2xl bg-white p-3 shadow-[0_12px_30px_rgba(255,106,0,0.12)]">
            <img :src="logoChinh" :alt="cuaHangUngDung.thuongHieu" class="h-16 w-auto object-contain" />
          </a>
          <p class="mt-4 max-w-sm text-xs leading-6 text-slate-400">
            Điểm đến dành cho những ai yêu giày chất lượng. Tự tin bước vào phong cách của riêng bạn.
          </p>
          <div class="mt-6 flex items-center gap-4 text-slate-400">
            <a href="#" class="transition hover:text-white" aria-label="Facebook">
              <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"/>
              </svg>
            </a>
            <a href="#" class="transition hover:text-white" aria-label="Instagram">
              <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect width="20" height="20" x="2" y="2" rx="5" ry="5"/>
                <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"/>
                <line x1="17.5" x2="17.51" y1="6.5" y2="6.5"/>
              </svg>
            </a>
            <a href="#" class="transition hover:text-white" aria-label="Twitter">
              <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 4s-.7 2.1-2 3.4c1.6 10-9.4 17.3-18 11.6 2.2.1 4.4-.6 6-2C3 15.5.5 9.6 3 5c2.2 2.6 5.6 4.1 9 4-.9-4.2 4-6.6 7-3.8 1.1 0 3-1.2 3-1.2z"/>
              </svg>
            </a>
            <a href="#" class="transition hover:text-white" aria-label="TikTok">
              <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 12a4 4 0 1 0 4 4V4a5 5 0 0 0 5 5"/>
              </svg>
            </a>
          </div>
        </div>

        <div>
          <p class="text-sm font-semibold text-white">Mua sắm</p>
          <div class="mt-4 space-y-2 text-xs text-slate-400">
            <a href="#noi-bat" class="block hover:text-[#ff6a00]">Hàng mới</a>
            <a href="#bo-suu-tap" class="block hover:text-[#ff6a00]">Nam</a>
            <a href="#bo-suu-tap" class="block hover:text-[#ff6a00]">Nữ</a>
            <a href="#bo-suu-tap" class="block hover:text-[#ff6a00]">Trẻ em</a>
            <a href="#gia-tri" class="block hover:text-[#ff6a00]">Giảm giá</a>
          </div>
        </div>

        <div>
          <p class="text-sm font-semibold text-white">Hỗ trợ</p>
          <div class="mt-4 space-y-2 text-xs text-slate-400">
            <a href="#" class="block hover:text-[#ff6a00]">Liên hệ</a>
            <a href="#" class="block hover:text-[#ff6a00]">Thông tin giao hàng</a>
            <a href="#" class="block hover:text-[#ff6a00]">Đổi trả</a>
            <a href="#" class="block hover:text-[#ff6a00]">Hướng dẫn chọn size</a>
            <a href="#" class="block hover:text-[#ff6a00]">FAQ</a>
          </div>
        </div>

        <div>
          <p class="text-sm font-semibold text-white">Bản tin</p>
          <p class="mt-4 text-xs leading-6 text-slate-400">
            Đăng ký để nhận ưu đãi và cập nhật mới nhất.
          </p>
          <div class="mt-4 flex gap-2">
            <input
              type="email"
              placeholder="Email của bạn"
              class="w-full rounded-md border border-white/10 bg-[#111111] px-3 py-2 text-xs text-white outline-none placeholder:text-slate-500"
            />
            <button class="rounded-md bg-gradient-to-r from-[#cf1018] to-[#ff6a00] px-4 py-2 text-xs font-semibold text-white">
              Đăng ký
            </button>
          </div>
        </div>
      </div>

      <div class="mx-auto mt-10 flex max-w-7xl flex-col gap-4 border-t border-white/10 pt-6 text-[11px] text-slate-500 md:flex-row md:items-center md:justify-between">
        <p>© 2025 Stride. Đã đăng ký mọi quyền.</p>
        <div class="flex flex-wrap items-center gap-4">
          <a href="#" class="hover:text-[#ff6a00]">Chính sách bảo mật</a>
          <a href="#" class="hover:text-[#ff6a00]">Điều khoản dịch vụ</a>
          <a href="#" class="hover:text-[#ff6a00]">Chính sách cookie</a>
        </div>
      </div>
    </footer>
  </div>
</template>
