<script setup lang="ts">
import { ref } from 'vue';
import { Calendar, TrendingUp, ShoppingCart, Users } from 'lucide-vue-next';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  ArcElement
} from 'chart.js'
import { Line, Pie } from 'vue-chartjs'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  ArcElement
)

const lineChartData = {
  labels: [],
  datasets: [
    {
      label: 'Doanh thu',
      backgroundColor: '#f87171',
      borderColor: '#8b5cf6',
      data: [],
      tension: 0.4,
      pointBackgroundColor: '#ffffff',
      pointBorderColor: '#8b5cf6',
      pointBorderWidth: 2,
    }
  ]
}

const lineChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false }
  },
  scales: {
    y: {
      beginAtZero: true,
      grid: {
        color: '#f3f4f6',           
      },
      ticks: {
         display: false
      }
    },
    x: {
      grid: {
        display: false
      }
    }
  }
}

const pieChartData = {
  labels: [],
  datasets: [
    {
      backgroundColor: ['#6366f1', '#a855f7', '#fb923c', '#eab308', '#22d3ee', '#3b82f6', '#ec4899', '#14b8a6', '#f97316', '#84cc16', '#0ea5e9', '#64748b'],
      data: [],
      borderWidth: 0
    }
  ]
}

const pieChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'right' as const,
      labels: {
        usePointStyle: true,
        padding: 20,
        font: {
          size: 11
        }
      }
    }
  }
}

const products = []

