<script setup>
import { ref, onMounted, nextTick, watch } from "vue";
import { useRouter } from "vue-router";
import { 
  chatWithAdminAi, 
  getAdminChatHistory, 
  closeAdminAiSession, 
  getAdminAiSessions, 
  getAdminAiSessionMessages 
} from "../../services/admin-chatbot";
import { showConfirm } from "../../utils/alert";
import { Bar, Line, Doughnut } from "vue-chartjs";
import { 
  Chart as ChartJS, 
  Title, 
  Tooltip, 
  Legend, 
  BarElement, 
  CategoryScale, 
  LinearScale, 
  PointElement, 
  LineElement, 
  ArcElement 
} from "chart.js";

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale, PointElement, LineElement, ArcElement);
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
  Zap,
  History,
  Power
} from "lucide-vue-next";

import { resolveHinhAnh } from "../../utils/resolve-image";

const router = useRouter();
const isOpen = ref(false);
const messages = ref([]);
const inputText = ref("");
const isSending = ref(false);
const chatContainer = ref(null);

const showHistoryModal = ref(false);
const sessionList = ref([]);
const loadingSessions = ref(false);
const activeSessionId = ref(null);

function formatDateTime(isoStr) {
  if (!isoStr) return "";
  try {
    const d = new Date(isoStr);
    return d.toLocaleString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    });
  } catch (e) {
    return isoStr;
  }
}

function dinhDangTien(val) {
  if (val === null || val === undefined) return "0 đ";
  const num = Number(val);
  if (isNaN(num)) return "0 đ";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(num);
}

const promptSuggestions = [
  { label: "Doanh thu hôm nay", text: "Thống kê doanh thu hôm nay", icon: TrendingUp },
  { label: "Sản phẩm sắp hết hàng", text: "Sản phẩm sắp hết hàng", icon: AlertTriangle },
  { label: "Sản phẩm bán chạy", text: "Sản phẩm bán chạy nhất", icon: Zap },
  { label: "Đánh giá tốt nhất", text: "Thống kê những sản phẩm được đánh giá tốt nhất và tệ nhất", icon: Sparkles }
];

function parseMessage(text) {
  if (!text) return [];
  const segments = [];
  const blockRegex = /```(chart|product)([\s\S]*?)```/g;
  let lastIndex = 0;
  let match;

  const hasProductBlock = text.includes("```product");

  while ((match = blockRegex.exec(text)) !== null) {
    const matchIndex = match.index;
    if (matchIndex > lastIndex) {
      const rawText = text.substring(lastIndex, matchIndex);
      if (!hasProductBlock) {
        segments.push(...parseLinksAndImages(rawText));
      } else {
        const cleanedText = rawText.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, "");
        segments.push(...parseLinksAndImages(cleanedText));
      }
    }
    const blockType = match[1].trim();
    const content = match[2].trim();
    if (blockType === "chart") {
      try {
        const chartJson = JSON.parse(content);
        segments.push({ type: "chart", chartData: chartJson });
      } catch (e) {
        console.error("Lỗi parse chart JSON:", e);
      }
    } else if (blockType === "product") {
      try {
        const productJson = JSON.parse(content);
        segments.push({ type: "product", productData: productJson });
      } catch (e) {
        console.error("Lỗi parse product JSON:", e);
      }
    }
    lastIndex = blockRegex.lastIndex;
  }

  if (lastIndex < text.length) {
    const rawText = text.substring(lastIndex);
    if (!hasProductBlock) {
      segments.push(...parseLinksAndImages(rawText));
    } else {
      const cleanedText = rawText.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, "");
      segments.push(...parseLinksAndImages(cleanedText));
    }
  }

  return segments;
}

function parseLinksAndImages(text) {
  const segments = [];
  const regex = /!\[([^\]]*)\]\(([^)]+)\)|\[([^\]]+)\]\(([^)]+)\)/g;
  let lastIndex = 0;
  let match;

  while ((match = regex.exec(text)) !== null) {
    const matchIndex = match.index;
    if (matchIndex > lastIndex) {
      segments.push({
        type: "text",
        content: text.substring(lastIndex, matchIndex)
      });
    }

    if (match[0].startsWith("!")) {
      segments.push({
        type: "image",
        alt: match[1],
        url: match[2]
      });
    } else {
      const linkText = match[3].replace(/\*\*/g, "");
      segments.push({
        type: "link",
        text: linkText,
        url: match[4]
      });
    }
    lastIndex = regex.lastIndex;
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
  let clean = text
    .replace(/số lượng tồn kho/gi, "Số lượng")
    .replace(/số lượng tồn/gi, "Số lượng")
    .replace(/tồn kho/gi, "Số lượng");
  let escaped = clean
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
  return escaped.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
}

