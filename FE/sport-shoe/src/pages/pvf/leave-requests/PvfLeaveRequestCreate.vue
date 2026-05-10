<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ChevronDown, Upload } from "lucide-vue-next";
import { usePvfLeaveRequests } from "../../../stores/pvfLeaveRequests";

const router = useRouter();
const { studentOptions, createRequest } = usePvfLeaveRequests();

const form = reactive({
  studentId: "",
  startDate: "",
  endDate: "",
  reason: "",
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
  form.reason = "";
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
  const createdRequest = createRequest({
    studentId: form.studentId || "hv-1",
    startDate: form.startDate || "2025-02-01",
    endDate: form.endDate || "2025-02-10",
    reason: form.reason || "Lý do nghỉ",
    fileName: form.fileName || "don.pdf",
  });
  router.push({
    name: "pvf-don-xin-phep-nghi-hoc-chi-tiet",
    params: { id: createdRequest.id },
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
        <h2 class="text-[18px] font-semibold text-[#303647]">Thêm mới Đơn xin phép nghỉ học</h2>
        <button type="button" class="pvf-btn pvf-btn-secondary" @click="goBack">Quay Lại</button>
      </div>

      <div class="space-y-4">
        <label class="relative block">
          <select v-model="form.studentId" class="pvf-select pr-9">
            <option value="">Học viên</option>
            <option v-for="student in studentOptions" :key="student.id" :value="student.id">
              {{ student.name }}
            </option>
          </select>
          <ChevronDown class="pointer-events-none absolute right-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[#98a0b1]" />
        </label>

        <div class="grid gap-4 md:grid-cols-2">
          <input
            v-model="form.startDate"
            type="text"
            placeholder="Ngày bắt đầu nghỉ"
            class="pvf-input"
          />
          <input
            v-model="form.endDate"
            type="text"
            placeholder="Ngày kết thúc nghỉ"
            class="pvf-input"
          />
        </div>

        <textarea
          v-model="form.reason"
          rows="4"
          placeholder="Lý do nghỉ"
          class="pvf-textarea"
        ></textarea>

        <label class="pvf-file-field">
          <input
            ref="fileInputRef"
            type="file"
            class="hidden"
            @change="handleFileChange"
          />
          <span class="truncate">{{ form.fileName || "File scan đơn nghỉ" }}</span>
          <Upload class="h-3.5 w-3.5 text-[#8d94a6]" />
        </label>

        <div class="flex items-center justify-center gap-3 pt-2">
          <button type="button" class="pvf-btn pvf-btn-danger" @click="submitForm">Thêm Mới</button>
          <button type="button" class="pvf-btn pvf-btn-warning" @click="resetForm">Đặt Lại</button>
        </div>
      </div>
    </section>
  </div>
</template>
