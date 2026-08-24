<script setup>
import { Trash2, Pencil } from "lucide-vue-next";
import { resolveHinhAnh } from "../../../utils/resolve-image";
import { dinhDangTien as formatTienTe } from "../../../features/ban-hang-tai-quay/TienTe";

const props = defineProps({
  cartItems: {
    type: Array,
    default: () => []
  },
  dinhDangTien: {
    type: Function,
    default: null
  },
  soLuongConLai: {
    type: Function,
    required: true
  },
  isOutdatedPrice: {
    type: Function,
    default: null
  }
});

const emit = defineEmits(["increase-item", "decrease-item", "update-item", "remove-item", "edit-item"]);

const formatTien = (val) => (typeof props.dinhDangTien === "function" ? props.dinhDangTien(val) : formatTienTe(val));

function checkOutdated(item) {
  if (!item) return false;
  if (item.isOutdatedPrice) return true;
  if (item.currentCatalogPrice && Number(item.currentCatalogPrice) !== Number(item.giaBan)) return true;
  if (props.isOutdatedPrice && props.isOutdatedPrice(item)) return true;
  const sameVariantItems = props.cartItems.filter(
    (it) => Number(it.chiTietId) === Number(item?.chiTietId)
  );
  if (sameVariantItems.length > 1) {
    const latestItem = sameVariantItems[sameVariantItems.length - 1];
    if (latestItem && Number(latestItem.giaBan) !== Number(item?.giaBan)) {
      return true;
    }
  }
  return false;
}

function isDiscounted(item) {
  return Number(item?.giaBan || 0) < Number(item?.giaGoc || 0);
}

function formatDiscountPercent(item) {
  const giaGoc = Number(item?.giaGoc || 0);
  const giaBan = Number(item?.giaBan || 0);
  if (giaGoc <= 0 || giaBan >= giaGoc) return "";
  const pct = Math.round(((giaGoc - giaBan) / giaGoc) * 100);
  return `-${pct}%`;
}

function getPriceChangeText(item) {
  if (!item || item.isOutdatedPrice) return null;

  // 1. Dòng sản phẩm mới có oldPrice khác với giaBan hiện tại
  if (item.oldPrice && Number(item.oldPrice) !== Number(item.giaBan)) {
    const oldP = Number(item.oldPrice);
    const newP = Number(item.giaBan);
    const direction = oldP < newP ? 'lên' : 'xuống';
    return `Giá sản phẩm đã được cập nhật từ ${formatTien(oldP)} ${direction} ${formatTien(newP)}.`;
  }

  // 2. Khi cùng biến thể có dòng cũ trong giỏ, dòng mới này sẽ hiển thị thông báo
  const sameVariantItems = props.cartItems.filter(
    (it) => Number(it.chiTietId) === Number(item?.chiTietId)
  );
  if (sameVariantItems.length > 1) {
    const olderItem = sameVariantItems.find(
      it => Number(it.giaBan) !== Number(item?.giaBan) && (it.isOutdatedPrice || sameVariantItems.indexOf(it) < sameVariantItems.indexOf(item))
    );
    if (olderItem) {
      const oldP = Number(olderItem.giaBan);
      const newP = Number(item.giaBan);
      const direction = oldP < newP ? 'lên' : 'xuống';
      return `Giá sản phẩm đã được cập nhật từ ${formatTien(oldP)} ${direction} ${formatTien(newP)}.`;
    }
  }

  return null;
}

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
                <span v-if="isDiscounted(item)" class="absolute top-0 left-0 origin-top-left scale-[0.6] rounded-br-md bg-rose-500 px-1 py-0.5 text-[10px] leading-none font-bold text-white shadow-sm z-10">
                  {{ formatDiscountPercent(item) }}
                </span>
              </div>
              <div>
                <p class="font-medium text-slate-900 dark:text-slate-100 line-clamp-2">{{ item.tenSanPham }}</p>
                <p v-if="getPriceChangeText(item)" class="mt-1 text-[11px] font-medium text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/30 px-1.5 py-0.5 rounded inline-block">
                  {{ getPriceChangeText(item) }}
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
                :readonly="checkOutdated(item)"
                :title="checkOutdated(item) ? 'Sản phẩm đã đổi giá, không thể sửa tăng số lượng ở giá cũ' : ''"
                @change="emit('update-item', item.cartItemId || item.chiTietId, $event.target.value)"
                min="1"
                :max="checkOutdated(item) ? item.soLuong : undefined"
                class="w-12 h-6 rounded border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 text-center text-xs font-semibold focus:border-rose-300 dark:focus:border-rose-500 focus:outline-none hide-spin-button"
              />
              <button
                type="button"
                class="w-6 h-6 flex items-center justify-center rounded border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 active:bg-slate-200 dark:active:bg-slate-600 disabled:opacity-40 disabled:cursor-not-allowed text-xs font-bold select-none"
                :disabled="checkOutdated(item) || soLuongConLai(item.chiTietId, item.soLuongTon) <= 0"
                :title="checkOutdated(item) ? 'Sản phẩm đã đổi giá, không thể tăng thêm số lượng ở giá cũ' : ''"
                @click="emit('increase-item', item.cartItemId || item.chiTietId)"
              >
                +
              </button>
            </div>
          </td>
          <td class="px-3 py-2 font-medium">
            <div class="flex flex-col items-start">
              <span class="text-slate-900 dark:text-slate-100">{{ dinhDangTien(item.giaBan) }}</span>
              <div v-if="isDiscounted(item)" class="flex items-center gap-1 mt-0.5">
                <span class="text-[11px] text-slate-400 line-through">{{ dinhDangTien(item.giaGoc) }}</span>
              </div>
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
