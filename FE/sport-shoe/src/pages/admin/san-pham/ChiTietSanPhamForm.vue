<script setup>
import {
  computed,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
  watch,
} from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  Check,
  CheckCircle2,
  ChevronDown,
  Plus,
  Save,
  Search,
  X,
} from "lucide-vue-next";
import * as api from "../../../services/san-pham-api";
import {
  chatLieuGiayApi,
  coGiayApi,
  congNgheDemApi,
  deGiayApi,
  kichCoApi,
  loaiGiayApi,
  mauSacApi,
  thuongHieuApi,
  trongLuongApi,
} from "../../../services/danh-muc-api";
import BienTheImageManager from "../../../components/admin/san-pham/BienTheImageManager.vue";
import ChiTietSanPhamGeneratedVariantsSection from "../../../components/admin/san-pham/ChiTietSanPhamGeneratedVariantsSection.vue";
import AdminSearchableSelect from "../../../components/common/AdminSearchableSelect.vue";
import {
  DEFAULT_COLOR_HEX,
  generateColorAttributeCode,
  generateHexColorFromText,
  isValidHexColor,
  normalizeSizeValue,
} from "../../../utils/thuoc-tinh-san-pham";
import {
  getDisplayErrorMessage,
  getFieldErrors,
} from "../../../utils/error-message";

const route = useRoute();
const router = useRouter();

const danhMuc = ref(null);
const loadingInit = ref(false);
const saving = ref(false);
const currentProduct = ref(null);
const currentProductId = ref(null);
const createdVariants = ref([]);
const draftColorImages = ref({});
const createdImageManagerRefs = ref({});
const showCreatedImagesModal = ref(false);

const redirectPopup = reactive({
  show: false,
  title: "",
  message: "",
  giayId: null,
  chiTietId: null,
});

const toast = reactive({
  show: false,
  message: "",
  type: "success",
});
let toastTimer = null;
let redirectTimer = null;

const productForm = reactive({
  ten: "",
  thuongHieuId: null,
  loaiGiayId: null,
  gioiTinh: null,
  chatLieuGiayId: null,
  moTa: "",
  deGiayId: null,
  coGiayId: null,
  congNgheDemId: null,
  trongLuongId: null,
});

const productErrors = reactive({});

const variantBuilder = reactive({
  mauSacIds: [],
  kichCoIds: [],
  soLuong: 0,
  giaGoc: 0,
  giaBan: 0,
});

const variantErrors = reactive({});
const generatedVariants = ref([]);
const openVariantDropdown = ref(null);
const mauSacSearch = ref("");
const kichCoSearch = ref("");
const mauSacDropdownRef = ref(null);
const kichCoDropdownRef = ref(null);
const quickCreateOpen = ref(false);
const quickCreateType = ref(null);
const quickCreateSaving = ref(false);
const quickCreateErrors = reactive({});
const quickCreateForm = reactive({
  ma: "",
  ten: "",
  xuatXu: "",
  maMauHex: DEFAULT_COLOR_HEX,
  giaTri: "",
  ghiChu: "",
});
const inlineCreatingType = ref(null);
const quickCreateColorSeed = ref(createAttributeSeed());
const quickCreatedPriority = reactive({
  thuongHieu: [],
  loaiGiay: [],
  chatLieuGiay: [],
  deGiay: [],
  coGiay: [],
  congNgheDem: [],
  trongLuong: [],
  mauSac: [],
  kichCo: [],
});

const isExistingProduct = computed(() => Boolean(currentProductId.value));
const productCode = computed(() => currentProduct.value?.ma || "(Tự sinh)");
const pageTitle = computed(() => "THÊM CHI TIẾT SẢN PHẨM");

const genderSearchOptions = [
  { value: 1, label: "Nam" },
  { value: 2, label: "N\u1EEF" },
  { value: 3, label: "Unisex" },
];

const quickCreateDefinitions = {
  thuongHieu: {
    title: "Thêm nhanh thương hiệu",
    description: "Tạo thương hiệu mới và gán luôn cho sản phẩm hiện tại.",
    fields: [
      { key: "ma", label: "Mã *", placeholder: "VD: NIKE", uppercase: true },
      {
        key: "ten",
        label: "Tên thương hiệu *",
        placeholder: "Nhập tên thương hiệu...",
      },
      { key: "xuatXu", label: "Xuất xứ", placeholder: "VD: Việt Nam" },
    ],
  },
  loaiGiay: {
    title: "Thêm nhanh loại giày",
    description: "Tạo loại giày mới mà không cần rời khỏi form.",
    fields: [
      { key: "ma", label: "Mã *", placeholder: "VD: RUN", uppercase: true },
      {
        key: "ten",
        label: "Tên loại giày *",
        placeholder: "Nhập tên loại giày...",
      },
    ],
  },
  chatLieuGiay: {
    title: "Thêm nhanh chất liệu",
    description: "Tạo chất liệu mới và chọn ngay cho sản phẩm.",
    fields: [
      { key: "ma", label: "Mã *", placeholder: "VD: CLMESH", uppercase: true },
      {
        key: "ten",
        label: "Tên chất liệu *",
        placeholder: "Nhập tên chất liệu...",
      },
    ],
  },
  deGiay: {
    title: "Thêm nhanh đế giày",
    description: "Tạo đế giày mới ngay trong form sản phẩm.",
    fields: [
      { key: "ma", label: "Mã *", placeholder: "VD: DG01", uppercase: true },
      {
        key: "ten",
        label: "Tên đế giày *",
        placeholder: "Nhập tên đế giày...",
      },
    ],
  },
  coGiay: {
    title: "Thêm nhanh cổ giày",
    description: "Tạo cổ giày mới và áp dụng luôn cho sản phẩm.",
    fields: [
      { key: "ma", label: "Mã *", placeholder: "VD: CGLOW", uppercase: true },
      {
        key: "ten",
        label: "Tên cổ giày *",
        placeholder: "Nhập tên cổ giày...",
      },
    ],
  },
  congNgheDem: {
    title: "Thêm nhanh công nghệ đệm",
    description: "Tạo công nghệ đệm mới mà không cần sang màn danh mục.",
    fields: [
      { key: "ma", label: "Mã *", placeholder: "VD: AIRMAX", uppercase: true },
      {
        key: "ten",
        label: "Tên công nghệ đệm *",
        placeholder: "Nhập tên công nghệ...",
      },
    ],
  },
  trongLuong: {
    title: "Thêm nhanh trọng lượng",
    description: "Tạo nhanh trọng lượng mới để gán ngay cho sản phẩm.",
    fields: [
      { key: "ma", label: "Mã *", placeholder: "VD: TL250", uppercase: true },
      {
        key: "giaTri",
        label: "Trọng lượng (gram) *",
        placeholder: "VD: 250",
        type: "number",
        min: 1,
      },
    ],
  },
  mauSac: {
    title: "Thêm nhanh màu sắc",
    description: "Tạo màu sắc mới và thêm luôn vào bộ biến thể đang chọn.",
    fields: [
      { key: "ma", label: "Mã *", placeholder: "VD: RED01", uppercase: true },
      {
        key: "ten",
        label: "Tên màu sắc *",
        placeholder: "Nhập tên màu sắc...",
      },
      { key: "maMauHex", label: "Mã HEX *", type: "color" },
    ],
  },
  kichCo: {
    title: "Thêm nhanh kích cỡ",
    description: "Tạo kích cỡ mới và chọn luôn vào danh sách biến thể.",
    fields: [
      { key: "giaTri", label: "Kích cỡ *", placeholder: "VD: 42" },
      {
        key: "ghiChu",
        label: "Ghi chú",
        placeholder: "Ghi chú thêm nếu cần...",
      },
    ],
  },
};

const quickCreateDefinition = computed(() =>
  quickCreateType.value
    ? quickCreateDefinitions[quickCreateType.value] || null
    : null,
);

watch(
  [() => quickCreateType.value, () => quickCreateForm.ten],
  ([type]) => {
    if (type !== "mauSac") {
      return;
    }

    quickCreateForm.ma = generateColorAttributeCode(
      quickCreateForm.ten,
      quickCreateColorSeed.value,
    );
    quickCreateForm.maMauHex = generateHexColorFromText(quickCreateForm.ten);
  },
  { immediate: true },
);