async function handleChatbotAction(url) {
  // Pattern 1: /action/confirm-order/HDxxx
  if (url.startsWith("/action/confirm-order/")) {
    const code = url.substring("/action/confirm-order/".length);
    const confirmed = await showConfirm(`Bạn có chắc chắn muốn **Xác nhận** đơn hàng **${code}** không?`, 'Xác nhận đơn hàng', 'Đồng ý', 'Hủy');
    if (confirmed) {
      guiTinNhan(`/execute-confirm-order ${code}`);
    }
  }
  // Pattern 2: /action/cancel-order/HDxxx
  else if (url.startsWith("/action/cancel-order/")) {
    const code = url.substring("/action/cancel-order/".length);
    const confirmed = await showConfirm(`Bạn có chắc chắn muốn **Hủy** đơn hàng **${code}** không?`, 'Hủy đơn hàng', 'Đồng ý hủy', 'Không');
    if (confirmed) {
      guiTinNhan(`/execute-cancel-order ${code}`);
    }
  }
  // Pattern 3: /action/update-stock/PRODUCT_NAME/SIZE/COLOR/NEW_STOCK
  else if (url.startsWith("/action/update-stock/")) {
    const parts = url.substring("/action/update-stock/".length).split("/");
    if (parts.length >= 4) {
      const productName = decodeURIComponent(parts[0]);
      const size = parts[1];
      const color = decodeURIComponent(parts[2]);
      const qty = parts[3];
      const confirmed = await showConfirm(
        `Bạn có chắc chắn muốn cập nhật tồn kho sản phẩm **${productName}** (Size **${size}**, Màu **${color}**) thành **${qty}** không?`, 
        'Cập nhật tồn kho', 
        'Đồng ý', 
        'Hủy'
      );
      if (confirmed) {
        guiTinNhan(`/execute-update-stock ${productName}|${size}|${color}|${qty}`);
      }
    }
  }
  // Pattern 4: /action/create-voucher/CODE/NAME/TYPE/VALUE/MIN_ORDER/MAX_DISCOUNT/QTY/DURATION
  else if (url.startsWith("/action/create-voucher/")) {
    const parts = url.substring("/action/create-voucher/".length).split("/");
    if (parts.length >= 8) {
      const code = parts[0];
      const name = decodeURIComponent(parts[1]);
      const type = parts[2];
      const value = parts[3];
      const minOrder = parts[4];
      const maxDiscount = parts[5];
      const qty = parts[6];
      const duration = parts[7];
      
      const typeText = type === "1" ? `${value}%` : `${Number(value).toLocaleString()}đ`;
      const confirmed = await showConfirm(
        `Bạn có muốn tạo mã giảm giá **${code}** (${name}) giảm **${typeText}**, áp dụng cho đơn từ **${Number(minOrder).toLocaleString()}đ** không?`,
        'Tạo mã giảm giá',
        'Đồng ý tạo',
        'Hủy'
      );
      if (confirmed) {
        guiTinNhan(`/execute-create-voucher ${code}|${name}|${type}|${value}|${minOrder}|${maxDiscount}|${qty}|${duration}`);
      }
    }
  }
}

function handleNavigate(url) {
  if (url.startsWith("/action/")) {
    handleChatbotAction(url);
  } else if (url.startsWith("/api/v1/admin/chatbot/download-csv")) {
    const host = window.location.origin;
    const token = localStorage.getItem("adminToken") || "";
    const downloadUrl = `${host}${url}`;

    fetch(downloadUrl, {
      headers: {
        "Authorization": `Bearer ${token}`
      }
    })
    .then(response => {
      if (!response.ok) throw new Error("Unauthorized");
      return response.blob();
    })
    .then(blob => {
      const blobUrl = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = blobUrl;
      a.download = "bao-cao-admin.csv";
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(blobUrl);
    })
    .catch(err => {
      console.error("Lỗi tải báo cáo:", err);
      showConfirm("Không thể tải xuống file báo cáo. Phiên làm việc có thể đã hết hạn hoặc không có quyền.", "Lỗi tải báo cáo", "Đóng", "");
    });
  } else if (url.startsWith("/")) {
    router.push(url);
  } else {
    window.open(url, "_blank");
  }
}

