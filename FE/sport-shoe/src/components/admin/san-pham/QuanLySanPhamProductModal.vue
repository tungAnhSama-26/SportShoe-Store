<script setup>
import { computed } from 'vue'
import { ImageOff, X } from 'lucide-vue-next'
import AdminSearchableSelect from '../../common/AdminSearchableSelect.vue'

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  mode: {
    type: String,
    default: 'add'
  },
  title: {
    type: String,
    default: ''
  },
  description: {
    type: String,
    default: ''
  },
  loading: {
    type: Boolean,
    default: false
  },
  saving: {
    type: Boolean,
    default: false
  },
  danhMuc: {
    type: Object,
    default: null
  },
  productForm: {
    type: Object,
    required: true
  },
  productErrors: {
    type: Object,
    required: true
  },
  selectedProduct: {
    type: Object,
    default: null
  },
  mainImage: {
    type: Object,
    default: null
  },
  thuongHieuName: {
    type: Function,
    required: true
  },
  loaiGiayName: {
    type: Function,
    required: true
  },
  gioiTinhLabel: {
    type: Function,
    required: true
  },
  trangThaiClass: {
    type: Function,
    required: true
  },
  trangThaiLabel: {
    type: Function,
    required: true
  },
  selectedAttributeList: {
    type: Function,
    required: true
  }
})

const emit = defineEmits(['close', 'save'])

const genderOptions = [
  { value: 1, label: 'Nam' },
  { value: 2, label: 'Nữ' },
  { value: 3, label: 'Unisex' }
]

const thuongHieuOptions = computed(() =>
  (props.danhMuc?.thuongHieu || []).map((item) => ({
    value: item.id,
    label: item.ten
  }))
)

const loaiGiayOptions = computed(() =>
  (props.danhMuc?.loaiGiay || []).map((item) => ({
    value: item.id,
    label: item.ten
  }))
)

const chatLieuOptions = computed(() =>
  (props.danhMuc?.chatLieuGiay || []).map((item) => ({
    value: item.id,
    label: item.ten
  }))
)

const deGiayOptions = computed(() =>
  (props.danhMuc?.deGiay || []).map((item) => ({
    value: item.id,
    label: item.ten
  }))
)

const coGiayOptions = computed(() =>
  (props.danhMuc?.coGiay || []).map((item) => ({
    value: item.id,
    label: item.ten
  }))
)

const congNgheDemOptions = computed(() =>
  (props.danhMuc?.congNgheDem || []).map((item) => ({
    value: item.id,
    label: item.ten
  }))
)