const thuongHieuOptions = computed(() =>
  (danhMuc.value?.thuongHieu || []).map((item) => ({
    value: item.id,
    label: item.ten,
  })),
);

const loaiGiayOptions = computed(() =>
  (danhMuc.value?.loaiGiay || []).map((item) => ({
    value: item.id,
    label: item.ten,
  })),
);

const chatLieuOptions = computed(() =>
  (danhMuc.value?.chatLieuGiay || []).map((item) => ({
    value: item.id,
    label: item.ten,
  })),
);

const deGiayOptions = computed(() =>
  (danhMuc.value?.deGiay || []).map((item) => ({
    value: item.id,
    label: item.ten,
  })),
);

const coGiayOptions = computed(() =>
  (danhMuc.value?.coGiay || []).map((item) => ({
    value: item.id,
    label: item.ten,
  })),
);

const congNgheDemOptions = computed(() =>
  (danhMuc.value?.congNgheDem || []).map((item) => ({
    value: item.id,
    label: item.ten,
  })),
);

const trongLuongOptions = computed(() =>
  (danhMuc.value?.trongLuong || []).map((item) => ({
    value: item.id,
    label: Number(item.giaTri || 0).toLocaleString("vi-VN"),
    searchText: `${item.ma || ""} ${item.giaTri || ""} ${item.moTa || ""}`,
    createMatchText: String(item.giaTri || ""),
  })),
);

function trimToNull(value) {
  const normalized = String(value ?? "").trim();
  return normalized || null;
}

function normalizeCodeValue(value) {
  return String(value ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[đĐ]/g, (char) => (char === "đ" ? "d" : "D"))
    .toUpperCase()
    .replace(/[^A-Z0-9]/g, "");
}

function generateInlineCode(prefix, value) {
  const seed = normalizeCodeValue(value).slice(0, 6) || "NEW";
  const suffix = Date.now().toString(36).toUpperCase().slice(-4);
  return `${prefix}${seed}${suffix}`;
}

function createAttributeSeed() {
  return Date.now().toString(36).toUpperCase().slice(-4);
}

function pinQuickCreatedAttribute(type, id) {
  if (!Object.prototype.hasOwnProperty.call(quickCreatedPriority, type)) {
    return;
  }

  const numericId = Number(id);
  quickCreatedPriority[type] = [
    numericId,
    ...quickCreatedPriority[type].filter((item) => item !== numericId),
  ];
}

function sortItemsByPriority(items, priorityIds = []) {
  if (!Array.isArray(items) || !items.length || !priorityIds.length) {
    return items || [];
  }

  const prioritySet = new Set(priorityIds.map(Number));
  const priorityMap = new Map(
    priorityIds.map((id, index) => [Number(id), index]),
  );

  return [...items].sort((first, second) => {
    const firstPriority = prioritySet.has(Number(first.id));
    const secondPriority = prioritySet.has(Number(second.id));

    if (firstPriority && secondPriority) {
      return (
        priorityMap.get(Number(first.id)) - priorityMap.get(Number(second.id))
      );
    }

    if (firstPriority) return -1;
    if (secondPriority) return 1;
    return 0;
  });
}

async function handleInlineCreateAttribute(type, rawValue) {
  const value = trimToNull(rawValue);
  if (!value || inlineCreatingType.value) return;

  inlineCreatingType.value = type;
  try {
    let created = null;

    switch (type) {
      case "thuongHieu":
        created = await thuongHieuApi.create({
          ma: generateInlineCode("TH", value),
          ten: value,
          xuatXu: null,
          logoUrl: null,
          website: null,
          moTa: null,
        });
        break;
      case "loaiGiay":
        created = await loaiGiayApi.create({
          ma: generateInlineCode("LG", value),
          ten: value,
          moTa: null,
        });
        break;
      case "chatLieuGiay":
        created = await chatLieuGiayApi.create({
          ma: generateInlineCode("CL", value),
          ten: value,
          moTa: null,
        });
        break;
      case "deGiay":
        created = await deGiayApi.create({
          ma: generateInlineCode("DG", value),
          ten: value,
          moTa: null,
        });
        break;
      case "coGiay":
        created = await coGiayApi.create({
          ma: generateInlineCode("CG", value),
          ten: value,
          moTa: null,
        });
        break;
      case "congNgheDem":
        created = await congNgheDemApi.create({
          ma: generateInlineCode("CND", value),
          ten: value,
          moTa: null,
        });
        break;
      case "trongLuong": {
        const parsedWeight = Number.parseInt(
          String(value).replace(/[^\d]/g, ""),
          10,
        );
        if (!Number.isInteger(parsedWeight) || parsedWeight < 1) {
          throw new Error("Trọng lượng phải là số nguyên lớn hơn 0");
        }

        created = await trongLuongApi.create({
          ma: generateInlineCode("TL", parsedWeight),
          giaTri: parsedWeight,
          moTa: null,
        });
        break;
      }
      default:
        return;
    }

    if (!created?.id) {
      throw new Error("Không nhận được dữ liệu thuộc tính vừa tạo");
    }

    pinQuickCreatedAttribute(type, created.id);
    await loadDanhMuc();
    assignQuickCreatedValue(type, created.id);
    showToast("Đã thêm thuộc tính mới vào form");
  } catch (error) {
    showToast(
      getDisplayErrorMessage(error, "Không thể thêm nhanh thuộc tính"),
      "error",
    );
  } finally {
    inlineCreatingType.value = null;
  }
}

function clearQuickCreateErrors() {
  Object.keys(quickCreateErrors).forEach(
    (key) => delete quickCreateErrors[key],
  );
}

function resetQuickCreateForm() {
  quickCreateColorSeed.value = createAttributeSeed();
  Object.assign(quickCreateForm, {
    ma: "",
    ten: "",
    xuatXu: "",
    maMauHex: DEFAULT_COLOR_HEX,
    giaTri: "",
    ghiChu: "",
  });
  clearQuickCreateErrors();
}

function openQuickCreate(type, presetValue = "") {
  resetQuickCreateForm();
  quickCreateType.value = type;
  quickCreateOpen.value = true;
  if (type === "mauSac" || type === "kichCo") {
    closeVariantDropdown();
  }

  const preset = String(presetValue || "").trim();
  if (!preset) return;

  if (type === "kichCo" || type === "trongLuong") {
    quickCreateForm.giaTri = preset;
    return;
  }

  quickCreateForm.ten = preset;
}

function closeQuickCreate() {
  quickCreateOpen.value = false;
  quickCreateType.value = null;
  quickCreateSaving.value = false;
  resetQuickCreateForm();
}

function assignQuickCreatedValue(type, id) {
  const numericId = Number(id);

  switch (type) {
    case "thuongHieu":
      productForm.thuongHieuId = numericId;
      break;
    case "loaiGiay":
      productForm.loaiGiayId = numericId;
      break;
    case "chatLieuGiay":
      productForm.chatLieuGiayId = numericId;
      break;
    case "deGiay":
      productForm.deGiayId = numericId;
      break;
    case "coGiay":
      productForm.coGiayId = numericId;
      break;
    case "congNgheDem":
      productForm.congNgheDemId = numericId;
      break;
    case "trongLuong":
      productForm.trongLuongId = numericId;
      break;
    case "mauSac":
      variantBuilder.mauSacIds = [
        numericId,
        ...variantBuilder.mauSacIds.filter((item) => item !== numericId),
      ];
      mauSacSearch.value = "";
      break;
    case "kichCo":
      variantBuilder.kichCoIds = [
        numericId,
        ...variantBuilder.kichCoIds.filter((item) => item !== numericId),
      ];
      kichCoSearch.value = "";
      break;
  }
}

