import { ref, readonly } from "vue";
import { 
  layCaHoatDong, 
  moCa, 
  banGiaoCa, 
  layCaChoXacNhan, 
  xacNhanBanGiao 
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

async function handleMoCa(tienDauCa, ghiChu) {
  try {
    const data = await moCa({ tienDauCa, ghiChu });
    activeShift.value = data;
    return { success: true, message: "Mở ca làm việc thành công" };
  } catch (error) {
    return { success: false, message: error?.message || "Mở ca thất bại" };
  }
}

async function handleBanGiaoCa(payload) {
  try {
    await banGiaoCa(payload);
    activeShift.value = null;
    await loadPendingHandovers();
    return { success: true, message: "Bàn giao ca thành công. Vui lòng chờ người nhận xác nhận." };
  } catch (error) {
    return { success: false, message: error?.message || "Bàn giao ca thất bại" };
  }
}

async function handleConfirmHandover(id, ghiChu) {
  try {
    await xacNhanBanGiao(id, ghiChu);
    await loadActiveShift();
    await loadPendingHandovers();
    return { success: true, message: "Nhận bàn giao và mở ca mới thành công" };
  } catch (error) {
    return { success: false, message: error?.message || "Xác nhận bàn giao thất bại" };
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
    confirmHandover: handleConfirmHandover
  };
}
