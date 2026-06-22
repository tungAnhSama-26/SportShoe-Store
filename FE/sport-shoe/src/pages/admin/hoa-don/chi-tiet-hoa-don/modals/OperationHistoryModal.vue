<script setup>
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";

const {
  History,
  CircleX,
  hienModalLichSu,
  lichSuRutGon,
  dinhDangGio,
  dinhDangNgay,
} = useInvoiceDetailContext();
</script>

<template>
  <div
    v-if="hienModalLichSu"
    class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm"
  >
    <div class="w-full max-w-2xl overflow-hidden rounded-[24px] bg-white shadow-2xl">
      <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
        <div class="flex items-center gap-3">
          <History class="h-5 w-5 text-slate-500" />
          <h3 class="text-[17px] font-bold text-slate-800">
            Lịch sử thao tác
          </h3>
        </div>
        <button
          @click="hienModalLichSu = false"
          class="text-slate-400 transition hover:text-slate-600"
        >
          <CircleX class="h-5 w-5" />
        </button>
      </div>
      <div class="max-h-[70vh] overflow-y-auto px-6 py-8">
        <div v-if="!lichSuRutGon.length" class="py-10 text-center text-sm text-slate-400">
          Chưa có lịch sử thao tác.
        </div>
        <div v-else class="relative pl-8">
          <div class="absolute bottom-0 left-[3.5px] top-0 w-[1.5px] bg-[#B82220]/20"></div>
          <div class="space-y-6">
            <div v-for="log in lichSuRutGon" :key="log.id" class="relative">
              <div
                class="absolute -left-[32px] top-4 h-2 w-2 rounded-full border-2 border-white bg-[#B82220] shadow-[0_0_0_2px_rgba(184,34,32,0.15)]"
              ></div>
              <div
                class="rounded-[6px] border border-slate-50 bg-slate-50/50 p-4 transition-colors hover:bg-slate-100/50"
              >
                <div class="text-[12px] text-slate-400 font-medium">
                  {{ dinhDangGio(log.ngayTao) }}
                  {{ dinhDangNgay(log.ngayTao) }}
                </div>
                <div class="mt-1 text-[13px] font-semibold text-slate-400">
                  {{ log.maNhanVien || "Hệ thống" }} -
                  {{ log.tenNhanVien || "admin" }}
                </div>
                <p class="mt-2 text-[15px] font-bold text-slate-800">
                  {{ log.trangThai }}
                </p>
                <p v-if="log.ghiChu" class="mt-2 text-[13px] text-slate-500 italic">
                  {{ log.ghiChu }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
