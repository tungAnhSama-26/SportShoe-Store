<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Check, ChevronDown, Search, Trash2, X } from 'lucide-vue-next'
import AdminFormattedNumberInput from '../../common/AdminFormattedNumberInput.vue'

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  editingBienThe: {
    type: Object,
    default: null
  },
  selectedGiay: {
    type: Object,
    default: null
  },
  danhMuc: {
    type: Object,
    default: null
  },
  bienTheForm: {
    type: Object,
    required: true
  },
  bienTheErrors: {
    type: Object,
    required: true
  },
  bulkBienTheForm: {
    type: Object,
    required: true
  },
  bulkBienTheErrors: {
    type: Object,
    required: true
  },
  generatedBulkBienThes: {
    type: Array,
    default: () => []
  },
  savingBienThe: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'save', 'generate-bulk', 'remove-generated-bulk'])

const openSelectionDropdown = ref(null)
const mauSacSearch = ref('')
const kichCoSearch = ref('')
const mauSacDropdownRef = ref(null)
const kichCoDropdownRef = ref(null)

const selectedMauSacItems = computed(() =>
  (props.danhMuc?.mauSac || []).filter((item) => props.bulkBienTheForm.mauSacIds.includes(item.id))
)

const selectedKichCoItems = computed(() =>
  (props.danhMuc?.kichCo || []).filter((item) => props.bulkBienTheForm.kichCoIds.includes(item.id))
)

const filteredMauSacItems = computed(() => {
  const keyword = mauSacSearch.value.trim().toLowerCase()
  const items = props.danhMuc?.mauSac || []

  if (!keyword) return items

  return items.filter((item) =>
    `${item.ten || ''} ${item.maMauHex || ''}`.toLowerCase().includes(keyword)
  )
})

const filteredKichCoItems = computed(() => {
  const keyword = kichCoSearch.value.trim().toLowerCase()
  const items = props.danhMuc?.kichCo || []

  if (!keyword) return items

  return items.filter((item) =>
    `${item.giaTri || ''} ${item.ghiChu || ''}`.toLowerCase().includes(keyword)
  )
})

const mauSacSummary = computed(() => {
  if (!selectedMauSacItems.value.length) return 'Chọn màu sắc'
  if (selectedMauSacItems.value.length === 1) return selectedMauSacItems.value[0].ten
  return `Đã chọn ${selectedMauSacItems.value.length} màu sắc`
})

const kichCoSummary = computed(() => {
  if (!selectedKichCoItems.value.length) return 'Chọn kích cỡ'
  if (selectedKichCoItems.value.length === 1) return `Size ${selectedKichCoItems.value[0].giaTri}`
  return `Đã chọn ${selectedKichCoItems.value.length} kích cỡ`
})

function toggleSelectionDropdown(type) {
  openSelectionDropdown.value = openSelectionDropdown.value === type ? null : type
}

function closeSelectionDropdown() {
  openSelectionDropdown.value = null
}

function handleDocumentClick(event) {
  const target = event.target

  if (mauSacDropdownRef.value?.contains(target) || kichCoDropdownRef.value?.contains(target)) {
    return
  }

  closeSelectionDropdown()
}

function toggleSelectedValue(field, id) {
  const numericId = Number(id)
  const currentValues = Array.isArray(props.bulkBienTheForm[field]) ? props.bulkBienTheForm[field] : []

  if (currentValues.includes(numericId)) {
    props.bulkBienTheForm[field] = currentValues.filter((item) => item !== numericId)
    return
  }

  props.bulkBienTheForm[field] = [...currentValues, numericId]
}

function isSelected(field, id) {
  return Array.isArray(props.bulkBienTheForm[field]) && props.bulkBienTheForm[field].includes(Number(id))
}

function clearSelectedValues(field) {
  props.bulkBienTheForm[field] = []
}

