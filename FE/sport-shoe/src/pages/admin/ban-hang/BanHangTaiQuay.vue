<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { Award, Box, Feather, Footprints, MoveVertical, Palette, Ruler, Weight } from "lucide-vue-next";
import {
  huyHoaDonCho,
  layChiTietHoaDonCho,
  layDanhSachHoaDonCho,
  thanhToanTaiQuay,
  taoHoaDonCho,
  timKhachHangTheoSoDienThoai,
  timSanPhamTaiQuay,
  type HoaDonChoChiTiet,
  type HoaDonChoTomTat,
  type KhachHangTaiQuay,
  type SanPhamTaiQuay,
} from "../../../services/ban-hang-tai-quay";

interface GioHangItem {
  chiTietId: number;
  maSanPham: string;
  tenSanPham: string;
  soLuong: number;
  giaBan: number;
  soLuongTon: number;
}

const customerKeyword = ref("");
const productKeyword = ref("");
const customerResults = ref<KhachHangTaiQuay[]>([]);
const productResults = ref<SanPhamTaiQuay[]>([]);
const selectedProductDetail = ref<SanPhamTaiQuay | null>(null);
const selectedColor = ref("");
const selectedSize = ref("");
const selectedQuantity = ref(1);
const selectedCustomer = ref<KhachHangTaiQuay | null>(null);
const cartItems = ref<GioHangItem[]>([]);
const pendingInvoices = ref<HoaDonChoTomTat[]>([]);
const activePendingInvoice = ref<HoaDonChoTomTat | null>(null);

const loadingCustomers = ref(false);
const loadingProducts = ref(false);
const loadingPendingInvoices = ref(false);
const savingPendingInvoice = ref(false);
const cancelingPendingInvoice = ref(false);
const payingInvoice = ref(false);
const invoiceLoading = ref(false);
const showCustomerDropdown = ref(false);
const showProductDropdown = ref(false);
const pageError = ref("");
const successMessage = ref("");
const paymentMethod = ref(1);
const amountPaid = ref("");
const paymentNote = ref("");

let customerTimer: number | undefined;
let productTimer: number | undefined;

const tongSoLuong = computed(() =>
  cartItems.value.reduce((total, item) => total + item.soLuong, 0),
);
const tongTien = computed(() =>
  cartItems.value.reduce((total, item) => total + item.soLuong * item.giaBan, 0),
);
const productSearchLabel = computed(() =>
  productKeyword.value.trim() ? "Kết quả tìm kiếm sản phẩm" : "Sản phẩm tại quầy",
);
const daChonKhach = computed(
  () =>
    Boolean(selectedCustomer.value) ||
    Boolean(activePendingInvoice.value) ||
    customerKeyword.value.trim().toLowerCase() === "khách vãng lai",
);
const khachCanTra = computed(() => tongTien.value);
const tienKhachThanhToan = computed(() => {
  const parsed = Number(amountPaid.value.replace(/[^\d]/g, ""));
  return Number.isFinite(parsed) ? parsed : 0;
});
const tienThua = computed(() => {
  if (paymentMethod.value !== 1) {
    return 0;
  }
  return Math.max(tienKhachThanhToan.value - khachCanTra.value, 0);
});
const canCreatePendingInvoice = computed(() => cartItems.value.length > 0 && !savingPendingInvoice.value);
const canPay = computed(() => {
  if (!cartItems.value.length || payingInvoice.value) {
    return false;
  }
  if (paymentMethod.value === 1) {
    return tienKhachThanhToan.value >= khachCanTra.value;
  }
  return true;
});

function dinhDangTien(value: number) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value || 0);
}

function clearFeedback() {
  pageError.value = "";
  successMessage.value = "";
}

function soLuongDaChon(chiTietId: number) {
  return cartItems.value.find((item) => item.chiTietId === chiTietId)?.soLuong ?? 0;
}

function soLuongConLai(chiTietId: number, soLuongTon: number) {
  return Math.max(soLuongTon - soLuongDaChon(chiTietId), 0);
}

function laySoLuongTonHienTai(chiTietId: number, fallback: number) {
  return productResults.value.find((product) => product.chiTietId === chiTietId)?.soLuongTon ?? fallback;
}

const productDetailFields = computed(() => {
  if (!selectedProductDetail.value) {
    return [];
  }

  return [
    { label: "Loại giày", value: selectedProductDetail.value.loaiGiay || "--", icon: Box },
    { label: "Thương hiệu", value: selectedProductDetail.value.thuongHieu || "--", icon: Award },
    { label: "Đế giày", value: selectedProductDetail.value.deGiay || "--", icon: Footprints },
    { label: "Cổ giày", value: selectedProductDetail.value.coGiay || "--", icon: MoveVertical },
    { label: "Công nghệ đệm", value: selectedProductDetail.value.congNgheDem || "--", icon: Feather },
    { label: "Màu sắc", value: selectedProductDetail.value.mauSac || "--", icon: Palette },
    { label: "Kích cỡ", value: selectedProductDetail.value.kichCo || "--", icon: Ruler },
    { label: "Trọng lượng", value: selectedProductDetail.value.trongLuong || "--", icon: Weight },
  ];
});
const relatedVariants = computed(() => {
  if (!selectedProductDetail.value) {
    return [];
  }

  return productResults.value.filter(
    (product) =>
      product.maSanPham === selectedProductDetail.value?.maSanPham &&
      product.tenSanPham === selectedProductDetail.value?.tenSanPham,
  );
});
const colorOptions = computed(() => {
  const grouped = new Map<string, SanPhamTaiQuay>();

  for (const variant of relatedVariants.value) {
    const key = variant.mauSac || variant.maBienThe;
    if (!grouped.has(key)) {
      grouped.set(key, variant);
    }
  }

  return Array.from(grouped.values());
});
const sizeOptions = computed(() =>
  relatedVariants.value.filter((variant) => {
    if (!selectedColor.value) {
      return true;
    }
    return (variant.mauSac || variant.maBienThe) === selectedColor.value;
  }),
);
const selectedVariant = computed(() => {
  if (!selectedProductDetail.value) {
    return null;
  }

  return (
    relatedVariants.value.find(
      (variant) =>
        (selectedColor.value ? (variant.mauSac || variant.maBienThe) === selectedColor.value : true) &&
        (selectedSize.value ? (variant.kichCo || "") === selectedSize.value : true),
    ) || selectedProductDetail.value
  );
});

