<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Plus, Eye, X, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { kichCoApi } from '../../../services/danh-muc-api'
import DanhMucPageShell from '../../../components/admin/danh-muc/DanhMucPageShell.vue'
import DanhMucQuickStatusToggle from '../../../components/admin/danh-muc/DanhMucQuickStatusToggle.vue'
import AdminQuickStatusAction from '../../../components/common/AdminQuickStatusAction.vue'
import { exportRowsToExcel } from '../../../utils/export-excel'
import { getDisplayErrorMessage, getFieldErrors } from '../../../utils/error-message'
import { exceedsMaxLength, normalizeOptionalText, normalizeSizeValue   hasSpecialCharacters,
} from '../../../utils/thuoc-tinh-san-pham'
import { showConfirm, showSuccess, showError } from '../../../utils/alert'

const items = ref([])
const totalItems = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const pageSize = ref(5)
const loading = ref(false)
const keyword = ref('')

const GHI_CHU_MAX_LENGTH = 200

async function loadData(page = 0) {
  loading.value = true
  try {
    const res = await kichCoApi.list(keyword.value || undefined, page, pageSize.value)
    items.value = res.items
    totalItems.value = res.totalItems
    totalPages.value = res.totalPages
    currentPage.value = res.page
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể tải danh sách kích cỡ'))
  } finally {
    loading.value = false
  }
}

function doSearch() {
  loadData(0)
}

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
const form = reactive({ giaTri: '', ghiChu: '' })
const errors = reactive({})
const updatingStatusId = ref(null)

function clearForm() {
  Object.assign(form, { giaTri: '', ghiChu: '' })
  Object.keys(errors).forEach((key) => delete errors[key])
}

function openAdd() {
  clearForm()
  modalMode.value = 'add'
  showModal.value = true
}

function openEdit(item) {
  clearForm()
  Object.assign(form, { giaTri: item.giaTri, ghiChu: item.ghiChu || '' })
  selectedItem.value = item
  modalMode.value = 'edit'
  showModal.value = true
}

function openView(item) {
  openEdit(item)
}

function validate() {
  Object.keys(errors).forEach((key) => delete errors[key])

  const normalizedSize = normalizeSizeValue(form.giaTri)
  const ghiChu = normalizeOptionalText(form.ghiChu)

  if (!normalizedSize) {
    errors.giaTri = 'Kích cỡ không hợp lệ, vui lòng nhập lại'
  }

  if (ghiChu && exceedsMaxLength(ghiChu, GHI_CHU_MAX_LENGTH)) {
    errors.ghiChu = `Ghi chú không được vượt quá ${GHI_CHU_MAX_LENGTH} ký tự`
  }

  if (Object.keys(errors).length > 0) {
    return false
  }

  form.giaTri = normalizedSize
  return true
}

