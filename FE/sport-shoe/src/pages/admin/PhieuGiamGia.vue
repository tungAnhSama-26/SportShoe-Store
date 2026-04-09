<script setup lang="ts">
import { computed } from 'vue';
import { Calendar, Edit, Eye, Filter, Plus, RotateCcw, Search, Trash2 } from 'lucide-vue-next';
import AppPagination from '../../components/common/AppPagination.vue';
import { usePagination } from '../../composable/usePagination';

const coupons = [
  { id: 1, code: 'V01', name: 'Giảm 10%', type: 'Phần trăm', value: '10%', startDate: '2026-03-01', endDate: '2026-12-31', quantity: 100, status: 'Đang áp dụng' },
  { id: 2, code: 'V02', name: 'Giảm 20%', type: 'Phần trăm', value: '20%', startDate: '2026-03-01', endDate: '2026-12-31', quantity: 100, status: 'Đang áp dụng' },
  { id: 3, code: 'V03', name: 'Giảm 30.000', type: 'Tiền cố định', value: '30.000', startDate: '2025-03-01', endDate: '2025-12-31', quantity: 100, status: 'Hết hạn' },
  { id: 4, code: 'V04', name: 'Giảm 50.000', type: 'Tiền cố định', value: '50.000', startDate: '2026-03-01', endDate: '2026-12-31', quantity: 0, status: 'Hết số lượng' },
  { id: 5, code: 'V05', name: 'Miễn phí vận chuyển', type: 'Vận chuyển', value: '0', startDate: '2027-03-01', endDate: '2027-12-31', quantity: 100, status: 'Chưa áp dụng' },
  { id: 6, code: 'V06', name: 'Giảm 15%', type: 'Phần trăm', value: '15%', startDate: '2026-04-01', endDate: '2026-10-31', quantity: 250, status: 'Đang áp dụng' },
  { id: 7, code: 'V07', name: 'Giảm 70.000', type: 'Tiền cố định', value: '70.000', startDate: '2026-06-01', endDate: '2026-08-31', quantity: 40, status: 'Chưa áp dụng' },
  { id: 8, code: 'V08', name: 'Giảm 5%', type: 'Phần trăm', value: '5%', startDate: '2026-02-15', endDate: '2026-06-30', quantity: 500, status: 'Đang áp dụng' },
  { id: 9, code: 'V09', name: 'Giảm 100.000', type: 'Tiền cố định', value: '100.000', startDate: '2025-11-01', endDate: '2025-12-15', quantity: 20, status: 'Hết hạn' },
  { id: 10, code: 'V10', name: 'Giảm 25%', type: 'Phần trăm', value: '25%', startDate: '2026-07-01', endDate: '2026-12-31', quantity: 60, status: 'Chưa áp dụng' },
  { id: 11, code: 'V11', name: 'Giảm 40.000', type: 'Tiền cố định', value: '40.000', startDate: '2026-01-01', endDate: '2026-05-31', quantity: 90, status: 'Đang áp dụng' },
  { id: 12, code: 'V12', name: 'Freeship nội thành', type: 'Vận chuyển', value: '0', startDate: '2026-05-01', endDate: '2026-09-30', quantity: 150, status: 'Chưa áp dụng' },
];

const getStatusClass = (status: string) => {
  if (status === 'Đang áp dụng') return 'bg-[#10b981] text-white';
  if (status === 'Hết hạn' || status === 'Hết số lượng') return 'bg-[#ff3b30] text-white';
  if (status === 'Chưa áp dụng') return 'bg-[#fbbf24] text-white';
  return 'bg-gray-500 text-white';
};

const { currentPage, pageSize, totalItems, paginatedItems } = usePagination(coupons, 5);
const startIndex = computed(() => (currentPage.value - 1) * pageSize.value);
</script>