function resetDraft() {
  selectedCustomer.value = null;
  customerKeyword.value = "";
  productKeyword.value = "";
  customerResults.value = [];
  productResults.value = [];
  selectedProductDetail.value = null;
  selectedColor.value = "";
  selectedSize.value = "";
  selectedQuantity.value = 1;
  cartItems.value = [];
  activePendingInvoice.value = null;
  paymentMethod.value = 1;
  amountPaid.value = "";
  paymentNote.value = "";
  showCustomerDropdown.value = false;
  showProductDropdown.value = false;
  clearFeedback();
  void fetchProducts("");
}

async function fetchPendingInvoices() {
  loadingPendingInvoices.value = true;
  try {
    pendingInvoices.value = await layDanhSachHoaDonCho();
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Không thể tải danh sách hóa đơn chờ";
  } finally {
    loadingPendingInvoices.value = false;
  }
}

async function fetchCustomers(keyword: string) {
  if (!keyword.trim() || keyword.trim().toLowerCase() === "khách vãng lai") {
    customerResults.value = [];
    return;
  }

  loadingCustomers.value = true;
  try {
    customerResults.value = await timKhachHangTheoSoDienThoai(keyword);
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Không thể tìm khách hàng";
  } finally {
    loadingCustomers.value = false;
  }
}

async function fetchProducts(keyword: string) {
  loadingProducts.value = true;
  try {
    productResults.value = await timSanPhamTaiQuay(keyword);
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Không thể tìm sản phẩm";
  } finally {
    loadingProducts.value = false;
  }
}

watch(customerKeyword, (value) => {
  if (customerTimer) {
    window.clearTimeout(customerTimer);
  }
  const keyword = value.trim().toLowerCase();
  showCustomerDropdown.value =
    value.trim().length > 0 && keyword !== "khách vãng lai";
  customerTimer = window.setTimeout(() => {
    void fetchCustomers(value);
  }, 250);
});

watch(productKeyword, (value) => {
  if (productTimer) {
    window.clearTimeout(productTimer);
  }
  showProductDropdown.value = value.trim().length > 0;
  productTimer = window.setTimeout(() => {
    void fetchProducts(value);
  }, 250);
});

watch(pageError, (message) => {
  if (!message) {
    return;
  }

  window.alert(message);
  pageError.value = "";
});

watch(successMessage, (message) => {
  if (!message) {
    return;
  }

  window.alert(message);
  successMessage.value = "";
});

function chonKhachHang(customer: KhachHangTaiQuay) {
  selectedCustomer.value = customer;
  customerKeyword.value = customer.hoTen;
  customerResults.value = [];
  showCustomerDropdown.value = false;
  clearFeedback();
}

function boChonKhachHang() {
  selectedCustomer.value = null;
  customerKeyword.value = "Khách vãng lai";
  customerResults.value = [];
  showCustomerDropdown.value = false;
}

function chonKhachVangLai() {
  selectedCustomer.value = null;
  customerKeyword.value = "Khách vãng lai";
  customerResults.value = [];
  showCustomerDropdown.value = false;
  clearFeedback();
}

function moChiTietSanPham(product: SanPhamTaiQuay) {
  if (!daChonKhach.value) {
    pageError.value = "Vui lòng chọn khách hàng hoặc Khách vãng lai trước khi thêm sản phẩm";
    return;
  }

  selectedProductDetail.value = product;
  selectedColor.value = product.mauSac || product.maBienThe;
  selectedSize.value = product.kichCo || "";
  selectedQuantity.value = 1;
}

function dongChiTietSanPham() {
  selectedProductDetail.value = null;
  selectedColor.value = "";
  selectedSize.value = "";
  selectedQuantity.value = 1;
}

function themSanPham(product: SanPhamTaiQuay, quantity = 1) {
  if (!daChonKhach.value) {
    pageError.value = "Vui lòng chọn khách hàng hoặc Khách vãng lai trước khi thêm sản phẩm";
    return;
  }

  const soLuongCoTheThem = soLuongConLai(product.chiTietId, product.soLuongTon);
  const existing = cartItems.value.find((item) => item.chiTietId === product.chiTietId);
  if (existing) {
    if (quantity > soLuongCoTheThem) {
      pageError.value = `Sản phẩm ${existing.tenSanPham} đã đạt giới hạn tồn kho`;
      return;
    }
    existing.soLuong += quantity;
  } else {
    if (quantity > soLuongCoTheThem) {
      pageError.value = `Sáº£n pháº©m ${product.tenSanPham} Ä‘Ã£ vÆ°á»£t giá»›i háº¡n tá»“n kho`;
      return;
    }
    cartItems.value = [
      ...cartItems.value,
      {
        chiTietId: product.chiTietId,
        maSanPham: product.maSanPham,
        tenSanPham: product.tenSanPham,
        soLuong: quantity,
        giaBan: product.giaBan,
        soLuongTon: product.soLuongTon,
      },
    ];
  }

  productKeyword.value = "";
  productResults.value = [];
  selectedProductDetail.value = null;
  selectedColor.value = "";
  selectedSize.value = "";
  selectedQuantity.value = 1;
  showProductDropdown.value = false;
  amountPaid.value = new Intl.NumberFormat("vi-VN").format(tongTien.value);
  clearFeedback();
}

