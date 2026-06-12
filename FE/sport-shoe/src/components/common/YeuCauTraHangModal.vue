<script setup>
import { ref, computed, watch } from "vue";
import { X, UploadCloud, Trash2, AlertCircle, Check } from "lucide-vue-next";
import { yeuCauTraHang } from "../../services/client-tra-hang";
import { uploadFileRequest } from "../../services/api-client";
import { showSuccess, showError } from "../../utils/alert";
import { dinhDangTienViet } from "../../utils/dinhDangTien";

const props = defineProps({
  isOpen: Boolean,
  don: Object,
});

const emit = defineEmits(["close", "success"]);

const lyDoMa = ref("PRODUCT_DEFECT");
const moTa = ref("");
const hinhThucHoan = ref(1);
const hinhAnhs = ref([]);
const dangTaiAnh = ref(false);
const dangGui = ref(false);
const fileInput = ref(null);

const dsLyDo = [
  { value: "PRODUCT_DEFECT", label: "Sản phẩm lỗi/hỏng hóc do nhà sản xuất" },
  { value: "WRONG_SIZE", label: "Giao sai kích cỡ (size) hoặc sai màu sắc" },
  { value: "NOT_AS_DESCRIBED", label: "Sản phẩm không đúng mô tả hình ảnh" },
  { value: "UNSATISFIED", label: "Không còn nhu cầu / Đổi ý" },
  { value: "KHAC", label: "Lý do khác" },
];

const dsHinhThucHoan = [
  { value: 1, label: "Chuyển khoản ngân hàng" },
  { value: 2, label: "Hoàn về ví điện tử" },
];

// Trạng thái chọn sản phẩm trong modal
const danhSachChon = ref([]);

// Khởi tạo danh sách khi đơn hàng thay đổi hoặc modal được mở
watch(
  () => props.isOpen,
  (open) => {
    if (open && props.don) {
      lyDoMa.value = "PRODUCT_DEFECT";
      moTa.value = "";
      hinhThucHoan.value = 1;
      hinhAnhs.value = [];
      danhSachChon.value = props.don.sanPhams.map((sp) => ({
        hoaDonChiTietId: sp.hoaDonChiTietId,
        ten: sp.tenSanPham,
        mauSac: sp.mauSac,
        kichCo: sp.kichCo,
        hinhAnh: sp.hinhAnh,
        giaDonVi: sp.giaDonVi,
        maxSoLuong: sp.soLuong,
        soLuong: sp.soLuong,
        checked: true,
        ghiChu: "",
      }));
    }
  },
  { immediate: true }
);

const triggerUpload = () => {
  if (fileInput.value) {
    fileInput.value.click();
  }
};

const handleFileUpload = async (event) => {
  const files = event.target.files;
  if (!files || files.length === 0) return;

  dangTaiAnh.value = true;
  try {
    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      const imageUrl = await uploadFileRequest(
        file,
        "Không thể tải ảnh bằng chứng lên lúc này."
      );
      hinhAnhs.value.push(imageUrl);
    }
  } catch (error) {
    showError(error.message || "Không thể tải lên hình ảnh. Vui lòng thử lại.");
  } finally {
    dangTaiAnh.value = false;
    if (fileInput.value) {
      fileInput.value.value = ""; // Reset file input
    }
  }
};

const removeImage = (index) => {
  hinhAnhs.value.splice(index, 1);
};

const validateForm = () => {
  const selectedItems = danhSachChon.value.filter((item) => item.checked);
  if (selectedItems.length === 0) {
    showError("Vui lòng chọn ít nhất một sản phẩm cần trả hàng.");
    return false;
  }

  for (const item of selectedItems) {
    if (!item.soLuong || item.soLuong < 1 || item.soLuong > item.maxSoLuong) {
      showError(`Số lượng trả của sản phẩm ${item.ten} không hợp lệ (tối đa ${item.maxSoLuong}).`);
      return false;
    }
  }

  if (!moTa.value.trim()) {
    showError("Vui lòng nhập mô tả chi tiết lý do trả hàng.");
    return false;
  }

  if (hinhAnhs.value.length === 0) {
    showError("Vui lòng tải lên ít nhất một hình ảnh minh chứng.");
    return false;
  }

  return true;
};

