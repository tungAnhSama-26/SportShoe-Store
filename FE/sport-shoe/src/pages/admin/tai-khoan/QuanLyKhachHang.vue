<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { Search, Plus, FileDown, Eye, ChevronLeft, ChevronRight } from 'lucide-vue-next';
import ChiTietKhachHang from './ChiTietKhachHang.vue';

// Dữ liệu mẫu
const customers = ref([
  { id: 1, username: 'nhatnguyen', name: 'Nguyễn Văn Nhật', email: 'nhatnguyendzpro@gmail.com', phone: '0261748212', dob: '01/01/1990', gender: 'Nam', password: 'hashedpassword', status: 1, createdAt: '10/04/2024', updatedAt: '11/04/2024' },
  { id: 2, username: 'anhle123', name: 'Anh Lê', email: 'anhle@gmail.com', phone: '0562718362', dob: '20/12/2001', gender: 'Nam', password: 'hashedpassword', status: 1, createdAt: '10/04/2024', updatedAt: '11/04/2024' },
  { id: 3, username: 'tuongtrieu', name: 'Tưởng Triệu', email: 'tuongtrieu@gmail.com', phone: '0253718362', dob: '20/12/2000', gender: 'Nam', password: 'hashedpassword', status: 1, createdAt: '10/04/2024', updatedAt: '11/04/2024' },
  { id: 4, username: 'quynhtrang', name: 'Quỳnh Trang', email: 'quynhtrang123@gmail.com', phone: '0452716382', dob: '20/12/2001', gender: 'Nữ', password: 'hashedpassword', status: 1, createdAt: '10/04/2024', updatedAt: '11/04/2024' },
  { id: 5, username: 'thuyduong', name: 'Nguyễn Thị Thùy Dương', email: 'nguyenthithuyduong948@gmail.com', phone: '0647536475', dob: '20/12/2023', gender: 'Nữ', password: 'hashedpassword', status: 1, createdAt: '10/04/2024', updatedAt: '11/04/2024' },
  { id: 6, username: 'nguyenvana', name: 'Nguyễn Văn A', email: 'nguyenvana@gmail.com', phone: '0912345678', dob: '05/05/1995', gender: 'Nam', password: 'hashedpassword', status: 1, createdAt: '10/04/2024', updatedAt: '11/04/2024' },
]);

const selectedCustomer = ref(null);
const showCreateModal = ref(false);
const newCustomer = ref({
  name: '',
  email: '',
  phone: '',
  dob: '',
  gender: 'Nam',
  username: '',
  password: ''
});

const handleCreate = () => {
  if (!newCustomer.value.name || !newCustomer.value.email || !newCustomer.value.phone || !newCustomer.value.username || !newCustomer.value.password) {
    alert("Vui lòng điền đủ các trường bắt buộc!");
    return;
  }
  
  const newId = customers.value.length ? Math.max(...customers.value.map(c => c.id)) + 1 : 1;
  const formattedDob = newCustomer.value.dob ? newCustomer.value.dob.split('-').reverse().join('/') : '';
  const now = new Date();
  const dateStr = `${now.getDate().toString().padStart(2, '0')}/${(now.getMonth()+1).toString().padStart(2, '0')}/${now.getFullYear()}`;

  customers.value.unshift({
    id: newId,
    name: newCustomer.value.name,
    email: newCustomer.value.email,
    phone: newCustomer.value.phone,
    dob: formattedDob,
    gender: newCustomer.value.gender,
    username: newCustomer.value.username,
    password: newCustomer.value.password,
    status: 1,
    createdAt: dateStr,
    updatedAt: dateStr
  });
  
  newCustomer.value = { name: '', email: '', phone: '', dob: '', gender: 'Nam', username: '', password: '' };
  showCreateModal.value = false;
};

const searchQuery = ref('');
const genderFilter = ref('Tất cả');
const statusFilter = ref('Tất cả');

const currentPage = ref(1);
const itemsPerPage = ref(5);

// Reset trang về 1 khi lọc
watch([searchQuery, genderFilter, statusFilter, itemsPerPage], () => {
  currentPage.value = 1;
});

