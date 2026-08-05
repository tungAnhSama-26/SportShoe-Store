import { ref, readonly } from "vue";
import { 
  layCaHoatDong, 
  moCa, 
  banGiaoCa, 
  layCaChoXacNhan, 
  xacNhanBanGiao,
  ketCa,
  huyBanGiao,
  tuChoiBanGiao,
  baoCaoSuCoGiaoCa
} from "../services/giao-ca";

const activeShift = ref(null);
const pendingHandovers = ref([]);
const loadingShift = ref(false);

async function loadActiveShift() {
  loadingShift.value = true;
  try {
    const data = await layCaHoatDong();
    activeShift.value = data || null;
  } catch (error) {
    console.error("Error loading active shift:", error);
    activeShift.value = null;
  } finally {
    loadingShift.value = false;
  }
}

async function loadPendingHandovers() {
  try {
    const data = await layCaChoXacNhan();
    pendingHandovers.value = data || [];
  } catch (error) {
    console.error("Error loading pending handovers:", error);
    pendingHandovers.value = [];
  }
}

async function handleMoCa(tienDauCa, ghiChu, caLamId = null, lyDoMoMuon = "") {
  try {
    const data = await moCa({ tienDauCa, ghiChu, caLamId, lyDoMoMuon });
    activeShift.value = data;
    return { success: true, message: "Mở ca làm việc thành công" };
  } catch (error) {
    return { success: false, message: error?.message || "Mở ca thất bại" };
  }
}

async function handleBanGiaoCa(payload) {
  try {
    activeShift.value = await banGiaoCa(payload);
    await loadPendingHandovers();
    return { success: true, message: "Bàn giao ca thành công. Vui lòng chờ người nhận xác nhận." };
  } catch (error) {
    return { success: false, message: error?.message || "Bàn giao ca thất bại" };
  }
}

async function handleKetCa(payload) {
  try {
    const data = await ketCa(payload);
    activeShift.value = null;
    return { success: true, data, message: "Kết ca làm việc thành công" };
  } catch (error) {
    return { success: false, message: error?.message || "Kết ca làm việc thất bại" };
  }
}

async function handleConfirmHandover(id, tienNhanKiemDem, ghiChu) {
  try {
    await xacNhanBanGiao(id, { tienNhanKiemDem, ghiChu });
    await loadActiveShift();
    await loadPendingHandovers();
    return { success: true, message: "Nhận bàn giao và mở ca mới thành công" };
  } catch (error) {
    return { success: false, message: error?.message || "Xác nhận bàn giao thất bại" };
  }
}

async function handleCancelHandover(id, lyDo) {
  try {
    activeShift.value = await huyBanGiao(id, { lyDo });
    await loadPendingHandovers();
    return { success: true, message: "Đã hủy yêu cầu bàn giao và mở lại ca làm việc" };
  } catch (error) {
    return { success: false, message: error?.message || "Hủy bàn giao thất bại" };
  }
}

async function handleRejectHandover(id, lyDo) {
  try {
    await tuChoiBanGiao(id, { lyDo });
    await loadActiveShift();
    await loadPendingHandovers();
    return { success: true, message: "Đã từ chối nhận bàn giao" };
  } catch (error) {
    return { success: false, message: error?.message || "Từ chối bàn giao thất bại" };
  }
}

async function handleReportIncident(id, payload) {
  try {
    await baoCaoSuCoGiaoCa(id, payload);
    return { success: true, message: "Đã gửi báo cáo sự cố đến quản trị viên" };
  } catch (error) {
    return { success: false, message: error?.message || "Gửi báo cáo sự cố thất bại" };
  }
}

export function useGiaoCa() {
  return {
    activeShift: readonly(activeShift),
    pendingHandovers: readonly(pendingHandovers),
    loadingShift: readonly(loadingShift),
    loadActiveShift,
    loadPendingHandovers,
    openShift: handleMoCa,
    submitHandover: handleBanGiaoCa,
    confirmHandover: handleConfirmHandover,
    endShift: handleKetCa,
    cancelHandover: handleCancelHandover,
    rejectHandover: handleRejectHandover,
    reportIncident: handleReportIncident
  };
}
