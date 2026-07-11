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
    headers: {
      "Content-Security-Policy": "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdnjs.cloudflare.com; font-src 'self' data: https://fonts.gstatic.com https://cdnjs.cloudflare.com; img-src 'self' data: blob: https: http://localhost:8080 http://127.0.0.1:8080 https://res.cloudinary.com https://firebasestorage.googleapis.com; media-src 'self' data: blob: http://localhost:8080 http://127.0.0.1:8080; connect-src 'self' ws: wss: https: http://localhost:8080 http://127.0.0.1:8080 https://provinces.open-api.vn;",
      "X-Frame-Options": "SAMEORIGIN",
      "X-Content-Type-Options": "nosniff",
      "X-XSS-Protection": "1; mode=block",
      "Referrer-Policy": "strict-origin-when-cross-origin"
    },
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
