<script setup lang="ts">
import { Calendar, Filter, Plus, Edit, Eye, Trash2, Search, RotateCcw } from 'lucide-vue-next';

const coupons = [
  { id: 1, code: 'V01', name: 'Giảm 10%', type: 'Phần trăm', value: '10%', startDate: '2026-03-01', endDate: '2026-12-31', quantity: 100, status: 'Đang áp dụng' },
  { id: 2, code: 'V02', name: 'Giảm 20%', type: 'Phần trăm', value: '20%', startDate: '2026-03-01', endDate: '2026-12-31', quantity: 100, status: 'Đang áp dụng' },
  { id: 3, code: 'V03', name: 'Giảm 30.000', type: 'Tiền cố định', value: '30.000', startDate: '2025-03-01', endDate: '2025-12-31', quantity: 100, status: 'Hết hạn' },
  { id: 4, code: 'V04', name: 'Giảm 50.000', type: 'Tiền cố định', value: '50.000', startDate: '2026-03-01', endDate: '2026-12-31', quantity: 0, status: 'Hết số lượng' },
  { id: 5, code: 'V05', name: 'Miễn phí vận chuyển', type: 'Vận chuyển', value: '0', startDate: '2027-03-01', endDate: '2027-12-31', quantity: 100, status: 'Chưa áp dụng' },
]

