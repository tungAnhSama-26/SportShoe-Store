<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Plus, Trash2, Eye, X, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { kichCoApi } from '../../../services/danh-muc-api'
import DanhMucPageShell from '../../../components/admin/danh-muc/DanhMucPageShell.vue'

const items = ref([])
const totalItems = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)
const loading = ref(false)
const keyword = ref('')

const toast = reactive({ show: false, message: '', type: 'success' })
function showToast(msg, type = 'success') { toast.message = msg; toast.type = type; toast.show = true; setTimeout(() => { toast.show = false }, 3000) }

async function loadData(page = 0) {
  loading.value = true
  try {
    const res = await kichCoApi.list(keyword.value || undefined, page, pageSize.value)
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
const pageSizeOptions = [10, 20, 50]

function handlePageSizeChange(size) {
  pageSize.value = size
  loadData(0)
}

const showModal = ref(false)
const modalMode = ref('add')
const saving = ref(false)
const selectedItem = ref(null)
const form = reactive({ giaTri: '', ghiChu: '' })
const errors = reactive({})

function clearForm() { Object.assign(form, { giaTri: '', ghiChu: '' }); Object.keys(errors).forEach(k => delete errors[k]) }
function openAdd() { clearForm(); modalMode.value = 'add'; showModal.value = true }
function openEdit(item) { clearForm(); Object.assign(form, { giaTri: item.giaTri, ghiChu: item.ghiChu || '' }); selectedItem.value = item; modalMode.value = 'edit'; showModal.value = true }
function openView(item) { openEdit(item) }

function validate() {
  Object.keys(errors).forEach(k => delete errors[k])
  if (!form.giaTri.trim()) errors.giaTri = 'Giá trị kích cỡ không được để trống'
  return Object.keys(errors).length === 0
}

async function handleSave() {
  if (!validate()) return
  saving.value = true
  try {
    const body = { giaTri: form.giaTri.trim(), ghiChu: form.ghiChu || null }
    if (modalMode.value === 'add') await kichCoApi.create(body)
    else await kichCoApi.update(selectedItem.value.id, body)
    showToast(modalMode.value === 'add' ? 'Tạo thành công' : 'Cập nhật thành công')
    showModal.value = false; loadData(currentPage.value)
  } catch (e) { showToast(e.message || 'Có lỗi xảy ra', 'error') }
  finally { saving.value = false }
}

async function handleDelete(item) {
  if (!confirm(`Xóa kích cỡ "${item.giaTri}"?`)) return
  try { await kichCoApi.delete(item.id); showToast('Xóa thành công'); loadData(currentPage.value) }
  catch (e) { showToast(e.message || 'Lỗi xóa', 'error') }
}

async function handleToggleStatus(item) {
  try { await kichCoApi.toggleStatus(item.id, item.trangThai === 1 ? 0 : 1); showToast('Cập nhật trạng thái thành công'); loadData(currentPage.value) }
  catch (e) { showToast(e.message || 'Lỗi cập nhật', 'error') }
}
</script>

<template>
  <DanhMucPageShell
    title="Quản lý kích cỡ"
    add-label="Thêm kích cỡ"
    list-title="Danh sách kích cỡ"
    search-placeholder="Tìm theo kích cỡ..."
    :toast="toast"
    :keyword="keyword"
    :page-size="pageSize"
    :page-size-options="pageSizeOptions"
    :total-items="totalItems"
    :current-page="currentPage"
    :total-pages="totalPages"
    :visible-pages="visiblePages"
    @add="openAdd"
    @go-page="loadData"
    @search="doSearch"
    @change-page-size="handlePageSizeChange"
    @update:keyword="keyword = $event"
  >
    <template #table>
      <table class="danh-muc-table table-fixed">
        <colgroup>
          <col style="width: 10%" />
          <col style="width: 22%" />
          <col style="width: 34%" />
          <col style="width: 17%" />
          <col style="width: 17%" />
        </colgroup>
        <thead class="bg-gray-50 border-b border-gray-100">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase w-12">STT</th>
            <th class="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase">Kích cỡ</th>
            <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Ghi chú</th>
            <th class="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase w-28">Trạng thái</th>
            <th class="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase w-28">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <template v-if="loading">
            <tr v-for="j in 5" :key="j" class="animate-pulse">
              <td v-for="c in 5" :key="c" class="px-4 py-3"><div class="h-4 bg-gray-200 rounded"></div></td>
            </tr>
          </template>
          <tr v-else-if="items.length === 0">
            <td colspan="5" class="px-4 py-12 text-center text-gray-400">Không có dữ liệu</td>
          </tr>
          <tr v-for="(item, idx) in items" :key="item.id" v-else class="hover:bg-gray-50">
            <td class="px-4 py-3 text-gray-500">{{ currentPage * pageSize + idx + 1 }}</td>
            <td class="px-4 py-3">
              <div class="flex justify-center">
                <span class="inline-flex h-8 min-w-12 items-center justify-center rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm font-semibold text-slate-800">{{ item.giaTri }}</span>
              </div>
            </td>
            <td class="px-4 py-3 text-xs text-gray-500"><span class="block truncate">{{ item.ghiChu || '—' }}</span></td>
            <td class="px-4 py-3 text-center">
              <div class="flex justify-center">
                <button @click="handleToggleStatus(item)" class="admin-status-chip"
                  :class="item.trangThai === 1 ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-500'">
                  {{ item.trangThai === 1 ? 'Hoạt động' : 'Dừng' }}
                </button>
              </div>
            </td>
            <td class="px-4 py-3">
              <div class="flex items-center justify-center gap-1">
                <button @click="openView(item)" title="Xem và sửa" class="admin-table-action text-slate-600 hover:text-rose-500"><Eye :size="14" /></button>
                <button @click="handleDelete(item)" class="admin-table-action text-red-500 hover:text-red-600"><Trash2 :size="14" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </template>

    <template #modal>
      <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showModal = false">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-sm">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h2 class="text-lg font-semibold text-gray-800">{{ modalMode === 'add' ? 'Thêm kích cỡ' : modalMode === 'edit' ? 'Cập nhật kích cỡ' : 'Chi tiết kích cỡ' }}</h2>
            <button @click="showModal = false" class="p-1.5 rounded-lg hover:bg-gray-100"><X :size="18" /></button>
          </div>
          <div class="p-6 space-y-4">
            <div>
              <label class="block text-xs font-medium text-gray-700 mb-1">Kích cỡ *</label>
              <input v-model="form.giaTri" :disabled="modalMode === 'view'"
                class="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                :class="errors.giaTri ? 'border-red-400' : 'border-gray-200'" placeholder="VD: 40, 40.5, EU42" />
              <p v-if="errors.giaTri" class="text-xs text-red-500 mt-1">{{ errors.giaTri }}</p>
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-700 mb-1">Ghi chú</label>
              <textarea v-model="form.ghiChu" :disabled="modalMode === 'view'" rows="3"
                class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400 resize-none"
                placeholder="Ghi chú (US size, EU size...)"></textarea>
            </div>
          </div>
          <div v-if="modalMode !== 'view'" class="px-6 py-4 border-t border-gray-100 flex justify-end gap-3">
            <button @click="showModal = false" class="px-4 py-2 border border-gray-200 rounded-lg text-sm hover:bg-gray-50">Hủy</button>
            <button @click="handleSave" :disabled="saving" class="px-4 py-2 bg-rose-500 hover:bg-rose-600 text-white rounded-lg text-sm font-medium disabled:opacity-60">{{ saving ? 'Đang lưu...' : 'Lưu' }}</button>
          </div>
        </div>
      </div>
      </Teleport>
    </template>
  </DanhMucPageShell>
</template>


