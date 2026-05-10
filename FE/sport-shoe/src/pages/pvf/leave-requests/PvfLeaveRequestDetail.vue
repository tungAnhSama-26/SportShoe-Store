<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Eye } from "lucide-vue-next";
import { usePvfLeaveRequests } from "../../../stores/pvfLeaveRequests";

const route = useRoute();
const router = useRouter();
const { findRequestById } = usePvfLeaveRequests();

const request = computed(() => findRequestById(route.params.id) ?? findRequestById(1));

function goBack() {
  router.push({ name: "pvf-don-xin-phep-nghi-hoc" });
}

function goToEdit() {
  router.push({
    name: "pvf-don-xin-phep-nghi-hoc-chinh-sua",
    params: { id: request.value?.id ?? 1 },
  });
}
</script>

<template>
  <div class="mx-auto max-w-[1220px]">
    <div class="mb-4 text-[10px] text-[#a1a8ba]">
      Học tập văn hoá <span class="mx-1">/</span> Đơn xin phép nghỉ học
    </div>

    <section class="pvf-panel px-4 py-4">
      <div class="mb-4 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <h2 class="text-[18px] font-semibold text-[#303647]">Chi tiết Đơn xin phép nghỉ học</h2>
        <div class="flex items-center gap-2">
          <button type="button" class="pvf-btn pvf-btn-secondary" @click="goBack">Quay Lại</button>
          <button type="button" class="pvf-btn pvf-btn-warning" @click="goToEdit">Chỉnh Sửa</button>
        </div>
      </div>

      <table class="w-full border-collapse overflow-hidden rounded-md border border-[#eceef5] text-[12px] text-[#606779]">
        <tbody>
          <tr class="border-b border-[#eceef5]">
            <td class="w-[230px] bg-white px-5 py-3 font-semibold text-[#4b5365]">Mã Đơn xin phép</td>
            <td class="px-5 py-3">{{ request?.code }}</td>
          </tr>
          <tr class="border-b border-[#eceef5]">
            <td class="px-5 py-3 font-semibold text-[#4b5365]">Học viên</td>
            <td class="px-5 py-3">{{ request?.studentName }}</td>
          </tr>
          <tr class="border-b border-[#eceef5]">
            <td class="px-5 py-3 font-semibold text-[#4b5365]">Ngày gửi đơn</td>
            <td class="px-5 py-3">{{ request?.submittedDisplay }}</td>
          </tr>
          <tr class="border-b border-[#eceef5]">
            <td class="px-5 py-3 font-semibold text-[#4b5365]">Ngày bắt đầu nghỉ</td>
            <td class="px-5 py-3">{{ request?.detailStartDate }}</td>
          </tr>
          <tr class="border-b border-[#eceef5]">
            <td class="px-5 py-3 font-semibold text-[#4b5365]">Ngày kết thúc nghỉ</td>
            <td class="px-5 py-3">{{ request?.detailEndDate }}</td>
          </tr>
          <tr class="border-b border-[#eceef5]">
            <td class="px-5 py-3 font-semibold text-[#4b5365]">Lý do nghỉ</td>
            <td class="px-5 py-3">{{ request?.reason }}</td>
          </tr>
          <tr class="border-b border-[#eceef5]">
            <td class="px-5 py-3 font-semibold text-[#4b5365]">Đường dẫn file scan đơn nghỉ</td>
            <td class="px-5 py-3">
              <div class="flex items-center justify-between gap-3">
                <span>{{ request?.fileName }}</span>
                <button type="button" class="pvf-action-icon">
                  <Eye class="h-3.5 w-3.5" />
                </button>
              </div>
            </td>
          </tr>
          <tr class="border-b border-[#eceef5]">
            <td class="px-5 py-3 font-semibold text-[#4b5365]">Thời gian cập nhật</td>
            <td class="px-5 py-3">{{ request?.updatedAt }}</td>
          </tr>
          <tr>
            <td class="px-5 py-3 font-semibold text-[#4b5365]">Trạng thái</td>
            <td class="px-5 py-3">
              <span class="pvf-status-chip">{{ request?.status }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>
