<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  ArrowLeft,
  Banknote,
  CheckCircle2,
  ClipboardCheck,
  Clock3,
  PackageCheck,
  RefreshCw,
  Truck,
  UserRound,
  X,
  XCircle,
} from "lucide-vue-next";
import Badge from "../../../components/ui/Badge.vue";
import Button from "../../../components/ui/Button.vue";
import Card from "../../../components/ui/Card.vue";
import Table from "../../../components/ui/Table.vue";
import {
  batDauKiemTraHang,
  capNhatKiemTraHang,
  duyetPhieuTraHang,
  hoanTienTraHang,
  huyPhieuTraHang,
  layChiTietTraHang,
  tuChoiTraHang,
  xacNhanGuiHangTra,
  xacNhanNhanHangTra,
} from "../../../services/tra-hang";
import { getDisplayErrorMessage } from "../../../utils/error-message";
import { showConfirm, showError, showSuccess } from "../../../utils/alert";

const route = useRoute();
const router = useRouter();
const phieu = ref(null);
const dangTai = ref(false);
const dangXuLy = ref(false);
const loiTrang = ref("");
const modal = ref("");
const formDuyet = ref({ nhanHangTrucTiep: false, ghiChu: "" });
const formVanChuyen = ref({ donViVanChuyen: "", maVanDonHoan: "", ghiChu: "" });
const formTuChoi = ref({ lyDo: "" });
const formHoanTien = ref({ hinhThucHoan: 2, maGiaoDich: "", ghiChu: "" });
const formKiemTra = ref({ sanPhams: [], ghiChu: "" });

const trangThai = computed(() => Number(phieu.value?.trangThai || 0));
const coTheDuyet = computed(() => trangThai.value === 1);
const coTheXacNhanGui = computed(() => trangThai.value === 2);
const coTheXacNhanNhan = computed(() => trangThai.value === 3);
const coTheBatDauKiemTra = computed(() => trangThai.value === 4);
const coTheKiemTra = computed(() => trangThai.value === 5);
const coTheHoanTien = computed(() => trangThai.value === 6);
const coTheHuy = computed(() => [1, 2, 10].includes(trangThai.value));

const cacBuoc = [
  { id: 1, ten: "Chờ duyệt" },
  { id: 2, ten: "Gửi hàng" },
  { id: 4, ten: "Đã nhận hàng" },
  { id: 5, ten: "Kiểm tra" },
  { id: 6, ten: "Chờ hoàn tiền" },
  { id: 7, ten: "Hoàn tất" },
];

const buocHienTai = computed(() => {
  const map = { 1: 0, 2: 1, 3: 1, 4: 2, 5: 3, 6: 4, 7: 5 };
  return map[trangThai.value] ?? 0;
});

function dinhDangTien(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(Number(value || 0));
}

function dinhDangNgay(value) {
  if (!value) return "Chưa cập nhật";
  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(new Date(value));
}

function badgeVariant(value) {
  if (value === 7) return "success";
  if ([8, 9, 10].includes(value)) return "danger";
  return "warning";
}

async function taiChiTiet() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    phieu.value = await layChiTietTraHang(route.params.id);
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(error, "Không thể tải phiếu trả hàng");
  } finally {
    dangTai.value = false;
  }
}

async function thucHien(noiDungThanhCong, callback) {
  if (dangXuLy.value) return;
  dangXuLy.value = true;
  try {
    phieu.value = await callback();
    modal.value = "";
    showSuccess(noiDungThanhCong);
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Không thể cập nhật phiếu trả hàng"));
  } finally {
    dangXuLy.value = false;
  }
}

function moModalDuyet() {
  formDuyet.value = { nhanHangTrucTiep: false, ghiChu: "" };
  modal.value = "duyet";
}

function moModalGuiHang() {
  formVanChuyen.value = {
    donViVanChuyen: phieu.value?.donViVanChuyen || "",
    maVanDonHoan: phieu.value?.maVanDonHoan || "",
    ghiChu: "",
  };
  modal.value = "gui-hang";
}

function moModalKiemTra() {
  formKiemTra.value = {
    sanPhams: (phieu.value?.chiTiet || []).map((item) => ({
      chiTietTraHangId: item.id,
      soLuongNhan: item.soLuongTra,
      soLuongChapNhan: item.soLuongTra,
      tinhTrangSanPham: "",
      nhapLaiTonKho: true,
      soLuongTra: item.soLuongTra,
      tenSanPham: item.tenSanPham,
      maBienThe: item.maBienThe,
    })),
    ghiChu: "",
  };
  modal.value = "kiem-tra";
}

