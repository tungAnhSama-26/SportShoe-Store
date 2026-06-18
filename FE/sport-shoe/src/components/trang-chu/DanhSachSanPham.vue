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
        <router-link
          to="/khachhang/san-pham"
          class="rounded-xl border border-primary/20 bg-white px-5 py-2.5 text-xs font-semibold text-primary transition-all hover:bg-primary/5 hover:border-primary/30 hover:-translate-y-0.5 hover:shadow-sm"
        >
          Xem tất cả
        </router-link>
      </div>

      <p v-if="!sanPham.length" class="rounded-2xl border border-dashed border-primary/20 bg-white/60 py-12 text-center text-sm text-slate-400">
        Chưa có sản phẩm nổi bật để hiển thị.
      </p>

      <div v-else class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <!-- Card giống màn sản phẩm; thứ tự do backend xếp: bán chạy > giảm sâu > đánh giá cao & nhiều. -->
        <router-link
          v-for="muc in sanPham"
          :key="muc.id ?? muc.ten"
          :to="`/khachhang/san-pham/${muc.id}`"
          class="group block overflow-hidden rounded-2xl border border-primary/15 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-primary/10 hover:shadow-2xl"
        >
          <div class="relative h-60 overflow-hidden bg-slate-50">
            <img :src="muc.hinhAnh" :alt="muc.ten" class="h-full w-full object-cover transition-transform duration-700 group-hover:scale-105" @error="xuLyAnhLoi" />
            <span
              v-if="muc.nhan"
              class="absolute left-3 top-3 rounded-md bg-red-600 px-2.5 py-1 text-xs font-extrabold text-white shadow-md"
            >
              {{ muc.nhan }}
            </span>
          </div>

          <div class="p-5">
            <p class="text-[11px] text-slate-400 font-medium">{{ muc.thuongHieu }}</p>
            <h3 class="mt-1 text-sm font-semibold text-black line-clamp-2 group-hover:text-primary transition-colors">{{ muc.ten }}</h3>

            <div class="mt-3 flex items-end gap-2">
              <span class="text-base font-bold text-primary">{{ dinhDangTienViet(muc.gia) }}</span>
              <span v-if="muc.giaCu" class="pb-0.5 text-[11px] text-slate-400 line-through">
                {{ dinhDangTienViet(muc.giaCu) }}
              </span>
            </div>
            <div class="mt-1.5 flex items-center gap-1.5">
              <div class="flex text-xs">
                <span v-for="i in 5" :key="i" :class="i <= Math.round(muc.soSao) ? 'text-amber-400' : 'text-slate-300'">★</span>
              </div>
              <span class="text-[11px] text-slate-400">
                {{ muc.soDanhGia ? `${muc.soSao.toFixed(1)} (${muc.soDanhGia})` : 'Chưa có đánh giá' }}
                <template v-if="muc.daBan"> · Đã bán {{ muc.daBan }}</template>
              </span>
            </div>
          </div>
        </router-link>
      </div>
    </div>
  </section>
</template>
