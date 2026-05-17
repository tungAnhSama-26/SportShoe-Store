<script setup>
import { ref } from 'vue';
import Button from '../components/ui/Button.vue';
import Card from '../components/ui/Card.vue';
import Badge from '../components/ui/Badge.vue';

const sanPhamNoiBat = ref([
  { id: 1, name: "Stride Elite Runner Pro", price: "3,500,000đ", desc: "Phiên bản giới hạn với công nghệ đệm tối đa.", image: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&q=80&w=600" },
  { id: 2, name: "Stride Phantom Basketball", price: "4,200,000đ", desc: "Độ bám tuyệt đối, bức tốc không giới hạn.", image: "https://images.unsplash.com/photo-1551107696-a4b0c5a0d9a2?auto=format&fit=crop&q=80&w=600" },
  { id: 3, name: "Stride Urban X", price: "2,850,000đ", desc: "Thiết kế tương lai cho phong cách đường phố.", image: "https://images.unsplash.com/photo-1608231387042-66d1773070a5?auto=format&fit=crop&q=80&w=600" },
]);
</script>

<template>
  <main class="bg-black min-h-screen text-white pb-20">
    <!-- Hero Banner -->
    <section class="relative py-24 px-6 lg:px-10 overflow-hidden flex flex-col items-center text-center">
      <div class="absolute inset-0 z-0">
        <div class="absolute inset-0 bg-gradient-to-b from-black/60 to-black z-10"></div>
        <img src="https://images.unsplash.com/photo-1556906781-9a412961c28c?auto=format&fit=crop&q=80&w=1920" alt="Featured" class="w-full h-full object-cover opacity-40 grayscale" />
      </div>
      <div class="relative z-10 max-w-3xl mx-auto">
        <Badge variant="primary" class="mb-6 bg-primary text-white border-none px-4 py-1 text-xs uppercase tracking-widest">Nổi Bật Nhất</Badge>
        <h1 class="text-5xl md:text-7xl font-black tracking-tighter mb-6">ĐỈNH CAO<br/><span class="text-transparent bg-clip-text bg-gradient-to-r from-primary to-rose-500">HIỆU SUẤT</span></h1>
        <p class="text-slate-400 text-lg md:text-xl font-light mb-10 max-w-2xl mx-auto">Những thiết kế mang tính biểu tượng, hội tụ công nghệ tinh túy nhất của Stride.</p>
      </div>
    </section>

    <!-- Featured Grid -->
    <section class="mx-auto max-w-7xl px-6 lg:px-10 mt-10 space-y-20">
      <div 
        v-for="(sp, index) in sanPhamNoiBat" 
        :key="sp.id"
        class="flex flex-col md:flex-row items-center gap-10 lg:gap-20"
        :class="index % 2 !== 0 ? 'md:flex-row-reverse' : ''"
      >
        <div class="w-full md:w-1/2">
          <div class="relative aspect-[4/3] rounded-2xl overflow-hidden bg-zinc-900 group">
            <div class="absolute inset-0 bg-gradient-to-tr from-primary/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 z-10"></div>
            <img :src="sp.image" :alt="sp.name" class="w-full h-full object-cover transition-transform duration-1000 group-hover:scale-110" />
          </div>
        </div>
        <div class="w-full md:w-1/2 space-y-6">
          <h2 class="text-4xl font-bold tracking-tight">{{ sp.name }}</h2>
          <p class="text-slate-400 text-lg">{{ sp.desc }}</p>
          <div class="text-3xl font-bold text-white">{{ sp.price }}</div>
          <div class="pt-6">
            <Button variant="primary" size="lg" class="shadow-primary/50 shadow-lg min-w-[200px]">Mua Ngay</Button>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>
