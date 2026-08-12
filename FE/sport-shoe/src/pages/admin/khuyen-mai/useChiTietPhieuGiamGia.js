import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  createPhieuGiamGia,
  createPhieuGiamGiaKhachHang,
  deletePhieuGiamGiaKhachHang,
  getPhieuGiamGiaDetail,
  getPhieuGiamGiaKhachHangList,
  updatePhieuGiamGia,
  checkTenPhieuGiamGia,
  checkMaPhieuGiamGia,
} from "../../../services/khuyen-mai";
import { layDanhSachKhachHang } from "../../../services/khach-hang";
import { layDanhSachHoaDon } from "../../../services/hoa-don";
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

export function useChiTietPhieuGiamGia() {
  const route = useRoute();
  const router = useRouter();

  const id = route.params.id;
  const laMoi = !id;

  const dangTai = ref(false);
  const saving = ref(false);
  const loiTrang = ref("");

  const formErrors = reactive({});

  const form = reactive({
    id: null,
    ma: "",
    ten: "",
    loai: "1",
    loaiPhieu: "1",
    giaTri: "",
    giaTriToiThieu: "0",
    giamToiDa: "0",
    ngayBatDau: "",
    ngayKetThuc: "",
    soLuong: "",
    soLuongDaDung: 0,
    trangThai: "1",
  });

  const soLuongVoHan = ref(false);
  const isLoadingData = ref(false);

  const MIN_INT = -2147483648;
  const MAX_INT = 2147483647;
  const MAX_GIAM_TOI_DA = 100000000;

  function parseQuantityNumber(value) {
    if (value === "" || value == null) return 0;
    const str = String(value).trim();
    const isNegative = str.startsWith("-");
    const rawDigits = str.replace(/[^\d]/g, "");
    if (!rawDigits) return 0;
    const num = Number(rawDigits);
    return isNegative ? -num : num;
  }

  function formatQuantityNumber(value) {
    if (value === "" || value == null) return "";
    const numberValue = parseQuantityNumber(value);
    return numberValue ? numberValue.toLocaleString("vi-VN") : "";
  }

  // Ô "Số lượng" hiển thị SỐ CÒN LẠI (= tổng - đã dùng), nhưng bên trong form.soLuong
  // vẫn giữ TỔNG để validate/lưu không đổi. Nhập còn lại mới -> tổng = còn lại + đã dùng.
  const soLuongDisplay = computed({
    get() {
      if (soLuongVoHan.value) {
        return "Vô hạn";
      }
      const daDung = Number(form.soLuongDaDung || 0);
      const conLai = Math.max(parseQuantityNumber(form.soLuong) - daDung, 0);
      return formatQuantityNumber(conLai);
    },
    set(val) {
      if (!soLuongVoHan.value) {
        const numeric = parseQuantityNumber(val);
        if (numeric > MAX_INT || numeric < MIN_INT) {
          soLuongVoHan.value = true;
          form.soLuong = "999999";
          delete formErrors.soLuong;
        } else {
          form.soLuong = formatQuantityNumber(val);
        }
      }
    }
  });

  function handleSoLuongEnter() {
    if (soLuongVoHan.value || form.loaiPhieu === "2") return;
    const numeric = parseQuantityNumber(form.soLuong);
    if (numeric > MAX_INT || numeric < MIN_INT) {
      soLuongVoHan.value = true;
      form.soLuong = "999999";
      delete formErrors.soLuong;
    }
  }

  function isHetHan(ngayKetThuc) {
    if (!ngayKetThuc) return false;
    const homNay = new Date();
    homNay.setHours(0, 0, 0, 0);
    const ngayKT = new Date(ngayKetThuc);
    ngayKT.setHours(0, 0, 0, 0);
    return ngayKT < homNay;
  }

  const isReadOnly = computed(() => {
    if (!laMoi && Number(form.loaiPhieu) === 2 && (isHetHan(form.ngayKetThuc) || Number(form.trangThai) === 2)) {
      return true;
    }
    return false;
  });

  const todayStr = computed(() => {
    const d = new Date();
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const date = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${date}`;
  });

  const isNgayBatDauReadOnly = computed(() => {
    if (isReadOnly.value) return true;
    if (!laMoi && form.ngayBatDau && form.ngayBatDau <= todayStr.value) {
      return true;
    }
    return false;
  });

  // Computed cho giá trị giảm (% hoặc VNĐ)
  const giaTriDisplay = computed({
    get() {
      return form.giaTri;
    },
    set(value) {
      if (form.loai === "1") {
        form.giaTri = value;
      } else {
        form.giaTri = formatVndNumber(value);
      }
    }
  });

  // Computed cho giá trị đơn tối thiểu
  const giaTriToiThieuVnd = computed({
    get() {
      return form.giaTriToiThieu;
    },
    set(value) {
      form.giaTriToiThieu = formatVndNumber(value);
    }
  });

  // Computed cho giảm tối đa
  const giamToiDaVnd = computed({
    get() {
      return form.giamToiDa;
    },
    set(value) {
      form.giamToiDa = formatVndNumber(value);
    }
  });

  const searchKh = ref("");
  const danhSachKh = ref([]);
  const dsEmailChon = ref([]);
  const dangTaiKh = ref(false);
  const lienKetKhachHangHienTai = ref([]);
  const soLuongPhieuCongKhai = ref("");

  // Phân trang khách hàng
  const trangKh = ref(1);
  const soPhanTuMotTrangKh = ref(5);
  const boLocKh = ref("tat-ca"); // "tat-ca" | "da-chon"

  // Computed: Danh sách khách hàng sau khi lọc
  const danhSachKhFiltered = computed(() => {
    if (boLocKh.value === "da-chon") {
      return danhSachKh.value.filter((kh) => dsEmailChon.value.includes(kh.email));
    }
    return danhSachKh.value;
  });

  // Computed: Tổng số trang
  const tongSoTrangKh = computed(() => {
    return Math.max(1, Math.ceil(danhSachKhFiltered.value.length / soPhanTuMotTrangKh.value));
  });

  // Computed: Khách hàng hiển thị trong trang hiện tại
  const danhSachKhTrang = computed(() => {
    const start = (trangKh.value - 1) * soPhanTuMotTrangKh.value;
    return danhSachKhFiltered.value.slice(start, start + soPhanTuMotTrangKh.value);
  });

  function getToday() {
    return todayStr.value;
  }

  function parseVndNumber(value) {
    const rawValue = String(value ?? "").replace(/[^\d]/g, "");
    return rawValue ? Number(rawValue) : 0;
  }

  function formatVndNumber(value) {
    const numberValue = parseVndNumber(value);
    return numberValue ? numberValue.toLocaleString("vi-VN") : "0";
  }

  function handleVndInput(field, event) {
    form[field] = formatVndNumber(event.target.value);
  }

  function resetErrors() {
    Object.keys(formErrors).forEach((key) => delete formErrors[key]);
  }

  function taoMaNgauNhien() {
    form.ma = `VCH${Math.random().toString(36).substring(2, 8).toUpperCase()}`;
  }

  function dongBoSoLuongPhieuCaNhan() {
    if (form.loaiPhieu === "2") {
      form.soLuong = formatQuantityNumber(dsEmailChon.value.length);
    }
  }

  async function taiDanhSachKh() {
    dangTaiKh.value = true;
    try {
      const res = await layDanhSachKhachHang({
        keyword: searchKh.value,
        page: 0,
        size: 50,
      });
      danhSachKh.value = Array.isArray(res) ? res : res?.content || [];
    } catch (error) {
      console.error("Lỗi tải khách hàng:", error);
    } finally {
      dangTaiKh.value = false;
    }
  }

  async function taiLienKetKhachHangTheoPhieu(maPhieu) {
    if (!maPhieu) {
      lienKetKhachHangHienTai.value = [];
      dsEmailChon.value = [];
      return;
    }

    const data = await getPhieuGiamGiaKhachHangList({
      keyword: maPhieu,
      pageNo: 0,
      pageSize: 1000,
    });

    lienKetKhachHangHienTai.value = (data?.content || []).filter(
      (item) => item?.maPhieuGiamGia === maPhieu && item?.email,
    );
    dsEmailChon.value = lienKetKhachHangHienTai.value.map((item) => item.email);
    dongBoSoLuongPhieuCaNhan();
  }

  function laKhachHangDaDung(email) {
    const link = lienKetKhachHangHienTai.value.find(item => item.email === email);
    return link && link.ngaySuDung != null;
  }

  function toggleEmail(email) {
    if (!email || isReadOnly.value) return;

    if (laKhachHangDaDung(email)) {
      showError("Không thể bỏ chọn khách hàng đã sử dụng phiếu giảm giá này.", "Không thể thực hiện");
      return;
    }

    const index = dsEmailChon.value.indexOf(email);
    if (index === -1) {
      dsEmailChon.value.unshift(email);
      return;
    }

    dsEmailChon.value.splice(index, 1);
  }

  function xoaKhachHang(email) {
    if (isReadOnly.value) return;
    if (laKhachHangDaDung(email)) {
      showError("Không thể xóa khách hàng đã sử dụng phiếu giảm giá này.", "Không thể thực hiện");
      return;
    }
    const index = dsEmailChon.value.indexOf(email);
    if (index !== -1) {
      dsEmailChon.value.splice(index, 1);
    }
  }

  function chonTatCa() {
    if (isReadOnly.value) return;
    const emailsTrang = danhSachKh.value.map((kh) => kh.email).filter(Boolean);
    const daChonHet =
      emailsTrang.length > 0 &&
      emailsTrang.every((email) => dsEmailChon.value.includes(email));

    if (daChonHet) {
      // Chỉ bỏ chọn những người chưa dùng
      const emailsChuaDung = emailsTrang.filter(email => !laKhachHangDaDung(email));
      dsEmailChon.value = dsEmailChon.value.filter(
        (email) => !emailsChuaDung.includes(email),
      );
      return;
    }

    dsEmailChon.value = Array.from(
      new Set([...dsEmailChon.value, ...emailsTrang]),
    );
  }

  let searchTimer;
  function handleSearch() {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(taiDanhSachKh, 400);
  }

  // --- Watchers ---

  // Check trùng tên realtime bằng API với debounce 500ms
  let nameCheckTimer = null;
  watch(
    () => form.ten,
    (newVal) => {
      clearTimeout(nameCheckTimer);
      if (!newVal || !newVal.trim()) {
        formErrors.ten = "Vui lòng nhập tên phiếu giảm giá";
        return;
      }
      if (newVal.trim().length > 200) {
        formErrors.ten = "Tên phiếu giảm giá không được vượt quá 200 ký tự";
        return;
      }
      delete formErrors.ten;

      nameCheckTimer = setTimeout(async () => {
        try {
          const res = await checkTenPhieuGiamGia(newVal.trim(), id ? Number(id) : null);
          if (res && res.exists) {
            formErrors.ten = "Tên phiếu giảm giá đã tồn tại";
          }
        } catch (e) {
          console.error("Lỗi kiểm tra trùng tên:", e);
        }
      }, 500);
    }
  );

  // Validate mã phiếu realtime
  watch(
    () => form.ma,
    (newVal) => {
      if (!newVal || !newVal.trim()) {
        formErrors.ma = "Vui lòng nhập mã phiếu giảm giá";
        return;
      }
      if (newVal.trim().length > 100) {
        formErrors.ma = "Mã phiếu giảm giá không được vượt quá 100 ký tự";
        return;
      }
      if (!/^[A-Za-z0-9_-]+$/.test(newVal.trim())) {
        formErrors.ma = "Mã phiếu chỉ được chứa chữ, số, dấu gạch ngang và gạch dưới";
        return;
      }
      delete formErrors.ma;
    }
  );

  // Validate ngày bắt đầu realtime
  watch(
    () => form.ngayBatDau,
    (newVal) => {
      if (!newVal) {
        formErrors.ngayBatDau = "Vui lòng chọn ngày bắt đầu áp dụng";
        return;
      }
      delete formErrors.ngayBatDau;
      if (!isNgayBatDauReadOnly.value && newVal < getToday()) {
        formErrors.ngayBatDau = "Ngày bắt đầu không được ở trong quá khứ";
      }
      if (form.ngayKetThuc && newVal > form.ngayKetThuc) {
        formErrors.ngayKetThuc = "Ngày kết thúc không được trước ngày bắt đầu";
      } else {
        delete formErrors.ngayKetThuc;
      }
    }
  );

  // Validate mã phiếu giảm giá realtime (kiểm tra trùng mã)
  let checkMaTimer = null;
  watch(
    () => form.ma,
    (newVal) => {
      if (checkMaTimer) clearTimeout(checkMaTimer);
      if (isLoadingData.value) return;

      const ma = (newVal || "").trim();
      if (!ma) {
        formErrors.ma = "Vui lòng nhập mã phiếu giảm giá";
        return;
      }
      if (ma.length > 100) {
        formErrors.ma = "Mã phiếu giảm giá không được vượt quá 100 ký tự";
        return;
      }
      if (!/^[A-Za-z0-9_-]+$/.test(ma)) {
        formErrors.ma = "Mã phiếu chỉ được chứa chữ, số, dấu gạch ngang và gạch dưới";
        return;
      }

      delete formErrors.ma;

      checkMaTimer = setTimeout(async () => {
        try {
          const res = await checkMaPhieuGiamGia(ma, id || null);
          if (res?.exists) {
            formErrors.ma = "Mã phiếu giảm giá đã tồn tại";
          } else if (formErrors.ma === "Mã phiếu giảm giá đã tồn tại") {
            delete formErrors.ma;
          }
        } catch (error) {
          console.error("Lỗi kiểm tra trùng mã phiếu:", error);
        }
      }, 300);
    }
  );

  // Validate ngày kết thúc realtime
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

  watch(
    () => form.loaiPhieu,
    (loaiPhieuMoi, loaiPhieuCu) => {
      if (loaiPhieuMoi === "2") {
        soLuongPhieuCongKhai.value = form.soLuong;
        dongBoSoLuongPhieuCaNhan();
        soLuongVoHan.value = false; // Reset vô hạn khi chuyển sang cá nhân
        return;
      }

      delete formErrors.email;
      if (loaiPhieuCu === "2") {
        form.soLuong = soLuongPhieuCongKhai.value;
      }
    },
  );

  watch(soLuongVoHan, (isVoHan) => {
    if (isVoHan) {
      form.soLuong = "999999";
      delete formErrors.soLuong;
    } else if (form.soLuong === "999999") {
      form.soLuong = "";
    }
  });

  watch(
    dsEmailChon,
    () => {
      if (form.loaiPhieu === "2") {
        dongBoSoLuongPhieuCaNhan();
      }
    },
    { deep: true },
  );

  watch(
    () => form.giaTri,
    (newVal) => {
      if (newVal === "" || newVal === null || newVal === undefined) {
        delete formErrors.giaTri;
        return;
      }
      if (Number(form.loai) === 1) {
        const val = Number(newVal);
        if (val <= 0) {
          formErrors.giaTri = "Giá trị giảm phải lớn hơn 0%";
        } else if (val > 100) {
          formErrors.giaTri = "Phần trăm giảm không được vượt quá 100%";
        } else {
          delete formErrors.giaTri;
        }
        if (val === 100) {
          form.giamToiDa = "0";
          delete formErrors.giamToiDa;
        }
      } else {
        const val = parseVndNumber(newVal);
        if (val <= 0) {
          formErrors.giaTri = "Giá trị giảm phải lớn hơn 0";
        } else if (val > MAX_GIAM_TOI_DA) {
          formErrors.giaTri = "Giá trị giảm không được vượt quá 100.000.000 VNĐ";
        } else {
          delete formErrors.giaTri;
        }
      }
    },
  );

  watch(
    () => form.giamToiDa,
    (newVal) => {
      if (newVal === "" || newVal === null || newVal === undefined) {
        delete formErrors.giamToiDa;
        return;
      }
      const val = parseVndNumber(newVal);
      if (val < 0) {
        formErrors.giamToiDa = "Mức giảm tối đa không được nhỏ hơn 0";
      } else if (val > MAX_GIAM_TOI_DA) {
        formErrors.giamToiDa = "Mức giảm tối đa không được vượt quá 100.000.000 VNĐ";
      } else {
        delete formErrors.giamToiDa;
      }
    },
  );

  watch(
    () => form.loai,
    (newLoai, oldLoai) => {
      if (isLoadingData.value) return;
      
      delete formErrors.giaTri;
      
      if (oldLoai && oldLoai !== newLoai) {
        form.giaTri = "";
        form.giamToiDa = "0";
      }
    },
  );

  async function taiChiTiet() {
    await taiDanhSachKh();

    if (laMoi) {
      if (!form.ma) {
        taoMaNgauNhien();
      }
      form.ngayBatDau = getToday();
      await taiHoaDonLienQuan();
      return;
    }

    dangTai.value = true;
    isLoadingData.value = true;
    try {
      const detail = await getPhieuGiamGiaDetail(id);
      const loai = String(detail.loai ?? 1);
      const loaiPhieu = String(detail.loaiPhieu ?? 1);
      const soLuong = String(detail.soLuong ?? "");

      if (Number(soLuong) === 999999) {
        soLuongVoHan.value = true;
      }

      Object.assign(form, {
        id: detail.id,
        ma: detail.ma ?? "",
        ten: detail.ten ?? "",
        loai,
        loaiPhieu,
        giaTri:
          loai === "2"
            ? formatVndNumber(detail.giaTri ?? 0)
            : String(detail.giaTri ?? ""),
        giaTriToiThieu: formatVndNumber(detail.giaTriToiThieu ?? 0),
        giamToiDa: formatVndNumber(detail.giamToiDa ?? 0),
        ngayBatDau: formatToLocalDateString(detail.ngayBatDau),
        ngayKetThuc: formatToLocalDateString(detail.ngayKetThuc),
        soLuong: formatQuantityNumber(soLuong),
        soLuongDaDung: Number(detail.soLuongDaDung ?? 0),
        trangThai: String(detail.trangThai ?? 1),
      });

      if (loaiPhieu === "2") {
        await taiLienKetKhachHangTheoPhieu(detail.ma);
      } else {
        soLuongPhieuCongKhai.value = soLuong;
        lienKetKhachHangHienTai.value = [];
        dsEmailChon.value = [];
      }
      await taiHoaDonLienQuan();
    } catch (error) {
      loiTrang.value = getDisplayErrorMessage(
        error,
        "Không thể tải chi tiết phiếu giảm giá",
      );
    } finally {
      dangTai.value = false;
      setTimeout(() => {
        isLoadingData.value = false;
      }, 100);
    }
  }

  async function dongBoKhachHangPhieuCaNhan(phieuId, maPhieu) {
    const emailsDaChon = new Set(dsEmailChon.value.filter(Boolean));
    const lienKetHienTai = lienKetKhachHangHienTai.value.filter(
      (item) => item?.id && item?.email,
    );
    const emailHienTai = new Set(lienKetHienTai.map((item) => item.email));

    const emailsCanThem = [...emailsDaChon].filter(
      (email) => !emailHienTai.has(email),
    );
    const lienKetCanXoa = lienKetHienTai.filter(
      (item) => !emailsDaChon.has(item.email),
    );

    await Promise.all(
      emailsCanThem.map((email) =>
        createPhieuGiamGiaKhachHang({
          phieuGiamGiaId: phieuId,
          email,
          trangThai: 1,
        }),
      ),
    );

    await Promise.all(
      lienKetCanXoa.map((item) => deletePhieuGiamGiaKhachHang(item.id)),
    );

    await taiLienKetKhachHangTheoPhieu(maPhieu);
  }

  async function submitForm() {
    resetErrors();
    let isValid = true;

    if (!form.ma.trim()) {
      formErrors.ma = "Vui lòng nhập mã phiếu giảm giá";
      isValid = false;
    } else if (form.ma.trim().length > 100) {
      formErrors.ma = "Mã phiếu giảm giá không được vượt quá 100 ký tự";
      isValid = false;
    } else if (!/^[A-Za-z0-9_-]+$/.test(form.ma.trim())) {
      formErrors.ma =
        "Mã phiếu chỉ được chứa chữ, số, dấu gạch ngang và gạch dưới";
      isValid = false;
    } else {
      try {
        const res = await checkMaPhieuGiamGia(form.ma.trim(), id || null);
        if (res?.exists) {
          formErrors.ma = "Mã phiếu giảm giá đã tồn tại";
          isValid = false;
        }
      } catch (error) {
        console.error("Lỗi kiểm tra trùng mã phiếu:", error);
      }
    }

    if (!form.ten.trim()) {
      formErrors.ten = "Vui lòng nhập tên phiếu giảm giá";
      isValid = false;
    } else if (form.ten.trim().length > 200) {
      formErrors.ten = "Tên phiếu giảm giá không được vượt quá 200 ký tự";
      isValid = false;
    }

    if (Number(form.loai) === 1) {
      const val = Number(form.giaTri);
      if (!form.giaTri || val <= 0) {
        formErrors.giaTri = "Giá trị giảm phải lớn hơn 0";
        isValid = false;
      } else if (val > 100) {
        formErrors.giaTri = "Phần trăm giảm không được vượt quá 100%";
        isValid = false;
      }
    } else {
      const val = parseVndNumber(form.giaTri);
      if (!form.giaTri || val <= 0) {
        formErrors.giaTri = "Giá trị giảm phải lớn hơn 0";
        isValid = false;
      } else if (val > MAX_GIAM_TOI_DA) {
        formErrors.giaTri = "Giá trị giảm không được vượt quá 100.000.000 VNĐ";
        isValid = false;
      }
    }

    const giaTriToiThieu = parseVndNumber(form.giaTriToiThieu);
    // Nếu loại là tiền mặt, bỏ qua mức giảm tối đa (luôn gán bằng 0)
    const giamToiDa = Number(form.loai) === 2 ? 0 : parseVndNumber(form.giamToiDa);

    if (giaTriToiThieu < 0) {
      formErrors.giaTriToiThieu =
        "Giá trị đơn tối thiểu không được nhỏ hơn 0";
      isValid = false;
    }
    if (giamToiDa < 0) {
      formErrors.giamToiDa = "Mức giảm tối đa không được nhỏ hơn 0";
      isValid = false;
    } else if (giamToiDa > MAX_GIAM_TOI_DA) {
      formErrors.giamToiDa = "Mức giảm tối đa không được vượt quá 100.000.000 VNĐ";
      isValid = false;
    }

    if (
      Number(form.loai) === 2 &&
      giaTriToiThieu > 0 &&
      parseVndNumber(form.giaTri) > giaTriToiThieu
    ) {
      formErrors.giaTri =
        "Số tiền giảm không được lớn hơn giá trị đơn tối thiểu";
      isValid = false;
    }

    if (!soLuongVoHan.value) {
      const numericSoLuong = parseQuantityNumber(form.soLuong);
      if (numericSoLuong > MAX_INT || numericSoLuong < MIN_INT) {
        soLuongVoHan.value = true;
        form.soLuong = "999999";
        delete formErrors.soLuong;
      } else if (!form.soLuong || numericSoLuong <= 0) {
        formErrors.soLuong = "Số lượng phiếu phải lớn hơn 0";
        isValid = false;
      } else if (
        !laMoi &&
        numericSoLuong < Number(form.soLuongDaDung || 0)
      ) {
        formErrors.soLuong =
          "Số lượng phiếu không được nhỏ hơn số lượng đã sử dụng";
        isValid = false;
      }
    } else {
      form.soLuong = "999999";
      delete formErrors.soLuong;
    }

    if (!form.ngayBatDau) {
      formErrors.ngayBatDau = "Vui lòng chọn ngày bắt đầu áp dụng";
      isValid = false;
    } else if (!isNgayBatDauReadOnly.value && form.ngayBatDau < getToday()) {
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

    if (form.loaiPhieu === "2" && dsEmailChon.value.length === 0) {
      formErrors.email = "Phải chọn ít nhất 1 khách hàng cho phiếu cá nhân";
      showError(
        "Vui lòng chọn ít nhất một khách hàng cho phiếu cá nhân",
        "Chưa chọn khách hàng",
      );
      isValid = false;
    }

    if (!isValid) return;

    const confirmMsg = laMoi
      ? "Bạn có chắc chắn muốn thêm mới phiếu giảm giá này không?"
      : "Bạn có chắc chắn muốn cập nhật thông tin phiếu giảm giá này không?";
    const isConfirmed = await showConfirm(confirmMsg);
    if (!isConfirmed) return;

    saving.value = true;
    loiTrang.value = "";

    try {
      const payload = {
        ma: form.ma.trim(),
        ten: form.ten.trim(),
        loai: Number(form.loai),
        loaiPhieu: Number(form.loaiPhieu),
        giaTri: parseVndNumber(form.giaTri),
        giaTriToiThieu,
        giamToiDa,
        ngayBatDau: form.ngayBatDau,
        ngayKetThuc: form.ngayKetThuc,
        soLuong: parseQuantityNumber(form.soLuong),
        trangThai: laMoi ? undefined : form.trangThai,
      };

      let phieuId = id;

      if (laMoi) {
        const res = await createPhieuGiamGia(payload);
        phieuId = res?.id;
        showSuccess("Tạo phiếu giảm giá thành công");
      } else {
        await updatePhieuGiamGia(id, payload);
        showSuccess("Cập nhật phiếu giảm giá thành công");
      }

      if (Number(form.loaiPhieu) === 2) {
        await dongBoKhachHangPhieuCaNhan(phieuId, payload.ma);
      } else if (lienKetKhachHangHienTai.value.length) {
        await Promise.all(
          lienKetKhachHangHienTai.value.map((item) =>
            deletePhieuGiamGiaKhachHang(item.id),
          ),
        );
        lienKetKhachHangHienTai.value = [];
        dsEmailChon.value = [];
      }

      setTimeout(() => {
        router.push({ name: "admin-phieu-giam-gia" });
      }, 900);
    } catch (error) {
      Object.assign(formErrors, getFieldErrors(error));
      loiTrang.value = getDisplayErrorMessage(
        error,
        "Không thể lưu phiếu giảm giá",
      );
    } finally {
      saving.value = false;
    }
  }

  const danhSachTatCaHoaDon = ref([]);
  const listHoaDonApplied = ref([]);
  const dangTaiHoaDon = ref(false);
  const loiTaiHoaDon = ref("");

  const getHoaDonsCuaKhachHang = (email) => {
    if (!email) return [];
    return listHoaDonApplied.value.filter(
      (hd) =>
        hd.emailKhachHang &&
        hd.emailKhachHang.toLowerCase() === email.toLowerCase(),
    );
  };

  const getTongDonHangCuaKhachHang = (email) => {
    if (!email) return 0;
    return danhSachTatCaHoaDon.value.filter(
      (hd) =>
        hd.emailKhachHang &&
        hd.emailKhachHang.toLowerCase() === email.toLowerCase(),
    ).length;
  };

  const getDonHangGanNhat = (email) => {
    if (!email) return null;
    const khHoaDons = danhSachTatCaHoaDon.value.filter(
      (hd) =>
        hd.emailKhachHang &&
        hd.emailKhachHang.toLowerCase() === email.toLowerCase(),
    );
    if (khHoaDons.length === 0) return null;
    return [...khHoaDons].sort(
      (a, b) => new Date(b.ngayTao) - new Date(a.ngayTao),
    )[0];
  };

  function dinhDangNgaySinh(ngay) {
    if (!ngay) return "—";
    try {
      const parts = ngay.split("-");
      if (parts.length === 3) {
        return `${parts[2]}/${parts[1]}/${parts[0]}`;
      }
      const d = new Date(ngay);
      if (isNaN(d.getTime())) return ngay;
      return new Intl.DateTimeFormat("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
      }).format(d);
    } catch (e) {
      return ngay;
    }
  }

  function dinhDangTien(value) {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
      maximumFractionDigits: 0,
    }).format(value || 0);
  }

  function dinhDangNgay(ngay) {
    if (!ngay) return "—";
    return new Intl.DateTimeFormat("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    }).format(new Date(ngay));
  }

  const mauTrangThai = {
    "Chờ xác nhận": "bg-amber-50 text-amber-600 border border-amber-100",
    "Đã xác nhận": "bg-orange-50 text-orange-600 border border-orange-100",
    "Chờ lấy hàng": "bg-blue-50 text-blue-600 border border-blue-100",
    "Đang giao hàng": "bg-violet-50 text-violet-600 border border-violet-100",
    "Đã giao hàng": "bg-cyan-50 text-cyan-600 border border-cyan-100",
    "Giao hàng thất bại": "bg-rose-50 text-rose-600 border border-rose-100",
    "Hoàn thành": "bg-emerald-50 text-emerald-600 border border-emerald-100",
    Hủy: "bg-stone-100 text-stone-600 border border-stone-200",
    "Yêu cầu hủy": "bg-slate-100 text-slate-600 border border-slate-200",
  };

  async function taiHoaDonLienQuan() {
    dangTaiHoaDon.value = true;
    loiTaiHoaDon.value = "";
    try {
      const allInvoices = await layDanhSachHoaDon();
      danhSachTatCaHoaDon.value = allInvoices || [];

      if (form.ma && !laMoi) {
        listHoaDonApplied.value = (allInvoices || []).filter(
          (hd) =>
            hd.maPhieuGiamGia &&
            hd.maPhieuGiamGia.toLowerCase() === form.ma.toLowerCase(),
        );
      } else {
        listHoaDonApplied.value = [];
      }
    } catch (err) {
      console.error("Lỗi tải hóa đơn liên quan:", err);
      loiTaiHoaDon.value = "Không thể tải danh sách hóa đơn liên quan";
    } finally {
      dangTaiHoaDon.value = false;
    }
  }

  function xemChiTietHoaDon(id) {
    router.push({ name: "admin-hoa-don-chi-tiet", params: { id } });
  }

  onMounted(taiChiTiet);

  return {
    route,
    router,
    id,
    laMoi,
    dangTai,
    saving,
    loiTrang,
    formErrors,
    form,
    soLuongVoHan,
    soLuongDisplay,
    handleSoLuongEnter,
    isReadOnly,
    isHetHan,
    giaTriDisplay,
    giaTriToiThieuVnd,
    giamToiDaVnd,
    searchKh,
    danhSachKh,
    dsEmailChon,
    dangTaiKh,
    lienKetKhachHangHienTai,
    trangKh,
    soPhanTuMotTrangKh,
    boLocKh,
    danhSachKhFiltered,
    tongSoTrangKh,
    danhSachKhTrang,
    getToday,
    parseVndNumber,
    formatVndNumber,
    handleVndInput,
    resetErrors,
    taoMaNgauNhien,
    dongBoSoLuongPhieuCaNhan,
    taiDanhSachKh,
    taiLienKetKhachHangTheoPhieu,
    laKhachHangDaDung,
    toggleEmail,
    xoaKhachHang,
    chonTatCa,
    handleSearch,
    taiChiTiet,
    dongBoKhachHangPhieuCaNhan,
    submitForm,
    danhSachTatCaHoaDon,
    listHoaDonApplied,
    dangTaiHoaDon,
    loiTaiHoaDon,
    getHoaDonsCuaKhachHang,
    getTongDonHangCuaKhachHang,
    getDonHangGanNhat,
    dinhDangNgaySinh,
    dinhDangTien,
    dinhDangNgay,
    taiHoaDonLienQuan,
    xemChiTietHoaDon,
    mauTrangThai,
    todayStr,
    isNgayBatDauReadOnly,
  };
}
