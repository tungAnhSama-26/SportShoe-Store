<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  AlertTriangle,
  ArrowLeft,
  FileQuestion,
  LockKeyhole,
  LogIn,
  RefreshCw,
  ShieldAlert,
  TimerReset,
} from "lucide-vue-next";
import { getCurrentAdminUser } from "../../services/auth";
import logoSportShoe from "../../assets/logo/delete-background-logo.png";

const route = useRoute();
const router = useRouter();

const status = computed(() => Number(route.params.status || route.query.status || 500));
const detailMessage = computed(() => String(route.query.message || "").trim());
const redirectPath = computed(() => String(route.query.redirect || "").trim());

const errorMap = {
  401: {
    icon: LockKeyhole,
    eyebrow: "Yêu cầu đăng nhập",
    title: "Phiên làm việc không hợp lệ",
    description:
      "Bạn chưa đăng nhập, token đã hết hạn hoặc chữ ký token không hợp lệ. Vui lòng đăng nhập lại để tiếp tục.",
    primaryText: "Đăng nhập admin",
    primaryIcon: LogIn,
    primaryAction: () =>
      router.push({
        path: "/admin/login",
        query: redirectPath.value ? { redirect: redirectPath.value } : {},
      }),
  },
  403: {
    icon: ShieldAlert,
    eyebrow: "Không đủ quyền",
    title: "Bạn không có quyền truy cập",
    description:
      "Tài khoản hiện tại không có quyền thực hiện chức năng này. Hãy dùng tài khoản admin hoặc quay lại màn phù hợp với vai trò của bạn.",
    primaryText: "Về màn phù hợp",
    primaryIcon: ArrowLeft,
    primaryAction: () => router.push(getCurrentAdminUser() ? "/admin/ban-hang" : "/admin/login"),
  },
  404: {
    icon: FileQuestion,
    eyebrow: "Không tìm thấy",
    title: "Đường dẫn không tồn tại",
    description:
      "Route hoặc API bạn vừa truy cập không còn tồn tại, bị sai đường dẫn hoặc đã được chuyển sang vị trí khác.",
    primaryText: "Về trang chủ",
    primaryIcon: ArrowLeft,
    primaryAction: () => router.push("/"),
  },
  422: {
    icon: AlertTriangle,
    eyebrow: "Dữ liệu chưa hợp lệ",
    title: "Thiếu hoặc sai thông tin bắt buộc",
    description:
      "Yêu cầu gửi lên thiếu dữ liệu như email, mật khẩu hoặc có trường không đúng định dạng. Vui lòng kiểm tra lại form.",
    primaryText: "Quay lại form",
    primaryIcon: ArrowLeft,
    primaryAction: () => router.back(),
  },
  429: {
    icon: TimerReset,
    eyebrow: "Thao tác quá nhiều",
    title: "Bạn đã thử quá nhiều lần",
    description:
      "Hệ thống đang tạm giới hạn yêu cầu để bảo vệ tài khoản. Vui lòng chờ một lát rồi thử lại.",
    primaryText: "Thử lại",
    primaryIcon: RefreshCw,
    primaryAction: () => router.back(),
  },
  500: {
    icon: AlertTriangle,
    eyebrow: "Lỗi hệ thống",
    title: "Máy chủ đang gặp sự cố",
    description:
      "Có lỗi phía server, ví dụ cấu hình JWT secret không hợp lệ hoặc một lỗi ngoài dự kiến. Vui lòng thử lại sau.",
    primaryText: "Tải lại trang",
    primaryIcon: RefreshCw,
    primaryAction: () => window.location.reload(),
  },
};

const config = computed(() => errorMap[status.value] || errorMap[500]);
const Icon = computed(() => config.value.icon);
const PrimaryIcon = computed(() => config.value.primaryIcon);

function goBack() {
  if (window.history.length > 1) {
    router.back();
    return;
  }
  router.push("/");
}
</script>

