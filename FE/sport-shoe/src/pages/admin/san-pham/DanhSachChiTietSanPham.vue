<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CircleCheckBig, Eye, FileSpreadsheet, Filter, Images, Layers3, Plus, RotateCcw, Search, Tag, TriangleAlert, X } from 'lucide-vue-next'
import * as api from '../../../services/san-pham-api'
import AdminQrCodeModal from '../../../components/common/AdminQrCodeModal.vue'
import ModalQuetQR from '../../../components/admin/ban-hang/ModalQuetQR.vue'
import AdminQuickStatusAction from '../../../components/common/AdminQuickStatusAction.vue'
import AdminTableFooter from '../../../components/common/AdminTableFooter.vue'
import BoLocBienThe from '../../../components/admin/san-pham/BoLocBienThe.vue'
import BangBienThe from '../../../components/admin/san-pham/BangBienThe.vue'
import QuanLySanPhamBienTheFormModal from '../../../components/admin/san-pham/QuanLySanPhamBienTheFormModal.vue'
import QuanLySanPhamHinhAnhModal from '../../../components/admin/san-pham/QuanLySanPhamHinhAnhModal.vue'
import { exportRowsToExcel } from '../../../utils/export-excel'
import { getDisplayErrorMessage, getFieldErrors } from '../../../utils/error-message'
import { showSuccess, showError } from '../../../utils/alert'
import { createQrCodeSvg } from '../../../utils/qr-code'
import { useRealtime } from '../../../composables/useRealtime'

const { subscribeTopic } = useRealtime()

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const items = ref([])
const danhMuc = ref(null)
const currentPage = ref(0)
const pageSize = ref(5)
const totalItems = ref(0)
const totalPages = ref(0)
const sanPhamDuocChon = ref(null)
const danhSachIdDangCapNhat = reactive(new Set())
const hienThiModalQr = ref(false)
const chiTietQrDuocChon = ref(null)

const hienThiModalQuetMa = ref(false)
const bangBienTheRef = ref(null)
const coBienTheDuocChon = ref(false)

const boLoc = reactive({
  keyword: '',
  mauSacId: null,
  kichCoId: null,
  trangThai: null
})

const hienThiModalHinhAnh = ref(false)
const bienTheDuocChon = ref(null)
const hienThiModalSuaBienThe = ref(false)
const bienTheDangSua = ref(null)
const dangLuuBienThe = ref(false)
const bienTheForm = reactive({
  soLuong: 0,
  giaGoc: 0,
  giaBan: 0,
  kichHoat: 1
})
const bienTheErrors = reactive({})
const bulkBienTheForm = reactive({
  mauSacIds: [],
  kichCoIds: [],
  soLuong: 0,
  giaGoc: 0,
  giaBan: 0
})
const bulkBienTheErrors = reactive({})
const generatedBulkBienThes = ref([])

const thongBao = reactive({
  show: false,
  message: '',
  type: 'success'
})

const pageSizeOptions = [5, 10, 20, 50]
let toastTimer = null
let latestLoadRequestId = 0
let keywordSearchTimer = null
let suppressGiayIdWatch = false

const selectedGiayId = computed(() => {
  const raw = Array.isArray(route.query.giayId) ? route.query.giayId[0] : route.query.giayId
  const parsed = Number(raw)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
})

const focusedChiTietId = computed(() => {
  const raw = Array.isArray(route.query.chiTietId) ? route.query.chiTietId[0] : route.query.chiTietId
  const parsed = Number(raw)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
})

const toastTitle = computed(() => {
  if (thongBao.type === 'error') return 'Không thể hoàn tất thao tác'
  if (thongBao.message.startsWith('Đang xem CTSP')) return 'Xem CTSP thành công'
  return 'Thao tác thành công'
})

const editingSelectedGiay = computed(() => {
  if (sanPhamDuocChon.value) return sanPhamDuocChon.value
  if (!bienTheDangSua.value) return null

  return {
    id: bienTheDangSua.value.giayId,
    ten: bienTheDangSua.value.tenSanPham,
    ma: bienTheDangSua.value.maSanPham
  }
})

