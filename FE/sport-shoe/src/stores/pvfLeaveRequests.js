import { computed, ref } from "vue";

const studentOptions = [
  { id: "hv-1", name: "Nguyễn Văn An" },
  { id: "hv-2", name: "Nguyễn Văn Bình" },
  { id: "hv-3", name: "Nguyễn Văn Dũng" },
  { id: "hv-4", name: "Nguyễn Văn Hùng" },
];

const activeRequests = ref([
  {
    id: 1,
    rowNumber: 1,
    code: "1",
    studentId: "hv-1",
    studentName: "Nguyễn Văn AN",
    listStudentName: "Nguyễn Văn An",
    submittedAt: "2025-01-01",
    submittedDisplay: "Ngày gửi đơn",
    startDate: "2025-02-01",
    endDate: "2025-02-10",
    detailStartDate: "2025-01-05",
    detailEndDate: "2025-01-05",
    reason: "Lịch cá nhân",
    fileName: "don.pdf",
    updatedAt: "2026-01-20 10:15:45",
    status: "Chấp nhận",
  },
  {
    id: 2,
    rowNumber: 1,
    code: "2",
    studentId: "hv-1",
    studentName: "Nguyễn Văn AN",
    listStudentName: "Nguyễn Văn An",
    submittedAt: "2025-01-01",
    submittedDisplay: "2025-01-01",
    startDate: "2025-02-01",
    endDate: "2025-02-10",
    detailStartDate: "2025-02-01",
    detailEndDate: "2025-02-10",
    reason: "Lịch cá nhân",
    fileName: "don.pdf",
    updatedAt: "2026-01-20 10:15:45",
    status: "Chấp nhận",
  },
  {
    id: 3,
    rowNumber: 1,
    code: "3",
    studentId: "hv-1",
    studentName: "Nguyễn Văn AN",
    listStudentName: "Nguyễn Văn An",
    submittedAt: "2025-01-01",
    submittedDisplay: "2025-01-01",
    startDate: "2025-02-01",
    endDate: "2025-02-10",
    detailStartDate: "2025-02-01",
    detailEndDate: "2025-02-10",
    reason: "Lịch cá nhân",
    fileName: "don.pdf",
    updatedAt: "2026-01-20 10:15:45",
    status: "Chấp nhận",
  },
  {
    id: 4,
    rowNumber: 1,
    code: "4",
    studentId: "hv-1",
    studentName: "Nguyễn Văn AN",
    listStudentName: "Nguyễn Văn An",
    submittedAt: "2025-01-01",
    submittedDisplay: "2025-01-01",
    startDate: "2025-02-01",
    endDate: "2025-02-10",
    detailStartDate: "2025-02-01",
    detailEndDate: "2025-02-10",
    reason: "Lịch cá nhân",
    fileName: "don.pdf",
    updatedAt: "2026-01-20 10:15:45",
    status: "Chấp nhận",
  },
  {
    id: 5,
    rowNumber: 1,
    code: "5",
    studentId: "hv-1",
    studentName: "Nguyễn Văn AN",
    listStudentName: "Nguyễn Văn An",
    submittedAt: "2025-01-01",
    submittedDisplay: "2025-01-01",
    startDate: "2025-02-01",
    endDate: "2025-02-10",
    detailStartDate: "2025-02-01",
    detailEndDate: "2025-02-10",
    reason: "Lịch cá nhân",
    fileName: "don.pdf",
    updatedAt: "2026-01-20 10:15:45",
    status: "Chấp nhận",
  },
  {
    id: 6,
    rowNumber: 1,
    code: "6",
    studentId: "hv-1",
    studentName: "Nguyễn Văn AN",
    listStudentName: "Nguyễn Văn An",
    submittedAt: "2025-01-01",
    submittedDisplay: "2025-01-01",
    startDate: "2025-02-01",
    endDate: "2025-02-10",
    detailStartDate: "2025-02-01",
    detailEndDate: "2025-02-10",
    reason: "Lịch cá nhân",
    fileName: "don.pdf",
    updatedAt: "2026-01-20 10:15:45",
    status: "Chấp nhận",
  },
  {
    id: 7,
    rowNumber: 1,
    code: "7",
    studentId: "hv-1",
    studentName: "Nguyễn Văn AN",
    listStudentName: "Nguyễn Văn An",
    submittedAt: "2025-01-01",
    submittedDisplay: "2025-01-01",
    startDate: "2025-02-01",
    endDate: "2025-02-10",
    detailStartDate: "2025-02-01",
    detailEndDate: "2025-02-10",
    reason: "Lịch cá nhân",
    fileName: "don.pdf",
    updatedAt: "2026-01-20 10:15:45",
    status: "Chấp nhận",
  },
]);

