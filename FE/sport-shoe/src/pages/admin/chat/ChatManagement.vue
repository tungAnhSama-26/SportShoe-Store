<script setup>
import { ref, onMounted, nextTick, watch, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { marked } from "marked";
import { 
  layDanhSachPhienAdmin, 
  layTinNhanAdmin, 
  nhanVienPhanHoi, 
  dongPhienChatAdmin,
  layLichSuPhienAdmin
} from "../../../services/chatbot";
import { useRealtime } from "../../../composables/useRealtime";
import { resolveHinhAnh } from "../../../utils/resolve-image";
import anhSanPhamDuPhong from "../../../assets/login-shoe.png";
import { 
  Search, 
  MessageSquare, 
  Send, 
  User, 
  Clock, 
  Phone, 
  XCircle, 
  CheckCircle,
  HelpCircle,
  AlertCircle,
  ExternalLink,
  Tag
} from "lucide-vue-next";
import Swal from "sweetalert2";

const router = useRouter();
const { subscribeTopic, unsubscribeTopic } = useRealtime();

const sessions = ref([]);
const historySessions = ref([]);
const filteredSessions = ref([]);
const activeSession = ref(null);
const messages = ref([]);
const searchKeyword = ref("");
const replyText = ref("");
const chatContainer = ref(null);
const isLoadingSessions = ref(false);
const isLoadingMessages = ref(false);
const isLoadingHistory = ref(false);
const activeTab = ref("active");

// Lưu trữ đối tượng subscribe để hủy khi chuyển session
let activeSessionSubscription = null;
let globalSessionsSubscription = null;

// Tải danh sách phiên chat hoạt động
async function TaiDanhSachPhien() {
  isLoadingSessions.value = true;
  try {
    const data = await layDanhSachPhienAdmin();
    sessions.value = data || [];
    LocDanhSachPhien();
  } catch (error) {
    console.error("Lỗi khi lấy danh sách phiên chat:", error);
  } finally {
    isLoadingSessions.value = false;
  }
}

async function TaiLichSuPhien() {
  isLoadingHistory.value = true;
  try {
    const data = await layLichSuPhienAdmin();
    historySessions.value = data || [];
    LocDanhSachPhien();
  } catch (error) {
    console.error("Lỗi khi lấy lịch sử phiên chat:", error);
  } finally {
    isLoadingHistory.value = false;
  }
}

// Lọc phiên chat theo từ khóa tìm kiếm
function LocDanhSachPhien() {
  const listToFilter = activeTab.value === "active" ? sessions.value : historySessions.value;
  if (!searchKeyword.value.trim()) {
    filteredSessions.value = listToFilter;
  } else {
    const keyword = searchKeyword.value.toLowerCase().trim();
    filteredSessions.value = listToFilter.filter(s => 
      s.tenKhachHang.toLowerCase().includes(keyword) || 
      (s.soDienThoai && s.soDienThoai.includes(keyword))
    );
  }
}

// Chọn một phiên chat để bắt đầu nhắn tin
async function ChonSession(session) {
  if (activeSession.value?.id === session.id) return;
  
  // Hủy đăng ký websocket của session cũ
  if (activeSessionSubscription) {
    unsubscribeTopic(activeSessionSubscription);
    activeSessionSubscription = null;
  }

  activeSession.value = session;
  messages.value = [];
  isLoadingMessages.value = true;

  try {
    const data = await layTinNhanAdmin(session.id);
    messages.value = data || [];
    CuonXuongCuoi();
    
    // Đăng ký nhận tin nhắn mới của session này qua WebSocket
    activeSessionSubscription = subscribeTopic(`/topic/chatbot/session/${session.id}`, (payload) => {
      if (payload.type === "NEW_MESSAGE") {
        messages.value.push(payload.payload);
        CuonXuongCuoi();
      } else if (payload.type === "STATE_CHANGED") {
        const state = payload.payload;
        if (activeSession.value) {
          activeSession.value.trangThai = state;
        }
        // Cập nhật lại danh sách sessions
        const idx = sessions.value.findIndex(s => s.id === session.id);
        if (idx > -1) {
          sessions.value[idx].trangThai = state;
          LocDanhSachPhien();
        }
      }
    });
  } catch (error) {
    console.error("Lỗi khi tải lịch sử tin nhắn:", error);
  } finally {
    isLoadingMessages.value = false;
  }
}

// Gửi câu trả lời của nhân viên
async function GuiPhanHoi() {
  if (!replyText.value.trim() || !activeSession.value) return;

  const text = replyText.value.trim();
  replyText.value = "";

  try {
    await nhanVienPhanHoi(activeSession.value.id, text);
    // Tin nhắn sẽ tự động cập nhật qua WebSocket
  } catch (error) {
    console.error("Lỗi khi gửi tin nhắn:", error);
    Swal.fire({
      icon: "error",
      title: "Lỗi",
      text: "Không thể gửi tin nhắn. Vui lòng thử lại!"
    });
  }
}

// Đóng phiên chat
async function DongPhienChat() {
  if (!activeSession.value) return;

  const result = await Swal.fire({
    title: "Xác nhận đóng cuộc chat?",
    text: "Khách hàng sẽ quay lại trò chuyện với AI tự động.",
    icon: "warning",
    showCancelButton: true,
    confirmButtonColor: "#3085d6",
    cancelButtonColor: "#d33",
    confirmButtonText: "Đồng ý đóng",
    cancelButtonText: "Hủy"
  });

  if (result.isConfirmed) {
    try {
      await dongPhienChatAdmin(activeSession.value.id);
      
      // Cập nhật local
      const id = activeSession.value.id;
      sessions.value = sessions.value.filter(s => s.id !== id);
      LocDanhSachPhien();

      if (activeSessionSubscription) {
        unsubscribeTopic(activeSessionSubscription);
        activeSessionSubscription = null;
      }
      activeSession.value = null;
      messages.value = [];

      Swal.fire("Đã đóng", "Phiên chat đã được đóng thành công.", "success");
    } catch (error) {
      console.error("Lỗi khi đóng phiên chat:", error);
    }
  }
}

// Cuộn khung chat xuống dưới cùng
function CuonXuongCuoi() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }
  });
}

