<script setup>
import { onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  Eye,
  FileSpreadsheet,
  Filter,
  Package,
  Plus,
  RotateCcw,
  Search,
} from "lucide-vue-next";
import * as api from "../../../services/san-pham-api";
import AdminQuickStatusAction from "../../../components/common/AdminQuickStatusAction.vue";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import BoLocSanPham from '../../../components/admin/san-pham/BoLocSanPham.vue'
import BangSanPham from '../../../components/admin/san-pham/BangSanPham.vue'
import QuanLySanPhamModal from '../../../components/admin/san-pham/QuanLySanPhamModal.vue'
import Card from "../../../components/ui/Card.vue";
import Table from "../../../components/ui/Table.vue";
import Button from "../../../components/ui/Button.vue";
import Badge from "../../../components/ui/Badge.vue";
import { exportRowsToExcel } from "../../../utils/export-excel";
import { getDisplayErrorMessage } from "../../../utils/error-message";
import { showSuccess, showError } from "../../../utils/alert";
import { useRealtime } from "../../../composables/useRealtime";

const { subscribeTopic } = useRealtime();

const router = useRouter();

const loading = ref(false);
const items = ref([]);
const danhMuc = ref(null);
const currentPage = ref(0);
const pageSize = ref(5);
const totalItems = ref(0);
const totalPages = ref(0);
const updatingStatusIds = reactive(new Set());

const boLoc = reactive({
  tuKhoa: "",
  thuongHieuId: null,
  loaiGiayId: null,
  trangThai: null,
});

const thongBao = reactive({
  show: false,
  message: "",
  type: "success",
});

const pageSizeOptions = [5, 10, 20, 50];
let toastTimer = null;
let latestLoadRequestId = 0;
let keywordSearchTimer = null;

function showToast(message, type = "success") {
  if (type === "success") {
    showSuccess(message);
    return;
  }

  if (type === "error") {
    showError(message);
    return;
  }

  if (toastTimer) clearTimeout(toastTimer);
  thongBao.message = message;
  thongBao.type = type;
  thongBao.show = true;
  toastTimer = setTimeout(() => {
    thongBao.show = false;
    toastTimer = null;
  }, 3000);
}

function formatCurrency(value) {
  return Number(value || 0).toLocaleString("vi-VN");
}

function normalizeFilterNumber(value) {
  if (value === null || value === undefined || value === "") return undefined;
  const parsed = Number(value);
  return Number.isInteger(parsed) ? parsed : undefined;
}

function formatPriceRange(minValue, maxValue) {
  if (minValue == null && maxValue == null) return "Chưa có giá";
  if (minValue === maxValue || maxValue == null)
    return `${formatCurrency(minValue)} đ`;
  return `${formatCurrency(minValue)} đ - ${formatCurrency(maxValue)} đ`;
}

function formatPriceParts(minValue, maxValue) {
  if (minValue == null && maxValue == null) {
    return {
      start: "Chưa có giá",
      end: "",
      isRange: false,
    };
  }

  const startText = `${formatCurrency(minValue ?? maxValue)} đ`;
  const endText = `${formatCurrency(maxValue ?? minValue)} đ`;

  if (minValue === maxValue || maxValue == null) {
    return {
      start: startText,
      end: "",
      isRange: false,
    };
  }

  return {
    start: startText,
    end: endText,
    isRange: true,
  };
}

function giaHienThi(item) {
  return formatPriceRange(item.giaMin, item.giaMax);
}

function giaGocHienThi(item) {
  return formatPriceRange(item.giaGocMin, item.giaGocMax);
}

function giaTrongBang(item) {
  return formatPriceParts(item.giaMin, item.giaMax);
}

function giaGocTrongBang(item) {
  return formatPriceParts(item.giaGocMin, item.giaGocMax);
}

function hasOriginalPrice(item) {
  if (!item?.coGiamGia) return false;
  if (item.giaGocMin == null && item.giaGocMax == null) return false;

  const currentMin = Number(item.giaMin ?? 0);
  const currentMax = Number(item.giaMax ?? item.giaMin ?? 0);
  const originalMin = Number(item.giaGocMin ?? 0);
  const originalMax = Number(item.giaGocMax ?? item.giaGocMin ?? 0);

  return originalMin !== currentMin || originalMax !== currentMax;
}

function trangThaiLabel(value) {
  if (value === 1) return "Kinh doanh";
  if (value === 2) return "Hết hàng";
  return "Ngừng bán";
}

function trangThaiClass(value) {
  if (value === 1) return "bg-emerald-50 text-emerald-600";
  if (value === 2) return "bg-amber-50 text-amber-600";
  return "bg-rose-50 text-rose-600";
}

function trangThaiKeTiep(item) {
  return Number(item.trangThai) === 0 ? 1 : 0;
}