function applyGeneratedDefaults() {
  props.generatedBulkBienThes.forEach((item) => {
    item.soLuong = Number(props.bulkBienTheForm.soLuong || 0)
    item.giaGoc = Number(props.bulkBienTheForm.giaGoc || 0)
    item.giaBan = Number(props.bulkBienTheForm.giaBan || 0)
  })
}

onMounted(() => {
  document.addEventListener('mousedown', handleDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleDocumentClick)
})
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open"
      class="fixed inset-0 z-[60] flex items-center justify-center bg-black/55 p-4"
      @click.self="emit('close')"
    >
      <div
        class="flex max-h-[90vh] w-full flex-col rounded-2xl bg-white shadow-2xl"
        :class="editingBienThe ? 'max-w-xl' : 'max-w-6xl'"
      >
        <div class="flex items-center justify-between border-b border-gray-100 px-6 py-4">
          <h2 class="text-lg font-semibold text-gray-800">
            {{ editingBienThe ? 'Cập nhật biến thể' : 'Tạo danh sách biến thể' }}
          </h2>
          <button type="button" class="rounded-lg p-1.5 hover:bg-gray-100" @click="emit('close')">
            <X :size="18" />
          </button>
        </div>

        <div class="overflow-y-auto p-6">
          <div class="mb-4 rounded-2xl border border-violet-100 bg-violet-50 px-4 py-3 text-sm text-violet-700">
            {{ selectedGiay?.ten }} · {{ selectedGiay?.ma }}
          </div>

          <div v-if="editingBienThe" class="space-y-4">
            <div class="grid gap-4 md:grid-cols-2">
              <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
                <div class="text-xs font-medium uppercase tracking-wide text-slate-400">Màu sắc</div>
                <div class="mt-2 flex items-center gap-2 text-sm font-semibold text-slate-700">
                  <span
                    v-if="editingBienThe.maMauHex"
                    class="h-3 w-3 rounded-full border border-black/5"
                    :style="{ backgroundColor: editingBienThe.maMauHex }"
                  ></span>
                  <span>{{ editingBienThe.mauSac }}</span>
                </div>
              </div>

              <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
                <div class="text-xs font-medium uppercase tracking-wide text-slate-400">Kích cỡ</div>
                <div class="mt-2 text-sm font-semibold text-slate-700">Size {{ editingBienThe.kichCo }}</div>
              </div>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <div>
                <label class="mb-1 block text-xs font-medium text-gray-700">Số lượng</label>
                <AdminFormattedNumberInput
                  v-model="bienTheForm.soLuong"
                  :min="0"
                  class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                />
                <p v-if="bienTheErrors.soLuong" class="mt-1 text-xs text-red-500">{{ bienTheErrors.soLuong }}</p>
              </div>

              <div>
                <label class="mb-1 block text-xs font-medium text-gray-700">Giá gốc</label>
                <AdminFormattedNumberInput
                  v-model="bienTheForm.giaGoc"
                  :min="0"
                  class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                />
                <p v-if="bienTheErrors.giaGoc" class="mt-1 text-xs text-red-500">{{ bienTheErrors.giaGoc }}</p>
              </div>

              <div>
                <label class="mb-1 block text-xs font-medium text-gray-700">Giá bán *</label>
                <AdminFormattedNumberInput
                  v-model="bienTheForm.giaBan"
                  :min="0"
                  class="w-full rounded-lg border bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                  :class="bienTheErrors.giaBan ? 'border-red-400' : 'border-gray-200'"
                />
                <p v-if="bienTheErrors.giaBan" class="mt-1 text-xs text-red-500">{{ bienTheErrors.giaBan }}</p>
              </div>

              <div>
                <label class="mb-1 block text-xs font-medium text-gray-700">Trạng thái</label>
                <select
                  v-model.number="bienTheForm.kichHoat"
                  class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                >
                  <option :value="1">Kích hoạt</option>
                  <option :value="2">Tạm dừng</option>
                </select>
              </div>
            </div>
          </div>

          <div v-else class="space-y-5">
            <div class="grid gap-5 xl:grid-cols-[340px_minmax(0,1fr)]">
              <section class="rounded-2xl border border-slate-100 bg-slate-50 p-4">
                <div class="mb-4">
                  <h3 class="text-sm font-bold text-slate-800">Bước 1: Chọn màu và kích cỡ</h3>
                  <p class="mt-1 text-xs text-slate-400">
                    Dùng dropdown giống bộ lọc để chọn nhiều màu sắc và kích cỡ, có thể gõ để tìm nhanh.
                  </p>
                </div>

                <div class="space-y-4">
                  <div ref="mauSacDropdownRef" class="relative" @click.stop>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Màu sắc *</label>
                    <button
                      type="button"
                      class="flex h-10 w-full items-center justify-between gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 text-[13px] transition hover:bg-white"
                      :class="selectedMauSacItems.length ? 'border-rose-300 text-rose-600' : 'text-slate-600'"
                      @click="toggleSelectionDropdown('mauSac')"
                    >
                      <span class="truncate">{{ mauSacSummary }}</span>
                      <ChevronDown :size="14" />
                    </button>

                    <div
                      v-if="openSelectionDropdown === 'mauSac'"
                      class="absolute left-0 top-full z-30 mt-1 w-full min-w-[260px] overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-lg"
                    >
                      <div class="border-b border-slate-100 p-3">
                        <div class="relative">
                          <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                          <input
                            v-model="mauSacSearch"
                            type="text"
                            placeholder="Tìm màu sắc..."
                            class="h-10 w-full rounded-xl border border-slate-200 bg-slate-50 pl-10 pr-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
                            @keydown.stop
                          />
                        </div>
                      </div>

                      <div class="max-h-64 overflow-y-auto p-2">
                        <button
                          v-if="selectedMauSacItems.length"
                          type="button"
                          class="mb-1 flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-sm text-slate-500 transition hover:bg-slate-50"
                          @click="clearSelectedValues('mauSacIds')"
                        >
                          <span>Bỏ chọn tất cả</span>
                          <X :size="15" />
                        </button>

                        <button
                          v-for="item in filteredMauSacItems"
                          :key="item.id"
                          type="button"
                          class="flex w-full items-center justify-between gap-3 rounded-xl px-3 py-2 text-left text-sm transition hover:bg-slate-50"
                          :class="isSelected('mauSacIds', item.id) ? 'bg-rose-50 text-rose-600' : 'text-slate-700'"
                          @click="toggleSelectedValue('mauSacIds', item.id)"
                        >
                          <div class="flex min-w-0 items-center gap-2">
                            <span
                              class="h-3 w-3 shrink-0 rounded-full border border-black/5"
                              :style="{ backgroundColor: item.maMauHex || '#e2e8f0' }"
                            ></span>
                            <span class="truncate">{{ item.ten }}</span>
                          </div>
                          <Check v-if="isSelected('mauSacIds', item.id)" :size="15" class="shrink-0" />
                        </button>

                        <div
                          v-if="!filteredMauSacItems.length"
                          class="rounded-xl px-3 py-6 text-center text-sm text-slate-400"
                        >
                          Không tìm thấy màu sắc phù hợp.
                        </div>
                      </div>
                    </div>

                    <p class="mt-1 text-xs text-slate-400">
                      {{ selectedMauSacItems.length ? `${selectedMauSacItems.length} màu đã chọn` : 'Chưa chọn màu sắc' }}
                    </p>
                    <p v-if="bulkBienTheErrors.mauSacIds" class="mt-1 text-xs text-red-500">{{ bulkBienTheErrors.mauSacIds }}</p>
                  </div>

                  <div ref="kichCoDropdownRef" class="relative" @click.stop>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Kích cỡ *</label>
                    <button
                      type="button"
                      class="flex h-10 w-full items-center justify-between gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 text-[13px] transition hover:bg-white"
                      :class="selectedKichCoItems.length ? 'border-rose-300 text-rose-600' : 'text-slate-600'"
                      @click="toggleSelectionDropdown('kichCo')"
                    >
                      <span class="truncate">{{ kichCoSummary }}</span>
                      <ChevronDown :size="14" />
                    </button>

                    <div
                      v-if="openSelectionDropdown === 'kichCo'"
                      class="absolute left-0 top-full z-30 mt-1 w-full min-w-[260px] overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-lg"
                    >
                      <div class="border-b border-slate-100 p-3">
                        <div class="relative">
                          <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                          <input
                            v-model="kichCoSearch"
                            type="text"
                            placeholder="Tìm kích cỡ..."
                            class="h-10 w-full rounded-xl border border-slate-200 bg-slate-50 pl-10 pr-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
                            @keydown.stop
                          />
                        </div>
                      </div>

                      <div class="max-h-64 overflow-y-auto p-2">
                        <button
                          v-if="selectedKichCoItems.length"
                          type="button"
                          class="mb-1 flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-sm text-slate-500 transition hover:bg-slate-50"
                          @click="clearSelectedValues('kichCoIds')"
                        >
                          <span>Bỏ chọn tất cả</span>
                          <X :size="15" />
                        </button>

                        <button
                          v-for="item in filteredKichCoItems"
                          :key="item.id"
                          type="button"
                          class="flex w-full items-center justify-between gap-3 rounded-xl px-3 py-2 text-left text-sm transition hover:bg-slate-50"
                          :class="isSelected('kichCoIds', item.id) ? 'bg-rose-50 text-rose-600' : 'text-slate-700'"
                          @click="toggleSelectedValue('kichCoIds', item.id)"
                        >
                          <span class="truncate">Size {{ item.giaTri }}</span>
                          <Check v-if="isSelected('kichCoIds', item.id)" :size="15" class="shrink-0" />
                        </button>

                        <div
                          v-if="!filteredKichCoItems.length"
                          class="rounded-xl px-3 py-6 text-center text-sm text-slate-400"
                        >
                          Không tìm thấy kích cỡ phù hợp.
                        </div>
                      </div>
                    </div>

                    <p class="mt-1 text-xs text-slate-400">
                      {{ selectedKichCoItems.length ? `${selectedKichCoItems.length} kích cỡ đã chọn` : 'Chưa chọn kích cỡ' }}
                    </p>
                    <p v-if="bulkBienTheErrors.kichCoIds" class="mt-1 text-xs text-red-500">{{ bulkBienTheErrors.kichCoIds }}</p>
                  </div>

                  <button
                    type="button"
                    class="w-full rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800"
                    @click="emit('generate-bulk')"
                  >
                    Tạo danh sách biến thể
                  </button>
                </div>
              </section>

              <section class="rounded-2xl border border-slate-100 bg-white p-4">
                <div class="mb-3 flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <h3 class="text-sm font-bold text-slate-800">Bước 2: Nhập số lượng và giá bán</h3>
                    <p class="mt-1 text-xs text-slate-400">
                      Sau khi sinh biến thể xong, mới nhập số lượng và giá cho từng dòng hoặc áp dụng hàng loạt.
                    </p>
                  </div>
                  <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
                    {{ generatedBulkBienThes.length }} biến thể
                  </span>
                </div>

                <p v-if="bulkBienTheErrors.generated" class="mb-3 text-xs text-red-500">
                  {{ bulkBienTheErrors.generated }}
                </p>

                <div
                  v-if="generatedBulkBienThes.length"
                  class="mb-4 grid gap-3 rounded-2xl border border-slate-100 bg-slate-50 p-4 md:grid-cols-[1fr_1fr_1fr_auto]"
                >
                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Số lượng mặc định</label>
                    <AdminFormattedNumberInput
                      v-model="bulkBienTheForm.soLuong"
                      :min="0"
                      class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                    />
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Giá gốc mặc định</label>
                    <AdminFormattedNumberInput
                      v-model="bulkBienTheForm.giaGoc"
                      :min="0"
                      class="w-full rounded-lg border bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                      :class="bulkBienTheErrors.giaGoc ? 'border-red-400' : 'border-gray-200'"
                    />
                    <p v-if="bulkBienTheErrors.giaGoc" class="mt-1 text-xs text-red-500">{{ bulkBienTheErrors.giaGoc }}</p>
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Giá bán mặc định</label>
                    <AdminFormattedNumberInput
                      v-model="bulkBienTheForm.giaBan"
                      :min="0"
                      class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                    />
                  </div>

                  <div class="flex items-end">
                    <button
                      type="button"
                      class="w-full rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
                      @click="applyGeneratedDefaults"
                    >
                      Áp dụng
                    </button>
                  </div>
                </div>

                <div v-if="generatedBulkBienThes.length" class="overflow-x-auto rounded-2xl border border-slate-200 bg-white">
                  <table class="min-w-full text-sm">
                    <thead class="bg-slate-50 text-slate-500">
                      <tr>
                        <th class="px-3 py-2 text-left font-semibold">Màu sắc</th>
                        <th class="px-3 py-2 text-left font-semibold">Kích cỡ</th>
                        <th class="px-3 py-2 text-left font-semibold">Số lượng</th>
                        <th class="px-3 py-2 text-left font-semibold">Giá gốc</th>
                        <th class="px-3 py-2 text-left font-semibold">Giá bán</th>
                        <th class="px-3 py-2 text-center font-semibold">Xóa</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr
                        v-for="item in generatedBulkBienThes"
                        :key="item.key"
                        class="border-t border-slate-100"
                      >
                        <td class="px-3 py-2 text-slate-700">{{ item.mauSac }}</td>
                        <td class="px-3 py-2 text-slate-700">Size {{ item.kichCo }}</td>
                        <td class="px-3 py-2">
                          <AdminFormattedNumberInput
                            v-model="item.soLuong"
                            :min="0"
                            class="w-28 rounded-lg border border-slate-200 px-2.5 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                          />
                        </td>
                        <td class="px-3 py-2">
                          <AdminFormattedNumberInput
                            v-model="item.giaGoc"
                            :min="0"
                            class="w-32 rounded-lg border border-slate-200 px-2.5 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                          />
                        </td>
                        <td class="px-3 py-2">
                          <AdminFormattedNumberInput
                            v-model="item.giaBan"
                            :min="0"
                            class="w-32 rounded-lg border border-slate-200 px-2.5 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                          />
                        </td>
                        <td class="px-3 py-2 text-center">
                          <button
                            type="button"
                            class="inline-flex rounded-lg p-2 text-rose-500 transition hover:bg-rose-50 hover:text-rose-600"
                            @click="emit('remove-generated-bulk', item.key)"
                          >
                            <Trash2 :size="14" />
                          </button>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div
                  v-else
                  class="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-4 py-10 text-center text-sm text-slate-400"
                >
                  Chọn màu sắc và kích cỡ trong dropdown, sau đó bấm tạo danh sách biến thể.
                </div>
              </section>
            </div>
          </div>
        </div>

        <div class="flex justify-end gap-3 border-t border-gray-100 px-6 py-4">
          <button
            type="button"
            class="rounded-lg border border-gray-200 px-4 py-2 text-sm hover:bg-gray-50"
            @click="emit('close')"
          >
            Hủy
          </button>
          <button
            type="button"
            :disabled="savingBienThe"
            class="rounded-lg bg-rose-500 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-rose-600 disabled:opacity-60"
            @click="emit('save')"
          >
            {{ savingBienThe ? 'Đang lưu...' : (editingBienThe ? 'Lưu biến thể' : 'Lưu danh sách biến thể') }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
