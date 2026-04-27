<script setup>
import { computed, onBeforeUnmount, ref, watch } from "vue";
import { Camera, QrCode, RefreshCw, X } from "lucide-vue-next";

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["close", "scan"]);

const videoRef = ref(null);
const loadingCamera = ref(false);
const scannerError = ref("");
const manualCode = ref("");
const usingCompatibilityScanner = ref(false);

let mediaStream = null;
let barcodeDetectorInstance = null;
let detectIntervalId = null;
let isDetecting = false;
let zxingReaderInstance = null;
let zxingControls = null;

const SCAN_FORMATS = [
  "qr_code",
  "code_128",
  "code_39",
  "ean_13",
  "ean_8",
  "upc_a",
  "upc_e",
];

const hasBarcodeDetectorSupport = computed(
  () => typeof window !== "undefined" && "BarcodeDetector" in window,
);

const canUseCamera = computed(
  () =>
    typeof navigator !== "undefined" &&
    Boolean(navigator.mediaDevices?.getUserMedia),
);

const helperText = computed(() => {
  if (loadingCamera.value) {
    return "Đang bật camera để quét mã sản phẩm...";
  }

  if (scannerError.value) {
    return scannerError.value;
  }

  if (!hasBarcodeDetectorSupport.value) {
    return usingCompatibilityScanner.value
      ? "Đang dùng chế độ quét tương thích cho trình duyệt hiện tại."
      : "Trình duyệt này sẽ dùng chế độ quét tương thích để nhận QR.";
  }

  return "Đưa QR hoặc mã vạch sản phẩm vào giữa khung quét.";
});

async function taoBarcodeDetector() {
  const DetectorClass = window.BarcodeDetector;
  if (!DetectorClass) {
    return null;
  }

  if (typeof DetectorClass.getSupportedFormats !== "function") {
    return new DetectorClass({ formats: SCAN_FORMATS });
  }

  const supportedFormats = await DetectorClass.getSupportedFormats();
  const preferredFormats = SCAN_FORMATS.filter((format) =>
    supportedFormats.includes(format),
  );

  return preferredFormats.length
    ? new DetectorClass({ formats: preferredFormats })
    : new DetectorClass();
}

function dungQuet() {
  if (detectIntervalId) {
    window.clearInterval(detectIntervalId);
    detectIntervalId = null;
  }
}

function dungCamera() {
  dungQuet();

  if (mediaStream) {
    mediaStream.getTracks().forEach((track) => track.stop());
    mediaStream = null;
  }

  if (videoRef.value) {
    videoRef.value.pause();
    videoRef.value.srcObject = null;
  }

  if (zxingControls?.stop) {
    zxingControls.stop();
  }
  zxingControls = null;

  if (zxingReaderInstance?.reset) {
    zxingReaderInstance.reset();
  }
  zxingReaderInstance = null;

  barcodeDetectorInstance = null;
  usingCompatibilityScanner.value = false;
  isDetecting = false;
  loadingCamera.value = false;
}

async function quetMa() {
  if (
    !props.open ||
    !barcodeDetectorInstance ||
    !videoRef.value ||
    isDetecting ||
    videoRef.value.readyState < 2
  ) {
    return;
  }

  isDetecting = true;
  try {
    const barcodes = await barcodeDetectorInstance.detect(videoRef.value);
    const rawValue = barcodes
      ?.map((item) => item.rawValue?.trim())
      .find(Boolean);

    if (rawValue) {
      manualCode.value = rawValue;
      emit("scan", rawValue);
      emit("close");
    }
  } catch (error) {
    if (!scannerError.value) {
      scannerError.value =
        error instanceof Error
          ? error.message
          : "Không thể đọc mã QR từ camera lúc này.";
    }
  } finally {
    isDetecting = false;
  }
}

async function batCamera() {
  scannerError.value = "";
  usingCompatibilityScanner.value = false;

  if (!props.open) {
    return;
  }

  if (!canUseCamera.value) {
    scannerError.value =
      "Thiết bị hoặc trình duyệt hiện tại chưa hỗ trợ mở camera để quét mã.";
    return;
  }

  loadingCamera.value = true;
  try {
    if (!videoRef.value) {
      dungCamera();
      return;
    }

    if (!hasBarcodeDetectorSupport.value) {
      const [{ BrowserMultiFormatReader }, zxingLibrary] =
        await Promise.all([
          import("@zxing/browser"),
          import("@zxing/library"),
        ]);

      const { BarcodeFormat } = zxingLibrary;

      const hints = new Map();
      hints.set(zxingLibrary.DecodeHintType.POSSIBLE_FORMATS, [
        BarcodeFormat.QR_CODE,
        BarcodeFormat.CODE_128,
        BarcodeFormat.CODE_39,
        BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8,
        BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E,
      ]);

      zxingReaderInstance = new BrowserMultiFormatReader(hints);
      usingCompatibilityScanner.value = true;

      zxingControls = await zxingReaderInstance.decodeFromConstraints(
        {
          audio: false,
          video: {
            facingMode: { ideal: "environment" },
          },
        },
        videoRef.value,
        (result, error, controls) => {
          const rawValue = result?.getText?.().trim();
          if (rawValue) {
            manualCode.value = rawValue;
            controls?.stop?.();
            emit("scan", rawValue);
            emit("close");
            return;
          }

          if (
            error &&
            !(error instanceof zxingLibrary.NotFoundException) &&
            !(error instanceof zxingLibrary.ChecksumException) &&
            !(error instanceof zxingLibrary.FormatException) &&
            !scannerError.value
          ) {
            scannerError.value =
              error instanceof Error
                ? error.message
                : "Không thể đọc mã QR từ camera lúc này.";
          }
        },
      );
      return;
    }

    barcodeDetectorInstance = await taoBarcodeDetector();
    mediaStream = await navigator.mediaDevices.getUserMedia({
      audio: false,
      video: {
        facingMode: { ideal: "environment" },
      },
    });

    videoRef.value.srcObject = mediaStream;
    await videoRef.value.play();

    dungQuet();
    detectIntervalId = window.setInterval(() => {
      void quetMa();
    }, 350);
  } catch (error) {
    dungCamera();
    scannerError.value =
      error instanceof Error
        ? error.message
        : "Không thể bật camera để quét mã sản phẩm.";
  } finally {
    loadingCamera.value = false;
  }
}

