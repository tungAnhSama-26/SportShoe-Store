import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  CheckCircle2,
  CircleX,
  Eye,
  FileSpreadsheet,
  Filter,
  Plus,
  RotateCcw,
  Search,
  X,
} from "lucide-vue-next";
import {
  getPhieuGiamGiaKhachHangList,
  getPhieuGiamGiaList,
  updatePhieuGiamGia,
  updatePhieuGiamGiaKhachHang,
} from "../../../services/khuyen-mai";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import AdminQuickStatusAction from "../../../components/common/AdminQuickStatusAction.vue";
import { exportRowsToExcel } from "../../../utils/export-excel";
import { getDisplayErrorMessage } from "../../../utils/error-message";

export function usePhieuGiamGiaList() {
  const router = useRouter();
  const route = useRoute();
  const dangTai = ref(false);
  const loiTrang = ref("");

  function resolveActiveTab() {
    if (
      route.name === "admin-phieu-giam-gia-khach-hang" ||
      route.query.tab === "khach-hang"
    ) {
      return "khach-hang";
    }

    return "phieu";
  }

  const activeTab = ref(resolveActiveTab());
  const toast = ref({
    hienThi: false,
    loai: "success",
    tieuDe: "",
    noiDung: "",
  });
  let toastTimer = null;

  const toastClass = computed(() => {
    if (toast.value.loai === "success") {
      return "border-emerald-100 bg-emerald-50 text-emerald-700";
    }
    if (toast.value.loai === "warning") {
      return "border-amber-100 bg-amber-50 text-amber-700";
    }
    return "border-rose-100 bg-rose-50 text-rose-700";
  });

  const toastIconClass = computed(() => {
    if (toast.value.loai === "success") {
      return "bg-emerald-100 text-emerald-600";
    }
    if (toast.value.loai === "warning") {
      return "bg-amber-100 text-amber-600";
    }
    return "bg-rose-100 text-rose-600";
  });

  const toastAccentClass = computed(() => {
    if (toast.value.loai === "success") {
      return "bg-emerald-500";
    }
    if (toast.value.loai === "warning") {
      return "bg-amber-500";
    }
    return "bg-rose-500";
  });

  const ToastIcon = computed(() => {
    if (toast.value.loai === "success") {
      return CheckCircle2;
    }
    return CircleX;
  });

  function hienThiThongBao(loai, tieuDe, noiDung = "") {
    if (toastTimer) {
      clearTimeout(toastTimer);
    }

    toast.value = { hienThi: true, loai, tieuDe, noiDung };
    toastTimer = setTimeout(() => {
      toast.value.hienThi = false;
    }, 3200);
  }

  const boLoc = ref({
    keyword: "",
    trangThai: "",
    tuNgay: "",
    denNgay: "",
    loai: "",
  });
  const boLocKh = ref({
    keyword: "",
    trangThai: "",
  });

  const danhSach = ref([]);
  const tongSoTrang = ref(1);
  const soPhanTuMotTrang = ref(5);
  const trangHienTai = ref(1);
  const totalItems = ref(0);

  const danhSachKh = ref([]);
  const tongSoTrangKh = ref(1);
  const soPhanTuMotTrangKh = ref(5);
  const trangHienTaiKh = ref(1);
  const totalItemsKh = ref(0);

  const dsTrangThai = [
    { label: "Tất cả", value: "" },
    { label: "Đang hoạt động", value: "1" },
    { label: "Ngưng hoạt động", value: "0" },
    { label: "Hết hạn", value: "het_han" },
    { label: "Hết số lượng", value: "3" },
    { label: "Sắp diễn ra", value: "4" },
  ];

  const dsLoai = [
    { label: "Tất cả", value: "" },
    { label: "Phần trăm", value: "1" },
    { label: "Tiền mặt", value: "2" },
  ];

  function isHetHan(ngayKetThuc) {
    if (!ngayKetThuc) return false;
    const homNay = new Date();
    homNay.setHours(0, 0, 0, 0);
    return new Date(ngayKetThuc) < homNay;
  }

  function mauTrangThai(trangThai, ngayKetThuc) {
    if (isHetHan(ngayKetThuc)) return "bg-rose-50 text-rose-600 ring-1 ring-rose-100";
    const status = Number(trangThai);
    if (status === 1) return "bg-emerald-50 text-emerald-600 ring-1 ring-emerald-100";
    if (status === 2) return "bg-slate-50 text-slate-600 ring-1 ring-slate-200";
    if (status === 3) return "bg-orange-50 text-orange-600 ring-1 ring-orange-200";
    if (status === 4) return "bg-blue-50 text-blue-600 ring-1 ring-blue-200";
    return "bg-rose-50 text-rose-600 ring-1 ring-rose-100";
  }

  function statusText(value, ngayKetThuc) {
    if (isHetHan(ngayKetThuc)) return "Hết hạn";
    const status = Number(value);
    if (status === 1) return "Đang hoạt động";
    if (status === 2) return "Hết hạn";
    if (status === 3) return "Hết số lượng";
    if (status === 4) return "Sắp diễn ra";
    return "Ngưng hoạt động";
  }

  function loaiGiamText(loai) {
    return Number(loai) === 1 ? "Phần trăm" : "Tiền mặt";
  }

  function loaiPhieuText(loaiPhieu) {
    return Number(loaiPhieu) === 2 ? "Cá nhân" : "Công khai";
  }

  function formatGiaTri(giaTri, loai) {
    if (giaTri == null || giaTri === "") {
      return "0";
    }

    return Number(loai) === 1
      ? `${giaTri}%`
      : `${Number(giaTri).toLocaleString("vi-VN")}đ`;
  }

  function formatTien(tien) {
    if (tien == null || tien === "") {
      return "0đ";
    }

    return `${Number(tien).toLocaleString("vi-VN")}đ`;
  }

  function toDisplayDate(value) {
    if (!value) {
      return "—";
    }

    return new Date(value).toLocaleDateString("vi-VN");
  }

  function soLuongDaDung(item) {
    return Number(item?.soLuongDaDung || 0);
  }

  function soLuongConLai(item) {
    return Math.max(Number(item?.soLuong || 0) - soLuongDaDung(item), 0);
  }

  watch(activeTab, (newTab) => {
    if (newTab === "phieu") {
      trangHienTai.value = 1;
      taiDanhSach();
      return;
    }

    trangHienTaiKh.value = 1;
    taiDanhSachKh();
  });

  watch(soPhanTuMotTrang, () => {
    trangHienTai.value = 1;
    taiDanhSach();
  });

  watch(trangHienTai, taiDanhSach);

  watch(soPhanTuMotTrangKh, () => {
    trangHienTaiKh.value = 1;
    taiDanhSachKh();
  });

  watch(trangHienTaiKh, taiDanhSachKh);

  let timer;
  watch(
    boLoc,
    () => {
      clearTimeout(timer);
      timer = setTimeout(() => {
        trangHienTai.value = 1;
        taiDanhSach();
      }, 300);
    },
    { deep: true },
  );

  watch(
    boLocKh,
    () => {
      clearTimeout(timer);
      timer = setTimeout(() => {
        trangHienTaiKh.value = 1;
        taiDanhSachKh();
      }, 300);
    },
    { deep: true },
  );

  watch(
    () => [route.name, route.query.tab],
    () => {
      const nextTab = resolveActiveTab();
      if (nextTab !== activeTab.value) {
        activeTab.value = nextTab;
      }
    },
  );

  async function taiDanhSach() {
    dangTai.value = true;
    loiTrang.value = "";

    try {
      // "het_han" là filter FE tự xử lý, không gửi lên backend
      const isFilterHetHan = boLoc.value.trangThai === "het_han";

      const data = await getPhieuGiamGiaList({
        keyword: boLoc.value.keyword || undefined,
        trangThai: (!isFilterHetHan && boLoc.value.trangThai !== "")
          ? Number(boLoc.value.trangThai)
          : undefined,
        loai: boLoc.value.loai !== "" ? Number(boLoc.value.loai) : undefined,
        tuNgay: boLoc.value.tuNgay || undefined,
        denNgay: boLoc.value.denNgay || undefined,
        pageNo: trangHienTai.value - 1,
        pageSize: isFilterHetHan ? 1000 : soPhanTuMotTrang.value,
      });

      let items = data?.content || [];

      if (isFilterHetHan) {
        // Lọc FE: chỉ lấy phiếu quá ngày kết thúc
        items = items.filter(item => isHetHan(item.ngayKetThuc));
      } else if (boLoc.value.trangThai === "1") {
        // Đang hoạt động: loại bỏ phiếu đã hết hạn theo ngày
        items = items.filter(item => !isHetHan(item.ngayKetThuc));
      }

      tongSoTrang.value = Math.max(1, Math.ceil(items.length / soPhanTuMotTrang.value));
      totalItems.value = items.length;
      const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
      danhSach.value = isFilterHetHan || boLoc.value.trangThai === "1"
        ? items.slice(start, start + soPhanTuMotTrang.value)
        : items;

      if (!isFilterHetHan && boLoc.value.trangThai !== "1") {
        danhSach.value = items;
        tongSoTrang.value = data?.totalPages || 1;
        totalItems.value = data?.totalElements || 0;
      }
    } catch (error) {
      loiTrang.value = getDisplayErrorMessage(
        error,
        "Không thể tải danh sách phiếu giảm giá",
      );
    } finally {
      dangTai.value = false;
    }
  }

  async function taiDanhSachKh() {
    dangTai.value = true;
    loiTrang.value = "";

    try {
      const data = await getPhieuGiamGiaKhachHangList({
        keyword: boLocKh.value.keyword || undefined,
        trangThai:
          boLocKh.value.trangThai !== ""
            ? Number(boLocKh.value.trangThai)
            : undefined,
        pageNo: trangHienTaiKh.value - 1,
        pageSize: soPhanTuMotTrangKh.value,
      });
      danhSachKh.value = data?.content || [];
      tongSoTrangKh.value = data?.totalPages || 1;
      totalItemsKh.value = data?.totalElements || 0;
    } catch (error) {
      loiTrang.value = getDisplayErrorMessage(
        error,
        "Không thể tải danh sách phiếu khách hàng",
      );
    } finally {
      dangTai.value = false;
    }
  }

  function lamMoiBoLoc() {
    if (activeTab.value === "phieu") {
      boLoc.value = {
        keyword: "",
        trangThai: "",
        tuNgay: "",
        denNgay: "",
        loai: "",
      };
      return;
    }

    boLocKh.value = {
      keyword: "",
      trangThai: "",
    };
  }

  async function nhanhDoiTrangThai(item) {
    if (isHetHan(item.ngayKetThuc)) return;
    try {
      const nextStatus = Number(item.trangThai) === 1 ? 0 : 1;
      await updatePhieuGiamGia(item.id, {
        ma: item.ma,
        ten: item.ten,
        loai: item.loai,
        loaiPhieu: item.loaiPhieu,
        giaTri: item.giaTri,
        giaTriToiThieu: item.giaTriToiThieu || null,
        giamToiDa: item.giamToiDa || null,
        ngayBatDau: item.ngayBatDau,
        ngayKetThuc: item.ngayKetThuc,
        soLuong: item.soLuong,
        soLuongDaDung: item.soLuongDaDung || 0,
        trangThai: nextStatus,
      });
      hienThiThongBao("success", "Cập nhật phiếu thành công");
      await taiDanhSach();
    } catch (error) {
      hienThiThongBao(
        "error",
        "Cập nhật thất bại",
        getDisplayErrorMessage(error, "Không thể thay đổi trạng thái phiếu giảm giá"),
      );
    }
  }

  async function nhanhDoiTrangThaiKh(item) {
    if (Number(item.trangThai) === 0) return;
    try {
      const nextStatus = 0;
      await updatePhieuGiamGiaKhachHang(item.id, {
        ...item,
        trangThai: nextStatus,
      });
      hienThiThongBao("success", "Cập nhật liên kết thành công");
      taiDanhSachKh();
    } catch (error) {
      hienThiThongBao(
        "error",
        "Cập nhật thất bại",
        getDisplayErrorMessage(
          error,
          "Không thể thay đổi trạng thái phiếu khách hàng",
        ),
      );
    }
  }

  function openCreateModal() {
    router.push({
      name:
        activeTab.value === "khach-hang"
          ? "admin-phieu-giam-gia-khach-hang-them"
          : "admin-phieu-giam-gia-them",
    });
  }

  function openEditModal(target, itemArg) {
    const item = typeof target === "string" ? itemArg : target;
    if (!item?.id) {
      return;
    }

    router.push({
      name:
        target === "khach-hang"
          ? "admin-phieu-giam-gia-khach-hang-chi-tiet"
          : "admin-phieu-giam-gia-chi-tiet",
      params: { id: item.id },
    });
  }

  async function xuatExcel() {
    try {
      if (activeTab.value === "phieu") {
        const data = await getPhieuGiamGiaList({
          keyword: boLoc.value.keyword || undefined,
          trangThai:
            boLoc.value.trangThai !== ""
              ? Number(boLoc.value.trangThai)
              : undefined,
          loai: boLoc.value.loai !== "" ? Number(boLoc.value.loai) : undefined,
          tuNgay: boLoc.value.tuNgay || undefined,
          denNgay: boLoc.value.denNgay || undefined,
          pageNo: 0,
          pageSize: 1000,
        });

        const rows = data?.content || [];
        if (!rows.length) {
          window.alert("Không có dữ liệu để xuất Excel.");
          return;
        }

        exportRowsToExcel({
          filename: "quan-ly-phieu-giam-gia",
          sheetName: "PhieuGiamGia",
          columns: [
            { label: "STT", value: (_, index) => index + 1 },
            { label: "Mã", key: "ma" },
            { label: "Tên phiếu", key: "ten" },
            {
              label: "Hình thức phiếu",
              value: (row) => loaiPhieuText(row.loaiPhieu),
            },
            {
              label: "Loại giảm",
              value: (row) => loaiGiamText(row.loai),
            },
            {
              label: "Giá trị giảm",
              value: (row) => formatGiaTri(row.giaTri, row.loai),
            },
            {
              label: "Giá trị đơn tối thiểu",
              value: (row) => formatTien(row.giaTriToiThieu),
            },
            {
              label: "Giảm tối đa",
              value: (row) =>
                Number(row.giamToiDa) > 0
                  ? formatTien(row.giamToiDa)
                  : "Không giới hạn",
            },
            { label: "Phát hành", value: (row) => Number(row.soLuong || 0) },
            { label: "Đã dùng", value: (row) => soLuongDaDung(row) },
            { label: "Còn lại", value: (row) => soLuongConLai(row) },
            {
              label: "Ngày bắt đầu",
              value: (row) => toDisplayDate(row.ngayBatDau),
            },
            {
              label: "Ngày kết thúc",
              value: (row) => toDisplayDate(row.ngayKetThuc),
            },
            { label: "Trạng thái", value: (row) => statusText(row.trangThai) },
          ],
          rows,
        });
        return;
      }

      const data = await getPhieuGiamGiaKhachHangList({
        keyword: boLocKh.value.keyword || undefined,
        trangThai:
          boLocKh.value.trangThai !== ""
            ? Number(boLocKh.value.trangThai)
            : undefined,
        pageNo: 0,
        pageSize: 1000,
      });

      const rows = data?.content || [];
      if (!rows.length) {
        window.alert("Không có dữ liệu để xuất Excel.");
        return;
      }

      exportRowsToExcel({
        filename: "quan-ly-phieu-khach-hang",
        sheetName: "PhieuKhachHang",
        columns: [
          { label: "STT", value: (_, index) => index + 1 },
          { label: "Mã phiếu", key: "maPhieuGiamGia" },
          { label: "Tên phiếu", key: "tenPhieuGiamGia" },
          { label: "Khách hàng", key: "tenKhachHang" },
          { label: "Ngày tặng", value: (row) => toDisplayDate(row.ngayTao) },
          { label: "Ngày dùng", value: (row) => toDisplayDate(row.ngaySuDung) },
          { label: "Trạng thái", value: (row) => statusText(row.trangThai) },
        ],
        rows,
      });
    } catch (error) {
      window.alert(error?.message || "Xuất Excel thất bại.");
    }
  }

  onMounted(() => {
    if (activeTab.value === "khach-hang") {
      taiDanhSachKh();
      return;
    }

    taiDanhSach();
  });

  return { computed, onMounted, ref, watch, useRoute, useRouter, CheckCircle2, CircleX, Eye, FileSpreadsheet, Filter, Plus, RotateCcw, Search, X, getPhieuGiamGiaKhachHangList, getPhieuGiamGiaList, updatePhieuGiamGia, updatePhieuGiamGiaKhachHang, AdminTableFooter, AdminQuickStatusAction, exportRowsToExcel, getDisplayErrorMessage, router, route, dangTai, loiTrang, resolveActiveTab, activeTab, toast, toastTimer, toastClass, toastIconClass, toastAccentClass, ToastIcon, hienThiThongBao, boLoc, boLocKh, danhSach, tongSoTrang, soPhanTuMotTrang, trangHienTai, totalItems, danhSachKh, tongSoTrangKh, soPhanTuMotTrangKh, trangHienTaiKh, totalItemsKh, dsTrangThai, dsLoai, isHetHan, mauTrangThai, statusText, loaiGiamText, loaiPhieuText, formatGiaTri, formatTien, toDisplayDate, soLuongDaDung, soLuongConLai, timer, taiDanhSach, taiDanhSachKh, lamMoiBoLoc, nhanhDoiTrangThai, nhanhDoiTrangThaiKh, openCreateModal, openEditModal, xuatExcel };
}
