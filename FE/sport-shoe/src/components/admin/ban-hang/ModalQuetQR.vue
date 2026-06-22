<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from "vue";
import { Camera, QrCode, RefreshCw, X } from "lucide-vue-next";

const DEFAULT_SCAN_FORMATS = [
  "qr_code",
  "code_128",
  "code_39",
  "ean_13",
  "ean_8",
  "upc_a",
  "upc_e",
];

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  chipLabel: {
    type: String,
    default: "Quét QR sản phẩm",
  },
  title: {
    type: String,
    default: "Dùng camera để nhận mã sản phẩm",
  },
  loadingText: {
    type: String,
    default: "Đang bật camera để quét mã sản phẩm...",
  },
  fallbackHelperText: {
    type: String,
    default: "Đưa QR hoặc mã vạch sản phẩm vào giữa khung quét.",
  },
  retryButtonLabel: {
    type: String,
    default: "Quét lại",
  },
  cameraHint: {
    type: String,
    default: "Ưu tiên camera sau nếu thiết bị có hỗ trợ",
  },
  closeOnScan: {
    type: Boolean,
    default: true,
  },
  forceCompatibilityScanner: {
    type: Boolean,
    default: false,
  },
  scanFormats: {
    type: Array,
    default: () => [
      "qr_code",
      "code_128",
      "code_39",
      "ean_13",
      "ean_8",
      "upc_a",
      "upc_e",
    ],
  },
  externalError: {
    type: String,
    default: "",
  },
  showRetryButton: {
    type: Boolean,
    default: true,
  },
  showHeaderContent: {
    type: Boolean,
    default: true,
  },
  showCameraHint: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(["close", "scan"]);

const videoRef = ref(null);
const loadingCamera = ref(false);
const scannerError = ref("");
const usingCompatibilityScanner = ref(false);

let mediaStream = null;
let barcodeDetectorInstance = null;
let detectIntervalId = null;
let isDetecting = false;
let zxingReaderInstance = null;
let zxingControls = null;
let lastScannedValue = "";
let lastScannedAt = 0;

const hasBarcodeDetectorSupport = computed(
  () => typeof window !== "undefined" && "BarcodeDetector" in window,
);

const canUseCamera = computed(
  () =>
    typeof navigator !== "undefined" &&
    Boolean(navigator.mediaDevices?.getUserMedia),
);

const activeScanFormats = computed(() =>
  Array.isArray(props.scanFormats) && props.scanFormats.length
    ? props.scanFormats
    : DEFAULT_SCAN_FORMATS,
);

const mergedError = computed(() => props.externalError || scannerError.value);

const helperText = computed(() => {
  if (loadingCamera.value) {
    return props.loadingText;
  }

  if (mergedError.value) {
    return mergedError.value;
  }

  if (!hasBarcodeDetectorSupport.value || props.forceCompatibilityScanner) {
    return usingCompatibilityScanner.value
      ? "Đang dùng chế độ quét tương thích cho trình duyệt hiện tại."
      : "Trình duyệt này sẽ dùng chế độ quét tương thích để nhận QR.";
  }

  return props.fallbackHelperText;
});

function layThongBaoLoiCamera(error, fallbackMessage) {
  const errorName = typeof error?.name === "string" ? error.name : "";
  const errorMessage =
    error instanceof Error ? error.message : typeof error === "string" ? error : "";

  if (errorName === "NotAllowedError" || /permission denied/i.test(errorMessage)) {
    return "Trinh duyet dang chan quyen camera. Bam bieu tuong canh dia chi, mo Site settings, cho phep Camera roi tai lai trang.";
  }

  if (errorName === "NotFoundError") {
    return "Khong tim thay camera tren thiet bi nay.";
  }

  if (errorName === "NotReadableError") {
    return "Camera dang duoc ung dung khac su dung. Hay tat app dang chiem camera roi thu lai.";
  }

  return error instanceof Error ? error.message : fallbackMessage;
}

