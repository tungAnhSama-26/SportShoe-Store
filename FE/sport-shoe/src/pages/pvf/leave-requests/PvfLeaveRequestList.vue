<script setup>
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import {
  Calendar,
  Eye,
  FileText,
  List,
  Pencil,
  Plus,
  RotateCcw,
  Search,
  Trash2,
} from "lucide-vue-next";
import PvfPagination from "../../../components/pvf/PvfPagination.vue";
import { usePvfLeaveRequests } from "../../../stores/pvfLeaveRequests";

const router = useRouter();
const { activeRequests, stats, softDeleteRequest } = usePvfLeaveRequests();

const currentPage = ref(3);
const keyword = ref("");
const period = ref("");
const status = ref("");

const rows = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase();
  return activeRequests.value.filter((request) => {
    const matchesKeyword = !normalizedKeyword || request.listStudentName.toLowerCase().includes(normalizedKeyword);
    const matchesStatus = !status.value || request.status === status.value;
    return matchesKeyword && matchesStatus;
  });
});

function goToDetail(id) {
  router.push({ name: "pvf-don-xin-phep-nghi-hoc-chi-tiet", params: { id } });
}

function goToEdit(id) {
  router.push({ name: "pvf-don-xin-phep-nghi-hoc-chinh-sua", params: { id } });
}

function goToCreate() {
  router.push({ name: "pvf-don-xin-phep-nghi-hoc-them-moi" });
}

function goToDeletedList() {
  router.push({ name: "pvf-don-xin-phep-nghi-hoc-da-xoa" });
}

function resetFilters() {
  keyword.value = "";
  period.value = "";
  status.value = "";
}

function removeRequest(id) {
  softDeleteRequest(id);
}

function statIconClass(accent) {
  return {
    purple: "bg-[#f1efff] text-[#7266ff]",
    orange: "bg-[#fff2ec] text-[#ff7a45]",
    amber: "bg-[#fff8e8] text-[#f0ab1e]",
    gold: "bg-[#fff6db] text-[#f3b11d]",
  }[accent] ?? "bg-[#f5f7fb] text-[#7b8394]";
}
</script>

<template>
  <div class="mx-auto max-w-[1220px]">
    <div class="mb-4 text-[10px] text-[#a1a8ba]">
      Học tập văn hoá <span class="mx-1">/</span> Đơn xin phép nghỉ học
    </div>

    <div class="mb-4 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      <div
        v-for="item in stats"
        :key="item.title"
        class="pvf-panel px-4 py-4"
      >
        <div class="mb-3 flex items-start justify-between gap-3">
          <div>
            <p class="text-[11px] text-[#8e95a6]">{{ item.title }}</p>
            <div class="mt-3 flex items-end gap-2">
              <span class="text-[24px] font-bold leading-none text-[#2f3545]">{{ item.value }}</span>
              <span v-if="item.note" class="text-[10px] font-semibold text-[#5ad16e]">{{ item.note }}</span>
            </div>
          </div>
          <div class="flex h-7 w-7 items-center justify-center rounded-md" :class="statIconClass(item.accent)">
            <FileText class="h-3.5 w-3.5" />
          </div>
        </div>
      </div>
    </div>

    <section class="pvf-panel px-4 py-4">
      <div class="mb-4 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <h2 class="text-[18px] font-semibold text-[#303647]">Danh sách Đơn xin phép nghỉ học</h2>
        <div class="flex flex-wrap items-center gap-2">
          <button type="button" class="pvf-btn pvf-btn-slate" @click="goToDeletedList">
            <List class="h-3.5 w-3.5" />
            <span>Danh Sách Đã Xóa</span>
          </button>
          <button type="button" class="pvf-btn pvf-btn-danger" @click="goToCreate">
            <Plus class="h-3.5 w-3.5" />
            <span>Thêm Mới</span>
          </button>
        </div>
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
            <option value="thang-1">Tháng 1</option>
            <option value="thang-2">Tháng 2</option>
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
              <th class="w-[118px]">TRẠNG THÁI</th>
              <th class="w-[118px] text-center">HÀNH ĐỘNG</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="request in rows" :key="request.id">
              <td>
                <input type="checkbox" class="h-3.5 w-3.5 rounded border-[#cfd5e3]" />
              </td>
              <td class="text-[#6373ff]">{{ request.rowNumber }}</td>
              <td>{{ request.listStudentName }}</td>
              <td>{{ request.submittedAt }}</td>
              <td>{{ request.startDate }}</td>
              <td>{{ request.endDate }}</td>
              <td>
                <span class="pvf-status-chip">{{ request.status }}</span>
              </td>
              <td>
                <div class="flex items-center justify-center gap-3 text-[#606879]">
                  <button type="button" class="pvf-action-icon" @click="goToDetail(request.id)">
                    <Eye class="h-3.5 w-3.5" />
                  </button>
                  <button type="button" class="pvf-action-icon" @click="goToEdit(request.id)">
                    <Pencil class="h-3.5 w-3.5" />
                  </button>
                  <button type="button" class="pvf-action-icon" @click="removeRequest(request.id)">
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
