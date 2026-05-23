<script setup>
import { computed, ref } from "vue";
import { Check, Copy, Download, Images, QrCode, X } from "lucide-vue-next";
import { createQrCodeSvg } from "../../utils/qr-code";

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  badge: {
    type: String,
    default: "QR sản phẩm",
  },
  title: {
    type: String,
    default: "",
  },
  subtitle: {
    type: String,
    default: "",
  },
  value: {
    type: String,
    default: "",
  },
  codeLabel: {
    type: String,
    default: "Mã quét",
  },
  note: {
    type: String,
    default:
      "Mã này có thể dùng trực tiếp ở màn bán hàng để quét hoặc nhập tay.",
  },
  imageUrl: {
    type: String,
    default: "",
  },
  imageAlt: {
    type: String,
    default: "Ảnh sản phẩm",
  },
  detailItems: {
    type: Array,
    default: () => [],
  },
  primaryActionLabel: {
    type: String,
    default: "",
  },
  discountBadge: {
    type: String,
    default: "",
  },
});

const emit = defineEmits(["close", "primary-action"]);

const copying = ref(false);
const copied = ref(false);

const normalizedValue = computed(() => String(props.value ?? "").trim());
const hasImage = computed(() => Boolean(String(props.imageUrl ?? "").trim()));

const qrPreview = computed(() => {
  if (!normalizedValue.value) {
    return {
      svg: "",
      error: "Không có dữ liệu để tạo QR",
    };
  }

  try {
    return {
      svg: createQrCodeSvg(normalizedValue.value, {
        title: props.title || props.badge,
        description: props.subtitle || props.note,
      }),
      error: "",
    };
  } catch (error) {
    return {
      svg: "",
      error:
        error instanceof Error
          ? error.message
          : "Không thể tạo mã QR lúc này",
    };
  }
});

function closeModal() {
  emit("close");
}

function handlePrimaryAction() {
  emit("primary-action");
}