async function taoBarcodeDetector() {
  const DetectorClass = window.BarcodeDetector;
  if (!DetectorClass) {
    return null;
  }

  if (typeof DetectorClass.getSupportedFormats !== "function") {
    return new DetectorClass({ formats: activeScanFormats.value });
  }

  const supportedFormats = await DetectorClass.getSupportedFormats();
  const preferredFormats = activeScanFormats.value.filter((format) =>
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
  lastScannedValue = "";
  lastScannedAt = 0;
}

function xuLyKetQuaQuet(rawValue, controls = null) {
  if (!props.closeOnScan) {
    const now = Date.now();
    if (rawValue === lastScannedValue && now - lastScannedAt < 1500) {
      return;
    }
    lastScannedValue = rawValue;
    lastScannedAt = now;
    emit("scan", rawValue);
    return;
  }
  controls?.stop?.();
  emit("scan", rawValue);
  emit("close");
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
      xuLyKetQuaQuet(rawValue);
    }
  } catch (error) {
    if (!scannerError.value) {
      scannerError.value = layThongBaoLoiCamera(
        error,
        "Khong the doc ma QR tu camera luc nay.",
      );
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

    if (!hasBarcodeDetectorSupport.value || props.forceCompatibilityScanner) {
      const { BrowserMultiFormatReader } = await import("@zxing/browser");

      zxingReaderInstance = new BrowserMultiFormatReader();
      usingCompatibilityScanner.value = true;

      zxingControls = await zxingReaderInstance.decodeFromConstraints(
        {
          audio: false,
          video: {
            facingMode: { ideal: "environment" },
            width: { ideal: 1280 },
            height: { ideal: 720 },
          },
        },
        videoRef.value,
        (result, error, controls) => {
          const rawValue = result?.getText?.().trim();
          if (rawValue) {
            xuLyKetQuaQuet(rawValue, controls);
            return;
          }

          const ignoredErrorNames = new Set([
            "NotFoundException",
            "ChecksumException",
            "FormatException",
          ]);
          
          const errorMessage = typeof error === "string" ? error : error?.message || "";
          const isIgnoredMessage = errorMessage.includes("No MultiFormat Readers were able to detect the code");

          if (error && !ignoredErrorNames.has(error?.name) && !isIgnoredMessage) {
             console.warn("Lỗi khung hình scanner:", error);
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
        width: { ideal: 1280 },
        height: { ideal: 720 },
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
    scannerError.value = layThongBaoLoiCamera(
      error,
      "Khong the bat camera de quet ma.",
    );
  } finally {
    loadingCamera.value = false;
  }
}

function dongModal() {
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
      return;
    }

    await nextTick();
    await batCamera();
  },
  { immediate: true, flush: "post" },
);

watch(
  () => props.externalError,
  (value) => {
    if (value) {
      scannerError.value = "";
    }
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
          class="w-full overflow-hidden rounded-[32px] border border-white/70 bg-white shadow-[0_30px_80px_rgba(15,23,42,0.28)] max-w-3xl"
        >
          <div
            :class="[
              'flex items-start gap-4 px-6 pt-5',
              props.showHeaderContent
                ? 'justify-between border-b border-slate-100 pb-5'
                : 'justify-end pb-0',
            ]"
          >
            <div v-if="props.showHeaderContent">
              <div
                class="inline-flex items-center gap-2 rounded-full bg-red-50 px-3 py-1 text-sm font-semibold text-red-600"
              >
                <QrCode :size="16" />
                {{ chipLabel }}
              </div>
              <h2 class="mt-3 text-2xl font-black text-slate-900">
                {{ title }}
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

          <div class="grid gap-5 p-6">
            <div
              class="overflow-hidden rounded-[28px] border border-slate-200 bg-[radial-gradient(circle_at_top,#111827_0%,#0f172a_55%,#020617_100%)] text-white p-4"
            >
              <div
                class="relative overflow-hidden rounded-[24px] border border-white/10 bg-black/40"
              >
                <video
                  ref="videoRef"
                  class="w-full object-cover aspect-[4/3]"
                  playsinline
                  muted
                  autoplay
                />

                <div
                  class="pointer-events-none absolute inset-0 flex items-center justify-center"
                >
                  <div
                      class="rounded-[32px] border-2 border-white/80 shadow-[0_0_0_9999px_rgba(2,6,23,0.24)] h-48 w-48"
                  />
                </div>

                <div
                  v-if="loadingCamera"
                  class="absolute inset-0 flex items-center justify-center bg-slate-950/55 text-sm font-semibold text-white"
                >
                  Đang khởi tạo camera...
                </div>
              </div>

              <div
                v-if="props.showCameraHint || props.showRetryButton"
                class="mt-4 flex items-center justify-between gap-3"
              >
                <div
                  v-if="props.showCameraHint"
                  class="inline-flex items-center gap-2 text-sm text-slate-200"
                >
                  <Camera :size="16" />
                  {{ cameraHint }}
                </div>

                <button
                  v-if="props.showRetryButton"
                  type="button"
                  class="inline-flex items-center gap-2 rounded-2xl border border-white/15 bg-white/10 px-4 py-2 text-sm font-semibold text-white transition hover:bg-white/15"
                  @click="thuLai"
                >
                  <RefreshCw :size="15" />
                  {{ retryButtonLabel }}
                </button>
              </div>

              <p
                v-if="mergedError"
                class="mt-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-600"
              >
                {{ mergedError }}
              </p>
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
