<script setup>
import { computed, ref, onMounted, onUnmounted } from "vue";
import { Search, ChevronDown, Check } from "lucide-vue-next";
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";

const {
  Banknote,
  CheckCircle2,
  X,
  hoaDon,
  hienModalHoanTien,
  dangXacNhanHoanTien,
  formHoanTien,
  tongTienHoan,
  dsTaiKhoanNganHangKhach,
  dangTaiNganHangKhach,
  taiKhoanNganHangChon,
  dsNganHangPhoBien,
  thongTinNhanTien,
  qrHoanTienUrl,
  dinhDangTien,
  handleXacNhanHoanTien,
} = useInvoiceDetailContext();

const dinhDangTienKhongDonVi = (value) => {
  if (value === null || value === undefined || String(value).trim() === "") {
    return "0";
  }
  const rawValue = String(value).replace(/\D/g, "");
  if (!rawValue) return "0";
  return new Intl.NumberFormat("vi-VN", { maximumFractionDigits: 0 }).format(
    Number(rawValue) || 0,
  );
};

const noiDungChuyenKhoanHienThi = computed(() => {
  const maHd = hoaDon.value?.maHoaDon || hoaDon.value?.ma || "";
  const cleanMaHd = String(maHd).replace(/[^a-zA-Z0-9]/g, "").toUpperCase();
  return `SHOEHT${cleanMaHd}`;
});

const coDuThongTinChuyenKhoan = computed(() => {
  if (formHoanTien.value.hinhThucHoanTien !== 2) return true;
  if (formHoanTien.value.cheDoTaiKhoan === "chon") {
    return Boolean(taiKhoanNganHangChon.value?.id);
  }
  return Boolean(
    formHoanTien.value.nganHangNhap &&
    formHoanTien.value.soTaiKhoanNhap?.trim()
  );
});

// State & Logic cho Combobox Ngân hàng tìm kiếm
const hienDropdownNganHang = ref(false);
const tuKhoaTimNganHang = ref("");
const inputTimNganHangRef = ref(null);
const dropdownNganHangRef = ref(null);

