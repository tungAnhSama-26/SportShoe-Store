<script setup>
import { computed } from 'vue'
import { ImageOff, X } from 'lucide-vue-next'
import AdminSearchableSelect from '../../common/AdminSearchableSelect.vue'

const props = defineProps({
  hienThiModal: {
    type: Boolean,
    default: false
  },
  cheDo: {
    type: String,
    default: 'them'
  },
  tieuDe: {
    type: String,
    default: ''
  },
  moTa: {
    type: String,
    default: ''
  },
  dangTai: {
    type: Boolean,
    default: false
  },
  dangLuu: {
    type: Boolean,
    default: false
  },
  danhMuc: {
    type: Object,
    default: null
  },
  formSanPham: {
    type: Object,
    required: true
  },
  loiSanPham: {
    type: Object,
    required: true
  },
  sanPhamDaChon: {
    type: Object,
    default: null
  },
  hinhAnhChinh: {
    type: Object,
    default: null
  },
  layTenThuongHieu: {
    type: Function,
    required: true
  },
  layTenLoaiGiay: {
    type: Function,
    required: true
  },
  layNhanGioiTinh: {
    type: Function,
    required: true
  },
  layLopTrangThai: {
    type: Function,
    required: true
  },
  layNhanTrangThai: {
    type: Function,
    required: true
  },
  layDanhSachThuocTinh: {
    type: Function,
    required: true
  }
})

const emit = defineEmits(['dong', 'luu'])

const luaChonGioiTinh = [
  { value: 1, label: 'Nam' },
  { value: 2, label: 'Nữ' },
  { value: 3, label: 'Unisex' }
]

const luaChonThuongHieu = computed(() =>
  (props.danhMuc?.thuongHieu || []).map((item) => ({
    value: item.id,
    label: item.ten
  }))
)

const luaChonLoaiGiay = computed(() =>
  (props.danhMuc?.loaiGiay || []).map((item) => ({
    value: item.id,
    label: item.ten
  }))
)

const luaChonChatLieu = computed(() =>
  (props.danhMuc?.chatLieuGiay || []).map((item) => ({
    value: item.id,
    label: item.ten
  }))
)

const luaChonDeGiay = computed(() =>
  (props.danhMuc?.deGiay || []).map((item) => ({
    value: item.id,
    label: item.ten
  }))
)

const luaChonCoGiay = computed(() =>
  (props.danhMuc?.coGiay || []).map((item) => ({
    value: item.id,
    label: item.ten
  }))
)

const luaChonCongNgheDem = computed(() =>
  (props.danhMuc?.congNgheDem || []).map((item) => ({
    value: item.id,
    label: item.ten
  }))
)

const luaChonTrongLuong = computed(() =>
  (props.danhMuc?.trongLuong || []).map((item) => ({
    value: item.id,
    label: Number(item.giaTri || 0).toLocaleString('vi-VN'),
    searchText: `${item.ma || ''} ${item.giaTri || ''} ${item.moTa || ''}`
  }))
)
</script>

