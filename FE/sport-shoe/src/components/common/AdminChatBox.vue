<script setup>
import { ref, onMounted, nextTick, watch } from "vue";
import { useRouter } from "vue-router";
import { chatWithAdminAi } from "../../services/admin-chatbot";
import { 
  Bot, 
  Send, 
  Trash2, 
  X, 
  MessageSquare, 
  Sparkles,
  TrendingUp,
  AlertTriangle,
  Search,
  Zap
} from "lucide-vue-next";

const router = useRouter();
const isOpen = ref(false);
const messages = ref([]);
const inputText = ref("");
const isSending = ref(false);
const chatContainer = ref(null);

const promptSuggestions = [
  { label: "Doanh thu hôm nay", text: "Thống kê doanh thu hôm nay", icon: TrendingUp },
  { label: "Doanh thu tháng này", text: "Thống kê doanh thu tháng này", icon: TrendingUp },
  { label: "Sản phẩm sắp hết hàng", text: "Sản phẩm sắp hết hàng", icon: AlertTriangle },
  { label: "Sản phẩm bán chạy", text: "Sản phẩm bán chạy nhất", icon: Zap }
];

function parseMessage(text) {
  if (!text) return [];
  const segments = [];
  const linkRegex = /\[([^]]+)\]\(([^)]+)\)/g;
  let lastIndex = 0;
  let match;

  while ((match = linkRegex.exec(text)) !== null) {
    const matchIndex = match.index;
    if (matchIndex > lastIndex) {
      segments.push({
        type: "text",
        content: text.substring(lastIndex, matchIndex)
      });
    }
    const linkText = match[1].replace(/\*\*/g, "");
    segments.push({
      type: "link",
      text: linkText,
      url: match[2]
    });
    lastIndex = linkRegex.lastIndex;
  }

  if (lastIndex < text.length) {
    segments.push({
      type: "text",
      content: text.substring(lastIndex)
    });
  }

  return segments;
}

function renderText(text) {
  if (!text) return "";
  let escaped = text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
  return escaped.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
}

function handleNavigate(url) {
  if (url.startsWith("/")) {
    router.push(url);
  } else {
    window.open(url, "_blank");
  }
}

function cuonXuongCuoi() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }
  });
}

function layLichSuChat() {
  try {
    const saved = localStorage.getItem("admin_chatbot_history");
    if (saved) {
      messages.value = JSON.parse(saved);
    } else {
      messages.value = [
        {
          id: "welcome",
          sender: "AI",
          content: "Xin chào Quản trị viên! Tôi là trợ lý ảo hỗ trợ quản lý SportShoe. Tôi có thể giúp bạn thống kê doanh thu, kiểm tra sản phẩm hết hàng hoặc tìm kiếm hóa đơn nhanh chóng.",
          time: new Date().toISOString()
        }
      ];
    }
  } catch (e) {
    console.error("Lỗi đọc lịch sử chat admin:", e);
  }
}

function luuLichSuChat() {
  try {
    localStorage.setItem("admin_chatbot_history", JSON.stringify(messages.value));
  } catch (e) {
    console.error("Lỗi lưu lịch sử chat admin:", e);
  }
}

function xoaLichSu() {
  messages.value = [
    {
      id: "welcome",
      sender: "AI",
      content: "Đã xóa lịch sử chat. Tôi có thể giúp gì thêm cho bạn?",
      time: new Date().toISOString()
    }
  ];
  luuLichSuChat();
}

async function guiTinNhan(contentStr) {
  const msgText = contentStr || inputText.value.trim();
  if (!msgText || isSending.value) return;

  if (!contentStr) {
    inputText.value = "";
  }

  // Push user message
  const userMsg = {
    id: Date.now().toString(),
    sender: "USER",
    content: msgText,
    time: new Date().toISOString()
  };
  messages.value.push(userMsg);
  luuLichSuChat();
  cuonXuongCuoi();

  isSending.value = true;
  try {
    const res = await chatWithAdminAi(msgText);
    const aiMsg = {
      id: (Date.now() + 1).toString(),
      sender: "AI",
      content: res.reply || "Tôi không nhận được phản hồi hợp lệ từ AI.",
      time: new Date().toISOString()
    };
    messages.value.push(aiMsg);
    luuLichSuChat();
  } catch (e) {
    console.error("Lỗi chat chatbot admin:", e);
    const errMsg = {
      id: (Date.now() + 1).toString(),
      sender: "AI",
      content: "Hệ thống AI hiện đang bận hoặc có lỗi xảy ra. Vui lòng thử lại sau.",
      time: new Date().toISOString()
    };
    messages.value.push(errMsg);
  } finally {
    isSending.value = false;
    cuonXuongCuoi();
  }
}

onMounted(() => {
  layLichSuChat();
  cuonXuongCuoi();
});

watch(isOpen, (newVal) => {
  if (newVal) {
    cuonXuongCuoi();
  }
});
</script>

