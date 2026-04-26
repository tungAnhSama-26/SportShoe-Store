<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus, Trash2, Eye, X, Upload, ImageOff } from 'lucide-vue-next'
import { thuongHieuApi } from '../../../services/danh-muc-api'
import DanhMucPageShell from '../../../components/admin/danh-muc/DanhMucPageShell.vue'
import DanhMucQuickStatusToggle from '../../../components/admin/danh-muc/DanhMucQuickStatusToggle.vue'
import AdminQuickStatusAction from '../../../components/common/AdminQuickStatusAction.vue'
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
    const res = await thuongHieuApi.list(keyword.value || undefined, page, pageSize.value)
    items.value = res.items; totalItems.value = res.totalItems; totalPages.value = res.totalPages; currentPage.value = res.page
  } catch (e) { showToast(e.message || 'Lỗi tải dữ liệu', 'error') }
  finally { loading.value = false }
}

function doSearch() { loadData(0) }
onMounted(() => loadData())

const visiblePages = computed(() => {
  const pages = []; const start = Math.max(0, currentPage.value - 2); const end = Math.min(totalPages.value - 1, start + 4)
  for (let i = start; i <= end; i++) pages.push(i); return pages
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
const form = reactive({ ma: '', ten: '', xuatXu: '', logoUrl: '', website: '', moTa: '' })
const errors = reactive({})
const updatingStatusId = ref(null)
const uploadingLogo = ref(false)

function clearForm() { Object.assign(form, { ma: '', ten: '', xuatXu: '', logoUrl: '', website: '', moTa: '' }); Object.keys(errors).forEach(k => delete errors[k]) }
function openAdd() { clearForm(); modalMode.value = 'add'; showModal.value = true }
function openEdit(item) { clearForm(); Object.assign(form, { ma: item.ma, ten: item.ten, xuatXu: item.xuatXu || '', logoUrl: item.logoUrl || '', website: item.website || '', moTa: item.moTa || '' }); selectedItem.value = item; modalMode.value = 'edit'; showModal.value = true }
function openView(item) { openEdit(item) }

async function handleLogoUpload(event) {
  const target = event.target
  if (!target.files?.length) return

  uploadingLogo.value = true
  try {
    form.logoUrl = await thuongHieuApi.uploadFile(target.files[0])
  } catch (e) { showToast(e.message || 'Tải logo thất bại', 'error') }
  finally { uploadingLogo.value = false; target.value = '' }
}

function clearLogo() { form.logoUrl = '' }

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
    const body = { ma: form.ma.trim(), ten: form.ten.trim(), xuatXu: form.xuatXu || null, logoUrl: form.logoUrl || null, website: form.website || null, moTa: form.moTa || null }
    if (modalMode.value === 'add') await thuongHieuApi.create(body)
    else await thuongHieuApi.update(selectedItem.value.id, body)
    showToast(modalMode.value === 'add' ? 'Tạo thành công' : 'Cập nhật thành công')
    showModal.value = false; loadData(currentPage.value)
  } catch (e) { showToast(e.message || 'Có lỗi xảy ra', 'error') }
  finally { saving.value = false }
}

async function handleToggleStatus(item) {
  const nextTrangThai = item.trangThai === 1 ? 0 : 1
  const actionLabel = nextTrangThai === 1 ? 'bật' : 'dừng'
  if (!confirm('Xác nhận ' + actionLabel + ' nhanh thương hiệu "' + item.ten + '"?')) return

  updatingStatusId.value = item.id
  try {
    await thuongHieuApi.toggleStatus(item.id, nextTrangThai); showToast('Cập nhật trạng thái thành công'); loadData(currentPage.value)
  }
  catch (e) { showToast(e.message || 'Lỗi cập nhật', 'error') }
  finally { updatingStatusId.value = null }
}