function validateQuickCreateForm() {
  clearQuickCreateErrors();

  if (quickCreateType.value === "kichCo") {
    const normalizedSize = normalizeSizeValue(quickCreateForm.giaTri);
    if (!normalizedSize) {
      quickCreateErrors.giaTri =
        "Vui lòng nhập kích cỡ theo dạng 42, 40.5 hoặc EU42";
      return false;
    }

    quickCreateForm.giaTri = normalizedSize;
  }

  switch (quickCreateType.value) {
    case "thuongHieu":
      if (!trimToNull(quickCreateForm.ma))
        quickCreateErrors.ma = "Vui lòng nhập mã thương hiệu";
      if (!trimToNull(quickCreateForm.ten))
        quickCreateErrors.ten = "Vui lòng nhập tên thương hiệu";
      break;
    case "loaiGiay":
      if (!trimToNull(quickCreateForm.ma))
        quickCreateErrors.ma = "Vui lòng nhập mã loại giày";
      if (!trimToNull(quickCreateForm.ten))
        quickCreateErrors.ten = "Vui lòng nhập tên loại giày";
      break;
    case "chatLieuGiay":
      if (!trimToNull(quickCreateForm.ma))
        quickCreateErrors.ma = "Vui lòng nhập mã chất liệu giày";
      if (!trimToNull(quickCreateForm.ten))
        quickCreateErrors.ten = "Vui lòng nhập tên chất liệu giày";
      break;
    case "deGiay":
      if (!trimToNull(quickCreateForm.ma))
        quickCreateErrors.ma = "Vui lòng nhập mã đế giày";
      if (!trimToNull(quickCreateForm.ten))
        quickCreateErrors.ten = "Vui lòng nhập tên đế giày";
      break;
    case "coGiay":
      if (!trimToNull(quickCreateForm.ma))
        quickCreateErrors.ma = "Vui lòng nhập mã cổ giày";
      if (!trimToNull(quickCreateForm.ten))
        quickCreateErrors.ten = "Vui lòng nhập tên cổ giày";
      break;
    case "congNgheDem":
      if (!trimToNull(quickCreateForm.ma))
        quickCreateErrors.ma = "Vui lòng nhập mã công nghệ đệm";
      if (!trimToNull(quickCreateForm.ten))
        quickCreateErrors.ten = "Vui lòng nhập tên công nghệ đệm";
      break;
    case "trongLuong":
      if (!trimToNull(quickCreateForm.ma))
        quickCreateErrors.ma = "Vui lòng nhập mã trọng lượng";
      if (
        !Number.isInteger(Number(quickCreateForm.giaTri)) ||
        Number(quickCreateForm.giaTri) < 1
      ) {
        quickCreateErrors.giaTri = "Trọng lượng phải từ 1 gram trở lên";
      }
      break;
    case "mauSac":
      if (!trimToNull(quickCreateForm.ma))
        quickCreateErrors.ma = "Vui lòng nhập mã màu sắc";
      if (!trimToNull(quickCreateForm.ten))
        quickCreateErrors.ten = "Vui lòng nhập tên màu sắc";
      delete quickCreateErrors.ma;
      quickCreateForm.ma =
        trimToNull(quickCreateForm.ma) ||
        generateColorAttributeCode(
          quickCreateForm.ten,
          quickCreateColorSeed.value,
        );
      if (!trimToNull(quickCreateForm.ma))
        quickCreateErrors.ma = "Vui lòng nhập mã màu sắc";
      quickCreateForm.maMauHex = isValidHexColor(quickCreateForm.maMauHex)
        ? String(quickCreateForm.maMauHex).toUpperCase()
        : generateHexColorFromText(quickCreateForm.ten);
      if (!isValidHexColor(quickCreateForm.maMauHex)) {
        quickCreateErrors.maMauHex =
          "Mã HEX màu sắc phải theo dạng #RRGGBB, ví dụ #FF5733";
      }
      break;
    case "kichCo":
      if (!trimToNull(quickCreateForm.giaTri))
        quickCreateErrors.giaTri = "Vui lòng nhập kích cỡ cần thêm";
      break;
  }

  return Object.keys(quickCreateErrors).length === 0;
}

async function handleQuickCreateSave() {
  if (!quickCreateType.value || !validateQuickCreateForm()) return;

  quickCreateSaving.value = true;
  try {
    let created = null;
    const normalizedSize =
      quickCreateType.value === "kichCo"
        ? normalizeSizeValue(quickCreateForm.giaTri)
        : "";
    const resolvedColorCode =
      quickCreateType.value === "mauSac"
        ? trimToNull(quickCreateForm.ma) ||
          generateColorAttributeCode(
            quickCreateForm.ten,
            quickCreateColorSeed.value,
          )
        : null;
    const resolvedHexColor =
      quickCreateType.value === "mauSac"
        ? isValidHexColor(quickCreateForm.maMauHex)
          ? String(quickCreateForm.maMauHex).toUpperCase()
          : generateHexColorFromText(quickCreateForm.ten)
        : null;

    if (quickCreateType.value === "kichCo" && !normalizedSize) {
      throw new Error("Vui lòng nhập kích cỡ theo dạng 42, 40.5 hoặc EU42");
    }

    switch (quickCreateType.value) {
      case "thuongHieu":
        created = await thuongHieuApi.create({
          ma: trimToNull(quickCreateForm.ma),
          ten: trimToNull(quickCreateForm.ten),
          xuatXu: trimToNull(quickCreateForm.xuatXu),
          logoUrl: null,
          website: null,
          moTa: null,
        });
        break;
      case "loaiGiay":
        created = await loaiGiayApi.create({
          ma: trimToNull(quickCreateForm.ma),
          ten: trimToNull(quickCreateForm.ten),
          moTa: null,
        });
        break;
      case "chatLieuGiay":
        created = await chatLieuGiayApi.create({
          ma: trimToNull(quickCreateForm.ma),
          ten: trimToNull(quickCreateForm.ten),
          moTa: null,
        });
        break;
      case "deGiay":
        created = await deGiayApi.create({
          ma: trimToNull(quickCreateForm.ma),
          ten: trimToNull(quickCreateForm.ten),
          moTa: null,
        });
        break;
      case "coGiay":
        created = await coGiayApi.create({
          ma: trimToNull(quickCreateForm.ma),
          ten: trimToNull(quickCreateForm.ten),
          moTa: null,
        });
        break;
      case "congNgheDem":
        created = await congNgheDemApi.create({
          ma: trimToNull(quickCreateForm.ma),
          ten: trimToNull(quickCreateForm.ten),
          moTa: null,
        });
        break;
      case "trongLuong":
        created = await trongLuongApi.create({
          ma: trimToNull(quickCreateForm.ma),
          giaTri: Number(quickCreateForm.giaTri),
          moTa: null,
        });
        break;
      case "mauSac":
        created = await mauSacApi.create({
          ma: resolvedColorCode,
          ten: trimToNull(quickCreateForm.ten),
          maMauHex: resolvedHexColor,
        });
        break;
      case "kichCo":
        created = await kichCoApi.create({
          giaTri: normalizedSize,
          ghiChu: trimToNull(quickCreateForm.ghiChu),
        });
        break;
    }

    if (!created?.id) {
      throw new Error("Không nhận được dữ liệu thuộc tính vừa tạo");
    }

    pinQuickCreatedAttribute(quickCreateType.value, created.id);
    await loadDanhMuc();
    assignQuickCreatedValue(quickCreateType.value, created.id);
    showToast("Đã thêm thuộc tính mới vào form");
    closeQuickCreate();
  } catch (error) {
    const fieldErrors = getFieldErrors(error);
    Object.assign(quickCreateErrors, fieldErrors);
    if (!Object.keys(fieldErrors).length) {
      showToast(
        getDisplayErrorMessage(error, "Không thể thêm nhanh thuộc tính"),
        "error",
      );
    }
  } finally {
    quickCreateSaving.value = false;
  }
}

function showToast(message, type = "success") {
  if (toastTimer) {
    clearTimeout(toastTimer);
  }

  toast.message = message;
  toast.type = type;
  toast.show = true;
  toastTimer = setTimeout(() => {
    toast.show = false;
    toastTimer = null;
  }, 3000);
}

const selectedMauSacItems = computed(() =>
  (danhMuc.value?.mauSac || []).filter((item) =>
    variantBuilder.mauSacIds.includes(item.id),
  ),
);

