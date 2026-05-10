<script setup>
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Upload } from "lucide-vue-next";
import { usePvfLeaveRequests } from "../../../stores/pvfLeaveRequests";

const route = useRoute();
const router = useRouter();
const { findActiveRequestById, updateRequest } = usePvfLeaveRequests();

const currentRequest = findActiveRequestById(route.params.id) ?? findActiveRequestById(1);

const form = reactive({
  studentId: "",
  startDate: "",
  endDate: "",
  reason: "Lý do nghỉ",
  fileName: "",
});

const fileInputRef = ref(null);

function goBack() {
  router.push({ name: "pvf-don-xin-phep-nghi-hoc" });
}

function resetForm() {
  form.studentId = "";
  form.startDate = "";
  form.endDate = "";
  form.reason = "Lý do nghỉ";
  form.fileName = "";
  if (fileInputRef.value) {
    fileInputRef.value.value = "";
  }
}

function handleFileChange(event) {
  const [file] = event.target.files ?? [];
  form.fileName = file?.name ?? "";
}

function submitForm() {
  updateRequest(route.params.id, {
    studentId: form.studentId || currentRequest?.studentId || "hv-1",
    startDate: form.startDate || currentRequest?.startDate || "2025-02-01",
    endDate: form.endDate || currentRequest?.endDate || "2025-02-10",
    reason: form.reason || currentRequest?.reason || "Lý do nghỉ",
    fileName: form.fileName || currentRequest?.fileName || "don.pdf",
  });

  router.push({
    name: "pvf-don-xin-phep-nghi-hoc-chi-tiet",
    params: { id: route.params.id || 1 },
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
        <h2 class="text-[18px] font-semibold text-[#303647]">Điều chỉnh Đơn xin phép nghỉ học</h2>
        <button type="button" class="pvf-btn pvf-btn-secondary" @click="goBack">Quay Lại</button>
      </div>

      <div class="space-y-4">
        <label class="block">
          <span class="mb-1 block text-[10px] text-[#a2a8b8]">Học viên</span>
          <input
            v-model="form.studentId"
            type="text"
            placeholder="Input"
            class="pvf-input"
          />
        </label>

        <div class="grid gap-4 md:grid-cols-2">
          <label class="block">
            <span class="mb-1 block text-[10px] text-[#a2a8b8]">Ngày bắt đầu nghỉ</span>
            <input
              v-model="form.startDate"
              type="text"
              placeholder="Input"
              class="pvf-input"
            />
          </label>
          <label class="block">
            <span class="mb-1 block text-[10px] text-[#a2a8b8]">Ngày kết thúc nghỉ</span>
            <input
              v-model="form.endDate"
              type="text"
              placeholder="Input"
              class="pvf-input"
            />
          </label>
        </div>

        <label class="block">
          <span class="mb-1 block text-[10px] text-[#a2a8b8]">Ghi chú</span>
          <textarea
            v-model="form.reason"
            rows="4"
            class="pvf-textarea"
          ></textarea>
        </label>

        <div class="block">
          <span class="mb-1 block text-[10px] text-[#a2a8b8]">File scan đơn nghỉ</span>
          <label class="pvf-file-field">
            <input
              ref="fileInputRef"
              type="file"
              class="hidden"
              @change="handleFileChange"
            />
            <span class="truncate">{{ form.fileName || "Input" }}</span>
            <Upload class="h-3.5 w-3.5 text-[#8d94a6]" />
          </label>
        </div>

        <div class="flex items-center justify-center gap-3 pt-2">
          <button type="button" class="pvf-btn pvf-btn-danger" @click="submitForm">Cập Nhật</button>
          <button type="button" class="pvf-btn pvf-btn-warning" @click="resetForm">Đặt Lại</button>
        </div>
      </div>
    </section>
  </div>
</template>
