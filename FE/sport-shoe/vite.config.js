import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import tailwindcss from "@tailwindcss/vite";

const backendTarget =
  process.env.VITE_BACKEND_PROXY_TARGET || "http://127.0.0.1:8080";

const createBackendProxy = ({ websocket = false } = {}) => ({
  target: backendTarget,
  changeOrigin: true,
  ws: websocket,
  configure(proxy) {
    proxy.on("proxyReq", (proxyRequest) => {
      proxyRequest.setHeader("origin", backendTarget);
    });
    proxy.on("proxyReqWs", (proxyRequest) => {
      proxyRequest.setHeader("origin", backendTarget);
    });
  }
});

var stdin_default = defineConfig({
  plugins: [vue(), tailwindcss()],
  build: {
    target: "esnext",
    outDir: "build",
    chunkSizeWarningLimit: 1200,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes("face-api.js")) return "face-api";
          if (id.includes("html2pdf.js")) return "html2pdf";
        }
      }
    }
  },
  server: {
    host: "0.0.0.0",
    port: 3e3,
    open: false,
    allowedHosts: [".trycloudflare.com"],
    proxy: {
      "/api": createBackendProxy(),
      "/uploads": createBackendProxy(),
      "/ws": createBackendProxy({ websocket: true })
    }
  }
});
export {
  stdin_default as default
};
