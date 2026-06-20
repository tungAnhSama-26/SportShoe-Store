<script setup>
import { ref, computed } from 'vue';
import { 
  Filter, Plus, RotateCcw, Clock, Eye, X, Search 
} from 'lucide-vue-next';
import { showSuccess, showError, showConfirm } from "../../../utils/alert.js";
import TimePicker24h from "../../../components/common/TimePicker24h.vue";

// --- Dữ liệu Mock ---
const danhSachCaLam = ref([
  { id: 'CA00002', ten: 'Ca chiều', gioBatDau: '13:30', gioKetThuc: '17:30', trangThai: true },
  { id: 'CA00001', ten: 'Ca sáng', gioBatDau: '08:00', gioKetThuc: '12:00', trangThai: true },
]);

// --- Bộ lọc ---
const filters = ref({
  timKiem: '',
  gioBatDau: '',
  gioKetThuc: '',
  trangThai: 'all' // 'all', 'active', 'inactive'
});

const dsHienThi = computed(() => {
  return danhSachCaLam.value.filter(ca => {
    // Tên/Mã
    if (filters.value.timKiem) {
      const keyword = filters.value.timKiem.toLowerCase();
      if (!ca.ten.toLowerCase().includes(keyword) && !ca.id.toLowerCase().includes(keyword)) {
        return false;
      }
    }
    // Trạng thái
    if (filters.value.trangThai === 'active' && !ca.trangThai) return false;
    if (filters.value.trangThai === 'inactive' && ca.trangThai) return false;
    // Giờ (Lọc đơn giản nếu có chọn)
    if (filters.value.gioBatDau && ca.gioBatDau < filters.value.gioBatDau) return false;
    if (filters.value.gioKetThuc && ca.gioKetThuc > filters.value.gioKetThuc) return false;

    return true;
  });
});

function lamMoi() {
  filters.value = { timKiem: '', gioBatDau: '', gioKetThuc: '', trangThai: 'all' };
}

function toggleTrangThai(ca) {
  ca.trangThai = !ca.trangThai;
  showSuccess(`Đã thay đổi trạng thái ca ${ca.ten}`);
}

// --- Modal Thêm/Sửa Ca ---
const showModalTaoCa = ref(false);
const isEdit = ref(false);
const formTaoCa = ref({
  tenCa: "",
  gioBatDau: "",
  gioKetThuc: "",
  moTa: ""
});
const formErrors = ref({
  tenCa: "",
  gioBatDau: "",
  gioKetThuc: ""
});

function moModalTaoCa() {
  isEdit.value = false;
  formTaoCa.value = {
    tenCa: "",
    gioBatDau: "",
    gioKetThuc: "",
    moTa: ""
  };
  formErrors.value = { tenCa: "", gioBatDau: "", gioKetThuc: "" };
  showModalTaoCa.value = true;
}

function moModalSuaCa(ca) {
  isEdit.value = true;
  formTaoCa.value = {
    id: ca.id,
    tenCa: ca.ten,
    gioBatDau: ca.gioBatDau,
    gioKetThuc: ca.gioKetThuc,
    moTa: ""
  };
  formErrors.value = { tenCa: "", gioBatDau: "", gioKetThuc: "" };
  showModalTaoCa.value = true;
}

function huyTaoCa() {
  showModalTaoCa.value = false;
}

