<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { ChevronDown, Star } from 'lucide-vue-next';

const props = defineProps({
  customer: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['back']);

const fileInput = ref<HTMLInputElement | null>(null);
const avatarUrl = ref<string | null>(null);
const showSuccessToast = ref(false);

const provinces = ref<any[]>([]);
const districts = ref<any[]>([]);
const wards = ref<any[]>([]);

const selectedProvinceCode = ref('');
const selectedDistrictCode = ref('');
const selectedWardCode = ref('');

onMounted(async () => {
  try {
    const res = await fetch('https://provinces.open-api.vn/api/p/');
    provinces.value = await res.json();
  } catch (error) {
    console.error('Lỗi khi tải tỉnh/thành:', error);
  }
});

watch(selectedProvinceCode, async (newCode) => {
  districts.value = [];
  wards.value = [];
  selectedDistrictCode.value = '';
  selectedWardCode.value = '';
  if (newCode) {
    try {
      const res = await fetch(`https://provinces.open-api.vn/api/p/${newCode}?depth=2`);
      const data = await res.json();
      districts.value = data.districts || [];
    } catch (error) {
      console.error('Lỗi khi tải quận/huyện:', error);
    }
  }
});

watch(selectedDistrictCode, async (newCode) => {
  wards.value = [];
  selectedWardCode.value = '';
  if (newCode) {
    try {
      const res = await fetch(`https://provinces.open-api.vn/api/d/${newCode}?depth=2`);
      const data = await res.json();
      wards.value = data.wards || [];
    } catch (error) {
      console.error('Lỗi khi tải xã/phường:', error);
    }
  }
});

const triggerFileInput = () => {
  if (fileInput.value) {
    fileInput.value.click();
  }
};

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    const file = target.files[0];
    avatarUrl.value = URL.createObjectURL(file);
  }
};

const updateCustomer = () => {
  showSuccessToast.value = true;
  setTimeout(() => {
    showSuccessToast.value = false;
    emit('back');
  }, 1500);
};
</script>

