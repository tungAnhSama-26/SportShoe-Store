<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import anhMacDinh from "../assets/login-shoe.png";
import Card from "../components/ui/Card.vue";
import { layTatCaSanPham } from "../services/san-pham";
import { dinhDangTienViet } from "../utils/dinhDangTien";
import { resolveHinhAnh } from "../utils/resolve-image";

const route = useRoute();
const tatCaSanPham = ref([]);
const dangTai = ref(true);
const sapXep = ref("moi");
const giaMin = ref("");
const giaMax = ref("");
const tuKhoaTen = ref("");

const CAU_HINH_TRANG = {
  "hang-moi": {
    tieuDe: "Hàng mới",
    moTa: "Khám phá những mẫu giày mới được cập nhật tại SportShoe.",
    cheDo: "hang-moi",
  },
  nam: {
    tieuDe: "Giày Nam",
    moTa: "Những thiết kế dành cho nam, từ luyện tập đến phong cách hằng ngày.",
    cheDo: "nam",
  },
  nu: {
    tieuDe: "Giày Nữ",
    moTa: "Bộ sưu tập giày nữ năng động, thoải mái và dễ phối đồ.",
    cheDo: "nu",
  },
  "tre-em": {
    tieuDe: "Giày Trẻ em",
    moTa: "Các mẫu giày được phân loại dành cho trẻ em.",
    cheDo: "tre-em",
  },
  "giam-gia": {
    tieuDe: "Sản phẩm giảm giá",
    moTa: "Săn các mẫu giày đang có mức giá ưu đãi tại SportShoe.",
    cheDo: "giam-gia",
  },
};

const cauHinhTrang = computed(() =>
  CAU_HINH_TRANG[String(route.name)] || {
    tieuDe: "Tất cả sản phẩm",
    moTa: "Tìm mẫu giày phù hợp với phong cách và nhu cầu của bạn.",
    cheDo: "tat-ca",
  }
);

const NHOM_LOC = [
  { khoa: "thuongHieu", nhan: "Hãng" },
  { khoa: "mauSac", nhan: "Màu sắc", nhieu: true },
  { khoa: "kichCo", nhan: "Kích cỡ", nhieu: true },
  { khoa: "loaiGiay", nhan: "Loại giày" },
  { khoa: "gioiTinhNhan", nhan: "Giới tính" },
  { khoa: "chatLieu", nhan: "Chất liệu" },
  { khoa: "deGiay", nhan: "Đế giày" },
  { khoa: "coGiay", nhan: "Cổ giày" },
  { khoa: "congNgheDem", nhan: "Công nghệ đệm" },
];

const taoBoLocRong = () => Object.fromEntries(NHOM_LOC.map((nhom) => [nhom.khoa, []]));
const boLoc = ref(taoBoLocRong());