const selectedKichCoItems = computed(() =>
  (danhMuc.value?.kichCo || []).filter((item) =>
    variantBuilder.kichCoIds.includes(item.id),
  ),
);

const representativeCreatedVariants = computed(() => {
  const groupedVariants = new Map();

  createdVariants.value.forEach((item) => {
    const colorKey = Number(item.mauSacId || 0) || item.mauSac || item.id;

    if (!groupedVariants.has(colorKey)) {
      groupedVariants.set(colorKey, item);
    }
  });

  return Array.from(groupedVariants.values());
});

const representativeGeneratedVariants = computed(() => {
  const groupedVariants = new Map();

  generatedVariants.value.forEach((item) => {
    const colorKey = Number(item.mauSacId || 0) || item.mauSac || item.key;

    if (!groupedVariants.has(colorKey)) {
      groupedVariants.set(colorKey, item);
    }
  });

  return Array.from(groupedVariants.values());
});


const createdVariantsByColor = computed(() => {
  const groupedVariants = new Map();

  createdVariants.value.forEach((item) => {
    const colorKey = Number(item.mauSacId || 0) || item.mauSac || item.id;
    const existingGroup = groupedVariants.get(colorKey) || [];
    groupedVariants.set(colorKey, [...existingGroup, item]);
  });

  return groupedVariants;
});

const filteredMauSacItems = computed(() => {
  const keyword = mauSacSearch.value.trim().toLowerCase();
  const items = danhMuc.value?.mauSac || [];
  if (!keyword) return items;

  return items.filter((item) =>
    `${item.ten || ""} ${item.maMauHex || ""}`.toLowerCase().includes(keyword),
  );
});

const filteredKichCoItems = computed(() => {
  const keyword = kichCoSearch.value.trim().toLowerCase();
  const items = danhMuc.value?.kichCo || [];
  if (!keyword) return items;

  return items.filter((item) =>
    `${item.giaTri || ""} ${item.ghiChu || ""}`.toLowerCase().includes(keyword),
  );
});

const mauSacSummary = computed(() => {
  if (!selectedMauSacItems.value.length) return "Chọn màu sắc";
  return selectedMauSacItems.value.map((item) => item.ten).join(", ");
});

const kichCoSummary = computed(() => {
  if (!selectedKichCoItems.value.length) return "Chọn kích cỡ";
  return selectedKichCoItems.value
    .map((item) => `Size ${item.giaTri}`)
    .join(", ");
});

function parsePositiveNumber(value) {
  const normalized = Array.isArray(value) ? value[0] : value;
  const parsed = Number(normalized);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
}

function formatCurrency(value) {
  return Number(value || 0).toLocaleString("vi-VN");
}

function normalizeNullableNumber(value) {
  return value == null || value === "" ? null : Number(value);
}

function clearProductErrors() {
  Object.keys(productErrors).forEach((key) => delete productErrors[key]);
}

function clearVariantErrors() {
  Object.keys(variantErrors).forEach((key) => delete variantErrors[key]);
}

function parseVariantNumber(value) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function assignVariantDefaultFieldErrors() {
  delete variantErrors.soLuong;
  delete variantErrors.giaGoc;
  delete variantErrors.giaBan;

  if (parseVariantNumber(variantBuilder.soLuong) < 0) {
    variantErrors.soLuong = "Số lượng mặc định không được âm";
  }

  if (parseVariantNumber(variantBuilder.giaGoc) < 0) {
    variantErrors.giaGoc = "Giá gốc mặc định không được âm";
  }

  if (parseVariantNumber(variantBuilder.giaBan) < 0) {
    variantErrors.giaBan = "Giá bán mặc định không được âm";
  }
}

function resetVariantBuilder() {
  variantBuilder.mauSacIds = [];
  variantBuilder.kichCoIds = [];
  variantBuilder.soLuong = 0;
  variantBuilder.giaGoc = 0;
  variantBuilder.giaBan = 0;
  generatedVariants.value = [];
  draftColorImages.value = {};
  openVariantDropdown.value = null;
  mauSacSearch.value = "";
  kichCoSearch.value = "";
  clearVariantErrors();
}

function resetProductForm() {
  productForm.ten = "";
  productForm.thuongHieuId = null;
  productForm.loaiGiayId = null;
  productForm.gioiTinh = null;
  productForm.chatLieuGiayId = null;
  productForm.moTa = "";
  productForm.deGiayId = null;
  productForm.coGiayId = null;
  productForm.congNgheDemId = null;
  productForm.trongLuongId = null;
  clearProductErrors();
}

function findChatLieuGiayIdByName(name) {
  if (!name || !danhMuc.value?.chatLieuGiay?.length) return null;
  const normalized = String(name).trim().toLowerCase();
  return (
    danhMuc.value.chatLieuGiay.find(
      (item) => item.ten?.trim().toLowerCase() === normalized,
    )?.id || null
  );
}

function hydrateProductForm(detail) {
  productForm.ten = detail.ten || "";
  productForm.thuongHieuId = detail.thuongHieuId || null;
  productForm.loaiGiayId = detail.loaiGiayId || null;
  productForm.gioiTinh = detail.gioiTinh ?? null;
  productForm.chatLieuGiayId =
    detail.thuocTinh?.chatLieuGiayId ||
    findChatLieuGiayIdByName(detail.chatLieu);
  productForm.moTa = detail.moTa || "";
  productForm.deGiayId = detail.thuocTinh?.deGiayId || null;
  productForm.coGiayId = detail.thuocTinh?.coGiayId || null;
  productForm.congNgheDemId = detail.thuocTinh?.congNgheDemId || null;
  productForm.trongLuongId = detail.thuocTinh?.trongLuongId || null;
}

function mauSacLabel(id) {
  return (
    danhMuc.value?.mauSac?.find((item) => item.id === Number(id))?.ten ||
    `Màu #${id}`
  );
}

function kichCoLabel(id) {
  return (
    danhMuc.value?.kichCo?.find((item) => item.id === Number(id))?.giaTri ||
    `Size #${id}`
  );
}

function validateProductForm() {
  clearProductErrors();
  if (!productForm.ten.trim()) productErrors.ten = "Vui lòng nhập tên sản phẩm";
  if (!productForm.thuongHieuId)
    productErrors.thuongHieuId = "Vui lòng chọn thương hiệu cho sản phẩm";
  if (!productForm.loaiGiayId)
    productErrors.loaiGiayId = "Vui lòng chọn loại giày cho sản phẩm";
  return Object.keys(productErrors).length === 0;
}

function validateVariantBuilder() {
  clearVariantErrors();
  if (!variantBuilder.mauSacIds.length)
    variantErrors.mauSacIds =
      "Vui lòng chọn ít nhất một màu sắc để tạo biến thể";
  if (!variantBuilder.kichCoIds.length)
    variantErrors.kichCoIds =
      "Vui lòng chọn ít nhất một kích cỡ để tạo biến thể";
  assignVariantDefaultFieldErrors();
  return Object.keys(variantErrors).length === 0;
}

function generateVariants() {
  if (!validateVariantBuilder()) return;

  const existingMap = new Map(
    generatedVariants.value.map((item) => [
      `${item.mauSacId}-${item.kichCoId}`,
      item,
    ]),
  );

  generatedVariants.value = variantBuilder.mauSacIds.flatMap((mauSacId) =>
    variantBuilder.kichCoIds.map((kichCoId) => {
      const key = `${mauSacId}-${kichCoId}`;
      return (
        existingMap.get(key) || {
          key,
          mauSacId: Number(mauSacId),
          mauSac: mauSacLabel(mauSacId),
          kichCoId: Number(kichCoId),
          kichCo: kichCoLabel(kichCoId),
          soLuong: Number(variantBuilder.soLuong),
          giaGoc: Number(variantBuilder.giaGoc),
          giaBan: Number(variantBuilder.giaBan),
        }
      );
    }),
  );

  delete variantErrors.generated;
  return showToast(`Đã tạo thành công ${generatedVariants.value.length} chi tiết sản phẩm`);
}

function removeGeneratedVariant(key) {
  generatedVariants.value = generatedVariants.value.filter(
    (item) => item.key !== key,
  );
}