function dongModal() {
  emit("close");
}

function xacNhanMaThuCong() {
  const value = manualCode.value.trim();
  if (!value) {
    scannerError.value = "Vui lòng quét hoặc nhập mã sản phẩm.";
    return;
  }

  emit("scan", value);
  emit("close");
}

async function thuLai() {
  dungCamera();
  await batCamera();
}

watch(
  () => props.open,
  async (isOpen) => {
    if (!isOpen) {
      dungCamera();
      scannerError.value = "";
      manualCode.value = "";
      return;
    }

    await batCamera();
  },
);

onBeforeUnmount(() => {
  dungCamera();
});
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="open"
        class="fixed inset-0 z-[90] flex items-center justify-center bg-slate-950/55 p-4"
        @click.self="dongModal"
      >
        <div
          class="w-full max-w-3xl overflow-hidden rounded-[32px] border border-white/70 bg-white shadow-[0_30px_80px_rgba(15,23,42,0.28)]"
        >
          <div
            class="flex items-start justify-between gap-4 border-b border-slate-100 px-6 py-5"
          >
            <div>
              <div
                class="inline-flex items-center gap-2 rounded-full bg-red-50 px-3 py-1 text-sm font-semibold text-red-600"
              >
                <QrCode :size="16" />
                Quét QR sản phẩm
              </div>
              <h2 class="mt-3 text-2xl font-black text-slate-900">
                Dùng camera để nhận mã sản phẩm
              </h2>
              <p class="mt-2 text-sm text-slate-500">
                {{ helperText }}
              </p>
            </div>

            <button
              type="button"
              class="inline-flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-500 transition hover:bg-slate-200"
              @click="dongModal"
            >
              <X :size="18" />
            </button>
          </div>

          <div class="grid gap-5 p-6 lg:grid-cols-[1.2fr_0.8fr]">
            <div
              class="overflow-hidden rounded-[28px] border border-slate-200 bg-[radial-gradient(circle_at_top,#111827_0%,#0f172a_55%,#020617_100%)] p-4 text-white"
            >
              <div
                class="relative overflow-hidden rounded-[24px] border border-white/10 bg-black/40"
              >
                <video
                  ref="videoRef"
                  class="aspect-[4/3] w-full object-cover"
                  playsinline
                  muted
                  autoplay
                />

                <div
                  class="pointer-events-none absolute inset-0 flex items-center justify-center"
                >
                  <div
                    class="h-48 w-48 rounded-[32px] border-2 border-white/80 shadow-[0_0_0_9999px_rgba(2,6,23,0.24)]"
                  />
                </div>

                <div
                  v-if="loadingCamera"
                  class="absolute inset-0 flex items-center justify-center bg-slate-950/55 text-sm font-semibold text-white"
                >
                  Đang khởi tạo camera...
                </div>
              </div>

              <div class="mt-4 flex items-center justify-between gap-3">
                <div class="inline-flex items-center gap-2 text-sm text-slate-200">
                  <Camera :size="16" />
                  Ưu tiên camera sau nếu thiết bị có hỗ trợ
                </div>

                <button
                  type="button"
                  class="inline-flex items-center gap-2 rounded-2xl border border-white/15 bg-white/10 px-4 py-2 text-sm font-semibold text-white transition hover:bg-white/15"
                  @click="thuLai"
                >
                  <RefreshCw :size="15" />
                  Quét lại
                </button>
              </div>
            </div>

            <div class="space-y-4 rounded-[28px] border border-slate-200 bg-slate-50/80 p-5">
              <div>
                <h3 class="text-lg font-black text-slate-900">Mã quét thủ công</h3>
                <p class="mt-1 text-sm text-slate-500">
                  Dùng khi camera chưa nhận được QR hoặc bạn muốn dán mã SKU / mã biến thể.
                </p>
              </div>

              <label class="block">
                <span class="mb-2 block text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">
                  Mã sản phẩm
                </span>
                <textarea
                  v-model="manualCode"
                  rows="5"
                  placeholder="Ví dụ: G49760-MS001-44-5816"
                  class="w-full rounded-[24px] border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300 focus:ring-2 focus:ring-red-200"
                />
              </label>

              <p
                v-if="scannerError"
                class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-600"
              >
                {{ scannerError }}
              </p>

              <button
                type="button"
                class="inline-flex w-full items-center justify-center gap-2 rounded-[22px] bg-red-500 px-4 py-3 text-sm font-semibold text-white transition hover:bg-red-600"
                @click="xacNhanMaThuCong"
              >
                <QrCode :size="16" />
                Dùng mã này để tìm sản phẩm
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
