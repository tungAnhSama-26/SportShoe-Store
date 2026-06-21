<script setup>
import { ref, onMounted, nextTick, watch, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { 
  guiTinNhanClient, 
  yeuCauNhanVien, 
  layTinNhanClient 
} from "../../services/chatbot";
import { useRealtime } from "../../composables/useRealtime";
import { 
  MessageCircle, 
  X, 
  Send, 
  Sparkles, 
  Headphones, 
  Minus, 
  User, 
  AlertCircle 
} from "lucide-vue-next";

const { subscribeTopic, unsubscribeTopic } = useRealtime();
const router = useRouter();

const isOpen = ref(false);
const sessionId = ref(null);

function parseMessage(text) {
  if (!text) return [];
  const segments = [];
  const linkRegex = /\[([^\]]+)\]\(([^)]+)\)/g;
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
const sessionState = ref(1); // 1: AI, 2: Chờ Staff, 3: Đang chat Staff, 4: Đã đóng
const messages = ref([]);
const inputText = ref("");
const isSending = ref(false);
const chatContainer = ref(null);

let sessionSubscription = null;

const promptSuggestions = [
  "Tìm đôi giày chạy bộ màu trắng",
  "Mẫu giày nào đang bán chạy nhất?",
  "Tôi muốn được tư vấn chọn size giày",
  "Liên hệ trực tiếp với nhân viên"
];

// Khởi tạo và đồng bộ phiên chat cũ từ localStorage
async function KhoiTaoChatbox() {
  const savedSessionId = localStorage.getItem("chatbot_session_id");
  if (savedSessionId) {
    sessionId.value = parseInt(savedSessionId);
    try {
      const history = await layTinNhanClient(sessionId.value);
      messages.value = history || [];
      
      // Kiểm tra trạng thái phiên thông qua tin nhắn cuối cùng hoặc api (tạm thời lấy thông tin)
      if (messages.value.length > 0) {
        // Dự kiến đồng bộ WebSocket
        DongBoWebsocket(sessionId.value);
      }
      CuonXuongCuoi();
    } catch (e) {
      console.error("Không thể tải lịch sử chat cũ:", e);
      localStorage.removeItem("chatbot_session_id");
      sessionId.value = null;
    }
  }
}

// Kết nối WebSocket cho session cụ thể
function DongBoWebsocket(id) {
  if (sessionSubscription) {
    unsubscribeTopic(sessionSubscription);
  }
  
  sessionSubscription = subscribeTopic(`/topic/chatbot/session/${id}`, (payload) => {
    if (payload.type === "NEW_MESSAGE") {
      const msg = payload.payload;
      // Tránh trùng tin nhắn do client tự append trước đó bằng cách kiểm tra cả ID và nội dung
      const existsIndex = messages.value.findIndex(m => 
        m.id === msg.id || 
        (m.nguoiGui === msg.nguoiGui && m.noiDung === msg.noiDung)
      );
      if (existsIndex > -1) {
        // Cập nhật lại ID chính xác từ database
        messages.value[existsIndex].id = msg.id;
      } else {
        messages.value.push(msg);
        CuonXuongCuoi();
      }
    } else if (payload.type === "STATE_CHANGED") {
      sessionState.value = payload.payload;
    }
  });
}

// Gửi tin nhắn lên hệ thống
async function GuiTinNhan(textToSend) {
  const text = textToSend || inputText.value.trim();
  if (!text) return;

  if (!textToSend) {
    inputText.value = "";
  }

  // Nếu người dùng chọn nút Kết nối nhân viên tư vấn
  if (text === "Liên hệ trực tiếp với nhân viên") {
    YeuCauKetNoiNhanVien();
    return;
  }

  // Lấy thông tin tài khoản nếu đã đăng nhập
  let customerName = "Khách hàng";
  let phoneNumber = "";
  try {
    const userRaw = localStorage.getItem("user");
    if (userRaw) {
      const user = JSON.parse(userRaw);
      if (user.hoTen) customerName = user.hoTen;
      if (user.sdt) phoneNumber = user.sdt;
    }
  } catch (e) {
    console.error("Lỗi lấy thông tin đăng nhập:", e);
  }

  // Hiển thị tin nhắn của khách hàng ngay lập tức trên UI
  const tempUserMsgId = Date.now();
  messages.value.push({
    id: tempUserMsgId,
    nguoiGui: "CUSTOMER",
    noiDung: text,
    ngayTao: new Date().toISOString()
  });

  isSending.value = true;
  CuonXuongCuoi();

  try {
    const res = await guiTinNhanClient(text, sessionId.value, customerName, phoneNumber);
    
    // Hiển thị tin nhắn trả lời của AI ngay lập tức trên UI từ kết quả HTTP
    if (res && res.message) {
      messages.value.push({
        id: Date.now() + 1,
        nguoiGui: "AI",
        noiDung: res.message,
        ngayTao: new Date().toISOString()
      });
    }

    // Lưu session ID nếu là lần chat đầu tiên
    if (!sessionId.value && res.sessionId) {
      sessionId.value = res.sessionId;
      localStorage.setItem("chatbot_session_id", res.sessionId);
      DongBoWebsocket(res.sessionId);
    }

    sessionState.value = res.trangThai;
    CuonXuongCuoi();
  } catch (error) {
    console.error("Lỗi khi gửi tin nhắn chatbot:", error);
    messages.value.push({
      id: Date.now(),
      nguoiGui: "AI",
      noiDung: "Không thể kết nối đến máy chủ AI. Bạn vui lòng thử lại sau.",
      ngayTao: new Date().toISOString()
    });
  } finally {
    isSending.value = false;
    CuonXuongCuoi();
  }
}