function chonMauSac(value: string) {
  selectedColor.value = value;
  selectedSize.value = sizeOptions.value[0]?.kichCo || "";
  selectedQuantity.value = 1;
}

function chonKichCo(value: string) {
  selectedSize.value = value;
  selectedQuantity.value = 1;
}

function giamSoLuongChiTiet() {
  selectedQuantity.value = Math.max(selectedQuantity.value - 1, 1);
}

function tangSoLuongChiTiet() {
  if (!selectedVariant.value) {
    return;
  }

  const soLuongToiDa = soLuongConLai(selectedVariant.value.chiTietId, selectedVariant.value.soLuongTon);
  selectedQuantity.value = Math.min(selectedQuantity.value + 1, Math.max(soLuongToiDa, 1));
}

function themBienTheDangChon() {
  if (!selectedVariant.value) {
    pageError.value = "Vui lòng chọn màu sắc và kích cỡ phù hợp";
    return;
  }

  themSanPham(selectedVariant.value, selectedQuantity.value);
}

function tangSoLuong(chiTietId: number) {
  let reachedLimit = "";
  cartItems.value = cartItems.value.map((item) => {
    if (item.chiTietId !== chiTietId) {
      return item;
    }
    if (item.soLuong >= item.soLuongTon) {
      reachedLimit = item.tenSanPham;
      return item;
    }
    return { ...item, soLuong: item.soLuong + 1 };
  });
  if (reachedLimit) {
    pageError.value = `Sản phẩm ${reachedLimit} đã vượt giới hạn tồn kho`;
    return;
  }
  if (paymentMethod.value !== 1) {
    amountPaid.value = new Intl.NumberFormat("vi-VN").format(tongTien.value);
  }
}

function giamSoLuong(chiTietId: number) {
  cartItems.value = cartItems.value
    .map((item) =>
      item.chiTietId === chiTietId ? { ...item, soLuong: item.soLuong - 1 } : item,
    )
    .filter((item) => item.soLuong > 0);
  if (!cartItems.value.length) {
    amountPaid.value = "";
  } else if (paymentMethod.value !== 1) {
    amountPaid.value = new Intl.NumberFormat("vi-VN").format(tongTien.value);
  }
}

function mapInvoiceToDraft(invoice: HoaDonChoChiTiet) {
  customerKeyword.value = invoice.tenKhachHang || invoice.soDienThoai || "";
  selectedCustomer.value = null;
  cartItems.value = invoice.items.map((item) => ({
    chiTietId: item.chiTietId,
    maSanPham: item.maSanPham,
    tenSanPham: item.tenSanPham,
    soLuong: item.soLuong,
    giaBan: item.giaBan,
    soLuongTon: laySoLuongTonHienTai(item.chiTietId, item.soLuong),
  }));
  amountPaid.value = new Intl.NumberFormat("vi-VN").format(invoice.tongTien || 0);
}

async function chonHoaDonCho(invoice: HoaDonChoTomTat) {
  invoiceLoading.value = true;
  pageError.value = "";
  try {
    await fetchProducts("");
    const detail = await layChiTietHoaDonCho(invoice.id);
    activePendingInvoice.value = invoice;
    mapInvoiceToDraft(detail);
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Không thể tải hóa đơn chờ";
  } finally {
    invoiceLoading.value = false;
  }
}

async function handleCreatePendingInvoice() {
  if (!canCreatePendingInvoice.value) {
    return;
  }

  savingPendingInvoice.value = true;
  pageError.value = "";
  successMessage.value = "";

  try {
    const createdInvoice = await taoHoaDonCho({
      khachHangId: selectedCustomer.value?.id ?? null,
      tenKhachHang:
        selectedCustomer.value?.hoTen ||
        (customerKeyword === "Khách vãng lai" ? "Khách vãng lai" : "Khách vãng lai"),
      soDienThoai: selectedCustomer.value?.sdt ?? "",
      items: cartItems.value.map((item) => ({
        chiTietId: item.chiTietId,
        soLuong: item.soLuong,
      })),
    });

    successMessage.value = `Đã tạo hóa đơn chờ ${createdInvoice.ma}`;
    await fetchPendingInvoices();
    const matchedInvoice = pendingInvoices.value.find((invoice) => invoice.id === createdInvoice.id) ?? null;
    activePendingInvoice.value = matchedInvoice;
    mapInvoiceToDraft(createdInvoice);
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Không thể tạo hóa đơn chờ";
  } finally {
    savingPendingInvoice.value = false;
  }
}

function formatCurrencyInput() {
  const digits = amountPaid.value.replace(/[^\d]/g, "");
  amountPaid.value = digits ? new Intl.NumberFormat("vi-VN").format(Number(digits)) : "";
}

