<script setup>
import { ref } from "vue";
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";

const {
  Card,
  Button,
  ClipboardList,
  History,
  TriangleAlert,
  CircleX,
  RefreshCw,
  hoaDon,
  cacBuocHienThi,
  lopVongTrangThai,
  lopTenTrangThai,
  dinhDangGio,
  dinhDangNgay,
  donYeuCauHuy,
  donDaHuy,
  hienModalLichSu,
  lyDoHuyDon,
  donDaKetThuc,
  dangCapNhat,
  dangGiaoLai,
  donGiaoThatBai,
  handleGiaoLaiDonHang,
  handleHuyDonTuModal,
} = useInvoiceDetailContext();

const hienModalLyDoHuy = ref(false);
const lyDoHuy = ref("");

const moModalHuy = () => {
  lyDoHuy.value = "";
  hienModalLyDoHuy.value = true;
};

const xacNhanHuy = async () => {
  if (!lyDoHuy.value.trim()) return;
  hienModalLyDoHuy.value = false;
  await handleHuyDonTuModal(lyDoHuy.value);
};
</script>

<template>
  <Card class="flex h-full flex-col px-4 py-4 md:px-5 xl:col-span-2">
    <template #header>
      <div class="flex w-full flex-wrap items-center justify-between gap-3">
        <div
          class="flex items-center gap-2 text-[15px] font-semibold text-slate-700"
        >
          <ClipboardList class="h-4.5 w-4.5 text-slate-500" />
          Trạng Thái Đơn Hàng
        </div>
        <div class="flex flex-wrap items-center justify-end gap-2">
          <span
            class="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700"
          >
            {{ hoaDon.trangThai }}
          </span>
        </div>
      </div>
    </template>
    <div class="relative mt-7 px-2 pt-2 flex justify-center">
      <div class="flex w-full items-start justify-around relative max-w-4xl">
        <div
          class="absolute top-7 hidden h-[2px] bg-slate-200 md:block z-0"
          :style="{
            left: 100 / cacBuocHienThi.length / 2 + '%',
            right: 100 / cacBuocHienThi.length / 2 + '%',
          }"
        ></div>
        <div
          v-for="buoc in cacBuocHienThi"
          :key="buoc.id"
          class="relative z-10 flex w-32 flex-col items-center text-center"
        >
          <div
            class="flex h-[58px] w-[58px] items-center justify-center overflow-visible rounded-full border-[2.5px] transition-all"
            :class="lopVongTrangThai(buoc)"
          >
            <component
              :is="buoc.icon"
              class="h-[22px] w-[22px] block shrink-0"
              stroke-width="2.25"
            />
          </div>
          <p
            class="mt-3 whitespace-nowrap text-[12px] font-semibold"
            :class="lopTenTrangThai(buoc)"
          >
            {{ buoc.ten }}
          </p>
          <div class="mt-1 min-h-[32px]">
            <p v-if="buoc.thoiGian" class="text-[11px] leading-4 text-slate-400">
              <span class="block">{{ dinhDangGio(buoc.thoiGian) }}</span>
              <span class="block">{{ dinhDangNgay(buoc.thoiGian) }}</span>
            </p>
            <p v-if="buoc.nhanVien" class="text-[11px] text-slate-400">
              {{ buoc.nhanVien }}
            </p>
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="donYeuCauHuy"
      class="mt-5 flex items-center justify-center gap-2 rounded-[6px] border border-amber-200 bg-amber-50 px-4 py-2.5 text-sm font-semibold text-amber-700"
    >
      <TriangleAlert class="h-4 w-4" />
      Khách hàng yêu cầu hủy - đang chờ xác nhận
    </div>
    <div
      v-if="donDaHuy"
      class="mt-5 flex items-center justify-center gap-2 rounded-[6px] border border-rose-200 bg-rose-50 px-4 py-2.5 text-sm font-semibold text-rose-700"
    >
      <CircleX class="h-4 w-4" />
      <span>
        Đơn hàng đã bị hủy
        <template v-if="lyDoHuyDon">
          do: <span class="font-normal">{{ lyDoHuyDon }}</span>
        </template>
      </span>
    </div>
    <div class="mt-5 flex justify-end gap-3">
      <Button
        v-if="donGiaoThatBai"
        :disabled="dangGiaoLai"
        @click="handleGiaoLaiDonHang"
        class="border-none bg-amber-500 text-white shadow-sm hover:bg-amber-600 disabled:cursor-not-allowed disabled:opacity-60"
      >
        <template #prefix>
          <RefreshCw class="h-4 w-4" :class="{ 'animate-spin': dangGiaoLai }" />
        </template>
        {{ dangGiaoLai ? "Đang tạo lượt giao..." : "Giao lại đơn hàng" }}
      </Button>
      <Button
        v-if="hoaDon && !donDaKetThuc"
        @click="moModalHuy"
        class="border-none bg-red-600 text-white hover:bg-red-700 shadow-sm"
      >
        <template #prefix>
          <CircleX class="h-4 w-4" />
        </template>
        Hủy Đơn Hàng
      </Button>
      <Button variant="primary" @click="hienModalLichSu = true">
        <template #prefix>
          <History class="h-4 w-4" />
        </template>
        Lịch Sử Thao Tác
      </Button>
    </div>
  </Card>

  <!-- Small modal for cancellation reason -->
  <div
    v-if="hienModalLyDoHuy"
    class="fixed inset-0 z-[60] flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm"
  >
    <div class="w-full max-w-sm overflow-hidden rounded-[16px] bg-white p-6 shadow-2xl">
      <div class="flex items-center justify-between border-b border-slate-100 pb-3">
        <h4 class="text-base font-bold text-slate-800">Lý do hủy đơn hàng</h4>
        <button
          @click="hienModalLyDoHuy = false"
          class="text-slate-400 transition hover:text-slate-600"
        >
          <CircleX class="h-5 w-5" />
        </button>
      </div>
      <div class="mt-4">
        <textarea
          v-model="lyDoHuy"
          rows="3"
          placeholder="Nhập lý do hủy đơn hàng..."
          class="w-full rounded-[6px] border border-slate-200 bg-slate-50 p-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white resize-none"
        ></textarea>
      </div>
      <div class="mt-6 flex gap-3">
        <button
          @click="hienModalLyDoHuy = false"
          type="button"
          class="flex-1 inline-flex h-9 items-center justify-center rounded-[6px] bg-slate-100 text-sm font-semibold text-slate-600 hover:bg-slate-200 transition"
        >
          Quay lại
        </button>
        <button
          @click="xacNhanHuy"
          :disabled="!lyDoHuy.trim() || dangCapNhat"
          type="button"
          class="flex-1 inline-flex h-9 items-center justify-center rounded-[6px] bg-[#B82220] text-sm font-semibold text-white hover:bg-[#9f1d1b] transition disabled:cursor-not-allowed disabled:opacity-55"
        >
          {{ dangCapNhat ? 'Đang hủy...' : 'Xác nhận hủy' }}
        </button>
      </div>
    </div>
  </div>
</template>