async function handleSave() {
  if (!validate()) return

  if (modalMode.value === 'add') {
    const isConfirmed = await showConfirm('Xác nhận thêm mới kích cỡ này?')
    if (!isConfirmed) return
  } else {
    const isConfirmed = await showConfirm('Xác nhận lưu thay đổi kích cỡ này?')
    if (!isConfirmed) return
  }

  saving.value = true
  try {
    const body = {
      giaTri: normalizeSizeValue(form.giaTri),
      ghiChu: normalizeOptionalText(form.ghiChu)
    }

    if (modalMode.value === 'add') await kichCoApi.create(body)
    else await kichCoApi.update(selectedItem.value.id, body)

    showSuccess(modalMode.value === 'add' ? 'Tạo thành công' : 'Cập nhật thành công')
    showModal.value = false
    loadData(currentPage.value)
  } catch (e) {
    Object.assign(errors, getFieldErrors(e))
    showError(getDisplayErrorMessage(e, 'Không thể lưu kích cỡ'))
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(item) {
  const nextTrangThai = item.trangThai === 1 ? 0 : 1
  const actionLabel = nextTrangThai === 1 ? 'bật' : 'dừng'

  const isConfirmed = await showConfirm(`Xác nhận ${actionLabel} nhanh kích cỡ "${item.giaTri}"?`)
  if (!isConfirmed) return

  updatingStatusId.value = item.id
  try {
    await kichCoApi.toggleStatus(item.id, nextTrangThai)
    showSuccess('Cập nhật trạng thái thành công')
    loadData(currentPage.value)
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể cập nhật trạng thái kích cỡ'))
  } finally {
    updatingStatusId.value = null
  }
}

async function xuatExcel() {
  if (!totalItems.value) {
    showError('Không có dữ liệu để xuất Excel')
    return
  }

  try {
    const res = await kichCoApi.list(keyword.value || undefined, 0, Math.max(totalItems.value, pageSize.value))
    const exported = exportRowsToExcel({
      filename: 'quan-ly-kich-co',
      sheetName: 'KichCo',
      columns: [
        { label: 'STT', value: (_, index) => index + 1 },
        { label: 'Kích cỡ', key: 'giaTri' },
        { label: 'Ghi chú', value: (row) => row.ghiChu || '—' },
        { label: 'Trạng thái', value: (row) => row.trangThai === 1 ? 'Hoạt động' : 'Dừng' }
      ],
      rows: res.items || []
    })

    if (exported) {
      showSuccess('Xuất Excel thành công')
    } else {
      showError('Không có dữ liệu để xuất Excel')
    }
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể xuất Excel kích cỡ'))
  }
}
</script>

<template>
  <DanhMucPageShell
    title="Quản lý kích cỡ"
    add-label="Thêm kích cỡ"
    list-title="Danh sách kích cỡ"
    search-placeholder="Tìm theo kích cỡ..."
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
          <col style="width: 10%" />
          <col style="width: 22%" />
          <col style="width: 34%" />
          <col style="width: 17%" />
          <col style="width: 17%" />
        </colgroup>
        <thead class="bg-gray-50 border-b border-gray-100">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-bold text-slate-950 uppercase w-12">STT</th>
            <th class="px-4 py-3 text-center text-xs font-bold text-slate-950 uppercase">Kích cỡ</th>
            <th class="px-4 py-3 text-left text-xs font-bold text-slate-950 uppercase">Ghi chú</th>
            <th class="px-4 py-3 text-center text-xs font-bold text-slate-950 uppercase w-28">Trạng thái</th>
            <th class="px-4 py-3 text-center text-xs font-bold text-slate-950 uppercase w-28">Thao tác</th>
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
            <td class="px-4 py-3 text-xs text-gray-500"><span class="table-text-wrap">{{ item.ghiChu || '—' }}</span></td>
            <td class="px-4 py-3 text-center">
              <div class="flex justify-center">
                <DanhMucQuickStatusToggle :trang-thai="item.trangThai" :loading="updatingStatusId === item.id" />
              </div>
            </td>
            <td class="px-4 py-3">
              <div class="flex items-center justify-center gap-1">
                <AdminQuickStatusAction
                  :loading="updatingStatusId === item.id"
                  :action-label="item.trangThai === 1 ? 'Chuyển sang ngừng bán' : 'Chuyển sang đang bán'"
                  :intent="item.trangThai === 1 ? 'deactivate' : 'activate'"
                  @toggle="handleToggleStatus(item)"
                />
                <button @click="openView(item)" title="Xem và sửa" class="admin-table-action text-slate-600 hover:text-rose-500">
                  <Eye :size="14" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </template>

    <template #modal>
      <Teleport to="body">
        <Transition name="fade">
          <div
            v-if="showModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
          @click.self="showModal = false"
        >
          <div class="bg-white rounded-2xl shadow-2xl w-full max-w-sm">
            <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
              <h2 class="text-lg font-semibold text-gray-800">
                {{ modalMode === 'add' ? 'Thêm kích cỡ' : modalMode === 'edit' ? 'Cập nhật kích cỡ' : 'Chi tiết kích cỡ' }}
              </h2>
              <button @click="showModal = false" class="p-1.5 rounded-lg hover:bg-gray-100">
                <X :size="18" />
              </button>
            </div>

            <div class="p-6 space-y-4">
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Kích cỡ *</label>
                <input
                  v-model="form.giaTri"
                  :disabled="modalMode === 'view'"
                  maxlength="20"
                  class="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                  :class="errors.giaTri ? 'border-red-400' : 'border-gray-200'"
                  placeholder="VD: 40, 40.5, EU42"
                />
                <p v-if="errors.giaTri" class="text-xs text-red-500 mt-1">{{ errors.giaTri }}</p>
              </div>

              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Ghi chú</label>
                <textarea
                  v-model="form.ghiChu"
                  :disabled="modalMode === 'view'"
                  rows="3"
                  maxlength="200"
                  class="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400 resize-none"
                  :class="errors.ghiChu ? 'border-red-400' : 'border-gray-200'"
                  placeholder="Ghi chú (US size, EU size...)"
                ></textarea>
                <p v-if="errors.ghiChu" class="text-xs text-red-500 mt-1">{{ errors.ghiChu }}</p>
              </div>
            </div>

            <div v-if="modalMode !== 'view'" class="px-6 py-4 border-t border-gray-100 flex justify-end gap-3">
              <button @click="showModal = false" class="px-4 py-2 border border-gray-200 rounded-lg text-sm hover:bg-gray-50">
                Hủy
              </button>
              <button
                @click="handleSave"
                :disabled="saving"
                class="px-4 py-2 bg-rose-500 hover:bg-rose-600 text-white rounded-lg text-sm font-medium disabled:opacity-60"
              >
                {{ saving ? 'Đang lưu...' : 'Lưu' }}
              </button>
            </div>
          </div>
        </div>
        </Transition>
      </Teleport>
    </template>
  </DanhMucPageShell>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