// Format thời gian hiển thị
function FormatThoiGian(instantString) {
  if (!instantString) return "";
  const date = new Date(instantString);
  return date.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" }) + " - " + date.toLocaleDateString("vi-VN");
}

function dinhDangTien(val) {
  if (val === null || val === undefined) return "0 đ";
  const num = Number(val);
  if (isNaN(num)) return "0 đ";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(num);
}

function xuLyLoiAnhSanPham(event) {
  const image = event?.target;
  if (!image || image.dataset.fallbackApplied === "true") return;
  image.dataset.fallbackApplied = "true";
  image.src = anhSanPhamDuPhong;
}

function moChiTietSanPham(url) {
  if (!url) return;
  if (url.startsWith("http")) {
    window.open(url, "_blank");
  } else {
    router.push(url);
  }
}

// Render markdown thành HTML (dùng cho tin nhắn AI/Chatbot)
marked.setOptions({
  breaks: true,
  gfm: true,
});

function renderMarkdown(text) {
  if (!text) return "";
  let escaped = text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
  return marked.parse(escaped);
}

function parseTextAndRawJson(rawText, segments) {
  if (!rawText) return;
  // Match single or multiple line JSON objects representing products
  const jsonRegex = /\{"name"\s*:\s*"[^"]+".*?"price"\s*:\s*\d+.*?\}/g;
  let lastIdx = 0;
  let m;
  while ((m = jsonRegex.exec(rawText)) !== null) {
    if (m.index > lastIdx) {
      const t = rawText.substring(lastIdx, m.index).trim();
      if (t) segments.push({ type: "text", content: t });
    }
    try {
      const parsed = JSON.parse(m[0]);
      segments.push({ type: "product", productData: parsed });
    } catch {
      segments.push({ type: "text", content: m[0] });
    }
    lastIdx = jsonRegex.lastIndex;
  }
  if (lastIdx < rawText.length) {
    const t = rawText.substring(lastIdx).trim();
    if (t) segments.push({ type: "text", content: t });
  }
}