function showToast(message, type = 'success') {
  if (type === 'success') {
    showSuccess(message)
    return
  }

  if (type === 'error') {
    showError(message)
    return
  }

  if (toastTimer) clearTimeout(toastTimer)
  thongBao.message = message
  thongBao.type = type
  thongBao.show = true
  toastTimer = setTimeout(() => {
    thongBao.show = false
    toastTimer = null
  }, 3000)
}

function closeToast() {
  if (toastTimer) {
    clearTimeout(toastTimer)
    toastTimer = null
  }
  thongBao.show = false
}

function dangCapNhatTrangThai(id) {
  return danhSachIdDangCapNhat.has(id)
}

function formatCurrency(value) {
  return Number(value || 0).toLocaleString('vi-VN')
}

function giaSauDotGiam(item) {
  const giaBan = Number(item?.giaBan || 0)
  const giaTriGiam = Number(item?.giaTriGiam || 0)
  const loaiGiam = Number(item?.loaiGiam || 0)

  if (!item?.dotGiamGiaId || giaBan <= 0 || giaTriGiam <= 0) return giaBan
  if (loaiGiam === 1) return Math.max(0, giaBan * (1 - Math.min(giaTriGiam, 100) / 100))
  if (loaiGiam === 2) return Math.max(0, giaBan - giaTriGiam)
  return giaBan
}

function giaHienThi(item) {
  return giaSauDotGiam(item)
}

function giaGachNgang(item) {
  if (item?.dotGiamGiaId && giaSauDotGiam(item) < Number(item?.giaBan || 0)) {
    return Number(item?.giaBan || 0)
  }
  if (Number(item?.giaBan || 0) < Number(item?.giaGoc || 0)) {
    return Number(item?.giaGoc || 0)
  }
  return null
}

function clearBienTheErrors() {
  Object.keys(bienTheErrors).forEach((key) => delete bienTheErrors[key])
}

function parsePositiveMoney(value) {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
}

function parseStock(value) {
  const parsed = Number(value)
  return Number.isInteger(parsed) && parsed >= 0 ? parsed : null
}

function isDiscounted(item) {
  return giaGachNgang(item) != null
}

function formatPercentValue(value) {
  const numericValue = Number(value)
  if (!Number.isFinite(numericValue) || numericValue <= 0) return '—'

  const normalizedValue = Math.min(numericValue, 100)
  return normalizedValue % 1 === 0 ? `${normalizedValue.toFixed(0)}%` : `${normalizedValue.toFixed(1)}%`
}

function formatDiscountPercent(item) {
  const loaiGiam = Number(item?.loaiGiam || 0)
  const giaTriGiam = Number(item?.giaTriGiam || 0)
  const giaGoc = Number(item?.giaGoc || 0)
  const giaBan = Number(item?.giaBan || 0)

  if (giaTriGiam > 0) {
    if (loaiGiam === 1) {
      return formatPercentValue(giaTriGiam)
    }

    if (loaiGiam === 2 && giaBan > 0) {
      return formatPercentValue((giaTriGiam / giaBan) * 100)
    }
  }
  if (giaGoc <= 0 || giaBan >= giaGoc) return '—'

  return formatPercentValue(((giaGoc - giaBan) / giaGoc) * 100)
}

function discountTitle(item) {
  return item?.maDotGiamGia || item?.tenDotGiamGia || 'Xem đợt giảm giá'
}

function isFocusedVariant(item) {
  return focusedChiTietId.value != null && Number(item?.id) === focusedChiTietId.value
}

function openDiscountDetail(item) {
  if (!item?.dotGiamGiaId) return
  router.push({
    name: 'admin-dot-giam-gia-chi-tiet',
    params: { id: item.dotGiamGiaId }
  })
}

function handleScannerResult(result) {
  if (result) {
    boLoc.keyword = result;
    hienThiModalQuetMa.value = false;
    showToast(`Đã quét mã: ${result}`);
    loadData(0);
  }
}

function bienTheTrangThaiLabel(item) {
  if (Number(item.kichHoat) === 0) return 'Ngừng bán'
  if (Number(item.soLuong || 0) <= 0) return 'Hết hàng'
  return 'Đang bán'
}