function coTheChuyenTrangThaiNhanh(item) {
  return Number(item.trangThai) !== 0 || Number(item.tongSoLuong || 0) > 0;
}

function isUpdatingStatus(id) {
  return updatingStatusIds.has(id);
}

function nhanChuyenTrangThaiNhanh(item) {
  return Number(item.trangThai) === 0
    ? "Chuyển sang kinh doanh"
    : "Chuyển sang ngừng kinh doanh";
}

function hanhDongChuyenTrangThaiNhanh(item) {
  return Number(item.trangThai) === 0 ? "activate" : "deactivate";
}

function tieuDeKhiKhongTheChuyenTrangThai(item) {
  return coTheChuyenTrangThaiNhanh(item)
    ? nhanChuyenTrangThaiNhanh(item)
    : "Hết hàng chưa thể chuyển sang kinh doanh";
}

function tinNhanXacNhanChuyenTrangThai(item) {
  const action =
    trangThaiKeTiep(item) === 1 ? "kinh doanh" : "ngừng kinh doanh";
  return `Bạn có muốn chuyển sản phẩm "${item.ten}" sang ${action} không?`;
}

async function loadDanhMuc() {
  try {
    danhMuc.value = await api.layDanhMuc();
  } catch (error) {
    showToast(
      getDisplayErrorMessage(error, "Không tải được danh mục sản phẩm"),
      "error",
    );
  }
}

async function loadData(page = 0) {
  const requestId = ++latestLoadRequestId;
  loading.value = true;
  try {
    const response = await api.layDanhSachGiay({
      keyword: boLoc.tuKhoa.trim() || undefined,
      thuongHieuId: normalizeFilterNumber(boLoc.thuongHieuId),
      loaiGiayId: normalizeFilterNumber(boLoc.loaiGiayId),
      trangThai: normalizeFilterNumber(boLoc.trangThai),
      page,
      size: pageSize.value,
    });
    if (requestId !== latestLoadRequestId) return;
    items.value = response.items || [];
    totalItems.value = response.totalItems;
    totalPages.value = response.totalPages;
    currentPage.value = response.page;
  } catch (error) {
    if (requestId !== latestLoadRequestId) return;
    showToast(
      getDisplayErrorMessage(error, "Không tải được danh sách sản phẩm"),
      "error",
    );
  } finally {
    if (requestId !== latestLoadRequestId) return;
    loading.value = false;
  }
}

function datLaiBoLoc() {
  boLoc.tuKhoa = "";
  boLoc.thuongHieuId = null;
  boLoc.loaiGiayId = null;
  boLoc.trangThai = null;
  loadData(0);
}

function scheduleKeywordSearch() {
  if (keywordSearchTimer) clearTimeout(keywordSearchTimer);
  keywordSearchTimer = setTimeout(() => {
    loadData(0);
    keywordSearchTimer = null;
  }, 300);
}

function goToForm() {
  router.push({ name: "admin-san-pham-them" });
}

function goToChiTietList(item) {
  router.push({
    name: "admin-bien-the-san-pham",
    query: { giayId: String(item.id) },
  });
}

async function xuLyDoiTrangThai(item) {
  if (isUpdatingStatus(item.id)) return;
  if (!coTheChuyenTrangThaiNhanh(item)) {
    showToast("Sản phẩm hết hàng chưa thể chuyển sang kinh doanh", "error");
    return;
  }
  updatingStatusIds.add(item.id);
  try {
    await api.doiTrangThai(item.id, trangThaiKeTiep(item));
    showToast("Cập nhật trạng thái sản phẩm thành công");
    await loadData(currentPage.value);
  } catch (error) {
    showToast(
      getDisplayErrorMessage(error, "Không thể cập nhật trạng thái sản phẩm"),
      "error",
    );
  } finally {
    updatingStatusIds.delete(item.id);
  }
}

function handlePageSizeChange(size) {
  pageSize.value = size;
  loadData(0);
}

async function xuatExcel() {
  if (!totalItems.value) {
    showToast("Không có dữ liệu để xuất Excel", "error");
    return;
  }

  try {
    const response = await api.layDanhSachGiay({
      keyword: boLoc.tuKhoa.trim() || undefined,
      thuongHieuId: normalizeFilterNumber(boLoc.thuongHieuId),
      loaiGiayId: normalizeFilterNumber(boLoc.loaiGiayId),
      trangThai: normalizeFilterNumber(boLoc.trangThai),
      page: 0,
      size: Math.max(totalItems.value, pageSize.value),
    });

    const exported = exportRowsToExcel({
      filename: "danh-sach-san-pham",
      sheetName: "SanPham",
      columns: [
        { label: "STT", value: (_, index) => index + 1 },
        { label: "Mã sản phẩm", key: "ma" },
        { label: "Tên sản phẩm", key: "ten" },
        { label: "Thương hiệu", key: "thuongHieu" },
        { label: "Loại giày", key: "loaiGiay" },
        { label: "Số lượng", value: (row) => row.tongSoLuong || 0 },
        { label: "Giá bán", value: (row) => giaHienThi(row) },
        { label: "Trạng thái", value: (row) => trangThaiLabel(row.trangThai) },
      ],
      rows: response.items || [],
    });

    showToast(
      exported ? "Xuất Excel thành công" : "Không có dữ liệu để xuất Excel",
      exported ? "success" : "error",
    );
  } catch (error) {
    showToast(
      getDisplayErrorMessage(error, "Không thể xuất Excel sản phẩm"),
      "error",
    );
  }
}