function getChartDataset(chartData) {
  const isDoughnut = chartData.chartType === 'doughnut' || chartData.chartType === 'pie';
  const colors = [
    '#F43F5E', // Rose
    '#3B82F6', // Blue
    '#10B981', // Emerald
    '#F59E0B', // Amber
    '#8B5CF6', // Violet
    '#EC4899', // Pink
    '#14B8A6'  // Teal
  ];

  return {
    labels: chartData.labels,
    datasets: [
      {
        label: chartData.title || "Số liệu",
        data: chartData.data,
        backgroundColor: isDoughnut ? colors.slice(0, chartData.labels.length) : 'rgba(244, 63, 94, 0.2)',
        borderColor: isDoughnut ? colors.slice(0, chartData.labels.length) : '#F43F5E',
        borderWidth: 1.5,
        tension: 0.3,
        fill: chartData.chartType === 'line' ? false : true
      }
    ]
  };
}

function getChartOptions(chartType) {
  const isDoughnut = chartType === 'doughnut' || chartType === 'pie';
  return {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
        labels: {
          boxWidth: 12,
          font: { size: 10 }
        }
      }
    },
    ...(isDoughnut ? {} : {
      scales: {
        x: {
          grid: { display: false },
          ticks: { font: { size: 9 } }
        },
        y: {
          beginAtZero: true,
          ticks: { font: { size: 9 } }
        }
      }
    })
  };
}

function cuonXuongCuoi() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }
  });
}