function bienTheTrangThaiClass(item) {
  if (Number(item.kichHoat) === 0) return 'bg-slate-100 text-slate-600'
  if (Number(item.soLuong || 0) <= 0) return 'bg-amber-50 text-amber-600'
  return 'bg-emerald-50 text-emerald-600'
}

function nextBienTheStatus(item) {
  return Number(item.kichHoat) === 1 ? 0 : 1
}

function quickToggleLabel(item) {
  if (Number(item.kichHoat) === 1) return 'Chuyển sang ngừng bán'
  return 'Chuyển sang đang bán'
}

function canToggleStatus(item) {
  return true
}

function quickToggleIntent(item) {
  return Number(item.kichHoat) === 1 ? 'deactivate' : 'activate'
}

function quickToggleDisabledTitle(item) {
  return quickToggleLabel(item)
}

function quickToggleConfirmMessage(item) {
  const action = Number(item.kichHoat) === 1 ? 'ngừng bán' : 'đang bán'
  const target = item.maChiTietSanPham || item.maBienThe || item.sku || `#${item.id}`
  return `Bạn có muốn chuyển CTSP "${target}" sang ${action} không?`
}

async function loadDanhMuc() {
  try {
    danhMuc.value = await api.layDanhMuc()
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không tải được danh mục sản phẩm'), 'error')
  }
}

async function syncSelectedProduct() {
  if (!selectedGiayId.value) {
    sanPhamDuocChon.value = null
    return
  }

  try {
    sanPhamDuocChon.value = await api.chiTietGiay(selectedGiayId.value)
    showToast(`Đang xem CTSP của ${sanPhamDuocChon.value.ten} (${sanPhamDuocChon.value.ma})`, 'info')
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không tải được sản phẩm đang chọn'), 'error')
  }
}

async function loadData(page = 0) {
  const requestId = ++latestLoadRequestId

  const effectiveGiayId = selectedGiayId.value

  loading.value = true
  try {
    const response = await api.layDanhSachChiTietSanPham({
      keyword: boLoc.keyword.trim() || undefined,
      giayId: effectiveGiayId,
      mauSacId: boLoc.mauSacId,
      kichCoId: boLoc.kichCoId,
      trangThai: boLoc.trangThai,
      page: effectiveGiayId ? 0 : page,
      size: effectiveGiayId ? 1000 : pageSize.value
    })

    if (requestId !== latestLoadRequestId) return
    items.value = response.items || []
    currentPage.value = response.page
    totalItems.value = response.totalItems
    totalPages.value = response.totalPages
  } catch (error) {
    if (requestId !== latestLoadRequestId) return
    showToast(getDisplayErrorMessage(error, 'Không tải được danh sách chi tiết sản phẩm'), 'error')
  } finally {
    if (requestId !== latestLoadRequestId) return
    loading.value = false
  }
}

async function resetFilters() {
  boLoc.keyword = ''
  boLoc.mauSacId = null
  boLoc.kichCoId = null
  boLoc.trangThai = null

  // Reset hoàn toàn: xóa sản phẩm đang chọn và giayId khỏi route
  // để hiển thị toàn bộ biến thể của tất cả sản phẩm
  if (selectedGiayId.value || sanPhamDuocChon.value) {
    sanPhamDuocChon.value = null
    suppressGiayIdWatch = true
    await router.replace({ name: 'admin-bien-the-san-pham' })
    suppressGiayIdWatch = false
  }

  loadData(0)
}

async function handleFilterChange() {
  if (selectedGiayId.value || sanPhamDuocChon.value) {
    sanPhamDuocChon.value = null
    suppressGiayIdWatch = true
    await router.replace({ name: 'admin-bien-the-san-pham' })
    suppressGiayIdWatch = false
  }
  loadData(0)
}

function scheduleKeywordSearch() {
  if (keywordSearchTimer) clearTimeout(keywordSearchTimer)
  keywordSearchTimer = setTimeout(async () => {
    if (selectedGiayId.value || sanPhamDuocChon.value) {
      sanPhamDuocChon.value = null
      suppressGiayIdWatch = true
      await router.replace({ name: 'admin-bien-the-san-pham' })
      suppressGiayIdWatch = false
    }
    loadData(0)
    keywordSearchTimer = null
  }, 300)
}

function handleQrScan(code) {
  boLoc.keyword = code
  hienThiModalQuetMa.value = false
  loadData(0)
}