async function handlePayNow() {
  if (!canPay.value) {
    return;
  }

  payingInvoice.value = true;
  pageError.value = "";
  successMessage.value = "";

  try {
    const response = await thanhToanTaiQuay({
      hoaDonId: activePendingInvoice.value?.id ?? null,
      khachHangId: selectedCustomer.value?.id ?? null,
      tenKhachHang:
        selectedCustomer.value?.hoTen ||
        activePendingInvoice?.tenKhachHang ||
        (customerKeyword === "Khách vãng lai" ? "Khách vãng lai" : "Khách vãng lai"),
      soDienThoai:
        selectedCustomer.value?.sdt ||
        activePendingInvoice?.soDienThoai ||
        (customerKeyword === "Khách vãng lai" ? "" : ""),
      hinhThucThanhToan: paymentMethod.value,
      tienKhachDua: paymentMethod.value === 1 ? tienKhachThanhToan.value : khachCanTra.value,
      ghiChu: paymentNote.value,
      items: cartItems.value.map((item) => ({
        chiTietId: item.chiTietId,
        soLuong: item.soLuong,
      })),
    });

    successMessage.value = `Đã thanh toán ${response.maHoaDon}`;
    await fetchPendingInvoices();
    resetDraft();
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Không thể thanh toán trực tiếp";
  } finally {
    payingInvoice.value = false;
  }
}

async function handleCancelPendingInvoice() {
  if (!activePendingInvoice.value || cancelingPendingInvoice.value) {
    return;
  }

  cancelingPendingInvoice.value = true;
  pageError.value = "";
  successMessage.value = "";

  try {
    await huyHoaDonCho(activePendingInvoice.value.id);
    successMessage.value = `Đã hủy hóa đơn chờ ${activePendingInvoice.value.ma}`;
    await fetchPendingInvoices();
    resetDraft();
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Không thể hủy hóa đơn chờ";
  } finally {
    cancelingPendingInvoice.value = false;
  }
}

async function moDanhSachKhachHang() {
  const keyword = customerKeyword.value.trim();
  if (keyword && keyword.toLowerCase() !== "khách vãng lai") {
    showCustomerDropdown.value = true;
    await fetchCustomers(customerKeyword.value);
    return;
  }

  showCustomerDropdown.value = false;
}

async function moDanhSachSanPham() {
  showProductDropdown.value = true;
  await fetchProducts(productKeyword.value);
}

function dongDanhSachKhachHang() {
  window.setTimeout(() => {
    showCustomerDropdown.value = false;
  }, 150);
}

function dongDanhSachSanPham() {
  window.setTimeout(() => {
    showProductDropdown.value = false;
  }, 150);
}

onMounted(async () => {
  await fetchProducts("");
  await fetchPendingInvoices();
});
</script>