function loaiBoDau(str) {
  return String(str || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D")
    .toLowerCase();
}

const nganHangHienTai = computed(() => {
  const list = dsNganHangPhoBien.value || [];
  return (
    list.find((nh) => nh.code === formHoanTien.value.nganHangNhap) || {
      code: formHoanTien.value.nganHangNhap || "MB",
      shortName: formHoanTien.value.nganHangNhap || "MB",
      name: "Ngân hàng Quân Đội (MBBank)",
    }
  );
});

const danhSachNganHangLoc = computed(() => {
  const list = dsNganHangPhoBien.value || [];
  const kw = loaiBoDau(tuKhoaTimNganHang.value.trim());
  if (!kw) return list;
  return list.filter((nh) => {
    return (
      loaiBoDau(nh.code).includes(kw) ||
      loaiBoDau(nh.shortName).includes(kw) ||
      loaiBoDau(nh.name).includes(kw)
    );
  });
});

function batTatDropdownNganHang() {
  hienDropdownNganHang.value = !hienDropdownNganHang.value;
  if (hienDropdownNganHang.value) {
    tuKhoaTimNganHang.value = "";
    setTimeout(() => {
      inputTimNganHangRef.value?.focus();
    }, 80);
  }
}

function chonNganHang(nh) {
  formHoanTien.value.nganHangNhap = nh.code;
  hienDropdownNganHang.value = false;
  tuKhoaTimNganHang.value = "";
}

function xuLyClickNgoai(event) {
  if (
    dropdownNganHangRef.value &&
    !dropdownNganHangRef.value.contains(event.target)
  ) {
    hienDropdownNganHang.value = false;
  }
}

onMounted(() => {
  document.addEventListener("click", xuLyClickNgoai);
});

onUnmounted(() => {
  document.removeEventListener("click", xuLyClickNgoai);
});
</script>

<template>
  <div
    v-if="hienModalHoanTien"
    class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm overflow-y-auto"
  >
    <div
      class="flex flex-col md:flex-row gap-6 max-w-[950px] w-full max-h-[92vh] items-center md:items-stretch justify-center"
    >
      <!-- Main Form Card -->
      <div
        class="w-full max-w-[540px] rounded-[24px] bg-white shadow-2xl flex flex-col justify-between border border-slate-100 overflow-hidden"
      >
        <div class="bg-amber-50 px-6 py-5 shrink-0">
          <div class="flex items-start justify-between gap-4">
            <div class="flex items-start gap-3">
              <div
                class="flex h-11 w-11 shrink-0 items-center justify-center rounded-[8px] bg-amber-500 text-white shadow-sm"
              >
                <Banknote class="h-5 w-5" />
              </div>
              <div>
                <h3 class="text-lg font-extrabold uppercase tracking-wide text-slate-800">
                  Xác nhận hoàn tiền
                </h3>
                <p class="mt-0.5 text-sm text-slate-500">
                  {{ hoaDon.maHoaDon || hoaDon.ma }} - {{ hoaDon.tenKhachHang }}
                </p>
              </div>
            </div>
            <button
              type="button"
              class="flex h-9 w-9 items-center justify-center rounded-full bg-white text-slate-400 shadow-sm transition hover:text-slate-700"
              @click="hienModalHoanTien = false"
            >
              <X class="h-5 w-5" />
            </button>
          </div>
        </div>

        <div class="space-y-4 px-6 py-5 overflow-y-auto max-h-[60vh] md:max-h-none">
          <!-- Số tiền cần hoàn cố định -->
          <div
            class="flex items-center justify-between rounded-[10px] border border-amber-200 bg-amber-50/70 px-4 py-3"
          >
            <div>
              <span class="text-xs font-bold uppercase tracking-wider text-amber-800">
                Số tiền hoàn (100%)
              </span>
              <p class="text-xs text-slate-500 mt-0.5">
                Hệ thống hoàn đủ số tiền cho khách hàng
              </p>
            </div>
            <span class="text-xl font-extrabold text-amber-600">
              {{ dinhDangTienKhongDonVi(tongTienHoan) }} đ
            </span>
          </div>

          <!-- Phương thức hoàn -->
          <label class="block space-y-1.5">
            <span class="text-sm font-bold text-slate-700">Phương thức hoàn tiền</span>
            <select
              v-model.number="formHoanTien.hinhThucHoanTien"
              class="h-11 w-full rounded-[8px] border border-slate-200 bg-slate-50 px-4 text-sm font-semibold text-slate-700 outline-none transition focus:border-amber-400 focus:bg-white focus:ring-4 focus:ring-amber-100"
            >
              <option :value="2">Chuyển khoản (SePay VietQR)</option>
              <option :value="1">Tiền mặt tại quầy</option>
            </select>
          </label>

          <!-- Tùy chọn tài khoản nhận tiền khi là Chuyển khoản -->
          <div v-if="formHoanTien.hinhThucHoanTien === 2" class="space-y-3 pt-1">
            <!-- Tabs chọn / nhập tài khoản -->
            <div class="flex rounded-lg bg-slate-100 p-1 text-xs font-bold text-slate-600">
              <button
                type="button"
                class="flex-1 py-1.5 rounded-md transition"
                :class="
                  formHoanTien.cheDoTaiKhoan === 'chon'
                    ? 'bg-white text-slate-900 shadow-sm'
                    : 'text-slate-500 hover:text-slate-800'
                "
                :disabled="dsTaiKhoanNganHangKhach.length === 0"
                @click="formHoanTien.cheDoTaiKhoan = 'chon'"
              >
                Tài khoản liên kết ({{ dsTaiKhoanNganHangKhach.length }})
              </button>
              <button
                type="button"
                class="flex-1 py-1.5 rounded-md transition"
                :class="
                  formHoanTien.cheDoTaiKhoan === 'nhap'
                    ? 'bg-white text-slate-900 shadow-sm'
                    : 'text-slate-500 hover:text-slate-800'
                "
                @click="formHoanTien.cheDoTaiKhoan = 'nhap'"
              >
                Nhập tài khoản ngân hàng
              </button>
            </div>

            <!-- Trường hợp chọn tài khoản liên kết -->
            <div v-if="formHoanTien.cheDoTaiKhoan === 'chon'" class="space-y-2">
              <div v-if="dangTaiNganHangKhach" class="text-xs text-slate-400 py-2">
                Đang tải danh sách tài khoản...
              </div>
              <select
                v-else-if="dsTaiKhoanNganHangKhach.length > 0"
                v-model="taiKhoanNganHangChon"
                class="h-11 w-full rounded-[8px] border border-slate-200 bg-slate-50 px-4 text-sm font-semibold text-slate-700 outline-none transition focus:border-amber-400 focus:bg-white focus:ring-4 focus:ring-amber-100"
              >
                <option v-for="tk in dsTaiKhoanNganHangKhach" :key="tk.id" :value="tk">
                  {{ tk.tenNganHang }} - {{ tk.soTaiKhoan }} ({{ tk.tenChuTaiKhoan }})
                  {{ tk.laMacDinh ? "[Mặc định]" : "" }}
                </option>
              </select>
              <div
                v-else
                class="rounded-[8px] border border-amber-100 bg-amber-50/70 p-3 text-xs text-amber-800"
              >
                Khách hàng chưa lưu tài khoản ngân hàng nào. Vui lòng chuyển sang tab
                <strong>Nhập tài khoản ngân hàng</strong>.
              </div>
            </div>

            <!-- Trường hợp nhập tài khoản trực tiếp -->
            <div v-else class="space-y-3 rounded-[12px] border border-slate-100 bg-slate-50/80 p-3.5">
              <!-- Searchable Combobox Ngân hàng thụ hưởng -->
              <div ref="dropdownNganHangRef" class="relative space-y-1">
                <div class="flex items-center justify-between">
                  <span class="text-xs font-bold text-slate-600">Ngân hàng thụ hưởng</span>
                  <span class="text-[11px] text-slate-400">
                    ({{ dsNganHangPhoBien.length }} ngân hàng)
                  </span>
                </div>

                <!-- Ô trigger click mở dropdown -->
                <button
                  type="button"
                  class="flex h-11 w-full items-center justify-between rounded-[8px] border border-slate-200 bg-white px-3 text-left transition focus:border-amber-400 focus:outline-none focus:ring-2 focus:ring-amber-100 hover:border-amber-300"
                  @click="batTatDropdownNganHang"
                >
                  <div class="flex items-center gap-2 overflow-hidden">
                    <img
                      v-if="nganHangHienTai.logo"
                      :src="nganHangHienTai.logo"
                      alt="logo"
                      class="h-5 w-auto max-w-[36px] object-contain shrink-0"
                    />
                    <span
                      v-else
                      class="rounded bg-amber-100 px-1.5 py-0.5 font-bold text-[11px] text-amber-800 shrink-0"
                    >
                      {{ nganHangHienTai.code }}
                    </span>
                    <span class="font-bold text-slate-800 text-sm truncate">
                      {{ nganHangHienTai.shortName }}
                    </span>
                    <span class="hidden sm:inline text-xs text-slate-400 truncate max-w-[200px]">
                      - {{ nganHangHienTai.name }}
                    </span>
                  </div>
                  <ChevronDown
                    class="h-4 w-4 text-slate-400 transition-transform duration-200 shrink-0 ml-1"
                    :class="{ 'rotate-180 text-amber-500': hienDropdownNganHang }"
                  />
                </button>

                <!-- Dropdown Popup tìm kiếm -->
                <div
                  v-if="hienDropdownNganHang"
                  class="absolute left-0 right-0 top-full z-50 mt-1 rounded-[12px] border border-slate-200 bg-white shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-100"
                >
                  <!-- Input tìm kiếm -->
                  <div class="border-b border-slate-100 bg-slate-50/70 p-2">
                    <div class="relative flex items-center">
                      <Search class="absolute left-3 h-4 w-4 text-slate-400" />
                      <input
                        ref="inputTimNganHangRef"
                        v-model="tuKhoaTimNganHang"
                        type="text"
                        placeholder="Tìm theo mã hoặc tên (VCB, MB, Techcombank, Quân đội...)"
                        class="h-9 w-full rounded-[6px] border border-slate-200 bg-white pl-9 pr-3 text-xs text-slate-700 outline-none focus:border-amber-400 focus:ring-1 focus:ring-amber-200"
                      />
                    </div>
                  </div>

                  <!-- Danh sách kết quả -->
                  <div class="max-h-56 overflow-y-auto divide-y divide-slate-100/70">
                    <div
                      v-if="danhSachNganHangLoc.length === 0"
                      class="p-4 text-center text-xs text-slate-400"
                    >
                      Không tìm thấy ngân hàng nào phù hợp với "{{ tuKhoaTimNganHang }}"
                    </div>
                    <button
                      v-for="nh in danhSachNganHangLoc"
                      :key="nh.code"
                      type="button"
                      class="flex w-full items-center justify-between px-3.5 py-2.5 text-left transition hover:bg-amber-50/70"
                      :class="{ 'bg-amber-50/90 font-semibold': formHoanTien.nganHangNhap === nh.code }"
                      @click="chonNganHang(nh)"
                    >
                      <div class="flex items-center gap-2.5 overflow-hidden">
                        <img
                          v-if="nh.logo"
                          :src="nh.logo"
                          alt="logo"
                          class="h-5 w-auto max-w-[32px] object-contain shrink-0"
                        />
                        <span
                          v-else
                          class="rounded bg-slate-100 px-1.5 py-0.5 font-bold text-[10px] text-slate-700 shrink-0"
                        >
                          {{ nh.code }}
                        </span>
                        <div class="flex flex-col overflow-hidden">
                          <span class="text-xs font-bold text-slate-800">
                            {{ nh.shortName }}
                          </span>
                          <span class="text-[11px] text-slate-500 truncate max-w-[300px]">
                            {{ nh.name }}
                          </span>
                        </div>
                      </div>
                      <Check
                        v-if="formHoanTien.nganHangNhap === nh.code"
                        class="h-4 w-4 text-amber-600 shrink-0 ml-2"
                      />
                    </button>
                  </div>
                </div>
              </div>

              <!-- Ô nhập Số tài khoản & Tên chủ tài khoản -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div class="space-y-1">
                  <span class="text-xs font-bold text-slate-600">Số tài khoản nhận</span>
                  <input
                    v-model="formHoanTien.soTaiKhoanNhap"
                    type="text"
                    placeholder="VD: 0965852782..."
                    class="h-10 w-full rounded-[6px] border border-slate-200 bg-white px-3 text-sm text-slate-700 outline-none focus:border-amber-400 focus:ring-2 focus:ring-amber-100"
                  />
                </div>
                <div class="space-y-1">
                  <span class="text-xs font-bold text-slate-600">Tên chủ tài khoản</span>
                  <input
                    v-model="formHoanTien.tenChuTaiKhoanNhap"
                    type="text"
                    placeholder="VD: NGUYEN MINH KHANG"
                    class="h-10 w-full rounded-[6px] border border-slate-200 bg-white px-3 text-sm uppercase text-slate-700 outline-none focus:border-amber-400 focus:ring-2 focus:ring-amber-100"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- Mã giao dịch & Ghi chú -->
          <div class="space-y-3">
            <label class="block space-y-1.5">
              <span class="text-xs font-bold text-slate-600">
                Mã giao dịch đối soát (nếu chuyển thủ công)
              </span>
              <input
                v-model="formHoanTien.maGiaoDichHoan"
                type="text"
                placeholder="Để trống hệ thống sẽ tự sinh mã..."
                class="h-10 w-full rounded-[8px] border border-slate-200 bg-slate-50 px-3.5 text-sm text-slate-700 outline-none transition focus:border-amber-400 focus:bg-white focus:ring-4 focus:ring-amber-100"
              />
            </label>
            <label class="block space-y-1.5">
              <span class="text-xs font-bold text-slate-600">Ghi chú hoàn tiền</span>
              <textarea
                v-model="formHoanTien.ghiChu"
                rows="2"
                placeholder="Lý do hoàn tiền..."
                class="w-full rounded-[8px] border border-slate-200 bg-slate-50 px-3.5 py-2.5 text-sm text-slate-700 outline-none transition focus:border-amber-400 focus:bg-white focus:ring-4 focus:ring-amber-100"
              ></textarea>
            </label>
          </div>
        </div>

        <div
          class="flex flex-col-reverse gap-3 border-t border-slate-100 bg-slate-50 px-6 py-4 sm:flex-row sm:justify-end shrink-0"
        >
          <button
            type="button"
            class="h-11 rounded-[8px] border border-slate-200 bg-white px-5 text-sm font-bold text-slate-600 transition hover:bg-slate-100"
            @click="hienModalHoanTien = false"
          >
            Đóng
          </button>
          <button
            type="button"
            class="inline-flex h-11 items-center justify-center gap-2 rounded-[8px] bg-amber-500 px-5 text-sm font-bold text-white shadow-[0_12px_28px_rgba(245,158,11,0.24)] transition hover:bg-amber-600 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="dangXacNhanHoanTien || !coDuThongTinChuyenKhoan"
            @click="handleXacNhanHoanTien"
          >
            <CheckCircle2 class="h-4 w-4" />
            {{ dangXacNhanHoanTien ? "Đang xác nhận..." : "Xác nhận hoàn tiền thủ công" }}
          </button>
        </div>
      </div>

      <!-- Separate QR Card (SePay) -->
      <div
        v-if="formHoanTien.hinhThucHoanTien === 2 && qrHoanTienUrl"
        class="w-full md:w-[350px] rounded-[24px] bg-white shadow-2xl border border-slate-100 p-6 flex flex-col items-center justify-between text-center shrink-0 self-center"
      >
        <div class="w-full">
          <div class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200 text-xs font-bold">
            <span class="h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></span>
            SePay Tự Động Nhận Diện
          </div>
          <p class="mt-2 text-xs text-slate-500">
            Quét mã QR từ app ngân hàng của cửa hàng để chuyển tiền cho khách:
          </p>
        </div>

        <div
          class="my-3 h-52 w-52 overflow-hidden rounded-[18px] border-2 border-amber-200 bg-white p-2 flex items-center justify-center shadow-md"
        >
          <img
            :src="qrHoanTienUrl"
            alt="SePay VietQR Hoàn Tiền"
            class="h-full w-full object-contain"
          />
        </div>

        <div class="w-full space-y-2 text-left bg-slate-50 rounded-[12px] p-3 text-xs border border-slate-100">
          <div class="flex justify-between">
            <span class="text-slate-500">Ngân hàng:</span>
            <span class="font-bold text-slate-700">{{ thongTinNhanTien?.bank }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-slate-500">Số tài khoản:</span>
            <span class="font-bold text-slate-800">{{ thongTinNhanTien?.account }}</span>
          </div>
          <div v-if="thongTinNhanTien?.name" class="flex justify-between">
            <span class="text-slate-500">Người nhận:</span>
            <span class="font-bold text-[#B82220] uppercase">{{ thongTinNhanTien.name }}</span>
          </div>
          <div class="flex justify-between border-t border-slate-200/60 pt-2">
            <span class="text-slate-500">Nội dung CK:</span>
            <span class="font-mono font-bold text-amber-700">{{ noiDungChuyenKhoanHienThi }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

