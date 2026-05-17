<script setup>
import Button from '../components/ui/Button.vue';
</script>

<template>
  <main class="bg-white min-h-screen pb-20">
    <!-- Hero Brand Section -->
    <section class="bg-slate-50 py-20 px-6 lg:px-10 text-center">
      <div class="mx-auto max-w-4xl space-y-8">
        <h1 class="text-5xl md:text-6xl font-black text-slate-900 tracking-tighter">
          TẦM NHÌN <span class="text-primary">STRIDE</span>
        </h1>
        <p class="text-lg md:text-xl text-slate-600 font-light max-w-2xl mx-auto leading-relaxed">
          Chúng tôi không chỉ bán giày. Chúng tôi trao quyền để bạn tự tin bước đi trên con đường của riêng mình. 
          Mỗi đôi giày Stride là sự kết hợp hoàn hảo giữa công nghệ tiên tiến và phong cách tối giản.
        </p>
      </div>
    </section>

    <!-- Content Sections -->
    <section class="mx-auto max-w-7xl px-6 lg:px-10 mt-20 space-y-32">
      <!-- Section 1 -->
      <div class="flex flex-col md:flex-row items-center gap-12">
        <div class="w-full md:w-1/2">
          <div class="aspect-square rounded-3xl overflow-hidden bg-slate-100 relative">
            <img src="https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&q=80&w=800" alt="Innovation" class="w-full h-full object-cover" />
          </div>
        </div>
        <div class="w-full md:w-1/2 space-y-6">
          <h2 class="text-3xl font-bold text-slate-900">Sự Đổi Mới Không Ngừng</h2>
          <p class="text-slate-600 leading-relaxed text-lg">
            Khởi nguồn từ một xưởng thiết kế nhỏ, Stride mang trong mình khát khao phá vỡ những ranh giới thiết kế truyền thống. 
            Chúng tôi ứng dụng vật liệu tái chế siêu nhẹ cùng công nghệ đệm phản hồi năng lượng độc quyền.
          </p>
          <ul class="space-y-4 text-slate-700 font-medium">
            <li class="flex items-center gap-3">
              <svg class="w-6 h-6 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
              Thiết kế công thái học vượt trội
            </li>
            <li class="flex items-center gap-3">
              <svg class="w-6 h-6 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
              Độ bền bỉ đã được kiểm chứng
            </li>
            <li class="flex items-center gap-3">
              <svg class="w-6 h-6 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
              Quy trình sản xuất thân thiện với môi trường
            </li>
          </ul>
        </div>
      </div>

      <!-- Section 2 -->
      <div class="flex flex-col md:flex-row-reverse items-center gap-12">
        <div class="w-full md:w-1/2">
          <div class="aspect-video rounded-3xl overflow-hidden bg-black relative">
            <img src="https://images.unsplash.com/photo-1551107696-a4b0c5a0d9a2?auto=format&fit=crop&q=80&w=800" alt="Performance" class="w-full h-full object-cover opacity-80" />
            <div class="absolute inset-0 flex items-center justify-center">
              <div class="text-center text-white">
                <p class="text-5xl font-black mb-2">1M+</p>
                <p class="text-sm font-semibold tracking-widest uppercase">Khách hàng tin dùng</p>
              </div>
            </div>
          </div>
        </div>
        <div class="w-full md:w-1/2 space-y-6">
          <h2 class="text-3xl font-bold text-slate-900">Cam Kết Của Chúng Tôi</h2>
          <p class="text-slate-600 leading-relaxed text-lg">
            Sứ mệnh của chúng tôi là đem đến trải nghiệm mang giày thoải mái nhất mà bạn từng có. Dù bạn là một vận động viên chuyên nghiệp hay đơn giản là người yêu thích sự xê dịch, Stride luôn đồng hành cùng bạn.
          </p>
          <Button variant="outline" size="lg" class="mt-4">
            Khám phá bộ sưu tập
          </Button>
        </div>
      </div>
    </section>
  </main>
</template>
