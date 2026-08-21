<script setup>
import { ref, onMounted, nextTick, watch, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { 
  guiTinNhanClient, 
  yeuCauNhanVien, 
  layTinNhanClient,
  dongPhienChatClientInactivity
} from "../../services/chatbot";
import { useRealtime } from "../../composables/useRealtime";
import { layChiTietSanPham } from "../../services/san-pham";
import { dinhDangTienViet } from "../../utils/dinhDangTien";
import { filterProfanity } from "../../utils/profanity-filter";
import { 
  MessageCircle, 
  X, 
  Send, 
  Bot, 
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
  const blockRegex = /```(product)([\s\S]*?)```/g;
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
    if (blockType === "product") {
      try {
        const productJson = JSON.parse(content);
        const productId = extractProductId(productJson.url);
        if (productId !== null) {
          // Không tin trực tiếp dữ liệu sản phẩm do AI sinh. Chuyển thành link để
          // FetchProductDetail xác minh ID và tải lại toàn bộ dữ liệu từ API.
          segments.push({
            type: "link",
            text: productJson.name || `Sản phẩm #${productId}`,
            url: `/khachhang/san-pham/${productId}`
          });
        }
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
  let previousWasProductLink = false;

  while ((match = regex.exec(text)) !== null) {
    const matchIndex = match.index;
    const isImage = match[0].startsWith("!");
    const isProductLink = !isImage && isProductUrl(match[4]);
    if (matchIndex > lastIndex) {
      let content = text.substring(lastIndex, matchIndex);
      if (isProductLink) {
        // Lịch sử cũ dùng "- [Sản phẩm](...)". Link đã thành card nên chỉ
        // loại dấu đầu dòng sát card, không đụng tới danh sách văn bản thường.
        content = content
          .replace(/(?:^|\r?\n)[ \t]*[-*•][ \t]*$/, "")
          .trimEnd();
      } else if (previousWasProductLink) {
        content = content.replace(/^[\r\n \t]+/, "");
      }
      if (content.trim()) {
        segments.push({ type: "text", content });
      }
    }

    if (isImage) {
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
    previousWasProductLink = isProductLink;
    lastIndex = regex.lastIndex;
  }

  if (lastIndex < text.length) {
    let content = text.substring(lastIndex);
    if (previousWasProductLink) {
      content = content.replace(/^[\r\n \t]+/, "");
    }
    if (content.trim()) {
      segments.push({ type: "text", content });
    }
  }

  return segments;
}

function isProductUrl(url) {
  return Boolean(url && (url.includes("/san-pham/") || url.includes("/product/")));
}

function messageHasProductCard(text) {
  return /\[[^\]]+]\((?:https?:\/\/[^)]+)?\/(?:khachhang\/)?(?:san-pham|product)\/\d+\)/i.test(text || "")
    || (text || "").includes("```product");
}

function formatOptions(values, visibleLimit) {
  if (!Array.isArray(values) || values.length === 0) return "Chưa cập nhật";
  const visible = values.slice(0, visibleLimit);
  const remaining = values.length - visible.length;
  return remaining > 0 ? `${visible.join(", ")} (+${remaining})` : visible.join(", ");
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
  "Xem các mẫu giày bán chạy và hot nhất",
  "Cửa hàng đang có đợt giảm giá nào không?",
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
      
      // Khôi phục sessionState = 3 nếu trong lịch sử có tin nhắn từ Nhân viên trực
      const hasStaffMsg = (history || []).some(m => m.nguoiGui === "STAFF");
      if (hasStaffMsg) {
        sessionState.value = 3;
      }
      
      DongBoWebsocket(sessionId.value);
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
      
      // Mở khóa input realtime ngay khi Nhân viên trực phản hồi
      if (msg.nguoiGui === "STAFF") {
        sessionState.value = 3;
      }

      // Tránh trùng tin nhắn bằng cách kiểm tra ID hoặc khớp tin nhắn tạm của CUSTOMER
      const existsIndex = messages.value.findIndex(m => 
        m.id === msg.id || 
        (m.nguoiGui === msg.nguoiGui && (m.noiDung === msg.noiDung || (m.nguoiGui === "CUSTOMER" && typeof m.id === "number" && m.id > 1000000000000)))
      );
      if (existsIndex > -1) {
        messages.value[existsIndex].id = msg.id;
        messages.value[existsIndex].noiDung = msg.noiDung;
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
    nextTick(() => {
      if (inputRef.value) {
        inputRef.value.style.height = "auto";
      }
    });
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

  // Hiển thị tin nhắn của khách hàng ngay lập tức trên UI (đã lọc từ thô tục)
  const filteredUserText = filterProfanity(text);
  const tempUserMsgId = Date.now();
  messages.value.push({
    id: tempUserMsgId,
    nguoiGui: "CUSTOMER",
    noiDung: filteredUserText,
    ngayTao: new Date().toISOString()
  });

  isSending.value = true;
  CuonXuongCuoi();

  try {
    const res = await guiTinNhanClient(text, sessionId.value, customerName, phoneNumber);
    
    // Hiển thị tin nhắn trả lời của AI ngay lập tức trên UI từ kết quả HTTP (nếu chưa được hiển thị qua WebSocket)
    if (res && res.response) {
      const alreadyPushed = messages.value.some(m => 
        m.nguoiGui === "AI" && m.noiDung === res.response
      );
      if (!alreadyPushed) {
        messages.value.push({
          id: Date.now() + 1,
          nguoiGui: "AI",
          noiDung: res.response,
          ngayTao: new Date().toISOString()
        });
      }
    }

    // Đồng bộ session ID mới từ server (lần đầu chat hoặc khi server tạo phiên mới)
    if (res && res.sessionId) {
      const isNewSession = res.sessionId !== sessionId.value;
      const isNotSubscribed = !sessionSubscription;
      if (isNewSession || isNotSubscribed) {
        sessionId.value = res.sessionId;
        localStorage.setItem("chatbot_session_id", res.sessionId);
        DongBoWebsocket(res.sessionId);
      }
    }

    sessionState.value = res.trangThai;
    CuonXuongCuoi();
  } catch (error) {
    console.error("Lỗi khi gửi tin nhắn chatbot:", error);
    messages.value.push({
      id: Date.now(),
      nguoiGui: "AI",
      noiDung: error?.message || "Không thể kết nối đến máy chủ AI. Bạn vui lòng thử lại sau.",
      ngayTao: new Date().toISOString()
    });
  } finally {
    isSending.value = false;
    CuonXuongCuoi();
  }
}

// Yêu cầu kết nối với nhân viên trực
async function YeuCauKetNoiNhanVien() {
  isSending.value = true;
  
  // Hiển thị tin nhắn yêu cầu của khách hàng trước
  const requestText = "Tôi muốn gặp nhân viên trực tiếp hỗ trợ";
  const userMsgExists = messages.value.some(m => m.nguoiGui === "CUSTOMER" && m.noiDung === requestText);
  if (!userMsgExists) {
    messages.value.push({
      id: Date.now(),
      nguoiGui: "CUSTOMER",
      noiDung: requestText,
      ngayTao: new Date().toISOString()
    });
    CuonXuongCuoi();
  }

  let alreadyRequested = false;

  if (!sessionId.value) {
    // Nếu chưa chat bao giờ mà click luôn nút hỗ trợ, cần tạo session trước
    try {
      const res = await guiTinNhanClient(requestText, null, "Khách hàng", "");
      sessionId.value = res.sessionId;
      localStorage.setItem("chatbot_session_id", res.sessionId);
      DongBoWebsocket(res.sessionId);
      
      // Hiển thị tin nhắn phản hồi của AI/Hệ thống nếu có
      if (res && res.response) {
        const alreadyPushed = messages.value.some(m => 
          m.nguoiGui === "AI" && m.noiDung === res.response
        );
        if (!alreadyPushed) {
          messages.value.push({
            id: Date.now() + 1,
            nguoiGui: "AI",
            noiDung: res.response,
            ngayTao: new Date().toISOString()
          });
          CuonXuongCuoi();
        }
      }

      if (res && res.trangThai === 2) {
        sessionState.value = 2;
        alreadyRequested = true;
      }
    } catch (e) {
      console.error(e);
      isSending.value = false;
      return;
    }
  }

  if (!alreadyRequested) {
    try {
      await yeuCauNhanVien(sessionId.value);
      sessionState.value = 2; // Đang chờ hỗ trợ
      
      // Hiển thị tin nhắn hệ thống phản hồi ngay lập tức
      const sysMsgContent = "Đã gửi yêu cầu kết nối với nhân viên tư vấn. Nhân viên trực sẽ phản hồi bạn trong giây lát!";
      const exists = messages.value.some(m => m.nguoiGui === "AI" && m.noiDung === sysMsgContent);
      if (!exists) {
        messages.value.push({
          id: Date.now() + 2,
          nguoiGui: "AI",
          noiDung: sysMsgContent,
          ngayTao: new Date().toISOString()
        });
        CuonXuongCuoi();
      }
    } catch (error) {
      console.error("Lỗi khi yêu cầu nhân viên hỗ trợ:", error);
    } finally {
      isSending.value = false;
      CuonXuongCuoi();
    }
  } else {
    isSending.value = false;
    CuonXuongCuoi();
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

const showCountdownPrompt = ref(false);
const countdownSeconds = ref(60);

let inactivityTimer = null;
let countdownInterval = null;

function ClearTimers() {
  if (inactivityTimer) {
    clearTimeout(inactivityTimer);
    inactivityTimer = null;
  }
  if (countdownInterval) {
    clearInterval(countdownInterval);
    countdownInterval = null;
  }
}

function ResetInactivityTimer() {
  ClearTimers();
  showCountdownPrompt.value = false;

  if (isOpen.value && sessionId.value && messages.value.length > 0 && sessionState.value !== 4) {
    inactivityTimer = setTimeout(() => {
      KichHoatDemNguoc();
    }, 60000);
  }
}

function KichHoatDemNguoc() {
  ClearTimers();
  showCountdownPrompt.value = true;
  countdownSeconds.value = 60;

  countdownInterval = setInterval(() => {
    countdownSeconds.value--;
    if (countdownSeconds.value <= 0) {
      ClearTimers();
      TuDongDongPhienChat();
    }
  }, 1000);
}

async function TuDongDongPhienChat() {
  if (sessionId.value) {
    try {
      await dongPhienChatClientInactivity(sessionId.value);
      sessionState.value = 4;
      showCountdownPrompt.value = false;
    } catch (e) {
      console.error("Lỗi khi tự động đóng phiên chat:", e);
    }
  }
}

function XacNhanConHoatDong() {
  showCountdownPrompt.value = false;
  ResetInactivityTimer();
}

const inputRef = ref(null);

function autoGrowInput() {
  const el = inputRef.value;
  if (!el) return;
  el.style.height = "auto";
  const newHeight = Math.min(el.scrollHeight, 120);
  el.style.height = `${newHeight}px`;
}

function handleEnterKey(event) {
  if (event.shiftKey) {
    return;
  }
  if (inputText.value.trim() && sessionState.value !== 2) {
    GuiTinNhan();
  }
}

const productDetailsMap = ref({});

function extractProductId(url) {
  if (!url) return null;
  const cleanUrl = url.split("?")[0].replace(/\/+$/, "");
  const parts = cleanUrl.split("/");
  const lastPart = parts[parts.length - 1];
  const id = parseInt(lastPart);
  return Number.isInteger(id) ? id : null;
}

async function FetchProductDetail(id, fallbackName = "") {
  if (!id) return;
  if (productDetailsMap.value[id] && !productDetailsMap.value[id].isLoading) return;

  if (!productDetailsMap.value[id]) {
    productDetailsMap.value[id] = {
      id,
      ten: fallbackName || `Sản phẩm #${id}`,
      hinhAnh: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80",
      giaBan: 0,
      giaGoc: 0,
      coGiam: false,
      phanTramGiam: 0,
      mauSac: [],
      kichCo: [],
      tonKho: 0,
      isLoading: true,
      notFound: false
    };
  }

  try {
    const rawSp = await layChiTietSanPham(id);
    if (!rawSp) {
      if (productDetailsMap.value[id]) {
        productDetailsMap.value[id].isLoading = false;
        productDetailsMap.value[id].notFound = true;
      }
      return;
    }

    const bienThe = rawSp.bienThe ?? [];
    const bienTheConHang = bienThe.filter((item) => Number(item.soLuong) > 0);
    let minVariant = null;
    for (const b of bienTheConHang) {
      const gia = Number(b.giaBan);
      if (!Number.isFinite(gia)) continue;
      if (!minVariant || gia < Number(minVariant.giaBan)) minVariant = b;
    }

    const mauSac = [...new Set(bienTheConHang.map((item) => item.mauSac).filter(Boolean))];
    const kichCo = [...new Set(bienTheConHang.map((item) => item.kichCo).filter(Boolean))]
      .sort((a, b) => String(a).localeCompare(String(b), "vi", { numeric: true }));
    const tonKho = bienTheConHang.reduce(
      (total, item) => total + Math.max(0, Number(item.soLuong) || 0),
      0
    );

    const hinhAnh = minVariant?.hinhAnh || rawSp.hinhAnh || "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80";
    const giaBan = minVariant ? Number(minVariant.giaBan) : 0;
    const giaGoc = minVariant ? Number(minVariant.giaGoc) : 0;
    const coGiam = minVariant && giaGoc > giaBan;
    const phanTramGiam = coGiam ? Math.round(((giaGoc - giaBan) / giaGoc) * 100) : 0;

    productDetailsMap.value[id] = {
      id: rawSp.id || id,
      ten: rawSp.ten || fallbackName || `Sản phẩm #${id}`,
      hinhAnh,
      giaBan,
      giaGoc,
      coGiam,
      phanTramGiam,
      mauSac,
      kichCo,
      tonKho,
      isLoading: false,
      notFound: false
    };
  } catch (e) {
    console.error("Lỗi khi tải chi tiết sản phẩm cho chatbot:", e);
    if (productDetailsMap.value[id]) {
      productDetailsMap.value[id].isLoading = false;
      productDetailsMap.value[id].notFound = true;
    }
  }
}

function getProductDetail(url) {
  const id = extractProductId(url);
  if (id === null) return null;
  if (!productDetailsMap.value[id]) {
    FetchProductDetail(id);
  }
  return productDetailsMap.value[id] || null;
}

watch(
  messages,
  async (newMsgs) => {
    ResetInactivityTimer();

    for (const msg of newMsgs || []) {
      if (msg.noiDung) {
        const segments = parseMessage(msg.noiDung);
        for (const seg of segments) {
          if (seg.type === "link" && (seg.url.includes("/san-pham/") || seg.url.includes("/product/"))) {
            const id = extractProductId(seg.url);
            if (id !== null) {
              await FetchProductDetail(id, seg.text);
            }
          }
        }
      }
    }
  },
  { deep: true, immediate: true }
);

watch(
  () => isOpen.value,
  (val) => {
    if (val) {
      ResetInactivityTimer();
      CuonXuongCuoi();
    } else {
      ClearTimers();
    }
  }
);

watch(
  () => sessionState.value,
  (val) => {
    if (val === 4) {
      ClearTimers();
      showCountdownPrompt.value = false;
    } else {
      ResetInactivityTimer();
    }
  }
);

onMounted(() => {
  KhoiTaoChatbox();
});

onUnmounted(() => {
  ClearTimers();
  if (sessionSubscription) {
    unsubscribeTopic(sessionSubscription);
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

function toggleChatWithDragCheck() {
  if (wasDragging) {
    wasDragging = false;
    return;
  }
  ToggleChat();
}
</script>

<template>
  <div 
    class="fixed bottom-6 right-6 z-[9999] flex flex-col items-end"
    :style="{ transform: `translate3d(0px, ${position.y}px, 0)`, transition: isDragging ? 'none' : 'transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1)' }"
  >
    <!-- Chat Widget Window -->
    <Transition name="chat-window">
      <div 
        v-show="isOpen"
        class="mb-4 w-96 h-[500px] bg-white dark:bg-slate-800 rounded-3xl shadow-2xl border border-slate-100 dark:border-slate-700/50 overflow-hidden flex flex-col origin-bottom-right"
      >
      <!-- Chat Header -->
      <div 
        class="px-5 py-4 bg-primary text-white flex items-center justify-between shrink-0 cursor-grab active:cursor-grabbing"
        @mousedown="onMouseDown"
      >
        <div class="flex items-center space-x-2.5">
          <div class="h-9 w-9 rounded-full bg-white/10 flex items-center justify-center">
            <Bot class="h-4.5 w-4.5 text-white" />
          </div>
          <div>
            <h4 class="font-bold text-sm leading-none flex items-center gap-1">
              Trợ lý ảo SportShoe
            </h4>
            <span class="text-[10px] text-white/80 mt-1 block">
              <span v-if="sessionState === 1" class="flex items-center gap-1">
                <span class="h-1.5 w-1.5 rounded-full bg-emerald-400"></span> Sẵn sàng hỗ trợ
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
            <Bot class="h-7 w-7" />
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
              <span v-else class="text-primary font-bold flex items-center">
                <Bot class="h-3 w-3 mr-0.5" /> Trợ lý AI
              </span>
            </div>

            <!-- Message bubble -->
            <div 
              class="rounded-2xl px-3.5 py-2 text-xs shadow-sm leading-relaxed"
              :class="[
                msg.nguoiGui !== 'CUSTOMER' && messageHasProductCard(msg.noiDung)
                  ? 'w-[94%] max-w-[94%]'
                  : 'max-w-[80%]',
                msg.nguoiGui === 'CUSTOMER'
                  ? 'bg-primary text-white rounded-tr-none'
                  : msg.nguoiGui === 'STAFF'
                    ? 'bg-sky-50 text-sky-950 dark:bg-sky-950/20 dark:text-sky-300 border border-sky-100 dark:border-sky-900/30 rounded-tl-none font-medium'
                    : 'bg-white text-slate-800 dark:bg-slate-800 dark:text-slate-100 border border-slate-100 dark:border-slate-700/50 rounded-tl-none'
              ]"
            >
              <div class="space-y-2 whitespace-pre-wrap">
                <template v-for="(seg, idx) in parseMessage(msg.noiDung)" :key="idx">
                  <div v-if="seg.type === 'text'" v-html="renderText(seg.content)"></div>
                  
                  <!-- Markdown Image -->
                  <div
                    v-else-if="seg.type === 'image'"
                    class="my-2.5 overflow-hidden rounded-2xl border border-slate-100 dark:border-slate-700/60 bg-white dark:bg-slate-800 shadow-sm"
                  >
                    <img 
                      :src="seg.url" 
                      :alt="seg.alt || 'Sản phẩm'" 
                      class="max-h-48 w-full object-cover rounded-2xl hover:scale-105 transition duration-300 cursor-pointer"
                      @click="handleNavigate('/khachhang/san-pham')"
                    />
                  </div>

                  <!-- Product Codeblock Card -->
                  <div
                    v-else-if="seg.type === 'product'"
                    class="mt-2 w-full max-w-full"
                  >
                    <div 
                      @click="handleNavigate(seg.productData.url || '/khachhang/san-pham')"
                      class="flex gap-3 bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700/60 rounded-2xl p-2.5 hover:border-primary/20 dark:hover:border-primary/20 hover:shadow-md cursor-pointer transition-all relative overflow-hidden group w-full"
                    >
                      <div class="h-16 w-16 rounded-xl overflow-hidden shrink-0 bg-slate-50 relative">
                        <img 
                          :src="seg.productData.image || 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80'" 
                          alt="Product"
                          class="h-full w-full object-cover group-hover:scale-105 transition duration-300"
                        />
                      </div>
                      <div class="flex-1 min-w-0 flex flex-col justify-center">
                        <h6 class="font-bold text-[11px] text-slate-800 dark:text-slate-200 truncate group-hover:text-primary transition-colors">
                          {{ seg.productData.name }}
                        </h6>
                        <div class="mt-1 flex items-baseline gap-1.5 flex-wrap">
                          <span class="text-xs font-extrabold text-primary">
                            {{ seg.productData.price ? dinhDangTienViet(seg.productData.price) : '' }}
                          </span>
                          <span 
                            v-if="seg.productData.originalPrice && seg.productData.originalPrice > seg.productData.price"
                            class="text-[10px] text-slate-400 dark:text-slate-500 line-through"
                          >
                            {{ dinhDangTienViet(seg.productData.originalPrice) }}
                          </span>
                        </div>
                        <div v-if="seg.productData.size || seg.productData.color" class="text-[10px] text-slate-500 mt-0.5">
                          <span v-if="seg.productData.size">Size: {{ seg.productData.size }}</span>
                          <span v-if="seg.productData.color" class="ml-2">Màu: {{ seg.productData.color }}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div 
                    v-else-if="seg.type === 'link' && (seg.url.includes('/san-pham/') || seg.url.includes('/product/'))"
                    class="w-full max-w-full"
                  >
                    <!-- Product Card -->
                    <div 
                      v-if="getProductDetail(seg.url) && !getProductDetail(seg.url).isLoading && !getProductDetail(seg.url).notFound"
                      @click="handleNavigate(seg.url)"
                      class="flex gap-3 bg-slate-50/80 dark:bg-slate-900/30 border border-slate-200/80 dark:border-slate-700/60 rounded-xl p-3 hover:border-primary/30 dark:hover:border-primary/30 hover:shadow-md cursor-pointer transition-all relative overflow-hidden group w-full"
                    >
                      <!-- Product Image & Badge -->
                      <div class="h-[76px] w-[76px] rounded-xl overflow-hidden shrink-0 bg-white relative border border-slate-100">
                        <img 
                          :src="getProductDetail(seg.url).hinhAnh || 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80'" 
                          :alt="getProductDetail(seg.url).ten" 
                          class="h-full w-full object-cover group-hover:scale-105 transition duration-300"
                          @error="(e) => e.target.src = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80'"
                        />
                        <span 
                          v-if="getProductDetail(seg.url).coGiam"
                          class="absolute top-1 left-1 bg-rose-500 text-white text-[9px] font-black px-1.5 py-0.5 rounded-md shadow-sm"
                        >
                          {{ getProductDetail(seg.url).phanTramGiam }}%
                        </span>
                      </div>
                      
                      <!-- Product Info -->
                      <div class="flex-1 min-w-0 flex flex-col justify-center">
                        <h6 class="font-bold text-[11px] leading-4 text-slate-800 dark:text-slate-200 line-clamp-2 group-hover:text-primary transition-colors">
                          {{ getProductDetail(seg.url).ten }}
                        </h6>
                        <div class="mt-1 flex items-baseline gap-1.5 flex-wrap">
                          <span v-if="getProductDetail(seg.url).giaBan > 0" class="text-xs font-extrabold text-primary">
                            {{ dinhDangTienViet(getProductDetail(seg.url).giaBan) }}
                          </span>
                          <span v-else class="text-[11px] font-bold text-primary">
                            Xem chi tiết
                          </span>
                          <span 
                            v-if="getProductDetail(seg.url).coGiam"
                            class="text-[10px] text-slate-400 dark:text-slate-500 line-through"
                          >
                            {{ dinhDangTienViet(getProductDetail(seg.url).giaGoc) }}
                          </span>
                        </div>
                        <div class="mt-1.5 space-y-0.5 text-[9px] leading-3.5 text-slate-500 dark:text-slate-400">
                          <div class="truncate" :title="getProductDetail(seg.url).mauSac.join(', ')">
                            <span class="font-semibold text-slate-600 dark:text-slate-300">Màu:</span>
                            {{ formatOptions(getProductDetail(seg.url).mauSac, 3) }}
                          </div>
                          <div class="truncate" :title="getProductDetail(seg.url).kichCo.join(', ')">
                            <span class="font-semibold text-slate-600 dark:text-slate-300">Size:</span>
                            {{ formatOptions(getProductDetail(seg.url).kichCo, 5) }}
                          </div>
                          <div>
                            <span class="font-semibold text-slate-600 dark:text-slate-300">Còn lại:</span>
                            <span class="font-bold text-emerald-600 dark:text-emerald-400">
                              {{ getProductDetail(seg.url).tonKho.toLocaleString('vi-VN') }} đôi
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                    <!-- Skeleton Loader (Only when actually loading) -->
                    <div 
                      v-else-if="getProductDetail(seg.url) && getProductDetail(seg.url).isLoading"
                      class="flex gap-3 bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700/60 rounded-2xl p-2.5 w-full animate-pulse"
                    >
                      <div class="h-16 w-16 rounded-xl bg-slate-100 dark:bg-slate-700 shrink-0"></div>
                      <div class="flex-1 flex flex-col justify-center space-y-2">
                        <div class="h-3 bg-slate-100 dark:bg-slate-700 rounded w-3/4"></div>
                        <div class="h-3.5 bg-slate-100 dark:bg-slate-700 rounded w-1/2"></div>
                      </div>
                    </div>
                  </div>

                  <!-- Original Button Style for Non-Product Links (e.g. Invoice, Coupon) -->
                  <button 
                    v-else-if="seg.type === 'link'" 
                    @click="handleNavigate(seg.url)"
                    class="inline-flex items-center justify-center px-4 py-2.5 mt-1.5 font-bold text-xs bg-primary hover:bg-primary-hover text-white rounded-xl shadow-md transition-all cursor-pointer gap-1.5 w-full text-center"
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
              <Bot class="h-3 w-3 text-primary animate-pulse" />
              <span class="text-primary font-bold">Đang tìm thông tin phù hợp...</span>
            </div>
            <div class="bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700/50 rounded-2xl rounded-tl-none px-3.5 py-3 shadow-sm flex items-center space-x-1">
              <div class="h-1.5 w-1.5 bg-slate-400 rounded-full animate-bounce"></div>
              <div class="h-1.5 w-1.5 bg-slate-400 rounded-full animate-bounce [animation-delay:0.2s]"></div>
              <div class="h-1.5 w-1.5 bg-slate-400 rounded-full animate-bounce [animation-delay:0.4s]"></div>
            </div>
          </div>

          <!-- Follow-up Quick Suggestions after messages -->
          <div v-if="sessionState === 1 && !isSending" class="pt-2 pb-1 space-y-1.5">
            <div class="text-[10px] text-slate-400 font-medium px-1 flex items-center gap-1">
              <span>💡 Gợi ý câu hỏi tiếp theo:</span>
            </div>
            <div class="space-y-1.5">
              <button
                v-for="prompt in promptSuggestions"
                :key="prompt"
                @click="GuiTinNhan(prompt)"
                class="w-full text-left bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700/60 hover:bg-primary/5 hover:text-primary dark:hover:bg-slate-700 hover:border-primary/20 p-2 rounded-xl text-[11px] text-slate-600 dark:text-slate-300 font-medium transition-all shadow-xs"
              >
                {{ prompt }}
              </button>
            </div>
          </div>
        </template>
      </div>

      <!-- Human Support Trigger Banner -->
      <div 
        v-if="messages.length > 0 && sessionState === 1"
        class="bg-red-50 dark:bg-red-950/20 border-t border-b border-red-100 dark:border-red-900/30 px-4 py-2.5 flex items-center justify-between shrink-0"
      >
        <span class="text-[10px] text-red-800 dark:text-red-400 font-medium">Bạn chưa tìm thấy thông tin phù hợp?</span>
        <button 
          @click="YeuCauKetNoiNhanVien"
          class="inline-flex items-center gap-1 bg-white hover:bg-red-50 dark:bg-slate-800 px-2.5 py-1.5 rounded-lg text-[10px] font-bold text-primary border border-red-200 dark:border-red-900/40 shadow-sm transition-colors"
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
          <textarea
            ref="inputRef"
            v-model="inputText"
            rows="1"
            :disabled="sessionState === 2"
            placeholder="Nhập câu hỏi của bạn tại đây..."
            @input="autoGrowInput"
            @keydown.enter.prevent="handleEnterKey"
            class="flex-1 bg-slate-50 dark:bg-slate-700 border border-slate-100 dark:border-slate-600 rounded-2xl px-4 py-2 text-xs text-slate-800 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all disabled:opacity-50 resize-none max-h-[120px] overflow-y-auto leading-normal align-middle"
          ></textarea>
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
    </Transition>

    <!-- Floating Chat Widget Button -->
    <div class="relative flex flex-col items-end">
      <button
        @mousedown="onMouseDown"
        @mouseup="isPressed = false"
        @mouseleave="isPressed = false"
        @click="toggleChatWithDragCheck"
        class="group relative flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-r from-red-600 to-rose-700 text-white shadow-xl focus:outline-none cursor-grab active:cursor-grabbing z-10 transition-transform duration-100 ease-out"
        :class="isPressed ? 'scale-90' : 'hover:scale-105 scale-100'"
      >
        <X v-if="isOpen" class="h-6 w-6" />
        <MessageCircle v-else class="h-6 w-6" />
      </button>
      
    </div>

    <!-- Inactivity Overlay Modal in the Middle of Screen -->
    <div 
      v-if="showCountdownPrompt"
      class="fixed inset-0 z-[99999] flex items-center justify-center bg-slate-900/60 backdrop-blur-sm"
    >
      <div 
        class="bg-white dark:bg-slate-800 rounded-3xl p-6 shadow-2xl border border-slate-100 dark:border-slate-700 max-w-sm w-full mx-4 flex flex-col items-center text-center space-y-4 animate-scale-up"
      >
        <div class="h-14 w-14 rounded-full bg-amber-500/10 flex items-center justify-center text-amber-500 animate-bounce">
          <AlertCircle class="h-7 w-7" />
        </div>
        <div>
          <h4 class="font-bold text-slate-900 dark:text-white text-base">Bạn còn ở đó không?</h4>
          <p class="text-xs text-slate-500 dark:text-slate-400 mt-1.5 px-2">
            Phiên chat của bạn với trợ lý ảo SportShoe sẽ tự động đóng sau
          </p>
        </div>
        <!-- Circular countdown display -->
        <div class="h-20 w-20 rounded-full border-4 border-amber-500 flex items-center justify-center">
          <span class="text-2xl font-black text-amber-600 dark:text-amber-400">{{ countdownSeconds }}</span>
        </div>
        <button 
          @click="XacNhanConHoatDong"
          class="w-full bg-amber-500 hover:bg-amber-600 text-white py-2.5 rounded-2xl text-xs font-bold transition shadow-lg shadow-amber-500/20 active:scale-95 cursor-pointer"
        >
          Tôi còn ở đây
        </button>
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


@keyframes scaleUp {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
.animate-scale-up {
  animation: scaleUp 0.25s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
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

.chat-btn-bounce {
  transition: transform 0.15s ease-in-out;
}
.chat-btn-bounce:hover {
  transform: scale(1.05);
}

/* Hide scrollbar for Chrome, Safari and Opera */
textarea::-webkit-scrollbar {
  display: none;
}
/* Hide scrollbar for IE, Edge and Firefox */
textarea {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