<template>
  <div class="space-y-4">
    <div class="mt-2 mb-4 flex items-center justify-between">
      <h2 class="text-[22px] font-bold text-gray-800">Quản lý phiếu giảm giá</h2>
    </div>

    <div class="relative flex w-full flex-col rounded-[20px] bg-white p-6 pb-8 shadow-sm">
      <div class="mb-6 flex items-center text-[15px] font-bold text-gray-800">
        <Filter class="mr-3 h-5 w-5 fill-gray-500 text-gray-500" />
        Bộ lọc
      </div>

      <div class="grid grid-cols-1 items-end gap-6 md:grid-cols-2 lg:grid-cols-[1fr_1fr_1fr_1fr]">
        <div class="flex flex-col gap-2.5">
          <label class="ml-1 text-[13px] font-bold text-gray-700">Tìm kiếm</label>
          <div class="relative">
            <Search class="absolute top-2.5 left-4 h-[18px] w-[18px] text-gray-400" />
            <input type="text" placeholder="Nhập mã phiếu" class="w-full rounded-full border border-gray-200 bg-white py-2 pr-5 pl-10 text-sm text-gray-700 outline-none focus:border-red-300">
          </div>
        </div>

        <div class="flex flex-col gap-2.5">
          <label class="ml-1 text-[13px] font-bold text-gray-700">Ngày bắt đầu</label>
          <div class="relative">
            <input type="text" value="28/03/2026" class="w-full rounded-full border border-gray-200 bg-white py-2 pr-10 pl-5 text-sm text-gray-700 outline-none focus:border-red-300">
            <Calendar class="absolute top-2.5 right-4 h-[18px] w-[18px] text-gray-500" />
          </div>
        </div>

        <div class="flex flex-col gap-2.5">
          <label class="ml-1 text-[13px] font-bold text-gray-700">Ngày kết thúc</label>
          <div class="relative">
            <input type="text" placeholder="dd/mm/yy" class="w-full rounded-full border border-gray-200 bg-white py-2 pr-10 pl-5 text-sm text-gray-700 outline-none focus:border-red-300">
            <Calendar class="absolute top-2.5 right-4 h-[18px] w-[18px] text-gray-500" />
          </div>
        </div>

        <div class="flex flex-col gap-2.5">
          <label class="ml-1 text-[13px] font-bold text-gray-700">Trạng thái</label>
          <select class="w-full cursor-pointer appearance-none rounded-full border border-gray-200 bg-white px-5 py-2 text-sm text-gray-700 outline-none focus:border-red-300">
            <option>Tất cả trạng thái</option>
            <option>Đang áp dụng</option>
            <option>Hết hạn</option>
            <option>Hết số lượng</option>
            <option>Chưa áp dụng</option>
          </select>
        </div>
      </div>

      <div class="mt-6 flex justify-end gap-3 border-t border-gray-100 pt-6">
        <button class="flex h-10 w-10 items-center justify-center rounded-full bg-[#ff5a5f] text-white shadow-sm shadow-red-200 transition-colors hover:bg-[#e0484d]" title="Tìm kiếm">
          <Search class="h-4 w-4" stroke-width="2.5" />
        </button>
        <button class="flex h-10 w-10 items-center justify-center rounded-full bg-[#9ca3af] text-white shadow-sm transition-colors hover:bg-gray-500" title="Làm mới">
          <RotateCcw class="h-4 w-4" stroke-width="2.5" />
        </button>
      </div>
    </div>

    <div class="rounded-[24px] border border-gray-50 border-t-2 border-t-gray-800 bg-white p-4 shadow-sm lg:p-6">
      <div class="mb-6 flex items-center justify-between">
        <h3 class="text-xl font-bold text-gray-800">Danh sách phiếu giảm giá</h3>
        <button class="flex h-10 w-10 shrink-0 items-center justify-center rounded-[12px] border border-gray-800 transition-colors hover:bg-gray-50 lg:h-12">
          <Plus class="h-5 w-5 text-gray-800 lg:h-6 lg:w-6" stroke-width="2.5" />
        </button>
      </div>

      <div class="hide-scrollbar overflow-x-auto">
        <table class="min-w-[1000px] w-full text-left">
          <thead>
            <tr class="bg-[#e2e2e2] text-[13px] font-bold text-gray-800">
              <th class="w-12 rounded-l-lg px-4 py-3.5 text-center">STT</th>
              <th class="w-20 px-4 py-3.5 text-center">Mã</th>
              <th class="px-4 py-3.5 text-center">Tên</th>
              <th class="px-4 py-3.5 text-center">Loại</th>
              <th class="px-4 py-3.5 text-center">Giá trị</th>
              <th class="px-4 py-3.5 text-center">Ngày bắt đầu</th>
              <th class="px-4 py-3.5 text-center">Ngày kết thúc</th>
              <th class="px-4 py-3.5 text-center">Số lượng</th>
              <th class="w-36 px-4 py-3.5 text-center">Trạng thái</th>
              <th class="w-32 rounded-r-lg px-4 py-3.5 text-center">Hành động</th>
            </tr>
          </thead>

          <tbody class="text-[13px] font-bold text-gray-800">
            <tr v-for="(coupon, index) in paginatedItems" :key="coupon.id" class="border-b border-gray-100 transition-colors hover:bg-gray-50/50">
              <td class="px-4 py-5 text-center">{{ startIndex + index + 1 }}</td>
              <td class="px-4 py-5 text-center">{{ coupon.code }}</td>
              <td class="px-4 py-5 text-center">{{ coupon.name }}</td>
              <td class="px-4 py-5 text-center">{{ coupon.type }}</td>
              <td class="px-4 py-5 text-center">{{ coupon.value }}</td>
              <td class="px-4 py-5 text-center">{{ coupon.startDate }}</td>
              <td class="px-4 py-5 text-center">{{ coupon.endDate }}</td>
              <td class="px-4 py-5 text-center">{{ coupon.quantity }}</td>
              <td class="px-4 py-5 text-center">
                <span class="rounded-[8px] px-3.5 py-1.5 text-[12px] font-bold tracking-wide" :class="getStatusClass(coupon.status)">
                  {{ coupon.status }}
                </span>
              </td>
              <td class="px-4 py-5 text-center">
                <div class="flex items-center justify-center gap-3">
                  <button class="rounded text-gray-600 transition-colors hover:text-black">
                    <Edit class="h-[18px] w-[18px]" stroke-width="2.5" />
                  </button>
                  <button class="rounded text-gray-600 transition-colors hover:text-black">
                    <Eye class="h-[18px] w-[18px]" stroke-width="2.5" />
                  </button>
                  <button class="rounded text-gray-600 transition-colors hover:text-red-600">
                    <Trash2 class="h-[18px] w-[18px]" stroke-width="2.5" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <AppPagination v-model="currentPage" class="mt-6" :total-items="totalItems" :page-size="pageSize" />
      </div>
    </div>
  </div>
</template>