async function layLichSuChat() {
  try {
    const data = await getAdminChatHistory();
    if (data && data.length > 0) {
      messages.value = data.map(m => ({
        id: m.id.toString(),
        sender: m.nguoiGui === "STAFF" ? "USER" : "AI",
        content: m.noiDung,
        time: m.ngayTao
      }));
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
    console.error("Lỗi đọc lịch sử chat admin từ database:", e);
    // Fallback sang localStorage nếu API lỗi
    const saved = localStorage.getItem("admin_chatbot_history");
    if (saved) {
      messages.value = JSON.parse(saved);
    }
  }
}

function xoaLichSu() {
  messages.value = [
    {
      id: "welcome",
      sender: "AI",
      content: "Đã xóa lịch sử chat trên màn hình. Tôi có thể giúp gì thêm cho bạn?",
      time: new Date().toISOString()
    }
  ];
}

async function xemLichSuTroChuyen() {
  showHistoryModal.value = true;
  loadingSessions.value = true;
  try {
    const data = await getAdminAiSessions();
    sessionList.value = data || [];
  } catch (e) {
    console.error("Lỗi lấy danh sách phiên chat:", e);
  } finally {
    loadingSessions.value = false;
  }
}

async function loadSessionMessages(sessionId) {
  try {
    const data = await getAdminAiSessionMessages(sessionId);
    if (data && data.length > 0) {
      activeSessionId.value = sessionId;
      messages.value = data.map(m => ({
        id: m.id.toString(),
        sender: m.nguoiGui === "STAFF" ? "USER" : "AI",
        content: m.noiDung,
        time: m.ngayTao
      }));
      showHistoryModal.value = false;
      cuonXuongCuoi();
    }
  } catch (e) {
    console.error("Lỗi tải tin nhắn của phiên:", e);
  }
}

async function dongPhienTroChuyen() {
  try {
    await closeAdminAiSession();
  } catch (e) {
    console.error("Lỗi đóng phiên backend:", e);
  }
  activeSessionId.value = null;
  messages.value = [
    {
      id: "welcome-" + Date.now(),
      sender: "AI",
      content: "Xin chào Quản trị viên! Tôi là trợ lý ảo hỗ trợ quản lý SportShoe. Tôi có thể giúp bạn thống kê doanh thu, kiểm tra sản phẩm hết hàng hoặc tìm kiếm hóa đơn nhanh chóng.",
      time: new Date().toISOString()
    }
  ];
  if (showHistoryModal.value) {
    showHistoryModal.value = false;
  }
  cuonXuongCuoi();
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

// --- Dragging Logic ---
const position = ref({ x: 0, y: 0 });
const isDragging = ref(false);
const isPressed = ref(false);
let wasDragging = false;
let startMousePos = { x: 0, y: 0 };
let startPos = { x: 0, y: 0 };

function onMouseDown(e) {
  if (e.button !== 0) return;
  isDragging.value = false;
  wasDragging = false;
  isPressed.value = true;
  startMousePos = { x: e.clientX, y: e.clientY };
  startPos = { ...position.value };
  
  document.addEventListener('mousemove', onMouseMove);
  document.addEventListener('mouseup', onMouseUp);
}

function onMouseMove(e) {
  const dx = e.clientX - startMousePos.x;
  const dy = e.clientY - startMousePos.y;
  
  if (!isDragging.value && (Math.abs(dx) > 3 || Math.abs(dy) > 3)) {
    isDragging.value = true;
    wasDragging = true;
  }
  
  if (isDragging.value) {
    // Only allow vertical dragging along the right edge
    position.value.x = 0;
    position.value.y = startPos.y + dy;
  }
}

function onMouseUp() {
  document.removeEventListener('mousemove', onMouseMove);
  document.removeEventListener('mouseup', onMouseUp);
  isDragging.value = false;
  isPressed.value = false;

  if (wasDragging) {
    // Ensure it stays on the right edge
    position.value.x = 0;

    const bottomOffset = 24; 
    const buttonHeight = 56;
    const absoluteY = window.innerHeight - bottomOffset - buttonHeight + position.value.y;
    
    if (absoluteY < 24) {
      position.value.y -= (absoluteY - 24);
    } else if (absoluteY > window.innerHeight - 24 - buttonHeight) {
      position.value.y -= (absoluteY - (window.innerHeight - 24 - buttonHeight));
    }
  }
}

function toggleChat() {
  if (wasDragging) {
    wasDragging = false;
    return;
  }
  isOpen.value = !isOpen.value;
}
</script>

<template>
  <div 
    class="fixed bottom-6 right-6 z-[9999] font-sans flex flex-col items-end" 
    :style="{ transform: `translate3d(0px, ${position.y}px, 0)`, transition: isDragging ? 'none' : 'transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1)' }"
  >
    <!-- Chat Window -->
    <Transition name="chat-window">
      <div
        v-show="isOpen"
        class="flex h-[560px] w-[380px] flex-col overflow-hidden rounded-[24px] border border-slate-100 bg-white shadow-2xl shadow-slate-200 dark:border-slate-800 dark:bg-slate-900 origin-bottom-right mb-4"
      >
        <!-- Header -->
        <div 
          class="flex items-center justify-between bg-gradient-to-r from-red-600 to-rose-700 px-5 py-4 text-white cursor-grab active:cursor-grabbing relative z-10"
          @mousedown="onMouseDown"
        >
          <div class="flex items-center gap-3">
            <div>
              <h3 class="text-sm font-bold tracking-wide">Trợ Lý Admin AI</h3>
              <p class="text-[10px] text-rose-200">Trực tuyến</p>
            </div>
          </div>
          <div class="flex items-center gap-1.5">
            <!-- Icon Lịch sử trò chuyện -->
            <button
              @click="xemLichSuTroChuyen"
              title="Lịch sử cuộc hội thoại AI"
              class="rounded-lg p-1.5 transition hover:bg-white/20 active:scale-95 cursor-pointer"
            >
              <History class="h-4.5 w-4.5 text-white" />
            </button>

            <!-- Icon Đóng phiên trò chuyện -->
            <button
              @click="dongPhienTroChuyen"
              title="Đóng phiên làm việc hiện tại"
              class="rounded-lg p-1.5 transition hover:bg-white/20 active:scale-95 text-rose-100 hover:text-white cursor-pointer"
            >
              <Power class="h-4.5 w-4.5" />
            </button>

            <!-- Icon Đóng cửa sổ -->
            <button
              @click="isOpen = false"
              title="Thu nhỏ cửa sổ"
              class="rounded-lg p-1.5 transition hover:bg-white/20 active:scale-95 cursor-pointer"
            >
              <X class="h-4.5 w-4.5 text-white" />
            </button>
          </div>
        </div>

        <!-- History Modal Overlay -->
        <div
          v-if="showHistoryModal"
          class="absolute inset-0 z-50 flex flex-col bg-white dark:bg-slate-900 rounded-[24px] p-4 shadow-2xl transition-all duration-200"
        >
          <div class="flex items-center justify-between border-b border-slate-100 dark:border-slate-800 pb-3 mb-3">
            <div class="flex items-center gap-2">
              <div class="p-1.5 rounded-xl bg-rose-50 text-rose-600 dark:bg-rose-900/20 dark:text-rose-400">
                <History class="w-4.5 h-4.5" />
              </div>
              <div>
                <h4 class="font-bold text-sm text-slate-800 dark:text-slate-100">Lịch sử cuộc hội thoại AI</h4>
                <p class="text-[10px] text-slate-400">Danh sách các phiên chat trước đây</p>
              </div>
            </div>
            <button
              @click="showHistoryModal = false"
              class="rounded-lg p-1.5 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition cursor-pointer"
            >
              <X class="w-4 h-4" />
            </button>
          </div>

          <!-- Loading state -->
          <div v-if="loadingSessions" class="flex-1 flex flex-col items-center justify-center text-xs text-slate-400 gap-2">
            <span class="w-5 h-5 border-2 border-rose-500 border-t-transparent rounded-full animate-spin"></span>
            Đang tải lịch sử phiên...
          </div>

          <!-- Session List -->
          <div v-else-if="sessionList && sessionList.length > 0" class="flex-1 overflow-y-auto space-y-2 pr-1 custom-scrollbar">
            <div
              v-for="ses in sessionList"
              :key="ses.id"
              @click="loadSessionMessages(ses.id)"
              class="p-3 rounded-2xl border border-slate-100 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-800/60 hover:border-rose-300 dark:hover:border-rose-900/50 hover:bg-rose-50/30 transition cursor-pointer group"
            >
              <div class="flex items-center justify-between mb-1.5">
                <span class="text-[11px] font-semibold text-slate-500 dark:text-slate-400 flex items-center gap-1">
                  📅 {{ formatDateTime(ses.ngayTao) }}
                </span>
                <span
                  class="text-[10px] font-bold px-2 py-0.5 rounded-full"
                  :class="ses.trangThai === 1 ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-slate-200 text-slate-600 dark:bg-slate-700 dark:text-slate-400'"
                >
                  {{ ses.trangThai === 1 ? 'Đang mở' : 'Đã đóng' }}
                </span>
              </div>
              <p class="text-xs font-bold text-slate-800 dark:text-slate-200 line-clamp-1 group-hover:text-rose-600 transition">
                Phiên chat #{{ ses.id }}
              </p>
              <p class="text-[11px] text-slate-400 mt-0.5">Bấm để tải lại toàn bộ nội dung tin nhắn phiên này</p>
            </div>
          </div>

          <!-- Empty state -->
          <div v-else class="flex-1 flex flex-col items-center justify-center text-center p-4 text-slate-400">
            <MessageSquare class="w-8 h-8 mb-2 text-slate-300" />
            <p class="text-xs font-medium">Chưa có lịch sử phiên trò chuyện nào.</p>
          </div>

          <div class="mt-3 pt-3 border-t border-slate-100 dark:border-slate-800 flex justify-between items-center">
            <button
              @click="dongPhienTroChuyen"
              type="button"
              class="w-full text-xs font-bold text-white bg-rose-600 hover:bg-rose-700 py-2.5 rounded-xl transition shadow-sm flex items-center justify-center gap-1.5 cursor-pointer"
            >
              <Power class="w-3.5 h-3.5" /> Bắt đầu cuộc hội thoại mới
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
                <div
                  v-else-if="seg.type === 'image'"
                  class="my-2 inline-block cursor-pointer group relative overflow-hidden rounded-2xl border border-slate-200 shadow-sm hover:shadow-md hover:border-rose-400 transition"
                  @click="handleNavigate('/admin/san-pham')"
                  title="Bấm để đến trang sản phẩm"
                >
                  <img
                    :src="resolveHinhAnh(seg.url)"
                    :alt="seg.alt || 'Sản phẩm'"
                    class="h-36 w-36 object-cover group-hover:scale-105 transition duration-300"
                  />
                  <div class="absolute inset-0 bg-slate-900/0 group-hover:bg-slate-900/25 transition flex items-center justify-center">
                    <span class="text-[11px] font-bold text-white bg-rose-500/90 px-2 py-1 rounded-lg opacity-0 group-hover:opacity-100 transition shadow">
                      Đến sản phẩm ➔
                    </span>
                  </div>
                </div>
                <div
                  v-else-if="seg.type === 'product'"
                  class="my-2.5 flex items-center gap-3 p-2.5 bg-white dark:bg-slate-800 border border-slate-200/80 dark:border-slate-700 rounded-2xl hover:border-rose-400 hover:shadow-md transition cursor-pointer group"
                  @click="handleNavigate(seg.productData.url || '/admin/san-pham')"
                >
                  <div class="relative w-14 h-14 shrink-0 rounded-xl overflow-hidden bg-slate-100 dark:bg-slate-700 border border-slate-200/60 dark:border-slate-600">
                    <img
                      v-if="seg.productData.image"
                      :src="resolveHinhAnh(seg.productData.image)"
                      class="w-full h-full object-cover group-hover:scale-105 transition duration-300"
                    />
                    <div v-else class="w-full h-full flex items-center justify-center text-base">
                      👟
                    </div>
                  </div>

                  <div class="flex-1 min-w-0">
                    <p class="font-bold text-xs text-slate-800 dark:text-slate-100 truncate group-hover:text-rose-600 transition">
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

                    <div class="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5 flex flex-wrap items-center gap-1.5">
                      <span v-if="seg.productData.color" class="inline-flex items-center gap-1">
                        <span class="w-1.5 h-1.5 rounded-full bg-rose-400"></span>
                        {{ seg.productData.color }}
                      </span>
                      <span v-if="seg.productData.size" class="inline-flex items-center gap-1">
                        • Size {{ seg.productData.size }}
                      </span>
                      <span v-if="seg.productData.stock !== undefined" class="font-bold text-amber-600 dark:text-amber-400">
                        • Số lượng: {{ seg.productData.stock }}
                      </span>
                    </div>
                  </div>

                  <button
                    type="button"
                    class="shrink-0 text-[11px] font-bold text-white bg-rose-500 hover:bg-rose-600 px-2.5 py-1.5 rounded-xl shadow-sm transition flex items-center gap-1"
                  >
                    Xem <span class="text-[9px]">➔</span>
                  </button>
                </div>
                <div v-else-if="seg.type === 'chart'" class="my-3 p-3 bg-slate-50 border border-slate-100 rounded-xl dark:bg-slate-900/80 dark:border-slate-800">
                  <div class="text-xs font-bold text-slate-700 mb-2 dark:text-slate-200">
                    {{ seg.chartData.title }}
                  </div>
                  <div class="h-[180px] relative w-full flex justify-center items-center">
                    <Bar
                      v-if="seg.chartData.chartType === 'bar'"
                      :data="getChartDataset(seg.chartData)"
                      :options="getChartOptions(seg.chartData.chartType)"
                    />
                    <Line
                      v-else-if="seg.chartData.chartType === 'line'"
                      :data="getChartDataset(seg.chartData)"
                      :options="getChartOptions(seg.chartData.chartType)"
                    />
                    <Doughnut
                      v-else-if="seg.chartData.chartType === 'doughnut' || seg.chartData.chartType === 'pie'"
                      :data="getChartDataset(seg.chartData)"
                      :options="getChartOptions(seg.chartData.chartType)"
                    />
                  </div>
                </div>
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
    </Transition>

    <!-- Chat Button -->
    <div class="relative flex flex-col items-end">
      <button
        @mousedown="onMouseDown"
        @mouseup="isPressed = false"
        @mouseleave="isPressed = false"
        @click="toggleChat"
        class="group relative flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-r from-red-600 to-rose-700 text-white shadow-xl cursor-grab active:cursor-grabbing z-10 transition-transform duration-100 ease-out"
        :class="isPressed ? 'scale-90' : 'hover:scale-105 scale-100'"
      >
        <transition name="fade" mode="out-in">
          <X v-if="isOpen" class="h-6 w-6" />
          <Bot v-else class="h-6 w-6" />
        </transition>
      </button>

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
  background: #cbd5e1;
}

/* Messenger-like Chat Window Animation */
.chat-window-enter-active,
.chat-window-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.chat-window-enter-from,
.chat-window-leave-to {
  opacity: 0;
  transform: scale(0.8) translateY(20px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scale(0.8) rotate(-45deg);
}
</style>
