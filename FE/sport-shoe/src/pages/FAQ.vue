<script setup>
import { ref } from "vue";

const dangMo = ref(0);
const cauHoi = [
  { hoi: "Tôi có cần tài khoản để mua hàng không?", dap: "Bạn có thể xem sản phẩm công khai. Tài khoản giúp lưu thông tin và quản lý lịch sử đơn hàng thuận tiện hơn." },
  { hoi: "Làm sao kiểm tra trạng thái đơn hàng?", dap: "Bạn có thể mở trang Theo dõi đơn hàng và nhập thông tin tra cứu, hoặc xem mục Đơn hàng của tôi sau khi đăng nhập." },
  { hoi: "SportShoe hỗ trợ những phương thức thanh toán nào?", dap: "Các phương thức khả dụng được hiển thị tại bước thanh toán, gồm thanh toán khi nhận hàng và phương thức điện tử được hệ thống hỗ trợ." },
  { hoi: "Phí vận chuyển được tính như thế nào?", dap: "Phí được xác định theo địa chỉ nhận hàng và hiển thị trước khi bạn xác nhận đặt hàng." },
  { hoi: "Làm thế nào để chọn đúng size giày?", dap: "Hãy đo chiều dài cả hai bàn chân vào cuối ngày và đối chiếu bảng tại trang Hướng dẫn chọn size." },
  { hoi: "Mã giảm giá được nhập ở đâu?", dap: "Mã giảm giá hợp lệ được chọn hoặc nhập trong luồng thanh toán trước khi xác nhận đơn hàng." },
  { hoi: "Tôi có thể thay đổi thông tin đơn hàng không?", dap: "Hãy liên hệ SportShoe sớm nhất có thể. Đơn đã bàn giao cho đơn vị vận chuyển có thể không sửa được địa chỉ hoặc sản phẩm." },
];
</script>

<template>
  <main class="min-h-screen bg-slate-50 pb-20">
    <section class="bg-black px-6 py-16 text-white lg:px-10"><div class="mx-auto max-w-4xl text-center"><p class="mb-3 text-sm font-semibold uppercase tracking-[0.22em] text-red-400">Trung tâm trợ giúp</p><h1 class="text-4xl font-bold md:text-5xl">Câu hỏi thường gặp</h1><p class="mx-auto mt-4 max-w-2xl leading-7 text-slate-300">Thông tin nhanh về mua hàng, thanh toán và vận chuyển tại SportShoe.</p></div></section>
    <section class="mx-auto max-w-4xl space-y-4 px-6 py-12">
      <article v-for="(muc, index) in cauHoi" :key="muc.hoi" class="overflow-hidden rounded-2xl bg-white shadow-sm">
        <button type="button" class="flex w-full items-center justify-between gap-5 px-6 py-5 text-left font-bold text-slate-900" :aria-expanded="dangMo === index" @click="dangMo = dangMo === index ? -1 : index"><span>{{ muc.hoi }}</span><span class="text-2xl font-light text-primary">{{ dangMo === index ? "−" : "+" }}</span></button>
        <div v-if="dangMo === index" class="border-t border-slate-100 px-6 py-5 text-sm leading-7 text-slate-600">{{ muc.dap }}</div>
      </article>
      <div class="mt-8 rounded-2xl bg-slate-900 p-7 text-white"><h2 class="text-xl font-bold">Bạn vẫn cần hỗ trợ?</h2><p class="mt-2 text-sm text-slate-300">Gửi thông tin cho SportShoe qua trang Liên hệ.</p><RouterLink :to="{ name: 'lien-he' }" class="mt-5 inline-flex rounded-xl bg-primary px-5 py-3 text-sm font-bold">Liên hệ ngay</RouterLink></div>
    </section>
  </main>
</template>