const submitYeuCau = async () => {
  if (!validateForm()) return;

  dangGui.value = true;
  try {
    const selectedItems = danhSachChon.value
      .filter((item) => item.checked)
      .map((item) => ({
        hoaDonChiTietId: item.hoaDonChiTietId,
        soLuong: item.soLuong,
        ghiChu: item.ghiChu || "",
      }));

    const payload = {
      hoaDonId: props.don.id,
      lyDoMa: lyDoMa.value,
      moTa: moTa.value.trim(),
      hinhThucHoan: hinhThucHoan.value,
      sanPhams: selectedItems,
      hinhAnhs: hinhAnhs.value,
    };

    await yeuCauTraHang(payload);
    showSuccess("Gửi yêu cầu trả hàng/hoàn tiền thành công. Admin sẽ sớm phê duyệt yêu cầu của bạn.");
    emit("success");
    emit("close");
  } catch (error) {
    showError(error.message || "Không thể gửi yêu cầu trả hàng. Vui lòng thử lại.");
  } finally {
    dangGui.value = false;
  }
};
</script>

<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm transition-opacity duration-300"
  >
    <div
      class="bg-white rounded-3xl w-full max-w-2xl max-h-[90vh] overflow-y-auto shadow-2xl border border-slate-100 flex flex-col transition-all duration-300 transform scale-100"
    >
      <!-- Modal Header -->
      <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100 sticky top-0 bg-white z-10">
        <h3 class="text-lg font-bold text-slate-800">Yêu cầu trả hàng & hoàn tiền</h3>
        <button
          @click="emit('close')"
          class="p-1.5 rounded-full hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition"
        >
          <X class="w-5 h-5" />
        </button>
      </div>

      <!-- Modal Body -->
      <div class="p-6 space-y-6 flex-1">
        <!-- Alert Info -->
        <div class="flex gap-3 bg-rose-50 border border-rose-100 p-4 rounded-2xl text-xs md:text-sm text-rose-700">
          <AlertCircle class="w-5 h-5 flex-shrink-0" />
          <div>
            <p class="font-bold">Lưu ý về quy định trả hàng:</p>
            <p class="mt-0.5 leading-relaxed">
              Yêu cầu trả hàng của bạn sẽ được gửi tới Ban quản trị kiểm duyệt. Vui lòng giữ sản phẩm còn nguyên tem mác, chưa qua sử dụng và tải ảnh chụp đầy đủ lỗi/bằng chứng để được duyệt nhanh nhất.
            </p>
          </div>
        </div>

        <!-- 1. Chọn sản phẩm cần trả -->
        <div class="space-y-3">
          <h4 class="font-bold text-slate-800 text-sm md:text-base">1. Chọn sản phẩm muốn trả</h4>
          <div class="space-y-3 max-h-60 overflow-y-auto pr-1">
            <div
              v-for="item in danhSachChon"
              :key="item.hoaDonChiTietId"
              class="flex items-start gap-4 p-3 border rounded-2xl transition"
              :class="item.checked ? 'border-primary/30 bg-primary/5' : 'border-slate-200'"
            >
              <!-- Checkbox -->
              <input
                type="checkbox"
                v-model="item.checked"
                class="mt-1 rounded border-slate-300 text-primary focus:ring-primary h-4 w-4"
              />

              <!-- Product Thumbnail -->
              <img
                :src="item.hinhAnh"
                alt="Product thumbnail"
                class="w-14 h-14 object-cover rounded-xl border border-slate-100 bg-white flex-shrink-0"
              />

              <!-- Detail info -->
              <div class="flex-1 min-w-0">
                <p class="font-semibold text-slate-800 text-sm truncate">{{ item.ten }}</p>
                <p class="text-xs text-slate-400">Phân loại: Màu {{ item.mauSac }}, Size {{ item.kichCo }}</p>
                <p class="text-xs font-bold text-slate-600 mt-1">{{ dinhDangTienViet(item.giaDonVi) }}</p>
              </div>

              <!-- Quantity selector -->
              <div v-if="item.checked" class="flex flex-col items-end gap-1.5">
                <div class="flex items-center border border-slate-200 rounded-lg overflow-hidden bg-white">
                  <button
                    type="button"
                    @click="item.soLuong > 1 && item.soLuong--"
                    class="px-2 py-0.5 text-slate-500 hover:bg-slate-50 font-bold"
                  >
                    -
                  </button>
                  <span class="px-2 text-xs font-bold text-slate-800">{{ item.soLuong }}</span>
                  <button
                    type="button"
                    @click="item.soLuong < item.maxSoLuong && item.soLuong++"
                    class="px-2 py-0.5 text-slate-500 hover:bg-slate-50 font-bold"
                  >
                    +
                  </button>
                </div>
                <span class="text-[10px] text-slate-400">Tối đa: {{ item.maxSoLuong }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 2. Lý do và mô tả -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1.5">
            <label class="text-xs md:text-sm font-bold text-slate-700">2. Lý do trả hàng</label>
            <select
              v-model="lyDoMa"
              class="return-form-control w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-primary focus:ring-primary bg-slate-50"
            >
              <option v-for="ld in dsLyDo" :key="ld.value" :value="ld.value">
                {{ ld.label }}
              </option>
            </select>
          </div>

          <div class="space-y-1.5">
            <label class="text-xs md:text-sm font-bold text-slate-700">3. Hình thức hoàn tiền mong muốn</label>
            <select
              v-model="hinhThucHoan"
              class="return-form-control w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-primary focus:ring-primary bg-slate-50"
            >
              <option v-for="ht in dsHinhThucHoan" :key="ht.value" :value="ht.value">
                {{ ht.label }}
              </option>
            </select>
          </div>
        </div>

        <div class="space-y-1.5">
          <label class="text-xs md:text-sm font-bold text-slate-700">4. Mô tả chi tiết lý do</label>
          <textarea
            v-model="moTa"
            rows="3"
            placeholder="Nhập mô tả cụ thể về tình trạng sản phẩm hoặc lý do trả hàng..."
            class="return-form-control w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-primary focus:ring-primary bg-slate-50 resize-none"
          ></textarea>
        </div>

        <!-- 3. Tải lên hình ảnh minh chứng -->
        <div class="space-y-3">
          <label class="text-xs md:text-sm font-bold text-slate-700 block">5. Hình ảnh bằng chứng (Tối thiểu 1 ảnh)</label>
          
          <div class="flex flex-wrap gap-3 items-center">
            <!-- Upload Button -->
            <button
              type="button"
              @click="triggerUpload"
              :disabled="dangTaiAnh"
              class="w-20 h-20 rounded-2xl border-2 border-dashed border-slate-200 hover:border-primary/50 bg-slate-50 flex flex-col items-center justify-center text-slate-400 hover:text-primary transition duration-300 group"
            >
              <UploadCloud class="w-6 h-6 group-hover:scale-110 transition duration-300" />
              <span class="text-[10px] font-semibold mt-1">{{ dangTaiAnh ? "Đang tải..." : "Tải ảnh" }}</span>
            </button>
            <input
              type="file"
              multiple
              @change="handleFileUpload"
              class="hidden"
              ref="fileInput"
              accept="image/*"
            />

            <!-- Images Preview -->
            <div
              v-for="(url, idx) in hinhAnhs"
              :key="idx"
              class="relative w-20 h-20 rounded-2xl border border-slate-100 overflow-hidden bg-slate-100 flex-shrink-0 group"
            >
              <img :src="url" alt="Preview Image" class="w-full h-full object-cover" />
              <button
                type="button"
                @click="removeImage(idx)"
                class="absolute inset-0 bg-black/40 flex items-center justify-center text-white opacity-0 group-hover:opacity-100 transition duration-300"
              >
                <Trash2 class="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal Footer -->
      <div class="px-6 py-4 border-t border-slate-100 flex justify-end gap-3 sticky bottom-0 bg-white z-10">
        <button
          type="button"
          @click="emit('close')"
          :disabled="dangGui"
          class="px-5 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold text-slate-600 hover:bg-slate-50 transition"
        >
          Hủy bỏ
        </button>
        <button
          type="button"
          @click="submitYeuCau"
          :disabled="dangGui || dangTaiAnh"
          class="px-6 py-2.5 rounded-xl bg-primary hover:bg-primary/95 text-sm font-bold text-white shadow-md disabled:opacity-50 transition"
        >
          {{ dangGui ? "Đang xử lý..." : "Gửi yêu cầu" }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.return-form-control {
  color: #334155 !important;
  -webkit-text-fill-color: #334155;
  color-scheme: light;
}

.return-form-control::placeholder {
  color: #94a3b8 !important;
  -webkit-text-fill-color: #94a3b8;
  opacity: 1;
}

.return-form-control option {
  color: #334155;
  background: #ffffff;
}

.scrollbar-none::-webkit-scrollbar {
  display: none;
}
.scrollbar-none {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
