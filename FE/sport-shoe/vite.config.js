import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import tailwindcss from "@tailwindcss/vite";
var stdin_default = defineConfig({
  plugins: [vue(), tailwindcss()],
  build: {
    target: "esnext",
    outDir: "build"
  },
  server: {
    port: 3e3,
    open: true
  }
});
export {
  stdin_default as default
};