<template>
  <main class="error-page">
    <section class="error-shell" aria-labelledby="error-title">
      <div class="error-brand">
        <img :src="logoSportShoe" alt="SportShoe" class="error-logo" />
        <div>
          <p class="brand-name">SportShoe</p>
          <p class="brand-subtitle">Trang xử lý truy cập</p>
        </div>
      </div>

      <div class="error-layout">
        <aside class="error-status" aria-hidden="true">
          <span class="status-label">HTTP</span>
          <strong>{{ status }}</strong>
        </aside>

        <div class="error-content">
          <div class="error-icon">
            <component :is="Icon" />
          </div>

          <div class="error-copy">
            <div class="error-meta">
              <span>{{ config.eyebrow }}</span>
              <span>HTTP {{ status }}</span>
            </div>

            <h1 id="error-title">{{ config.title }}</h1>
            <p>{{ config.description }}</p>

            <div v-if="detailMessage" class="error-detail">
              {{ detailMessage }}
            </div>

            <div class="error-actions">
              <button type="button" class="primary-action" @click="config.primaryAction">
                <component :is="PrimaryIcon" />
                {{ config.primaryText }}
              </button>
              <button type="button" class="secondary-action" @click="goBack">
                <ArrowLeft />
                Quay lại
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
.error-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px;
  color: #080808;
  background:
    linear-gradient(135deg, rgba(207, 16, 24, 0.08) 0%, rgba(255, 106, 0, 0.05) 38%, rgba(255, 255, 255, 0) 72%),
    linear-gradient(180deg, #fff8f4 0%, #ffffff 45%, #f8fafc 100%);
}

.error-shell {
  width: min(100%, 960px);
  min-height: min(680px, calc(100vh - 64px));
  border: 1px solid rgba(207, 16, 24, 0.16);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 28px 70px rgba(15, 23, 42, 0.1);
  overflow: hidden;
}

.error-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 26px 30px 18px;
  border-bottom: 1px solid #fee2e2;
  background: linear-gradient(90deg, #ffffff 0%, #fff8f4 100%);
}

.error-logo {
  width: 64px;
  height: 64px;
  object-fit: contain;
}

.brand-name {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  line-height: 1.1;
  color: #cf1018;
}

.brand-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
}

.error-layout {
  min-height: 560px;
  display: grid;
  grid-template-columns: 260px 1fr;
}

.error-status {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 44px 34px;
  background: linear-gradient(180deg, #cf1018 0%, #a40d13 100%);
  color: #ffffff;
}

.status-label {
  width: fit-content;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.error-status strong {
  margin-top: 18px;
  font-size: clamp(72px, 10vw, 118px);
  line-height: 0.9;
  letter-spacing: -0.05em;
}

.error-content {
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 54px 58px;
}

.error-icon {
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  width: 86px;
  height: 86px;
  border-radius: 24px;
  color: #cf1018;
  background: #fff1f2;
  box-shadow: inset 0 0 0 1px #fecaca;
}

.error-icon svg {
  width: 42px;
  height: 42px;
}

.error-copy {
  min-width: 0;
}

.error-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.error-meta span {
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.04em;
}

.error-meta span:first-child {
  color: #cf1018;
  background: #fff1f2;
}

.error-meta span:last-child {
  color: #ff6a00;
  background: #fff7ed;
}

.error-copy h1 {
  margin: 18px 0 0;
  max-width: 620px;
  font-size: clamp(30px, 4vw, 48px);
  line-height: 1.08;
  letter-spacing: 0;
  font-weight: 800;
  color: #080808;
}

.error-copy p {
  margin: 18px 0 0;
  max-width: 600px;
  color: #475569;
  font-size: 15px;
  line-height: 1.8;
  font-weight: 500;
}

.error-detail {
  margin-top: 22px;
  max-width: 600px;
  border-left: 4px solid #ff6a00;
  border-radius: 16px;
  background: #fff8f4;
  padding: 14px 16px;
  color: #334155;
  font-size: 14px;
  font-weight: 600;
}

.error-actions {
  margin-top: 30px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.primary-action,
.secondary-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  min-height: 46px;
  border-radius: 16px;
  padding: 0 18px;
  font-size: 14px;
  font-weight: 800;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.primary-action {
  border: 0;
  color: #ffffff;
  background: linear-gradient(135deg, #cf1018 0%, #ef2f3b 54%, #ff6a00 100%);
  box-shadow: 0 14px 26px rgba(207, 16, 24, 0.24);
}

.secondary-action {
  border: 1px solid #e2e8f0;
  color: #334155;
  background: #ffffff;
}

.primary-action:hover,
.secondary-action:hover {
  transform: translateY(-1px);
}

.primary-action svg,
.secondary-action svg {
  width: 17px;
  height: 17px;
}

@media (max-width: 820px) {
  .error-page {
    padding: 16px;
  }

  .error-shell {
    min-height: auto;
  }

  .error-layout {
    min-height: auto;
    grid-template-columns: 1fr;
  }

  .error-status {
    min-height: 150px;
    padding: 28px 30px;
  }

  .error-status strong {
    font-size: 72px;
  }

  .error-content {
    align-items: flex-start;
    padding: 32px 26px 36px;
  }
}

@media (max-width: 560px) {
  .error-brand {
    padding: 20px;
  }

  .error-content {
    flex-direction: column;
    gap: 20px;
  }

  .primary-action,
  .secondary-action {
    width: 100%;
  }
}
</style>