function boDau(giaTri) {
  return String(giaTri || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim();
}

function apLocTuRoute() {
  boLoc.value = taoBoLocRong();
  giaMin.value = "";
  giaMax.value = "";
  sapXep.value = "moi";
  const hang = route.query.hang;
  if (hang) boLoc.value.thuongHieu = [String(hang)];
  tuKhoaTen.value = route.query.q ? String(route.query.q) : "";
}

watch(
  () => [route.name, route.query.hang, route.query.q],
  apLocTuRoute
);

onMounted(async () => {
  apLocTuRoute();
  try {
    tatCaSanPham.value = await layTatCaSanPham();
  } catch {
    tatCaSanPham.value = [];
  } finally {
    dangTai.value = false;
  }
});

const danhSachTheoTrang = computed(() => {
  const cheDo = cauHinhTrang.value.cheDo;
  if (cheDo === "nam") return tatCaSanPham.value.filter((sp) => boDau(sp.gioiTinhNhan) === "nam");
  if (cheDo === "nu") return tatCaSanPham.value.filter((sp) => boDau(sp.gioiTinhNhan) === "nu");
  if (cheDo === "tre-em") {
    return tatCaSanPham.value.filter((sp) =>
      boDau(`${sp.ten} ${sp.loaiGiay}`).includes("tre em")
    );
  }
  if (cheDo === "giam-gia") return tatCaSanPham.value.filter((sp) => Boolean(sp.giaCu));
  return tatCaSanPham.value;
});

function giaTriDuyNhat(nhom) {
  const tapGiaTri = new Set();
  danhSachTheoTrang.value.forEach((sanPham) => {
    if (nhom.nhieu) {
      (sanPham[nhom.khoa] || []).forEach((giaTri) => giaTri && tapGiaTri.add(giaTri));
    } else if (sanPham[nhom.khoa]) {
      tapGiaTri.add(sanPham[nhom.khoa]);
    }
  });
  return Array.from(tapGiaTri).sort((a, b) =>
    String(a).localeCompare(String(b), "vi", { numeric: true })
  );
}

const cacNhomCoGiaTri = computed(() =>
  NHOM_LOC.map((nhom) => ({ ...nhom, giaTri: giaTriDuyNhat(nhom) })).filter(
    (nhom) => nhom.giaTri.length
  )
);

const dangLoc = computed(
  () =>
    tuKhoaTen.value.trim() !== "" ||
    giaMin.value !== "" ||
    giaMax.value !== "" ||
    NHOM_LOC.some((nhom) => boLoc.value[nhom.khoa].length)
);

const danhSachLoc = computed(() => {
  let danhSach = danhSachTheoTrang.value;

  for (const nhom of NHOM_LOC) {
    const daChon = boLoc.value[nhom.khoa];
    if (daChon.length) {
      danhSach = nhom.nhieu
        ? danhSach.filter((sanPham) =>
            daChon.some((giaTri) => (sanPham[nhom.khoa] || []).includes(giaTri))
          )
        : danhSach.filter((sanPham) => daChon.includes(sanPham[nhom.khoa]));
    }
  }

  const min = giaMin.value === "" ? null : Number(giaMin.value);
  const max = giaMax.value === "" ? null : Number(giaMax.value);
  if (Number.isFinite(min)) danhSach = danhSach.filter((sanPham) => sanPham.gia >= min);
  if (Number.isFinite(max)) danhSach = danhSach.filter((sanPham) => sanPham.gia <= max);

  const tuKhoa = boDau(tuKhoaTen.value);
  if (tuKhoa) danhSach = danhSach.filter((sanPham) => boDau(sanPham.ten).includes(tuKhoa));

  danhSach = [...danhSach];
  if (sapXep.value === "gia-tang") danhSach.sort((a, b) => a.gia - b.gia);
  else if (sapXep.value === "gia-giam") danhSach.sort((a, b) => b.gia - a.gia);
  else danhSach.sort((a, b) => Number(b.id || 0) - Number(a.id || 0));
  return danhSach;
});

const tieuDeTrang = computed(() =>
  tuKhoaTen.value.trim() ? `Kết quả cho “${tuKhoaTen.value.trim()}”` : cauHinhTrang.value.tieuDe
);

function xoaLoc() {
  boLoc.value = taoBoLocRong();
  giaMin.value = "";
  giaMax.value = "";
  tuKhoaTen.value = "";
}

function xuLyAnhLoi(event) {
  if (event.target.src !== anhMacDinh) event.target.src = anhMacDinh;
}
</script>

<template>
  <main class="min-h-screen bg-slate-50 pb-20">
    <section class="bg-black px-6 py-16 text-white lg:px-10">
      <div class="mx-auto max-w-7xl">
        <p class="mb-3 text-sm font-semibold uppercase tracking-[0.22em] text-red-400">SportShoe</p>
        <h1 class="mb-4 text-4xl font-bold tracking-tight md:text-5xl">{{ tieuDeTrang }}</h1>
        <p class="max-w-2xl text-sm leading-7 text-slate-300">{{ cauHinhTrang.moTa }}</p>
      </div>
    </section>

    <div class="mx-auto mt-10 grid max-w-7xl grid-cols-1 gap-10 px-6 lg:grid-cols-[280px_1fr] lg:px-10">
      <aside class="space-y-7">
        <div class="flex items-center justify-between">
          <h2 class="text-lg font-bold text-slate-900">Bộ lọc</h2>
          <button v-if="dangLoc" type="button" class="text-xs font-semibold text-primary hover:underline" @click="xoaLoc">
            Xóa lọc
          </button>
        </div>

        <div>
          <h3 class="mb-3 border-b border-slate-200 pb-2 text-sm font-bold text-slate-900">Khoảng giá (đ)</h3>
          <div class="flex items-center gap-2">
            <input v-model="giaMin" type="number" min="0" placeholder="Giá từ" class="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" />
            <span class="text-slate-400">—</span>
            <input v-model="giaMax" type="number" min="0" placeholder="Giá đến" class="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" />
          </div>
        </div>

        <div v-for="nhom in cacNhomCoGiaTri" :key="nhom.khoa">
          <h3 class="mb-3 border-b border-slate-200 pb-2 text-sm font-bold text-slate-900">{{ nhom.nhan }}</h3>
          <div class="max-h-56 space-y-2.5 overflow-y-auto pr-1 text-sm font-medium text-slate-600">
            <label v-for="giaTri in nhom.giaTri" :key="giaTri" class="flex cursor-pointer items-center gap-3 transition-colors hover:text-primary">
              <input v-model="boLoc[nhom.khoa]" type="checkbox" :value="giaTri" class="rounded border-slate-300 text-primary focus:ring-primary/30" />
              {{ giaTri }}
            </label>
          </div>
        </div>
      </aside>

      <section>
        <div class="mb-6 flex flex-wrap items-center justify-between gap-3">
          <p class="text-sm text-slate-500">Hiển thị <span class="font-bold text-slate-900">{{ danhSachLoc.length }}</span> sản phẩm</p>
          <select v-model="sapXep" aria-label="Sắp xếp sản phẩm" class="rounded-lg border-slate-200 bg-white text-sm focus:border-primary focus:ring-primary/30">
            <option value="moi">Mới nhất</option>
            <option value="gia-tang">Giá tăng dần</option>
            <option value="gia-giam">Giá giảm dần</option>
          </select>
        </div>

        <div v-if="dangTai" class="py-20 text-center text-sm text-slate-400">Đang tải sản phẩm...</div>
        <div v-else-if="!danhSachLoc.length" class="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-20 text-center">
          <p class="font-semibold text-slate-700">Chưa có sản phẩm phù hợp</p>
          <p class="mt-2 text-sm text-slate-400">Vui lòng thử một danh mục hoặc bộ lọc khác.</p>
        </div>

        <div v-else class="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3">
          <RouterLink v-for="sanPham in danhSachLoc" :key="sanPham.id" :to="{ name: 'san-pham-chi-tiet', params: { id: sanPham.id } }" class="block">
            <Card class="group flex h-full cursor-pointer flex-col overflow-hidden border-none bg-white transition-all duration-300 hover:-translate-y-1 hover:shadow-xl">
              <div class="relative aspect-square overflow-hidden bg-slate-100">
                <img :src="resolveHinhAnh(sanPham.hinhAnh)" :alt="sanPham.ten" class="h-full w-full object-cover transition-transform duration-700 group-hover:scale-105" @error="xuLyAnhLoi" />
                <span v-if="sanPham.nhan" class="absolute left-3 top-3 rounded-md bg-red-600 px-2.5 py-1 text-xs font-extrabold text-white shadow-md">{{ sanPham.nhan }}</span>
              </div>
              <div class="flex flex-1 flex-col p-4">
                <p class="mb-1 text-xs font-medium text-slate-400">{{ sanPham.thuongHieu }}</p>
                <h3 class="mb-2 line-clamp-2 text-base font-bold text-slate-900 transition-colors group-hover:text-primary">{{ sanPham.ten }}</h3>
                <div class="mt-auto flex items-end gap-2">
                  <p class="text-lg font-bold text-primary">{{ dinhDangTienViet(sanPham.gia) }}</p>
                  <p v-if="sanPham.giaCu" class="pb-1 text-xs text-slate-400 line-through">{{ dinhDangTienViet(sanPham.giaCu) }}</p>
                </div>
                <div class="mt-1.5 flex items-center gap-1.5">
                  <div class="flex text-xs">
                    <span v-for="i in 5" :key="i" :class="i <= Math.round(sanPham.soSao) ? 'text-amber-400' : 'text-slate-300'">★</span>
                  </div>
                  <span class="text-xs text-slate-400">{{ sanPham.soDanhGia ? `${sanPham.soSao.toFixed(1)} (${sanPham.soDanhGia})` : "Chưa có đánh giá" }}</span>
                </div>
              </div>
            </Card>
          </RouterLink>
        </div>
      </section>
    </div>
  </main>
</template>
