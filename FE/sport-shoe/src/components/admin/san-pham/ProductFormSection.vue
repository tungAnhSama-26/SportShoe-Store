<script setup>
import { computed } from 'vue'
import AdminSearchableSelect from '../../../components/common/AdminSearchableSelect.vue'

const props = defineProps({
  productForm: {
    type: Object,
    default: () => ({
      ten: "",
      thuongHieuId: null,
      loaiGiayId: null,
      gioiTinh: null,
      chatLieuGiayId: null,
      moTa: "",
      deGiayId: null,
      coGiayId: null,
      congNgheDemId: null,
      trongLuongId: null
    })
  },
  productErrors: {
    type: Object,
    default: () => ({})
  },
  productCode: {
    type: String,
    required: true
  },
  danhMuc: {
    type: Object,
    default: null
  },
  inlineCreatingType: {
    type: String,
    default: null
  }
})

const emit = defineEmits([
  'update:product-form',
  'inline-create-attribute'
])

const thuongHieuOptions = computed(() => {
  return (props.danhMuc?.thuongHieu || []).filter(Boolean).map(item => ({
    value: item.id,
    label: item.ten,
    subtitle: item.xuatXu ? `Xuất xứ: ${item.xuatXu}` : null
  }))
})

const loaiGiayOptions = computed(() => {
  return (props.danhMuc?.loaiGiay || []).filter(Boolean).map(item => ({
    value: item.id,
    label: item.ten
  }))
})

const chatLieuOptions = computed(() => {
  return (props.danhMuc?.chatLieuGiay || []).filter(Boolean).map(item => ({
    value: item.id,
    label: item.ten
  }))
})

const deGiayOptions = computed(() => {
  return (props.danhMuc?.deGiay || []).filter(Boolean).map(item => ({
    value: item.id,
    label: item.ten
  }))
})

const coGiayOptions = computed(() => {
  return (props.danhMuc?.coGiay || []).filter(Boolean).map(item => ({
    value: item.id,
    label: item.ten
  }))
})

const congNgheDemOptions = computed(() => {
  return (props.danhMuc?.congNgheDem || []).filter(Boolean).map(item => ({
    value: item.id,
    label: item.ten
  }))
})

const trongLuongOptions = computed(() => {
  return (props.danhMuc?.trongLuong || []).filter(Boolean).map(item => ({
    value: item.id,
    label: item.giaTri != null ? `${item.giaTri} g` : item.ma,
    description: null,
    searchText: `${item.giaTri ?? ''} ${item.ma ?? ''}`,
    createMatchText: `${item.giaTri ?? ''}`
  }))
})

const genderSearchOptions = [
  { value: 1, label: "Nam" },
  { value: 2, label: "Nữ" },
  { value: 3, label: "Unisex" },
]

function handleInlineCreateAttribute(type, value) {
  emit('inline-create-attribute', type, value)
}

function toNullableNumber(value) {
  if (value == null || value === '') return null
  if (typeof value === 'object') {
    return toNullableNumber(value.id ?? value.value)
  }
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
}

function updateProductField(field, value, numeric = false) {
  props.productForm[field] = numeric ? toNullableNumber(value) : value
  if (props.productErrors?.[field]) {
    delete props.productErrors[field]
  }
}
</script>

