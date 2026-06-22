import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import "./index.css";

// Điều hướng về trang đăng nhập khi phát hiện tài khoản khác đăng nhập hoặc đăng xuất ở tab khác
window.addEventListener("storage", (event) => {
  if (!event.key) {
    window.location.assign("/login");
    return;
  }
  
  if (event.key === "user") {
    let oldVal = null;
    let newVal = null;
    try {
      oldVal = event.oldValue ? JSON.parse(event.oldValue) : null;
    } catch (e) {}
    try {
      newVal = event.newValue ? JSON.parse(event.newValue) : null;
    } catch (e) {}

    // Nếu trước đó đang đăng nhập và giờ đăng xuất hoặc đổi tài khoản khác
    if (oldVal && (!newVal || oldVal.id !== newVal.id)) {
      window.location.assign("/login?notice=session_terminated");
    }
  }

  if (event.key === "adminUser") {
    let oldVal = null;
    let newVal = null;
    try {
      oldVal = event.oldValue ? JSON.parse(event.oldValue) : null;
    } catch (e) {}
    try {
      newVal = event.newValue ? JSON.parse(event.newValue) : null;
    } catch (e) {}

    // Nếu admin/nhân viên trước đó đang đăng nhập và giờ đăng xuất hoặc đổi tài khoản khác
    if (oldVal && (!newVal || oldVal.id !== newVal.id)) {
      window.location.assign("/admin/login?notice=session_terminated");
    }
  }
});

const app = createApp(App);
app.use(router);
app.mount("#app");
