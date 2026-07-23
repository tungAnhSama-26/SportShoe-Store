<script setup>
import AdminTableFooter from "../../../../../components/common/AdminTableFooter.vue";
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";
import {
  handleProductImageError,
  resolveProductImageUrl,
} from "../utils/productImage";

const {
  Card,
  Package,
  Search,
  hoaDon,
  tuKhoaLocSanPham,
  loaiSanPhamDangLoc,
  sapXepSanPham,
  danhSachLoaiSanPham,
  giaTuSanPhamSo,
  giaDenSanPhamSo,
  giaLonNhatSanPham,
  nhanKhoangGiaSanPham,
  styleKhoangGiaSanPham,
  danhSachSanPhamPhanTrang,
  trangSanPhamHienTai,
  soSanPhamMoiTrang,
  danhSachSanPhamDaLoc,
  tongTrangSanPham,
  hienPhanTrangSanPham,
  vietHoaChuCaiDau,
  dinhDangGio,
  dinhDangNgay,
  dinhDangTien,
} = useInvoiceDetailContext();
</script>

<template>
  <section class="grid gap-3">
    <Card class="px-6 py-5">
      <template #header>
        <div>
          <h2
            class="flex shrink-0 items-center gap-2 whitespace-nowrap text-base font-semibold text-slate-700"
          >
            <span
              class="flex h-9 w-9 items-center justify-center rounded-[6px] bg-slate-50 text-slate-500"
            >
              <Package class="h-5 w-5" />
            </span>
            <span>Danh Sách Sản Phẩm ({{ hoaDon.sanPham?.length || 0 }})</span>
          </h2>
        </div>
      </template>
      <div
        class="mb-6 space-y-4 rounded-[6px] border border-slate-100 bg-slate-50/50 p-4"
      >
        <div
          class="grid gap-3 lg:grid-cols-[minmax(260px,1.35fr)_minmax(190px,0.85fr)_minmax(210px,0.9fr)]"
        >
          <label class="block">
            <span class="mb-1.5 block text-xs font-semibold text-slate-500"
              >Tìm kiếm</span
            >
            <div class="relative">
              <Search
                class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
              />
              <input
                v-model.trim="tuKhoaLocSanPham"
                type="text"
                class="h-11 w-full rounded-[6px] border border-slate-200 bg-white pl-9 pr-3 text-sm font-medium text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-[#B82220] focus:ring-2 focus:ring-rose-100"
                placeholder="Tên sản phẩm, màu, size..."
              />
            </div>
          </label>
          <label class="block">
            <span class="mb-1.5 block text-xs font-semibold text-slate-500"
              >Loại sản phẩm</span
            >
            <select
              v-model="loaiSanPhamDangLoc"
              class="h-11 w-full rounded-[6px] border border-slate-200 bg-white px-3 text-sm font-medium text-slate-700 outline-none transition focus:border-[#B82220] focus:ring-2 focus:ring-rose-100"
            >
              <option value="">Tất cả loại</option>
              <option v-for="loai in danhSachLoaiSanPham" :key="loai" :value="loai">
                {{ loai }}
              </option>
            </select>
          </label>
          <label class="block">
            <span class="mb-1.5 block text-xs font-semibold text-slate-500"
              >Sắp xếp</span
            >
            <select
              v-model="sapXepSanPham"
              class="h-11 w-full rounded-[6px] border border-slate-200 bg-white px-3 text-sm font-medium text-slate-700 outline-none transition focus:border-[#B82220] focus:ring-2 focus:ring-rose-100"
            >
              <option value="macDinh">Mặc định</option>
              <option value="giaTang">Giá thấp đến cao</option>
              <option value="giaGiam">Giá cao đến thấp</option>
              <option value="soLuongGiam">Số lượng nhiều nhất</option>
              <option value="tongTienGiam">Tổng tiền cao nhất</option>
            </select>
          </label>
        </div>
        <div class="w-full">
          <div
            class="mb-3 flex items-center justify-between gap-4 text-xs font-semibold text-slate-400"
          >
            <span>Khoảng giá</span>
            <span class="text-right text-slate-500">{{
              nhanKhoangGiaSanPham
            }}</span>
          </div>
          <div class="relative h-8 w-full">
            <div
              class="absolute left-[9px] right-[9px] top-1/2 h-1 -translate-y-1/2 rounded-full"
              :style="styleKhoangGiaSanPham"
            ></div>
            <input
              v-model.number="giaTuSanPhamSo"
              type="range"
              min="0"
              :max="giaLonNhatSanPham || 0"
              step="1"
              class="price-range-input"
              aria-label="Giá từ"
            />
            <input
              v-model.number="giaDenSanPhamSo"
              type="range"
              min="0"
              :max="giaLonNhatSanPham || 0"
              step="1"
              class="price-range-input"
              aria-label="Giá đến"
            />
          </div>
        </div>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full table-auto text-[15px]">
          <thead>
            <tr class="bg-slate-100 text-left text-xs font-bold tracking-wide text-slate-950">
              <th class="rounded-l-2xl px-5 py-3.5">STT</th>
              <th class="px-5 py-3.5">Mã SPCT</th>
              <th class="px-5 py-3.5">Ảnh</th>
              <th class="px-5 py-3.5">Sản Phẩm</th>
              <th class="px-5 py-3.5">Màu Sắc</th>
              <th class="px-5 py-3.5">Size</th>
              <th class="px-5 py-3.5">Số Lượng</th>
              <th class="px-5 py-3.5">Thời Gian</th>
              <th class="px-5 py-3.5">Giảm Giá</th>
              <th class="rounded-r-2xl px-5 py-3.5">Đơn Giá</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!danhSachSanPhamPhanTrang.length">
              <td colspan="10" class="px-5 py-10 text-center text-sm text-slate-400">
                Không có sản phẩm phù hợp với bộ lọc.
              </td>
            </tr>
            <tr
              v-for="(item, index) in danhSachSanPhamPhanTrang"
              :key="item.id"
              class="border-b border-slate-100 last:border-b-0"
            >
              <td class="px-5 py-6 font-semibold text-slate-600">
                {{ (trangSanPhamHienTai - 1) * soSanPhamMoiTrang + index + 1 }}
              </td>
              <td class="px-5 py-6 font-medium text-slate-600">
                {{ item.maBienThe || '—' }}
              </td>
              <td class="px-5 py-6">
                <img
                  :src="resolveProductImageUrl(item.hinhAnh)"
                  @error="handleProductImageError"
                  class="h-14 w-14 rounded-[6px] object-cover"
                />
              </td>
              <td class="px-5 py-6">
                <p class="text-base font-semibold text-slate-800">
                  {{ vietHoaChuCaiDau(item.tenSanPham) }}
                </p>
                <p class="mt-1 text-sm text-slate-400">{{ item.phanLoai }}</p>
              </td>
              <td class="px-5 py-6 text-slate-600">
                {{ item.mauSac || '—' }}
              </td>
              <td class="px-5 py-6 text-slate-600">
                {{ item.kichCo || '—' }}
              </td>
              <td class="px-5 py-6 font-semibold text-slate-700">
                {{ item.soLuong }}
              </td>
              <td class="px-5 py-6">
                <p class="text-sm font-semibold text-slate-700">
                  {{ dinhDangGio(hoaDon.ngayTao) }}
                </p>
                <p class="text-sm text-slate-400">
                  {{ dinhDangNgay(hoaDon.ngayTao) }}
                </p>
              </td>
              <td class="px-5 py-6">
                <span
                  v-if="item.giaTriGiamDotGiamGia"
                  class="inline-flex items-center gap-1 rounded bg-rose-50 px-2.5 py-1 text-xs font-bold text-rose-600"
                  :title="item.tenDotGiamGia"
                >
                  -{{ item.giaTriGiamDotGiamGia }}%
                </span>
                <span v-else class="text-slate-400">—</span>
              </td>
              <td class="px-5 py-6 text-[#B82220]">
                <div v-if="item.giaBanChiTiet && Number(item.giaBanChiTiet) > Number(item.donGia)">
                  <p class="text-xs text-slate-400 line-through mb-0.5">
                    {{ dinhDangTien(item.giaBanChiTiet) }}
                  </p>
                  <p class="text-sm font-semibold">
                    {{ dinhDangTien(item.donGia) }}
                  </p>
                </div>
                <div v-else class="font-semibold">
                  {{ dinhDangTien(item.donGia) }}
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <AdminTableFooter
        v-if="hienPhanTrangSanPham"
        :current-page="trangSanPhamHienTai"
        :page-size="soSanPhamMoiTrang"
        :page-size-options="[5]"
        :total-items="danhSachSanPhamDaLoc.length"
        :total-pages="tongTrangSanPham"
        compact
        @update:current-page="trangSanPhamHienTai = $event"
        @update:page-size="() => {}"
      />
    </Card>
  </section>
</template>

<style scoped>
.price-range-input {
  display: block;
  pointer-events: none;
  position: absolute;
  left: 0;
  top: 50%;
  height: 18px;
  width: 100%;
  margin: 0;
  transform: translateY(-50%);
  appearance: none;
  background: transparent;
}
.price-range-input::-webkit-slider-thumb {
  pointer-events: auto;
  height: 18px;
  width: 18px;
  margin-top: -7px;
  appearance: none;
  border: 3px solid #ffffff;
  border-radius: 9999px;
  background: #ffffff;
  box-shadow:
    0 0 0 1px #d9dde6,
    0 4px 12px rgba(184, 34, 32, 0.2);
  cursor: pointer;
}
.price-range-input::-moz-range-thumb {
  pointer-events: auto;
  height: 18px;
  width: 18px;
  border: 3px solid #ffffff;
  border-radius: 9999px;
  background: #ffffff;
  box-shadow:
    0 0 0 1px #d9dde6,
    0 4px 12px rgba(184, 34, 32, 0.2);
  cursor: pointer;
}
.price-range-input::-webkit-slider-runnable-track {
  height: 4px;
  background: transparent;
}
.price-range-input::-moz-range-track {
  height: 4px;
  background: transparent;
}
</style>