async function xuatExcel() {
  if (!totalItems.value) {
    showToast('Không có dữ liệu để xuất Excel', 'error')
    return
  }

  try {
    const res = await thuongHieuApi.list(keyword.value || undefined, 0, Math.max(totalItems.value, pageSize.value))
    const exported = exportRowsToExcel({
      filename: 'quan-ly-thuong-hieu',
      sheetName: 'ThuongHieu',
      columns: [
        { label: 'STT', value: (_, index) => index + 1 },
        { label: 'Mã', key: 'ma' },
        { label: 'Tên thương hiệu', key: 'ten' },
        { label: 'Xuất xứ', value: (row) => row.xuatXu || '—' },
        { label: 'Website', value: (row) => row.website || '—' },
        { label: 'Logo', value: (row) => row.logoUrl || '—' },
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
    title="Quản lý thương hiệu"
    add-label="Thêm thương hiệu"
    list-title="Danh sách thương hiệu"
    search-placeholder="Tìm theo mã, tên thương hiệu..."
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
          <col style="width: 10%" />
          <col style="width: 14%" />
          <col style="width: 24%" />
          <col style="width: 16%" />
          <col style="width: 14%" />
          <col style="width: 14%" />
        </colgroup>
        <thead class="bg-gray-50 border-b border-gray-100">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase w-12">STT</th>
            <th class="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase w-16">Logo</th>
            <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Mã</th>
            <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Tên thương hiệu</th>
            <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Xuất xứ</th>
            <th class="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase w-28">Trạng thái</th>
            <th class="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase w-28">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <template v-if="loading">
            <tr v-for="j in 5" :key="j" class="animate-pulse">
              <td class="px-4 py-3"><div class="h-4 bg-gray-200 rounded w-6"></div></td>
              <td class="px-4 py-3"><div class="h-10 w-10 bg-gray-200 rounded-lg"></div></td>
              <td class="px-4 py-3"><div class="h-4 bg-gray-200 rounded w-16"></div></td>
              <td class="px-4 py-3"><div class="h-4 bg-gray-200 rounded w-32"></div></td>
              <td class="px-4 py-3"><div class="h-4 bg-gray-200 rounded w-20"></div></td>
              <td class="px-4 py-3"><div class="h-6 bg-gray-200 rounded w-20 mx-auto"></div></td>
              <td class="px-4 py-3"><div class="h-4 bg-gray-200 rounded w-20 ml-auto"></div></td>
            </tr>
          </template>
          <tr v-else-if="items.length === 0">
            <td colspan="7" class="px-4 py-12 text-center text-gray-400">Không có dữ liệu</td>
          </tr>
          <tr v-for="(item, idx) in items" :key="item.id" v-else class="hover:bg-gray-50">
            <td class="px-4 py-3 text-gray-500">{{ currentPage * pageSize + idx + 1 }}</td>
            <td class="px-4 py-3">
              <div class="flex justify-center">
                <img v-if="item.logoUrl" :src="item.logoUrl" alt="" class="h-10 w-10 object-contain rounded-lg border border-gray-100" />
                <div v-else class="flex h-10 w-10 items-center justify-center rounded-lg bg-gray-100 text-xs text-gray-400">N/A</div>
              </div>
            </td>
            <td class="px-4 py-3 font-semibold text-slate-800"><span class="block truncate">{{ item.ma }}</span></td>
            <td class="px-4 py-3 font-medium text-gray-800"><span class="block truncate">{{ item.ten }}</span></td>
            <td class="px-4 py-3 text-gray-500"><span class="block truncate">{{ item.xuatXu || '—' }}</span></td>
            <td class="px-4 py-3 text-center"><div class="flex justify-center"><DanhMucQuickStatusToggle :trang-thai="item.trangThai" :loading="updatingStatusId === item.id" /></div></td>
            <td class="px-4 py-3">
              <div class="flex items-center justify-center gap-1">
                <AdminQuickStatusAction :loading="updatingStatusId === item.id" :action-label="item.trangThai === 1 ? 'Chuyển sang ngừng bán' : 'Chuyển sang đang bán'" :intent="item.trangThai === 1 ? 'deactivate' : 'activate'" @toggle="handleToggleStatus(item)" />
                <button @click="openView(item)" title="Xem và sửa" class="admin-table-action text-slate-600 hover:text-rose-500"><Eye :size="14" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </template>

    <template #modal>
      <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showModal = false">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md max-h-[90vh] flex flex-col">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h2 class="text-lg font-semibold text-gray-800">{{ modalMode === 'add' ? 'Thêm thương hiệu' : modalMode === 'edit' ? 'Cập nhật thương hiệu' : 'Chi tiết thương hiệu' }}</h2>
            <button @click="showModal = false" class="p-1.5 rounded-lg hover:bg-gray-100"><X :size="18" /></button>
          </div>
          <div class="overflow-y-auto flex-1 p-6 space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Mã *</label>
                <input v-model="form.ma" :disabled="modalMode === 'view'" class="w-full px-3 py-2 border rounded-lg text-sm uppercase focus:outline-none focus:ring-2 focus:ring-rose-400" :class="errors.ma ? 'border-red-400' : 'border-gray-200'" placeholder="VD: NIKE" />
                <p v-if="errors.ma" class="text-xs text-red-500 mt-1">{{ errors.ma }}</p>
              </div>
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Tên *</label>
                <input v-model="form.ten" :disabled="modalMode === 'view'" class="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400" :class="errors.ten ? 'border-red-400' : 'border-gray-200'" placeholder="Tên thương hiệu" />
                <p v-if="errors.ten" class="text-xs text-red-500 mt-1">{{ errors.ten }}</p>
              </div>
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-700 mb-1">Xuất xứ</label>
              <input v-model="form.xuatXu" :disabled="modalMode === 'view'" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400" placeholder="VD: Mỹ" />
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-700 mb-2">Logo</label>
              <div class="rounded-xl border border-dashed border-gray-200 bg-gray-50 p-4">
                <div class="flex flex-col gap-4 sm:flex-row sm:items-center">
                  <div class="flex h-24 w-24 shrink-0 items-center justify-center overflow-hidden rounded-2xl border border-gray-200 bg-white">
                    <img v-if="form.logoUrl" :src="form.logoUrl" class="h-full w-full object-contain p-2" alt="Logo preview" />
                    <div v-else class="flex flex-col items-center gap-2 text-gray-400">
                      <ImageOff :size="22" />
                      <span class="text-[11px] font-medium">Chưa có logo</span>
                    </div>
                  </div>

                  <div class="min-w-0 flex-1 space-y-2">
                    <label
                      v-if="modalMode !== 'view'"
                      class="inline-flex cursor-pointer items-center gap-2 rounded-lg bg-rose-500 px-4 py-2 text-sm font-medium text-white transition hover:bg-rose-600"
                    >
                      <Upload :size="16" />
                      {{ uploadingLogo ? 'Đang tải ảnh...' : form.logoUrl ? 'Đổi ảnh logo' : 'Chọn ảnh logo' }}
                      <input type="file" accept="image/*" class="hidden" @change="handleLogoUpload" />
                    </label>

                    <button
                      v-if="modalMode !== 'view' && form.logoUrl"
                      type="button"
                      class="ml-2 inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-gray-50"
                      @click="clearLogo"
                    >
                      <Trash2 :size="16" />
                      Xóa ảnh
                    </button>

                    <p class="text-xs text-gray-500">
                      Chọn file ảnh để hệ thống tự upload và hiển thị logo.
                    </p>
                  </div>
                </div>
              </div>
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-700 mb-1">Website</label>
              <input v-model="form.website" :disabled="modalMode === 'view'" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400" placeholder="https://nike.com" />
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-700 mb-1">Mô tả</label>
              <textarea v-model="form.moTa" :disabled="modalMode === 'view'" rows="3" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400 resize-none" placeholder="Mô tả..."></textarea>
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