async function luuTaoCa() {
  // Loại bỏ khoảng trắng đầu cuối
  formTaoCa.value.tenCa = formTaoCa.value.tenCa.trim();

  let isValid = true;
  formErrors.value = { tenCa: "", gioBatDau: "", gioKetThuc: "" };

  if (!formTaoCa.value.tenCa) {
    formErrors.value.tenCa = "Vui lòng nhập tên ca làm việc.";
    isValid = false;
  } else {
    // Validate không chứa kí tự đặc biệt
    const regexTenCa = /^[a-zA-ZÀ-ỹ0-9 ]+$/;
    if (!regexTenCa.test(formTaoCa.value.tenCa)) {
      formErrors.value.tenCa = "Tên ca không được chứa ký tự đặc biệt.";
      isValid = false;
    }
  }

  if (!formTaoCa.value.gioBatDau) {
    formErrors.value.gioBatDau = "Vui lòng chọn thời gian bắt đầu.";
    isValid = false;
  }
  if (!formTaoCa.value.gioKetThuc) {
    formErrors.value.gioKetThuc = "Vui lòng chọn thời gian kết thúc.";
    isValid = false;
  }

  if (!isValid) return;

  const actionText = isEdit.value ? "cập nhật" : "thêm mới";
  const confirmed = await showConfirm(`Bạn có chắc chắn muốn ${actionText} ca làm việc này không?`, "Xác nhận");
  if (!confirmed) return;

  if (isEdit.value) {
    const idx = danhSachCaLam.value.findIndex(c => c.id === formTaoCa.value.id);
    if (idx !== -1) {
      danhSachCaLam.value[idx].ten = formTaoCa.value.tenCa;
      danhSachCaLam.value[idx].gioBatDau = formTaoCa.value.gioBatDau;
      danhSachCaLam.value[idx].gioKetThuc = formTaoCa.value.gioKetThuc;
    }
    showSuccess("Cập nhật ca làm việc thành công!");
  } else {
    const nextId = "CA" + String(danhSachCaLam.value.length + 1).padStart(5, '0');
    danhSachCaLam.value.unshift({
      id: nextId,
      ten: formTaoCa.value.tenCa,
      gioBatDau: formTaoCa.value.gioBatDau,
      gioKetThuc: formTaoCa.value.gioKetThuc,
      trangThai: true
    });
    showSuccess("Thêm mới ca làm việc thành công!");
  }

  showModalTaoCa.value = false;
}
</script>