function goToForm() {
  if (selectedGiayId.value) {
    router.push({
      name: 'admin-bien-the-san-pham-them',
      query: { giayId: String(selectedGiayId.value) }
    })
    return
  }

  router.push({ name: 'admin-bien-the-san-pham-them' })
}

function handlePageSizeChange(size) {
  pageSize.value = size
  loadData(0)
}

function openImageModal(item) {
  bienTheDuocChon.value = item
  hienThiModalHinhAnh.value = true
}

function openEditVariantModal(item) {
  bienTheDangSua.value = item
  bienTheForm.soLuong = Number(item.soLuong || 0)
  bienTheForm.giaGoc = Number(item.giaGoc || 0)
  bienTheForm.giaBan = Number(item.giaBan || 0)
  bienTheForm.kichHoat = Number(item.kichHoat) === 0 ? 0 : 1
  clearBienTheErrors()
  hienThiModalSuaBienThe.value = true
}

function closeEditVariantModal() {
  hienThiModalSuaBienThe.value = false
  bienTheDangSua.value = null
  dangLuuBienThe.value = false
  clearBienTheErrors()
}

function validateEditVariantForm() {
  clearBienTheErrors()

  const soLuong = parseStock(bienTheForm.soLuong)
  const giaGoc = parsePositiveMoney(bienTheForm.giaGoc)
  const giaBan = parsePositiveMoney(bienTheForm.giaBan)

  if (soLuong == null) {
    bienTheErrors.soLuong = 'Số lượng phải là số nguyên không âm'
  }
  if (giaGoc == null) {
    bienTheErrors.giaGoc = 'Giá gốc phải lớn hơn 0'
  }
  if (giaBan == null) {
    bienTheErrors.giaBan = 'Giá bán phải lớn hơn 0'
  }
  if (giaGoc != null && giaBan != null && giaGoc > giaBan) {
    bienTheErrors.giaGoc = 'Giá gốc không được lớn hơn giá bán'
  }
  if (![1, 0].includes(Number(bienTheForm.kichHoat))) {
    bienTheErrors.kichHoat = 'Trạng thái biến thể không hợp lệ'
  }

  return Object.keys(bienTheErrors).length === 0
}

async function saveEditingVariant() {
  if (!bienTheDangSua.value || dangLuuBienThe.value) return
  if (!validateEditVariantForm()) return

  dangLuuBienThe.value = true
  try {
    await api.capNhatBienThe(bienTheDangSua.value.id, {
      soLuong: Number(bienTheForm.soLuong),
      giaGoc: Number(bienTheForm.giaGoc),
      giaBan: Number(bienTheForm.giaBan),
      kichHoat: Number(bienTheForm.kichHoat)
    })
    showToast('Cập nhật biến thể thành công')
    const editedGiayId = bienTheDangSua.value.giayId
    closeEditVariantModal()
    await Promise.all([
      loadData(currentPage.value),
      selectedGiayId.value === editedGiayId ? syncSelectedProduct() : Promise.resolve()
    ])
  } catch (error) {
    Object.assign(bienTheErrors, getFieldErrors(error))
    showToast(getDisplayErrorMessage(error, 'Không thể cập nhật biến thể'), 'error')
  } finally {
    dangLuuBienThe.value = false
  }
}

function closeImageModal() {
  bienTheDuocChon.value = null
  hienThiModalHinhAnh.value = false
}

function openImageModalFromEdit(variant) {
  // Đóng modal form sửa rồi mở modal quản lý ảnh cho biến thể đó
  closeEditVariantModal()
  bienTheDuocChon.value = variant
  hienThiModalHinhAnh.value = true
}

