<script setup>
import { ref, watch } from "vue";
import { X } from "lucide-vue-next";
import { taoKhachHang } from "../../../services/khach-hang";
import { isValidEmail, isValidVnPhone } from "../../../utils/validation";
import { showSuccess, showError } from "../../../utils/alert";

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  sdtGoiY: {
    type: String,
    default: "",
  }
});

const emit = defineEmits(["close", "created"]);

const form = ref({
  hoTen: "",
  sdt: "",
  email: "",
  gioiTinh: "",
});

const errors = ref({
  hoTen: "",
  sdt: "",
  email: "",
});

const isSubmitting = ref(false);

watch(() => props.open, (newVal) => {
  if (newVal) {
    form.value = {
      hoTen: "",
      sdt: props.sdtGoiY || "",
      email: "",
      gioiTinh: "",
    };
    errors.value = {
      hoTen: "",
      sdt: "",
      email: "",
    };
  }
});

function taoMatKhauNgauNhien() {
  const chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$!";
  let result = "";
  for (let i = 0; i < 10; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

function taoTenDangNhap() {
  const phone = form.value.sdt.replace(/\D/g, "");
  return phone ? `kh${phone}` : `kh${Date.now()}`;
}

function validate() {
  let isValid = true;
  errors.value = { hoTen: "", sdt: "", email: "" };

  if (!form.value.hoTen.trim()) {
    errors.value.hoTen = "Vui lòng nhập họ tên.";
    isValid = false;
  }

  if (!form.value.sdt.trim()) {
    errors.value.sdt = "Vui lòng nhập số điện thoại.";
    isValid = false;
  } else if (!isValidVnPhone(form.value.sdt)) {
    errors.value.sdt = "Số điện thoại không hợp lệ.";
    isValid = false;
  }

  if (form.value.email && !isValidEmail(form.value.email)) {
    errors.value.email = "Email không hợp lệ.";
    isValid = false;
  }

  return isValid;
}

async function handleSubmit() {
  if (!validate()) return;
  
  isSubmitting.value = true;
  
  try {
    const payload = {
      hoTen: form.value.hoTen.trim(),
      sdt: form.value.sdt.trim(),
      email: form.value.email.trim() || undefined,
      gioiTinh: form.value.gioiTinh !== "" ? Number(form.value.gioiTinh) : undefined,
      tenDangNhap: taoTenDangNhap(),
      matKhau: taoMatKhauNgauNhien(),
    };
    
    const khachHangMoi = await taoKhachHang(payload);
    showSuccess("Thêm khách hàng thành công!");
    emit("created", khachHangMoi);
    emit("close");
  } catch (error) {
    showError(error.message || "Không thể thêm khách hàng, vui lòng kiểm tra lại thông tin (SĐT hoặc Email có thể đã tồn tại).");
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
    <div class="relative w-full max-w-md bg-white dark:bg-slate-800 rounded-[24px] shadow-2xl animate-in fade-in zoom-in-95 duration-200">
      <div class="flex items-center justify-between p-5 border-b border-slate-100 dark:border-slate-700">
        <h3 class="text-lg font-bold text-slate-800 dark:text-white">Thêm nhanh khách hàng</h3>
        <button type="button" class="p-2 transition rounded-full hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-500" @click="emit('close')">
          <X class="w-5 h-5" />
        </button>
      </div>
      
      <div class="p-5 space-y-4">
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-slate-700 dark:text-slate-300">Họ và tên <span class="text-red-500">*</span></label>
          <input v-model="form.hoTen" type="text" class="w-full h-11 px-3 text-sm border rounded-xl border-slate-200 focus:border-red-500 outline-none" placeholder="Nhập họ tên" />
          <p v-if="errors.hoTen" class="text-xs text-red-500">{{ errors.hoTen }}</p>
        </div>
        
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-slate-700 dark:text-slate-300">Số điện thoại <span class="text-red-500">*</span></label>
          <input v-model="form.sdt" type="tel" class="w-full h-11 px-3 text-sm border rounded-xl border-slate-200 focus:border-red-500 outline-none" placeholder="Nhập số điện thoại" />
          <p v-if="errors.sdt" class="text-xs text-red-500">{{ errors.sdt }}</p>
        </div>
        
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-slate-700 dark:text-slate-300">Email</label>
          <input v-model="form.email" type="email" class="w-full h-11 px-3 text-sm border rounded-xl border-slate-200 focus:border-red-500 outline-none" placeholder="Nhập email (không bắt buộc)" />
          <p v-if="errors.email" class="text-xs text-red-500">{{ errors.email }}</p>
        </div>
        
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-slate-700 dark:text-slate-300">Giới tính</label>
          <select v-model="form.gioiTinh" class="w-full h-11 px-3 text-sm border rounded-xl border-slate-200 focus:border-red-500 outline-none bg-white">
            <option value="">-- Chọn giới tính --</option>
            <option value="1">Nam</option>
            <option value="0">Nữ</option>
            <option value="2">Khác</option>
          </select>
        </div>
      </div>
      
      <div class="flex gap-3 p-5 border-t border-slate-100 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50 rounded-b-[24px]">
        <button type="button" class="flex-1 h-11 font-semibold text-slate-600 bg-white border border-slate-200 rounded-xl hover:bg-slate-50" @click="emit('close')">
          Hủy
        </button>
        <button type="button" class="flex-1 h-11 font-semibold text-white bg-red-500 rounded-xl hover:bg-red-600 flex items-center justify-center disabled:opacity-50" :disabled="isSubmitting" @click="handleSubmit">
          <span v-if="isSubmitting" class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
          <span v-else>Lưu khách hàng</span>
        </button>
      </div>
    </div>
  </div>
</template>
