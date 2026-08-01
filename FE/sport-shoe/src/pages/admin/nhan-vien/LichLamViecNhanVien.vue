<script setup>
import { computed, onMounted, ref } from "vue";
import { layLichLamViec } from "../../../services/lich-lam.js";
import { useAdminSession } from "../../../composable/useAdminSession.js";

const { adminSession } = useAdminSession();
const dangTai = ref(false);
const lichLam = ref([]);

function dinhDangNgay(date) {
  return date.toISOString().slice(0, 10);
}

const tuNgay = computed(() => {
  const date = new Date();
  const day = date.getDay() || 7;
  date.setDate(date.getDate() - day + 1);
  return dinhDangNgay(date);
});

const denNgay = computed(() => {
  const date = new Date(`${tuNgay.value}T00:00:00`);
  date.setDate(date.getDate() + 6);
  return dinhDangNgay(date);
});

const lichCuaToi = computed(() =>
  lichLam.value.filter((item) => String(item.nhanVienId) === String(adminSession.value.id)),
);

function tenCa(ca) {
  return { sang: "Ca sáng", chieu: "Ca chiều", toi: "Ca tối" }[ca] || ca || "Nghỉ";
}

async function taiLich() {
  dangTai.value = true;
  try {
    const data = await layLichLamViec(tuNgay.value, denNgay.value);
    lichLam.value = Array.isArray(data) ? data : [];
  } finally {
    dangTai.value = false;
  }
}

onMounted(taiLich);
</script>

<template>
  <section class="space-y-5">
    <header>
      <h1 class="text-2xl font-bold text-slate-900">Lịch làm việc của tôi</h1>
      <p class="mt-1 text-sm text-slate-500">Tuần {{ tuNgay }} đến {{ denNgay }}</p>
    </header>

    <div v-if="dangTai" class="rounded-2xl bg-white p-8 text-center text-slate-500">Đang tải lịch làm việc...</div>
    <div v-else-if="!lichCuaToi.length" class="rounded-2xl bg-white p-8 text-center text-slate-500">Bạn chưa được phân lịch trong tuần này.</div>
    <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <article v-for="item in lichCuaToi" :key="item.id" class="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
        <p class="text-sm font-semibold text-slate-500">{{ item.ngay }}</p>
        <p class="mt-2 text-lg font-bold text-slate-900">{{ tenCa(item.ca) }}</p>
      </article>
    </div>
  </section>
</template>