function toggleVariantDropdown(type) {
  openVariantDropdown.value = openVariantDropdown.value === type ? null : type;
}

function closeVariantDropdown() {
  openVariantDropdown.value = null;
}

function handleDocumentClick(event) {
  const target = event.target;

  if (
    mauSacDropdownRef.value?.contains(target) ||
    kichCoDropdownRef.value?.contains(target)
  ) {
    return;
  }

  closeVariantDropdown();
}

function toggleSelectedValue(field, id) {
  const numericId = Number(id);
  const currentValues = Array.isArray(variantBuilder[field])
    ? variantBuilder[field]
    : [];

  if (currentValues.includes(numericId)) {
    variantBuilder[field] = currentValues.filter((item) => item !== numericId);
    return;
  }

  variantBuilder[field] = [...currentValues, numericId];
}

function isSelected(field, id) {
  return (
    Array.isArray(variantBuilder[field]) &&
    variantBuilder[field].includes(Number(id))
  );
}

function clearSelectedValues(field) {
  variantBuilder[field] = [];
}

function applyGeneratedDefaults() {
  assignVariantDefaultFieldErrors();
  if (
    variantErrors.soLuong ||
    variantErrors.giaGoc ||
    variantErrors.giaBan
  ) {
    variantErrors.generated =
      "Vui lòng sửa các giá trị mặc định đang bị âm trước khi áp dụng";
    return;
  }

  delete variantErrors.generated;
  generatedVariants.value.forEach((item) => {
    item.soLuong = Number(variantBuilder.soLuong || 0);
    item.giaGoc = Number(variantBuilder.giaGoc || 0);
    item.giaBan = Number(variantBuilder.giaBan || 0);
  });
}

function syncDraftColorImagesWithGeneratedVariants() {
  const validColorKeys = new Set(
    generatedVariants.value.map((item) => String(item.mauSacId)),
  );

  draftColorImages.value = Object.fromEntries(
    Array.from(validColorKeys).map((key) => [
      key,
      draftColorImages.value[key] || [],
    ]),
  );
}


function relatedCreatedVariants(mauSacId) {
  return createdVariantsByColor.value.get(Number(mauSacId) || mauSacId) || [];
}

function setCreatedImageManagerRef(mauSacId, instance) {
  const colorKey = String(mauSacId);
  if (instance) {
    createdImageManagerRefs.value[colorKey] = instance;
    return;
  }

  delete createdImageManagerRefs.value[colorKey];
}


function updateDraftImagesForColor(mauSacId, nextImages) {
  draftColorImages.value = {
    ...draftColorImages.value,
    [String(mauSacId)]: nextImages,
  };
}

function clearRedirectTimer() {
  if (!redirectTimer) {
    return;
  }

  clearTimeout(redirectTimer);
  redirectTimer = null;
}

function navigateToVariantScreen(giayId = currentProductId.value, chiTietId = null) {
  if (giayId) {
    const query = { giayId: String(giayId) };

    if (chiTietId) {
      query.chiTietId = String(chiTietId);
    }

    router.push({
      name: "admin-bien-the-san-pham",
      query,
    });
    return;
  }

  router.push({ name: "admin-san-pham" });
}

function closeRedirectPopup() {
  clearRedirectTimer();
  redirectPopup.show = false;
}

function scheduleVariantRedirect({
  giayId = currentProductId.value,
  chiTietId = null,
  title = "Lưu ảnh thành công",
  message = "Đang chuyển sang màn biến thể sản phẩm.",
} = {}) {
  clearRedirectTimer();
  showCreatedImagesModal.value = false;
  redirectPopup.show = true;
  redirectPopup.title = title;
  redirectPopup.message = message;
  redirectPopup.giayId = giayId;
  redirectPopup.chiTietId = chiTietId;
  redirectTimer = setTimeout(() => {
    closeRedirectPopup();
    navigateToVariantScreen(giayId, chiTietId);
  }, 1400);
}

function handleRedirectNow() {
  const targetGiayId = redirectPopup.giayId;
  const targetChiTietId = redirectPopup.chiTietId;
  closeRedirectPopup();
  navigateToVariantScreen(targetGiayId, targetChiTietId);
}

function handleCreatedImageSaved(variant) {
  scheduleVariantRedirect({
    giayId: currentProductId.value,
    chiTietId: variant?.id ?? null,
    message: "Ảnh chi tiết sản phẩm đã được lưu. Đang chuyển sang màn biến thể sản phẩm.",
  });
}

function validateGeneratedVariants() {
  delete variantErrors.generated;
  assignVariantDefaultFieldErrors();

  if (
    variantErrors.soLuong ||
    variantErrors.giaGoc ||
    variantErrors.giaBan
  ) {
    variantErrors.generated =
      "Vui lòng sửa các giá trị mặc định đang bị âm trước khi lưu";
    return false;
  }

  if (!generatedVariants.value.length) {
    variantErrors.generated =
      "Bạn chưa tạo danh sách chi tiết sản phẩm tự động";
    return false;
  }

  const hasInvalid = generatedVariants.value.some(
    (item) =>
      Number(item.soLuong) < 0 ||
      Number(item.giaGoc) < 0 ||
      Number(item.giaBan) <= 0,
  );

  if (hasInvalid) {
    variantErrors.generated =
      "Vui lòng kiểm tra lại số lượng tồn, giá gốc và giá bán của từng chi tiết sản phẩm";
    return false;
  }

  return true;
}

async function syncDraftImagesToCreatedVariants(variants) {
  const colorVariantsMap = variants.reduce((map, item) => {
    const colorKey = String(item.mauSacId);
    map[colorKey] = [...(map[colorKey] || []), item];
    return map;
  }, {});

  let syncedImageCount = 0;

  for (const [colorKey, images] of Object.entries(draftColorImages.value)) {
    if (!Array.isArray(images) || !images.length) {
      continue;
    }

    const targetVariants = colorVariantsMap[colorKey] || [];
    if (!targetVariants.length) {
      continue;
    }

    const orderedImages = [...images].sort(
      (left, right) =>
        Number(Boolean(right.laHinhChinh)) - Number(Boolean(left.laHinhChinh)),
    );

    for (const variant of targetVariants) {
      for (const image of orderedImages) {
        const created = await api.themHinhAnh(variant.id, {
          url: image.url.trim(),
          loaiHinh: image.laHinhChinh ? 1 : 2,
          moTa: image.moTa?.trim() || undefined,
        });

        if (image.laHinhChinh && !created.laHinhChinh) {
          await api.datHinhChinh(created.id);
        }

        syncedImageCount += 1;
      }
    }
  }

  return syncedImageCount;
}

function buildCreateProductPayload() {
  return {
    ten: productForm.ten.trim(),
    thuongHieuId: Number(productForm.thuongHieuId),
    loaiGiayId: Number(productForm.loaiGiayId),
    gioiTinh: normalizeNullableNumber(productForm.gioiTinh),
    chatLieuGiayId: normalizeNullableNumber(productForm.chatLieuGiayId),
    moTa: productForm.moTa.trim() || undefined,
    deGiayId: normalizeNullableNumber(productForm.deGiayId),
    coGiayId: normalizeNullableNumber(productForm.coGiayId),
    congNgheDemId: normalizeNullableNumber(productForm.congNgheDemId),
    trongLuongId: normalizeNullableNumber(productForm.trongLuongId),
  };
}

function buildGeneratedVariantPayload() {
  return generatedVariants.value.map((item) => ({
    mauSacId: Number(item.mauSacId),
    kichCoId: Number(item.kichCoId),
    soLuong: Number(item.soLuong),
    giaGoc: Number(item.giaGoc),
    giaBan: Number(item.giaBan),
  }));
}