// Lọc khách hàng
const filteredCustomers = computed(() => {
  let result = customers.value;

  if (searchQuery.value) {
    const lowerQuery = searchQuery.value.toLowerCase();
    result = result.filter(c => 
      c.name.toLowerCase().includes(lowerQuery) ||
      c.username.toLowerCase().includes(lowerQuery) ||
      c.email.toLowerCase().includes(lowerQuery) ||
      c.phone.includes(lowerQuery)
    );
  }

  if (genderFilter.value !== 'Tất cả') {
    result = result.filter(c => c.gender === genderFilter.value);
  }

  if (statusFilter.value !== 'Tất cả') {
    const isStatus = statusFilter.value === 'Hoạt động' ? 1 : 0;
    result = result.filter(c => c.status === isStatus);
  }

  return result;
});

// Phân trang
const totalPages = computed(() => Math.ceil(filteredCustomers.value.length / itemsPerPage.value));

const paginatedCustomers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredCustomers.value.slice(start, end);
});

const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--;
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};
</script>

<template>
  <ChiTietKhachHang v-if="selectedCustomer" :customer="selectedCustomer" @back="selectedCustomer = null" />
  <div v-else class="p-6 bg-gray-50/50 min-h-screen">
    <div class="mb-4">
      <h1 class="text-xl font-bold text-gray-800">Khách hàng</h1>
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-gray-100 p-4">
      <!-- Top actions -->
      <div class="flex justify-between items-center mb-6">
        <div class="relative w-1/3 min-w-[350px]">
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="Tìm kiếm tên, sđt, email, tên đăng nhập" 
            class="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:ring-1 focus:ring-orange-500 focus:border-orange-500 transition-colors text-sm"
          />
          <Search class="absolute left-3 top-2.5 text-orange-400 w-5 h-5 opacity-70" />
        </div>
        
        <button @click="showCreateModal = true" class="flex items-center text-sm font-medium text-orange-500 border border-orange-500 px-4 py-2 rounded-md hover:bg-orange-50 transition-colors">
          <Plus class="w-4 h-4 mr-2" />
          Tạo khách hàng
        </button>
      </div>

      <!-- Filters -->
      <div class="flex items-center space-x-6 mb-6">
        <div class="flex items-center space-x-2 text-sm">
          <span class="font-semibold text-gray-700">Giới tính:</span>
          <select v-model="genderFilter" class="border border-gray-300 rounded-md px-3 py-1.5 focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 bg-white text-gray-700 cursor-pointer font-medium shadow-sm transition-colors">
            <option value="Tất cả">Tất cả</option>
            <option value="Nam">Nam</option>
            <option value="Nữ">Nữ</option>
          </select>
        </div>

        <div class="flex items-center space-x-2 text-sm">
          <span class="font-semibold text-gray-700">Trạng thái:</span>
          <select v-model="statusFilter" class="border border-gray-300 rounded-md px-3 py-1.5 focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 bg-white text-gray-700 cursor-pointer font-medium shadow-sm transition-colors">
            <option value="Tất cả">Tất cả</option>
            <option value="Hoạt động">Hoạt động</option>
            <option value="Ngừng hoạt động">Ngừng hoạt động</option>
          </select>
        </div>

        <button class="flex items-center text-sm font-medium text-orange-500 border border-orange-200 px-3 py-1.5 rounded-md hover:bg-orange-50 transition-colors">
          <FileDown class="w-4 h-4 mr-2" />
          Xuất Excel
        </button>
      </div>

      <!-- Table -->
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="border-b border-gray-200">
              <th class="py-4 px-2 font-semibold text-gray-800 text-sm whitespace-nowrap text-center">ID</th>
              <th class="py-4 px-2 font-semibold text-gray-800 text-sm whitespace-nowrap text-center">Tên đăng nhập</th>
              <th class="py-4 px-2 font-semibold text-gray-800 text-sm whitespace-nowrap text-center">Họ tên</th>
              <th class="py-4 px-2 font-semibold text-gray-800 text-sm whitespace-nowrap text-center">Email</th>
              <th class="py-4 px-2 font-semibold text-gray-800 text-sm whitespace-nowrap text-center">Số điện thoại</th>
              <th class="py-4 px-2 font-semibold text-gray-800 text-sm whitespace-nowrap text-center">Ngày sinh</th>
              <th class="py-4 px-2 font-semibold text-gray-800 text-sm whitespace-nowrap text-center">Giới tính</th>
              <th class="py-4 px-2 font-semibold text-gray-800 text-sm whitespace-nowrap text-center">Mật khẩu</th>
              <th class="py-4 px-2 font-semibold text-gray-800 text-sm whitespace-nowrap text-center">Trạng thái</th>
              <th class="py-4 px-2 font-semibold text-gray-800 text-sm whitespace-nowrap text-center">Ngày tạo</th>
              <th class="py-4 px-2 font-semibold text-gray-800 text-sm whitespace-nowrap text-center">Ngày cập nhật</th>
              <th class="py-4 px-2 font-semibold text-gray-800 text-sm whitespace-nowrap text-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="paginatedCustomers.length === 0">
              <td colspan="12" class="p-8 text-center text-gray-500">
                Không tìm thấy khách hàng nào.
              </td>
            </tr>
            <tr v-for="item in paginatedCustomers" :key="item.id" class="border-b border-gray-100 hover:bg-gray-50/50 transition-colors">
              <td class="py-4 px-2 text-sm text-gray-600 text-center">{{ item.id }}</td>
              <td class="py-4 px-2 text-sm text-gray-600 text-center">{{ item.username }}</td>
              <td class="py-4 px-2 text-sm text-gray-800 text-center">{{ item.name }}</td>
              <td class="py-4 px-2 text-sm text-gray-600 text-center">{{ item.email }}</td>
              <td class="py-4 px-2 text-sm text-gray-600 text-center">{{ item.phone }}</td>
              <td class="py-4 px-2 text-sm text-gray-600 text-center">{{ item.dob }}</td>
              <td class="py-4 px-2 text-sm text-gray-600 text-center">{{ item.gender }}</td>
              <td class="py-4 px-2 text-sm text-gray-400 text-center">••••••••</td>
              <td class="py-4 px-2 text-center">
                <span 
                  v-if="item.status === 1" 
                  class="inline-block px-3 py-1 text-xs font-medium text-green-600 bg-green-50 border border-green-200 rounded-full w-28"
                >
                  Hoạt động
                </span>
                <span 
                  v-else 
                  class="inline-block px-3 py-1 text-xs font-medium text-gray-600 bg-gray-50 border border-gray-200 rounded-full w-28"
                >
                  Ngừng hoạt động
                </span>
              </td>
              <td class="py-4 px-2 text-sm text-gray-600 text-center">{{ item.createdAt }}</td>
              <td class="py-4 px-2 text-sm text-gray-600 text-center">{{ item.updatedAt }}</td>
              <td class="py-4 px-2 flex justify-center items-center h-full">
                <button @click="selectedCustomer = item" class="p-1.5 text-orange-500 hover:bg-orange-50 rounded-full transition-colors" title="Thao tác">
                  <Eye class="w-5 h-5 opacity-80" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 0" class="mt-6 flex flex-col md:flex-row items-center justify-between text-sm text-gray-600">
        <div class="flex items-center mb-4 md:mb-0 space-x-2">
          <span>Xem</span>
          <select v-model="itemsPerPage" class="border border-gray-300 rounded-md px-2 py-1 focus:outline-none focus:border-orange-500 outline-none">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          <span>Khách hàng</span>
        </div>
        
        <div class="flex items-center space-x-2">
          <button 
            @click="prevPage" 
            :disabled="currentPage === 1"
            class="w-8 h-8 flex items-center justify-center rounded-full border border-gray-200 hover:bg-gray-50 disabled:opacity-50 disabled:hover:bg-transparent transition-colors text-gray-400"
          >
            <ChevronLeft class="w-4 h-4" />
          </button>
          
          <button 
            v-for="page in totalPages" 
            :key="page"
            @click="currentPage = page"
            class="w-8 h-8 flex items-center justify-center rounded-full text-sm font-medium transition-colors"
            :class="currentPage === page ? 'text-orange-500 border border-orange-500 bg-orange-50' : 'text-gray-600 hover:bg-gray-50 bg-transparent'"
          >
            {{ page }}
          </button>

          <button 
            @click="nextPage"
            :disabled="currentPage === totalPages"
            class="w-8 h-8 flex items-center justify-center rounded-full border border-gray-200 hover:bg-gray-50 disabled:opacity-50 disabled:hover:bg-transparent transition-colors text-gray-400"
          >
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>
      </div>
      </div>
    </div>
    
  <!-- Create Customer Modal -->
  <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[60]">
    <div class="bg-white rounded-lg w-full max-w-2xl overflow-hidden shadow-2xl transform transition-all m-4">
      <div class="flex justify-between items-center p-6 border-b border-gray-100">
        <h2 class="text-xl font-bold text-gray-800">Thêm mới khách hàng</h2>
        <button @click="showCreateModal = false" class="text-gray-400 hover:text-red-500 transition-colors rounded-full p-1 hover:bg-red-50">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
        </button>
      </div>
      
      <div class="p-6">
        <div class="grid grid-cols-2 gap-5">
          <div>
            <label class="block text-sm mb-1.5 text-gray-600 font-medium">Họ tên <span class="text-red-500">*</span></label>
            <input type="text" v-model="newCustomer.name" placeholder="Ví dụ: Nguyễn Văn A" class="w-full px-3 py-2.5 border border-gray-300 rounded-md focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 text-sm transition-colors" />
          </div>
          <div>
            <label class="block text-sm mb-1.5 text-gray-600 font-medium">Email <span class="text-red-500">*</span></label>
            <input type="email" v-model="newCustomer.email" placeholder="example@gmail.com" class="w-full px-3 py-2.5 border border-gray-300 rounded-md focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 text-sm transition-colors" />
          </div>
          <div>
            <label class="block text-sm mb-1.5 text-gray-600 font-medium">Số điện thoại <span class="text-red-500">*</span></label>
            <input type="text" v-model="newCustomer.phone" placeholder="0901234567" class="w-full px-3 py-2.5 border border-gray-300 rounded-md focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 text-sm transition-colors" />
          </div>
          <div>
            <label class="block text-sm mb-1.5 text-gray-600 font-medium">Ngày sinh</label>
            <input type="date" v-model="newCustomer.dob" class="w-full px-3 py-2.5 border border-gray-300 rounded-md focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 text-sm transition-colors" />
          </div>
          <div>
            <label class="block text-sm mb-1.5 text-gray-600 font-medium">Giới tính</label>
            <select v-model="newCustomer.gender" class="w-full px-3 py-2.5 border border-gray-300 rounded-md focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 text-sm transition-colors bg-white cursor-pointer">
              <option value="Nam">Nam</option>
              <option value="Nữ">Nữ</option>
            </select>
          </div>
          <div>
            <label class="block text-sm mb-1.5 text-gray-600 font-medium">Tên đăng nhập <span class="text-red-500">*</span></label>
            <input type="text" v-model="newCustomer.username" placeholder="Username" class="w-full px-3 py-2.5 border border-gray-300 rounded-md focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 text-sm transition-colors" />
          </div>
          <div class="col-span-2">
            <label class="block text-sm mb-1.5 text-gray-600 font-medium">Mật khẩu <span class="text-red-500">*</span></label>
            <input type="password" v-model="newCustomer.password" placeholder="••••••••" class="w-full px-3 py-2.5 border border-gray-300 rounded-md focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 text-sm transition-colors" />
          </div>
        </div>
      </div>
      
      <div class="px-6 py-4 bg-gray-50 flex justify-end space-x-3 border-t border-gray-100">
        <button @click="showCreateModal = false" class="px-5 py-2.5 border border-gray-300 text-gray-700 bg-white rounded-lg hover:bg-gray-100 transition-colors text-sm font-medium shadow-sm">Hủy</button>
        <button @click="handleCreate" class="px-5 py-2.5 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition-colors text-sm font-medium shadow-sm">Thêm mới</button>
      </div>
    </div>
  </div>
</template>