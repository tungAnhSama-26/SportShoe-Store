import { API_BASE_URL } from "./api-client";

// Kênh SSE CÔNG KHAI báo thay đổi catalog (ngừng bán / đổi giá / đợt giảm giá).
// Không cần đăng nhập -> khách vãng lai cũng nhận được để giỏ hàng tự đồng bộ lại.
const REALTIME_PATH = "/realtime/san-pham";
const RETRY_DELAY_MS = 3000;

function parseEventBlock(block) {
  let eventName = "message";
  const dataLines = [];
  for (const rawLine of block.split(/\r?\n/)) {
    if (!rawLine || rawLine.startsWith(":")) continue;
    const idx = rawLine.indexOf(":");
    const field = idx >= 0 ? rawLine.slice(0, idx) : rawLine;
    const value = idx >= 0 ? rawLine.slice(idx + 1).replace(/^ /, "") : "";
    if (field === "event") eventName = value;
    if (field === "data") dataLines.push(value);
  }
  if (!dataLines.length) return null;
  const rawData = dataLines.join("\n");
  try {
    return { eventName, data: JSON.parse(rawData) };
  } catch {
    return { eventName, data: rawData };
  }
}

export function ketNoiSanPhamRealtime({ onSanPhamThayDoi, onConnectionChange } = {}) {
  let stopped = false;
  let reconnectTimer = null;
  let controller = null;
  let retryCount = 0;

  const scheduleReconnect = () => {
    if (stopped || reconnectTimer) return;
    const delay = Math.min(RETRY_DELAY_MS * Math.max(1, retryCount), 15000);
    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null;
      connect();
    }, delay);
  };

  const connect = async () => {
    if (stopped) return;
    controller = new AbortController();
    try {
      const response = await fetch(`${API_BASE_URL}${REALTIME_PATH}`, {
        method: "GET",
        headers: { Accept: "text/event-stream", "Cache-Control": "no-cache" },
        cache: "no-store",
        signal: controller.signal,
      });
      if (!response.ok || !response.body) {
        throw new Error(`Realtime HTTP ${response.status}`);
      }
      retryCount = 0;
      onConnectionChange?.("connected");

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = "";
      while (!stopped) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const blocks = buffer.split(/\r?\n\r?\n/);
        buffer = blocks.pop() ?? "";
        for (const block of blocks) {
          const event = parseEventBlock(block);
          if (event?.eventName === "san-pham-thay-doi") {
            onSanPhamThayDoi?.(event.data);
          }
        }
      }
    } catch (error) {
      if (error?.name !== "AbortError") {
        retryCount += 1;
        onConnectionChange?.("disconnected");
      }
    } finally {
      controller = null;
      if (!stopped) scheduleReconnect();
    }
  };

  connect();

  return () => {
    stopped = true;
    if (reconnectTimer) {
      window.clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }
    controller?.abort();
    controller = null;
  };
}
