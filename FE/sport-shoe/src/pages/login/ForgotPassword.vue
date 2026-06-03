<template>
  <main class="login-page forgot-password-page">
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

    <section class="login-stage" aria-label="SportShoe forgot password">
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

      <aside class="login-box">
        <h1 class="login-logo">SPORTSHOE</h1>
        <h2 class="login-title">{{ step === 1 ? 'Quên mật khẩu' : 'Đặt lại mật khẩu' }}</h2>

        <form v-if="step === 1" class="login-form" @submit.prevent="handleSendCode">
          <p class="form-desc">Vui lòng nhập tên đăng nhập hoặc email đã đăng ký để nhận mã xác nhận.</p>
          
          <label class="field">
            <span>Tên đăng nhập hoặc Email</span>
            <input
              v-model="forgotForm.account"
              type="text"
              placeholder="Nhập tên đăng nhập hoặc email"
              required
            />
          </label>

          <button type="submit" class="primary-login" :disabled="loading">
            <span v-if="!loading">Gửi mã xác nhận</span>
            <span v-else class="loader"></span>
          </button>

          <p class="register-link">
            Quay lại <router-link to="/login">Đăng nhập</router-link>
          </p>
        </form>

        <form v-else class="login-form" @submit.prevent="handleResetPassword">
          <p class="form-desc">Mã xác nhận đã được gửi đến email của bạn. Vui lòng kiểm tra và nhập bên dưới.</p>
          
          <label class="field">
            <span>Mã xác nhận (6 số)</span>
            <input
              v-model="resetForm.otp"
              type="text"
              placeholder="Nhập 6 số"
              maxlength="6"
              required
            />
          </label>

          <label class="field">
            <span>Mật khẩu mới</span>
            <div class="password-field">
              <input
                v-model="resetForm.newPassword"
                :type="showPassword ? 'text' : 'password'"
                placeholder="Nhập mật khẩu mới"
                required
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

          <button type="submit" class="primary-login" :disabled="loading">
            <span v-if="!loading">Đổi mật khẩu</span>
            <span v-else class="loader"></span>
          </button>

          <p class="register-link">
            Chưa nhận được mã? <a href="#" @click.prevent="step = 1">Gửi lại</a>
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
import { request } from "../../services/auth";
import { showSuccess } from "../../utils/alert";
import "../login/Login.css";

const router = useRouter();
const loading = ref(false);
const step = ref(1);
const showPassword = ref(false);

const forgotForm = reactive({
  account: ""
});

const resetForm = reactive({
  account: "",
  otp: "",
  newPassword: ""
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

const handleSendCode = async () => {
  loading.value = true;
  try {
    const response = await request("/auth/forgot-password", {
      method: "POST",
      body: JSON.stringify({ account: forgotForm.account })
    });
    
    showToast(response.message || "Mã xác nhận đã được gửi!", "success");
    resetForm.account = forgotForm.account;
    step.value = 2;
  } catch (error) {
    showToast(error.message || "Gửi mã thất bại");
  } finally {
    loading.value = false;
  }
};

const handleResetPassword = async () => {
  loading.value = true;
  try {
    const response = await request("/auth/reset-password", {
      method: "POST",
      body: JSON.stringify(resetForm)
    });
    
    showToast(response.message || "Đổi mật khẩu thành công!", "success");
    setTimeout(() => {
      router.push("/login");
    }, 2000);
  } catch (error) {
    showToast(error.message || "Đổi mật khẩu thất bại");
  } finally {
    loading.value = false;
  }
};

const handleImageError = (event) => {
  event.target.style.display = "none";
};
</script>

<style scoped>
.form-desc {
  color: rgba(255, 255, 255, 0.8);
  font-size: calc(10px * var(--scale));
  text-align: center;
  margin-bottom: calc(15px * var(--scale));
  line-height: 1.5;
}

.primary-login {
  margin-top: calc(20px * var(--scale));
}
</style>
