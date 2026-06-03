<template>
  <main class="login-page">
    <!-- Toast Notification -->
    <Transition name="toast">
      <div v-if="toast.show && toast.type !== 'success'" :class="['toast-notification', toast.type]">
        <div class="toast-content">
          <i v-if="toast.type === 'error'" class="fas fa-exclamation-circle"></i>
          <i v-if="toast.type === 'success'" class="fas fa-check-circle"></i>
          <span>{{ toast.message }}</span>
        </div>
      </div>
    </Transition>

    <section class="login-stage" aria-label="SportShoe login">
      <div class="corner-logo">SPORTSHOE</div>

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
          <h1 class="login-logo">SPORTSHOE</h1>
          <h2 class="login-title">Đăng nhập</h2>

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
            <div class="form-row">
              <label class="remember">
                <input v-model="loginForm.remember" type="checkbox" />
                <span>Ghi nhớ mật khẩu</span>
              </label>
              <router-link to="/forgot-password" class="forgot-link">Quên mật khẩu ?</router-link>
            </div>

            <button type="submit" class="primary-login" :disabled="loading">
              <span v-if="!loading">Đăng nhập</span>
              <span v-else class="loader"></span>
            </button>

            <button type="button" class="google-login" @click="loginWithGoogle">
              <svg viewBox="0 0 24 24" class="google-icon" xmlns="http://www.w3.org/2000/svg">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 12-4.53z" fill="#EA4335"/>
              </svg>
              <span>Đăng nhập bằng google</span>
            </button>

            <router-link to="/admin/login" class="admin-login-switch">
              Đăng nhập hệ thống quản trị
            </router-link>

            <p class="register-link">
              Chưa có tài khoản? <router-link to="/register">Đăng ký ngay</router-link>
            </p>
          </form>
        </aside>
      </div>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from "vue";
import { Eye, EyeOff } from "lucide-vue-next";
import { useRouter } from "vue-router";
import { login } from "../../services/auth";
import { showSuccess } from "../../utils/alert";
import "./Login.css";

const router = useRouter();
const loginForm = reactive({
  username: "",
  password: "",
  remember: false,
});

const showPassword = ref(false);
const loading = ref(false);
const toast = reactive({
  show: false,
  message: "",
  type: "error" // error, success, info
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
  // Validation
  if (!loginForm.username.trim()) {
    showToast("Vui lòng nhập tên đăng nhập");
    return;
  }
  if (loginForm.username.includes("@")) {
    showToast("Vui lòng đăng nhập bằng tên đăng nhập, không sử dụng email");
    return;
  }
  if (!loginForm.password.trim()) {
    showToast("Vui lòng nhập mật khẩu");
    return;
  }

  loading.value = true;
  try {
    await login(loginForm.username, loginForm.password);
    showToast("Đăng nhập thành công!", "success");
    setTimeout(() => {
      router.push("/");
    }, 800);
  } catch (error) {
    showToast(error.message || "Đăng nhập thất bại");
  } finally {
    loading.value = false;
  }
};

const loginWithGoogle = () => {
  console.log("Login with Google");
};

const handleImageError = (event) => {
  event.target.style.display = "none";
};
</script>