const trongLuongOptions = computed(() =>
  (props.danhMuc?.trongLuong || []).map((item) => ({
    value: item.id,
    label: `${item.ma || 'TL'} - ${Number(item.giaTri || 0).toLocaleString('vi-VN')}`
  }))
)
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open"
      class="fixed inset-0 z-[55] flex items-center justify-center bg-black/55 p-4"
      @click.self="emit('close')"
    >
      <div class="flex max-h-[92vh] w-full max-w-5xl flex-col overflow-hidden rounded-[28px] bg-white shadow-2xl">
        <div class="flex items-start justify-between gap-4 border-b border-slate-100 px-6 py-5">
          <div>
            <h2 class="text-xl font-bold text-slate-800">{{ title }}</h2>
            <p class="mt-1 text-sm text-slate-500">{{ description }}</p>
          </div>
          <button
            class="rounded-xl p-2 text-slate-500 transition hover:bg-slate-100 hover:text-slate-700"
            @click="emit('close')"
          >
            <X :size="18" />
          </button>
        </div>

        <div v-if="loading" class="px-6 py-16 text-center text-sm text-slate-400">
          Đang tải chi tiết sản phẩm...
        </div>

        <div v-else class="flex-1 overflow-y-auto px-6 py-6">
          <div class="grid gap-5 xl:grid-cols-[minmax(0,1fr)_300px]">
            <section class="rounded-[24px] border border-slate-100 bg-white p-5">
              <div class="mb-5">
                <h3 class="text-base font-bold text-slate-800">Thông tin cơ bản</h3>
                <p class="mt-1 text-xs text-slate-400">
                  {{
                    mode === 'add'
                      ? 'Nhập thông tin sản phẩm rồi quản lý biến thể ở popup riêng.'
                      : 'Cập nhật nhanh thông tin nền và tìm thuộc tính ngay trong dropdown.'
                  }}
                </p>
              </div>

              <div class="grid gap-4 md:grid-cols-2">
                <div v-if="mode === 'add'">
                  <label class="mb-1 block text-xs font-medium text-gray-700">Mã sản phẩm</label>
                  <div class="rounded-lg border border-dashed border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-500">
                    Tự sinh khi lưu
                  </div>
                </div>

                <div :class="mode === 'add' ? '' : 'md:col-span-2'">
                  <label class="mb-1 block text-xs font-medium text-gray-700">Tên sản phẩm *</label>
                  <input
                    v-model="productForm.ten"
                    type="text"
                    class="w-full rounded-lg border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                    :class="productErrors.ten ? 'border-red-400' : 'border-gray-200'"
                    placeholder="Tên sản phẩm"
                  />
                  <p v-if="productErrors.ten" class="mt-1 text-xs text-red-500">{{ productErrors.ten }}</p>
                </div>

                <div>
                  <label class="mb-1 block text-xs font-medium text-gray-700">Thương hiệu *</label>
                  <AdminSearchableSelect
                    :model-value="productForm.thuongHieuId"
                    :options="thuongHieuOptions"
                    placeholder="Chọn thương hiệu"
                    search-placeholder="Tìm thương hiệu..."
                    :error="Boolean(productErrors.thuongHieuId)"
                    @update:model-value="productForm.thuongHieuId = $event"
                  />
                  <p v-if="productErrors.thuongHieuId" class="mt-1 text-xs text-red-500">{{ productErrors.thuongHieuId }}</p>
                </div>

                <div>
                  <label class="mb-1 block text-xs font-medium text-gray-700">Loại giày *</label>
                  <AdminSearchableSelect
                    :model-value="productForm.loaiGiayId"
                    :options="loaiGiayOptions"
                    placeholder="Chọn loại giày"
                    search-placeholder="Tìm loại giày..."
                    :error="Boolean(productErrors.loaiGiayId)"
                    @update:model-value="productForm.loaiGiayId = $event"
                  />
                  <p v-if="productErrors.loaiGiayId" class="mt-1 text-xs text-red-500">{{ productErrors.loaiGiayId }}</p>
                </div>

                <div>
                  <label class="mb-1 block text-xs font-medium text-gray-700">Giới tính</label>
                  <AdminSearchableSelect
                    :model-value="productForm.gioiTinh"
                    :options="genderOptions"
                    placeholder="Tất cả"
                    search-placeholder="Tìm giới tính..."
                    @update:model-value="productForm.gioiTinh = $event"
                  />
                </div>

                <div>
                  <label class="mb-1 block text-xs font-medium text-gray-700">Chất liệu</label>
                  <AdminSearchableSelect
                    :model-value="productForm.chatLieuGiayId"
                    :options="chatLieuOptions"
                    placeholder="Chọn chất liệu giày"
                    search-placeholder="Tìm chất liệu..."
                    @update:model-value="productForm.chatLieuGiayId = $event"
                  />
                </div>
              </div>

              <div class="mt-5 rounded-2xl border border-slate-100 bg-slate-50 p-4">
                <div class="mb-4">
                  <h3 class="text-sm font-bold text-slate-700">Thuộc tính kỹ thuật</h3>
                  <p class="mt-1 text-xs text-slate-400">
                    Có thể gõ để tìm nhanh rồi chọn ngay trong từng thuộc tính.
                  </p>
                </div>

                <div class="grid gap-4 md:grid-cols-2">
                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Đế giày</label>
                    <AdminSearchableSelect
                      :model-value="productForm.deGiayId"
                      :options="deGiayOptions"
                      placeholder="Không có"
                      search-placeholder="Tìm đế giày..."
                      @update:model-value="productForm.deGiayId = $event"
                    />
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Cổ giày</label>
                    <AdminSearchableSelect
                      :model-value="productForm.coGiayId"
                      :options="coGiayOptions"
                      placeholder="Không có"
                      search-placeholder="Tìm cổ giày..."
                      @update:model-value="productForm.coGiayId = $event"
                    />
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Công nghệ đệm</label>
                    <AdminSearchableSelect
                      :model-value="productForm.congNgheDemId"
                      :options="congNgheDemOptions"
                      placeholder="Không có"
                      search-placeholder="Tìm công nghệ đệm..."
                      @update:model-value="productForm.congNgheDemId = $event"
                    />
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Trọng lượng</label>
                    <AdminSearchableSelect
                      :model-value="productForm.trongLuongId"
                      :options="trongLuongOptions"
                      placeholder="Không có"
                      search-placeholder="Tìm trọng lượng..."
                      @update:model-value="productForm.trongLuongId = $event"
                    />
                  </div>
                </div>
              </div>

              <div class="mt-5">
                <label class="mb-1 block text-xs font-medium text-gray-700">Mô tả</label>
                <textarea
                  v-model="productForm.moTa"
                  rows="5"
                  class="w-full resize-none rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                  placeholder="Mô tả ngắn về sản phẩm..."
                ></textarea>
              </div>
            </section>

            <aside class="space-y-5">
              <section class="rounded-[24px] border border-slate-100 bg-white p-5">
                <div class="mb-4">
                  <h3 class="text-base font-bold text-slate-800">
                    {{ mode === 'add' ? 'Thông tin nhanh' : 'Tóm tắt' }}
                  </h3>
                  <p class="mt-1 text-xs text-slate-400">
                    {{
                      mode === 'add'
                        ? 'Lưu xong sẽ mở popup biến thể để bạn thêm màu, size, giá và số lượng.'
                        : 'Theo dõi nhanh trạng thái hiện tại của sản phẩm.'
                    }}
                  </p>
                </div>

                <div v-if="mode === 'edit'" class="space-y-4">
                  <div class="overflow-hidden rounded-2xl border border-slate-100 bg-slate-50">
                    <div class="aspect-square">
                      <img
                        v-if="mainImage"
                        :src="mainImage.url"
                        alt=""
                        class="h-full w-full object-cover"
                      />
                      <div v-else class="flex h-full items-center justify-center text-slate-400">
                        <ImageOff class="h-8 w-8" />
                      </div>
                    </div>
                  </div>

                  <div class="space-y-2 text-sm text-slate-600">
                    <div class="flex items-center justify-between gap-3">
                      <span class="text-slate-400">Mã sản phẩm</span>
                      <span class="font-semibold text-slate-700">{{ selectedProduct?.ma }}</span>
                    </div>
                    <div class="flex items-center justify-between gap-3">
                      <span class="text-slate-400">Trạng thái</span>
                      <span class="admin-status-chip whitespace-nowrap" :class="trangThaiClass(selectedProduct?.trangThai)">
                        {{ trangThaiLabel(selectedProduct?.trangThai) }}
                      </span>
                    </div>
                  </div>

                  <div class="flex flex-wrap gap-2">
                    <span
                      v-for="attribute in selectedAttributeList(selectedProduct)"
                      :key="attribute"
                      class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600"
                    >
                      {{ attribute }}
                    </span>
                  </div>
                </div>

                <div v-else class="space-y-4">
                  <div class="rounded-2xl border border-slate-100 bg-slate-50 p-4">
                    <div class="space-y-2 text-sm text-slate-600">
                      <div class="flex items-center justify-between gap-3">
                        <span class="text-slate-400">Mã sản phẩm</span>
                        <span class="font-semibold text-slate-700">Tự sinh khi lưu</span>
                      </div>
                      <div class="flex items-center justify-between gap-3">
                        <span class="text-slate-400">Biến thể</span>
                        <span class="font-semibold text-slate-700">Tạo sau khi lưu</span>
                      </div>
                      <div class="flex items-center justify-between gap-3">
                        <span class="text-slate-400">Màu / Size</span>
                        <span class="font-semibold text-slate-700">Chọn trong modal</span>
                      </div>
                    </div>
                  </div>

                  <div class="flex flex-wrap gap-2">
                    <span
                      v-if="productForm.thuongHieuId"
                      class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600"
                    >
                      {{ thuongHieuName(productForm.thuongHieuId) }}
                    </span>
                    <span
                      v-if="productForm.loaiGiayId"
                      class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600"
                    >
                      {{ loaiGiayName(productForm.loaiGiayId) }}
                    </span>
                    <span
                      v-if="productForm.gioiTinh"
                      class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600"
                    >
                      {{ gioiTinhLabel(productForm.gioiTinh) }}
                    </span>
                  </div>
                </div>
              </section>
            </aside>
          </div>
        </div>

        <div class="flex justify-end gap-3 border-t border-slate-100 px-6 py-4">
          <button
            class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
            @click="emit('close')"
          >
            Hủy
          </button>
          <button
            :disabled="saving || loading"
            class="rounded-xl bg-rose-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-rose-600 disabled:cursor-not-allowed disabled:opacity-60"
            @click="emit('save')"
          >
            {{ saving ? 'Đang lưu...' : (mode === 'add' ? 'Lưu sản phẩm' : 'Lưu thay đổi') }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