function moModalHoanTien() {
  formHoanTien.value = {
    hinhThucHoan: phieu.value?.hinhThucHoan || 2,
    maGiaoDich: "",
    ghiChu: "Hoàn tiền theo phiếu trả hàng",
  };
  modal.value = "hoan-tien";
}

function moModalTuChoi() {
  formTuChoi.value = { lyDo: "" };
  modal.value = "tu-choi";
}

async function xacNhanNhanHang() {
  const confirmed = await showConfirm(
    "Xác nhận cửa hàng đã nhận đủ kiện hàng trả?",
    "Xác nhận nhận hàng",
    "Đã nhận hàng",
  );
  if (!confirmed) return;
  await thucHien("Đã xác nhận nhận hàng trả", () =>
    xacNhanNhanHangTra(phieu.value.id, { ghiChu: "Cửa hàng đã nhận kiện hàng trả" }),
  );
}

async function batDauKiemTra() {
  const confirmed = await showConfirm(
    "Bắt đầu kiểm tra tình trạng các sản phẩm khách gửi trả?",
    "Kiểm tra hàng trả",
    "Bắt đầu",
  );
  if (!confirmed) return;
  await thucHien("Đã chuyển phiếu sang kiểm tra", () =>
    batDauKiemTraHang(phieu.value.id, { ghiChu: "Bắt đầu kiểm tra hàng trả" }),
  );
}

async function huyPhieu() {
  const confirmed = await showConfirm(
    "Phiếu đã hủy sẽ không tiếp tục được xử lý.",
    "Hủy phiếu trả hàng",
    "Xác nhận hủy",
  );
  if (!confirmed) return;
  await thucHien("Đã hủy phiếu trả hàng", () =>
    huyPhieuTraHang(phieu.value.id, { ghiChu: "Nhân viên hủy phiếu trả hàng" }),
  );
}

onMounted(taiChiTiet);
</script>