</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between mb-4 mt-2">
      <h2 class="text-[22px] font-bold text-gray-800">Thống kê</h2>
    </div>

    <!-- Filters -->
    <div class="bg-white p-4 rounded-[20px] grid grid-cols-1 md:grid-cols-2 lg:flex lg:items-end gap-6 shadow-sm">
      <div class="flex flex-col gap-2">
        <label class="text-xs text-gray-500 font-medium ml-1">Từ ngày</label>
        <div class="relative">
          <input type="text" value="31/03/2026" class="pl-4 pr-10 py-2 bg-gray-50 border border-gray-100 rounded-[12px] text-sm text-gray-700 w-full lg:w-40 outline-none focus:border-red-300">
          <Calendar class="w-4 h-4 text-gray-800 absolute right-4 top-2.5" />
        </div>
      </div>
      <div class="flex flex-col gap-2">
        <label class="text-xs text-gray-500 font-medium ml-1">Đến ngày</label>
        <div class="relative">
          <input type="text" value="1/04/2026" class="pl-4 pr-10 py-2 bg-gray-50 border border-gray-100 rounded-[12px] text-sm text-gray-700 w-full lg:w-40 outline-none focus:border-red-300">
          <Calendar class="w-4 h-4 text-gray-800 absolute right-4 top-2.5" />
        </div>
      </div>
      <div class="flex flex-col gap-2 md:col-span-2 lg:col-span-1">
        <label class="text-xs text-gray-500 font-medium ml-1">Thương hiệu</label>
        <select class="px-4 py-2 bg-gray-50 border border-gray-100 rounded-[12px] text-sm text-gray-700 w-full lg:w-56 outline-none appearance-none cursor-pointer focus:border-red-300">
          <option>Tất cả thương hiệu &or;</option>
          <option>Nike</option>
          <option>Adidas</option>
        </select>
      </div>
      <div class="flex gap-3 md:col-span-2 lg:col-span-1 lg:ml-4">
        <button class="bg-[#ff5a5f] hover:bg-[#e0484d] text-white flex-1 lg:flex-none lg:px-8 py-2 rounded-[12px] text-sm font-medium transition-colors h-[38px] shadow-sm shadow-red-200">
          Lọc
        </button>
        <button class="bg-[#9ca3af] hover:bg-gray-500 text-white flex-1 lg:flex-none lg:px-8 py-2 rounded-[12px] text-sm font-medium transition-colors h-[38px] shadow-sm">
          Reset
        </button>
      </div>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <div class="bg-white p-5 rounded-[20px] flex items-center gap-4 shadow-sm border border-gray-50/50">
        <div class="w-[52px] h-[52px] rounded-2xl bg-gray-50 flex items-center justify-center shrink-0">
          <TrendingUp class="w-6 h-6 text-gray-800" />
        </div>
        <div>
          <div class="text-lg font-bold text-gray-800 tracking-tight">320.670.000đ</div>
          <div class="text-[13px] text-gray-500 font-medium mt-0.5">Tổng doanh thu</div>
        </div>
      </div>
      <div class="bg-white p-5 rounded-[20px] flex items-center gap-4 shadow-sm border border-gray-50/50">
        <div class="w-[52px] h-[52px] rounded-2xl bg-gray-50 flex items-center justify-center shrink-0">
          <ShoppingCart class="w-6 h-6 text-gray-800" />
        </div>
        <div>
          <div class="text-lg font-bold text-gray-800 tracking-tight">1.890</div>
          <div class="text-[13px] text-gray-500 font-medium mt-0.5">Tổng đơn hàng</div>
        </div>
      </div>
      <div class="bg-white p-5 rounded-[20px] flex items-center gap-4 shadow-sm border border-gray-50/50">
        <div class="w-[52px] h-[52px] rounded-2xl bg-gray-50 flex items-center justify-center shrink-0">
          <svg class="w-6 h-6 text-gray-800" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.38 3.46 16 2a4 4 0 0 1-8 0L3.62 3.46a2 2 0 0 0-1.34 2.23l.58 3.47a1 1 0 0 0 .99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 0 0 2-2V10h2.15a1 1 0 0 0 .99-.84l.58-3.47a2 2 0 0 0-1.34-2.23z"/></svg>
        </div>
        <div>
          <div class="text-lg font-bold text-gray-800 tracking-tight">3.720</div>
          <div class="text-[13px] text-gray-500 font-medium mt-0.5">Sản phẩm đã bán</div>
        </div>
      </div>
      <div class="bg-white p-5 rounded-[20px] flex items-center gap-4 shadow-sm border border-gray-50/50">
        <div class="w-[52px] h-[52px] rounded-2xl bg-gray-50 flex items-center justify-center shrink-0">
          <Users class="w-6 h-6 text-gray-800" />
        </div>
        <div>
          <div class="text-lg font-bold text-gray-800 tracking-tight">500</div>
          <div class="text-[13px] text-gray-500 font-medium mt-0.5">Khách hàng mới</div>
        </div>
      </div>
    </div>

    <!-- Charts -->
    <div class="grid grid-cols-1 lg:grid-cols-[1.5fr_1fr] gap-6">
      <div class="bg-white p-6 rounded-[20px] shadow-sm border border-gray-50/50">
        <h3 class="text-[15px] font-bold text-gray-800 mb-6 px-2">Doanh thu theo tháng</h3>
        <div class="h-64 px-2">
           <Line :data="lineChartData" :options="lineChartOptions" />
        </div>
      </div>
      <div class="bg-white p-6 rounded-[20px] shadow-sm border border-gray-50/50">
        <h3 class="text-[15px] font-bold text-gray-800 mb-6">Doanh thu theo thương hiệu</h3>
        <div class="h-64 flex justify-center">
           <div class="w-[90%]">
             <Pie :data="pieChartData" :options="pieChartOptions" />
           </div>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="bg-white p-4 lg:p-6 rounded-[24px] shadow-sm border border-gray-50 border-t-2 border-t-gray-800">
      <h3 class="text-[15px] font-bold text-gray-800 mb-6">Thống kê sản phẩm</h3>
      <div class="overflow-x-auto hide-scrollbar">
        <table class="w-full text-left min-w-[800px]">
          <thead>
            <tr class="bg-[#dcdcdc] rounded-[8px] text-gray-800 text-[13px]">
            <th class="py-3.5 px-4 font-bold text-center w-16 first:rounded-l-lg">STT</th>
            <th class="py-3.5 px-4 font-bold text-left">Sản phẩm</th>
            <th class="py-3.5 px-4 font-bold text-center">Đã bán</th>
            <th class="py-3.5 px-4 font-bold text-center">Doanh thu</th>
            <th class="py-3.5 px-4 font-bold text-center last:rounded-r-lg">Tồn kho</th>
          </tr>
        </thead>
        <tbody class="text-sm">
          <tr v-for="product in products" :key="product.rank" class="border-b border-gray-50/50 hover:bg-gray-50/50 transition-colors">
            <td class="py-4 px-4 text-center font-bold text-gray-800">{{ product.rank }}</td>
            <td class="py-4 px-4 flex items-center text-gray-800 font-bold text-[13px]">
              <svg class="w-4 h-4 mr-2.5 text-gray-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.38 3.46 16 2a4 4 0 0 1-8 0L3.62 3.46a2 2 0 0 0-1.34 2.23l.58 3.47a1 1 0 0 0 .99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 0 0 2-2V10h2.15a1 1 0 0 0 .99-.84l.58-3.47a2 2 0 0 0-1.34-2.23z"/></svg>
              {{ product.name }}
            </td>
            <td class="py-4 px-4 text-center">
              <span class="inline-flex items-center text-orange-800 font-bold text-[13px]">
                <span class="mr-1.5 text-sm">📦</span> {{ product.sold }}
              </span>
            </td>
            <td class="py-4 px-4 text-center font-bold text-gray-800 text-[13px]">
              <span class="text-yellow-600 mr-2 text-sm">💰</span>{{ product.revenue }}
            </td>
            <td class="py-4 px-4 text-center">
              <span class="inline-flex items-center text-orange-500 bg-orange-50 px-3 py-1 rounded-full text-xs font-bold whitespace-nowrap border border-orange-100">
                <span class="w-[6px] h-[6px] rounded-full bg-orange-400 mr-2"></span> {{ product.stock }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
      </div>
    </div>
  </div>
</template>
