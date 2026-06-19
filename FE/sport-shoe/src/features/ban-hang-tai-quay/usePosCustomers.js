import { computed, ref, watch } from "vue";
import { timKhachHangTheoSoDienThoai } from "../../services/ban-hang-tai-quay";
import {
  GUEST_LABEL,
  HIDDEN_INFO_LABEL,
  NO_CUSTOMER_LABEL,
  NO_CUSTOMER_PHONE_LABEL
} from "./constants";

export function usePosCustomers({
  activePendingInvoice,
  deliveryRecipientName,
  deliveryRecipientPhone,
  deliveryAddress,
  danhDauCanApDungLaiPhieu,
  clearFeedback,
  pageError
}) {
  const customerKeyword = ref("");
  const customerResults = ref([]);
  const selectedCustomer = ref(null);
  const loadingCustomers = ref(false);
  const showCustomerDropdown = ref(false);

  let customerTimer;

  const isGuestCustomer = computed(
    () => customerKeyword.value.trim().toLowerCase() === GUEST_LABEL.toLowerCase()
  );
  const tenKhachHangHienThi = computed(() => {
    if (selectedCustomer.value) {
      return selectedCustomer.value.hoTen;
    }
    if (isGuestCustomer.value) {
      return GUEST_LABEL;
    }
    return activePendingInvoice.value?.tenKhachHang || NO_CUSTOMER_LABEL;
  });
  const soDienThoaiKhachHangHienThi = computed(() => {
    if (selectedCustomer.value) {
      return selectedCustomer.value.sdt;
    }
    if (isGuestCustomer.value) {
      return HIDDEN_INFO_LABEL;
    }
    return activePendingInvoice.value?.soDienThoai || NO_CUSTOMER_PHONE_LABEL;
  });

  async function fetchCustomers(keyword) {
    if (!keyword.trim() || keyword.trim().toLowerCase() === GUEST_LABEL.toLowerCase()) {
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

  function chonKhachHang(customer) {
    selectedCustomer.value = customer;
    customerKeyword.value = customer.hoTen;
    if (!deliveryRecipientName.value.trim()) {
      deliveryRecipientName.value = customer.hoTen || "";
    }
    if (!deliveryRecipientPhone.value.trim()) {
      deliveryRecipientPhone.value = customer.sdt || "";
    }
    if (deliveryAddress && !deliveryAddress.value.trim() && customer.diaChiMacDinh) {
      deliveryAddress.value = customer.diaChiMacDinh;
    }
    customerResults.value = [];
    showCustomerDropdown.value = false;
    danhDauCanApDungLaiPhieu();
    clearFeedback();
  }

  function boChonKhachHang() {
    selectedCustomer.value = null;
    customerKeyword.value = "";
    customerResults.value = [];
    showCustomerDropdown.value = false;
    danhDauCanApDungLaiPhieu();
    clearFeedback();
  }

  function chonKhachVangLai() {
    selectedCustomer.value = null;
    customerKeyword.value = GUEST_LABEL;
    if (!activePendingInvoice.value) {
      deliveryRecipientName.value = "";
      deliveryRecipientPhone.value = "";
    }
    customerResults.value = [];
    showCustomerDropdown.value = false;
    danhDauCanApDungLaiPhieu();
    clearFeedback();
  }

  async function moDanhSachKhachHang() {
    const keyword = customerKeyword.value.trim();
    if (keyword && keyword.toLowerCase() !== GUEST_LABEL.toLowerCase()) {
      showCustomerDropdown.value = true;
      await fetchCustomers(customerKeyword.value);
      return;
    }
    showCustomerDropdown.value = false;
  }

  function dongDanhSachKhachHang() {
    window.setTimeout(() => {
      showCustomerDropdown.value = false;
    }, 150);
  }

  function clearCustomerTimer() {
    if (customerTimer) {
      window.clearTimeout(customerTimer);
    }
  }

  watch(customerKeyword, (value) => {
    clearCustomerTimer();
    const keyword = value.trim().toLowerCase();
    if (selectedCustomer.value) {
      const tenKhachDangChon = selectedCustomer.value.hoTen?.trim().toLowerCase() ?? "";
      const soDienThoaiDangChon = selectedCustomer.value.sdt?.trim().toLowerCase() ?? "";
      if (keyword !== tenKhachDangChon && keyword !== soDienThoaiDangChon) {
        selectedCustomer.value = null;
        danhDauCanApDungLaiPhieu();
      }
    }
    showCustomerDropdown.value = value.trim().length > 0 && keyword !== GUEST_LABEL.toLowerCase();
    customerTimer = window.setTimeout(() => {
      void fetchCustomers(value);
    }, 250);
  });

  return {
    customerKeyword,
    customerResults,
    selectedCustomer,
    loadingCustomers,
    showCustomerDropdown,
    isGuestCustomer,
    tenKhachHangHienThi,
    soDienThoaiKhachHangHienThi,
    fetchCustomers,
    chonKhachHang,
    boChonKhachHang,
    chonKhachVangLai,
    moDanhSachKhachHang,
    dongDanhSachKhachHang,
    clearCustomerTimer
  };
}