const deletedRequests = ref([
  {
    id: 101,
    rowNumber: 1,
    code: "101",
    studentId: "hv-1",
    studentName: "Nguyễn Văn AN",
    listStudentName: "Nguyễn Văn An",
    submittedAt: "2025-01-01",
    startDate: "2025-02-01",
    endDate: "2025-02-10",
    deletedAt: "2025-03-02 7:20",
    reason: "Lịch cá nhân",
    fileName: "don.pdf",
    updatedAt: "2026-01-20 10:15:45",
    status: "Chấp nhận",
  },
]);

const stats = [
  {
    title: "Tổng số đơn",
    value: "21",
    accent: "purple",
  },
  {
    title: "Số đơn mới",
    value: "20",
    note: "(95%)",
    accent: "orange",
  },
  {
    title: "Số đơn chưa xử lý",
    value: "1",
    note: "(5%)",
    accent: "amber",
  },
  {
    title: "Số đơn đã xử lý",
    value: "21",
    note: "(5%)",
    accent: "gold",
  },
];

let nextRequestId = 200;

function getStudentNameById(studentId) {
  return studentOptions.find((student) => student.id === studentId)?.name ?? "Nguyễn Văn An";
}

function buildRequestPayload(formState = {}) {
  const studentName = getStudentNameById(formState.studentId);
  return {
    id: nextRequestId++,
    rowNumber: 1,
    code: String(nextRequestId - 1),
    studentId: formState.studentId || "hv-1",
    studentName: studentName.toUpperCase(),
    listStudentName: studentName,
    submittedAt: "2025-01-01",
    submittedDisplay: "2025-01-01",
    startDate: formState.startDate || "2025-02-01",
    endDate: formState.endDate || "2025-02-10",
    detailStartDate: formState.startDate || "2025-01-05",
    detailEndDate: formState.endDate || "2025-01-05",
    reason: formState.reason || "Lịch cá nhân",
    fileName: formState.fileName || "don.pdf",
    updatedAt: "2026-01-20 10:15:45",
    status: "Chấp nhận",
  };
}

function findActiveRequestById(id) {
  return activeRequests.value.find((request) => String(request.id) === String(id)) ?? null;
}

function findRequestById(id) {
  return (
    activeRequests.value.find((request) => String(request.id) === String(id))
    ?? deletedRequests.value.find((request) => String(request.id) === String(id))
    ?? null
  );
}

function createRequest(formState) {
  const request = buildRequestPayload(formState);
  activeRequests.value.unshift(request);
  return request;
}

function updateRequest(id, formState) {
  const target = findActiveRequestById(id);
  if (!target) {
    return null;
  }

  const studentName = getStudentNameById(formState.studentId || target.studentId);
  target.studentId = formState.studentId || target.studentId;
  target.studentName = studentName.toUpperCase();
  target.listStudentName = studentName;
  target.startDate = formState.startDate || target.startDate;
  target.endDate = formState.endDate || target.endDate;
  target.detailStartDate = formState.startDate || target.detailStartDate;
  target.detailEndDate = formState.endDate || target.detailEndDate;
  target.reason = formState.reason || target.reason;
  target.fileName = formState.fileName || target.fileName;
  target.updatedAt = "2026-01-20 10:15:45";
  return target;
}

function softDeleteRequest(id) {
  const index = activeRequests.value.findIndex((request) => String(request.id) === String(id));
  if (index === -1) {
    return;
  }

  const [request] = activeRequests.value.splice(index, 1);
  deletedRequests.value.unshift({
    ...request,
    deletedAt: "2025-03-02 7:20",
  });
}

export function usePvfLeaveRequests() {
  const totalActiveRequests = computed(() => activeRequests.value.length);
  const totalDeletedRequests = computed(() => deletedRequests.value.length);

  return {
    studentOptions,
    activeRequests,
    deletedRequests,
    stats,
    totalActiveRequests,
    totalDeletedRequests,
    createRequest,
    updateRequest,
    softDeleteRequest,
    findRequestById,
    findActiveRequestById,
  };
}
