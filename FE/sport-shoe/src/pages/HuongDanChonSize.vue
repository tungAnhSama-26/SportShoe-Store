<script setup>
const bangSize = [
  { chieuDai: "22.0–22.5", size: "35" },
  { chieuDai: "22.6–23.0", size: "36" },
  { chieuDai: "23.1–23.5", size: "37" },
  { chieuDai: "23.6–24.0", size: "38" },
  { chieuDai: "24.1–24.5", size: "39" },
  { chieuDai: "24.6–25.0", size: "40" },
  { chieuDai: "25.1–25.5", size: "41" },
  { chieuDai: "25.6–26.0", size: "42" },
  { chieuDai: "26.1–26.5", size: "43" },
  { chieuDai: "26.6–27.0", size: "44" },
];
</script>

<template>
  <main class="min-h-screen bg-slate-50 pb-20">
    <section class="bg-black px-6 py-16 text-white lg:px-10"><div class="mx-auto max-w-6xl"><p class="mb-3 text-sm font-semibold uppercase tracking-[0.22em] text-red-400">Vừa chân, thoải mái</p><h1 class="text-4xl font-bold md:text-5xl">Hướng dẫn chọn size</h1><p class="mt-4 max-w-2xl leading-7 text-slate-300">Đo bàn chân đúng cách để lựa chọn kích cỡ phù hợp hơn.</p></div></section>
    <section class="mx-auto grid max-w-6xl gap-8 px-6 py-12 lg:grid-cols-[0.9fr_1.1fr]">
      <article class="rounded-2xl bg-white p-7 shadow-sm"><h2 class="text-xl font-bold">Cách đo bàn chân</h2><ol class="mt-6 space-y-5 text-sm leading-7 text-slate-600"><li><strong class="mr-2 text-primary">1.</strong>Đặt một tờ giấy sát tường và đứng thẳng, gót chân chạm nhẹ vào tường.</li><li><strong class="mr-2 text-primary">2.</strong>Đánh dấu điểm xa nhất của ngón chân, sau đó đo từ mép giấy đến điểm vừa đánh dấu.</li><li><strong class="mr-2 text-primary">3.</strong>Đo cả hai chân và dùng số đo của bàn chân dài hơn.</li><li><strong class="mr-2 text-primary">4.</strong>Đối chiếu số đo với bảng size. Nếu nằm giữa hai size, ưu tiên size lớn hơn.</li></ol><p class="mt-6 rounded-xl bg-amber-50 p-4 text-sm leading-6 text-amber-800">Nên đo vào cuối ngày và mang loại tất bạn thường sử dụng cùng đôi giày.</p></article>
      <article class="overflow-hidden rounded-2xl bg-white shadow-sm"><div class="border-b border-slate-100 p-7"><h2 class="text-xl font-bold">Bảng quy đổi tham khảo</h2><p class="mt-2 text-sm text-slate-500">Chiều dài bàn chân tính bằng centimet.</p></div><div class="overflow-x-auto"><table class="w-full text-left text-sm"><thead class="bg-slate-100 text-slate-700"><tr><th class="px-7 py-4">Chiều dài chân (cm)</th><th class="px-7 py-4">Size EU</th></tr></thead><tbody><tr v-for="dong in bangSize" :key="dong.size" class="border-t border-slate-100"><td class="px-7 py-3.5 text-slate-600">{{ dong.chieuDai }}</td><td class="px-7 py-3.5 font-bold text-slate-900">{{ dong.size }}</td></tr></tbody></table></div></article>
    </section>
  </main>
</template>