<template>
  <div class="p-6">
    <div class="mb-6 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
      <div>
        <h1 class="mt-2 text-3xl font-bold text-slate-900">Bán hàng tại quầy</h1>
      </div>
      <button
        type="button"
        class="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-red-300 hover:text-red-500"
        @click="resetDraft"
      >
        Tạo phiếu mới
      </button>
    </div>

    <section class="mb-6 rounded-[28px] border border-white/70 bg-white/90 p-5 shadow-[0_24px_60px_rgba(15,23,42,0.08)] backdrop-blur">
      <div class="mb-4 flex items-center justify-between">
        <div>
          <h2 class="text-lg font-bold text-slate-900">Hóa đơn chờ</h2>
          <p class="text-sm text-slate-500">Chọn nhanh để xem lại hóa đơn đang chờ xử lý.</p>
        </div>
        <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
          {{ loadingPendingInvoices ? "Đang tải..." : `${pendingInvoices.length} hóa đơn` }}
        </span>
      </div>

      <div class="flex flex-wrap gap-3">
        <button
          v-for="invoice in pendingInvoices"
          :key="invoice.id"
          type="button"
          class="min-w-[220px] rounded-2xl border px-4 py-3 text-left transition"
          :class="
            activePendingInvoice?.id === invoice.id
              ? 'border-red-500 bg-red-50 shadow-[0_16px_30px_rgba(239,68,68,0.15)]'
              : 'border-slate-200 bg-slate-50 hover:border-red-200 hover:bg-white'
          "
          @click="chonHoaDonCho(invoice)"
        >
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="text-sm font-bold text-slate-900">{{ invoice.ma }}</p>
              <p class="mt-1 text-sm text-slate-600">{{ invoice.tenKhachHang }}</p>
            </div>
            <span class="rounded-full bg-white px-2 py-1 text-xs font-semibold text-slate-500">
              {{ invoice.tongSanPham }} SP
            </span>
          </div>
          <p class="mt-3 text-sm font-semibold text-red-500">{{ dinhDangTien(invoice.tongTien) }}</p>
        </button>

        <div
          v-if="!loadingPendingInvoices && !pendingInvoices.length"
          class="rounded-2xl border border-dashed border-slate-200 px-4 py-6 text-sm text-slate-500"
        >
          Chưa có hóa đơn chờ nào.
        </div>
      </div>
    </section>

    <div class="grid gap-6 xl:grid-cols-[1.5fr_0.8fr]">
      <section class="space-y-6 rounded-[32px] border border-white/70 bg-white/95 p-6 shadow-[0_24px_60px_rgba(15,23,42,0.08)]">
        <div class="grid gap-4 lg:grid-cols-2">
          <div class="relative">
            <label class="mb-2 block text-sm font-semibold text-slate-700">Tìm khách hàng theo tên hoặc số điện thoại</label>
            <div class="flex gap-3">
              <input
                v-model="customerKeyword"
                type="text"
                placeholder="Nhập tên hoặc số điện thoại khách hàng"
                class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300 focus:bg-white"
                @focus="moDanhSachKhachHang"
                @blur="dongDanhSachKhachHang"
              />
              <button
                type="button"
                class="shrink-0 rounded-2xl border border-dashed border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-red-300 hover:text-red-500"
                @click="chonKhachVangLai"
              >
                Khách vãng lai
              </button>
            </div>

            <div v-if="loadingCustomers" class="absolute right-4 top-[46px] text-xs font-semibold text-slate-400">
              Đang tìm...
            </div>

            <div
              v-if="showCustomerDropdown"
              class="absolute z-20 mt-2 w-full rounded-2xl border border-slate-200 bg-white p-2 shadow-[0_24px_50px_rgba(15,23,42,0.12)]"
            >
              <button v-if="false"
                type="button"
                class="mb-1 w-full rounded-2xl border border-dashed border-slate-200 px-3 py-3 text-left transition hover:border-red-200 hover:bg-red-50"
                @click="chonKhachVangLai"
              >
                <p class="text-sm font-semibold text-slate-900">Khách vãng lai</p>
                <p class="mt-1 text-xs text-slate-500">Không lưu số điện thoại hoặc thông tin cá nhân</p>
              </button>
              <div v-if="!loadingCustomers && !customerResults.length" class="rounded-2xl px-3 py-3 text-sm text-slate-500">
                Không tìm thấy khách hàng phù hợp.
              </div>
              <button
                v-for="customer in customerResults"
                :key="customer.id"
                type="button"
                class="w-full rounded-2xl px-3 py-3 text-left transition hover:bg-red-50"
                @click="chonKhachHang(customer)"
              >
                <p class="text-sm font-semibold text-slate-900">{{ customer.hoTen }}</p>
                <p class="mt-1 text-xs text-slate-500">{{ customer.sdt }} <span v-if="customer.email">- {{ customer.email }}</span></p>
              </button>
            </div>
          </div>

          <div class="rounded-3xl border border-slate-100 bg-slate-50 p-4">
            <div class="flex items-start justify-between gap-3">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">Khách được chọn</p>
                <p class="mt-2 text-lg font-bold text-slate-900">
                  {{
                    selectedCustomer?.hoTen ||
                    activePendingInvoice?.tenKhachHang ||
                    (customerKeyword === "Khách vãng lai" ? "Khách vãng lai" : "Khách vãng lai")
                  }}
                </p>
                <p class="mt-1 text-sm text-slate-500">
                  {{
                    selectedCustomer?.sdt ||
                    activePendingInvoice?.soDienThoai ||
                    (customerKeyword === "Khách vãng lai" ? "Ẩn thông tin khách hàng" : "Ẩn thông tin khách hàng")
                  }}
                </p>
              </div>
              <button
                v-if="selectedCustomer || customerKeyword === 'Khách vãng lai'"
                type="button"
                class="text-sm font-semibold text-slate-400 transition hover:text-red-500"
                @click="boChonKhachHang"
              >
                Bỏ chọn
              </button>
            </div>
          </div>
        </div>

        <div class="relative">
          <label class="mb-2 block text-sm font-semibold text-slate-700">Tìm sản phẩm</label>
          <input
            v-model="productKeyword"
            type="text"
            placeholder="Nhập mã, tên sản phẩm, SKU..."
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300 focus:bg-white"
            @focus="moDanhSachSanPham"
            @blur="dongDanhSachSanPham"
          />

          <div v-if="loadingProducts" class="absolute right-4 top-[46px] text-xs font-semibold text-slate-400">
            Đang tìm...
          </div>

          <div
            v-if="showProductDropdown"
            class="absolute z-20 mt-2 w-full rounded-3xl border border-slate-200 bg-white p-2 shadow-[0_24px_50px_rgba(15,23,42,0.12)]"
          >
            <div v-if="!loadingProducts && !productResults.length" class="rounded-2xl px-3 py-3 text-sm text-slate-500">
              Không tìm thấy sản phẩm phù hợp.
            </div>
            <button
              v-for="product in productResults"
              :key="product.chiTietId"
              type="button"
              class="flex w-full items-start justify-between gap-4 rounded-2xl px-3 py-3 text-left transition hover:bg-red-50"
              @click="moChiTietSanPham(product)"
            >
              <div>
                <p class="text-sm font-bold text-slate-900">{{ product.tenSanPham }}</p>
                <p class="mt-1 text-xs text-slate-500">
                  Mã: {{ product.maSanPham }} | SKU: {{ product.sku }} | Biến thể: {{ product.maBienThe }}
                </p>
              </div>
              <div class="text-right">
                <p class="text-sm font-semibold text-red-500">{{ dinhDangTien(product.giaBan) }}</p>
                <p class="mt-1 text-xs text-slate-500">Tồn: {{ product.soLuongTon }}</p>
              </div>
            </button>
          </div>
        </div>

        <div class="rounded-[28px] border border-slate-100 bg-[linear-gradient(180deg,#fff8f5_0%,#ffffff_100%)] p-4 shadow-[0_18px_40px_rgba(15,23,42,0.06)]">
          <div class="flex flex-col gap-3 border-b border-slate-100 pb-4 md:flex-row md:items-center md:justify-between">
            <div>
              <p class="text-sm font-semibold text-slate-800">{{ productSearchLabel }}</p>
            </div>
            <div class="rounded-2xl bg-white px-4 py-3 text-xs font-semibold text-slate-500 shadow-sm">
              {{ loadingProducts ? "Đang tải sản phẩm..." : `${productResults.length} sản phẩm` }}
            </div>
          </div>

          <div class="mt-4 max-h-[360px] space-y-3 overflow-y-auto pr-1">
            <div
              v-if="!loadingProducts && !productResults.length"
              class="rounded-2xl border border-dashed border-slate-200 bg-white px-4 py-8 text-center text-sm text-slate-500"
            >
              Không tìm thấy sản phẩm phù hợp.
            </div>

            <button
              v-for="product in productResults"
              :key="`panel-${product.chiTietId}`"
              type="button"
              class="flex w-full items-center justify-between gap-4 rounded-[24px] border border-white bg-white px-4 py-4 text-left shadow-[0_12px_30px_rgba(15,23,42,0.06)] transition hover:-translate-y-0.5 hover:border-red-200 hover:bg-red-50"
              @click="moChiTietSanPham(product)"
            >
              <div class="flex min-w-0 items-center gap-4">
                <div class="flex h-16 w-16 shrink-0 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#fff1eb_0%,#ffe4dc_100%)] text-lg font-bold text-red-400">
                  {{ product.tenSanPham.slice(0, 1) }}
                </div>
                <div class="min-w-0">
                  <p class="truncate text-base font-bold text-slate-900">{{ product.tenSanPham }}</p>
                  <p class="mt-1 truncate text-xs text-slate-500">
                    Mã: {{ product.maSanPham }} | SKU: {{ product.sku }} | Biến thể: {{ product.maBienThe }}
                  </p>
                  <p class="mt-2 text-sm font-semibold text-slate-700">Tồn kho: x{{ product.soLuongTon }}</p>
                </div>
              </div>

              <div class="shrink-0 text-right">
                <p class="text-sm font-semibold text-red-500">{{ dinhDangTien(product.giaBan) }}</p>
                <span class="mt-2 inline-flex rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
                  Xem chi tiết
                </span>
              </div>
            </button>
          </div>
        </div>

        <div class="overflow-hidden rounded-[28px] border border-slate-100">
          <table class="min-w-full border-collapse">
            <thead class="bg-slate-100 text-left text-sm text-slate-600">
              <tr>
                <th class="px-5 py-4 font-semibold">STT</th>
                <th class="px-5 py-4 font-semibold">Mã sản phẩm</th>
                <th class="px-5 py-4 font-semibold">Tên sản phẩm</th>
                <th class="px-5 py-4 font-semibold">Đơn giá</th>
                <th class="px-5 py-4 font-semibold">Số lượng</th>
              </tr>
            </thead>
            <tbody class="bg-white text-sm text-slate-700">
              <tr v-for="(item, index) in cartItems" :key="item.chiTietId" class="border-t border-slate-100">
                <td class="px-5 py-4 font-semibold text-slate-900">{{ index + 1 }}</td>
                <td class="px-5 py-4 font-semibold text-slate-600">{{ item.maSanPham }}</td>
                <td class="px-5 py-4">
                  <p class="font-semibold text-slate-900">{{ item.tenSanPham }}</p>
                </td>
                <td class="px-5 py-4 font-semibold text-slate-700">{{ dinhDangTien(item.giaBan) }}</td>
                <td class="px-5 py-4">
                  <div class="inline-flex items-center rounded-full border border-slate-200 bg-slate-50">
                    <button
                      type="button"
                      class="px-3 py-1 text-base font-bold text-slate-500 transition hover:text-red-500"
                      @click="giamSoLuong(item.chiTietId)"
                    >
                      -
                    </button>
                    <span class="min-w-10 px-2 text-center font-semibold text-slate-900">{{ item.soLuong }}</span>
                    <button
                      type="button"
                      class="px-3 py-1 text-base font-bold transition"
                      :class="
                        soLuongConLai(item.chiTietId, item.soLuongTon) <= 0
                          ? 'cursor-not-allowed text-slate-300'
                          : 'text-slate-500 hover:text-red-500'
                      "
                      @click="tangSoLuong(item.chiTietId)"
                    >
                      +
                    </button>
                  </div>
                  <p class="mt-2 text-xs text-slate-400">Tồn kho: {{ item.soLuongTon }}</p>
                </td>
              </tr>
              <tr v-if="!cartItems.length">
                <td colspan="5" class="px-5 py-14 text-center text-sm text-slate-400">
                  Chọn sản phẩm từ ô tìm kiếm để đưa vào hóa đơn.
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div
          v-if="selectedProductDetail"
          class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/45 px-4 py-6"
          @click.self="dongChiTietSanPham"
        >
          <div class="max-h-[85vh] w-full max-w-3xl overflow-hidden rounded-[32px] bg-white shadow-[0_30px_80px_rgba(15,23,42,0.25)]">
            <div class="flex items-start justify-between border-b border-slate-100 px-6 py-5">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.2em] text-red-400">Sản phẩm chi tiết</p>
                <h3 class="mt-2 text-2xl font-bold text-slate-900">{{ selectedProductDetail.tenSanPham }}</h3>
                <p class="mt-1 text-sm text-slate-500">
                  Mã: {{ selectedProductDetail.maSanPham }} | Biến thể: {{ selectedProductDetail.maBienThe }}
                </p>
              </div>
              <button
                type="button"
                class="rounded-full bg-slate-100 px-3 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-200"
                @click="dongChiTietSanPham"
              >
                Đóng
              </button>
            </div>

            <div class="max-h-[calc(85vh-110px)] overflow-y-auto px-6 py-5">
              <div class="mb-4 rounded-2xl border border-amber-100 bg-amber-50 px-4 py-3 text-sm text-amber-700">
                Chọn màu sắc, kích cỡ và số lượng trước khi thêm vào hóa đơn.
              </div>

              <div class="mb-5 flex items-center justify-between rounded-[24px] border border-slate-200 bg-slate-50 px-5 py-4">
                <div>
                  <p class="text-sm text-slate-500">SKU</p>
                  <p class="mt-1 text-base font-semibold text-slate-900">{{ (selectedVariant || selectedProductDetail).sku }}</p>
                </div>
                <div class="text-right">
                  <p class="text-sm text-slate-500">Tồn kho</p>
                  <p class="mt-1 text-base font-semibold text-slate-900">{{ (selectedVariant || selectedProductDetail).soLuongTon }}</p>
                </div>
                <div class="text-right">
                  <p class="text-sm text-slate-500">Giá bán</p>
                  <p class="mt-1 text-base font-bold text-red-500">{{ dinhDangTien((selectedVariant || selectedProductDetail).giaBan) }}</p>
                </div>
              </div>

              <div class="mt-6 grid gap-6">
                <div class="grid gap-3 md:grid-cols-[84px_1fr] md:items-start">
                  <p class="text-base font-medium text-slate-700">Màu sắc</p>
                  <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                    <button
                      v-for="option in colorOptions"
                      :key="`color-${option.mauSac || option.maBienThe}`"
                      type="button"
                      class="flex items-center gap-3 rounded-xl border px-3 py-2 text-left transition"
                      :class="
                        selectedColor === (option.mauSac || option.maBienThe)
                          ? 'border-red-400 bg-red-50 text-red-600'
                          : 'border-slate-200 bg-white text-slate-700 hover:border-red-200 hover:bg-red-50'
                      "
                      @click="chonMauSac(option.mauSac || option.maBienThe)"
                    >
                      <div class="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-100 text-xs font-bold text-slate-500">
                        {{ (option.mauSac || "?").slice(0, 1) }}
                      </div>
                      <div class="min-w-0">
                        <p class="truncate text-sm font-semibold">{{ option.mauSac || option.maBienThe }}</p>
                        <p class="truncate text-xs text-slate-500">{{ option.maBienThe }}</p>
                      </div>
                    </button>
                  </div>
                </div>

                <div class="grid gap-3 md:grid-cols-[84px_1fr] md:items-start">
                  <p class="text-base font-medium text-slate-700">Size</p>
                  <div class="flex flex-wrap gap-3">
                    <button
                      v-for="option in sizeOptions"
                      :key="`size-${option.chiTietId}`"
                      type="button"
                      class="min-w-20 rounded-xl border px-5 py-3 text-center text-sm font-semibold transition"
                      :class="
                        selectedSize === (option.kichCo || '')
                          ? 'border-red-400 bg-red-50 text-red-600'
                          : 'border-slate-200 bg-white text-slate-700 hover:border-red-200 hover:bg-red-50'
                      "
                      @click="chonKichCo(option.kichCo || '')"
                    >
                      {{ option.kichCo || '--' }}
                    </button>
                  </div>
                </div>

                <div class="text-sm font-medium text-blue-600">Bảng Quy Đổi Kích Cỡ</div>

                <div class="grid gap-3 md:grid-cols-[84px_1fr_120px] md:items-center">
                  <p class="text-base font-medium text-slate-700">Số lượng</p>
                  <div class="inline-flex w-fit items-center rounded-xl border border-slate-200 bg-white">
                    <button
                      type="button"
                      class="px-4 py-3 text-lg font-bold text-slate-400 transition hover:text-red-500"
                      @click="giamSoLuongChiTiet"
                    >
                      -
                    </button>
                    <span class="min-w-14 border-x border-slate-200 px-4 py-3 text-center text-base font-semibold text-slate-900">
                      {{ selectedQuantity }}
                    </span>
                    <button
                      type="button"
                      class="px-4 py-3 text-lg font-bold text-slate-500 transition hover:text-red-500"
                      @click="tangSoLuongChiTiet"
                    >
                      +
                    </button>
                  </div>
                  <p class="text-sm font-semibold uppercase text-emerald-600">
                    {{ (selectedVariant || selectedProductDetail).soLuongTon > 0 ? 'Còn hàng' : 'Hết hàng' }}
                  </p>
                </div>
              </div>

              <div class="mt-6 flex justify-end">
                <button
                  type="button"
                  class="rounded-2xl bg-red-500 px-5 py-3 text-sm font-bold text-white shadow-[0_20px_40px_rgba(239,68,68,0.25)] transition hover:bg-red-600"
                  @click="themBienTheDangChon"
                >
                  Thêm vào hóa đơn
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <aside class="rounded-[32px] border border-white/70 bg-white/95 p-6 shadow-[0_24px_60px_rgba(15,23,42,0.08)]">
        <div class="rounded-[28px] bg-[linear-gradient(180deg,#fff7f4_0%,#ffffff_100%)] p-5">
          <p class="text-xs font-semibold uppercase tracking-[0.24em] text-red-400">Tổng quan</p>
          <h2 class="mt-3 text-2xl font-bold text-slate-900">
            {{ activePendingInvoice?.ma || "Hóa đơn mới" }}
          </h2>
          <p class="mt-2 text-sm text-slate-500">
            {{ invoiceLoading ? "Đang tải chi tiết hóa đơn..." : "Hóa đơn bán hàng tại quầy đang thao tác." }}
          </p>

          <div class="mt-6 space-y-4">
            <div class="flex items-center justify-between border-b border-slate-200 pb-3">
              <span class="text-sm text-slate-500">Tổng sản phẩm</span>
              <span class="text-lg font-bold text-slate-900">{{ tongSoLuong }}</span>
            </div>
            <div class="flex items-center justify-between border-b border-slate-200 pb-3">
              <span class="text-sm text-slate-500">Tổng tiền hàng</span>
              <span class="text-lg font-bold text-slate-900">{{ dinhDangTien(tongTien) }}</span>
            </div>
            <div class="flex items-center justify-between border-b border-slate-200 pb-3">
              <span class="text-sm text-slate-500">Khách cần trả</span>
              <span class="text-lg font-bold text-slate-900">{{ dinhDangTien(khachCanTra) }}</span>
            </div>
            <div class="flex items-center justify-between border-b border-slate-200 pb-3">
              <span class="text-sm text-slate-500">Khách hàng</span>
              <span class="text-right text-sm font-semibold text-slate-700">
                {{
                  selectedCustomer?.hoTen ||
                  activePendingInvoice?.tenKhachHang ||
                  (customerKeyword === "Khách vãng lai" ? "Khách vãng lai" : "Khách vãng lai")
                }}
              </span>
            </div>
            <div class="flex items-center justify-between border-b border-slate-200 pb-3">
              <span class="text-sm text-slate-500">Số điện thoại</span>
              <span class="text-right text-sm font-semibold text-slate-700">
                {{
                  selectedCustomer?.sdt ||
                  activePendingInvoice?.soDienThoai ||
                  (customerKeyword === "Khách vãng lai" ? "Ẩn thông tin" : "Ẩn thông tin")
                }}
              </span>
            </div>
            <div>
              <p class="mb-2 text-sm text-slate-500">Hình thức thanh toán</p>
              <div class="grid grid-cols-2 gap-x-6 gap-y-3">
                <label
                  class="flex cursor-pointer items-center gap-3 text-sm text-slate-700"
                >
                  <input v-model="paymentMethod" type="radio" class="h-4 w-4 accent-red-500" :value="1" />
                  <span>Tiền mặt</span>
                </label>
                <label
                  class="flex cursor-pointer items-center gap-3 text-sm text-slate-700"
                >
                  <input v-model="paymentMethod" type="radio" class="h-4 w-4 accent-red-500" :value="2" />
                  <span>Chuyển khoản</span>
                </label>
                <label
                  class="flex cursor-pointer items-center gap-3 text-sm text-slate-700"
                >
                  <input v-model="paymentMethod" type="radio" class="h-4 w-4 accent-red-500" :value="4" />
                  <span>Thẻ</span>
                </label>
                <label
                  class="flex cursor-pointer items-center gap-3 text-sm text-slate-700"
                >
                  <input v-model="paymentMethod" type="radio" class="h-4 w-4 accent-red-500" :value="3" />
                  <span>Ví</span>
                </label>
              </div>
            </div>
            <div>
              <label class="mb-2 block text-sm text-slate-500">Khách thanh toán</label>
              <input
                v-model="amountPaid"
                type="text"
                placeholder="Nhập số tiền khách đưa"
                class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                @input="formatCurrencyInput"
              />
            </div>
            <div class="flex items-center justify-between border-b border-slate-200 pb-3">
              <span class="text-sm text-slate-500">Tiền thừa trả khách</span>
              <span class="text-lg font-bold text-slate-900">{{ dinhDangTien(tienThua) }}</span>
            </div>
            <div>
              <label class="mb-2 block text-sm text-slate-500">Ghi chú thanh toán</label>
              <textarea
                v-model="paymentNote"
                rows="3"
                placeholder="Ghi chú thêm nếu cần"
                class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
              />
            </div>
          </div>

          <div class="mt-8 grid gap-3 sm:grid-cols-2">
            <button
              type="button"
              class="rounded-2xl bg-slate-200 px-4 py-4 text-sm font-bold text-slate-700 transition hover:bg-slate-300 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
              :disabled="!canCreatePendingInvoice"
              @click="handleCreatePendingInvoice"
            >
              {{ savingPendingInvoice ? "Đang tạo..." : "Tạo hóa đơn chờ" }}
            </button>
            <button
              type="button"
              class="rounded-2xl bg-red-500 px-4 py-4 text-sm font-bold text-white shadow-[0_20px_40px_rgba(239,68,68,0.35)] transition hover:bg-red-600 disabled:cursor-not-allowed disabled:bg-slate-300 disabled:shadow-none"
              :disabled="!canPay"
              @click="handlePayNow"
            >
              {{ payingInvoice ? "Đang thanh toán..." : "Thanh toán" }}
            </button>
          </div>
          <button
            v-if="activePendingInvoice"
            type="button"
            class="mt-3 w-full rounded-2xl border border-red-200 bg-white px-4 py-3 text-sm font-semibold text-red-600 transition hover:bg-red-50 disabled:cursor-not-allowed disabled:border-slate-200 disabled:text-slate-400"
            :disabled="cancelingPendingInvoice"
            @click="handleCancelPendingInvoice"
          >
            {{ cancelingPendingInvoice ? "Đang hủy hóa đơn chờ..." : "Hủy hóa đơn chờ" }}
          </button>
        </div>
      </aside>
    </div>
  </div>
</template>