// Yêu cầu kết nối với nhân viên trực
async function YeuCauKetNoiNhanVien() {
  if (!sessionId.value) {
    // Nếu chưa chat bao giờ mà click luôn nút hỗ trợ, cần tạo session trước
    isSending.value = true;
    try {
      const res = await guiTinNhanClient("Tôi muốn gặp nhân viên trực tiếp hỗ trợ", null, "Khách hàng", "");
      sessionId.value = res.sessionId;
      localStorage.setItem("chatbot_session_id", res.sessionId);
      DongBoWebsocket(res.sessionId);
    } catch (e) {
      console.error(e);
      isSending.value = false;
      return;
    }
  }

  try {
    await yeuCauNhanVien(sessionId.value);
    sessionState.value = 2; // Đang chờ hỗ trợ
    CuonXuongCuoi();
  } catch (error) {
    console.error("Lỗi khi yêu cầu nhân viên hỗ trợ:", error);
  } finally {
    isSending.value = false;
  }
}

// Hủy chat hiện tại để bắt đầu chat mới
function LamMoiPhienChat() {
  localStorage.removeItem("chatbot_session_id");
  if (sessionSubscription) {
    unsubscribeTopic(sessionSubscription);
    sessionSubscription = null;
  }
  sessionId.value = null;
  sessionState.value = 1;
  messages.value = [];
}

// Cuộn hộp thoại chat xuống cuối
function CuonXuongCuoi() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }
  });
}

// Toggle mở rộng / thu nhỏ box chat
function ToggleChat() {
  isOpen.value = !isOpen.value;
  if (isOpen.value) {
    CuonXuongCuoi();
  }
}

onMounted(() => {
  KhoiTaoChatbox();
});

onUnmounted(() => {
  if (sessionSubscription) {
    unsubscribeTopic(sessionSubscription);
  }
});
</script>

