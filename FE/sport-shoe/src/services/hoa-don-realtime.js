import { API_BASE_URL, getAuthHeaders } from "./api-client";

const REALTIME_PATH = "/realtime/hoa-don";
const RETRY_DELAY_MS = 3000;
const LOCAL_CHANNEL_NAME = "sportshoe-hoa-don-realtime";
const LOCAL_STORAGE_KEY = "sportshoe:hoa-don-realtime";

function parseEventBlock(block) {
  let eventName = "message";
  const dataLines = [];

  for (const rawLine of block.split(/\r?\n/)) {
    if (!rawLine || rawLine.startsWith(":")) continue;

    const separatorIndex = rawLine.indexOf(":");
    const field = separatorIndex >= 0 ? rawLine.slice(0, separatorIndex) : rawLine;
    const value = separatorIndex >= 0
      ? rawLine.slice(separatorIndex + 1).replace(/^ /, "")
      : "";

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

export function ketNoiHoaDonRealtime({
  authScope,
  onHoaDonThayDoi,
  onConnectionChange,
}) {
  let stopped = false;
  let reconnectTimer = null;
  let controller = null;
  let retryCount = 0;
  let reconnectAllowed = true;

  const scheduleReconnect = () => {
    if (stopped || !reconnectAllowed || reconnectTimer) return;
    const delay = Math.min(RETRY_DELAY_MS * Math.max(1, retryCount), 15000);
    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null;
      connect();
    }, delay);
  };

  const connect = async () => {
    if (stopped) return;

    const authHeaders = getAuthHeaders(authScope, REALTIME_PATH);
    if (!authHeaders.Authorization) {
      onConnectionChange?.("unauthenticated");
      return;
    }

    controller = new AbortController();
    try {
      const response = await fetch(`${API_BASE_URL}${REALTIME_PATH}`, {
        method: "GET",
        headers: {
          Accept: "text/event-stream",
          "Cache-Control": "no-cache",
          ...authHeaders,
        },
        cache: "no-store",
        signal: controller.signal,
      });

      if (response.status === 401 || response.status === 403) {
        reconnectAllowed = false;
        onConnectionChange?.("unauthenticated");
        return;
      }
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
          if (event?.eventName === "hoa-don-thay-doi") {
            onHoaDonThayDoi?.(event.data);
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
      if (!stopped && reconnectAllowed) scheduleReconnect();
    }
  };

  connect();

  return () => {
    stopped = true;
    reconnectAllowed = false;
    if (reconnectTimer) {
      window.clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }
    controller?.abort();
    controller = null;
  };
}

export function phatTinHoaDonThayDoiNoiBo(event = {}) {
  if (typeof window === "undefined") return;

  const payload = {
    ...event,
    thoiGianPhat: Date.now(),
  };

  if ("BroadcastChannel" in window) {
    const channel = new BroadcastChannel(LOCAL_CHANNEL_NAME);
    channel.postMessage(payload);
    channel.close();
  }

  try {
    localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(payload));
  } catch {
    // Bỏ qua nếu trình duyệt chặn localStorage.
  }
}

export function langNgheHoaDonThayDoiNoiBo(onHoaDonThayDoi) {
  if (typeof window === "undefined") return () => {};

  let channel = null;
  const handleEvent = (event) => {
    if (!event) return;
    onHoaDonThayDoi?.(event);
  };

  if ("BroadcastChannel" in window) {
    channel = new BroadcastChannel(LOCAL_CHANNEL_NAME);
    channel.onmessage = (message) => handleEvent(message.data);
  }

  const handleStorage = (storageEvent) => {
    if (storageEvent.key !== LOCAL_STORAGE_KEY || !storageEvent.newValue) return;
    try {
      handleEvent(JSON.parse(storageEvent.newValue));
    } catch {
      // Dữ liệu cũ/hỏng thì bỏ qua.
    }
  };

  window.addEventListener("storage", handleStorage);

  return () => {
    channel?.close();
    window.removeEventListener("storage", handleStorage);
  };
}
