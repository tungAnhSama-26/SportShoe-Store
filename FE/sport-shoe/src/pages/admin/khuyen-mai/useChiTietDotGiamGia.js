import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  ArrowLeft,
  ArrowUpRight,
  CheckCircle2,
  CheckSquare,
  CircleX,
  RefreshCcw,
  Save,
  Search,
  Square,
  Tag,
  X,
} from "lucide-vue-next";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import {
  createDotGiamGia,
  getDotGiamGiaDetail,
  updateDotGiamGia,
  getDotGiamGiaSanPhamList,
  syncDotGiamGiaSanPham,
} from "../../../services/khuyen-mai";
import {
  chiTietGiay,
  layDanhSachGiay,
  layBienThe,
  layDanhMuc,
} from "../../../services/san-pham-api";
import {
  getDisplayErrorMessage,
  getFieldErrors,
} from "../../../utils/error-message";
import { showConfirm, showSuccess, showError } from "../../../utils/alert";

const formatToLocalDateString = (dateStr) => {
  if (!dateStr) return "";
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return dateStr;
  if (/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) return dateStr;
  const gmt7Time = d.getTime() + (7 * 60 * 60 * 1000);
  const localDate = new Date(gmt7Time);
  const year = localDate.getUTCFullYear();
  const month = String(localDate.getUTCMonth() + 1).padStart(2, '0');
  const date = String(localDate.getUTCDate()).padStart(2, '0');
  return `${year}-${month}-${date}`;
};