function parseMessage(text) {
  if (!text) return [];
  const segments = [];
  const blockRegex = /```(product|offer|chart)([\s\S]*?)```/g;
  let lastIndex = 0;
  let match;

  while ((match = blockRegex.exec(text)) !== null) {
    const matchIndex = match.index;
    if (matchIndex > lastIndex) {
      const rawText = text.substring(lastIndex, matchIndex);
      parseTextAndRawJson(rawText, segments);
    }
    const blockType = match[1].trim();
    const content = match[2].trim();
    if (blockType === "product") {
      try {
        const productJson = JSON.parse(content);
        segments.push({ type: "product", productData: productJson });
      } catch (e) {
        console.error("Lỗi parse product JSON:", e);
        segments.push({ type: "text", content: match[0] });
      }
    } else if (blockType === "offer") {
      try {
        const offerJson = JSON.parse(content);
        segments.push({ type: "offer", offerData: offerJson });
      } catch (e) {
        segments.push({ type: "text", content: match[0] });
      }
    } else {
      segments.push({ type: "text", content: match[0] });
    }
    lastIndex = blockRegex.lastIndex;
  }

  if (lastIndex < text.length) {
    const rawText = text.substring(lastIndex);
    parseTextAndRawJson(rawText, segments);
  }

  return segments;
}

// Theo dõi thay đổi từ khóa để lọc danh sách
watch(searchKeyword, LocDanhSachPhien);

watch(activeTab, (newTab) => {
  activeSession.value = null;
  messages.value = [];
  if (activeSessionSubscription) {
    unsubscribeTopic(activeSessionSubscription);
    activeSessionSubscription = null;
  }
  if (newTab === "active") {
    TaiDanhSachPhien();
  } else {
    TaiLichSuPhien();
  }
});

onMounted(() => {
  TaiDanhSachPhien();

  // Đăng ký lắng nghe cập nhật danh sách các phiên chat qua WebSocket
  globalSessionsSubscription = subscribeTopic("/topic/chatbot/sessions", (payload) => {
    if (payload.type === "SESSION_UPDATED") {
      const updatedSession = payload.payload;
      
      // Nếu session kết thúc (trang thai = 4), xóa khỏi danh sách hoạt động
      if (updatedSession.trangThai === 4) {
        sessions.value = sessions.value.filter(s => s.id !== updatedSession.id);
        
        // Thêm/cập nhật vào historySessions
        const histIdx = historySessions.value.findIndex(s => s.id === updatedSession.id);
        if (histIdx === -1) {
          historySessions.value.unshift(updatedSession);
        } else {
          historySessions.value[histIdx] = updatedSession;
        }

        if (activeSession.value?.id === updatedSession.id) {
          if (activeTab.value === "active") {
            activeSession.value = null;
            messages.value = [];
            if (activeSessionSubscription) {
              unsubscribeTopic(activeSessionSubscription);
              activeSessionSubscription = null;
            }
          } else {
            activeSession.value.trangThai = 4;
          }
        }
      } else {
        const index = sessions.value.findIndex(s => s.id === updatedSession.id);
        if (index > -1) {
          sessions.value[index] = updatedSession;
        } else {
          sessions.value.unshift(updatedSession);
        }
        
        // Cập nhật ở historySessions nếu có
        const histIdx = historySessions.value.findIndex(s => s.id === updatedSession.id);
        if (histIdx > -1) {
          historySessions.value.splice(histIdx, 1);
        }
      }
      LocDanhSachPhien();
    }
  });
});

onUnmounted(() => {
  if (activeSessionSubscription) {
    unsubscribeTopic(activeSessionSubscription);
  }
  if (globalSessionsSubscription) {
    unsubscribeTopic(globalSessionsSubscription);
  }
});
</script>

