<template>
  <main class="login-page register-page">
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

    <section class="login-stage" aria-label="SportShoe register">
      <div class="corner-logo">SPORTSHOE</div>

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

      <aside class="login-box register-box">
        <h1 class="login-logo">SPORTSHOE</h1>
        <h2 class="login-title">Đăng ký tài khoản</h2>

        <form class="login-form" @submit.prevent="handleRegister">
          <label class="field">
            <span>Họ và tên</span>
            <input
              v-model="registerForm.hoTen"
              type="text"
              placeholder="Nhập họ và tên"
              required
            />
          </label>

          <label class="field">
            <span>Email</span>
            <input
              v-model="registerForm.email"
              type="email"
              placeholder="Nhập email"
              required
            />
          </label>

          <label class="field">
            <span>Số điện thoại</span>
            <input
              v-model="registerForm.sdt"
              type="tel"
              placeholder="Nhập số điện thoại"
              required
            />
          </label>

          <button type="submit" class="primary-login" :disabled="loading">
            <span v-if="!loading">Đăng ký ngay</span>
            <span v-else class="loader"></span>
          </button>

          <p class="register-link">
            Đã có tài khoản? <router-link to="/login">Đăng nhập ngay</router-link>
          </p>
        </form>
      </aside>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from "vue";
import { Eye, EyeOff } from "lucide-vue-next";
import { useRouter } from "vue-router";
import { register } from "../../services/auth";
import { showSuccess } from "../../utils/alert";
import "../login/Login.css";

const router = useRouter();
const loading = ref(false);
const showPassword = ref(false);

const registerForm = reactive({
  hoTen: "",
  email: "",
  sdt: ""
});

const toast = reactive({
  show: false,
  message: "",
  type: "error"
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

const handleRegister = async () => {
  loading.value = true;
  try {
    const response = await register(registerForm);
    
    showToast(response.message || "Đăng ký thành công! Vui lòng kiểm tra email để nhận mật khẩu.", "success");
    setTimeout(() => {
      router.push("/login");
    }, 3000);
  } catch (error) {
    showToast(error.message || "Đăng ký thất bại");
  } finally {
    loading.value = false;
  }
};

const handleImageError = (event) => {
  event.target.style.display = "none";
};
</script>

<style scoped>

.register-box {
  min-height: calc(365px * var(--scale));
  top: calc(50% - (140px * var(--scale)));
}

.primary-login {
  margin-top: calc(30px * var(--scale));
}

@media (max-width: 760px) {
  .register-box {
    margin-top: -18px;
  }
}
</style>