<template>
  <div class="space-y-5 pb-10">
    <div class="flex flex-col justify-between gap-4 lg:flex-row lg:items-start">
      <div>
        <div class="flex items-center gap-3">
          <button
            type="button"
            class="flex h-10 w-10 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 transition hover:border-rose-200 hover:text-primary"
            @click="router.push({ name: 'admin-tra-hang' })"
          >
            <ArrowLeft class="h-4 w-4" />
          </button>
          <div>
            <h1 class="text-2xl font-bold text-slate-800">Chi tiết phiếu trả hàng</h1>
            <p v-if="phieu" class="mt-1 text-sm text-slate-500">
              {{ phieu.ma }} · Hóa đơn {{ phieu.maHoaDon }}
            </p>
          </div>
        </div>
      </div>
      <Button variant="soft" :loading="dangTai" @click="taiChiTiet">
        <template #prefix><RefreshCw class="h-4 w-4" /></template>
        Làm mới
      </Button>
    </div>

    <Card v-if="dangTai">
      <div class="flex min-h-64 items-center justify-center text-sm text-slate-400">
        Đang tải chi tiết phiếu trả hàng...
      </div>
    </Card>

    <Card v-else-if="loiTrang || !phieu">
      <div class="flex min-h-64 flex-col items-center justify-center gap-4 text-center">
        <XCircle class="h-10 w-10 text-rose-400" />
        <p class="font-medium text-rose-600">{{ loiTrang || "Không tìm thấy phiếu trả hàng" }}</p>
        <Button variant="soft" @click="taiChiTiet">Thử lại</Button>
      </div>
    </Card>

    <template v-else>
      <Card>
        <template #header>
          <div class="flex w-full flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div class="flex items-center gap-3">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-rose-50 text-primary">
                <PackageCheck class="h-5 w-5" />
              </div>
              <div>
                <h2 class="font-semibold text-slate-800">Tiến trình xử lý</h2>
                <p class="mt-0.5 text-xs text-slate-400">Cập nhật lần cuối {{ dinhDangNgay(phieu.ngayCapNhat) }}</p>
              </div>
            </div>
            <Badge :variant="badgeVariant(phieu.trangThai)">{{ phieu.tenTrangThai }}</Badge>
          </div>
        </template>

        <div v-if="[8, 9, 10].includes(trangThai)" class="rounded-2xl border border-rose-100 bg-rose-50 px-4 py-3 text-sm text-rose-700">
          {{ phieu.lyDoTuChoi || `Phiếu đang ở trạng thái ${phieu.tenTrangThai.toLowerCase()}.` }}
        </div>
        <div v-else class="relative grid grid-cols-2 gap-4 md:grid-cols-6">
          <div class="absolute left-[8%] right-[8%] top-6 hidden h-0.5 bg-slate-200 md:block"></div>
          <div
            v-for="(buoc, index) in cacBuoc"
            :key="buoc.id"
            class="relative z-10 flex flex-col items-center text-center"
          >
            <div
              class="flex h-12 w-12 items-center justify-center rounded-full border-2 bg-white transition"
              :class="index <= buocHienTai ? 'border-primary bg-rose-50 text-primary' : 'border-slate-200 text-slate-300'"
            >
              <CheckCircle2 v-if="index < buocHienTai" class="h-5 w-5" />
              <Clock3 v-else class="h-5 w-5" />
            </div>
            <p
              class="mt-2 text-xs font-semibold"
              :class="index <= buocHienTai ? 'text-slate-700' : 'text-slate-400'"
            >
              {{ buoc.ten }}
            </p>
          </div>
        </div>
      </Card>

      <section class="grid gap-4 xl:grid-cols-[1.45fr_0.8fr]">
        <Card>
          <template #header>
            <div class="flex items-center gap-3">
              <ClipboardCheck class="h-5 w-5 text-primary" />
              <h2 class="font-semibold text-slate-800">Thông tin yêu cầu</h2>
            </div>
          </template>

          <dl class="grid gap-x-8 gap-y-5 text-sm sm:grid-cols-2">
            <div>
              <dt class="text-slate-400">Khách hàng</dt>
              <dd class="mt-1 font-semibold text-slate-700">{{ phieu.tenKhachHang || "Khách vãng lai" }}</dd>
            </div>
            <div>
              <dt class="text-slate-400">Số điện thoại</dt>
              <dd class="mt-1 font-semibold text-slate-700">{{ phieu.soDienThoaiKhachHang || "Không có" }}</dd>
            </div>
            <div>
              <dt class="text-slate-400">Lý do</dt>
              <dd class="mt-1 font-semibold text-slate-700">{{ phieu.lyDoMa || "Khác" }}</dd>
            </div>
            <div>
              <dt class="text-slate-400">Nhân viên xử lý</dt>
              <dd class="mt-1 font-semibold text-slate-700">{{ phieu.maNhanVien || "Chưa phân công" }}</dd>
            </div>
            <div v-if="phieu.donViVanChuyen">
              <dt class="text-slate-400">Vận chuyển hoàn</dt>
              <dd class="mt-1 font-semibold text-slate-700">
                {{ phieu.donViVanChuyen }} · {{ phieu.maVanDonHoan }}
              </dd>
            </div>
            <div class="sm:col-span-2">
              <dt class="text-slate-400">Mô tả</dt>
              <dd class="mt-1 leading-6 text-slate-700">{{ phieu.moTa || "Không có mô tả bổ sung" }}</dd>
            </div>
          </dl>
        </Card>

        <Card>
          <template #header>
            <div class="flex items-center gap-3">
              <Banknote class="h-5 w-5 text-primary" />
              <h2 class="font-semibold text-slate-800">Tổng kết hoàn tiền</h2>
            </div>
          </template>
          <div class="space-y-4 text-sm">
            <div class="flex items-center justify-between">
              <span class="text-slate-500">Tiền hoàn dự kiến</span>
              <span class="font-semibold text-slate-700">{{ dinhDangTien(phieu.tongTienDuKien) }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-slate-500">Tiền hoàn được duyệt</span>
              <span class="font-semibold text-emerald-600">{{ dinhDangTien(phieu.tongTienThucTe) }}</span>
            </div>
            <div class="border-t border-slate-200 pt-4">
              <div class="flex items-center justify-between">
                <span class="font-bold text-slate-800">Trạng thái</span>
                <Badge :variant="badgeVariant(phieu.trangThai)">{{ phieu.tenTrangThai }}</Badge>
              </div>
            </div>
          </div>
        </Card>
      </section>

      <Card>
        <template #header>
          <div>
            <h2 class="font-semibold text-slate-800">Sản phẩm trả</h2>
            <p class="mt-1 text-xs text-slate-400">{{ phieu.chiTiet?.length || 0 }} dòng sản phẩm</p>
          </div>
        </template>
        <Table>
          <template #header>
            <th class="px-4 py-3 text-center">STT</th>
            <th class="px-4 py-3">Sản phẩm</th>
            <th class="px-4 py-3">Biến thể</th>
            <th class="px-4 py-3 text-center">Yêu cầu trả</th>
            <th class="px-4 py-3 text-center">Chấp nhận</th>
            <th class="px-4 py-3 text-right">Tiền hoàn</th>
            <th class="px-4 py-3">Tình trạng</th>
          </template>
          <template #body>
            <tr v-for="(item, index) in phieu.chiTiet" :key="item.id">
              <td class="px-4 py-4 text-center text-slate-500">{{ index + 1 }}</td>
              <td class="px-4 py-4">
                <p class="font-semibold text-slate-800">{{ item.tenSanPham || "Sản phẩm" }}</p>
                <p class="mt-1 text-xs text-slate-400">{{ item.maBienThe }}</p>
              </td>
              <td class="px-4 py-4 text-slate-600">{{ item.mauSac || "—" }} / {{ item.kichCo || "—" }}</td>
              <td class="px-4 py-4 text-center font-semibold">{{ item.soLuongTra }}</td>
              <td class="px-4 py-4 text-center font-semibold text-emerald-600">{{ item.soLuongChapNhan }}</td>
              <td class="px-4 py-4 text-right font-semibold text-primary">{{ dinhDangTien(item.soTienHoan) }}</td>
              <td class="px-4 py-4 text-slate-500">{{ item.tinhTrangSanPham || "Chưa kiểm tra" }}</td>
            </tr>
          </template>
        </Table>
      </Card>

      <section class="grid gap-4 lg:grid-cols-[1fr_0.9fr]">
        <Card>
          <template #header>
            <div class="flex items-center gap-3">
              <Clock3 class="h-5 w-5 text-primary" />
              <h2 class="font-semibold text-slate-800">Lịch sử xử lý</h2>
            </div>
          </template>
          <div class="space-y-0">
            <div
              v-for="(item, index) in phieu.lichSu"
              :key="item.id || index"
              class="relative flex gap-4 pb-5 last:pb-0"
            >
              <div v-if="index < phieu.lichSu.length - 1" class="absolute left-[15px] top-8 h-[calc(100%-20px)] w-px bg-slate-200"></div>
              <div class="relative z-10 mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-rose-50 text-primary">
                <CheckCircle2 class="h-4 w-4" />
              </div>
              <div class="min-w-0 flex-1 rounded-2xl bg-slate-50 px-4 py-3">
                <div class="flex flex-wrap items-center justify-between gap-2">
                  <p class="font-semibold text-slate-700">{{ item.hanhDong }}</p>
                  <span class="text-xs text-slate-400">{{ dinhDangNgay(item.ngayTao) }}</span>
                </div>
                <p class="mt-1 text-xs text-slate-500">
                  {{ item.maNhanVien || "Hệ thống" }} · {{ item.tenTrangThaiMoi }}
                </p>
                <p v-if="item.ghiChu" class="mt-2 text-sm leading-5 text-slate-600">{{ item.ghiChu }}</p>
              </div>
            </div>
          </div>
        </Card>

        <Card>
          <template #header>
            <div class="flex items-center gap-3">
              <UserRound class="h-5 w-5 text-primary" />
              <h2 class="font-semibold text-slate-800">Thao tác tiếp theo</h2>
            </div>
          </template>
          <div class="grid gap-3">
            <template v-if="coTheDuyet">
              <Button full-width @click="moModalDuyet">Duyệt phiếu trả hàng</Button>
              <Button variant="soft" full-width @click="moModalTuChoi">Từ chối yêu cầu</Button>
            </template>
            <Button v-if="coTheXacNhanGui" full-width @click="moModalGuiHang">
              <template #prefix><Truck class="h-4 w-4" /></template>
              Xác nhận khách đã gửi hàng
            </Button>
            <Button v-if="coTheXacNhanNhan" full-width @click="xacNhanNhanHang">
              Xác nhận đã nhận hàng
            </Button>
            <Button v-if="coTheBatDauKiemTra" full-width @click="batDauKiemTra">
              Bắt đầu kiểm tra
            </Button>
            <Button v-if="coTheKiemTra" full-width @click="moModalKiemTra">
              Nhập kết quả kiểm tra
            </Button>
            <Button v-if="coTheHoanTien" full-width @click="moModalHoanTien">
              <template #prefix><Banknote class="h-4 w-4" /></template>
              Xác nhận hoàn tiền
            </Button>
            <Button v-if="coTheHuy" variant="outline" full-width @click="huyPhieu">
              Hủy phiếu trả hàng
            </Button>
            <div
              v-if="![1, 2, 3, 4, 5, 6, 10].includes(trangThai)"
              class="rounded-2xl bg-slate-50 px-4 py-5 text-center text-sm text-slate-500"
            >
              Phiếu đã kết thúc, không còn thao tác cần xử lý.
            </div>
          </div>
        </Card>
      </section>
    </template>

    <div
      v-if="modal"
      class="fixed inset-0 z-[80] flex items-center justify-center bg-slate-900/45 p-4 backdrop-blur-sm"
      @click.self="modal = ''"
    >
      <div class="max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-3xl border border-rose-100 bg-white shadow-2xl">
        <div class="flex items-center justify-between border-b border-slate-100 px-6 py-5">
          <h3 class="text-lg font-bold text-slate-800">
            {{
              {
                duyet: "Duyệt phiếu trả hàng",
                "gui-hang": "Thông tin vận chuyển hoàn",
                "kiem-tra": "Kiểm tra sản phẩm trả",
                "hoan-tien": "Xác nhận hoàn tiền",
                "tu-choi": "Từ chối phiếu trả hàng",
              }[modal]
            }}
          </h3>
          <button class="rounded-full p-2 text-slate-400 hover:bg-slate-100" @click="modal = ''">
            <X class="h-5 w-5" />
          </button>
        </div>

        <div class="space-y-5 px-6 py-5">
          <template v-if="modal === 'duyet'">
            <div class="grid gap-3 sm:grid-cols-2">
              <button
                type="button"
                class="rounded-2xl border p-4 text-left transition"
                :class="!formDuyet.nhanHangTrucTiep ? 'border-primary bg-rose-50' : 'border-slate-200'"
                @click="formDuyet.nhanHangTrucTiep = false"
              >
                <Truck class="h-5 w-5 text-primary" />
                <p class="mt-3 font-semibold text-slate-800">Khách gửi hàng về</p>
                <p class="mt-1 text-xs leading-5 text-slate-500">Chờ khách cung cấp kiện hàng và mã vận đơn.</p>
              </button>
              <button
                type="button"
                class="rounded-2xl border p-4 text-left transition"
                :class="formDuyet.nhanHangTrucTiep ? 'border-primary bg-rose-50' : 'border-slate-200'"
                @click="formDuyet.nhanHangTrucTiep = true"
              >
                <PackageCheck class="h-5 w-5 text-primary" />
                <p class="mt-3 font-semibold text-slate-800">Đã nhận tại cửa hàng</p>
                <p class="mt-1 text-xs leading-5 text-slate-500">Bỏ qua bước vận chuyển và chuyển sang nhận hàng.</p>
              </button>
            </div>
            <textarea
              v-model="formDuyet.ghiChu"
              rows="3"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm outline-none focus:border-rose-300 focus:bg-white"
              placeholder="Ghi chú duyệt phiếu..."
            ></textarea>
            <Button
              full-width
              :loading="dangXuLy"
              @click="thucHien('Duyệt phiếu trả hàng thành công', () => duyetPhieuTraHang(phieu.id, formDuyet))"
            >
              Xác nhận duyệt
            </Button>
          </template>

          <template v-else-if="modal === 'gui-hang'">
            <label class="block space-y-2">
              <span class="text-sm font-semibold text-slate-600">Đơn vị vận chuyển</span>
              <input v-model="formVanChuyen.donViVanChuyen" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300" placeholder="VD: GHN, GHTK..." />
            </label>
            <label class="block space-y-2">
              <span class="text-sm font-semibold text-slate-600">Mã vận đơn hoàn</span>
              <input v-model="formVanChuyen.maVanDonHoan" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300" placeholder="Nhập mã vận đơn..." />
            </label>
            <textarea v-model="formVanChuyen.ghiChu" rows="3" class="w-full rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm outline-none focus:border-rose-300" placeholder="Ghi chú..."></textarea>
            <Button
              full-width
              :loading="dangXuLy"
              @click="thucHien('Đã xác nhận khách gửi hàng', () => xacNhanGuiHangTra(phieu.id, formVanChuyen))"
            >
              Xác nhận gửi hàng
            </Button>
          </template>

          <template v-else-if="modal === 'kiem-tra'">
            <div
              v-for="item in formKiemTra.sanPhams"
              :key="item.chiTietTraHangId"
              class="rounded-2xl border border-slate-200 p-4"
            >
              <div class="flex items-start justify-between gap-3">
                <div>
                  <p class="font-semibold text-slate-800">{{ item.tenSanPham || "Sản phẩm" }}</p>
                  <p class="mt-1 text-xs text-slate-400">{{ item.maBienThe }} · Yêu cầu trả {{ item.soLuongTra }}</p>
                </div>
                <label class="flex items-center gap-2 text-sm text-slate-600">
                  <input v-model="item.nhapLaiTonKho" type="checkbox" class="h-4 w-4 accent-red-600" />
                  Nhập lại tồn
                </label>
              </div>
              <div class="mt-4 grid gap-3 sm:grid-cols-2">
                <label class="space-y-2">
                  <span class="text-xs font-semibold text-slate-500">Số lượng đã nhận</span>
                  <input v-model.number="item.soLuongNhan" type="number" min="0" :max="item.soLuongTra" class="h-10 w-full rounded-xl border border-slate-200 px-3 outline-none focus:border-rose-300" />
                </label>
                <label class="space-y-2">
                  <span class="text-xs font-semibold text-slate-500">Số lượng chấp nhận</span>
                  <input v-model.number="item.soLuongChapNhan" type="number" min="0" :max="item.soLuongNhan" class="h-10 w-full rounded-xl border border-slate-200 px-3 outline-none focus:border-rose-300" />
                </label>
              </div>
              <input v-model="item.tinhTrangSanPham" class="mt-3 h-10 w-full rounded-xl border border-slate-200 px-3 text-sm outline-none focus:border-rose-300" placeholder="Tình trạng sản phẩm..." />
            </div>
            <textarea v-model="formKiemTra.ghiChu" rows="3" class="w-full rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm outline-none focus:border-rose-300" placeholder="Kết luận kiểm tra..."></textarea>
            <Button
              full-width
              :loading="dangXuLy"
              @click="thucHien('Đã lưu kết quả kiểm tra', () => capNhatKiemTraHang(phieu.id, {
                sanPhams: formKiemTra.sanPhams.map(({ soLuongTra, tenSanPham, maBienThe, ...item }) => item),
                ghiChu: formKiemTra.ghiChu,
              }))"
            >
              Lưu kết quả kiểm tra
            </Button>
          </template>

          <template v-else-if="modal === 'hoan-tien'">
            <div class="rounded-2xl bg-rose-50 px-5 py-4">
              <p class="text-sm text-rose-600">Số tiền cần hoàn</p>
              <p class="mt-1 text-2xl font-bold text-primary">{{ dinhDangTien(phieu.tongTienThucTe) }}</p>
            </div>
            <label class="block space-y-2">
              <span class="text-sm font-semibold text-slate-600">Hình thức hoàn tiền</span>
              <select v-model.number="formHoanTien.hinhThucHoan" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300">
                <option :value="1">Tiền mặt</option>
                <option :value="2">Chuyển khoản</option>
                <option :value="3">Ví điện tử</option>
              </select>
            </label>
            <label class="block space-y-2">
              <span class="text-sm font-semibold text-slate-600">Mã giao dịch</span>
              <input v-model="formHoanTien.maGiaoDich" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300" placeholder="Để trống nếu hoàn tiền mặt" />
            </label>
            <textarea v-model="formHoanTien.ghiChu" rows="3" class="w-full rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm outline-none focus:border-rose-300"></textarea>
            <Button
              full-width
              :loading="dangXuLy"
              @click="thucHien('Hoàn tiền trả hàng thành công', () => hoanTienTraHang(phieu.id, formHoanTien))"
            >
              Xác nhận đã hoàn tiền
            </Button>
          </template>

          <template v-else-if="modal === 'tu-choi'">
            <textarea v-model="formTuChoi.lyDo" rows="5" class="w-full rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm outline-none focus:border-rose-300" placeholder="Nhập lý do từ chối..."></textarea>
            <Button
              variant="danger"
              full-width
              :loading="dangXuLy"
              @click="thucHien('Đã từ chối phiếu trả hàng', () => tuChoiTraHang(phieu.id, formTuChoi))"
            >
              Xác nhận từ chối
            </Button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>