<template>
  <div class="h-[calc(100vh-95px)] flex flex-col">
    <!-- Main Chat Workspace -->
    <div class="flex-1 flex bg-white dark:bg-slate-800 rounded-2xl border border-slate-100 dark:border-slate-700/50 shadow-sm overflow-hidden min-h-0">
      
      <!-- Left sidebar: Session list -->
      <div class="w-80 border-r border-slate-100 dark:border-slate-700/50 flex flex-col shrink-0 bg-slate-50/50 dark:bg-slate-800/50">
        <!-- Search bar & Tabs -->
        <div class="p-4 pb-2 border-b border-slate-100 dark:border-slate-700/50 space-y-3">
          <div class="relative">
            <Search class="absolute left-3 top-2.5 h-4 w-4 text-slate-400" />
            <input 
              v-model="searchKeyword"
              type="text" 
              placeholder="Tìm khách hàng, SĐT..." 
              class="w-full bg-white dark:bg-slate-700 border border-slate-200 dark:border-slate-600 rounded-xl pl-9 pr-4 py-2 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            />
          </div>

          <!-- Tabs -->
          <div class="flex bg-slate-100 dark:bg-slate-700 p-0.5 rounded-lg text-xs font-semibold">
            <button 
              @click="activeTab = 'active'"
              class="flex-1 py-1.5 rounded-md text-center transition-all"
              :class="activeTab === 'active' ? 'bg-white dark:bg-slate-600 text-slate-800 dark:text-slate-100 shadow-sm' : 'text-slate-400 hover:text-slate-600 dark:hover:text-slate-200'"
            >
              Đang hoạt động
            </button>
            <button 
              @click="activeTab = 'history'"
              class="flex-1 py-1.5 rounded-md text-center transition-all"
              :class="activeTab === 'history' ? 'bg-white dark:bg-slate-600 text-slate-800 dark:text-slate-100 shadow-sm' : 'text-slate-400 hover:text-slate-600 dark:hover:text-slate-200'"
            >
              Lịch sử chat
            </button>
          </div>
        </div>

        <!-- Session List container -->
        <div class="flex-1 overflow-y-auto p-2 space-y-1">
          <div v-if="isLoadingSessions || isLoadingHistory" class="flex flex-col items-center justify-center py-10 space-y-2">
            <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
            <span class="text-xs text-slate-400">Đang tải danh sách...</span>
          </div>

          <div v-else-if="filteredSessions.length === 0" class="flex flex-col items-center justify-center py-12 text-center px-4">
            <MessageSquare class="h-10 w-10 text-slate-300 dark:text-slate-600 mb-2" />
            <p class="text-sm font-semibold text-slate-600 dark:text-slate-300">Không có cuộc trò chuyện nào</p>
            <p class="text-xs text-slate-400 mt-1">
              {{ activeTab === 'active' ? 'Hiện không có khách hàng nào đang yêu cầu hỗ trợ trực tuyến.' : 'Không tìm thấy lịch sử cuộc trò chuyện nào.' }}
            </p>
          </div>

          <button
            v-else
            v-for="session in filteredSessions"
            :key="session.id"
            @click="ChonSession(session)"
            class="w-full text-left p-3 rounded-xl transition-all flex items-start space-x-3"
            :class="[
              activeSession?.id === session.id 
                ? 'bg-primary/10 text-primary border border-primary/20 shadow-sm' 
                : 'hover:bg-slate-100/70 dark:hover:bg-slate-700/40 border border-transparent'
            ]"
          >
            <!-- Avatar -->
            <div class="h-10 w-10 rounded-full bg-slate-200 dark:bg-slate-700 flex items-center justify-center shrink-0">
              <User class="h-5 w-5 text-slate-500 dark:text-slate-300" />
            </div>

            <!-- Info -->
            <div class="min-w-0 flex-1">
              <div class="flex items-center justify-between">
                <span class="font-bold text-sm truncate text-slate-800 dark:text-slate-200">
                  {{ session.tenKhachHang }}
                </span>
                <span class="text-[10px] text-slate-400">
                  {{ FormatThoiGian(session.ngayCapNhat || session.ngayTao) }}
                </span>
              </div>
              <div class="flex items-center text-xs text-slate-500 dark:text-slate-400 mt-1">
                <Phone class="h-3 w-3 mr-1 shrink-0" />
                <span class="truncate">{{ session.soDienThoai || 'Chưa cung cấp SĐT' }}</span>
              </div>

              <!-- Status badge -->
              <div class="mt-2 flex items-center justify-between">
                <span 
                  v-if="session.trangThai === 2" 
                  class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-rose-50 text-rose-600 dark:bg-rose-950/30 dark:text-rose-400 border border-rose-100 dark:border-rose-900/30"
                >
                  <span class="h-1.5 w-1.5 rounded-full bg-rose-500 animate-pulse"></span>
                  Cần hỗ trợ
                </span>
                <span 
                  v-else-if="session.trangThai === 3" 
                  class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-600 dark:bg-emerald-950/30 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-900/30"
                >
                  <span class="h-1.5 w-1.5 rounded-full bg-emerald-500"></span>
                  Đang chat
                </span>
                <span 
                  v-else-if="session.trangThai === 4" 
                  class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400 border border-slate-200 dark:border-slate-700"
                >
                  <CheckCircle class="h-3 w-3 text-slate-500" />
                  Đã đóng
                </span>
              </div>
            </div>
          </button>
        </div>
      </div>

      <!-- Right side: Message area -->
      <div class="flex-1 flex flex-col min-w-0 bg-slate-50/20 dark:bg-slate-900/10">
        
        <!-- Case 1: Session selected -->
        <template v-if="activeSession">
          <!-- Session Header bar -->
          <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-700/50 bg-white dark:bg-slate-800 flex items-center justify-between shadow-sm">
            <div class="flex items-center space-x-3">
              <div class="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                <User class="h-5 w-5 text-primary" />
              </div>
              <div>
                <h3 class="font-bold text-slate-800 dark:text-slate-100">{{ activeSession.tenKhachHang }}</h3>
                <div class="flex items-center space-x-3 text-xs text-slate-400 mt-0.5">
                  <span v-if="activeSession.soDienThoai" class="flex items-center">
                    <Phone class="h-3 w-3 mr-1" /> {{ activeSession.soDienThoai }}
                  </span>
                  <span class="flex items-center">
                    <Clock class="h-3 w-3 mr-1" /> Bắt đầu lúc: {{ FormatThoiGian(activeSession.ngayTao) }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Close Session Button -->
            <button 
              v-if="activeSession.trangThai !== 4"
              @click="DongPhienChat"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold bg-rose-50 text-rose-600 dark:bg-rose-950/30 dark:text-rose-400 border border-rose-100 dark:border-rose-900/30 hover:bg-rose-100 dark:hover:bg-rose-900/50 transition-colors"
            >
              <XCircle class="h-4 w-4" />
              Đóng phiên chat
            </button>
          </div>

          <!-- Messages scroll Container -->
          <div 
            ref="chatContainer"
            class="flex-1 overflow-y-auto p-6 space-y-4 min-h-0 bg-slate-50/50 dark:bg-slate-900/20"
          >
            <div v-if="isLoadingMessages" class="flex items-center justify-center h-full">
              <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
            </div>

            <template v-else>
              <div 
                v-for="msg in messages" 
                :key="msg.id"
                class="flex flex-col"
                :class="[msg.nguoiGui === 'STAFF' ? 'items-end' : 'items-start']"
              >
                <!-- Avatar and Sender Name (optional details) -->
                <div class="flex items-center space-x-1.5 mb-1 text-[11px] text-slate-400 px-1">
                  <span v-if="msg.nguoiGui === 'STAFF'" class="font-semibold text-primary">
                    {{ msg.maNhanVien ? `Nhân viên (${msg.maNhanVien})` : 'Bạn (Nhân viên)' }}
                  </span>
                  <span v-else-if="msg.nguoiGui === 'AI'" class="font-semibold text-purple-600 flex items-center">
                    <HelpCircle class="h-3 w-3 mr-0.5" /> Trợ lý AI
                  </span>
                  <span v-else class="font-semibold text-slate-600 dark:text-slate-300">{{ activeSession.tenKhachHang }}</span>
                  <span>•</span>
                  <span>{{ FormatThoiGian(msg.ngayTao) }}</span>
                </div>

                <!-- Message bubble -->
                <div 
                  class="max-w-[70%] rounded-2xl px-4 py-2.5 text-sm shadow-sm"
                  :class="[
                    msg.nguoiGui === 'STAFF'
                      ? 'bg-primary text-white rounded-tr-none'
                      : msg.nguoiGui === 'AI'
                        ? 'bg-purple-50 text-purple-950 dark:bg-purple-950/20 dark:text-purple-300 border border-purple-100 dark:border-purple-900/30 rounded-tl-none font-medium'
                        : 'bg-white text-slate-800 dark:bg-slate-800 dark:text-slate-100 border border-slate-100 dark:border-slate-700/50 rounded-tl-none'
                  ]"
                >
                  <template v-if="msg.nguoiGui === 'AI'">
                    <div class="space-y-2">
                      <div v-for="(seg, idx) in parseMessage(msg.noiDung)" :key="idx">
                        <div 
                          v-if="seg.type === 'text'"
                          class="leading-relaxed prose prose-sm max-w-none prose-a:text-blue-600 prose-a:underline dark:prose-invert"
                          v-html="renderMarkdown(seg.content)"
                        ></div>

                        <!-- Product Card -->
                        <div
                          v-else-if="seg.type === 'product'"
                          class="my-2 flex gap-3 p-2.5 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl hover:border-purple-400 hover:shadow-md transition-all cursor-pointer group w-full relative overflow-hidden text-left"
                          @click="moChiTietSanPham(seg.productData.url || '/admin/san-pham')"
                        >
                          <div class="relative w-16 h-16 shrink-0 rounded-lg overflow-hidden bg-slate-50 dark:bg-slate-700 border border-slate-100 dark:border-slate-600">
                            <img
                              :src="seg.productData.image ? resolveHinhAnh(seg.productData.image) : anhSanPhamDuPhong"
                              :alt="seg.productData.name || 'Sản phẩm'"
                              @error="xuLyLoiAnhSanPham"
                              class="w-full h-full object-cover group-hover:scale-105 transition duration-300"
                            />
                          </div>

                          <div class="flex-1 min-w-0 flex flex-col justify-center">
                            <p class="font-bold text-xs text-slate-800 dark:text-slate-100 line-clamp-1 group-hover:text-purple-700 transition">
                              {{ seg.productData.name }}
                            </p>

                            <div class="flex items-center gap-2 mt-0.5">
                              <span class="text-xs font-extrabold text-rose-600 dark:text-rose-400">
                                {{ seg.productData.price ? dinhDangTien(seg.productData.price) : '' }}
                              </span>
                              <span
                                v-if="seg.productData.originalPrice && Number(seg.productData.originalPrice) > Number(seg.productData.price)"
                                class="text-[10px] text-slate-400 line-through"
                              >
                                {{ dinhDangTien(seg.productData.originalPrice) }}
                              </span>
                            </div>

                            <div class="text-[10px] text-slate-500 dark:text-slate-400 mt-0.5 flex flex-wrap items-center gap-1.5">
                              <span v-if="seg.productData.color" class="inline-flex items-center gap-1">
                                <span class="w-1.5 h-1.5 rounded-full bg-purple-400"></span>
                                {{ seg.productData.color }}
                              </span>
                              <span v-if="seg.productData.size" class="inline-flex items-center gap-1">
                                • {{ seg.productData.size.toLowerCase().includes("size") ? seg.productData.size : "Size " + seg.productData.size }}
                              </span>
                              <span v-if="seg.productData.stock !== undefined" class="font-bold text-amber-600 dark:text-amber-400">
                                • {{ Number(seg.productData.stock) === 0
                                  ? "Đã hết hàng"
                                  : `${seg.productData.stockLabel || "Còn lại"}: ${seg.productData.stock} đôi` }}
                              </span>
                            </div>
                          </div>
                          <span class="absolute right-2 bottom-1 text-[10px] font-semibold text-purple-600 opacity-0 group-hover:opacity-100 transition flex items-center gap-0.5">
                            Chi tiết <ExternalLink class="w-3 h-3" />
                          </span>
                        </div>

                        <!-- Offer Card -->
                        <div
                          v-else-if="seg.type === 'offer'"
                          class="my-2 p-3 bg-gradient-to-r from-amber-50 to-orange-50 border border-amber-200 rounded-xl text-left"
                        >
                          <div class="flex items-center gap-2 font-bold text-amber-800 text-xs">
                            <Tag class="w-3.5 h-3.5" />
                            {{ seg.offerData.name }}
                          </div>
                          <p class="text-xs text-slate-600 mt-1">Mã: <span class="font-mono font-bold text-rose-600 bg-white px-1.5 py-0.5 rounded border border-rose-200">{{ seg.offerData.code }}</span> - Giảm {{ seg.offerData.discount }}</p>
                        </div>
                      </div>
                    </div>
                  </template>
                  <p v-else class="whitespace-pre-wrap leading-relaxed">{{ msg.noiDung }}</p>
                </div>
              </div>
            </template>
          </div>

          <!-- Message Input area -->
          <div v-if="activeSession.trangThai !== 4" class="p-4 bg-white dark:bg-slate-800 border-t border-slate-100 dark:border-slate-700/50 shadow-sm">
            <form @submit.prevent="GuiPhanHoi" class="flex gap-2 items-end">
              <textarea
                v-model="replyText"
                placeholder="Nhập nội dung phản hồi khách hàng... (Nhấn Enter để gửi)"
                rows="2"
                @keydown.enter.prevent="GuiPhanHoi"
                class="flex-1 resize-none bg-slate-50 dark:bg-slate-700 border border-slate-200 dark:border-slate-600 rounded-xl p-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
              ></textarea>
              <button 
                type="submit" 
                :disabled="!replyText.trim()"
                class="rounded-xl bg-primary hover:bg-primary-hover disabled:bg-slate-200 disabled:text-slate-400 text-white p-3.5 transition-all shadow-sm h-12 w-12 flex items-center justify-center shrink-0"
              >
                <Send class="h-5 w-5" />
              </button>
            </form>
          </div>
          <div v-else class="p-4 bg-slate-50 dark:bg-slate-800/40 border-t border-slate-100 dark:border-slate-700/50 text-center text-xs text-slate-400 font-medium">
            Cuộc hội thoại này đã kết thúc. Bạn không thể gửi thêm tin nhắn.
          </div>
        </template>

        <!-- Case 2: No session selected -->
        <div v-else class="flex-1 flex flex-col items-center justify-center text-center p-8">
          <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-800/80 flex items-center justify-center mb-4">
            <MessageSquare class="h-8 w-8 text-slate-400 dark:text-slate-500" />
          </div>
          <h3 class="text-lg font-bold text-slate-700 dark:text-slate-200 mb-1">Hộp thư hỗ trợ trực tuyến</h3>
          <p class="text-sm text-slate-400 dark:text-slate-500 max-w-sm">Hãy chọn một khách hàng đang cần hỗ trợ ở thanh bên trái để bắt đầu cuộc hội thoại.</p>
        </div>

      </div>

    </div>
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

/* Markdown styles cho tin nhắn AI */
:deep(.prose) p {
  margin: 0 0 0.5rem 0;
}
:deep(.prose) p:last-child {
  margin-bottom: 0;
}
:deep(.prose) strong {
  font-weight: 700;
}
:deep(.prose) em {
  font-style: italic;
}
:deep(.prose) ul {
  list-style: disc;
  padding-left: 1.25rem;
  margin: 0.25rem 0;
}
:deep(.prose) ol {
  list-style: decimal;
  padding-left: 1.25rem;
  margin: 0.25rem 0;
}
:deep(.prose) li {
  margin: 0.1rem 0;
}
:deep(.prose) a {
  color: #2563eb;
  text-decoration: underline;
  word-break: break-all;
}
:deep(.prose) a:hover {
  color: #1d4ed8;
}
:deep(.prose) code {
  background: rgba(0,0,0,0.07);
  border-radius: 3px;
  padding: 0.1em 0.35em;
  font-size: 0.85em;
  font-family: monospace;
}
:deep(.prose) hr {
  border: none;
  border-top: 1px solid rgba(0,0,0,0.1);
  margin: 0.5rem 0;
}
</style>
