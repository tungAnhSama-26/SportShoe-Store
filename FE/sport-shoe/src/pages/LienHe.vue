<script setup>
import { ref } from "vue";

const daGui = ref(false);

function guiLienHe() {
  daGui.value = true;
}
</script>

<template>
  <main class="min-h-screen bg-slate-50 pb-20">
    <section class="bg-black px-6 py-16 text-white lg:px-10">
      <div class="mx-auto max-w-6xl">
        <p class="mb-3 text-sm font-semibold uppercase tracking-[0.22em] text-red-400">SportShoe</p>
        <h1 class="text-4xl font-bold md:text-5xl">Liên hệ với chúng tôi</h1>
        <p class="mt-4 max-w-2xl leading-7 text-slate-300">SportShoe luôn sẵn sàng giải đáp thắc mắc về sản phẩm và đơn hàng của bạn.</p>
      </div>
    </section>

    <section class="mx-auto grid max-w-6xl gap-8 px-6 py-12 lg:grid-cols-[0.8fr_1.2fr]">
      <div class="space-y-5">
        <article class="rounded-2xl bg-white p-6 shadow-sm">
          <h2 class="font-bold text-slate-900">Thông tin cửa hàng</h2>
          <dl class="mt-5 space-y-4 text-sm leading-6 text-slate-600">
            <div><dt class="font-semibold text-slate-900">Địa chỉ</dt><dd>SportShoe Store, Việt Nam</dd></div>
            <div><dt class="font-semibold text-slate-900">Điện thoại</dt><dd>1900 0000</dd></div>
            <div><dt class="font-semibold text-slate-900">Email</dt><dd>support@sportshoe.vn</dd></div>
            <div><dt class="font-semibold text-slate-900">Giờ làm việc</dt><dd>08:00–22:00, từ thứ Hai đến Chủ nhật</dd></div>
          </dl>
        </article>
        <p class="rounded-2xl border border-amber-200 bg-amber-50 p-5 text-sm leading-6 text-amber-800">Với vấn đề về đơn hàng, vui lòng ghi kèm mã đơn để nhân viên hỗ trợ nhanh hơn.</p>
      </div>

      <form class="rounded-2xl bg-white p-6 shadow-sm md:p-8" @submit.prevent="guiLienHe">
        <h2 class="text-xl font-bold text-slate-900">Gửi lời nhắn</h2>
        <div class="mt-6 grid gap-5 sm:grid-cols-2">
          <label class="text-sm font-semibold text-slate-700">Họ và tên<input required type="text" class="mt-2 w-full rounded-xl border-slate-200 focus:border-primary focus:ring-primary/30" /></label>
          <label class="text-sm font-semibold text-slate-700">Số điện thoại<input required type="tel" class="mt-2 w-full rounded-xl border-slate-200 focus:border-primary focus:ring-primary/30" /></label>
          <label class="text-sm font-semibold text-slate-700 sm:col-span-2">Email<input required type="email" class="mt-2 w-full rounded-xl border-slate-200 focus:border-primary focus:ring-primary/30" /></label>
          <label class="text-sm font-semibold text-slate-700 sm:col-span-2">Nội dung<textarea required rows="6" class="mt-2 w-full rounded-xl border-slate-200 focus:border-primary focus:ring-primary/30"></textarea></label>
        </div>
        <button type="submit" class="mt-5 rounded-xl bg-primary px-6 py-3 text-sm font-bold text-white transition hover:bg-primary-hover">Gửi liên hệ</button>
        <p v-if="daGui" class="mt-4 text-sm text-emerald-600">Đã ghi nhận nội dung trên giao diện. Vui lòng liên hệ hotline nếu bạn cần hỗ trợ ngay.</p>
      </form>
    </section>
  </main>
</template>
