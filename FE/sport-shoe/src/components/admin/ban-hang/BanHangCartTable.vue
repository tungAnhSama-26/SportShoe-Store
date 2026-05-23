<script setup>
defineProps({
  cartItems: {
    type: Array,
    default: () => []
  },
  dinhDangTien: {
    type: Function,
    required: true
  },
  soLuongConLai: {
    type: Function,
    required: true
  }
});

const emit = defineEmits(["increase-item", "decrease-item"]);
</script>

<template>
  <div class="overflow-x-auto rounded-[28px] border border-slate-100">
    <table class="min-w-full border-collapse">
      <thead class="bg-slate-100 text-left text-sm text-slate-950 font-bold">
        <tr>
          <th class="px-5 py-4">STT</th>
          <th class="px-5 py-4">Mã sản phẩm</th>
          <th class="px-5 py-4">Tên sản phẩm</th>
          <th class="px-5 py-4">Đơn giá</th>
          <th class="px-5 py-4">Số lượng</th>
        </tr>
      </thead>
      <tbody class="bg-white text-sm text-slate-700">
        <tr v-for="(item, index) in cartItems" :key="item.chiTietId" class="border-t border-slate-100">
          <td class="px-5 py-4 font-semibold text-slate-900">{{ index + 1 }}</td>
          <td class="px-5 py-4 font-semibold text-slate-600">{{ item.maSanPham }}</td>
          <td class="px-5 py-4">
            <div class="flex items-center gap-3">
              <div class="flex h-14 w-14 shrink-0 items-center justify-center overflow-hidden rounded-2xl bg-[linear-gradient(135deg,#fff1eb_0%,#ffe4dc_100%)] text-sm font-bold text-red-400">
                <img v-if="item.hinhAnh" :src="item.hinhAnh" alt="" class="h-full w-full object-cover" />
                <span v-else>{{ item.tenSanPham?.slice(0, 1) }}</span>
              </div>

              <div class="min-w-0">
                <p class="font-semibold text-slate-900">{{ item.tenSanPham }}</p>
                <p class="mt-1 text-xs text-slate-500">
                  {{ item.mauSac || "--" }} / {{ item.kichCo || "--" }}
                </p>
                <p v-if="item.sku" class="mt-1 text-xs text-slate-400">SKU: {{ item.sku }}</p>
              </div>
            </div>
          </td>
          <td class="px-5 py-4 font-semibold text-slate-700">{{ dinhDangTien(item.giaBan) }}</td>
          <td class="px-5 py-4">
            <div class="inline-flex items-center rounded-full border border-slate-200 bg-slate-50">
              <button
                type="button"
                class="px-3 py-1 text-base font-bold text-slate-500 transition hover:text-red-500"
                @click="emit('decrease-item', item.chiTietId)"
              >
                -
              </button>
              <span class="min-w-10 px-2 text-center font-semibold text-slate-900">{{ item.soLuong }}</span>
              <button
                type="button"
                class="px-3 py-1 text-base font-bold transition"
                :class="
                  soLuongConLai(item.chiTietId, item.soLuongTon) <= 0
                    ? 'cursor-not-allowed text-slate-300'
                    : 'text-slate-500 hover:text-red-500'
                "
                @click="emit('increase-item', item.chiTietId)"
              >
                +
              </button>
            </div>
            <p class="mt-2 text-xs text-slate-400">Tồn còn lại: {{ soLuongConLai(item.chiTietId, item.soLuongTon) }}</p>
          </td>
        </tr>
        <tr v-if="!cartItems.length">
          <td colspan="5" class="px-5 py-14 text-center text-sm text-slate-400">
            Giỏ hàng trống.
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