<template>
  <div class="p-6 bg-gray-50/50 min-h-screen relative">
    <!-- Toast Notification -->
    <div 
      v-if="showSuccessToast" 
      class="fixed top-6 right-6 bg-green-50 text-green-600 border border-green-200 px-6 py-3 rounded-lg shadow-lg flex items-center space-x-2 z-50 animate-bounce"
    >
      <svg class="w-6 h-6 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
      </svg>
      <span class="font-medium">Cập nhật khách hàng thành công!</span>
    </div>

    <!-- Breadcrumb -->
    <div class="mb-6 flex items-center text-sm">
      <button @click="emit('back')" class="font-bold text-gray-800 hover:text-orange-500 transition-colors">Khách hàng</button>
      <span class="mx-2 text-gray-400">/</span>
      <span class="text-gray-500">KH{{ customer.id }}</span>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <!-- Left Column: Customer Info -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-100 p-6 flex flex-col">
        <h2 class="text-xl font-bold text-gray-800 pb-4 border-b border-gray-100 mb-6">Thông tin khách hàng</h2>
        
        <div class="flex justify-center mb-8 relative">
          <input 
            type="file" 
            ref="fileInput" 
            class="hidden" 
            accept="image/*" 
            @change="handleFileChange" 
          />
          <div 
            @click="triggerFileInput"
            class="w-32 h-32 rounded-full border-2 border-dashed border-gray-300 flex items-center justify-center text-gray-500 cursor-pointer hover:bg-gray-50 transition-colors overflow-hidden relative group"
          >
            <img v-if="avatarUrl" :src="avatarUrl" class="w-full h-full object-cover" />
            <span v-else class="text-sm">Chọn ảnh</span>
            <div v-if="avatarUrl" class="absolute inset-0 bg-black bg-opacity-40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
              <span class="text-white text-xs">Đổi ảnh</span>
            </div>
          </div>
        </div>

        <div class="space-y-4 flex-grow">
          <div>
            <label class="block text-sm mb-1 text-gray-500"><span class="text-red-500">*</span>Tên khách hàng</label>
            <input type="text" :value="customer.name" readonly class="w-full px-3 py-2 border border-gray-200 rounded-md bg-gray-50 text-gray-700 outline-none hover:border-red-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 transition-colors" />
          </div>
          <div>
            <label class="block text-sm mb-1 text-gray-500"><span class="text-red-500">*</span>Email</label>
            <input type="text" :value="customer.email" readonly class="w-full px-3 py-2 border border-gray-200 rounded-md bg-gray-50 text-gray-700 outline-none hover:border-red-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 transition-colors" />
          </div>
          <div>
            <label class="block text-sm mb-1 text-gray-500"><span class="text-red-500">*</span>Số điện thoại</label>
            <input type="text" :value="customer.phone" readonly class="w-full px-3 py-2 border border-gray-200 rounded-md bg-gray-50 text-gray-700 outline-none hover:border-red-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 transition-colors" />
          </div>
        </div>
      </div>

      <!-- Right Column: Addresses -->
      <div class="md:col-span-2 bg-white rounded-lg shadow-sm border border-gray-100 p-6 flex flex-col">
        <h2 class="text-xl font-bold text-gray-800 pb-4 border-b border-gray-100 mb-6">Danh sách địa chỉ</h2>

        <div class="mb-4 flex-grow">
          <div class="flex justify-between items-center mb-4 cursor-pointer">
            <span class="font-semibold text-gray-700 text-sm">Địa chỉ 1</span>
            <ChevronDown class="w-5 h-5 text-gray-400" />
          </div>

          <div class="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label class="block text-sm mb-1 text-gray-500"><span class="text-red-500">*</span>Tên</label>
              <input type="text" :value="customer.name" class="w-full px-3 py-2 border border-gray-300 rounded-md outline-none hover:border-red-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 text-sm transition-colors" />
            </div>
            <div>
              <label class="block text-sm mb-1 text-gray-500"><span class="text-red-500">*</span>Số điện thoại</label>
              <input type="text" :value="customer.phone" class="w-full px-3 py-2 border border-gray-300 rounded-md outline-none hover:border-red-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 text-sm transition-colors" />
            </div>
          </div>

          <div class="grid grid-cols-3 gap-4 mb-4">
            <div>
              <label class="block text-sm mb-1 text-gray-500"><span class="text-red-500">*</span>Tỉnh/thành phố</label>
              <select v-model="selectedProvinceCode" class="w-full px-3 py-2 border border-gray-300 rounded-md outline-none hover:border-red-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 text-sm bg-white cursor-pointer transition-colors disabled:bg-gray-100 disabled:cursor-not-allowed">
                <option value="">Chọn tỉnh/thành phố</option>
                <option v-for="p in provinces" :key="p.code" :value="p.code">{{ p.name }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm mb-1 text-gray-500"><span class="text-red-500">*</span>Quận/huyện</label>
              <select v-model="selectedDistrictCode" :disabled="!selectedProvinceCode" class="w-full px-3 py-2 border border-gray-300 rounded-md outline-none hover:border-red-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 text-sm bg-white cursor-pointer transition-colors disabled:bg-gray-100 disabled:cursor-not-allowed">
                <option value="">Chọn quận/huyện</option>
                <option v-for="d in districts" :key="d.code" :value="d.code">{{ d.name }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm mb-1 text-gray-500"><span class="text-red-500">*</span>Xã/phường/thị trấn</label>
              <select v-model="selectedWardCode" :disabled="!selectedDistrictCode" class="w-full px-3 py-2 border border-gray-300 rounded-md outline-none hover:border-red-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 text-sm bg-white cursor-pointer transition-colors disabled:bg-gray-100 disabled:cursor-not-allowed">
                <option value="">Chọn xã/phường</option>
                <option v-for="w in wards" :key="w.code" :value="w.code">{{ w.name }}</option>
              </select>
            </div>
          </div>

          <div class="mb-4">
            <label class="block text-sm mb-1 text-gray-500"><span class="text-red-500">*</span>Địa chỉ cụ thể</label>
            <input type="text" value="nhà 36" class="w-full px-3 py-2 border border-gray-300 rounded-md outline-none hover:border-red-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 text-sm transition-colors" />
          </div>

          <div class="flex items-center mt-2">
            <Star class="w-5 h-5 text-gray-300 cursor-pointer hover:text-orange-400" />
          </div>
        </div>
      </div>
    </div>

    <!-- Update Button -->
    <div class="mt-6 flex justify-end">
      <button 
        @click="updateCustomer" 
        class="bg-orange-500 text-white px-6 py-2 rounded-lg font-medium hover:bg-orange-600 transition-colors shadow-sm"
      >
        Cập nhật khách hàng
      </button>
    </div>
  </div>
</template>