const getStatusClass = (status: string) => {
  if (status === 'Đang áp dụng') return 'bg-[#10b981] text-white';
  if (status === 'Hết hạn' || status === 'Hết số lượng') return 'bg-[#ff3b30] text-white';
  if (status === 'Chưa áp dụng') return 'bg-[#fbbf24] text-white';
  return 'bg-gray-500 text-white';
}
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between mb-4 mt-2">
      <h2 class="text-[22px] font-bold text-gray-800">Quản lí phiếu giảm giá</h2>
    </div>

    <!-- Filter Block -->
    <div class="bg-white p-6 rounded-[20px] shadow-sm flex flex-col relative w-full pb-8">
      <div class="flex items-center text-gray-800 font-bold mb-6 text-[15px]">
        <Filter class="w-5 h-5 mr-3 text-gray-500 fill-gray-500"/>
        Bộ Lọc
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-[1fr_1fr_1fr_1fr] gap-6 items-end">
        <div class="flex flex-col gap-2.5">
          <label class="text-[13px] text-gray-700 font-bold ml-1">Tìm kiếm</label>
          <div class="relative">
            <Search class="w-[18px] h-[18px] text-gray-400 absolute left-4 top-2.5" />
            <input type="text" placeholder="Nhập mã phiếu" class="pl-10 pr-5 py-2 bg-white border border-gray-200 rounded-full text-sm text-gray-700 w-full outline-none focus:border-red-300">
          </div>
        </div>
        
        <div class="flex flex-col gap-2.5">
          <label class="text-[13px] text-gray-700 font-bold ml-1">Ngày Bắt Đầu</label>
          <div class="relative">
            <input type="text" value="28/03/2026" class="pl-5 pr-10 py-2 bg-white border border-gray-200 rounded-full text-sm text-gray-700 w-full outline-none focus:border-red-300">
            <Calendar class="w-[18px] h-[18px] text-gray-500 absolute right-4 top-2.5" />
          </div>
        </div>

        <div class="flex flex-col gap-2.5">
          <label class="text-[13px] text-gray-700 font-bold ml-1">Ngày Kết Thúc</label>
          <div class="relative">
            <input type="text" placeholder="dd/mm/yy" class="pl-5 pr-10 py-2 bg-white border border-gray-200 rounded-full text-sm text-gray-700 w-full outline-none focus:border-red-300">
            <Calendar class="w-[18px] h-[18px] text-gray-500 absolute right-4 top-2.5" />
          </div>
        </div>

        <div class="flex flex-col gap-2.5">
          <label class="text-[13px] text-gray-700 font-bold ml-1">Trạng thái</label>
          <select class="px-5 py-2 bg-white border border-gray-200 rounded-full text-sm text-gray-700 w-full outline-none appearance-none cursor-pointer focus:border-red-300">
            <option>Tất cả trạng thái</option>
            <option>Đang áp dụng</option>
            <option>Hết hạn</option>
            <option>Hết số lượng</option>
            <option>Chưa áp dụng</option>
          </select>
        </div>
      </div>
      
      <div class="flex justify-end gap-3 mt-6 border-t border-gray-100 pt-6">
        <button class="bg-[#ff5a5f] hover:bg-[#e0484d] text-white w-10 h-10 rounded-full flex items-center justify-center transition-colors shadow-sm shadow-red-200" title="Tìm kiếm">
          <Search class="w-4 h-4" stroke-width="2.5" />
        </button>
        <button class="bg-[#9ca3af] hover:bg-gray-500 text-white w-10 h-10 rounded-full flex items-center justify-center transition-colors shadow-sm" title="Làm mới">
          <RotateCcw class="w-4 h-4" stroke-width="2.5" />
        </button>
      </div>
    </div>


    <!-- Table Block -->
    <div class="bg-white p-4 lg:p-6 rounded-[24px] shadow-sm border border-gray-50 border-t-2 border-t-gray-800">
      <div class="flex justify-between items-center mb-6">
        <h3 class="text-xl font-bold text-gray-800">Danh sách phiếu giảm giá</h3>
        <button class="w-10 h-10 lg:w-12 border border-gray-800 rounded-[12px] flex items-center justify-center hover:bg-gray-50 transition-colors shrink-0">
          <Plus class="w-5 h-5 lg:w-6 lg:h-6 text-gray-800" stroke-width="2.5" />
        </button>
      </div>

      <div class="overflow-x-auto hide-scrollbar">
        <table class="w-full text-left min-w-[1000px]">
          <thead>
          <tr class="bg-[#e2e2e2] text-gray-800 text-[13px] font-bold">
            <th class="py-3.5 px-4 text-center w-12 first:rounded-l-lg">STT</th>
            <th class="py-3.5 px-4 text-center w-20">Mã</th>
            <th class="py-3.5 px-4 text-center">Tên</th>
            <th class="py-3.5 px-4 text-center">Loại</th>
            <th class="py-3.5 px-4 text-center">Giá trị</th>
            <th class="py-3.5 px-4 text-center">Ngày bắt đầu</th>
            <th class="py-3.5 px-4 text-center">Ngày kết thúc</th>
            <th class="py-3.5 px-4 text-center">Số lượng</th>
            <th class="py-3.5 px-4 text-center w-36">Trạng thái</th>
            <th class="py-3.5 px-4 text-center w-32 last:rounded-r-lg">Hành động</th>
          </tr>
        </thead>
        <tbody class="text-[13px] font-bold text-gray-800">
          <tr v-for="(coupon, index) in coupons" :key="coupon.id" class="border-b border-gray-100 hover:bg-gray-50/50 transition-colors">
            <td class="py-5 px-4 text-center">{{ index + 1 }}</td>
            <td class="py-5 px-4 text-center">{{ coupon.code }}</td>
            <td class="py-5 px-4 text-center">{{ coupon.name }}</td>
            <td class="py-5 px-4 text-center">{{ coupon.type }}</td>
            <td class="py-5 px-4 text-center">{{ coupon.value }}</td>
            <td class="py-5 px-4 text-center">{{ coupon.startDate }}</td>
            <td class="py-5 px-4 text-center">{{ coupon.endDate }}</td>
            <td class="py-5 px-4 text-center">{{ coupon.quantity }}</td>
            <td class="py-5 px-4 text-center">
              <span class="px-3.5 py-1.5 rounded-[8px] text-[12px] font-bold tracking-wide" :class="getStatusClass(coupon.status)">
                {{ coupon.status }}
              </span>
            </td>
            <td class="py-5 px-4 text-center">
              <div class="flex items-center justify-center gap-3">
                <button class="text-gray-600 hover:text-black transition-colors rounded">
                  <Edit class="w-[18px] h-[18px]" stroke-width="2.5" />
                </button>
                <button class="text-gray-600 hover:text-black transition-colors rounded">
                  <Eye class="w-[18px] h-[18px]" stroke-width="2.5" />
                </button>
                <button class="text-gray-600 hover:text-red-600 transition-colors rounded">
                  <Trash2 class="w-[18px] h-[18px]" stroke-width="2.5" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- Pagination -->
      <div class="flex items-center justify-between mt-6 px-2">
        <span class="text-[13px] text-gray-500 font-medium">Đang hiển thị 1 tới 5 trong số 50 kết quả</span>
        <div class="flex items-center gap-1.5">
          <button class="w-8 h-8 flex items-center justify-center rounded-[8px] border border-gray-200 text-gray-400 hover:bg-gray-50 hover:text-gray-600 transition-colors">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6"/></svg>
          </button>
          <button class="w-8 h-8 flex items-center justify-center rounded-[8px] text-white bg-[#ff5a5f] font-bold text-[13px] shadow-sm shadow-red-200 border border-transparent">1</button>
          <button class="w-8 h-8 flex items-center justify-center rounded-[8px] text-gray-600 hover:bg-gray-50 hover:text-gray-900 border border-transparent font-semibold text-[13px]">2</button>
          <button class="w-8 h-8 flex items-center justify-center rounded-[8px] text-gray-600 hover:bg-gray-50 hover:text-gray-900 border border-transparent font-semibold text-[13px]">3</button>
          <span class="text-gray-400 mx-0.5">...</span>
          <button class="w-8 h-8 flex items-center justify-center rounded-[8px] text-gray-600 hover:bg-gray-50 hover:text-gray-900 border border-transparent font-semibold text-[13px]">10</button>
          <button class="w-8 h-8 flex items-center justify-center rounded-[8px] border border-gray-200 text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-colors">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>
          </button>
        </div>
      </div>
      </div>
    </div>
  </div>
</template>
