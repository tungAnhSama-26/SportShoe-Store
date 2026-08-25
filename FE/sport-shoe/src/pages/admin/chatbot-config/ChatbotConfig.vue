<script setup>
import { ref, onMounted } from "vue";
import { Eye, EyeOff, Save, Loader2, Cpu, KeyRound, AlertCircle } from "lucide-vue-next";
import { apiRequest } from "../../../services/api-client";
import Swal from "sweetalert2";

const keys = ref({
  openaiApiKey: "",
  geminiApiKey: "",
  deepseekApiKey: "",
  groqApiKey: ""
});

const visibility = ref({
  openai: false,
  gemini: false,
  deepseek: false,
  groq: false
});

const loading = ref(false);
const saving = ref(false);
const localStatus = ref({
  enabled: true,
  reachable: false,
  modelAvailable: false,
  model: "qwen3:4b-instruct-2507-q4_K_M",
  message: "Đang kiểm tra Ollama...",
  providerOrder: []
});

async function fetchConfig() {
  loading.value = true;
  try {
    const data = await apiRequest("/admin/chatbot/config", { method: "GET" });
    if (data) {
      keys.value = {
        openaiApiKey: data.openaiApiKey || "",
        geminiApiKey: data.geminiApiKey || "",
        deepseekApiKey: data.deepseekApiKey || "",
        groqApiKey: data.groqApiKey || ""
      };
      localStatus.value = {
        enabled: data.localFallbackEnabled !== false,
        reachable: data.ollamaReachable === true,
        modelAvailable: data.ollamaModelAvailable === true,
        model: data.ollamaModel || "qwen3:4b-instruct-2507-q4_K_M",
        message: data.ollamaMessage || "Chưa có trạng thái Ollama",
        providerOrder: data.providerOrder || []
      };
    }
  } catch (error) {
    console.error("Lỗi lấy cấu hình API Key:", error);
    Swal.fire({
      icon: "error",
      title: "Lỗi tải cấu hình",
      text: error.message || "Không thể tải cấu hình API từ máy chủ.",
      confirmButtonColor: "#B82220"
    });
  } finally {
    loading.value = false;
  }
}

async function saveConfig() {
  saving.value = true;
  try {
    await apiRequest("/admin/chatbot/config", {
      method: "POST",
      body: JSON.stringify(keys.value),
      fallbackMessage: "Không thể lưu cấu hình API Key"
    });
    Swal.fire({
      icon: "success",
      title: "Thành công",
      text: "Cấu hình API Key đã được cập nhật và nạp nóng thành công!",
      confirmButtonColor: "#B82220"
    });
    await fetchConfig();
  } catch (error) {
    console.error("Lỗi lưu cấu hình API Key:", error);
    Swal.fire({
      icon: "error",
      title: "Lưu thất bại",
      text: error.message || "Có lỗi xảy ra khi lưu cấu hình.",
      confirmButtonColor: "#B82220"
    });
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  fetchConfig();
});
</script>