function openVariantQr(item) {
  const qrValue = String(item?.sku || item?.maChiTietSanPham || '').trim()
  if (!qrValue) {
    showToast('Chi tiết sản phẩm này chưa có mã để tạo QR', 'error')
    return
  }

  chiTietQrDuocChon.value = {
    badge: 'QR chi tiết sản phẩm',
    title: item.tenSanPham || 'Chi tiết sản phẩm',
    subtitle: `${item.maChiTietSanPham || qrValue} • ${item.mauSac || 'Chưa có màu'} / ${item.kichCo || 'Chưa có kích cỡ'}`,
    codeLabel: item.sku ? 'SKU / mã quét' : 'Mã chi tiết sản phẩm',
    value: qrValue,
    note: 'Quét mã này ở bán hàng tại quầy để tìm nhanh đúng biến thể sản phẩm.',
    imageUrl: item.hinhAnh || '',
    imageAlt: item.tenSanPham || item.maChiTietSanPham || 'Ảnh chi tiết sản phẩm',
    detailItems: [
      { label: 'Màu sắc', value: item.mauSac || '—' },
      { label: 'Kích cỡ', value: item.kichCo || '—' },
      { label: 'Số lượng', value: Number(item.soLuong || 0).toLocaleString('vi-VN') },
      { label: 'Giá bán', value: `${formatCurrency(giaHienThi(item))} đ` },
      { label: 'Trạng thái', value: bienTheTrangThaiLabel(item) },
      { label: 'SKU', value: item.sku || '—' }
    ],
    primaryActionLabel: 'Quản lý ảnh của biến thể',
    actionType: 'manage-images',
    item
  }
  hienThiModalQr.value = true
}

function closeQrModal() {
  hienThiModalQr.value = false
  chiTietQrDuocChon.value = null
}

function handleQrPrimaryAction() {
  const actionType = chiTietQrDuocChon.value?.actionType
  const targetItem = chiTietQrDuocChon.value?.item
  if (actionType === 'manage-images' && targetItem) {
    closeQrModal()
    bienTheDuocChon.value = targetItem
    hienThiModalHinhAnh.value = true
  }
}



function triggerDownloadQr() {
  const selectedIds = bangBienTheRef.value?.selectedVariantIds
  if (!selectedIds || selectedIds.size === 0) {
    showToast('Vui lòng chọn ít nhất 1 biến thể để tải mã QR', 'error')
    return
  }
  const selectedItems = items.value.filter(i => selectedIds.has(i.id))
  handleBulkQr(selectedItems)
  selectedIds.clear()
}

