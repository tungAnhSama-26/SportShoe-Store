<template>
  <main class="login-page admin-login-page">
    <Transition name="toast">
      <div v-if="toast.show && toast.type !== 'success'" :class="['toast-notification', toast.type]">
        <div class="toast-content">
          <i v-if="toast.type === 'error'" class="fas fa-exclamation-circle"></i>
          <i v-if="toast.type === 'success'" class="fas fa-check-circle"></i>
          <span>{{ toast.message }}</span>
        </div>
      </div>
    </Transition>

    <section class="login-stage" aria-label="SportShoe admin login">
      <div class="corner-logo">SPORTSHOE ADMIN</div>

      <div class="login-content-wrapper">
        <div class="shoe-panel" aria-hidden="true">
          <div class="brand-repeat">
            <span>SPORTSHOE</span>
            <span>SPORTSHOE</span>
            <span>SPORTSHOE</span>
            <span>SPORTSHOE</span>
          </div>
        </div>

        <img
          src="../../assets/login-shoe.png"
          alt="SportShoe sneaker"
          class="hero-shoe"
          @error="handleImageError"
        />

        <aside class="login-box">
          <p class="admin-login-kicker">Hệ thống quản trị</p>
          <h1 class="login-logo">SPORTSHOE</h1>
          <h2 class="login-title">Đăng nhập hệ thống</h2>

          <form class="login-form" @submit.prevent="handleLogin">
            <label class="field">
              <span>Tên đăng nhập</span>
              <input
                v-model="loginForm.username"
                type="text"
                placeholder="Nhập tên đăng nhập"
                autocomplete="username"
              />
            </label>

            <label class="field">
              <span>Mật khẩu</span>
              <div class="password-field">
                <input
                  v-model="loginForm.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="Nhập mật khẩu"
                  autocomplete="current-password"
                />
                <button
                  type="button"
                  class="password-toggle"
                  @click="showPassword = !showPassword"
                >
                  <component :is="showPassword ? EyeOff : Eye" class="icon" />
                </button>
              </div>
            </label>

            <p class="admin-login-note">
              Chỉ tài khoản nhân viên đang hoạt động mới được truy cập hệ thống admin.
            </p>

            <button type="submit" class="primary-login" :disabled="loading">
              <span v-if="!loading">Đăng nhập hệ thống</span>
              <span v-else class="loader"></span>
            </button>

            <router-link to="/login" class="admin-login-switch">
              Đăng nhập tài khoản khách hàng
            </router-link>
          </form>
        </aside>
      </div>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from "vue";
import { Eye, EyeOff } from "lucide-vue-next";
import { useRoute, useRouter } from "vue-router";
import { adminLogin } from "../../services/auth";
import { showSuccess } from "../../utils/alert";
import "./Login.css";

const router = useRouter();
const route = useRoute();

const loginForm = reactive({
  username: "",
  password: "",
});

const showPassword = ref(false);
const loading = ref(false);
const toast = reactive({
  show: false,
  message: "",
  type: "error",
});

const showToast = (message, type = "error") => {
  if (type === "success") {
    showSuccess(message);
    return;
  }

  toast.message = message;
  toast.type = type;
  toast.show = true;
  setTimeout(() => {
    toast.show = false;
  }, 3000);
};

const handleLogin = async () => {
  if (!loginForm.username.trim()) {
    showToast("Vui lòng nhập tên đăng nhập");
    return;
  }
  if (!loginForm.password.trim()) {
    showToast("Vui lòng nhập mật khẩu");
    return;
  }

  loading.value = true;
  try {
    await adminLogin(loginForm.username, loginForm.password);
    showToast("Đăng nhập hệ thống thành công!", "success");
    setTimeout(() => {
      const redirectPath = typeof route.query.redirect === "string" ? route.query.redirect : "/admin";
      router.push(redirectPath.startsWith("/admin") ? redirectPath : "/admin");
    }, 800);
  } catch (error) {
    showToast(error.message || "Đăng nhập hệ thống thất bại");
  } finally {
    loading.value = false;
  }
};

const handleImageError = (event) => {
  event.target.style.display = "none";
};
</script>