<template>
  <div class="p-6 max-w-4xl mx-auto font-sans">
    <!-- Breadcrumb & Header -->
    <div class="mb-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-slate-800 dark:text-slate-100 flex items-center gap-2">
          <Cpu class="h-6 w-6 text-[#B82220]" />
          Cấu hình API Key Chatbot AI
        </h1>
        <p class="text-sm text-slate-500 mt-1">
          Quản lý các tài khoản AI model và tự động cập nhật cấu hình cho hệ thống trợ lý ảo Admin.
        </p>
      </div>
      <button
        @click="saveConfig"
        :disabled="loading || saving"
        class="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-red-600 to-rose-700 hover:from-red-700 hover:to-rose-800 text-white font-semibold text-sm shadow-md transition-all active:scale-95 disabled:opacity-50"
      >
        <Loader2 v-if="saving" class="h-4 w-4 animate-spin" />
        <Save v-else class="h-4 w-4" />
        Lưu cấu hình
      </button>
    </div>

    <!-- Ollama fallback status -->
    <div class="mb-6 rounded-2xl border p-4"
      :class="localStatus.reachable && localStatus.modelAvailable
        ? 'border-emerald-200 bg-emerald-50 dark:border-emerald-900/40 dark:bg-emerald-950/20'
        : 'border-rose-200 bg-rose-50 dark:border-rose-900/40 dark:bg-rose-950/20'">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div class="flex items-start gap-3">
          <Cpu class="mt-0.5 h-5 w-5 shrink-0"
            :class="localStatus.reachable && localStatus.modelAvailable ? 'text-emerald-600' : 'text-rose-600'" />
          <div>
            <p class="font-bold text-slate-800 dark:text-slate-100">Ollama local fallback</p>
            <p class="mt-1 text-sm text-slate-600 dark:text-slate-300">{{ localStatus.message }}</p>
            <p class="mt-1 text-xs text-slate-500">
              Model: <code class="rounded bg-white/70 px-1.5 py-0.5 dark:bg-slate-900/50">{{ localStatus.model }}</code>
            </p>
          </div>
        </div>
        <span class="self-start rounded-full px-3 py-1 text-xs font-bold sm:self-center"
          :class="localStatus.reachable && localStatus.modelAvailable
            ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/50 dark:text-emerald-300'
            : 'bg-rose-100 text-rose-700 dark:bg-rose-900/50 dark:text-rose-300'">
          {{ localStatus.reachable && localStatus.modelAvailable ? 'Sẵn sàng' : 'Chưa sẵn sàng' }}
        </span>
      </div>
      <div v-if="localStatus.providerOrder.length" class="mt-3 border-t border-black/5 pt-3 text-xs text-slate-500 dark:border-white/10">
        Thứ tự hiện tại:
        <span class="font-semibold text-slate-700 dark:text-slate-200">{{ localStatus.providerOrder.join(' → ') }}</span>
      </div>
      <p class="mt-2 text-xs text-slate-500">
        Cloud được gọi trước. Ollama chỉ xử lý khi các API key không có hoặc tất cả provider cloud đều thất bại.
      </p>
    </div>

    <!-- Alert Box -->
    <div class="mb-6 flex gap-3 p-4 bg-amber-50 border border-amber-200 rounded-2xl dark:bg-amber-950/20 dark:border-amber-900/30">
      <AlertCircle class="h-5 w-5 text-amber-600 shrink-0 mt-0.5 dark:text-amber-400" />
      <div class="text-sm text-amber-800 dark:text-amber-300">
        <span class="font-bold">Lưu ý bảo mật:</span> Các API Key hiện tại được hiển thị dưới dạng mã hóa một phần (ví dụ: <code class="font-mono bg-amber-100 px-1 py-0.5 rounded text-amber-900 dark:bg-amber-900/40 dark:text-amber-200">AQ.Ab8...5vX4Q</code>). Nếu bạn không muốn thay đổi một Key cụ thể, vui lòng giữ nguyên ô nhập liệu của Key đó mà không cần sửa đổi.
      </div>
    </div>

    <!-- Cards Layout -->
    <div class="relative bg-white border border-slate-100 rounded-3xl p-6 shadow-sm dark:bg-slate-800 dark:border-slate-700">
      <!-- Loading Overlay -->
      <div v-if="loading" class="absolute inset-0 bg-white/60 dark:bg-slate-800/60 z-10 flex justify-center items-center rounded-3xl">
        <div class="flex flex-col items-center gap-3">
          <Loader2 class="h-8 w-8 text-[#B82220] animate-spin" />
          <span class="text-sm text-slate-500 font-semibold">Đang tải cấu hình...</span>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Google Gemini -->
        <div class="flex flex-col gap-2 p-4 bg-slate-50 rounded-2xl border border-slate-100 dark:bg-slate-900/30 dark:border-slate-800/40">
          <label class="text-sm font-bold text-slate-700 dark:text-slate-200 flex items-center gap-2">
            <KeyRound class="h-4 w-4 text-emerald-500" />
            Google Gemini API Key
          </label>
          <div class="relative mt-1">
            <input
              :type="visibility.gemini ? 'text' : 'password'"
              v-model="keys.geminiApiKey"
              placeholder="Nhập Google Gemini API Key (Bắt đầu bằng AIzaSy...)"
              class="w-full pl-3 pr-10 py-2.5 rounded-xl border border-slate-200 bg-white text-sm focus:border-[#B82220] focus:ring-1 focus:ring-[#B82220] outline-none transition-all dark:bg-slate-800 dark:border-slate-700 dark:text-white"
            />
            <button
              @click="visibility.gemini = !visibility.gemini"
              type="button"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition"
            >
              <Eye v-if="visibility.gemini" class="h-4 w-4" />
              <EyeOff v-else class="h-4 w-4" />
            </button>
          </div>
          <span class="text-xs text-slate-400">Được dùng mặc định bởi model <code class="bg-slate-100 dark:bg-slate-800 px-1 py-0.5 rounded">gemini-3.5-flash</code>.</span>
        </div>

        <!-- DeepSeek -->
        <div class="flex flex-col gap-2 p-4 bg-slate-50 rounded-2xl border border-slate-100 dark:bg-slate-900/30 dark:border-slate-800/40">
          <label class="text-sm font-bold text-slate-700 dark:text-slate-200 flex items-center gap-2">
            <KeyRound class="h-4 w-4 text-sky-500" />
            DeepSeek API Key
          </label>
          <div class="relative mt-1">
            <input
              :type="visibility.deepseek ? 'text' : 'password'"
              v-model="keys.deepseekApiKey"
              placeholder="Nhập DeepSeek API Key (Bắt đầu bằng sk-...)"
              class="w-full pl-3 pr-10 py-2.5 rounded-xl border border-slate-200 bg-white text-sm focus:border-[#B82220] focus:ring-1 focus:ring-[#B82220] outline-none transition-all dark:bg-slate-800 dark:border-slate-700 dark:text-white"
            />
            <button
              @click="visibility.deepseek = !visibility.deepseek"
              type="button"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition"
            >
              <Eye v-if="visibility.deepseek" class="h-4 w-4" />
              <EyeOff v-else class="h-4 w-4" />
            </button>
          </div>
          <span class="text-xs text-slate-400">Được dùng bởi model <code class="bg-slate-100 dark:bg-slate-800 px-1 py-0.5 rounded">deepseek-chat</code>.</span>
        </div>

        <!-- Groq -->
        <div class="flex flex-col gap-2 p-4 bg-slate-50 rounded-2xl border border-slate-100 dark:bg-slate-900/30 dark:border-slate-800/40">
          <label class="text-sm font-bold text-slate-700 dark:text-slate-200 flex items-center gap-2">
            <KeyRound class="h-4 w-4 text-indigo-500" />
            Groq API Key
          </label>
          <div class="relative mt-1">
            <input
              :type="visibility.groq ? 'text' : 'password'"
              v-model="keys.groqApiKey"
              placeholder="Nhập Groq API Key (Bắt đầu bằng gsk_...)"
              class="w-full pl-3 pr-10 py-2.5 rounded-xl border border-slate-200 bg-white text-sm focus:border-[#B82220] focus:ring-1 focus:ring-[#B82220] outline-none transition-all dark:bg-slate-800 dark:border-slate-700 dark:text-white"
            />
            <button
              @click="visibility.groq = !visibility.groq"
              type="button"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition"
            >
              <Eye v-if="visibility.groq" class="h-4 w-4" />
              <EyeOff v-else class="h-4 w-4" />
            </button>
          </div>
          <span class="text-xs text-slate-400">Được dùng bởi model <code class="bg-slate-100 dark:bg-slate-800 px-1 py-0.5 rounded">llama-3.3-70b-versatile</code>.</span>
        </div>

        <!-- OpenAI -->
        <div class="flex flex-col gap-2 p-4 bg-slate-50 rounded-2xl border border-slate-100 dark:bg-slate-900/30 dark:border-slate-800/40">
          <label class="text-sm font-bold text-slate-700 dark:text-slate-200 flex items-center gap-2">
            <KeyRound class="h-4 w-4 text-rose-500" />
            OpenAI API Key
          </label>
          <div class="relative mt-1">
            <input
              :type="visibility.openai ? 'text' : 'password'"
              v-model="keys.openaiApiKey"
              placeholder="Nhập OpenAI API Key (Bắt đầu bằng sk-...)"
              class="w-full pl-3 pr-10 py-2.5 rounded-xl border border-slate-200 bg-white text-sm focus:border-[#B82220] focus:ring-1 focus:ring-[#B82220] outline-none transition-all dark:bg-slate-800 dark:border-slate-700 dark:text-white"
            />
            <button
              @click="visibility.openai = !visibility.openai"
              type="button"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition"
            >
              <Eye v-if="visibility.openai" class="h-4 w-4" />
              <EyeOff v-else class="h-4 w-4" />
            </button>
          </div>
          <span class="text-xs text-slate-400">Được dùng bởi model <code class="bg-slate-100 dark:bg-slate-800 px-1 py-0.5 rounded">gpt-4o-mini</code>.</span>
        </div>
      </div>
    </div>
  </div>
</template>
