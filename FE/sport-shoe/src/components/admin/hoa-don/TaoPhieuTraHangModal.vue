<script setup>
import { computed, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { PackageCheck, X } from "lucide-vue-next";
import Button from "../../ui/Button.vue";
import { taoPhieuTraHang } from "../../../services/tra-hang";
import { getDisplayErrorMessage } from "../../../utils/error-message";
import { showError, showSuccess } from "../../../utils/alert";

const props = defineProps({
  hoaDon: {
    type: Object,
    required: true,
  },
});

const router = useRouter();
const hienModal = ref(false);
const dangLuu = ref(false);
const form = ref(taoForm());

const coTheTaoPhieu = computed(() => {
  const value = boDau(props.hoaDon?.trangThai);
  return value === "da giao hang" || value === "hoan thanh";
});

const sanPhamDaChon = computed(() =>
  form.value.sanPhams.filter((item) => item.daChon && Number(item.soLuong) > 0),
);

watch(
  () => props.hoaDon?.id,
  () => {
    form.value = taoForm();
  },
);

function boDau(value) {
  return String(value || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim();
}

function taoForm() {
  return {
    lyDoMa: "KHONG_VUA",
    moTa: "",
    hinhThucHoan: 2,
    sanPhams: (props.hoaDon?.sanPham || []).map((item) => ({
      hoaDonChiTietId: item.id,
      tenSanPham: item.tenSanPham,
      phanLoai: [item.mauSac, item.kichCo].filter(Boolean).join(" / "),
      soLuongDaMua: item.soLuong,
      soLuong: 1,
      ghiChu: "",
      daChon: false,
    })),
  };
}

function moModal() {
  form.value = taoForm();
  hienModal.value = true;
}

async function taoPhieu() {
  if (!sanPhamDaChon.value.length) {
    showError("Vui lòng chọn ít nhất một sản phẩm cần trả.");
    return;
  }

  dangLuu.value = true;
  try {
    const response = await taoPhieuTraHang({
      hoaDonId: props.hoaDon.id,
      lyDoMa: form.value.lyDoMa,
      moTa: form.value.moTa,
      hinhThucHoan: form.value.hinhThucHoan,
      sanPhams: sanPhamDaChon.value.map((item) => ({
        hoaDonChiTietId: item.hoaDonChiTietId,
        soLuong: Number(item.soLuong),
        ghiChu: item.ghiChu,
      })),
    });
    hienModal.value = false;
    showSuccess("Tạo phiếu trả hàng thành công");
    router.push({ name: "admin-tra-hang-chi-tiet", params: { id: response.id } });
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Không thể tạo phiếu trả hàng"));
  } finally {
    dangLuu.value = false;
  }
}
</script>

<template>
  <Button v-if="coTheTaoPhieu" variant="soft" full-width @click="moModal">
    <template #prefix><PackageCheck class="h-4 w-4" /></template>
    Tạo phiếu trả hàng
  </Button>

  <div
    v-if="hienModal"
    class="fixed inset-0 z-[90] flex items-center justify-center bg-slate-950/50 p-4 backdrop-blur-sm"
  >
    <div class="flex max-h-[92vh] w-full max-w-3xl flex-col overflow-hidden rounded-3xl border border-rose-100 bg-white shadow-2xl">
      <div class="flex items-center justify-between border-b border-slate-100 px-6 py-5">
        <div>
          <h3 class="text-xl font-bold text-slate-800">Tạo phiếu trả hàng</h3>
          <p class="mt-1 text-sm text-slate-400">{{ hoaDon.maHoaDon }} · {{ hoaDon.tenKhachHang }}</p>
        </div>
        <button
          type="button"
          class="rounded-full p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
          @click="hienModal = false"
        >
          <X class="h-5 w-5" />
        </button>
      </div>

      <div class="min-h-0 flex-1 space-y-5 overflow-y-auto px-6 py-5">
        <div class="grid gap-4 sm:grid-cols-2">
          <label class="space-y-2">
            <span class="text-sm font-semibold text-slate-600">Lý do trả hàng</span>
            <select
              v-model="form.lyDoMa"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white"
            >
              <option value="KHONG_VUA">Không vừa kích cỡ</option>
              <option value="LOI_SAN_PHAM">Sản phẩm bị lỗi</option>
              <option value="SAI_SAN_PHAM">Giao sai sản phẩm</option>
              <option value="KHONG_DUNG_MO_TA">Không đúng mô tả</option>
              <option value="KHAC">Lý do khác</option>
            </select>
          </label>
          <label class="space-y-2">
            <span class="text-sm font-semibold text-slate-600">Hình thức hoàn tiền</span>
            <select
              v-model.number="form.hinhThucHoan"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white"
            >
              <option :value="1">Tiền mặt</option>
              <option :value="2">Chuyển khoản</option>
              <option :value="3">Ví điện tử</option>
            </select>
          </label>
        </div>

        <label class="block space-y-2">
          <span class="text-sm font-semibold text-slate-600">Mô tả yêu cầu</span>
          <textarea
            v-model="form.moTa"
            rows="3"
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm outline-none focus:border-rose-300 focus:bg-white"
            placeholder="Mô tả tình trạng và mong muốn của khách hàng..."
          ></textarea>
        </label>

        <div>
          <div class="mb-3 flex items-center justify-between">
            <div>
              <h4 class="font-semibold text-slate-800">Chọn sản phẩm trả</h4>
              <p class="mt-1 text-xs text-slate-400">Số lượng trả không được vượt quá số lượng đã mua</p>
            </div>
            <span class="text-sm font-semibold text-primary">{{ sanPhamDaChon.length }} sản phẩm</span>
          </div>

          <div class="space-y-3">
            <div
              v-for="item in form.sanPhams"
              :key="item.hoaDonChiTietId"
              class="rounded-2xl border p-4 transition"
              :class="item.daChon ? 'border-rose-200 bg-rose-50/50' : 'border-slate-200 bg-white'"
            >
              <div class="flex flex-col gap-4 sm:flex-row sm:items-center">
                <label class="flex min-w-0 flex-1 cursor-pointer items-start gap-3">
                  <input v-model="item.daChon" type="checkbox" class="mt-1 h-4 w-4 accent-red-600" />
                  <span class="min-w-0">
                    <span class="block font-semibold text-slate-800">{{ item.tenSanPham }}</span>
                    <span class="mt-1 block text-xs text-slate-400">
                      {{ item.phanLoai || "Không có phân loại" }} · Đã mua {{ item.soLuongDaMua }}
                    </span>
                  </span>
                </label>

                <label class="flex items-center gap-2 text-sm text-slate-500">
                  Số lượng
                  <input
                    v-model.number="item.soLuong"
                    type="number"
                    min="1"
                    :max="item.soLuongDaMua"
                    :disabled="!item.daChon"
                    class="h-10 w-20 rounded-xl border border-slate-200 bg-white px-3 text-center font-semibold outline-none focus:border-rose-300 disabled:bg-slate-100"
                  />
                </label>
              </div>
              <input
                v-if="item.daChon"
                v-model="item.ghiChu"
                class="mt-3 h-10 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm outline-none focus:border-rose-300"
                placeholder="Ghi chú riêng cho sản phẩm..."
              />
            </div>
          </div>
        </div>
      </div>

      <div class="flex justify-end gap-3 border-t border-slate-100 bg-slate-50 px-6 py-4">
        <Button variant="outline" @click="hienModal = false">Hủy</Button>
        <Button :loading="dangLuu" @click="taoPhieu">Tạo phiếu trả hàng</Button>
      </div>
    </div>
  </div>
</template>
