<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, Package, ShoppingBag } from "lucide-vue-next";
import {
  layChiTietKhachHang,
  layDanhSachDiaChi,
  layHoaDonTheoKhachHang,
} from "../../../services/khach-hang";
import { getDisplayErrorMessage } from "../../../utils/error-message";
import Card from "../../../components/ui/Card.vue";
import Button from "../../../components/ui/Button.vue";
import Table from "../../../components/ui/Table.vue";

const route = useRoute();
const router = useRouter();
const id = route.params.id;

const dangTai = ref(false);
const loiTrang = ref("");
const khachHang = ref(null);
const diaChiMacDinh = ref(null);
const dsHoaDon = ref([]);
const dangTaiDon = ref(false);

function dinhDangNgay(ngay) {
  if (!ngay) return "—";
  return new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(new Date(ngay));
}

function dinhDangTien(n) {
  if (n == null) return "—";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(n);
}

function mauTrangThaiDon(trangThai) {
  const map = {
    "Hoàn thành": "bg-emerald-50 text-emerald-600",
    "Đang giao hàng": "bg-sky-50 text-sky-600",
    "Chờ xác nhận": "bg-amber-50 text-amber-600",
    "Đã xác nhận": "bg-orange-50 text-orange-600",
    "Chờ lấy hàng": "bg-violet-50 text-violet-600",
    "Đã giao hàng": "bg-teal-50 text-teal-600",
    Hủy: "bg-rose-50 text-rose-600",
    "Yêu cầu hủy": "bg-orange-50 text-orange-600",
  };
  return map[trangThai] ?? "bg-slate-50 text-slate-600";
}

function xemChiTietDon(hoaDonId) {
  router.push({ name: "admin-hoa-don-chi-tiet", params: { id: hoaDonId } });
}

async function taiDuLieu() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    const [kh, dsDiaChi] = await Promise.all([
      layChiTietKhachHang(id),
      layDanhSachDiaChi(id),
    ]);
    khachHang.value = kh;
    diaChiMacDinh.value =
      dsDiaChi.find((dc) => dc.laMacDinh) ?? dsDiaChi[0] ?? null;
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(
      e,
      "Không thể tải thông tin khách hàng",
    );
    dangTai.value = false;
    return;
  } finally {
    dangTai.value = false;
  }

  dangTaiDon.value = true;
  try {
    dsHoaDon.value = await layHoaDonTheoKhachHang(id);
  } catch {
    dsHoaDon.value = [];
  } finally {
    dangTaiDon.value = false;
  }
}

onMounted(taiDuLieu);
</script>

