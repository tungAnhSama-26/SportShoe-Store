<script setup>
import {
  Bot,
  Clock3,
  History,
  ShieldCheck,
  UserRound,
  X,
} from "lucide-vue-next";
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";

const {
  hienModalLichSu,
  lichSuRutGon,
  dinhDangGio,
  dinhDangNgay,
} = useInvoiceDetailContext();

const tacNhan = {
  nhanVien: {
    label: "Nhân viên",
    icon: ShieldCheck,
    badge: "border-sky-200 bg-sky-50 text-sky-700",
    iconClass: "bg-sky-100 text-sky-700",
    dot: "bg-sky-500 ring-sky-100",
  },
  khachHang: {
    label: "Khách hàng",
    icon: UserRound,
    badge: "border-emerald-200 bg-emerald-50 text-emerald-700",
    iconClass: "bg-emerald-100 text-emerald-700",
    dot: "bg-emerald-500 ring-emerald-100",
  },
  heThong: {
    label: "Hệ thống",
    icon: Bot,
    badge: "border-slate-200 bg-slate-100 text-slate-600",
    iconClass: "bg-slate-100 text-slate-600",
    dot: "bg-slate-400 ring-slate-100",
  },
};

function layLoaiTacNhan(log) {
  const ma = String(log?.maNhanVien || "").trim().toLocaleLowerCase("vi");
  const ten = String(log?.tenNhanVien || "").trim().toLocaleLowerCase("vi");
  if (ma.includes("khách hàng") || ten.includes("khách hàng")) return "khachHang";
  if (!ma || ma.includes("hệ thống") || ten.includes("hệ thống")) return "heThong";
  return "nhanVien";
}

function layTacNhan(log) {
  return tacNhan[layLoaiTacNhan(log)];
}

function layTenNguoiThaoTac(log) {
  const ma = String(log?.maNhanVien || "").trim();
  const ten = String(log?.tenNhanVien || "").trim();
  if (!ma && !ten) return "Hệ thống";
  if (!ma || ma.toLocaleLowerCase("vi") === ten.toLocaleLowerCase("vi")) {
    return ten || ma;
  }
  return `${ma} · ${ten}`;
}
</script>

<template>
  <div
    v-if="hienModalLichSu"
    class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 p-3 backdrop-blur-sm sm:p-6"
    @click.self="hienModalLichSu = false"
  >
    <section
      class="history-modal-surface flex max-h-[90vh] w-full max-w-3xl flex-col overflow-hidden border border-white/70 bg-white shadow-[0_32px_100px_rgba(15,23,42,0.35)]"
      role="dialog"
      aria-modal="true"
      aria-labelledby="operation-history-title"
    >
      <header class="flex shrink-0 items-center justify-between border-b border-slate-100 bg-gradient-to-r from-rose-50 via-white to-white px-5 py-4 sm:px-7 sm:py-5">
        <div class="flex min-w-0 items-center gap-3.5">
          <div class="flex size-11 shrink-0 items-center justify-center rounded-full bg-rose-100 text-[#B82220]">
            <History class="size-5" />
          </div>
          <div class="min-w-0">
            <div class="flex flex-wrap items-center gap-2.5">
              <h3 id="operation-history-title" class="text-lg font-bold text-slate-900 sm:text-xl">
                Lịch sử thao tác
              </h3>
              <span class="rounded-full border border-rose-100 bg-white px-2.5 py-1 text-[11px] font-bold text-[#B82220] shadow-sm">
                {{ lichSuRutGon.length }} hoạt động
              </span>
            </div>
            <p class="mt-1 text-xs text-slate-500 sm:text-sm">
              Theo dõi người thực hiện và nội dung thay đổi trên hóa đơn
            </p>
          </div>
        </div>
        <button
          type="button"
          aria-label="Đóng lịch sử thao tác"
          class="flex size-10 shrink-0 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-700"
          @click="hienModalLichSu = false"
        >
          <X class="size-5" />
        </button>
      </header>

      <div class="min-h-0 flex-1 overflow-y-auto bg-slate-50/70 px-4 py-5 sm:px-7 sm:py-7">
        <div v-if="!lichSuRutGon.length" class="history-empty-state border border-dashed border-slate-200 bg-white px-6 py-14 text-center">
          <div class="mx-auto flex size-12 items-center justify-center rounded-full bg-slate-100 text-slate-400">
            <History class="size-5" />
          </div>
          <p class="mt-4 text-sm font-semibold text-slate-700">Chưa có lịch sử thao tác</p>
          <p class="mt-1 text-xs text-slate-400">Các thay đổi của hóa đơn sẽ xuất hiện tại đây.</p>
        </div>

        <div v-else class="relative pl-5 sm:pl-8">
          <div class="absolute bottom-5 left-[5px] top-5 w-px bg-gradient-to-b from-rose-200 via-slate-200 to-transparent sm:left-[9px]"></div>
          <div class="space-y-4 sm:space-y-5">
            <article v-for="log in lichSuRutGon" :key="log.id" class="relative">
              <span
                class="absolute -left-[19px] top-7 size-3 rounded-full ring-4 sm:-left-[27px]"
                :class="layTacNhan(log).dot"
              ></span>

              <div class="history-entry-card border border-slate-200/80 bg-white p-4 shadow-sm transition duration-200 hover:-translate-y-0.5 hover:border-slate-300 hover:shadow-md sm:p-5">
                <div class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                  <div class="flex min-w-0 items-center gap-3">
                    <div
                      class="flex size-10 shrink-0 items-center justify-center rounded-full"
                      :class="layTacNhan(log).iconClass"
                    >
                      <component :is="layTacNhan(log).icon" class="size-4.5" />
                    </div>
                    <div class="min-w-0">
                      <p class="truncate text-sm font-bold text-slate-800 sm:text-[15px]">
                        {{ layTenNguoiThaoTac(log) }}
                      </p>
                      <span
                        class="mt-1 inline-flex rounded-full border px-2 py-0.5 text-[10px] font-bold uppercase tracking-wide"
                        :class="layTacNhan(log).badge"
                      >
                        {{ layTacNhan(log).label }}
                      </span>
                    </div>
                  </div>

                  <div class="flex shrink-0 items-center gap-1.5 text-xs font-medium text-slate-400">
                    <Clock3 class="size-3.5" />
                    <time :datetime="log.ngayTao">
                      {{ dinhDangGio(log.ngayTao) }} · {{ dinhDangNgay(log.ngayTao) }}
                    </time>
                  </div>
                </div>

                <div class="mt-4 border-t border-slate-100 pt-4">
                  <p class="text-sm font-bold text-slate-900 sm:text-[15px]">
                    {{ log.trangThai }}
                  </p>
                  <div
                    v-if="log.ghiChu"
                    class="history-note mt-2.5 border border-slate-100 bg-slate-50 px-3.5 py-3 text-[13px] leading-6 text-slate-600 sm:px-4"
                  >{{ log.ghiChu }}</div>
                </div>
              </div>
            </article>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.history-modal-surface {
  border-radius: 24px;
}

.history-entry-card,
.history-empty-state {
  border-radius: 18px;
}

.history-note {
  border-radius: 12px;
  overflow-wrap: anywhere;
  white-space: pre-line;
}
</style>
