<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Plus, Pencil, Trash2, Eye, X, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { mauSacApi } from '../../../services/danh-muc-api'

const items = ref([])
const totalItems = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const loading = ref(false)
const keyword = ref('')

const toast = reactive({ show: false, message: '', type: 'success' })
function showToast(msg, type = 'success') { toast.message = msg; toast.type = type; toast.show = true; setTimeout(() => { toast.show = false }, 3000) }

async function loadData(page = 0) {
  loading.value = true
  try {
    const res = await mauSacApi.list(keyword.value || undefined, page)
    items.value = res.items; totalItems.value = res.totalItems; totalPages.value = res.totalPages; currentPage.value = res.page
  } catch (e) { showToast(e.message || 'Lỗi tải dữ liệu', 'error') }
  finally { loading.value = false }
}

function doSearch() { loadData(0) }
onMounted(() => loadData())

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(0, currentPage.value - 2)
  const end = Math.min(totalPages.value - 1, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const showModal = ref(false)
const modalMode = ref('add')
const saving = ref(false)
const selectedItem = ref(null)
const form = reactive({ ma: '', ten: '', maMauHex: '#000000' })
const errors = reactive({})

function clearForm() { Object.assign(form, { ma: '', ten: '', maMauHex: '#000000' }); Object.keys(errors).forEach(k => delete errors[k]) }
function openAdd() { clearForm(); modalMode.value = 'add'; showModal.value = true }
function openEdit(item) {
  clearForm()
  Object.assign(form, { ma: item.ma, ten: item.ten, maMauHex: item.maMauHex || '#000000' })
  selectedItem.value = item; modalMode.value = 'edit'; showModal.value = true
}
function openView(item) {
  clearForm()
  Object.assign(form, { ma: item.ma, ten: item.ten, maMauHex: item.maMauHex || '#000000' })
  selectedItem.value = item; modalMode.value = 'view'; showModal.value = true
}

function validate() {
  Object.keys(errors).forEach(k => delete errors[k])
  if (!form.ma.trim()) errors.ma = 'Mã không được để trống'
  if (!form.ten.trim()) errors.ten = 'Tên không được để trống'
  return Object.keys(errors).length === 0
}

async function handleSave() {
  if (!validate()) return
  saving.value = true
  try {
    const body = { ma: form.ma.trim(), ten: form.ten.trim(), maMauHex: form.maMauHex || null }
    if (modalMode.value === 'add') await mauSacApi.create(body)
    else await mauSacApi.update(selectedItem.value.id, body)
    showToast(modalMode.value === 'add' ? 'Tạo thành công' : 'Cập nhật thành công')
    showModal.value = false; loadData(currentPage.value)
  } catch (e) { showToast(e.message || 'Có lỗi xảy ra', 'error') }
  finally { saving.value = false }
}

async function handleDelete(item) {
  if (!confirm(`Xóa màu sắc "${item.ten}"?`)) return
  try { await mauSacApi.delete(item.id); showToast('Xóa thành công'); loadData(currentPage.value) }
  catch (e) { showToast(e.message || 'Lỗi xóa', 'error') }
}

async function handleToggleStatus(item) {
  try { await mauSacApi.toggleStatus(item.id, item.trangThai === 1 ? 0 : 1); showToast('Cập nhật trạng thái thành công'); loadData(currentPage.value) }
  catch (e) { showToast(e.message || 'Lỗi cập nhật', 'error') }
}
</script>

<template>
  <div class="p-6">
    <Transition name="fade">
      <div v-if="toast.show" class="fixed top-4 right-4 z-50 px-4 py-3 rounded-lg shadow-lg text-white text-sm font-medium"
        :class="toast.type === 'success' ? 'bg-green-500' : 'bg-red-500'">{{ toast.message }}</div>
    </Transition>

    <div class="mb-6 flex justify-between items-center">
      <h1 class="text-2xl font-bold text-gray-800">Quản Lý Màu Sắc</h1>
      <button @click="openAdd" class="flex items-center gap-2 bg-rose-500 hover:bg-rose-600 text-white px-4 py-2 rounded-lg text-sm font-medium">
        <Plus :size="16" /> Thêm mới
      </button>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4 mb-4">
      <div class="flex gap-3">
        <div class="relative flex-1">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" :size="16" />
          <input v-model="keyword" @keyup.enter="doSearch" placeholder="Tìm kiếm theo mã, tên..." class="w-full pl-9 pr-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400" />
        </div>
        <button @click="doSearch" class="px-4 py-2 bg-rose-500 hover:bg-rose-600 text-white text-sm rounded-lg">Tìm kiếm</button>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-gray-100">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase w-12">STT</th>
            <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase w-16">Màu</th>
            <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Mã</th>
            <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Tên màu sắc</th>
            <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Mã HEX</th>
            <th class="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase w-28">Trạng thái</th>
            <th class="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase w-28">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <template v-if="loading">
            <tr v-for="j in 5" :key="j" class="animate-pulse">
              <td v-for="c in 7" :key="c" class="px-4 py-3"><div class="h-4 bg-gray-200 rounded"></div></td>
            </tr>
          </template>
          <tr v-else-if="items.length === 0">
            <td colspan="7" class="px-4 py-12 text-center text-gray-400">Không có dữ liệu</td>
          </tr>
          <tr v-for="(item, idx) in items" :key="item.id" v-else class="hover:bg-gray-50">
            <td class="px-4 py-3 text-gray-500">{{ currentPage * 10 + idx + 1 }}</td>
            <td class="px-4 py-3">
              <div class="w-8 h-8 rounded-full border-2 border-gray-200 shadow-sm"
                :style="item.maMauHex ? `background-color: ${item.maMauHex}` : 'background: #e5e7eb'"></div>
            </td>
            <td class="px-4 py-3 font-mono text-xs font-semibold text-rose-700">{{ item.ma }}</td>
            <td class="px-4 py-3 font-medium text-gray-800">{{ item.ten }}</td>
            <td class="px-4 py-3 font-mono text-xs text-gray-500">{{ item.maMauHex || '—' }}</td>
            <td class="px-4 py-3 text-center">
              <button @click="handleToggleStatus(item)" class="px-2 py-1 rounded-full text-xs font-medium"
                :class="item.trangThai === 1 ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-600'">
                {{ item.trangThai === 1 ? 'Hoạt động' : 'Dừng' }}
              </button>
            </td>
            <td class="px-4 py-3">
              <div class="flex items-center gap-1 justify-end">
                <button @click="openView(item)" class="p-1.5 rounded-lg text-gray-500 hover:bg-gray-100 border border-gray-200"><Eye :size="14" /></button>
                <button @click="openEdit(item)" class="p-1.5 rounded-lg text-rose-600 hover:bg-rose-50 border border-rose-200"><Pencil :size="14" /></button>
                <button @click="handleDelete(item)" class="p-1.5 rounded-lg text-red-600 hover:bg-red-50 border border-red-200"><Trash2 :size="14" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="totalPages > 0" class="px-4 py-3 border-t border-gray-100 flex items-center justify-between text-sm text-gray-500">
        <span>{{ totalItems }} bản ghi</span>
        <div class="flex items-center gap-1">
          <button @click="loadData(currentPage - 1)" :disabled="currentPage === 0" class="p-1.5 rounded hover:bg-gray-100 disabled:opacity-40"><ChevronLeft :size="16" /></button>
          <button v-for="p in visiblePages" :key="p" @click="loadData(p)" class="w-8 h-8 rounded text-xs font-medium" :class="p === currentPage ? 'bg-rose-500 text-white' : 'hover:bg-gray-100 text-gray-600'">{{ p + 1 }}</button>
          <button @click="loadData(currentPage + 1)" :disabled="currentPage >= totalPages - 1" class="p-1.5 rounded hover:bg-gray-100 disabled:opacity-40"><ChevronRight :size="16" /></button>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showModal = false">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h2 class="text-lg font-semibold text-gray-800">{{ modalMode === 'add' ? 'Thêm màu sắc' : modalMode === 'edit' ? 'Cập nhật màu sắc' : 'Chi tiết màu sắc' }}</h2>
            <button @click="showModal = false" class="p-1.5 rounded-lg hover:bg-gray-100"><X :size="18" /></button>
          </div>
          <div class="p-6 space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Mã *</label>
                <input v-model="form.ma" :disabled="modalMode === 'view'"
                  class="w-full px-3 py-2 border rounded-lg text-sm uppercase focus:outline-none focus:ring-2 focus:ring-rose-400"
                  :class="errors.ma ? 'border-red-400' : 'border-gray-200'" placeholder="VD: RED" />
                <p v-if="errors.ma" class="text-xs text-red-500 mt-1">{{ errors.ma }}</p>
              </div>
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Tên *</label>
                <input v-model="form.ten" :disabled="modalMode === 'view'"
                  class="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                  :class="errors.ten ? 'border-red-400' : 'border-gray-200'" placeholder="Tên màu" />
                <p v-if="errors.ten" class="text-xs text-red-500 mt-1">{{ errors.ten }}</p>
              </div>
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-700 mb-1">Mã màu HEX</label>
              <div class="flex items-center gap-3">
                <input v-model="form.maMauHex" type="color" :disabled="modalMode === 'view'"
                  class="h-10 w-16 rounded-lg border border-gray-200 cursor-pointer disabled:opacity-60" />
                <input v-model="form.maMauHex" :disabled="modalMode === 'view'"
                  class="flex-1 px-3 py-2 border border-gray-200 rounded-lg text-sm font-mono focus:outline-none focus:ring-2 focus:ring-rose-400 uppercase"
                  placeholder="#000000" maxlength="7" />
              </div>
              <div class="mt-2 h-8 rounded-lg border border-gray-100" :style="`background-color: ${form.maMauHex}`"></div>
            </div>
          </div>
          <div v-if="modalMode !== 'view'" class="px-6 py-4 border-t border-gray-100 flex justify-end gap-3">
            <button @click="showModal = false" class="px-4 py-2 border border-gray-200 rounded-lg text-sm hover:bg-gray-50">Hủy</button>
            <button @click="handleSave" :disabled="saving" class="px-4 py-2 bg-rose-500 hover:bg-rose-600 text-white rounded-lg text-sm font-medium disabled:opacity-60">{{ saving ? 'Đang lưu...' : 'Lưu' }}</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