<template>
  <div class="space-y-6">
    
    <!-- Bộ lọc tìm kiếm -->
    <div class="bg-white rounded-xl shadow-sm border border-slate-100 p-5">
      <div class="flex items-center gap-2 mb-4">
        <Filter class="w-5 h-5 text-slate-600" />
        <h3 class="text-[16px] font-bold text-slate-800">Bộ lọc tìm kiếm</h3>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <!-- Tìm kiếm chung -->
        <div class="space-y-2">
          <label class="text-[13px] font-medium text-slate-600">Tìm kiếm chung</label>
          <div class="relative">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input 
              v-model="filters.timKiem"
              type="text" 
              placeholder="Nhập tên ca, mã ca..." 
              class="w-full h-10 pl-9 pr-3 text-[14px] border border-slate-200 rounded-lg focus:outline-none focus:border-rose-400 transition"
            />
          </div>
        </div>

        <!-- Thời gian bắt đầu -->
        <div class="space-y-2">
          <label class="text-[13px] font-medium text-slate-600">Thời gian bắt đầu</label>
          <div class="w-full h-10 border border-slate-200 rounded-lg focus-within:border-rose-400 focus-within:ring-1 focus-within:ring-rose-400 transition overflow-hidden">
            <TimePicker24h v-model="filters.gioBatDau" />
          </div>
        </div>

        <!-- Thời gian kết thúc -->
        <div class="space-y-2">
          <label class="text-[13px] font-medium text-slate-600">Thời gian kết thúc</label>
          <div class="w-full h-10 border border-slate-200 rounded-lg focus-within:border-rose-400 focus-within:ring-1 focus-within:ring-rose-400 transition overflow-hidden">
            <TimePicker24h v-model="filters.gioKetThuc" />
          </div>
        </div>

        <!-- Trạng thái -->
        <div class="space-y-2">
          <label class="text-[13px] font-medium text-slate-600">Trạng thái</label>
          <select 
            v-model="filters.trangThai"
            class="w-full h-10 px-3 text-[14px] border border-slate-200 rounded-lg focus:outline-none focus:border-rose-400 transition bg-white"
          >
            <option value="all">Tất cả</option>
            <option value="active">Hoạt động</option>
            <option value="inactive">Ngừng hoạt động</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Danh sách ca làm việc -->
    <div class="bg-white rounded-xl shadow-sm border border-slate-100 p-5">
      <div class="flex items-center justify-between mb-6">
        <h3 class="text-[16px] font-bold text-slate-800">Danh sách ca làm việc</h3>
        <div class="flex items-center gap-3">
          <button @click="lamMoi" class="h-9 px-4 flex items-center gap-2 rounded-lg border border-rose-200 bg-rose-50 hover:bg-rose-100 text-rose-500 text-[14px] font-medium transition shadow-sm">
            <RotateCcw class="w-4 h-4" />
            <span>Đặt lại bộ lọc</span>
          </button>
          <button @click="moModalTaoCa" class="h-9 px-4 flex items-center gap-2 rounded-lg bg-rose-500 hover:bg-rose-600 text-white text-[14px] font-medium transition shadow-sm">
            <Plus class="w-4 h-4" />
            <span>Thêm lịch ca làm</span>
          </button>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-slate-50/50">
              <th class="py-3 px-4 text-[13px] font-semibold text-slate-500 w-16">STT</th>
              <th class="py-3 px-4 text-[13px] font-semibold text-slate-500 w-56">Thông tin ca</th>
              <th class="py-3 px-4 text-[13px] font-semibold text-slate-500 text-center w-32">Giờ bắt đầu</th>
              <th class="py-3 px-4 text-[13px] font-semibold text-slate-500 text-center w-32">Giờ kết thúc</th>
              <th class="py-3 px-4 text-[13px] font-semibold text-slate-500 text-center w-28">Trạng thái</th>
              <th class="py-3 px-4 text-[13px] font-semibold text-slate-500 text-center w-28 whitespace-nowrap">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(ca, idx) in dsHienThi" :key="ca.id" class="border-b border-slate-100 hover:bg-slate-50/50 transition">
              <td class="py-4 px-4 text-[14px] text-slate-600">{{ idx + 1 }}</td>
              <td class="py-4 px-4">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full bg-blue-50 flex items-center justify-center text-blue-500">
                    <Clock class="w-5 h-5" />
                  </div>
                  <div>
                    <div class="text-[14px] font-bold text-slate-800">{{ ca.ten }}</div>
                    <div class="text-[12px] text-slate-500 mt-0.5">{{ ca.id }}</div>
                  </div>
                </div>
              </td>
              <td class="py-4 px-4 text-center">
                <span class="inline-block px-3 py-1.5 rounded-full bg-emerald-100/50 text-emerald-600 text-[13px] font-bold border border-emerald-100">
                  {{ ca.gioBatDau }}
                </span>
              </td>
              <td class="py-4 px-4 text-center">
                <span class="inline-block px-3 py-1.5 rounded-full bg-rose-100/50 text-rose-500 text-[13px] font-bold border border-rose-100">
                  {{ ca.gioKetThuc }}
                </span>
              </td>
              <td class="py-4 px-4 text-center">
                <button 
                  @click="toggleTrangThai(ca)"
                  class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none"
                  :class="ca.trangThai ? 'bg-rose-500' : 'bg-slate-200'"
                >
                  <span 
                    class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform shadow-sm"
                    :class="ca.trangThai ? 'translate-x-6' : 'translate-x-1'"
                  />
                </button>
              </td>
              <td class="py-4 px-4 text-center">
                <button @click="moModalSuaCa(ca)" class="w-8 h-8 inline-flex items-center justify-center rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-50 transition shadow-sm">
                  <Eye class="w-4 h-4" />
                </button>
              </td>
            </tr>
            <tr v-if="dsHienThi.length === 0">
              <td colspan="6" class="py-8 text-center text-[14px] text-slate-500">
                Không tìm thấy ca làm việc nào phù hợp.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Thêm/Sửa Ca Làm Việc -->
    <Teleport to="body">
      <div v-if="showModalTaoCa" class="fixed inset-0 z-[120] flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm" @click="huyTaoCa"></div>
        <div class="relative w-full max-w-[500px] overflow-hidden rounded-[16px] bg-white shadow-xl animate-in zoom-in-95 duration-200">
          
          <!-- Header -->
          <div class="p-6 pb-4">
            <div class="flex items-center justify-between">
              <h3 class="text-[18px] font-bold text-slate-800">
                {{ isEdit ? 'Cập nhật ca làm việc' : 'Thêm mới ca làm việc' }}
              </h3>
              <button @click="huyTaoCa" class="text-slate-400 hover:text-slate-600 transition">
                <X class="w-5 h-5" />
              </button>
            </div>
          </div>

          <!-- Body -->
          <div class="p-6 pt-0 space-y-5">
            
            <div class="space-y-1.5">
              <label class="text-[14px] font-medium text-slate-700">Tên ca <span class="text-rose-500">*</span></label>
              <input 
                v-model="formTaoCa.tenCa" 
                type="text" 
                class="w-full h-11 px-3 text-[14px] border border-slate-200 rounded-xl focus:outline-none focus:border-slate-400 transition placeholder-slate-400"
                :class="{'border-rose-500 focus:border-rose-500': formErrors.tenCa}"
                placeholder="VD: Ca sáng, Ca chiều..."
                @input="formErrors.tenCa = ''"
              />
              <p v-if="formErrors.tenCa" class="text-[12px] text-rose-500 mt-1">{{ formErrors.tenCa }}</p>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-[14px] font-medium text-slate-700">Giờ bắt đầu <span class="text-rose-500">*</span></label>
                <input 
                  type="time" 
                  v-model="formTaoCa.gioBatDau" 
                  class="w-full h-11 px-3 text-[14px] border border-slate-200 rounded-xl focus:outline-none focus:border-slate-400 transition bg-white" 
                  :class="{'border-rose-500 focus:border-rose-500': formErrors.gioBatDau}"
                  @input="formErrors.gioBatDau = ''"
                />
                <p v-if="formErrors.gioBatDau" class="text-[12px] text-rose-500 mt-1">{{ formErrors.gioBatDau }}</p>
              </div>
              
              <div class="space-y-1.5">
                <label class="text-[14px] font-medium text-slate-700">Giờ kết thúc <span class="text-rose-500">*</span></label>
                <input 
                  type="time" 
                  v-model="formTaoCa.gioKetThuc" 
                  class="w-full h-11 px-3 text-[14px] border border-slate-200 rounded-xl focus:outline-none focus:border-slate-400 transition bg-white" 
                  :class="{'border-rose-500 focus:border-rose-500': formErrors.gioKetThuc}"
                  @input="formErrors.gioKetThuc = ''"
                />
                <p v-if="formErrors.gioKetThuc" class="text-[12px] text-rose-500 mt-1">{{ formErrors.gioKetThuc }}</p>
              </div>
            </div>

            <div class="space-y-1.5">
              <label class="text-[14px] font-medium text-slate-700">Mô tả</label>
              <textarea 
                v-model="formTaoCa.moTa" 
                rows="4"
                class="w-full p-3 text-[14px] border border-slate-200 rounded-xl focus:outline-none focus:border-slate-400 transition placeholder-slate-400 resize-none"
                placeholder="Ghi chú thêm về ca làm việc..."
              ></textarea>
            </div>
            
          </div>

          <!-- Footer -->
          <div class="flex items-center justify-end gap-3 p-6 pt-2">
            <button @click="huyTaoCa" class="px-6 py-2.5 rounded-xl border border-slate-300 bg-white text-[14px] font-medium text-slate-700 hover:bg-slate-50 transition shadow-sm">
              Hủy bỏ
            </button>
            <button 
              @click="luuTaoCa" 
              class="px-6 py-2.5 rounded-xl bg-[#e61d4a] text-white text-[14px] font-medium transition shadow-sm hover:bg-[#cf1018]"
            >
              {{ isEdit ? 'Cập nhật' : 'Thêm mới' }}
            </button>
          </div>

        </div>
      </div>
    </Teleport>
  </div>
</template>
