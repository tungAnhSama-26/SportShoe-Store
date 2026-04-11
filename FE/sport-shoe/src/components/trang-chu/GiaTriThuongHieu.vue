<script setup>
defineProps({
  danhSach: {
    type: Array,
    required: true,
  },
});

function laySvg(loai) {
  if (loai === "giao-hang") return "M3 7h11l4 4m0 0 3-3m-3 3v7m7-11h3l3 3v8a2 2 0 0 1-2 2h-1m-13 0H6a2 2 0 0 1-2-2V7m2 13a2 2 0 1 0 4 0m8 0a2 2 0 1 0 4 0";
  if (loai === "thanh-toan") return "M12 3l7 4v5c0 5-3.5 8-7 9-3.5-1-7-4-7-9V7l7-4z";
  if (loai === "doi-tra") return "M3 12a9 9 0 1 0 3-6.7M3 4v5h5M21 12a9 9 0 1 1-3 6.7M21 20v-5h-5";
  return "M18 10c0 3.31-2.69 6-6 6s-6-2.69-6-6 2.69-6 6-6 6 2.69 6 6Zm-6 9c4.97 0 9-4.03 9-9S16.97 1 12 1 3 5.03 3 10s4.03 9 9 9Zm-1-5h2v2h-2Zm0-8h2v6h-2Z";
}
</script>

<template>
  <section id="gia-tri" class="bg-transparent px-6 py-10 lg:px-10 lg:py-12">
    <div class="mx-auto grid max-w-7xl gap-8 border-t border-[#f0d2c9] pt-10 md:grid-cols-2 xl:grid-cols-4">
      <article v-for="muc in danhSach" :key="muc.tieuDe" class="text-center">
        <div class="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-[#cf1018] to-[#ff6a00] text-white shadow-[0_10px_25px_rgba(207,16,24,0.24)]">
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path :d="laySvg(muc.bieuTuong)" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </div>
        <p class="mt-4 text-sm font-medium text-[#080808]">{{ muc.tieuDe }}</p>
        <p class="mt-2 text-xs text-slate-500">{{ muc.noiDung }}</p>
      </article>
    </div>
  </section>
</template>
