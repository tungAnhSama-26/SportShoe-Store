<script setup>
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";

const {
  Button,
  Search,
  Trash2,
  hienModalSanPham,
  dangCapNhat,
  donDaHoanThanh,
  tuKhoaSanPham,
  dangTimKiem,
  ketQuaTimKiem,
  danhSachSanPhamUpdate,
  timKiemSanPham,
  themSanPham,
  removeSanPham,
  handleSaveSanPham,
  dinhDangTien,
} = useInvoiceDetailContext();
</script>

<template>
  <div
    v-if="hienModalSanPham"
    class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm"
  >
    <div class="w-full max-w-4xl overflow-hidden rounded-[32px] bg-white shadow-2xl">
      <div class="grid lg:grid-cols-2">
        <div class="border-r border-slate-100 p-7">
          <h3 class="text-xl font-bold text-slate-800">Tìm Sản Phẩm</h3>
          <div class="relative mt-6">
            <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="tuKhoaSanPham"
              @input="timKiemSanPham"
              type="text"
              placeholder="Tên Sản Phẩm, SKU..."
              class="h-12 w-full rounded-[6px] border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            />
          </div>
          <div class="mt-6 max-h-[420px] space-y-3 overflow-y-auto pr-2">
            <div v-if="dangTimKiem" class="py-10 text-center text-sm text-slate-400">
              Đang Tìm Kiếm...
            </div>
            <div
              v-else-if="!ketQuaTimKiem.length"
              class="py-10 text-center text-sm text-slate-400"
            >
              Nhập Từ Khóa Để Tìm Sản Phẩm
            </div>
            <div
              v-for="sp in ketQuaTimKiem"
              :key="sp.chiTietId"
              @click="themSanPham(sp)"
              class="cursor-pointer rounded-[6px] border border-slate-100 p-4 transition hover:border-rose-200 hover:bg-rose-50/40"
            >
              <div class="flex items-center justify-between gap-4">
                <div>
                  <p class="font-semibold text-slate-800">{{ sp.tenSanPham }}</p>
                  <p class="mt-1 text-xs text-slate-400">
                    {{ sp.mauSac }} · {{ sp.kichCo }} · Tồn: {{ sp.soLuongTon }}
                  </p>
                </div>
                <p class="font-semibold text-[#B82220]">
                  {{ dinhDangTien(sp.giaBan) }}
                </p>
              </div>
            </div>
          </div>
        </div>
        <div class="bg-slate-50/60 p-7">
          <h3 class="text-xl font-bold text-slate-800">Chỉnh Sửa Đơn Hàng</h3>
          <div class="mt-6 max-h-[420px] space-y-3 overflow-y-auto pr-2">
            <div
              v-if="!danhSachSanPhamUpdate.length"
              class="py-10 text-center text-sm text-slate-400"
            >
              Chưa Có Sản Phẩm Nào
            </div>
            <div
              v-for="item in danhSachSanPhamUpdate"
              :key="item.chiTietId"
              class="flex items-center gap-4 rounded-[6px] bg-white p-4 shadow-sm"
            >
              <div class="flex-1">
                <p class="font-semibold text-slate-800">{{ item.tenSanPham }}</p>
                <p class="mt-1 text-xs text-slate-400">{{ item.maBienThe }}</p>
              </div>
              <input
                v-model.number="item.soLuong"
                type="number"
                min="1"
                class="h-10 w-16 rounded-[6px] border border-slate-200 bg-slate-50 text-center text-sm font-semibold outline-none focus:border-rose-300"
              />
              <button
                @click="removeSanPham(item.chiTietId)"
                class="rounded-[6px] bg-[#B82220]/10 p-2 text-[#B82220] transition hover:bg-[#B82220]/20"
              >
                <Trash2 class="h-4 w-4" />
              </button>
            </div>
          </div>
          <div class="mt-6 border-t border-slate-200 pt-5">
            <div class="flex items-center justify-between text-sm font-semibold">
              <span class="text-slate-700">Tổng Tiền Hàng</span>
              <span class="text-[#B82220]">{{
                dinhDangTien(
                  danhSachSanPhamUpdate.reduce(
                    (t, i) => t + i.giaBan * i.soLuong,
                    0,
                  ),
                )
              }}</span>
            </div>
            <div class="mt-5 flex gap-3">
              <Button @click="hienModalSanPham = false" variant="soft" class="flex-1">
                Đóng
              </Button>
              <Button
                @click="handleSaveSanPham"
                :disabled="dangCapNhat || donDaHoanThanh"
                class="flex-1 bg-amber-500 hover:bg-amber-600 text-white border-transparent"
              >
                {{ dangCapNhat ? "Đang Lưu..." : "Lưu Thay Đổi" }}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
