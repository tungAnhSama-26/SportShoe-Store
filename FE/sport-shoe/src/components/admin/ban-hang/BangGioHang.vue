<script setup>
import { Trash2 } from "lucide-vue-next";
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

const emit = defineEmits(["increase-item", "decrease-item", "update-item", "remove-item"]);
</script>

<template>
  <div class="overflow-x-auto rounded-[28px] border border-slate-100">
    <table class="min-w-full border-collapse">
      <thead class="bg-slate-100 text-left text-sm text-slate-950 font-bold">
        <tr>
          <th class="px-3 py-2 whitespace-nowrap w-[5%]">STT</th>
          <th class="px-3 py-2 whitespace-nowrap w-[15%]">Mã sản phẩm</th>
          <th class="px-3 py-2 whitespace-nowrap w-[30%]">Tên sản phẩm</th>
          <th class="px-3 py-2 whitespace-nowrap w-[10%]">Màu sắc</th>
          <th class="px-3 py-2 whitespace-nowrap w-[10%]">Kích cỡ</th>
          <th class="px-3 py-2 text-center whitespace-nowrap w-[10%]">Số lượng</th>
          <th class="px-3 py-2 whitespace-nowrap w-[15%]">Đơn giá</th>
          <th class="px-3 py-2 text-center whitespace-nowrap w-[5%]">Thao tác</th>
        </tr>
      </thead>
      <tbody class="bg-white text-sm text-slate-700">
        <tr v-for="(item, index) in cartItems" :key="item.cartItemId || item.chiTietId" class="border-t border-slate-100 text-xs font-medium">
          <td class="px-3 py-2 text-slate-900">{{ index + 1 }}</td>
          <td class="px-3 py-2 text-slate-600">{{ item.maSanPham }}</td>
          <td class="px-3 py-2">
            <div class="flex items-center gap-2">
              <div class="flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-xl bg-[linear-gradient(135deg,#fff1eb_0%,#ffe4dc_100%)] text-xs font-bold text-red-400">
                <img v-if="item.hinhAnh" :src="item.hinhAnh" alt="" class="h-full w-full object-cover" />
                <span v-else>{{ item.tenSanPham?.slice(0, 1) }}</span>
              </div>
              <div>
                <p class="font-medium text-slate-900 line-clamp-2">{{ item.tenSanPham }}</p>
              </div>
            </div>
          </td>
          <td class="px-3 py-2 text-slate-700 whitespace-nowrap">{{ item.mauSac || "--" }}</td>
          <td class="px-3 py-2 text-slate-700 whitespace-nowrap">{{ item.kichCo || "--" }}</td>
          <td class="px-3 py-2 text-center font-semibold text-slate-900">{{ item.soLuong }}</td>
          <td class="px-3 py-2 font-medium text-slate-700">
            <div>{{ dinhDangTien(item.giaBan) }}</div>
          </td>
          <td class="px-3 py-2">
            <div class="flex items-center justify-center">
              <button
                type="button"
                class="p-1.5 text-slate-400 transition hover:text-red-500 hover:bg-red-50 rounded-lg flex items-center justify-center"
                @click="emit('remove-item', item.cartItemId || item.chiTietId)"
                title="Xóa sản phẩm"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
          </td>
        </tr>
        <tr v-if="!cartItems.length">
          <td colspan="8" class="px-3 py-8 text-center text-xs text-slate-400">
            Giỏ hàng trống.
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.hide-spin-button::-webkit-inner-spin-button,
.hide-spin-button::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.hide-spin-button {
  -moz-appearance: textfield;
}
</style>
