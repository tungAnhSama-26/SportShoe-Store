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
  <div class="overflow-hidden rounded-[28px] border border-slate-100">
    <table class="min-w-full border-collapse">
      <thead class="bg-slate-100 text-left text-sm text-slate-600">
        <tr>
          <th class="px-5 py-4 font-semibold">STT</th>
          <th class="px-5 py-4 font-semibold">Mã sản phẩm</th>
          <th class="px-5 py-4 font-semibold">Tên sản phẩm</th>
          <th class="px-5 py-4 font-semibold">Đơn giá</th>
          <th class="px-5 py-4 font-semibold">Số lượng</th>
        </tr>
      </thead>
      <tbody class="bg-white text-sm text-slate-700">
        <tr v-for="(item, index) in cartItems" :key="item.chiTietId" class="border-t border-slate-100">
          <td class="px-5 py-4 font-semibold text-slate-900">{{ index + 1 }}</td>
          <td class="px-5 py-4 font-semibold text-slate-600">{{ item.maSanPham }}</td>
          <td class="px-5 py-4">
            <p class="font-semibold text-slate-900">{{ item.tenSanPham }}</p>
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
            Chọn sản phẩm từ ô tìm kiếm để đưa vào hóa đơn.
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
