<script setup>
import { Trash2, X } from 'lucide-vue-next'

defineProps({
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
        :class="editingBienThe ? 'max-w-xl' : 'max-w-5xl'"
      >
        <div class="flex items-center justify-between border-b border-gray-100 px-6 py-4">
          <h2 class="text-lg font-semibold text-gray-800">
            {{ editingBienThe ? 'Cập nhật CTSP' : 'Thêm CTSP mới' }}
          </h2>
          <button class="rounded-lg p-1.5 hover:bg-gray-100" @click="emit('close')">
            <X :size="18" />
          </button>
        </div>

        <div class="overflow-y-auto p-6">
          <div class="mb-4 rounded-2xl border border-violet-100 bg-violet-50 px-4 py-3 text-sm text-violet-700">
            {{ selectedGiay?.ten }} · {{ selectedGiay?.ma }}
          </div>

          <div v-if="editingBienThe" class="grid grid-cols-2 gap-4">
            <div>
              <label class="mb-1 block text-xs font-medium text-gray-700">Số lượng</label>
              <input
                v-model.number="bienTheForm.soLuong"
                type="number"
                min="0"
                class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
              />
              <p v-if="bienTheErrors.soLuong" class="mt-1 text-xs text-red-500">{{ bienTheErrors.soLuong }}</p>
            </div>

            <div>
              <label class="mb-1 block text-xs font-medium text-gray-700">Giá gốc</label>
              <input
                v-model.number="bienTheForm.giaGoc"
                type="number"
                min="0"
                class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
              />
              <p v-if="bienTheErrors.giaGoc" class="mt-1 text-xs text-red-500">{{ bienTheErrors.giaGoc }}</p>
            </div>

            <div>
              <label class="mb-1 block text-xs font-medium text-gray-700">Giá bán *</label>
              <input
                v-model.number="bienTheForm.giaBan"
                type="number"
                min="1"
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

          <div v-else class="space-y-5">
            <div class="grid gap-5 xl:grid-cols-[360px_minmax(0,1fr)]">
              <section class="rounded-2xl border border-slate-100 bg-slate-50 p-4">
                <div class="mb-4">
                  <h3 class="text-sm font-bold text-slate-800">Sinh CTSP tự động</h3>
                  <p class="mt-1 text-xs text-slate-400">Chọn màu sắc, kích cỡ và giá trị mặc định để tạo danh sách CTSP.</p>
                </div>

                <div class="space-y-4">
                  <div>
                    <label class="mb-2 block text-xs font-medium text-gray-700">Màu sắc *</label>
                    <div class="flex flex-wrap gap-2">
                      <label
                        v-for="item in danhMuc?.mauSac"
                        :key="item.id"
                        class="inline-flex cursor-pointer items-center gap-2 rounded-full border px-3 py-1.5 text-xs transition"
                        :class="bulkBienTheForm.mauSacIds.includes(item.id) ? 'border-rose-300 bg-rose-50 text-rose-600' : 'border-slate-200 bg-white text-slate-600'"
                      >
                        <input v-model="bulkBienTheForm.mauSacIds" type="checkbox" class="hidden" :value="item.id" />
                        <span
                          class="h-2.5 w-2.5 rounded-full border border-black/5"
                          :style="{ backgroundColor: item.maMauHex || '#e2e8f0' }"
                        ></span>
                        {{ item.ten }}
                      </label>
                    </div>
                    <p v-if="bulkBienTheErrors.mauSacIds" class="mt-1 text-xs text-red-500">{{ bulkBienTheErrors.mauSacIds }}</p>
                  </div>

                  <div>
                    <label class="mb-2 block text-xs font-medium text-gray-700">Kích cỡ *</label>
                    <div class="flex flex-wrap gap-2">
                      <label
                        v-for="item in danhMuc?.kichCo"
                        :key="item.id"
                        class="inline-flex cursor-pointer items-center gap-2 rounded-full border px-3 py-1.5 text-xs transition"
                        :class="bulkBienTheForm.kichCoIds.includes(item.id) ? 'border-rose-300 bg-rose-50 text-rose-600' : 'border-slate-200 bg-white text-slate-600'"
                      >
                        <input v-model="bulkBienTheForm.kichCoIds" type="checkbox" class="hidden" :value="item.id" />
                        Size {{ item.giaTri }}
                      </label>
                    </div>
                    <p v-if="bulkBienTheErrors.kichCoIds" class="mt-1 text-xs text-red-500">{{ bulkBienTheErrors.kichCoIds }}</p>
                  </div>

                  <div class="grid gap-3 sm:grid-cols-3">
                    <div>
                      <label class="mb-1 block text-xs font-medium text-gray-700">Số lượng mặc định</label>
                      <input
                        v-model.number="bulkBienTheForm.soLuong"
                        type="number"
                        min="0"
                        class="w-full rounded-lg border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400"
                        :class="bulkBienTheErrors.soLuong ? 'border-red-400' : 'border-gray-200'"
                      />
                      <p v-if="bulkBienTheErrors.soLuong" class="mt-1 text-xs text-red-500">{{ bulkBienTheErrors.soLuong }}</p>
                    </div>

                    <div>
                      <label class="mb-1 block text-xs font-medium text-gray-700">Giá gốc mặc định</label>
                      <input
                        v-model.number="bulkBienTheForm.giaGoc"
                        type="number"
                        min="0"
                        class="w-full rounded-lg border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400"
                        :class="bulkBienTheErrors.giaGoc ? 'border-red-400' : 'border-gray-200'"
                      />
                      <p v-if="bulkBienTheErrors.giaGoc" class="mt-1 text-xs text-red-500">{{ bulkBienTheErrors.giaGoc }}</p>
                    </div>

                    <div>
                      <label class="mb-1 block text-xs font-medium text-gray-700">Giá bán mặc định *</label>
                      <input
                        v-model.number="bulkBienTheForm.giaBan"
                        type="number"
                        min="1"
                        class="w-full rounded-lg border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400"
                        :class="bulkBienTheErrors.giaBan ? 'border-red-400' : 'border-gray-200'"
                      />
                      <p v-if="bulkBienTheErrors.giaBan" class="mt-1 text-xs text-red-500">{{ bulkBienTheErrors.giaBan }}</p>
                    </div>
                  </div>

                  <button
                    type="button"
                    class="w-full rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800"
                    @click="emit('generate-bulk')"
                  >
                    Tạo CTSP tự động
                  </button>
                </div>
              </section>

              <section class="rounded-2xl border border-slate-100 bg-white p-4">
                <div class="mb-3 flex items-center justify-between gap-3">
                  <div>
                    <h3 class="text-sm font-bold text-slate-800">Danh sách CTSP sẽ tạo</h3>
                    <p class="mt-1 text-xs text-slate-400">Bạn có thể chỉnh từng dòng trước khi lưu.</p>
                  </div>
                  <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
                    {{ generatedBulkBienThes.length }} CTSP
                  </span>
                </div>

                <p v-if="bulkBienTheErrors.generated" class="mb-3 text-xs text-red-500">
                  {{ bulkBienTheErrors.generated }}
                </p>

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
                          <input
                            v-model.number="item.soLuong"
                            type="number"
                            min="0"
                            class="w-24 rounded-lg border border-slate-200 px-2.5 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                          />
                        </td>
                        <td class="px-3 py-2">
                          <input
                            v-model.number="item.giaGoc"
                            type="number"
                            min="0"
                            class="w-28 rounded-lg border border-slate-200 px-2.5 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                          />
                        </td>
                        <td class="px-3 py-2">
                          <input
                            v-model.number="item.giaBan"
                            type="number"
                            min="1"
                            class="w-28 rounded-lg border border-slate-200 px-2.5 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
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
                  Chọn màu sắc, kích cỡ rồi bấm "Tạo CTSP tự động" để sinh danh sách biến thể.
                </div>
              </section>
            </div>
          </div>
        </div>

        <div class="flex justify-end gap-3 border-t border-gray-100 px-6 py-4">
          <button class="rounded-lg border border-gray-200 px-4 py-2 text-sm hover:bg-gray-50" @click="emit('close')">
            Hủy
          </button>
          <button
            :disabled="savingBienThe"
            class="rounded-lg bg-rose-500 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-rose-600 disabled:opacity-60"
            @click="emit('save')"
          >
            {{ savingBienThe ? 'Đang lưu...' : (editingBienThe ? 'Lưu CTSP' : 'Lưu danh sách CTSP') }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