export function useChiTietDotGiamGia() {
  const route = useRoute();
  const router = useRouter();

  const id = route.params.id;
  const laMoi = !id;

  const dangTai = ref(false);
  const dangTaiSP = ref(false);
  const saving = ref(false);
  const loiTrang = ref("");
  function hienThiThongBao(loai, tieuDe, noiDung = "") {
    if (loai === "success") {
      showSuccess(noiDung || tieuDe, tieuDe);
    } else if (loai === "error") {
      showError(noiDung || tieuDe, tieuDe);
    }
  }
  const formErrors = reactive({});

  const form = reactive({
    id: null,
    ma: "",
    ten: "",
    moTa: "",
    loaiGiam: "1",
    giaTriGiam: "",
    ngayBatDau: "",
    ngayKetThuc: "",
    kichHoat: "1",
  });

  const isReadOnly = computed(() => false);

  const searchSP = ref("");
  const danhSachSP = ref([]);
  const selectedVariants = ref([]);
  const blockedVariantIds = ref(new Set());
  const trangBienThe = ref(1);
  const soHangMoiTrang = ref(5);
  const pageSizeOptions = [5, 10, 20, 50, 100];

  const filterMauSac = ref("");
  const filterKichCo = ref("");

  const danhMuc = reactive({
    mauSac: [],
    kichCo: [],
  });

  const danhSachSPSauKhiLoc = computed(() => {
    if (!filterMauSac.value && !filterKichCo.value) {
      return danhSachSP.value;
    }

    return danhSachSP.value
      .map((sp) => {
        if (!sp.bienThes) return sp;
        const filteredBienThes = sp.bienThes.filter((bt) => {
          let matchMau = true;
          let matchSize = true;
          if (filterMauSac.value) {
            matchMau = String(bt.mauSacId) === String(filterMauSac.value);
          }
          if (filterKichCo.value) {
            matchSize = String(bt.kichCoId) === String(filterKichCo.value);
          }
          return matchMau && matchSize;
        });

        return { ...sp, bienThes: filteredBienThes };
      })
      .filter((sp) => sp.bienThes && sp.bienThes.length > 0);
  });

  const tatCaBienThe = computed(() => {
    const result = [];
    for (const sp of danhSachSPSauKhiLoc.value) {
      for (const bt of sp.bienThes || []) {
        result.push({ ...bt, _sp: sp });
      }
    }

    // Sắp xếp sản phẩm mới nhất lên trước (theo ID giảm dần)
    // Các biến thể của cùng sản phẩm sẽ được nhóm lại với nhau
    return result.sort((a, b) => {
      const spIdA = a._sp?.id || 0;
      const spIdB = b._sp?.id || 0;
      if (spIdB !== spIdA) {
        return spIdB - spIdA;
      }
      return (b.id || 0) - (a.id || 0);
    });
  });

  // Tinh gia sau giam theo gia ban cua bien the.
  const bienTheHienThi = computed(() => {
    const mucGiam = Number(form.giaTriGiam) || 0;
    return tatCaBienThe.value.map(bt => ({
      ...bt,
      giaSauGiam: Number(bt.giaBan || 0) * (1 - mucGiam / 100)
    }));
  });

  const tongSoTrang = computed(() => Math.max(1, Math.ceil(danhSachSPSauKhiLoc.value.length / soHangMoiTrang.value)));

  const spTrang = computed(() => {
    const start = (trangBienThe.value - 1) * soHangMoiTrang.value;
    return danhSachSPSauKhiLoc.value.slice(start, start + soHangMoiTrang.value);
  });

  const bienTheTrang = computed(() => {
    const start = (trangBienThe.value - 1) * soHangMoiTrang.value;
    return bienTheHienThi.value.slice(start, start + soHangMoiTrang.value);
  });

  function getToday() {
    return new Date().toISOString().slice(0, 10);
  }

  function resetErrors() {
    Object.keys(formErrors).forEach((key) => delete formErrors[key]);
  }

  function formatCurrency(value) {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(value || 0);
  }

  function resolveProductImage(product) {
    if (product?.hinhAnh) {
      return product.hinhAnh;
    }

    if (Array.isArray(product?.hinhAnhs) && product.hinhAnhs.length) {
      return (
        product.hinhAnhs.find((item) => item?.laHinhChinh)?.url ||
        product.hinhAnhs[0]?.url ||
        ""
      );
    }

    return "";
  }

  function normalizeVariantForSelection(variant, product = null) {
    const variantId =
      variant?.giayChiTietId ??
      variant?.idChiTietSanPham ??
      variant?.chiTietId ??
      variant?.id ??
      null;

    const giaBan = Number(variant?.giaBan ?? variant?.gia ?? 0);

    return {
      ...variant,
      linkId:
        variant?.giayChiTietId != null
          ? (variant?.id ?? null)
          : (variant?.linkId ?? null),
      id: variantId,
      giayChiTietId: variantId,
      maSanPham: variant?.maSanPham || product?.ma || "",
      tenSanPham: variant?.tenSanPham || variant?.tenGiay || product?.ten || "",
      maBienThe:
        variant?.maBienThe ||
        variant?.maChiTietSanPham ||
        variant?.sku ||
        variant?.ma ||
        "",
      giaBan,
      mauSac: variant?.mauSac || variant?.tenMauSac || "",
      kichCo: variant?.kichCo || variant?.tenKichCo || "",
      thuongHieu: variant?.thuongHieu || product?.thuongHieu || "",
      hinhAnh: variant?.hinhAnh || resolveProductImage(product),
      giayId: variant?.giayId || variant?.idGiay || product?.id || null,
      sku:
        variant?.sku ||
        variant?.maBienThe ||
        variant?.maChiTietSanPham ||
        variant?.ma ||
        "",
      soLuong: Number(variant?.soLuong ?? variant?.soLuongTon ?? 0),
    };
  }

  function hopNhatBienThe(existing, incoming) {
    return {
      ...existing,
      ...incoming,
      linkId: incoming?.linkId ?? existing?.linkId ?? null,
      id: incoming?.id ?? existing?.id ?? null,
      giayChiTietId:
        incoming?.giayChiTietId ??
        existing?.giayChiTietId ??
        incoming?.id ??
        existing?.id ??
        null,
      giayId: incoming?.giayId ?? existing?.giayId ?? null,
      maSanPham: incoming?.maSanPham || existing?.maSanPham || "",
      tenSanPham: incoming?.tenSanPham || existing?.tenSanPham || "",
      maBienThe: incoming?.maBienThe || existing?.maBienThe || "",
      giaBan: Number(incoming?.giaBan ?? existing?.giaBan ?? 0),
      mauSac: incoming?.mauSac || existing?.mauSac || "",
      kichCo: incoming?.kichCo || existing?.kichCo || "",
      thuongHieu: incoming?.thuongHieu || existing?.thuongHieu || "",
      hinhAnh: incoming?.hinhAnh || existing?.hinhAnh || "",
      sku: incoming?.sku || existing?.sku || "",
      soLuong: Number(incoming?.soLuong ?? existing?.soLuong ?? 0),
    };
  }

  function dedupeSelectedVariants(items) {
    const selectedMap = new Map();

    for (const item of items) {
      const normalized = normalizeVariantForSelection(item);
      const variantId = Number(normalized.id);
      if (!Number.isInteger(variantId) || variantId <= 0) {
        continue;
      }

      const current = selectedMap.get(variantId);
      selectedMap.set(
        variantId,
        current ? hopNhatBienThe(current, normalized) : normalized,
      );
    }

    return Array.from(selectedMap.values());
  }

  function dongBoBienTheDaChonTheoDanhSachSanPham(products) {
    if (!selectedVariants.value.length) {
      return;
    }

    const variantLookup = new Map();
    for (const product of products) {
      for (const variant of product.bienThes || []) {
        const normalized = normalizeVariantForSelection(variant, product);
        const variantId = Number(normalized.id);
        if (Number.isInteger(variantId) && variantId > 0) {
          variantLookup.set(variantId, normalized);
        }
      }
    }

    selectedVariants.value = dedupeSelectedVariants(
      selectedVariants.value.map((variant) => {
        const variantId = Number(variant?.id);
        return variantLookup.get(variantId) ?? variant;
      }),
    );
  }

  async function taiSanPhamDaChonConThieu(items) {
    if (!selectedVariants.value.length || searchSP.value.trim()) {
      return items;
    }

    const existingProductIds = new Set(
      items
        .map((item) => Number(item?.id))
        .filter((productId) => Number.isInteger(productId) && productId > 0),
    );

    const missingProductIds = Array.from(
      new Set(
        selectedVariants.value
          .map((variant) => Number(variant?.giayId))
          .filter(
            (productId) =>
              Number.isInteger(productId) &&
              productId > 0 &&
              !existingProductIds.has(productId),
          ),
      ),
    );

    if (!missingProductIds.length) {
      return items;
    }

    const extraProducts = (
      await Promise.all(
        missingProductIds.map(async (productId) => {
          try {
            const [productDetail, variants] = await Promise.all([
              chiTietGiay(productId),
              layBienThe(productId),
            ]);

            return {
              id: productId,
              ten: productDetail?.ten || "",
              ma: productDetail?.ma || "",
              thuongHieu: productDetail?.thuongHieu || "",
              hinhAnh: resolveProductImage(productDetail),
              bienThes: (variants || []).map((variant) =>
                normalizeVariantForSelection(variant, productDetail),
              ),
            };
          } catch (error) {
            console.error("Khong the tai san pham da chon:", error);
            return null;
          }
        }),
      )
    ).filter(Boolean);

    return [...extraProducts, ...items];
  }

  function tinhGiaGiam(giaBan) {
    const giam = Number(form.giaTriGiam) || 0;
    return Number(giaBan || 0) * (1 - giam / 100);
  }

  function taoMaNgauNhien() {
    form.ma = "DGG" + Math.random().toString(36).substring(2, 8).toUpperCase();
  }

  async function taiDanhSachSP() {
    dangTaiSP.value = true;
    try {
      const res = await layDanhSachGiay({
        keyword: searchSP.value,
        page: 0,
        size: 50,
      });
      const items = [...(res?.content || res?.items || [])];

      // Load variants for each product to allow selection
      for (const item of items) {
        if (!item.bienThes) {
          try {
            const btRes = await layBienThe(item.id);
            item.bienThes = (btRes || []).map((bt) =>
              normalizeVariantForSelection(bt, item),
            );
          } catch (err) {
            item.bienThes = [];
          }
        }
      }
      const mergedItems = await taiSanPhamDaChonConThieu(items);
      danhSachSP.value = mergedItems;
      dongBoBienTheDaChonTheoDanhSachSanPham(mergedItems);
    } catch (e) {
      console.error("Lỗi tải sản phẩm:", e);
    } finally {
      dangTaiSP.value = false;
    }
  }

  let searchTimer;

  watch(searchSP, () => {
    clearTimeout(searchTimer);
    trangBienThe.value = 1;
    searchTimer = setTimeout(taiDanhSachSP, 400);
  });

  watch([filterMauSac, filterKichCo], () => {
    trangBienThe.value = 1;
  });

  watch(
    () => form.giaTriGiam,
    (newVal) => {
      if (newVal === "" || newVal === null || newVal === undefined) {
        delete formErrors.giaTriGiam;
        return;
      }
      const val = Number(newVal);
      if (val <= 0) {
        formErrors.giaTriGiam = "Giá trị giảm phải lớn hơn 0%";
      } else if (val > 100) {
        formErrors.giaTriGiam = "Phần trăm giảm không được vượt quá 100%";
      } else {
        delete formErrors.giaTriGiam;
      }
    },
  );

  watch(
    () => form.ngayBatDau,
    (newVal) => {
      if (!newVal) {
        formErrors.ngayBatDau = "Vui lòng chọn ngày bắt đầu áp dụng";
        return;
      }
      delete formErrors.ngayBatDau;
      if (laMoi && newVal < getToday()) {
        formErrors.ngayBatDau = "Ngày bắt đầu không được ở trong quá khứ";
      }
      if (form.ngayKetThuc && newVal > form.ngayKetThuc) {
        formErrors.ngayKetThuc = "Ngày kết thúc không được trước ngày bắt đầu";
      } else {
        delete formErrors.ngayKetThuc;
      }
    }
  );

  watch(
    () => form.ngayKetThuc,
    (newVal) => {
      if (!newVal) {
        formErrors.ngayKetThuc = "Vui lòng chọn ngày kết thúc áp dụng";
        return;
      }
      delete formErrors.ngayKetThuc;
      if (form.ngayBatDau && form.ngayBatDau > newVal) {
        formErrors.ngayKetThuc = "Ngày kết thúc không được trước ngày bắt đầu";
      }
    }
  );

  function isVariantSelected(variantId) {
    return selectedVariants.value.some((v) => Number(v.id) === Number(variantId));
  }

  function isVariantBlocked(variantId) {
    return blockedVariantIds.value.has(Number(variantId));
  }

  const tatCaCoTheChon = computed(() =>
    tatCaBienThe.value.filter(bt => !isVariantBlocked(bt.id))
  );

  const tatCaDaChon = computed(() =>
    tatCaCoTheChon.value.length > 0 &&
    tatCaCoTheChon.value.every(bt => isVariantSelected(bt.id))
  );

  const motSoDaChon = computed(() =>
    !tatCaDaChon.value &&
    tatCaCoTheChon.value.some(bt => isVariantSelected(bt.id))
  );

  function isProductBlocked(sp) {
    if (!sp.bienThes || !sp.bienThes.length) return true;
    return sp.bienThes.every(bt => isVariantBlocked(bt.id || bt.giayChiTietId));
  }

  function getProductSelectState(sp) {
    if (!sp.bienThes || !sp.bienThes.length) return { checked: false, indeterminate: false, disabled: true };
    const unblocked = sp.bienThes.filter(bt => !isVariantBlocked(bt.id || bt.giayChiTietId));
    if (unblocked.length === 0) return { checked: false, indeterminate: false, disabled: true };

    const selectedCount = unblocked.filter(bt => isVariantSelected(bt.id || bt.giayChiTietId)).length;
    return {
      checked: selectedCount > 0 && selectedCount === unblocked.length,
      indeterminate: selectedCount > 0 && selectedCount < unblocked.length,
      disabled: false
    };
  }

  function toggleProduct(sp) {
    if (!sp.bienThes) return;
    const state = getProductSelectState(sp);
    const unblocked = sp.bienThes.filter(bt => !isVariantBlocked(bt.id || bt.giayChiTietId));
    if (state.checked) {
      // Bỏ chọn tất cả
      unblocked.forEach(bt => removeSelectedVariant(bt.id || bt.giayChiTietId));
    } else {
      // Chọn tất cả
      unblocked.forEach(bt => {
        if (!isVariantSelected(bt.id || bt.giayChiTietId)) {
          toggleVariant(bt, sp);
        }
      });
    }
  }

  function toggleChonTatCa() {
    if (tatCaDaChon.value) {
      tatCaCoTheChon.value.forEach(bt => removeSelectedVariant(bt.id));
    } else {
      tatCaCoTheChon.value.forEach(bt => {
        if (!isVariantSelected(bt.id)) toggleVariant(bt, bt._sp);
      });
    }
  }

  function toggleVariant(variant, product) {
    const normalized = normalizeVariantForSelection(variant, product);
    const index = selectedVariants.value.findIndex(
      (v) => Number(v.id) === Number(normalized.id),
    );
    if (index === -1) {
      // Thêm sản phẩm mới vào đầu danh sách
      selectedVariants.value = [
        normalized,
        ...dedupeSelectedVariants(selectedVariants.value),
      ];
    } else {
      selectedVariants.value.splice(index, 1);
      selectedVariants.value = dedupeSelectedVariants(selectedVariants.value);
    }
  }

  function removeSelectedVariant(variantId) {
    selectedVariants.value = dedupeSelectedVariants(
      selectedVariants.value.filter((v) => Number(v.id) !== Number(variantId)),
    );
  }

  const expandedProducts = ref(new Set());

  function toggleProductExpansion(productId) {
    if (expandedProducts.value.has(productId)) {
      expandedProducts.value.delete(productId);
    } else {
      expandedProducts.value.add(productId);
    }
  }

  async function taiDanhMuc() {
    try {
      const res = await layDanhMuc();
      danhMuc.mauSac = res.mauSac || [];
      danhMuc.kichCo = res.kichCo || [];
    } catch (error) {
      console.error("Lỗi tải danh mục:", error);
    }
  }

  async function taiChiTiet() {
    if (laMoi) {
      if (!form.ma) {
        taoMaNgauNhien();
      }
      form.ngayBatDau = getToday();
      // Keep blocked variants empty to allow assigning products to multiple promotions
      blockedVariantIds.value = new Set();
      await taiDanhMuc();
      await taiDanhSachSP();
      return;
    }
    dangTai.value = true;
    try {
      const detail = await getDotGiamGiaDetail(id);
      Object.assign(form, {
        id: detail.id,
        ma: detail.ma ?? "",
        ten: detail.ten ?? "",
        moTa: detail.moTa ?? "",
        loaiGiam: String(detail.loaiGiam ?? 1),
        giaTriGiam: detail.giaTriGiam ?? "",
        ngayBatDau: formatToLocalDateString(detail.ngayBatDau),
        ngayKetThuc: formatToLocalDateString(detail.ngayKetThuc),
        kichHoat: String(detail.kichHoat ?? 1)
      });

      // Load selected variants for this campaign
      const spList = await getDotGiamGiaSanPhamList();
      selectedVariants.value = dedupeSelectedVariants(
        spList
          .filter((item) => String(item.dotGiamGiaId) === String(id))
          .map((item) => normalizeVariantForSelection(item)),
      );

      // Keep blocked variants empty to allow assigning products to multiple promotions
      blockedVariantIds.value = new Set();

      await taiDanhMuc();
      await taiDanhSachSP();
    } catch (e) {
      loiTrang.value = getDisplayErrorMessage(
        e,
        "Không thể tải chi tiết đợt giảm giá",
      );
    } finally {
      dangTai.value = false;
    }
  }

  async function submitForm() {
    resetErrors();
    let isValid = true;

    if (!form.ma.trim()) {
      formErrors.ma = "Vui lòng nhập mã đợt giảm giá";
      isValid = false;
    } else if (form.ma.trim().length > 100) {
      formErrors.ma = "Mã đợt giảm giá không được vượt quá 100 ký tự";
      isValid = false;
    } else if (!/^[A-Za-z0-9_-]+$/.test(form.ma.trim())) {
      formErrors.ma =
        "Mã đợt chỉ được chứa chữ, số, dấu gạch ngang và gạch dưới";
      isValid = false;
    }
    if (!form.ten.trim()) {
      formErrors.ten = "Vui lòng nhập tên đợt giảm giá";
      isValid = false;
    } else if (form.ten.trim().length > 200) {
      formErrors.ten = "Tên đợt giảm giá không được vượt quá 200 ký tự";
      isValid = false;
    }
    if ((form.moTa || "").trim().length > 500) {
      formErrors.moTa = "Mô tả không được vượt quá 500 ký tự";
      isValid = false;
    }
    if (!form.giaTriGiam || Number(form.giaTriGiam) <= 0) {
      formErrors.giaTriGiam = "Giá trị giảm phải lớn hơn 0%";
      isValid = false;
    } else if (Number(form.giaTriGiam) > 100) {
      formErrors.giaTriGiam = "Phần trăm giảm không được vượt quá 100%";
      isValid = false;
    }
    if (!form.ngayBatDau) {
      formErrors.ngayBatDau = "Vui lòng chọn ngày bắt đầu áp dụng";
      isValid = false;
    } else if (laMoi && form.ngayBatDau < getToday()) {
      formErrors.ngayBatDau = "Ngày bắt đầu không được ở trong quá khứ";
      isValid = false;
    }

    if (!form.ngayKetThuc) {
      formErrors.ngayKetThuc = "Vui lòng chọn ngày kết thúc áp dụng";
      isValid = false;
    }

    if (
      form.ngayBatDau &&
      form.ngayKetThuc &&
      form.ngayBatDau > form.ngayKetThuc
    ) {
      formErrors.ngayKetThuc =
        "Ngày kết thúc không được trước ngày bắt đầu";
      isValid = false;
    }
    if (selectedVariants.value.length === 0) {
      formErrors.giayChiTietIds =
        "Phải chọn ít nhất một biến thể sản phẩm";
      isValid = false;
    }

    if (!isValid) return;

    const confirmMsg = laMoi
      ? "Bạn có chắc chắn muốn thêm mới đợt giảm giá này không?"
      : "Bạn có chắc chắn muốn cập nhật thông tin đợt giảm giá này không?";
    const isConfirmed = await showConfirm(confirmMsg);
    if (!isConfirmed) return;

    saving.value = true;
    loiTrang.value = "";
    try {
      const payload = {
        ma: form.ma.trim(),
        ten: form.ten.trim(),
        moTa: (form.moTa || "").trim(),
        loaiGiam: Number(form.loaiGiam),
        giaTriGiam: Number(form.giaTriGiam),
        ngayBatDau: form.ngayBatDau,
        ngayKetThuc: form.ngayKetThuc,
        kichHoat: laMoi ? undefined : form.kichHoat,
      };

      let campaignId = id;
      if (laMoi) {
        const res = await createDotGiamGia(payload);
        campaignId = res.id;
      } else {
        await updateDotGiamGia(id, payload);
      }

      await syncDotGiamGiaSanPham({
        dotGiamGiaId: Number(campaignId),
        giayChiTietIds: Array.from(
          new Set(
            selectedVariants.value
              .map((variant) => Number(variant?.id))
              .filter(
                (variantId) => Number.isInteger(variantId) && variantId > 0,
              ),
          ),
        ),
      });

      hienThiThongBao(
        "success",
        "Thành công",
        laMoi ? "Đã tạo mới đợt giảm giá thành công." : "Đã cập nhật đợt giảm giá thành công."
      );

      // Lưu thông báo vào sessionStorage để hiển thị ở trang danh sách
      if (laMoi) {
        window.sessionStorage.setItem(
          "admin-dot-giam-gia-toast",
          JSON.stringify({
            loai: "success",
            tieuDe: "Thành công",
            noiDung: "Đã tạo mới đợt giảm giá thành công."
          })
        );
      }

      setTimeout(() => {
        router.push({ name: "admin-dot-giam-gia" });
      }, 500);
    } catch (error) {
      Object.assign(formErrors, getFieldErrors(error));
      const msg = getDisplayErrorMessage(error, "Không thể lưu đợt giảm giá");
      loiTrang.value = msg;

      // Thêm thông báo lỗi dạng Toast
      hienThiThongBao(
        "error",
        laMoi ? "Lỗi tạo mới" : "Lỗi cập nhật",
        msg
      );
    } finally {
      saving.value = false;
    }
  }

  onMounted(taiChiTiet);

  return { computed, onMounted, reactive, ref, watch, useRoute, useRouter, ArrowLeft, ArrowUpRight, CheckCircle2, CheckSquare, CircleX, RefreshCcw, Save, Search, Square, Tag, X, AdminTableFooter, createDotGiamGia, getDotGiamGiaDetail, updateDotGiamGia, getDotGiamGiaSanPhamList, syncDotGiamGiaSanPham, chiTietGiay, layDanhSachGiay, layBienThe, getDisplayErrorMessage, route, router, id, laMoi, dangTai, dangTaiSP, saving, loiTrang, hienThiThongBao, formErrors, form, isReadOnly, searchSP, danhSachSP, danhSachSPSauKhiLoc, spTrang, selectedVariants, blockedVariantIds, trangBienThe, soHangMoiTrang, pageSizeOptions, tatCaBienThe, tongSoTrang, bienTheTrang, getToday, resetErrors, formatCurrency, resolveProductImage, normalizeVariantForSelection, hopNhatBienThe, dedupeSelectedVariants, dongBoBienTheDaChonTheoDanhSachSanPham, taiSanPhamDaChonConThieu, tinhGiaGiam, taoMaNgauNhien, taiDanhSachSP, searchTimer, isVariantSelected, isVariantBlocked, tatCaCoTheChon, tatCaDaChon, motSoDaChon, isProductBlocked, getProductSelectState, toggleProduct, toggleChonTatCa, toggleVariant, removeSelectedVariant, expandedProducts, toggleProductExpansion, taiChiTiet, submitForm, filterMauSac, filterKichCo, danhMuc };
}
