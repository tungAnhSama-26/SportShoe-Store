<script setup>
import { ref } from 'vue';
import Button from '../components/ui/Button.vue';
import Card from '../components/ui/Card.vue';
import Badge from '../components/ui/Badge.vue';
import { danhMucGiay } from '../constants/trangChu';

// Dummy products for the static UI layout as requested (keep logic unchanged)
const sanPhamDanhSach = ref([
  { id: 1, name: "Stride Elite Runner", price: "2,500,000đ", category: "Chạy bộ", image: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&q=80&w=600", isNew: true },
  { id: 2, name: "Stride Urban Velocity", price: "1,850,000đ", category: "Thời trang", image: "https://images.unsplash.com/photo-1608231387042-66d1773070a5?auto=format&fit=crop&q=80&w=600", isNew: false },
  { id: 3, name: "Stride Pro Basketball", price: "3,200,000đ", category: "Bóng rổ", image: "https://images.unsplash.com/photo-1551107696-a4b0c5a0d9a2?auto=format&fit=crop&q=80&w=600", isNew: true },
  { id: 4, name: "Stride Classic Leather", price: "1,950,000đ", category: "Cổ điển", image: "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&q=80&w=600", isNew: false },
  { id: 5, name: "Stride Trail Blazer", price: "2,800,000đ", category: "Leo núi", image: "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&q=80&w=600", isNew: false },
  { id: 6, name: "Stride Flex Trainer", price: "1,500,000đ", category: "Gym", image: "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?auto=format&fit=crop&q=80&w=600", isNew: false },
]);

const boLocChon = ref('Tất cả');
</script>

<template>
  <main class="bg-slate-50 min-h-screen pb-20">
    <!-- Hero Section -->
    <section class="bg-black text-white py-16 px-6 lg:px-10">
      <div class="mx-auto max-w-7xl">
        <h1 class="text-4xl md:text-5xl font-bold tracking-tight mb-4">Bộ Sưu Tập Giày</h1>
        <p class="text-slate-400 max-w-xl text-sm md:text-base">Khám phá các dòng sản phẩm chất lượng cao của Stride, được thiết kế để mang lại hiệu suất và phong cách vượt trội cho mọi hoạt động của bạn.</p>
      </div>
    </section>

    <div class="mx-auto max-w-7xl px-6 lg:px-10 mt-10 grid grid-cols-1 lg:grid-cols-[280px_1fr] gap-10">
      <!-- Sidebar Filters -->
      <aside class="space-y-8">
        <div>
          <h3 class="text-lg font-bold text-slate-900 mb-4 pb-2 border-b border-slate-200">Danh Mục</h3>
          <ul class="space-y-3">
            <li>
              <button 
                @click="boLocChon = 'Tất cả'" 
                class="text-sm font-medium transition-colors hover:text-primary"
                :class="boLocChon === 'Tất cả' ? 'text-primary' : 'text-slate-600'"
              >
                Tất cả sản phẩm
              </button>
            </li>
            <li v-for="dm in danhMucGiay" :key="dm.id">
              <button 
                @click="boLocChon = dm.ten" 
                class="text-sm font-medium transition-colors hover:text-primary"
                :class="boLocChon === dm.ten ? 'text-primary' : 'text-slate-600'"
              >
                {{ dm.ten }}
              </button>
            </li>
          </ul>
        </div>
        
        <div>
          <h3 class="text-lg font-bold text-slate-900 mb-4 pb-2 border-b border-slate-200">Lọc theo giá</h3>
          <div class="space-y-3 text-sm text-slate-600 font-medium">
            <label class="flex items-center gap-3 cursor-pointer hover:text-primary transition-colors">
              <input type="checkbox" class="rounded border-slate-300 text-primary focus:ring-primary/30" />
              Dưới 1,000,000đ
            </label>
            <label class="flex items-center gap-3 cursor-pointer hover:text-primary transition-colors">
              <input type="checkbox" class="rounded border-slate-300 text-primary focus:ring-primary/30" />
              1,000,000đ - 2,000,000đ
            </label>
            <label class="flex items-center gap-3 cursor-pointer hover:text-primary transition-colors">
              <input type="checkbox" class="rounded border-slate-300 text-primary focus:ring-primary/30" />
              Trên 2,000,000đ
            </label>
          </div>
        </div>
      </aside>

      <!-- Product Grid -->
      <section>
        <div class="flex items-center justify-between mb-6">
          <p class="text-sm text-slate-500">Hiển thị <span class="font-bold text-slate-900">{{ sanPhamDanhSach.length }}</span> kết quả</p>
          <select class="text-sm border-slate-200 rounded-lg focus:border-primary focus:ring-primary/30 bg-white">
            <option>Mới nhất</option>
            <option>Giá tăng dần</option>
            <option>Giá giảm dần</option>
          </select>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
          <Card 
            v-for="sp in sanPhamDanhSach" 
            :key="sp.id"
            class="group overflow-hidden cursor-pointer flex flex-col hover:shadow-xl hover:-translate-y-1 transition-all duration-300 border-none bg-white"
          >
            <div class="relative aspect-square overflow-hidden bg-slate-100">
              <img :src="sp.image" :alt="sp.name" class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105" />
              <Badge v-if="sp.isNew" variant="primary" class="absolute top-3 left-3 bg-primary text-white border-none shadow-md">Mới</Badge>
              <div class="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity flex items-end p-4">
                <Button variant="primary" size="sm" class="w-full shadow-lg translate-y-4 group-hover:translate-y-0 transition-transform duration-300">
                  Thêm vào giỏ
                </Button>
              </div>
            </div>
            <div class="p-4 flex flex-col flex-1">
              <p class="text-xs text-slate-400 mb-1 font-medium">{{ sp.category }}</p>
              <h3 class="font-bold text-slate-900 text-base mb-2 line-clamp-2 group-hover:text-primary transition-colors">{{ sp.name }}</h3>
              <p class="font-bold text-lg mt-auto">{{ sp.price }}</p>
            </div>
          </Card>
        </div>

        <div class="mt-12 flex justify-center">
          <Button variant="outline" class="min-w-[200px]">Tải thêm sản phẩm</Button>
        </div>
      </section>
    </div>
  </main>
</template>
