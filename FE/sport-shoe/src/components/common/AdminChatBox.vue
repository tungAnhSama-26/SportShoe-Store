<script setup>
import { ref, onMounted, nextTick, watch } from "vue";
import { useRouter } from "vue-router";
import { chatWithAdminAi, getAdminChatHistory } from "../../services/admin-chatbot";
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
  { label: "Sản phẩm sắp hết hàng", text: "Sản phẩm sắp hết hàng", icon: AlertTriangle },
  { label: "Sản phẩm bán chạy", text: "Sản phẩm bán chạy nhất", icon: Zap },
  { label: "Đánh giá tốt nhất", text: "Thống kê những sản phẩm được đánh giá tốt nhất và tệ nhất", icon: Sparkles }
];

function parseMessage(text) {
  if (!text) return [];
  const segments = [];
  const chartRegex = /```chart([\s\S]*?)```/g;
  let lastIndex = 0;
  let match;

  while ((match = chartRegex.exec(text)) !== null) {
    const matchIndex = match.index;
    if (matchIndex > lastIndex) {
      segments.push(...parseLinks(text.substring(lastIndex, matchIndex)));
    }
    try {
      const chartJson = JSON.parse(match[1].trim());
      segments.push({
        type: "chart",
        chartData: chartJson
      });
    } catch (e) {
      console.error("Lỗi parse JSON chart:", e);
      segments.push({
        type: "text",
        content: "[Lỗi hiển thị biểu đồ: Dữ liệu JSON không hợp lệ]"
      });
    }
    lastIndex = chartRegex.lastIndex;
  }

  if (lastIndex < text.length) {
    segments.push(...parseLinks(text.substring(lastIndex)));
  }

  return segments;
}

function parseLinks(text) {
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
