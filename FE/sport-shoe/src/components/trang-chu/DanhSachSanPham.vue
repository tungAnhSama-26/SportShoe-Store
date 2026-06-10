<script setup>
import { dinhDangTienViet } from "../../utils/dinhDangTien";
import anhMacDinh from "../../assets/login-shoe.png";

defineProps({
  sanPham: {
    type: Array,
    required: true,
  },
});

// Khi ảnh sản phẩm không tải được (đường dẫn trong DB chưa có file), dùng ảnh mặc định.
function xuLyAnhLoi(event) {
  if (event.target.src !== anhMacDinh) {
    event.target.src = anhMacDinh;
  }
}
</script>

<template>
  <section id="noi-bat" class="bg-transparent px-6 py-14 lg:px-10 lg:py-16">
    <div class="mx-auto max-w-7xl">
      <div class="mb-8 flex items-start justify-between gap-4">
        <div>
          <p class="text-sm font-semibold text-black">Sản phẩm nổi bật</p>
          <p class="mt-2 text-xs text-slate-500">Những lựa chọn được chọn riêng cho bạn</p>
        </div>
        <button class="rounded-xl border border-primary/20 bg-white px-5 py-2.5 text-xs font-semibold text-primary transition-all hover:bg-primary/5 hover:border-primary/30 hover:-translate-y-0.5 hover:shadow-sm">
          Xem tất cả
        </button>
      </div>

      <p v-if="!sanPham.length" class="rounded-2xl border border-dashed border-primary/20 bg-white/60 py-12 text-center text-sm text-slate-400">
        Chưa có sản phẩm nổi bật để hiển thị.
      </p>

      <div v-else class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <article
          v-for="muc in sanPham"
          :key="muc.id ?? muc.ten"
          class="overflow-hidden rounded-2xl border border-primary/15 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-primary/10 hover:shadow-2xl"
        >
          <div class="relative h-60 overflow-hidden bg-slate-50">
            <img :src="muc.hinhAnh" :alt="muc.ten" class="h-full w-full object-cover" @error="xuLyAnhLoi" />
            <span
              v-if="muc.nhan"
              class="absolute left-3 top-3 rounded-md bg-primary px-2 py-1 text-[10px] font-semibold text-white shadow-sm"
            >
              {{ muc.nhan }}
            </span>
            <button
              class="absolute right-3 top-3 flex h-7 w-7 items-center justify-center rounded-full bg-white/90 text-slate-600 shadow-sm backdrop-blur-sm transition-all hover:text-primary hover:scale-110"
              aria-label="Yêu thích"
            >
              <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="m12 21-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3A5.5 5.5 0 0 1 12 5.09 5.5 5.5 0 0 1 16.5 3C19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.18z" />
              </svg>
            </button>
          </div>

          <div class="p-5">
            <h3 class="text-sm font-medium text-black">{{ muc.ten }}</h3>
            <p class="mt-1 text-[11px] text-slate-500">{{ muc.soMau }}</p>

            <div class="mt-4 flex items-end justify-between gap-3">
              <div class="flex items-center gap-2">
                <span class="text-sm font-semibold text-primary">{{ dinhDangTienViet(muc.gia) }}</span>
                <span v-if="muc.giaCu" class="text-[11px] text-slate-400 line-through">
                  {{ dinhDangTienViet(muc.giaCu) }}
                </span>
              </div>

              <button class="text-slate-400 transition hover:text-primary hover:scale-110" aria-label="Thêm vào giỏ">
                <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="9" cy="20" r="1" />
                  <circle cx="18" cy="20" r="1" />
                  <path d="M3 4h2l2.4 10.2a1 1 0 0 0 1 .8h9.7a1 1 0 0 0 1-.8L21 7H7" />
                </svg>
              </button>
            </div>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>
