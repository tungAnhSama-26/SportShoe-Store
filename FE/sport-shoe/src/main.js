import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import "./index.css";

// Kịch bản đồng bộ trạng thái đăng nhập
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

    // Kịch bản người dùng thay đổi trạng thái đăng nhập
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

    // Kịch bản nhân viên thay đổi trạng thái đăng nhập
    if (oldVal && (!newVal || oldVal.id !== newVal.id)) {
      window.location.assign("/admin/login?notice=session_terminated");
    }
  }
});

const app = createApp(App);
app.use(router);
app.mount("#app");
