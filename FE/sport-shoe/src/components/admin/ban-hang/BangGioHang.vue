<script setup>
import { Trash2, Pencil } from "lucide-vue-next";
import { resolveHinhAnh } from "../../../utils/resolve-image";
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
  <div class="overflow-x-auto rounded-[28px] border border-slate-100 dark:border-slate-700/60 custom-scrollbar">
    <table class="min-w-full border-collapse">
      <thead class="bg-slate-100 dark:bg-slate-800/80 text-left text-sm text-slate-950 dark:text-slate-200 font-bold">
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
      <tbody class="bg-white dark:bg-slate-800/30 text-sm text-slate-700 dark:text-slate-300">
        <tr v-for="(item, index) in cartItems" :key="item.cartItemId || item.chiTietId" class="border-t border-slate-100 dark:border-slate-700/50 text-xs font-medium hover:bg-slate-50 dark:hover:bg-slate-800/50 transition">
          <td class="px-3 py-2 text-slate-900 dark:text-slate-100">{{ index + 1 }}</td>
          <td class="px-3 py-2 text-slate-600 dark:text-slate-400">{{ item.maSanPham }}</td>
          <td class="px-3 py-2">
            <div class="flex items-center gap-2">
              <div class="relative flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-md bg-[linear-gradient(135deg,#fff1eb_0%,#ffe4dc_100%)] dark:bg-[linear-gradient(135deg,#4a1c1c_0%,#2d1111_100%)] text-xs font-bold text-red-400 dark:text-red-300">
                <img v-if="item.hinhAnh" :src="resolveHinhAnh(item.hinhAnh)" alt="" class="h-full w-full object-cover" />
                <span v-else>{{ item.tenSanPham?.slice(0, 1) }}</span>
              </div>
              <div>
                <p class="font-medium text-slate-900 dark:text-slate-100 line-clamp-2">{{ item.tenSanPham }}</p>
                <p v-if="item.oldPrice && item.oldPrice !== item.giaBan" class="mt-1 text-[11px] font-medium text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/30 px-1.5 py-0.5 rounded inline-block">
                  Giá đổi từ {{ dinhDangTien(item.oldPrice) }} thành {{ dinhDangTien(item.giaBan) }}
                </p>
              </div>
            </div>
          </td>
          <td class="px-3 py-2 text-slate-700 dark:text-slate-300 whitespace-nowrap">{{ item.mauSac || "--" }}</td>
          <td class="px-3 py-2 text-slate-700 dark:text-slate-300 whitespace-nowrap">{{ item.kichCo || "--" }}</td>
          <td class="px-3 py-2 text-center">
            <div class="flex items-center justify-center gap-1">
              <button
                type="button"
                class="w-6 h-6 flex items-center justify-center rounded border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 active:bg-slate-200 dark:active:bg-slate-600 disabled:opacity-50 text-xs font-bold select-none"
                :disabled="item.soLuong <= 1"
                @click="emit('decrease-item', item.cartItemId || item.chiTietId)"
              >
                -
              </button>
              <input
                type="number"
                :value="item.soLuong"
                @change="emit('update-item', item.cartItemId || item.chiTietId, $event.target.value)"
                min="1"
                class="w-12 h-6 rounded border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 text-center text-xs font-semibold focus:border-rose-300 dark:focus:border-rose-500 focus:outline-none hide-spin-button"
              />
              <button
                type="button"
                class="w-6 h-6 flex items-center justify-center rounded border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 active:bg-slate-200 dark:active:bg-slate-600 disabled:opacity-50 text-xs font-bold select-none"
                :disabled="soLuongConLai(item.chiTietId, item.soLuongTon) <= 0"
                @click="emit('increase-item', item.cartItemId || item.chiTietId)"
              >
                +
              </button>
            </div>
          </td>
          <td class="px-3 py-2 font-medium text-slate-700 dark:text-slate-200">
            <div class="flex flex-col">
              <span>{{ dinhDangTien(item.giaBan) }}</span>
            </div>
          </td>
          <td class="px-3 py-2">
            <div class="flex items-center justify-center gap-1">
              <button
                type="button"
                class="w-8 h-8 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 hover:bg-slate-200 dark:hover:bg-slate-700 flex items-center justify-center transition"
                @click="emit('edit-item', item)"
                title="Đổi biến thể (Size/Màu)"
              >
                <Pencil class="w-4 h-4" />
              </button>
              <button
                type="button"
                class="p-1.5 text-slate-400 dark:text-slate-500 transition hover:text-red-500 dark:hover:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-md flex items-center justify-center"
                @click="emit('remove-item', item.cartItemId || item.chiTietId)"
                title="Xóa sản phẩm"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
          </td>
        </tr>
        <tr v-if="!cartItems.length">
          <td colspan="8" class="px-3 py-8 text-center text-xs text-slate-400 dark:text-slate-500">
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
