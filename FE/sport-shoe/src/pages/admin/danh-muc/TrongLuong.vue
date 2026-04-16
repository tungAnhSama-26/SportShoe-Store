<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Plus, Trash2, Eye, X, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { trongLuongApi } from '../../../services/danh-muc-api'
import DanhMucPageShell from '../../../components/admin/danh-muc/DanhMucPageShell.vue'
import { exportRowsToExcel } from '../../../utils/export-excel'

const items = ref([])
const totalItems = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const pageSize = ref(5)
const loading = ref(false)
const keyword = ref('')

const toast = reactive({ show: false, message: '', type: 'success' })
function showToast(msg, type = 'success') { toast.message = msg; toast.type = type; toast.show = true; setTimeout(() => { toast.show = false }, 3000) }

async function loadData(page = 0) {
  loading.value = true
  try {
    const res = await trongLuongApi.list(keyword.value || undefined, page, pageSize.value)
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
const pageSizeOptions = [5, 10, 20, 50]

function handlePageSizeChange(size) {
  pageSize.value = size
  loadData(0)
}

const showModal = ref(false)
const modalMode = ref('add')
const saving = ref(false)
const selectedItem = ref(null)
const form = reactive({ ma: '', giaTri: null, moTa: '' })
const errors = reactive({})

function clearForm() { Object.assign(form, { ma: '', giaTri: null, moTa: '' }); Object.keys(errors).forEach(k => delete errors[k]) }
function openAdd() { clearForm(); modalMode.value = 'add'; showModal.value = true }
function openEdit(item) { clearForm(); Object.assign(form, { ma: item.ma, giaTri: item.giaTri, moTa: item.moTa || '' }); selectedItem.value = item; modalMode.value = 'edit'; showModal.value = true }
function openView(item) { openEdit(item) }

function validate() {
  Object.keys(errors).forEach(k => delete errors[k])
  if (!form.ma.trim()) errors.ma = 'Mã không được để trống'
  if (!form.giaTri || form.giaTri < 1) errors.giaTri = 'Giá trị phải >= 1'
  return Object.keys(errors).length === 0
}

async function handleSave() {
  if (!validate()) return
  saving.value = true
  try {
    const body = { ma: form.ma.trim(), giaTri: Number(form.giaTri), moTa: form.moTa || null }
    if (modalMode.value === 'add') await trongLuongApi.create(body)
    else await trongLuongApi.update(selectedItem.value.id, body)
    showToast(modalMode.value === 'add' ? 'Tạo thành công' : 'Cập nhật thành công')
    showModal.value = false; loadData(currentPage.value)
  } catch (e) { showToast(e.message || 'Có lỗi xảy ra', 'error') }
  finally { saving.value = false }
}

async function handleDelete(item) {
  if (!confirm(`Xóa trọng lượng "${item.ma}"?`)) return
  try { await trongLuongApi.delete(item.id); showToast('Xóa thành công'); loadData(currentPage.value) }
  catch (e) { showToast(e.message || 'Lỗi xóa', 'error') }
}

async function handleToggleStatus(item) {
  try { await trongLuongApi.toggleStatus(item.id, item.trangThai === 1 ? 0 : 1); showToast('Cập nhật trạng thái thành công'); loadData(currentPage.value) }
  catch (e) { showToast(e.message || 'Lỗi cập nhật', 'error') }
}

async function xuatExcel() {
  if (!totalItems.value) {
    showToast('Không có dữ liệu để xuất Excel', 'error')
    return
  }

  try {
    const res = await trongLuongApi.list(keyword.value || undefined, 0, Math.max(totalItems.value, pageSize.value))
    const exported = exportRowsToExcel({
      filename: 'quan-ly-trong-luong',
      sheetName: 'TrongLuong',
      columns: [
        { label: 'STT', value: (_, index) => index + 1 },
        { label: 'Mã', key: 'ma' },
        { label: 'Trọng lượng', value: (row) => row.giaTri ?? '—' },
        { label: 'Mô tả', value: (row) => row.moTa || '—' },
        { label: 'Trạng thái', value: (row) => row.trangThai === 1 ? 'Hoạt động' : 'Dừng' }
      ],
      rows: res.items || []
    })

    showToast(exported ? 'Xuất Excel thành công' : 'Không có dữ liệu để xuất Excel', exported ? 'success' : 'error')
  } catch (e) {
    showToast(e.message || 'Lỗi xuất Excel', 'error')
  }
}
</script>

<template>
  <DanhMucPageShell
    title="Quản lý trọng lượng"
    add-label="Thêm trọng lượng"
    list-title="Danh sách trọng lượng"
    search-placeholder="Tìm theo mã trọng lượng..."
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
    @export="xuatExcel"
    @change-page-size="handlePageSizeChange"
    @update:keyword="keyword = $event"
  >
    <template #table>
      <table class="danh-muc-table table-fixed">
        <colgroup>
          <col style="width: 8%" />
          <col style="width: 16%" />
          <col style="width: 16%" />
          <col style="width: 32%" />
          <col style="width: 14%" />
          <col style="width: 14%" />
        </colgroup>
        <thead class="bg-gray-50 border-b border-gray-100">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase w-12">STT</th>
            <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Mã</th>
            <th class="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase">Trọng lượng</th>
            <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Mô tả</th>
            <th class="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase w-28">Trạng thái</th>
            <th class="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase w-28">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <template v-if="loading">
            <tr v-for="j in 5" :key="j" class="animate-pulse">
              <td v-for="c in 6" :key="c" class="px-4 py-3"><div class="h-4 bg-gray-200 rounded"></div></td>
            </tr>
          </template>
          <tr v-else-if="items.length === 0">
            <td colspan="6" class="px-4 py-12 text-center text-gray-400">Không có dữ liệu</td>
          </tr>
          <tr v-for="(item, idx) in items" :key="item.id" v-else class="hover:bg-gray-50">
            <td class="px-4 py-3 text-gray-500">{{ currentPage * pageSize + idx + 1 }}</td>
            <td class="px-4 py-3 font-semibold text-slate-800"><span class="block truncate">{{ item.ma }}</span></td>
            <td class="px-4 py-3 text-center font-medium text-gray-800 tabular-nums">{{ item.giaTri }}</td>
            <td class="px-4 py-3 text-xs text-gray-500"><span class="block truncate">{{ item.moTa || '—' }}</span></td>
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
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h2 class="text-lg font-semibold text-gray-800">{{ modalMode === 'add' ? 'Thêm trọng lượng' : modalMode === 'edit' ? 'Cập nhật trọng lượng' : 'Chi tiết trọng lượng' }}</h2>
            <button @click="showModal = false" class="p-1.5 rounded-lg hover:bg-gray-100"><X :size="18" /></button>
          </div>
          <div class="p-6 space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Mã *</label>
                <input v-model="form.ma" :disabled="modalMode === 'view'"
                  class="w-full px-3 py-2 border rounded-lg text-sm uppercase focus:outline-none focus:ring-2 focus:ring-rose-400"
                  :class="errors.ma ? 'border-red-400' : 'border-gray-200'" placeholder="VD: TL250" />
                <p v-if="errors.ma" class="text-xs text-red-500 mt-1">{{ errors.ma }}</p>
              </div>
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Trọng lượng *</label>
                <input v-model.number="form.giaTri" type="number" min="1" :disabled="modalMode === 'view'"
                  class="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                  :class="errors.giaTri ? 'border-red-400' : 'border-gray-200'" placeholder="VD: 250" />
                <p v-if="errors.giaTri" class="text-xs text-red-500 mt-1">{{ errors.giaTri }}</p>
              </div>
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-700 mb-1">Mô tả</label>
              <textarea v-model="form.moTa" :disabled="modalMode === 'view'" rows="3"
                class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400 resize-none"
                placeholder="Mô tả..."></textarea>
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