async function loadDanhMuc() {
  const categories = await api.layDanhMuc();

  danhMuc.value = {
    ...categories,
    thuongHieu: sortItemsByPriority(
      categories.thuongHieu,
      quickCreatedPriority.thuongHieu,
    ),
    loaiGiay: sortItemsByPriority(
      categories.loaiGiay,
      quickCreatedPriority.loaiGiay,
    ),
    chatLieuGiay: sortItemsByPriority(
      categories.chatLieuGiay,
      quickCreatedPriority.chatLieuGiay,
    ),
    deGiay: sortItemsByPriority(categories.deGiay, quickCreatedPriority.deGiay),
    coGiay: sortItemsByPriority(categories.coGiay, quickCreatedPriority.coGiay),
    congNgheDem: sortItemsByPriority(
      categories.congNgheDem,
      quickCreatedPriority.congNgheDem,
    ),
    trongLuong: sortItemsByPriority(
      categories.trongLuong,
      quickCreatedPriority.trongLuong,
    ),
    mauSac: sortItemsByPriority(categories.mauSac, quickCreatedPriority.mauSac),
    kichCo: sortItemsByPriority(categories.kichCo, quickCreatedPriority.kichCo),
  };
}

async function loadCurrentProduct() {
  const giayId = parsePositiveNumber(route.query.giayId);

  if (!giayId) {
    currentProductId.value = null;
    currentProduct.value = null;
    createdVariants.value = [];
    resetProductForm();
    resetVariantBuilder();
    showCreatedImagesModal.value = false;
    return;
  }

  if (
    currentProduct.value?.id === giayId &&
    currentProductId.value === giayId
  ) {
    return;
  }

  currentProductId.value = giayId;
  createdVariants.value = [];
  const detail = await api.chiTietGiay(giayId);
  currentProduct.value = detail;
  hydrateProductForm(detail);
  resetVariantBuilder();
}

async function loadInitialData() {
  loadingInit.value = true;
  try {
    if (!danhMuc.value) {
      await loadDanhMuc();
    }
    await loadCurrentProduct();
  } catch (error) {
    showToast(
      getDisplayErrorMessage(error, "Không tải được dữ liệu khởi tạo"),
      "error",
    );
  } finally {
    loadingInit.value = false;
  }
}

async function handleSave() {
  if (!validateProductForm() || !validateGeneratedVariants()) return;

  saving.value = true;
  try {
    let giayId = currentProductId.value;

    if (giayId) {
      currentProduct.value = await api.capNhatGiay(
        giayId,
        buildCreateProductPayload(),
      );
    }

    const response = await api.taoChiTietSanPhamHangLoat(
      giayId
        ? {
            giayId,
            bienThes: buildGeneratedVariantPayload(),
          }
        : {
            ...buildCreateProductPayload(),
            bienThes: buildGeneratedVariantPayload(),
          },
    );

    currentProduct.value = response.giay;
    currentProductId.value = response.giay.id;
    createdVariants.value = response.bienThes || [];

    let syncedImageCount = 0;
    let imageSyncError = null;

    try {
      syncedImageCount = await syncDraftImagesToCreatedVariants(
        createdVariants.value,
      );
    } catch (error) {
      imageSyncError = error;
    }

    resetVariantBuilder();

    await router.replace({
      name: "admin-chi-tiet-san-pham-new",
      query: { giayId: String(response.giay.id) },
    });

    if (imageSyncError) {
      showCreatedImagesModal.value =
        representativeCreatedVariants.value.length > 0;
      showToast(
        getDisplayErrorMessage(
          imageSyncError,
          "Đã lưu sản phẩm và CTSP nhưng chưa đồng bộ hết ảnh theo màu",
        ),
        "error",
      );
      return;
    }

    if (syncedImageCount) {
      showToast(
        `Lưu sản phẩm, CTSP và ${syncedImageCount} ảnh theo màu thành công`,
      );
      return;
    }

    showToast("Lưu sản phẩm và chi tiết sản phẩm thành công");
  } catch (error) {
    const fieldErrors = getFieldErrors(error);
    Object.assign(productErrors, fieldErrors);
    Object.assign(variantErrors, fieldErrors);
    if (fieldErrors.bienThes && !variantErrors.generated) {
      variantErrors.generated = fieldErrors.bienThes;
    }
    if (!Object.keys(fieldErrors).length) {
      showToast(
        getDisplayErrorMessage(error, "Không thể lưu sản phẩm và biến thể"),
        "error",
      );
    }
  } finally {
    saving.value = false;
  }
}

function goBack() {
  if (currentProductId.value) {
    router.push({
      name: "admin-bien-the-san-pham",
      query: { giayId: String(currentProductId.value) },
    });
    return;
  }

  router.push({ name: "admin-san-pham" });
}

async function commitPendingCreatedImages() {
  for (const item of representativeCreatedVariants.value) {
    const manager = createdImageManagerRefs.value[String(item.mauSacId)];
    if (!manager?.commitPendingForm) continue;

    const committed = await manager.commitPendingForm();
    if (!committed) {
      return false;
    }
  }

  return true;
}

async function handleGoBack() {
  const committed = await commitPendingCreatedImages();
  if (!committed) {
    return;
  }

  goBack();
}

watch(
  generatedVariants,
  () => {
    syncDraftColorImagesWithGeneratedVariants();
  },
  { deep: true },
);

watch(
  () => route.query.giayId,
  async () => {
    await loadCurrentProduct();
  },
);

onMounted(async () => {
  document.addEventListener("mousedown", handleDocumentClick);
  await loadInitialData();
});

onBeforeUnmount(() => {
  document.removeEventListener("mousedown", handleDocumentClick);
});
</script>

