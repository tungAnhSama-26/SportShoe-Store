<script setup>
import { defineAsyncComponent } from "vue";
import { useChiTietHoaDon } from "../useChiTietHoaDon";
import { provideInvoiceDetailContext } from "./composables/useInvoiceDetailContext";
import DetailHeader from "./components/DetailHeader.vue";
import OrderStatusCard from "./components/OrderStatusCard.vue";
import PaymentSummaryCard from "./components/PaymentSummaryCard.vue";
import CustomerInfoCard from "./components/CustomerInfoCard.vue";
import ShippingInfoCard from "./components/ShippingInfoCard.vue";
import PaymentHistoryPanel from "./components/PaymentHistoryPanel.vue";
import ProductListSection from "./components/ProductListSection.vue";

const ChinhSuaGiaoHangModal = defineAsyncComponent(() =>
  import("../../../../components/common/ChinhSuaGiaoHangModal.vue"),
);
const CodPaymentModal = defineAsyncComponent(() => import("./modals/CodPaymentModal.vue"));
const RefundPaymentModal = defineAsyncComponent(() => import("./modals/RefundPaymentModal.vue"));
const ConfirmStatusModal = defineAsyncComponent(() => import("./modals/ConfirmStatusModal.vue"));
const ConfirmCancelModal = defineAsyncComponent(() => import("./modals/ConfirmCancelModal.vue"));
const OperationHistoryModal = defineAsyncComponent(() => import("./modals/OperationHistoryModal.vue"));
const ProductEditModal = defineAsyncComponent(() => import("./modals/ProductEditModal.vue"));
const UpdateOrderModal = defineAsyncComponent(() => import("./modals/UpdateOrderModal.vue"));

const invoiceDetail = useChiTietHoaDon();
provideInvoiceDetailContext(invoiceDetail);

const {
  Card,
  hoaDon,
  dangTai,
  loiTrang,
  hienModalGiaoHang,
  hienModalThanhToanCod,
  hienModalHoanTien,
  hienModalXacNhan,
  hienModalXacNhanHuy,
  hienModalLichSu,
  hienModalSanPham,
  hienModalThongTin,
  dangLuuGiaoHang,
  diaChiDaLuu,
  formThongTin,
  handleLuuGiaoHang,
} = invoiceDetail;
</script>

<template>
  <div class="invoice-flat space-y-6 pb-10">
    <DetailHeader />

    <Card v-if="dangTai" class="p-10 text-center text-sm text-slate-400">
      Đang tải chi tiết đơn hàng...
    </Card>
    <Card v-else-if="loiTrang || !hoaDon" class="p-10 text-center">
      <h2 class="text-2xl font-bold text-slate-800">Không tìm thấy đơn hàng</h2>
      <p class="mt-3 text-sm text-slate-400">
        {{ loiTrang || "Đơn hàng không tồn tại." }}
      </p>
    </Card>
    <template v-else>
      <section class="grid items-stretch gap-3 xl:grid-cols-[1fr_1fr_0.95fr]">
        <OrderStatusCard />
        <PaymentSummaryCard />
      </section>

      <section class="grid gap-3 xl:grid-cols-[1fr_1fr_0.95fr]">
        <CustomerInfoCard />
        <ShippingInfoCard />
        <PaymentHistoryPanel />
      </section>

      <ProductListSection />
    </template>

    <CodPaymentModal v-if="hienModalThanhToanCod" />
    <RefundPaymentModal v-if="hienModalHoanTien" />
    <ConfirmStatusModal v-if="hienModalXacNhan" />
    <ConfirmCancelModal v-if="hienModalXacNhanHuy" />
    <OperationHistoryModal v-if="hienModalLichSu" />
    <ProductEditModal v-if="hienModalSanPham" />
    <UpdateOrderModal v-if="hienModalThongTin" />

    <ChinhSuaGiaoHangModal
      v-if="hienModalGiaoHang"
      v-model="hienModalGiaoHang"
      title="Chỉnh sửa thông tin nhận hàng"
      :initial-data="{
        tenNguoiNhan: formThongTin.tenKhachHang,
        sdtNguoiNhan: formThongTin.soDienThoai,
        diaChiGiaoHang: formThongTin.diaChi,
      }"
      :saved-addresses="diaChiDaLuu"
      :saving="dangLuuGiaoHang"
      @save="handleLuuGiaoHang"
    />
  </div>
</template>

<style scoped>
.invoice-flat :deep([class*="rounded-"]:not(.rounded-full)) {
  border-radius: 6px !important;
}
</style>
