<template>
  <main class="min-h-screen bg-slate-50 pb-20">
    <section class="bg-black px-6 py-16 text-white lg:px-10">
      <div class="mx-auto max-w-6xl">
        <p class="mb-3 text-sm font-semibold uppercase tracking-[0.22em] text-red-400">Hỗ trợ mua hàng</p>
        <h1 class="text-4xl font-bold md:text-5xl">Thông tin giao hàng</h1>
        <p class="mt-4 max-w-2xl leading-7 text-slate-300">Những thông tin cần biết từ lúc xác nhận đơn đến khi nhận giày.</p>
      </div>
    </section>
    <section class="mx-auto max-w-6xl px-6 py-12">
      <div class="grid gap-6 md:grid-cols-3">
        <article class="rounded-2xl bg-white p-6 shadow-sm"><span class="text-sm font-bold text-primary">01</span><h2 class="mt-3 text-lg font-bold">Xác nhận đơn</h2><p class="mt-3 text-sm leading-7 text-slate-600">Đơn hàng được kiểm tra thông tin, tồn kho và phương thức thanh toán trước khi đóng gói.</p></article>
        <article class="rounded-2xl bg-white p-6 shadow-sm"><span class="text-sm font-bold text-primary">02</span><h2 class="mt-3 text-lg font-bold">Bàn giao vận chuyển</h2><p class="mt-3 text-sm leading-7 text-slate-600">Khi đơn được bàn giao, trạng thái và mã vận đơn sẽ được cập nhật để khách hàng theo dõi.</p></article>
        <article class="rounded-2xl bg-white p-6 shadow-sm"><span class="text-sm font-bold text-primary">03</span><h2 class="mt-3 text-lg font-bold">Nhận hàng</h2><p class="mt-3 text-sm leading-7 text-slate-600">Kiểm tra tình trạng kiện hàng và sản phẩm trước khi xác nhận đã nhận.</p></article>
      </div>
      <div class="mt-8 grid gap-6 lg:grid-cols-2">
        <article class="rounded-2xl bg-white p-7 shadow-sm"><h2 class="text-xl font-bold">Thời gian dự kiến</h2><ul class="mt-5 space-y-3 text-sm leading-7 text-slate-600"><li><strong>Nội thành:</strong> khoảng 1–3 ngày làm việc.</li><li><strong>Ngoại thành và tỉnh khác:</strong> khoảng 3–7 ngày làm việc.</li><li>Thời gian có thể thay đổi vào ngày lễ hoặc khi đơn vị vận chuyển quá tải.</li></ul></article>
        <article class="rounded-2xl bg-white p-7 shadow-sm"><h2 class="text-xl font-bold">Phí và theo dõi giao hàng</h2><ul class="mt-5 space-y-3 text-sm leading-7 text-slate-600"><li>Phí vận chuyển được hiển thị tại bước thanh toán theo địa chỉ nhận hàng.</li><li>Tra cứu bằng mã đơn tại trang Theo dõi đơn hàng.</li><li>Khi cần thay đổi địa chỉ, hãy liên hệ trước lúc đơn được bàn giao vận chuyển.</li></ul><RouterLink :to="{ name: 'tra-cuu-don' }" class="mt-5 inline-flex rounded-xl bg-primary px-5 py-3 text-sm font-bold text-white">Theo dõi đơn hàng</RouterLink></article>
      </div>
    </section>
  </main>
</template>
