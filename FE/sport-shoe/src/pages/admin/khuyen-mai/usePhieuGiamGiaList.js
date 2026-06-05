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
import { showConfirm, showSuccess, showError } from "../../../utils/alert";

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
  function hienThiThongBao(loai, tieuDe, noiDung = "") {
    if (loai === "success") {
      showSuccess(noiDung || tieuDe, tieuDe);
    } else if (loai === "error") {
      showError(noiDung || tieuDe, tieuDe);
    }
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
    const ngayKT = new Date(ngayKetThuc);
    ngayKT.setHours(0, 0, 0, 0);
    // Hết hạn nếu ngayKetThuc <= homNay (bao gồm cả hôm nay)
    return ngayKT <= homNay;
  }

  function mauTrangThai(trangThai, ngayKetThuc) {
    if (isHetHan(ngayKetThuc)) return "bg-slate-50 text-slate-600 ring-1 ring-slate-200";
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
    if (Number(item?.soLuong || 0) === 999999) return "Vô hạn";
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
      const isFilterDangHoatDong = boLoc.value.trangThai === "1";

      // Cần lấy tất cả dữ liệu khi filter FE
      const needFetchAll = isFilterHetHan || isFilterDangHoatDong;

      const data = await getPhieuGiamGiaList({
        keyword: boLoc.value.keyword || undefined,
        trangThai: (!needFetchAll && boLoc.value.trangThai !== "")
          ? Number(boLoc.value.trangThai)
          : undefined,
        loai: boLoc.value.loai !== "" ? Number(boLoc.value.loai) : undefined,
        tuNgay: boLoc.value.tuNgay || undefined,
        denNgay: boLoc.value.denNgay || undefined,
        pageNo: needFetchAll ? 0 : (trangHienTai.value - 1),
        pageSize: needFetchAll ? 9999 : soPhanTuMotTrang.value,
      });

      let items = data?.content || [];

      // Áp dụng filter FE
      if (isFilterHetHan) {
        // Lọc FE: Phiếu hết hạn khi ngày kết thúc <= hôm nay HOẶC trangThai = 2
        items = items.filter(item =>
          isHetHan(item.ngayKetThuc) || Number(item.trangThai) === 2
        );
      } else if (isFilterDangHoatDong) {
        // Đang hoạt động: Chỉ lấy phiếu chưa hết hạn VÀ có trangThai = 1
        items = items.filter(item =>
          Number(item.trangThai) === 1 && !isHetHan(item.ngayKetThuc)
        );
      }

      // Tính toán phân trang
      if (needFetchAll) {
        // Phân trang FE
        totalItems.value = items.length;
        tongSoTrang.value = Math.max(1, Math.ceil(items.length / soPhanTuMotTrang.value));

        // Đảm bảo trang hiện tại không vượt quá tổng số trang
        if (trangHienTai.value > tongSoTrang.value) {
          trangHienTai.value = tongSoTrang.value;
        }

        const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
        danhSach.value = items.slice(start, start + soPhanTuMotTrang.value);
      } else {
        // Phân trang BE
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
    if (isHetHan(item.ngayKetThuc) || Number(item.trangThai) === 2) {
      hienThiThongBao("warning", "Thao tác bị chặn", "Phiếu đã hết hạn, vui lòng vào chi tiết để gia hạn ngày kết thúc.");
      return;
    }

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
      hienThiThongBao(
        "success",
        "Thành công",
        Number(item.trangThai) === 1 ? "Đã ngừng hoạt động phiếu." : "Đã chuyển phiếu sang đang hoạt động thành công."
      );
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
    if (isHetHan(item.ngayKetThuc) || Number(item.trangThai) === 2) {
      hienThiThongBao("warning", "Thao tác bị chặn", "Phiếu đã hết hạn, không thể thay đổi trạng thái liên kết.");
      return;
    }

    try {
      const nextStatus = 0;
      await updatePhieuGiamGiaKhachHang(item.id, {
        ...item,
        trangThai: nextStatus,
      });
      hienThiThongBao(
        "success",
        "Thành công",
        "Đã dừng áp dụng phiếu giảm giá cho khách hàng này."
      );
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
          showError("Không có dữ liệu để xuất Excel.");
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
            {
              label: "Phát hành",
              value: (row) =>
                Number(row.soLuong || 0) === 999999
                  ? "Vô hạn"
                  : Number(row.soLuong || 0),
            },
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
        showError("Không có dữ liệu để xuất Excel.");
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
          {
            label: "Giá trị giảm",
            value: (row) => formatGiaTri(row.giaTriPhieuGiamGia, row.loaiPhieuGiamGia)
          },
          { label: "Ngày tặng", value: (row) => toDisplayDate(row.ngayTao) },
          { label: "Ngày dùng", value: (row) => toDisplayDate(row.ngaySuDung) },
          { label: "Trạng thái", value: (row) => statusText(row.trangThai) },
        ],
        rows,
      });
    } catch (error) {
      showError(error?.message || "Xuất Excel thất bại.");
    }
  }

  onMounted(() => {
    // Kiểm tra và hiển thị thông báo từ trang Chi tiết nếu có
    const flash = window.sessionStorage.getItem("admin-phieu-giam-gia-toast");
    if (flash) {
      const { loai, tieuDe, noiDung } = JSON.parse(flash);
      hienThiThongBao(loai, tieuDe, noiDung);
      window.sessionStorage.removeItem("admin-phieu-giam-gia-toast");
    }

    if (activeTab.value === "khach-hang") {
      taiDanhSachKh();
      return;
    }

    taiDanhSach();
  });

  return { computed, onMounted, ref, watch, useRoute, useRouter, CheckCircle2, CircleX, Eye, FileSpreadsheet, Filter, Plus, RotateCcw, Search, X, getPhieuGiamGiaKhachHangList, getPhieuGiamGiaList, updatePhieuGiamGia, updatePhieuGiamGiaKhachHang, AdminTableFooter, AdminQuickStatusAction, exportRowsToExcel, getDisplayErrorMessage, router, route, dangTai, loiTrang, resolveActiveTab, activeTab, hienThiThongBao, boLoc, boLocKh, danhSach, tongSoTrang, soPhanTuMotTrang, trangHienTai, totalItems, danhSachKh, tongSoTrangKh, soPhanTuMotTrangKh, trangHienTaiKh, totalItemsKh, dsTrangThai, dsLoai, isHetHan, mauTrangThai, statusText, loaiGiamText, loaiPhieuText, formatGiaTri, formatTien, toDisplayDate, soLuongDaDung, soLuongConLai, timer, taiDanhSach, taiDanhSachKh, lamMoiBoLoc, nhanhDoiTrangThai, nhanhDoiTrangThaiKh, openCreateModal, openEditModal, xuatExcel };
}
