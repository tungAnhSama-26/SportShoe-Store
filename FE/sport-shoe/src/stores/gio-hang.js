import { reactive } from "vue";
import { layGioHang } from "../services/gio-hang";

// Store nhẹ giữ số lượng giỏ hàng để hiển thị badge trên header.
export const gioHangStore = reactive({
  soLuong: 0,
  async lamMoi() {
    try {
      const gio = await layGioHang();
      this.soLuong = gio?.tongSoLuong ?? 0;
    } catch {
      this.soLuong = 0;
    }
  },
  datSoLuong(n) {
    this.soLuong = Number(n) || 0;
  },
});