<template>
  <article class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
    <section class="grid gap-4">
        <div class="grid gap-4 md:grid-cols-[220px_minmax(0,1fr)]">
          <label class="block">
            <span class="mb-1 block text-[13px] font-semibold text-slate-500">Mã sản phẩm</span>
            <div class="flex h-11 items-center rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-semibold text-slate-700">
              {{ productCode }}
            </div>
          </label>

          <label class="block">
            <span class="mb-1 block text-[13px] font-semibold text-slate-500">Sản phẩm <span class="text-rose-500">*</span></span>
            <input
              :value="productForm.ten"
              type="text"
              class="h-11 w-full rounded-2xl border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
              :class="
                productErrors.ten
                  ? 'border-rose-300 bg-rose-50'
                  : 'border-slate-200 bg-slate-50'
              "
              placeholder="Nhập tên sản phẩm..."
              @input="updateProductField('ten', $event.target.value)"
            />
            <p v-if="productErrors.ten" class="mt-1 text-xs text-rose-500">
              {{ productErrors.ten }}
            </p>
          </label>
        </div>


    </section>

    <div class="mt-5 border-t border-slate-100 pt-5">
      <div class="grid gap-4 md:grid-cols-2">
        <div class="block">
          <span class="mb-1 block text-[13px] font-semibold text-slate-500">Thương hiệu <span class="text-rose-500">*</span></span>
          <AdminSearchableSelect
            :model-value="productForm.thuongHieuId"
            :options="thuongHieuOptions"
            placeholder="Chọn thương hiệu..."
            search-placeholder="Tìm thương hiệu..."
            :error="Boolean(productErrors.thuongHieuId)"
            allow-create
            :creating="inlineCreatingType === 'thuongHieu'"
            @create="handleInlineCreateAttribute('thuongHieu', $event)"
            @update:model-value="updateProductField('thuongHieuId', $event, true)"
          />
          <p v-if="productErrors.thuongHieuId" class="mt-1 text-xs text-rose-500">
            {{ productErrors.thuongHieuId }}
          </p>
        </div>

        <div class="block">
          <span class="mb-1 block text-[13px] font-semibold text-slate-500">Loại giày <span class="text-rose-500">*</span></span>
          <AdminSearchableSelect
            :model-value="productForm.loaiGiayId"
            :options="loaiGiayOptions"
            placeholder="Chọn loại giày..."
            search-placeholder="Tìm loại giày..."
            :error="Boolean(productErrors.loaiGiayId)"
            allow-create
            :creating="inlineCreatingType === 'loaiGiay'"
            @create="handleInlineCreateAttribute('loaiGiay', $event)"
            @update:model-value="updateProductField('loaiGiayId', $event, true)"
          />
          <p v-if="productErrors.loaiGiayId" class="mt-1 text-xs text-rose-500">
            {{ productErrors.loaiGiayId }}
          </p>
        </div>

        <div class="block">
          <span class="mb-1 block text-[13px] font-semibold text-slate-500">Giới tính <span class="text-rose-500">*</span></span>
          <AdminSearchableSelect
            :model-value="productForm.gioiTinh"
            :options="genderSearchOptions"
            placeholder="Tất cả"
            search-placeholder="Tìm giới tính..."
            :error="Boolean(productErrors.gioiTinh)"
            @update:model-value="updateProductField('gioiTinh', $event, true)"
          />
          <p v-if="productErrors.gioiTinh" class="mt-1 text-xs text-rose-500">
            {{ productErrors.gioiTinh }}
          </p>
        </div>

        <div class="block">
          <span class="mb-1 block text-[13px] font-semibold text-slate-500">Chất liệu <span class="text-rose-500">*</span></span>
          <AdminSearchableSelect
            :model-value="productForm.chatLieuGiayId"
            :options="chatLieuOptions"
            placeholder="Chọn chất liệu giày..."
            search-placeholder="Tìm chất liệu..."
            :error="Boolean(productErrors.chatLieuGiayId)"
            allow-create
            :creating="inlineCreatingType === 'chatLieuGiay'"
            @create="handleInlineCreateAttribute('chatLieuGiay', $event)"
            @update:model-value="updateProductField('chatLieuGiayId', $event, true)"
          />
          <p v-if="productErrors.chatLieuGiayId" class="mt-1 text-xs text-rose-500">
            {{ productErrors.chatLieuGiayId }}
          </p>
        </div>

        <div class="block">
          <span class="mb-1 block text-[13px] font-semibold text-slate-500">Đế giày <span class="text-rose-500">*</span></span>
          <AdminSearchableSelect
            :model-value="productForm.deGiayId"
            :options="deGiayOptions"
            placeholder="Chọn đế giày..."
            search-placeholder="Tìm đế giày..."
            :error="Boolean(productErrors.deGiayId)"
            allow-create
            :creating="inlineCreatingType === 'deGiay'"
            @create="handleInlineCreateAttribute('deGiay', $event)"
            @update:model-value="updateProductField('deGiayId', $event, true)"
          />
          <p v-if="productErrors.deGiayId" class="mt-1 text-xs text-rose-500">
            {{ productErrors.deGiayId }}
          </p>
        </div>

        <div class="block">
          <span class="mb-1 block text-[13px] font-semibold text-slate-500">Cổ giày <span class="text-rose-500">*</span></span>
          <AdminSearchableSelect
            :model-value="productForm.coGiayId"
            :options="coGiayOptions"
            placeholder="Chọn cổ giày..."
            search-placeholder="Tìm cổ giày..."
            :error="Boolean(productErrors.coGiayId)"
            allow-create
            :creating="inlineCreatingType === 'coGiay'"
            @create="handleInlineCreateAttribute('coGiay', $event)"
            @update:model-value="updateProductField('coGiayId', $event, true)"
          />
          <p v-if="productErrors.coGiayId" class="mt-1 text-xs text-rose-500">
            {{ productErrors.coGiayId }}
          </p>
        </div>

        <div class="block">
          <span class="mb-1 block text-[13px] font-semibold text-slate-500">Công nghệ đệm <span class="text-rose-500">*</span></span>
          <AdminSearchableSelect
            :model-value="productForm.congNgheDemId"
            :options="congNgheDemOptions"
            placeholder="Chọn công nghệ đệm..."
            search-placeholder="Tìm công nghệ đệm..."
            :error="Boolean(productErrors.congNgheDemId)"
            allow-create
            :creating="inlineCreatingType === 'congNgheDem'"
            @create="handleInlineCreateAttribute('congNgheDem', $event)"
            @update:model-value="updateProductField('congNgheDemId', $event, true)"
          />
          <p v-if="productErrors.congNgheDemId" class="mt-1 text-xs text-rose-500">
            {{ productErrors.congNgheDemId }}
          </p>
        </div>

        <div class="block">
          <span class="mb-1 block text-[13px] font-semibold text-slate-500">Trọng lượng <span class="text-rose-500">*</span></span>
          <AdminSearchableSelect
            :model-value="productForm.trongLuongId"
            :options="trongLuongOptions"
            placeholder="Chọn trọng lượng..."
            search-placeholder="Tìm trọng lượng..."
            :error="Boolean(productErrors.trongLuongId)"
            allow-create
            :creating="inlineCreatingType === 'trongLuong'"
            @create="handleInlineCreateAttribute('trongLuong', $event)"
            @update:model-value="updateProductField('trongLuongId', $event, true)"
          />
          <p v-if="productErrors.trongLuongId" class="mt-1 text-xs text-rose-500">
            {{ productErrors.trongLuongId }}
          </p>
        </div>
      </div>
    </div>
  </article>
</template>