<template>
  <div class="space-y-5">
    <!-- Header -->
    <section class="flex items-center gap-4 border-b border-slate-100 pb-4">
      <Button
        variant="ghost"
        size="icon"
        class="rounded-full bg-slate-100 hover:bg-slate-200"
        @click="router.push({ name: 'admin-khach-hang' })"
      >
        <ArrowLeft class="h-5 w-5" />
      </Button>
    </section>

    <div
      v-if="loiTrang"
      class="rounded-2xl bg-rose-50 border border-rose-100 px-5 py-3 text-sm font-medium text-rose-600"
    >
      {{ loiTrang }}
    </div>

    <div
      v-if="dangTai"
      class="rounded-[24px] border bg-white p-10 text-center text-slate-400 text-sm"
    >
      Đang tải...
    </div>

    <template v-else-if="khachHang">
      <div class="flex flex-col xl:flex-row gap-5">
        <!-- ===== THÔNG TIN KHÁCH HÀNG (trái) ===== -->
        <div class="xl:w-72 shrink-0">
          <Card class="text-center sticky top-5">
            <img
              :src="
                khachHang.hinhAnh ||
                'https://ui-avatars.com/api/?name=' +
                  encodeURIComponent(khachHang.hoTen || 'KH') +
                  '&background=f1f5f9&color=475569&size=128'
              "
              :alt="khachHang.hoTen"
              class="mx-auto h-24 w-24 rounded-full object-cover ring-4 ring-slate-100"
            />
            <p class="mt-4 text-base font-bold text-slate-800">
              {{ khachHang.hoTen }}
            </p>
            <p class="text-xs text-slate-400 mt-0.5">
              {{ khachHang.tenDangNhap }}
            </p>

            <span
              class="mt-2 inline-flex rounded-full px-3 py-1 text-[11px] font-semibold"
              :class="
                khachHang.trangThai === 1
                  ? 'bg-emerald-50 text-emerald-600'
                  : 'bg-rose-50 text-rose-600'
              "
            >
              {{ khachHang.tenTrangThai }}
            </span>

            <div
              class="mt-4 space-y-2 text-left text-sm border-t border-slate-100 pt-4"
            >
              <div v-if="khachHang.email" class="flex gap-2">
                <span
                  class="w-16 shrink-0 text-[12px] font-semibold text-slate-400"
                  >Email</span
                >
                <span class="text-slate-700 break-all text-[12px]">{{
                  khachHang.email
                }}</span>
              </div>
              <div v-if="khachHang.sdt" class="flex gap-2">
                <span
                  class="w-16 shrink-0 text-[12px] font-semibold text-slate-400"
                  >SĐT</span
                >
                <span class="text-slate-700 text-[12px]">{{
                  khachHang.sdt
                }}</span>
              </div>
              <div v-if="khachHang.ngaySinh" class="flex gap-2">
                <span
                  class="w-16 shrink-0 text-[12px] font-semibold text-slate-400"
                  >Ngày sinh</span
                >
                <span class="text-slate-700 text-[12px]">{{
                  dinhDangNgay(khachHang.ngaySinh)
                }}</span>
              </div>
              <div v-if="khachHang.tenGioiTinh" class="flex gap-2">
                <span
                  class="w-16 shrink-0 text-[12px] font-semibold text-slate-400"
                  >Giới tính</span
                >
                <span class="text-slate-700 text-[12px]">{{
                  khachHang.tenGioiTinh
                }}</span>
              </div>
              <div v-if="diaChiMacDinh" class="flex gap-2">
                <span
                  class="w-16 shrink-0 text-[12px] font-semibold text-slate-400"
                  >Địa chỉ</span
                >
                <span class="text-slate-600 text-[12px] leading-relaxed">
                  {{ diaChiMacDinh.diaChiCuThe }}, {{ diaChiMacDinh.phuongXa }},
                  {{ diaChiMacDinh.tinhThanh }}
                </span>
              </div>
            </div>

            <div class="mt-4 border-t border-slate-100 pt-4">
              <Button
                variant="outline"
                class="w-full justify-center text-sm"
                @click="
                  router.push({
                    name: 'admin-khach-hang-chi-tiet',
                    params: { id },
                  })
                "
              >
                Xem chi tiết tài khoản
              </Button>
            </div>
          </Card>
        </div>

        <!-- ===== BẢNG ĐƠN HÀNG (phải) ===== -->
        <div class="flex-1 min-w-0">
          <Card no-padding>
            <template #icon>
              <div
                class="flex h-9 w-9 items-center justify-center rounded-xl bg-violet-50 text-violet-500"
              >
                <ShoppingBag class="h-4 w-4" />
              </div>
            </template>
            <template #title>
              Lịch sử đơn hàng
              <span
                v-if="dsHoaDon.length"
                class="ml-2 inline-flex items-center rounded-full bg-violet-100 px-2 py-0.5 text-xs font-bold text-violet-600"
              >
                {{ dsHoaDon.length }}
              </span>
            </template>

            <div class="p-6">
              <div
                v-if="dangTaiDon"
                class="py-12 text-center text-sm text-slate-400"
              >
                Đang tải đơn hàng...
              </div>

              <div v-else-if="!dsHoaDon.length" class="py-14 text-center">
                <Package class="h-12 w-12 mx-auto mb-3 text-slate-200" />
                <p class="text-sm text-slate-400">
                  Khách hàng chưa có đơn hàng nào.
                </p>
              </div>

              <div v-else class="admin-table-scroll">
                <Table>
                  <template #header>
                    <th class="px-3 py-3 whitespace-nowrap">#</th>
                    <th class="px-3 py-3 whitespace-nowrap">Mã đơn hàng</th>
                    <th class="px-3 py-3 whitespace-nowrap">Trạng thái</th>
                    <th class="px-3 py-3 whitespace-nowrap">Loại</th>
                    <th class="px-3 py-3 whitespace-nowrap">Tổng cộng</th>
                    <th class="px-3 py-3 whitespace-nowrap">Ngày tạo</th>
                    <th class="px-3 py-3 text-center whitespace-nowrap">
                      Chi tiết
                    </th>
                  </template>
                  <template #body>
                    <tr
                      v-for="(hd, idx) in dsHoaDon"
                      :key="hd.id"
                      class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100"
                    >
                      <td
                        class="rounded-l-2xl px-3 py-3 font-semibold text-slate-400"
                      >
                        {{ idx + 1 }}
                      </td>
                      <td
                        class="px-3 py-3 font-mono font-semibold text-slate-800 text-xs"
                      >
                        {{ hd.maHoaDon }}
                      </td>
                      <td class="px-3 py-3">
                        <span
                          class="inline-flex rounded-full px-2.5 py-0.5 text-[11px] font-semibold"
                          :class="mauTrangThaiDon(hd.trangThai)"
                        >
                          {{ hd.trangThai }}
                        </span>
                      </td>
                      <td class="px-3 py-3 text-slate-600 text-xs">
                        {{ hd.loaiDon }}
                      </td>
                      <td class="px-3 py-3 font-semibold text-slate-800">
                        {{ dinhDangTien(hd.tongTien) }}
                      </td>
                      <td class="px-3 py-3 text-slate-500 text-xs">
                        {{ dinhDangNgay(hd.ngayTao) }}
                      </td>
                      <td class="rounded-r-2xl px-3 py-3 text-center">
                        <button
                          @click="xemChiTietDon(hd.id)"
                          class="inline-flex items-center gap-1 rounded-lg bg-violet-50 px-3 py-1 text-[11px] font-semibold text-violet-600 hover:bg-violet-100 transition"
                        >
                          Xem chi tiết
                        </button>
                      </td>
                    </tr>
                  </template>
                </Table>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </template>
  </div>
</template>