<template>
  <div class="fixed bottom-6 right-6 z-[9999] flex flex-col items-end">
    <!-- Chat Widget Window -->
    <div 
      v-show="isOpen"
      class="mb-4 w-96 h-[500px] bg-white dark:bg-slate-800 rounded-3xl shadow-2xl border border-slate-100 dark:border-slate-700/50 overflow-hidden flex flex-col transition-all duration-300"
    >
      <!-- Chat Header -->
      <div class="px-5 py-4 bg-gradient-to-r from-primary to-[#4f46e5] text-white flex items-center justify-between shrink-0">
        <div class="flex items-center space-x-2.5">
          <div class="h-9 w-9 rounded-full bg-white/10 flex items-center justify-center">
            <Sparkles class="h-4.5 w-4.5 text-white" />
          </div>
          <div>
            <h4 class="font-bold text-sm leading-none flex items-center gap-1">
              Trợ lý ảo SportShoe
            </h4>
            <span class="text-[10px] text-white/80 mt-1 block">
              <span v-if="sessionState === 1" class="flex items-center gap-1">
                <span class="h-1.5 w-1.5 rounded-full bg-emerald-400"></span> AI sẵn sàng phản hồi
              </span>
              <span v-else-if="sessionState === 2" class="flex items-center gap-1">
                <span class="h-1.5 w-1.5 rounded-full bg-yellow-400 animate-pulse"></span> Đang chờ nhân viên kết nối
              </span>
              <span v-else-if="sessionState === 3" class="flex items-center gap-1">
                <span class="h-1.5 w-1.5 rounded-full bg-sky-400"></span> Nhân viên đang tư vấn
              </span>
              <span v-else-if="sessionState === 4" class="flex items-center gap-1">
                <span class="h-1.5 w-1.5 rounded-full bg-rose-400"></span> Phiên trò chuyện đã đóng
              </span>
            </span>
          </div>
        </div>

        <div class="flex items-center space-x-2">
          <!-- Refresh button -->
          <button 
            v-if="messages.length > 0"
            @click="LamMoiPhienChat"
            title="Bắt đầu cuộc trò chuyện mới"
            class="text-white/70 hover:text-white hover:bg-white/10 p-1.5 rounded-lg transition-colors"
          >
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 1 1 21.306 7M7 9a4.5 4.5 0 1 0 0-9 4.5 4.5 0 0 0 0 9z"/>
            </svg>
          </button>
          <!-- Close button -->
          <button @click="ToggleChat" class="text-white/70 hover:text-white hover:bg-white/10 p-1.5 rounded-lg transition-colors">
            <Minus class="h-4.5 w-4.5" />
          </button>
        </div>
      </div>

      <!-- Messages Body Area -->
      <div 
        ref="chatContainer"
        class="flex-1 overflow-y-auto p-4 space-y-3 bg-slate-50/50 dark:bg-slate-900/10"
      >
        <!-- Empty State / Welcome Screen -->
        <div v-if="messages.length === 0" class="py-6 px-2 text-center flex flex-col items-center justify-center space-y-4">
          <div class="h-14 w-14 rounded-full bg-primary/10 flex items-center justify-center text-primary">
            <Sparkles class="h-7 w-7" />
          </div>
          <div>
            <h5 class="font-bold text-slate-800 dark:text-slate-200 text-sm">Xin chào! 👋</h5>
            <p class="text-xs text-slate-500 dark:text-slate-400 mt-1 px-4">
              Tôi là chuyên viên ảo của SportShoe. Tôi có thể hỗ trợ bạn tìm giày bán chạy, tư vấn size và tra cứu sản phẩm trong nháy mắt.
            </p>
          </div>

          <!-- Quick Actions Prompt Suggestions -->
          <div class="w-full space-y-1.5 pt-2">
            <button
              v-for="prompt in promptSuggestions"
              :key="prompt"
              @click="GuiTinNhan(prompt)"
              class="w-full text-left bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700/60 hover:bg-primary/5 hover:text-primary dark:hover:bg-slate-700 hover:border-primary/20 p-2.5 rounded-xl text-[11px] text-slate-600 dark:text-slate-300 font-medium transition-all shadow-sm"
            >
              {{ prompt }}
            </button>
          </div>
        </div>

        <!-- Chat messages bubbles -->
        <template v-else>
          <div 
            v-for="msg in messages" 
            :key="msg.id"
            class="flex flex-col"
            :class="[msg.nguoiGui === 'CUSTOMER' ? 'items-end' : 'items-start']"
          >
            <!-- Sender details -->
            <div class="flex items-center space-x-1.5 mb-0.5 text-[10px] text-slate-400 px-1">
              <span v-if="msg.nguoiGui === 'CUSTOMER'">Bạn</span>
              <span v-else-if="msg.nguoiGui === 'STAFF'" class="text-primary font-bold flex items-center">
                <Headphones class="h-3 w-3 mr-0.5" /> Nhân viên trực
              </span>
              <span v-else class="text-purple-600 font-bold flex items-center">
                <Sparkles class="h-3 w-3 mr-0.5" /> Trợ lý AI
              </span>
            </div>

            <!-- Message bubble -->
            <div 
              class="max-w-[80%] rounded-2xl px-3.5 py-2 text-xs shadow-sm leading-relaxed"
              :class="[
                msg.nguoiGui === 'CUSTOMER'
                  ? 'bg-primary text-white rounded-tr-none'
                  : msg.nguoiGui === 'STAFF'
                    ? 'bg-sky-50 text-sky-950 dark:bg-sky-950/20 dark:text-sky-300 border border-sky-100 dark:border-sky-900/30 rounded-tl-none font-medium'
                    : 'bg-white text-slate-800 dark:bg-slate-800 dark:text-slate-100 border border-slate-100 dark:border-slate-700/50 rounded-tl-none'
              ]"
            >
              <div class="space-y-1.5 whitespace-pre-wrap">
                <template v-for="(seg, idx) in parseMessage(msg.noiDung)" :key="idx">
                  <span v-if="seg.type === 'text'" v-html="renderText(seg.content)"></span>
                  <button 
                    v-else-if="seg.type === 'link'" 
                    @click="handleNavigate(seg.url)"
                    class="inline-flex items-center justify-center px-4 py-2.5 mt-1.5 font-bold text-xs bg-gradient-to-r from-primary to-[#4f46e5] hover:opacity-90 text-white rounded-xl shadow-md transition-all cursor-pointer gap-1.5 w-full text-center"
                  >
                    <svg class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                    {{ seg.text }}
                  </button>
                </template>
              </div>
            </div>
          </div>

          <!-- AI Loading bubble -->
          <div v-if="isSending" class="flex flex-col items-start">
            <div class="flex items-center space-x-1 mb-0.5 text-[10px] text-slate-400 px-1">
              <Sparkles class="h-3 w-3 text-purple-600 animate-spin" />
              <span class="text-purple-600 font-bold">AI đang viết...</span>
            </div>
            <div class="bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700/50 rounded-2xl rounded-tl-none px-3.5 py-3 shadow-sm flex items-center space-x-1">
              <div class="h-1.5 w-1.5 bg-slate-400 rounded-full animate-bounce"></div>
              <div class="h-1.5 w-1.5 bg-slate-400 rounded-full animate-bounce [animation-delay:0.2s]"></div>
              <div class="h-1.5 w-1.5 bg-slate-400 rounded-full animate-bounce [animation-delay:0.4s]"></div>
            </div>
          </div>
        </template>
      </div>

      <!-- Human Support Trigger Banner -->
      <div 
        v-if="messages.length > 0 && sessionState === 1"
        class="bg-purple-50 dark:bg-purple-950/20 border-t border-b border-purple-100 dark:border-purple-900/30 px-4 py-2.5 flex items-center justify-between shrink-0"
      >
        <span class="text-[10px] text-purple-800 dark:text-purple-400 font-medium">Bạn chưa tìm thấy thông tin phù hợp?</span>
        <button 
          @click="YeuCauKetNoiNhanVien"
          class="inline-flex items-center gap-1 bg-white hover:bg-purple-100 dark:bg-slate-800 px-2.5 py-1.5 rounded-lg text-[10px] font-bold text-primary border border-purple-200 dark:border-purple-900/40 shadow-sm transition-colors"
        >
          <Headphones class="h-3.5 w-3.5" />
          Gặp nhân viên
        </button>
      </div>

      <!-- Closed status notice -->
      <div 
        v-if="sessionState === 4"
        class="bg-slate-100 dark:bg-slate-700 px-4 py-3 flex items-center justify-center shrink-0"
      >
        <span class="text-[10px] text-slate-500 dark:text-slate-400 font-bold flex items-center gap-1.5">
          <AlertCircle class="h-4 w-4" />
          Cuộc chat đã đóng. Click nút tải lại để bắt đầu cuộc trò chuyện mới.
        </span>
      </div>

      <!-- Message Input field -->
      <div 
        v-if="sessionState !== 4"
        class="px-4 py-3 border-t border-slate-100 dark:border-slate-700/50 bg-white dark:bg-slate-800 shrink-0"
      >
        <form @submit.prevent="GuiTinNhan()" class="flex items-center gap-2">
          <input
            v-model="inputText"
            type="text"
            :disabled="sessionState === 2"
            placeholder="Nhập câu hỏi của bạn tại đây..."
            class="flex-1 bg-slate-50 dark:bg-slate-700 border border-slate-100 dark:border-slate-600 rounded-2xl px-4 py-2.5 text-xs text-slate-800 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all disabled:opacity-50"
          />
          <button 
            type="submit"
            :disabled="!inputText.trim() || sessionState === 2"
            class="bg-primary hover:bg-primary-hover disabled:bg-slate-100 disabled:text-slate-300 text-white rounded-2xl p-2.5 shadow-sm transition-all shrink-0"
          >
            <Send class="h-4 w-4" />
          </button>
        </form>
      </div>
    </div>

    <!-- Floating Chat Widget Button -->
    <button
      @click="ToggleChat"
      class="h-14 w-14 rounded-full bg-gradient-to-r from-primary to-[#4f46e5] text-white flex items-center justify-center shadow-xl hover:scale-105 active:scale-95 transition-all focus:outline-none"
    >
      <X v-if="isOpen" class="h-6 w-6" />
      <MessageCircle v-else class="h-6 w-6" />
    </button>
  </div>
</template>

<style scoped>
.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: .5;
    transform: scale(1.1);
  }
}
</style>