function applyStatusFilter(value) {
  filters.trangThai = value;
  loadData(0);
}

onMounted(async () => {
  await loadDanhMuc();
  await loadData(0);

  subscribeTopic('/topic/admin/san-pham', (message) => {
    console.log("Realtime update: Product list changed", message);
    loadData(currentPage.value);
  });

  subscribeTopic('/topic/admin/thuoc-tinh', (message) => {
    console.log("Realtime update: Attribute changed", message);
    loadDanhMuc();
    loadData(currentPage.value);
  });
});

watch(
  () => boLoc.tuKhoa,
  () => {
    scheduleKeywordSearch();
  },
);

onUnmounted(() => {
  if (toastTimer) clearTimeout(toastTimer);
  if (keywordSearchTimer) clearTimeout(keywordSearchTimer);
});
</script>

<template>
  <div class="radius-6px">
    <div class="space-y-5">
      <BoLocSanPham
        :bo-loc="boLoc"
        :danh-muc="danhMuc"
        @reset="lamMoiBoLoc"
        @them-moi="moModalThem"
        @load-data="loadData(0)"
        @xuat-excel="xuatExcel"
      />

      <BangSanPham
        :danh-sach-san-pham="items"
        :dang-tai="loading"
        :trang-hien-tai="currentPage"
        :kich-thuoc-trang="pageSize"
        :tong-so-san-pham="totalItems"
        :tong-so-trang="totalPages"
        :lua-chon-kich-thuoc-trang="pageSizeOptions"
        :danh-sach-id-dang-cap-nhat="danhSachIdDangCapNhat"
        :gia-trong-bang="giaTrongBang"
        :trang-thai-label="trangThaiLabel"
        :co-the-chuyen-trang-thai-nhanh="coTheChuyenTrangThaiNhanh"
        :nhan-chuyen-trang-thai-nhanh="nhanChuyenTrangThaiNhanh"
        :tieu-de-khi-khong-the-chuyen-trang-thai="tieuDeKhiKhongTheChuyenTrangThai"
        :tin-nhan-xac-nhan-chuyen-trang-thai="tinNhanXacNhanChuyenTrangThai"
        :hanh-dong-chuyen-trang-thai-nhanh="hanhDongChuyenTrangThaiNhanh"
        @chuyen-trang-thai="xuLyDoiTrangThai"
        @sua="moModalSua"
        @xem-chi-tiet="goToChiTietList"
        @chuyen-trang="loadData"
        @doi-kich-thuoc-trang="handlePageSizeChange"
      />

      <QuanLySanPhamModal
        :hien-thi-modal="hienThiModal"
        :che-do="cheDoModal"
        :tieu-de="cheDoModal === 'them' ? 'Thêm mới sản phẩm' : 'Sửa sản phẩm'"
        :mo-ta="cheDoModal === 'them' ? 'Thêm một sản phẩm mới vào cửa hàng.' : 'Cập nhật thông tin cơ bản của sản phẩm.'"
        :dang-tai="dangTaiThuocTinh"
        :dang-luu="dangLuu"
        :danh-muc="danhMuc"
        :form-san-pham="formSanPham"
        :loi-san-pham="loiSanPham"
        :san-pham-da-chon="sanPhamDangChon"
        :hinh-anh-chinh="sanPhamDangChon?.hinhAnh?.[0]"
        :lay-ten-thuong-hieu="layTenThuongHieu"
        :lay-ten-loai-giay="layTenLoaiGiay"
        :lay-nhan-gioi-tinh="layNhanGioiTinh"
        :lay-lop-trang-thai="layLopTrangThai"
        :lay-nhan-trang-thai="layNhanTrangThai"
        :lay-danh-sach-thuoc-tinh="layDanhSachThuocTinh"
        @dong="dongModal"
        @luu="luuSanPham"
      />
    </div>

    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="thongBao.show && thongBao.type !== 'success'"
          class="fixed right-5 top-5 z-[90] rounded-md px-4 py-3 text-sm font-medium text-white shadow-lg"
          :class="thongBao.type === 'error' ? 'bg-[#cf1018]' : 'bg-[#ff6a00]'"
        >
          {{ thongBao.message }}
        </div>
      </Transition>
    </Teleport>
  </div>
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