<template>
  <div class="fixed bottom-6 right-6 z-[9999] font-sans">
    <!-- Chat Button -->
    <button
      v-show="!isOpen"
      @click="isOpen = true"
      class="group relative flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-r from-red-600 to-rose-700 text-white shadow-lg transition hover:scale-105 hover:shadow-xl active:scale-95"
    >
      <span class="absolute -top-1 -right-1 flex h-4 w-4">
        <span class="absolute inline-flex h-full w-full animate-ping rounded-full bg-rose-400 opacity-75"></span>
        <span class="relative inline-flex h-4 w-4 rounded-full bg-rose-500"></span>
      </span>
      <Bot class="h-6 w-6 transition group-hover:rotate-12" />
    </button>

    <!-- Chat Window -->
    <div
      v-show="isOpen"
      class="flex h-[560px] w-[380px] flex-col overflow-hidden rounded-[24px] border border-slate-100 bg-white shadow-2xl shadow-slate-200 dark:border-slate-800 dark:bg-slate-900"
    >
      <!-- Header -->
      <div class="flex items-center justify-between bg-gradient-to-r from-red-600 to-rose-700 px-5 py-4 text-white">
        <div class="flex items-center gap-3">
          <div class="rounded-xl bg-white/10 p-2">
            <Sparkles class="h-5 w-5" />
          </div>
          <div>
            <h3 class="text-sm font-bold tracking-wide">Trợ Lý Admin AI</h3>
            <p class="text-[10px] text-rose-200">Trực tuyến</p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button
            @click="xoaLichSu"
            title="Xóa lịch sử trò chuyện"
            class="rounded-lg p-1.5 transition hover:bg-white/10"
          >
            <Trash2 class="h-4.5 w-4.5" />
          </button>
          <button
            @click="isOpen = false"
            class="rounded-lg p-1.5 transition hover:bg-white/10"
          >
            <X class="h-4.5 w-4.5" />
          </button>
        </div>
      </div>

      <!-- Messages list -->
      <div
        ref="chatContainer"
        class="flex-1 overflow-y-auto bg-slate-50/50 p-4 space-y-4 dark:bg-slate-900/50 scrollbar-thin"
      >
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="flex"
          :class="msg.sender === 'USER' ? 'justify-end' : 'justify-start'"
        >
          <div
            class="max-w-[85%] rounded-[18px] px-4 py-2.5 text-sm shadow-sm"
            :class="msg.sender === 'USER' 
              ? 'bg-[#B82220] text-white rounded-br-none' 
              : 'bg-white text-slate-800 rounded-bl-none border border-slate-100 dark:bg-slate-800 dark:text-slate-100 dark:border-slate-700'"
          >
            <!-- Content -->
            <div class="leading-relaxed">
              <span v-for="(seg, idx) in parseMessage(msg.content)" :key="idx">
                <span v-if="seg.type === 'text'" v-html="renderText(seg.content)"></span>
                <button
                  v-else-if="seg.type === 'link'"
                  @click="handleNavigate(seg.url)"
                  class="mx-1 inline-flex items-center gap-1 rounded-lg bg-rose-50 border border-rose-100 px-2 py-1 text-xs font-bold text-rose-600 hover:bg-rose-100 transition duration-150 dark:bg-rose-900/10 dark:border-rose-900/20 dark:text-rose-400"
                >
                  {{ seg.text }}
                </button>
              </span>
            </div>
          </div>
        </div>

        <!-- Typing indicator -->
        <div v-if="isSending" class="flex justify-start">
          <div class="flex items-center gap-1 bg-white border border-slate-100 rounded-[18px] px-4 py-3 dark:bg-slate-800 dark:border-slate-700">
            <span class="h-2 w-2 animate-bounce rounded-full bg-rose-500" style="animation-delay: 0ms"></span>
            <span class="h-2 w-2 animate-bounce rounded-full bg-rose-500" style="animation-delay: 150ms"></span>
            <span class="h-2 w-2 animate-bounce rounded-full bg-rose-500" style="animation-delay: 300ms"></span>
          </div>
        </div>
      </div>

      <!-- Quick suggestions -->
      <div v-if="messages.length <= 1" class="border-t border-slate-50 p-3 bg-slate-50/30 flex flex-wrap gap-2 dark:border-slate-800 dark:bg-slate-900/30">
        <button
          v-for="sug in promptSuggestions"
          :key="sug.label"
          @click="guiTinNhan(sug.text)"
          class="flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-xs text-slate-600 shadow-sm transition hover:border-rose-300 hover:text-rose-600 hover:bg-rose-50 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300"
        >
          <component :is="sug.icon" class="h-3.5 w-3.5" />
          {{ sug.label }}
        </button>
      </div>

      <!-- Input area -->
      <div class="border-t border-slate-100 p-4 bg-white dark:border-slate-800 dark:bg-slate-900">
        <form @submit.prevent="guiTinNhan()" class="flex items-center gap-2">
          <input
            v-model="inputText"
            type="text"
            placeholder="Nhập câu hỏi hỗ trợ quản trị..."
            :disabled="isSending"
            class="flex-1 rounded-xl border border-slate-200 bg-slate-50/50 px-4 py-2.5 text-sm text-slate-700 placeholder-slate-400 focus:border-rose-500 focus:bg-white focus:outline-none disabled:opacity-50 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
          />
          <button
            type="submit"
            :disabled="!inputText.trim() || isSending"
            class="flex h-10 w-10 items-center justify-center rounded-xl bg-[#B82220] text-white shadow transition hover:bg-[#B82220]/95 focus:outline-none disabled:opacity-40 disabled:cursor-not-allowed"
          >
            <Send class="h-4.5 w-4.5" />
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.scrollbar-thin::-webkit-scrollbar {
  width: 5px;
}
.scrollbar-thin::-webkit-scrollbar-track {
  background: transparent;
}
.scrollbar-thin::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}
.scrollbar-thin::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
