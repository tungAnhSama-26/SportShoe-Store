<script setup>
import anhMacDinh from "../../assets/login-shoe.png";

// Prop "danhMuc" giờ chứa danh sách HÃNG (thương hiệu) nổi bật.
defineProps({
  danhMuc: {
    type: Array,
    required: true,
  },
});

function xuLyAnhLoi(event) {
  if (event.target.src !== anhMacDinh) {
    event.target.src = anhMacDinh;
  }
}
</script>

<template>
  <section id="bo-suu-tap" class="bg-transparent px-6 py-14 lg:px-10 lg:py-16">
    <div class="mx-auto max-w-7xl">
      <div class="text-center">
        <h2 class="text-2xl font-semibold text-black">Mua sắm theo hãng</h2>
        <p class="mt-3 text-sm text-slate-500">Khám phá các thương hiệu nổi bật nhất</p>
      </div>

      <p v-if="!danhMuc.length" class="mt-10 rounded-2xl border border-dashed border-primary/20 bg-white/60 py-12 text-center text-sm text-slate-400">
        Chưa có hãng nào để hiển thị.
      </p>

      <div v-else class="mt-10 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <!-- Bấm vào hãng -> sang trang sản phẩm và tự tích bộ lọc hãng đó. -->
        <router-link
          v-for="(muc, index) in danhMuc"
          :key="muc.id ?? muc.ten"
          :to="{ path: '/khachhang/san-pham', query: { hang: muc.ten } }"
          class="group block overflow-hidden rounded-2xl border border-primary/20 bg-white transition hover:-translate-y-1 hover:shadow-primary/10 hover:shadow-2xl"
        >
          <div class="relative h-72 overflow-hidden">
            <img
              :src="muc.hinhAnh"
              :alt="muc.ten"
              class="h-full w-full object-cover transition duration-500 group-hover:scale-105"
              @error="xuLyAnhLoi"
            />
            <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>
            <div class="absolute inset-x-0 bottom-0 p-5 text-white">
              <p class="text-[11px] uppercase tracking-[0.24em] text-secondary">0{{ index + 1 }}</p>
              <h3 class="mt-2 text-xl font-semibold">{{ muc.ten }}</h3>
              <p v-if="muc.moTa" class="mt-2 text-sm text-white/85">{{ muc.moTa }}</p>
            </div>
          </div>
        </router-link>
      </div>
    </div>
  </section>
</template>