async function handleBulkQr(selectedItems) {
  if (!selectedItems?.length) return
  
  const qrDataList = [];
  for (const item of selectedItems) {
    const qrValue = String(item.sku || item.maChiTietSanPham || '').trim();
    if (!qrValue) continue;
    try {
      const svg = createQrCodeSvg(qrValue);
      qrDataList.push({ item, qrValue, svg });
    } catch (e) {
      console.error(e);
    }
  }

  if (qrDataList.length === 0) {
    showToast('Không có dữ liệu hợp lệ để tạo QR', 'error');
    return;
  }

  showToast('Đang tạo ảnh QR, vui lòng đợi...', 'info');

  const CARD_WIDTH = 300;
  const CARD_HEIGHT = 380;
  const COLS = Math.min(qrDataList.length, 4);
  const ROWS = Math.ceil(qrDataList.length / COLS);
  
  const canvas = document.createElement('canvas');
  canvas.width = COLS * CARD_WIDTH;
  canvas.height = ROWS * CARD_HEIGHT;
  const ctx = canvas.getContext('2d');
  
  ctx.fillStyle = 'white';
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  
  await Promise.all(qrDataList.map((data, index) => {
    return new Promise((resolve) => {
      const img = new Image();
      const blob = new Blob([data.svg], { type: "image/svg+xml;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      
      img.onload = () => {
        const col = index % COLS;
        const row = Math.floor(index / COLS);
        const x = col * CARD_WIDTH;
        const y = row * CARD_HEIGHT;
        
        ctx.strokeStyle = '#e2e8f0';
        ctx.lineWidth = 2;
        ctx.strokeRect(x + 10, y + 10, CARD_WIDTH - 20, CARD_HEIGHT - 20);
        
        const qrSize = 200;
        const qrX = x + (CARD_WIDTH - qrSize) / 2;
        const qrY = y + 30;
        ctx.drawImage(img, qrX, qrY, qrSize, qrSize);
        
        ctx.textAlign = 'center';
        ctx.fillStyle = '#0f172a';
        ctx.font = 'bold 16px sans-serif';
        // handle long text
        let title = data.item.tenSanPham || 'Chi tiết sản phẩm';
        if (title.length > 25) title = title.substring(0, 22) + '...';
        ctx.fillText(title, x + CARD_WIDTH / 2, qrY + qrSize + 40);
        
        ctx.fillStyle = '#64748b';
        ctx.font = '14px sans-serif';
        ctx.fillText(`${data.item.mauSac} / ${data.item.kichCo}`, x + CARD_WIDTH / 2, qrY + qrSize + 65);
        
        ctx.fillStyle = '#0f172a';
        ctx.font = 'bold 18px sans-serif';
        ctx.fillText(data.qrValue, x + CARD_WIDTH / 2, qrY + qrSize + 95);
        
        URL.revokeObjectURL(url);
        resolve();
      };
      img.src = url;
    });
  }));

  const pngUrl = canvas.toDataURL("image/png");
  const link = document.createElement("a");
  link.href = pngUrl;
  link.download = `DanhSach_QRCode_${new Date().getTime()}.png`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

async function toggleBienTheStatus(item) {
  if (danhSachIdDangCapNhat.has(item.id)) return

  danhSachIdDangCapNhat.add(item.id)
  try {
    const newTrangThai = Number(item.kichHoat) === 1 ? 0 : 1

    await api.doiTrangThaiBienThe(item.id, newTrangThai)
    item.kichHoat = newTrangThai
    showSuccess('Cập nhật trạng thái thành công')
  } catch (error) {
    showError(getDisplayErrorMessage(error, 'Cập nhật trạng thái thất bại'))
  } finally {
    danhSachIdDangCapNhat.delete(item.id)
  }
}

async function xuatExcel() {
  try {
    const response = await api.layDanhSachChiTietSanPham({
      keyword: boLoc.keyword.trim() || undefined,
      giayId: selectedGiayId.value,
      mauSacId: boLoc.mauSacId,
      kichCoId: boLoc.kichCoId,
      trangThai: boLoc.trangThai,
      page: 0,
      size: Math.max(totalItems.value, pageSize.value)
    })

    const exported = exportRowsToExcel({
      filename: 'danh-sach-chi-tiet-san-pham',
      sheetName: 'ChiTietSanPham',
      columns: [
        { label: 'STT', value: (_, index) => index + 1 },
        { label: 'Mã SP', key: 'maSanPham' },
        { label: 'Mã CTSP', key: 'maChiTietSanPham' },
        { label: 'Tên sản phẩm', key: 'tenSanPham' },
        { label: 'Thương hiệu', key: 'thuongHieu' },
        { label: 'Loại giày', key: 'loaiGiay' },
        { label: 'Màu sắc', key: 'mauSac' },
        { label: 'Kích cỡ', key: 'kichCo' },
        { label: 'Số lượng', value: (row) => row.soLuong || 0 },
        { label: 'Giá bán', value: (row) => formatCurrency(giaHienThi(row)) },
        { label: 'Trạng thái', value: (row) => bienTheTrangThaiLabel(row) }
      ],
      rows: response.items || []
    })

    showToast(
      exported ? 'Xuất Excel thành công' : 'Không có dữ liệu để xuất Excel',
      exported ? 'success' : 'error'
    )
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không thể xuất Excel chi tiết sản phẩm'), 'error')
  }
}

watch(
  () => route.query.giayId,
  async () => {
    // Bỏ qua nếu route thay đổi do chính ta xóa giayId để mở rộng bộ lọc
    if (suppressGiayIdWatch) return
    await syncSelectedProduct()
    await loadData(0)
  }
)

watch(
  () => boLoc.keyword,
  () => {
    scheduleKeywordSearch()
  }
)

watch(
  () => route.query.variant,
  async (newVariant) => {
    if (newVariant) {
      boLoc.keyword = String(newVariant);
      await loadData(0);
    }
  }
)

onMounted(async () => {
  if (route.query.variant) {
    boLoc.keyword = String(route.query.variant);
  }
  await loadDanhMuc()
  await syncSelectedProduct()
  await loadData(0)

  subscribeTopic('/topic/admin/san-pham', (message) => {
    console.log("Realtime update: Variant list changed", message)
    if (!suppressGiayIdWatch) {
      loadData(currentPage.value)
    }
  })

  subscribeTopic('/topic/admin/thuoc-tinh', (message) => {
    console.log("Realtime update: Attribute changed", message)
    loadDanhMuc()
    if (!suppressGiayIdWatch) {
      loadData(currentPage.value)
    }
  })
})

onUnmounted(() => {
  closeToast()
  if (keywordSearchTimer) clearTimeout(keywordSearchTimer)
})
</script>

<template>
  <div class="space-y-5 radius-6px">

    <BoLocBienThe
      :filters="boLoc"
      :danh-muc="danhMuc"
      :selected-product="sanPhamDuocChon"
      :has-selected-variants="coBienTheDuocChon"
      @reset-filters="resetFilters"
      @export-excel="xuatExcel"
      @download-qr="triggerDownloadQr"
      @go-to-form="goToForm"
      @load-data="handleFilterChange"
      @open-scanner="hienThiModalQuetMa = true"
    />

    <BangBienThe
      ref="bangBienTheRef"
      :items="items"
      :loading="loading"
      :current-page="currentPage"
      :page-size="pageSize"
      :total-items="totalItems"
      :total-pages="totalPages"
      :page-size-options="pageSizeOptions"
      :updating-status-ids="danhSachIdDangCapNhat"
      :focused-chi-tiet-id="focusedChiTietId"
      :hide-pagination="!!selectedGiayId"
      @toggle-status="toggleBienTheStatus"
      @edit-variant="openEditVariantModal"
      @open-images="openImageModal"
      @open-qr="openVariantQr"
      @bulk-qr="handleBulkQr"
      @refresh="loadData"
      @selection-changed="coBienTheDuocChon = $event"
      @update:current-page="loadData"
      @update:page-size="handlePageSizeChange"
      @open-discount-detail="openDiscountDetail"
    />

    <QuanLySanPhamBienTheFormModal
      :is-open="hienThiModalSuaBienThe"
      :editing-bien-the="bienTheDangSua"
      :selected-giay="editingSelectedGiay"
      :danh-muc="danhMuc"
      :bien-the-form="bienTheForm"
      :bien-the-errors="bienTheErrors"
      :bulk-bien-the-form="bulkBienTheForm"
      :bulk-bien-the-errors="bulkBienTheErrors"
      :generated-bulk-bien-thes="generatedBulkBienThes"
      :saving-bien-the="dangLuuBienThe"
      @close="closeEditVariantModal"
      @save="saveEditingVariant"
      @open-images="openImageModalFromEdit"
    />

    <AdminQrCodeModal
      :open="hienThiModalQr"
      v-bind="chiTietQrDuocChon"
      @close="closeQrModal"
      @primary-action="handleQrPrimaryAction"
    />

    <QuanLySanPhamHinhAnhModal
      :open="hienThiModalHinhAnh"
      :variant="bienTheDuocChon"
      @close="closeImageModal"
      @updated="loadData(currentPage)"
      @error="showToast($event, 'error')"
    />

    <ModalQuetQR
      :is-open="hienThiModalQuetMa"
      :is-admin="true"
      @close="hienThiModalQuetMa = false"
      @scan="handleScannerResult"
    />

    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="thongBao.show && thongBao.type !== 'success'"
          class="fixed right-4 top-[88px] z-[100] w-[min(92vw,380px)] rounded-md border bg-white px-4 py-4 shadow-[0_20px_45px_rgba(15,23,42,0.12)]"
          :class="thongBao.type === 'error' ? 'border-rose-100' : 'border-slate-100'"
        >
          <div class="flex items-start gap-3">
            <div
              class="mt-0.5 rounded-md p-2"
              :class="thongBao.type === 'error' ? 'bg-rose-50 text-rose-600' : 'bg-slate-100 text-slate-600'"
            >
              <TriangleAlert v-if="thongBao.type === 'error'" class="h-5 w-5" />
              <CircleCheckBig v-else class="h-5 w-5" />
            </div>

            <div class="flex-1">
              <h3 class="font-semibold text-slate-800">{{ toastTitle }}</h3>
              <p class="mt-1 text-sm text-slate-500">{{ thongBao.message }}</p>
            </div>

            <button
              type="button"
              class="rounded-full p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
              @click="closeToast"
            >
              <X class="h-4 w-4" />
            </button>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>