<template>
  <div class="space-y-5">
    <section
      class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between"
    >
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">
        {{ pageTitle }}
      </h1>

      <button type="button" class="admin-btn-soft" @click="goBack">
        <ArrowLeft :size="16" />
        Quay lại danh sách
      </button>
    </section>

    <section
      v-if="loadingInit"
      class="rounded-[24px] border border-slate-200 bg-white p-10 text-center text-slate-400 shadow-sm"
    >
      Đang tải dữ liệu...
    </section>

    <template v-else>
      <section
        class="grid gap-6 xl:grid-cols-[minmax(0,1.05fr)_minmax(360px,0.95fr)]"
      >
        <article
          class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm"
        >
          <div class="grid gap-4 md:grid-cols-2">
            <label class="block md:col-span-2">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500"
                >Mã</span
              >
              <input
                :value="productCode"
                type="text"
                disabled
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-100 px-4 text-sm text-slate-500"
              />
            </label>

            <label class="block md:col-span-2">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500"
                >Sản phẩm *</span
              >
              <input
                v-model="productForm.ten"
                type="text"
                class="h-11 w-full rounded-2xl border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                :class="
                  productErrors.ten
                    ? 'border-rose-300 bg-rose-50'
                    : 'border-slate-200 bg-slate-50'
                "
                placeholder="Nhập tên sản phẩm..."
              />
              <p v-if="productErrors.ten" class="mt-1 text-xs text-rose-500">
                {{ productErrors.ten }}
              </p>
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500"
                >Thương hiệu *</span
              >
              <AdminSearchableSelect
                :model-value="productForm.thuongHieuId"
                :options="thuongHieuOptions"
                placeholder="Chọn thương hiệu..."
                search-placeholder="Tìm thương hiệu..."
                :error="Boolean(productErrors.thuongHieuId)"
                allow-create
                :creating="inlineCreatingType === 'thuongHieu'"
                @create="handleInlineCreateAttribute('thuongHieu', $event)"
                @update:model-value="productForm.thuongHieuId = $event"
              />
              <p
                v-if="productErrors.thuongHieuId"
                class="mt-1 text-xs text-rose-500"
              >
                {{ productErrors.thuongHieuId }}
              </p>
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500"
                >Loại giày *</span
              >
              <AdminSearchableSelect
                :model-value="productForm.loaiGiayId"
                :options="loaiGiayOptions"
                placeholder="Chọn loại giày..."
                search-placeholder="Tìm loại giày..."
                :error="Boolean(productErrors.loaiGiayId)"
                allow-create
                :creating="inlineCreatingType === 'loaiGiay'"
                @create="handleInlineCreateAttribute('loaiGiay', $event)"
                @update:model-value="productForm.loaiGiayId = $event"
              />
              <p
                v-if="productErrors.loaiGiayId"
                class="mt-1 text-xs text-rose-500"
              >
                {{ productErrors.loaiGiayId }}
              </p>
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500"
                >Giới tính</span
              >
              <AdminSearchableSelect
                :model-value="productForm.gioiTinh"
                :options="genderSearchOptions"
                placeholder="Tất cả"
                search-placeholder="Tìm giới tính..."
                @update:model-value="productForm.gioiTinh = $event"
              />
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500"
                >Chất liệu</span
              >
              <AdminSearchableSelect
                :model-value="productForm.chatLieuGiayId"
                :options="chatLieuOptions"
                placeholder="Chọn chất liệu giày..."
                search-placeholder="Tìm chất liệu..."
                allow-create
                :creating="inlineCreatingType === 'chatLieuGiay'"
                @create="handleInlineCreateAttribute('chatLieuGiay', $event)"
                @update:model-value="productForm.chatLieuGiayId = $event"
              />
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500"
                >Đế giày</span
              >
              <AdminSearchableSelect
                :model-value="productForm.deGiayId"
                :options="deGiayOptions"
                placeholder="Chọn đế giày..."
                search-placeholder="Tìm đế giày..."
                allow-create
                :creating="inlineCreatingType === 'deGiay'"
                @create="handleInlineCreateAttribute('deGiay', $event)"
                @update:model-value="productForm.deGiayId = $event"
              />
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500"
                >Cổ giày</span
              >
              <AdminSearchableSelect
                :model-value="productForm.coGiayId"
                :options="coGiayOptions"
                placeholder="Chọn cổ giày..."
                search-placeholder="Tìm cổ giày..."
                allow-create
                :creating="inlineCreatingType === 'coGiay'"
                @create="handleInlineCreateAttribute('coGiay', $event)"
                @update:model-value="productForm.coGiayId = $event"
              />
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500"
                >Công nghệ đệm</span
              >
              <AdminSearchableSelect
                :model-value="productForm.congNgheDemId"
                :options="congNgheDemOptions"
                placeholder="Chọn công nghệ đệm..."
                search-placeholder="Tìm công nghệ đệm..."
                allow-create
                :creating="inlineCreatingType === 'congNgheDem'"
                @create="handleInlineCreateAttribute('congNgheDem', $event)"
                @update:model-value="productForm.congNgheDemId = $event"
              />
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500"
                >Trọng lượng</span
              >
              <AdminSearchableSelect
                :model-value="productForm.trongLuongId"
                :options="trongLuongOptions"
                placeholder="Chọn trọng lượng..."
                search-placeholder="Tìm trọng lượng..."
                allow-create
                :creating="inlineCreatingType === 'trongLuong'"
                @create="handleInlineCreateAttribute('trongLuong', $event)"
                @update:model-value="productForm.trongLuongId = $event"
              />
            </label>

            <label class="block md:col-span-2">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500"
                >Mô tả</span
              >
              <textarea
                v-model="productForm.moTa"
                rows="4"
                class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                placeholder="Mô tả sản phẩm"
              ></textarea>
            </label>
          </div>
        </article>

        <article
          class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm"
        >
          <div class="space-y-4">
            <div ref="mauSacDropdownRef" class="relative" @click.stop>
              <div class="mb-1 flex items-center justify-between gap-3">
                <label class="block text-[13px] font-semibold text-slate-500"
                  >Màu sắc *</label
                >
                <button
                  type="button"
                  class="inline-flex items-center gap-1 rounded-full border border-rose-200 bg-rose-50 px-2.5 py-1 text-[11px] font-semibold text-rose-600 transition hover:bg-rose-100"
                  @click.stop="openQuickCreate('mauSac', mauSacSearch)"
                >
                  <Plus :size="12" />
                  Thêm nhanh
                </button>
              </div>
              <button
                type="button"
                class="flex min-h-11 w-full items-start justify-between gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm transition hover:bg-white"
                :class="
                  selectedMauSacItems.length
                    ? 'border-rose-300 text-rose-600'
                    : 'text-slate-600'
                "
                @click="toggleVariantDropdown('mauSac')"
              >
                <span class="min-w-0 flex-1 text-left leading-5 whitespace-normal break-words">
                  {{ mauSacSummary }}
                </span>
                <ChevronDown :size="16" class="mt-0.5 shrink-0" />
              </button>

              <div
                v-if="openVariantDropdown === 'mauSac'"
                class="absolute left-0 top-full z-20 mt-2 w-full overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-xl"
              >
                <div class="border-b border-slate-100 p-3">
                  <div class="relative">
                    <Search
                      class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
                    />
                    <input
                      v-model="mauSacSearch"
                      type="text"
                      placeholder="Tìm màu sắc..."
                      class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-10 pr-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
                      @keydown.stop
                    />
                  </div>
                </div>

                <div class="max-h-64 overflow-y-auto p-2">
                  <button
                    v-if="selectedMauSacItems.length"
                    type="button"
                    class="mb-1 flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-sm text-slate-500 transition hover:bg-slate-50"
                    @click="clearSelectedValues('mauSacIds')"
                  >
                    <span>Bỏ chọn tất cả</span>
                    <X :size="15" />
                  </button>

                  <button
                    v-for="item in filteredMauSacItems"
                    :key="item.id"
                    type="button"
                    class="flex w-full items-center justify-between gap-3 rounded-xl px-3 py-2 text-left text-sm transition hover:bg-slate-50"
                    :class="
                      isSelected('mauSacIds', item.id)
                        ? 'bg-rose-50 text-rose-600'
                        : 'text-slate-700'
                    "
                    @click="toggleSelectedValue('mauSacIds', item.id)"
                  >
                    <div class="flex min-w-0 items-center gap-2">
                      <span
                        class="h-3 w-3 shrink-0 rounded-full border border-black/5"
                        :style="{ backgroundColor: item.maMauHex || '#e2e8f0' }"
                      ></span>
                      <span class="truncate">{{ item.ten }}</span>
                    </div>
                    <Check
                      v-if="isSelected('mauSacIds', item.id)"
                      :size="15"
                      class="shrink-0"
                    />
                  </button>

                  <div
                    v-if="!filteredMauSacItems.length"
                    class="rounded-xl px-3 py-6 text-center text-sm text-slate-400"
                  >
                    Không tìm thấy màu sắc phù hợp.
                  </div>
                </div>
              </div>

              <p class="mt-1 text-xs text-slate-400">
                {{
                  selectedMauSacItems.length
                    ? selectedMauSacItems.map((item) => item.ten).join(", ")
                    : "Chưa chọn màu sắc"
                }}
              </p>
              <p
                v-if="variantErrors.mauSacIds"
                class="mt-1 text-xs text-rose-500"
              >
                {{ variantErrors.mauSacIds }}
              </p>
            </div>

            <div ref="kichCoDropdownRef" class="relative" @click.stop>
              <div class="mb-1 flex items-center justify-between gap-3">
                <label class="block text-[13px] font-semibold text-slate-500"
                  >Kích cỡ *</label
                >
                <button
                  type="button"
                  class="inline-flex items-center gap-1 rounded-full border border-rose-200 bg-rose-50 px-2.5 py-1 text-[11px] font-semibold text-rose-600 transition hover:bg-rose-100"
                  @click.stop="openQuickCreate('kichCo', kichCoSearch)"
                >
                  <Plus :size="12" />
                  Thêm nhanh
                </button>
              </div>
              <button
                type="button"
                class="flex min-h-11 w-full items-start justify-between gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm transition hover:bg-white"
                :class="
                  selectedKichCoItems.length
                    ? 'border-rose-300 text-rose-600'
                    : 'text-slate-600'
                "
                @click="toggleVariantDropdown('kichCo')"
              >
                <span class="min-w-0 flex-1 text-left leading-5 whitespace-normal break-words">
                  {{ kichCoSummary }}
                </span>
                <ChevronDown :size="16" class="mt-0.5 shrink-0" />
              </button>

              <div
                v-if="openVariantDropdown === 'kichCo'"
                class="absolute left-0 top-full z-20 mt-2 w-full overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-xl"
              >
                <div class="border-b border-slate-100 p-3">
                  <div class="relative">
                    <Search
                      class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
                    />
                    <input
                      v-model="kichCoSearch"
                      type="text"
                      placeholder="Tìm kích cỡ..."
                      class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-10 pr-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
                      @keydown.stop
                    />
                  </div>
                </div>

                <div class="max-h-64 overflow-y-auto p-2">
                  <button
                    v-if="selectedKichCoItems.length"
                    type="button"
                    class="mb-1 flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-sm text-slate-500 transition hover:bg-slate-50"
                    @click="clearSelectedValues('kichCoIds')"
                  >
                    <span>Bỏ chọn tất cả</span>
                    <X :size="15" />
                  </button>

                  <button
                    v-for="item in filteredKichCoItems"
                    :key="item.id"
                    type="button"
                    class="flex w-full items-center justify-between gap-3 rounded-xl px-3 py-2 text-left text-sm transition hover:bg-slate-50"
                    :class="
                      isSelected('kichCoIds', item.id)
                        ? 'bg-rose-50 text-rose-600'
                        : 'text-slate-700'
                    "
                    @click="toggleSelectedValue('kichCoIds', item.id)"
                  >
                    <span class="truncate">Size {{ item.giaTri }}</span>
                    <Check
                      v-if="isSelected('kichCoIds', item.id)"
                      :size="15"
                      class="shrink-0"
                    />
                  </button>

                  <div
                    v-if="!filteredKichCoItems.length"
                    class="rounded-xl px-3 py-6 text-center text-sm text-slate-400"
                  >
                    Không tìm thấy kích cỡ phù hợp.
                  </div>
                </div>
              </div>

              <p class="mt-1 text-xs text-slate-400">
                {{
                  selectedKichCoItems.length
                    ? selectedKichCoItems
                        .map((item) => `Size ${item.giaTri}`)
                        .join(", ")
                    : "Chưa chọn kích cỡ"
                }}
              </p>
              <p
                v-if="variantErrors.kichCoIds"
                class="mt-1 text-xs text-rose-500"
              >
                {{ variantErrors.kichCoIds }}
              </p>
            </div>

            <button
              type="button"
              class="admin-btn-primary w-full"
              @click="generateVariants"
            >
              Tạo biến thể tự động
            </button>
          </div>
        </article>
      </section>

      <ChiTietSanPhamGeneratedVariantsSection
        :generated-variants="generatedVariants"
        :representative-generated-variants="representativeGeneratedVariants"
        :variant-builder="variantBuilder"
        :variant-errors="variantErrors"
        :draft-color-images="draftColorImages"
        :saving="saving"
        :is-existing-product="isExistingProduct"
        @apply-defaults="applyGeneratedDefaults"
        @remove-generated-variant="removeGeneratedVariant"
        @save="handleSave"
        @change-draft-images="
          updateDraftImagesForColor($event.mauSacId, $event.images)
        "
        @error="showToast($event, 'error')"
      />

      <section
        v-if="representativeCreatedVariants.length"
        class="rounded-[24px] border border-emerald-100 bg-emerald-50/60 p-5 shadow-sm"
      >
        <div
          class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between"
        >
          <div>
            <div
              class="inline-flex items-center gap-2 rounded-full bg-white px-3 py-1 text-sm font-semibold text-emerald-700 shadow-sm"
            >
              <CheckCircle2 :size="16" />
              Lưu chi tiết sản phẩm thành công
            </div>
            <h2 class="mt-3 text-2xl font-black text-slate-900">
              Thêm ảnh theo màu đại diện
            </h2>
            <p class="mt-2 text-sm text-slate-600">
              Mỗi màu chỉ hiển thị một biến thể đại diện để bạn thêm ảnh nhanh
              cho sản phẩm. Bạn đang có
              {{ representativeCreatedVariants.length }} màu cần bổ sung ảnh.
            </p>
          </div>

          <button type="button" class="admin-btn-soft" @click="handleGoBack">
            <ArrowLeft :size="16" />
            Hoàn tất và quay lại danh sách
          </button>
        </div>

        <div class="mt-6 grid gap-5">
          <BienTheImageManager
            v-for="item in representativeCreatedVariants"
            :key="item.id"
            :ref="(instance) => setCreatedImageManagerRef(item.mauSacId, instance)"
            :variant="item"
            :related-variants="relatedCreatedVariants(item.mauSacId)"
            display-mode="color"
            @updated="showToast('Cập nhật ảnh thành công')"
            @error="showToast($event, 'error')"
          />
        </div>
      </section>
    </template>

    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="quickCreateOpen && quickCreateDefinition"
          class="fixed inset-0 z-[90] flex items-center justify-center bg-slate-950/45 p-4"
          @click.self="closeQuickCreate"
        >
          <div
            class="w-full max-w-xl overflow-hidden rounded-[28px] bg-white shadow-2xl"
          >
            <div
              class="flex items-start justify-between gap-4 border-b border-slate-100 px-6 py-5"
            >
              <div>
                <h2 class="text-xl font-black text-slate-900">
                  {{ quickCreateDefinition.title }}
                </h2>
                <p class="mt-1 text-sm text-slate-500">
                  {{ quickCreateDefinition.description }}
                </p>
              </div>

              <button
                type="button"
                class="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-slate-100 text-slate-500 transition hover:bg-slate-200"
                @click="closeQuickCreate"
              >
                <X :size="16" />
              </button>
            </div>

            <div class="p-6">
              <div class="grid gap-4 md:grid-cols-2">
                <div
                  v-for="field in quickCreateDefinition.fields"
                  :key="field.key"
                  :class="field.type === 'color' ? 'md:col-span-2' : ''"
                >
                  <label
                    class="mb-1 block text-[13px] font-semibold text-slate-500"
                  >
                    {{ field.label }}
                  </label>

                  <div
                    v-if="field.type === 'color'"
                    class="flex items-center gap-3"
                  >
                    <input
                      :value="quickCreateForm[field.key]"
                      type="color"
                      class="h-11 w-16 rounded-2xl border border-slate-200 bg-white p-1"
                      @input="
                        quickCreateForm[field.key] = String(
                          $event.target.value || '',
                        ).toUpperCase()
                      "
                    />
                    <input
                      :value="quickCreateForm[field.key]"
                      type="text"
                      maxlength="7"
                      class="h-11 flex-1 rounded-2xl border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                      :class="
                        quickCreateErrors[field.key]
                          ? 'border-rose-300 bg-rose-50'
                          : 'border-slate-200 bg-slate-50'
                      "
                      placeholder="#000000"
                      @input="
                        quickCreateForm[field.key] = String(
                          $event.target.value || '',
                        ).toUpperCase()
                      "
                    />
                  </div>

                  <input
                    v-else
                    :value="quickCreateForm[field.key]"
                    :type="field.type || 'text'"
                    :min="field.min"
                    class="h-11 w-full rounded-2xl border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                    :class="[
                      quickCreateErrors[field.key]
                        ? 'border-rose-300 bg-rose-50'
                        : 'border-slate-200 bg-slate-50',
                      field.uppercase ? 'uppercase' : '',
                    ]"
                    :placeholder="field.placeholder"
                    @input="
                      quickCreateForm[field.key] = field.uppercase
                        ? String($event.target.value || '').toUpperCase()
                        : $event.target.value
                    "
                  />

                  <p
                    v-if="quickCreateErrors[field.key]"
                    class="mt-1 text-xs text-rose-500"
                  >
                    {{ quickCreateErrors[field.key] }}
                  </p>
                </div>
              </div>
            </div>

            <div
              class="flex items-center justify-end gap-3 border-t border-slate-100 px-6 py-4"
            >
              <button
                type="button"
                class="admin-btn-soft"
                @click="closeQuickCreate"
              >
                Hủy
              </button>

              <button
                type="button"
                class="admin-btn-primary disabled:opacity-60"
                :disabled="quickCreateSaving"
                @click="handleQuickCreateSave"
              >
                <Save :size="16" />
                {{ quickCreateSaving ? "Đang thêm..." : "Thêm vào form" }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="toast.show"
          class="fixed right-5 top-5 z-[100] rounded-2xl px-4 py-3 text-sm font-medium text-white shadow-lg"
          :class="toast.type === 'error' ? 'bg-rose-500' : 'bg-emerald-500'"
        >
          {{ toast.message }}
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