<template>
  <Teleport to="body">
    <div
      v-if="hienThiModal"
      class="fixed inset-0 z-[55] flex items-center justify-center bg-black/55 p-4"
    >
      <div class="flex max-h-[92vh] w-full max-w-5xl flex-col overflow-hidden rounded-[28px] bg-white shadow-2xl">
        <div class="flex items-start justify-between gap-4 border-b border-slate-100 px-6 py-5">
          <div>
            <h2 class="text-xl font-bold text-slate-800">{{ tieuDe }}</h2>
            <p class="mt-1 text-sm text-slate-500">{{ moTa }}</p>
          </div>
          <button
            class="rounded-md p-2 text-slate-500 transition hover:bg-slate-100 hover:text-slate-700"
            @click="emit('dong')"
          >
            <X :size="18" />
          </button>
        </div>

        <div v-if="dangTai" class="px-6 py-16 text-center text-sm text-slate-400">
          Đang tải chi tiết sản phẩm...
        </div>

        <div v-else class="flex-1 overflow-y-auto px-6 py-6">
          <div class="grid gap-5 xl:grid-cols-[minmax(0,1fr)_300px]">
            <section class="rounded-[24px] border border-slate-100 bg-white p-5">
              <div class="mb-5">
                <h3 class="text-base font-bold text-slate-800">Thông tin cơ bản</h3>
                <p class="mt-1 text-xs text-slate-400">
                  {{
                    cheDo === 'them'
                      ? 'Nhập thông tin sản phẩm rồi quản lý biến thể ở popup riêng.'
                      : 'Cập nhật nhanh thông tin nền và tìm thuộc tính ngay trong dropdown.'
                  }}
                </p>
              </div>

              <div class="grid gap-4 md:grid-cols-2">
                <div v-if="cheDo === 'them'">
                  <label class="mb-1 block text-xs font-medium text-gray-700">Mã sản phẩm</label>
                  <div class="rounded-md border border-dashed border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-500">
                    Tự sinh khi lưu
                  </div>
                </div>

                <div :class="cheDo === 'them' ? '' : 'md:col-span-2'">
                  <label class="mb-1 block text-xs font-medium text-gray-700">Tên sản phẩm <span class="text-rose-500">*</span></label>
                  <input
                    v-model="formSanPham.ten"
                    type="text"
                    class="w-full rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                    :class="loiSanPham.ten ? 'border-red-400' : 'border-gray-200'"
                    placeholder="Tên sản phẩm"
                  />
                  <p v-if="loiSanPham.ten" class="mt-1 text-xs text-red-500">{{ loiSanPham.ten }}</p>
                </div>

                <div>
                  <label class="mb-1 block text-xs font-medium text-gray-700">Thương hiệu <span class="text-rose-500">*</span></label>
                  <AdminSearchableSelect
                    :model-value="formSanPham.thuongHieuId"
                    :options="luaChonThuongHieu"
                    placeholder="Chọn thương hiệu"
                    search-placeholder="Tìm thương hiệu..."
                    :error="Boolean(loiSanPham.thuongHieuId)"
                    @update:model-value="formSanPham.thuongHieuId = $event"
                  />
                  <p v-if="loiSanPham.thuongHieuId" class="mt-1 text-xs text-red-500">{{ loiSanPham.thuongHieuId }}</p>
                </div>

                <div>
                  <label class="mb-1 block text-xs font-medium text-gray-700">Loại giày <span class="text-rose-500">*</span></label>
                  <AdminSearchableSelect
                    :model-value="formSanPham.loaiGiayId"
                    :options="luaChonLoaiGiay"
                    placeholder="Chọn loại giày"
                    search-placeholder="Tìm loại giày..."
                    :error="Boolean(loiSanPham.loaiGiayId)"
                    @update:model-value="formSanPham.loaiGiayId = $event"
                  />
                  <p v-if="loiSanPham.loaiGiayId" class="mt-1 text-xs text-red-500">{{ loiSanPham.loaiGiayId }}</p>
                </div>

                <div>
                  <label class="mb-1 block text-xs font-medium text-gray-700">Giới tính</label>
                  <AdminSearchableSelect
                    :model-value="formSanPham.gioiTinh"
                    :options="luaChonGioiTinh"
                    placeholder="Tất cả"
                    search-placeholder="Tìm giới tính..."
                    @update:model-value="formSanPham.gioiTinh = $event"
                  />
                </div>

                <div>
                  <label class="mb-1 block text-xs font-medium text-gray-700">Chất liệu</label>
                  <AdminSearchableSelect
                    :model-value="formSanPham.chatLieuGiayId"
                    :options="luaChonChatLieu"
                    placeholder="Chọn chất liệu giày"
                    search-placeholder="Tìm chất liệu..."
                    @update:model-value="formSanPham.chatLieuGiayId = $event"
                  />
                </div>
              </div>

              <div class="mt-5 rounded-md border border-slate-100 bg-slate-50 p-4">
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
                      :model-value="formSanPham.deGiayId"
                      :options="luaChonDeGiay"
                      placeholder="Không có"
                      search-placeholder="Tìm đế giày..."
                      @update:model-value="formSanPham.deGiayId = $event"
                    />
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Cổ giày</label>
                    <AdminSearchableSelect
                      :model-value="formSanPham.coGiayId"
                      :options="luaChonCoGiay"
                      placeholder="Không có"
                      search-placeholder="Tìm cổ giày..."
                      @update:model-value="formSanPham.coGiayId = $event"
                    />
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Công nghệ đệm</label>
                    <AdminSearchableSelect
                      :model-value="formSanPham.congNgheDemId"
                      :options="luaChonCongNgheDem"
                      placeholder="Không có"
                      search-placeholder="Tìm công nghệ đệm..."
                      @update:model-value="formSanPham.congNgheDemId = $event"
                    />
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Trọng lượng</label>
                    <AdminSearchableSelect
                      :model-value="formSanPham.trongLuongId"
                      :options="luaChonTrongLuong"
                      placeholder="Không có"
                      search-placeholder="Tìm trọng lượng..."
                      @update:model-value="formSanPham.trongLuongId = $event"
                    />
                  </div>
                </div>
              </div>

              <div class="mt-5">
                <label class="mb-1 block text-xs font-medium text-gray-700">Mô tả</label>
                <textarea
                  v-model="formSanPham.moTa"
                  rows="5"
                  class="w-full resize-none rounded-md border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                  placeholder="Mô tả sản phẩm"
                ></textarea>
              </div>
            </section>

            <aside class="space-y-5">
              <section class="rounded-[24px] border border-slate-100 bg-white p-5">
                <div class="mb-4">
                  <h3 class="text-base font-bold text-slate-800">
                    {{ cheDo === 'them' ? 'Thông tin nhanh' : 'Tóm tắt' }}
                  </h3>
                  <p class="mt-1 text-xs text-slate-400">
                    {{
                      cheDo === 'them'
                        ? 'Lưu xong sẽ mở popup biến thể để bạn thêm màu, size, giá và số lượng.'
                        : 'Theo dõi nhanh trạng thái hiện tại của sản phẩm.'
                    }}
                  </p>
                </div>

                <div v-if="cheDo === 'sua'" class="space-y-4">
                  <div class="overflow-hidden rounded-md border border-slate-100 bg-slate-50">
                    <div class="aspect-square">
                      <img
                        v-if="hinhAnhChinh"
                        :src="hinhAnhChinh.url"
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
                      <span class="font-semibold text-slate-700">{{ sanPhamDaChon?.ma }}</span>
                    </div>
                    <div class="flex items-center justify-between gap-3">
                      <span class="text-slate-400">Trạng thái</span>
                      <span class="admin-status-chip whitespace-nowrap" :class="layLopTrangThai(sanPhamDaChon?.trangThai)">
                        {{ layNhanTrangThai(sanPhamDaChon?.trangThai) }}
                      </span>
                    </div>
                  </div>

                  <div class="flex flex-wrap gap-2">
                    <span
                      v-for="attribute in layDanhSachThuocTinh(sanPhamDaChon)"
                      :key="attribute"
                      class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600"
                    >
                      {{ attribute }}
                    </span>
                  </div>
                </div>

                <div v-else class="space-y-4">
                  <div class="rounded-md border border-slate-100 bg-slate-50 p-4">
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
                      v-if="formSanPham.thuongHieuId"
                      class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600"
                    >
                      {{ layTenThuongHieu(formSanPham.thuongHieuId) }}
                    </span>
                    <span
                      v-if="formSanPham.loaiGiayId"
                      class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600"
                    >
                      {{ layTenLoaiGiay(formSanPham.loaiGiayId) }}
                    </span>
                    <span
                      v-if="formSanPham.gioiTinh"
                      class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600"
                    >
                      {{ layNhanGioiTinh(formSanPham.gioiTinh) }}
                    </span>
                  </div>
                </div>
              </section>
            </aside>
          </div>
        </div>

        <div class="flex justify-end gap-3 border-t border-slate-100 px-6 py-4">
          <button
            class="rounded-md border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
            @click="emit('dong')"
          >
            Hủy
          </button>
          <button
            :disabled="dangLuu || dangTai"
            class="rounded-md bg-rose-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-rose-600 disabled:cursor-not-allowed disabled:opacity-60"
            @click="emit('luu')"
          >
            {{ dangLuu ? 'Đang lưu...' : (cheDo === 'them' ? 'Lưu sản phẩm' : 'Lưu thay đổi') }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