function createSafeFilename(value) {
  return (
    String(value ?? "qr-code")
      .trim()
      .replace(/[\\/:*?"<>|]+/g, "-")
      .replace(/\s+/g, "-")
      .replace(/-+/g, "-")
      .replace(/^-|-$/g, "")
      .toLowerCase() || "qr-code"
  );
}

async function copyCode() {
  if (!normalizedValue.value || copying.value) {
    return;
  }

  copying.value = true;
  try {
    if (navigator?.clipboard?.writeText) {
      await navigator.clipboard.writeText(normalizedValue.value);
    } else {
      const textArea = document.createElement("textarea");
      textArea.value = normalizedValue.value;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand("copy");
      document.body.removeChild(textArea);
    }

    copied.value = true;
    window.setTimeout(() => {
      copied.value = false;
    }, 1800);
  } finally {
    copying.value = false;
  }
}

function downloadSvg() {
  if (!qrPreview.value.svg) {
    return;
  }

  const blob = new Blob([qrPreview.value.svg], {
    type: "image/svg+xml;charset=utf-8",
  });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `${createSafeFilename(normalizedValue.value)}.svg`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(link.href);
}
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="open"
        class="fixed inset-0 z-[95] flex items-center justify-center bg-slate-950/55 p-4"
        @click.self="closeModal"
      >
        <div
          class="w-full max-w-4xl overflow-hidden rounded-[30px] border border-white/70 bg-white shadow-[0_26px_70px_rgba(15,23,42,0.24)]"
        >
          <div
            class="flex items-start justify-between gap-4 border-b border-slate-100 px-6 py-5"
          >
            <div>
              <div
                class="inline-flex items-center gap-2 rounded-full bg-red-50 px-3 py-1 text-sm font-semibold text-red-600"
              >
                <QrCode :size="16" />
                {{ badge }}
              </div>
              <h2 class="mt-3 text-2xl font-black text-slate-900">
                {{ title || "Xem mã QR sản phẩm" }}
              </h2>
              <p v-if="subtitle" class="mt-2 text-sm text-slate-500">
                {{ subtitle }}
              </p>
            </div>

            <button
              type="button"
              class="inline-flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-500 transition hover:bg-slate-200"
              @click="closeModal"
            >
              <X :size="18" />
            </button>
          </div>

          <div class="grid gap-4 p-5 lg:grid-cols-[0.95fr_1.05fr]">
            <div class="space-y-4">
              <div
                class="overflow-hidden rounded-[24px] border border-slate-200 bg-[radial-gradient(circle_at_top,#ffffff_0%,#f8fafc_52%,#eef2ff_100%)] p-4"
              >
                <div
                  v-if="hasImage"
                  class="relative flex aspect-[4/3] items-center justify-center overflow-hidden rounded-[20px] border border-slate-200 bg-white p-3 shadow-sm"
                >
                  <img
                    :src="imageUrl"
                    :alt="imageAlt"
                    class="h-full w-full object-contain"
                  />
                </div>
                <div
                  v-else
                  class="flex aspect-[4/3] items-center justify-center rounded-[20px] border border-dashed border-slate-200 bg-white text-slate-400"
                >
                  <div class="text-center">
                    <div
                      class="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-100 text-slate-400"
                    >
                      <Images :size="20" />
                    </div>
                    <p class="mt-3 text-sm font-medium">Chưa có ảnh hiển thị</p>
                  </div>
                </div>
              </div>

              <div
                class="flex items-center justify-center rounded-[24px] border border-slate-200 bg-[radial-gradient(circle_at_top,#ffffff_0%,#f8fafc_52%,#eef2ff_100%)] p-4"
              >
                <div
                  class="w-full max-w-[160px] rounded-[20px] border border-slate-200 bg-white p-3 shadow-sm"
                >
                  <div
                    v-if="qrPreview.svg"
                    class="aspect-square w-full"
                    v-html="qrPreview.svg"
                  />
                  <div
                    v-else
                    class="flex aspect-square w-full items-center justify-center rounded-[20px] border border-dashed border-rose-200 bg-rose-50 px-6 text-center text-sm font-medium text-rose-500"
                  >
                    {{ qrPreview.error }}
                  </div>
                </div>
              </div>
            </div>

            <div class="space-y-4 rounded-[24px] border border-slate-200 bg-slate-50/80 p-4">
              <div>
                <p
                  class="text-sm font-semibold uppercase tracking-[0.16em] text-slate-400"
                >
                  {{ codeLabel }}
                </p>
                <p class="mt-2 break-all text-2xl font-black text-slate-900">
                  {{ normalizedValue || "—" }}
                </p>
              </div>

              <div
                v-if="detailItems.length"
                class="grid gap-3 rounded-2xl border border-slate-200 bg-white p-4 sm:grid-cols-2"
              >
                <div
                  v-for="item in detailItems"
                  :key="item.label"
                  class="rounded-2xl bg-slate-50 px-3 py-3"
                >
                  <p class="text-xs font-semibold uppercase tracking-[0.12em] text-slate-400">
                    {{ item.label }}
                  </p>
                  <p class="mt-1 text-sm font-semibold text-slate-800">
                    {{ item.value || "—" }}
                  </p>
                </div>
              </div>

              <div class="grid gap-3 sm:grid-cols-2">
                <button
                  type="button"
                  class="inline-flex items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-300 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-60"
                  :disabled="!normalizedValue"
                  @click="copyCode"
                >
                  <Check v-if="copied" :size="16" />
                  <Copy v-else :size="16" />
                  {{ copied ? "Đã sao chép" : "Sao chép mã" }}
                </button>

                <button
                  type="button"
                  class="inline-flex items-center justify-center gap-2 rounded-2xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
                  :disabled="!qrPreview.svg"
                  @click="downloadSvg"
                >
                  <Download :size="16" />
                  Tải QR SVG
                </button>
              </div>

              <button
                v-if="primaryActionLabel"
                type="button"
                class="inline-flex w-full items-center justify-center rounded-2xl bg-rose-500 px-4 py-3 text-sm font-semibold text-white transition hover:bg-rose-600"
                @click="handlePrimaryAction"
              >
                {{ primaryActionLabel }}
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
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
