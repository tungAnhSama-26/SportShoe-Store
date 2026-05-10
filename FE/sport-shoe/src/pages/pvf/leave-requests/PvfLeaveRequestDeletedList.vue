<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { Calendar, Eye, Pencil, RotateCcw, Search, Trash2 } from "lucide-vue-next";
import PvfPagination from "../../../components/pvf/PvfPagination.vue";
import { usePvfLeaveRequests } from "../../../stores/pvfLeaveRequests";

const router = useRouter();
const { deletedRequests } = usePvfLeaveRequests();

const currentPage = ref(3);
const keyword = ref("");
const period = ref("");
const status = ref("");

function goBack() {
  router.push({ name: "pvf-don-xin-phep-nghi-hoc" });
}

function goToDetail(id) {
  router.push({ name: "pvf-don-xin-phep-nghi-hoc-chi-tiet", params: { id } });
}

function goToEdit(id) {
  router.push({ name: "pvf-don-xin-phep-nghi-hoc-chinh-sua", params: { id } });
}

function resetFilters() {
  keyword.value = "";
  period.value = "";
  status.value = "";
}
</script>

<template>
  <div class="mx-auto max-w-[1220px]">
    <div class="mb-4 text-[10px] text-[#a1a8ba]">
      Học tập văn hoá <span class="mx-1">/</span> Đơn xin phép nghỉ học
    </div>

    <section class="pvf-panel px-4 py-4">
      <div class="mb-4 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <h2 class="text-[18px] font-semibold text-[#303647]">Danh sách Đơn xin phép nghỉ học đã xóa</h2>
        <button type="button" class="pvf-btn pvf-btn-secondary" @click="goBack">Quay Lại</button>
      </div>

      <div class="mb-4 flex flex-col gap-3 xl:flex-row xl:items-center">
        <label class="relative block min-w-0 flex-1">
          <Search class="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[#a4abbc]" />
          <input
            v-model="keyword"
            type="text"
            placeholder="Tìm kiếm"
            class="pvf-input pl-9"
          />
        </label>

        <label class="relative block min-w-0 xl:w-[200px]">
          <select v-model="period" class="pvf-select pr-9">
            <option value="">Chọn thời gian</option>
          </select>
          <Calendar class="pointer-events-none absolute right-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[#8e95a6]" />
        </label>

        <label class="block min-w-0 xl:w-[200px]">
          <select v-model="status" class="pvf-select">
            <option value="">Chọn trạng thái</option>
            <option value="Chấp nhận">Chấp nhận</option>
          </select>
        </label>

        <button type="button" class="pvf-btn pvf-btn-purple">
          <Search class="h-3.5 w-3.5" />
          <span>Tìm Kiếm</span>
        </button>

        <button type="button" class="pvf-btn pvf-btn-square" @click="resetFilters">
          <RotateCcw class="h-3.5 w-3.5" />
        </button>
      </div>

      <div class="overflow-x-auto">
        <table class="pvf-table min-w-[980px]">
          <thead>
            <tr>
              <th class="w-[34px]">
                <input type="checkbox" class="h-3.5 w-3.5 rounded border-[#cfd5e3]" />
              </th>
              <th class="w-[38px]">#</th>
              <th>HỌC VIÊN</th>
              <th class="w-[128px]">NGÀY GỬI ĐƠN</th>
              <th class="w-[116px]">NGÀY BĐ NGHỈ</th>
              <th class="w-[116px]">NGÀY KT NGHỈ</th>
              <th class="w-[124px]">NGÀY XÓA</th>
              <th class="w-[118px] text-center">HÀNH ĐỘNG</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="request in deletedRequests" :key="request.id">
              <td>
                <input type="checkbox" class="h-3.5 w-3.5 rounded border-[#cfd5e3]" />
              </td>
              <td class="text-[#6373ff]">{{ request.rowNumber }}</td>
              <td>{{ request.listStudentName }}</td>
              <td>{{ request.submittedAt }}</td>
              <td>{{ request.startDate }}</td>
              <td>{{ request.endDate }}</td>
              <td>{{ request.deletedAt }}</td>
              <td>
                <div class="flex items-center justify-center gap-3 text-[#606879]">
                  <button type="button" class="pvf-action-icon" @click="goToDetail(request.id)">
                    <Eye class="h-3.5 w-3.5" />
                  </button>
                  <button type="button" class="pvf-action-icon" @click="goToEdit(request.id)">
                    <Pencil class="h-3.5 w-3.5" />
                  </button>
                  <button type="button" class="pvf-action-icon">
                    <Trash2 class="h-3.5 w-3.5" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="mt-4">
        <PvfPagination v-model:current-page="currentPage" />
      </div>
    </section>
  </div>
</template>
