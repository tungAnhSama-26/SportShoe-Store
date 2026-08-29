<template>
  <div class="space-y-5 max-w-7xl mx-auto pb-10">
    <!-- Top Action Bar -->
    <div class="flex items-center justify-end">
      <button
        type="button"
        @click="quayLai"
        class="h-9 px-4 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 hover:border-slate-300 dark:hover:border-slate-600 text-xs font-semibold transition flex items-center gap-2 shadow-sm cursor-pointer"
      >
        <ArrowLeft class="h-4 w-4" />
        Quay lại danh sách
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200/80 dark:border-slate-700/80 p-12 text-center shadow-sm">
      <RefreshCw class="h-8 w-8 animate-spin text-primary mx-auto mb-3" />
      <p class="text-sm font-semibold text-slate-500">Đang tải chi tiết ca làm việc...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-rose-50 dark:bg-rose-950/20 border border-rose-200 dark:border-rose-900/40 rounded-2xl p-8 text-center shadow-sm">
      <AlertTriangle class="h-10 w-10 text-rose-500 mx-auto mb-3" />
      <h3 class="text-base font-bold text-rose-700 dark:text-rose-300">Không thể tải thông tin ca</h3>
      <p class="text-xs text-rose-600 dark:text-rose-400 mt-1">{{ error }}</p>
      <button @click="taiChiTietCa" class="mt-4 px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold rounded-xl transition">
        Thử lại
      </button>
    </div>

    <!-- Main Content -->
    <div v-else class="space-y-5">
      <!-- 4-Step Progress Bar -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200/80 dark:border-slate-700/80 p-3.5 shadow-sm">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-3">
          <!-- Step 1 -->
          <div
            class="flex items-center gap-3 p-3 rounded-xl border transition"
            :class="buocHienTai >= 1 ? (buocHienTai === 1 ? 'border-primary/30 bg-primary/5 dark:bg-primary/10 shadow-sm' : 'border-emerald-200 bg-emerald-50/50 dark:border-emerald-900/40 dark:bg-emerald-950/20') : 'border-slate-100 dark:border-slate-700/60'"
          >
            <div
              class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-bold transition"
              :class="buocHienTai >= 1 ? (buocHienTai === 1 ? 'bg-primary text-white' : 'bg-emerald-500 text-white') : 'bg-slate-100 dark:bg-slate-700 text-slate-500'"
            >
              <CheckCircle2 v-if="buocHienTai > 1" class="h-4 w-4" />
              <span v-else>1</span>
            </div>
            <div>
              <p class="text-xs font-bold" :class="buocHienTai >= 1 ? 'text-slate-800 dark:text-white' : 'text-slate-400'">
                1. Đóng ca
              </p>
              <p class="text-[11px] text-slate-400">Nhân viên kết thúc ca</p>
            </div>
          </div>

          <!-- Step 2 -->
          <div
            class="flex items-center gap-3 p-3 rounded-xl border transition"
            :class="buocHienTai >= 2 ? (buocHienTai === 2 ? 'border-primary/30 bg-primary/5 dark:bg-primary/10 shadow-sm' : 'border-emerald-200 bg-emerald-50/50 dark:border-emerald-900/40 dark:bg-emerald-950/20') : 'border-slate-100 dark:border-slate-700/60'"
          >
            <div
              class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-bold transition"
              :class="buocHienTai >= 2 ? (buocHienTai === 2 ? 'bg-primary text-white' : 'bg-emerald-500 text-white') : 'bg-slate-100 dark:bg-slate-700 text-slate-500'"
            >
              <CheckCircle2 v-if="buocHienTai > 2" class="h-4 w-4" />
              <span v-else>2</span>
            </div>
            <div>
              <p class="text-xs font-bold" :class="buocHienTai >= 2 ? 'text-slate-800 dark:text-white' : 'text-slate-400'">
                2. Bàn giao
              </p>
              <p class="text-[11px] text-slate-400">Giao ca cho nhân viên tiếp theo</p>
            </div>
          </div>

          <!-- Step 3 -->
          <div
            class="flex items-center gap-3 p-3 rounded-xl border transition"
            :class="buocHienTai >= 3 ? (buocHienTai === 3 ? 'border-amber-400/80 bg-amber-50/60 dark:border-amber-800 dark:bg-amber-950/30 shadow-sm' : 'border-emerald-200 bg-emerald-50/50 dark:border-emerald-900/40 dark:bg-emerald-950/20') : 'border-slate-100 dark:border-slate-700/60'"
          >
            <div
              class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-bold transition"
              :class="buocHienTai >= 3 ? (buocHienTai === 3 ? 'bg-amber-500 text-white animate-pulse' : 'bg-emerald-500 text-white') : 'bg-slate-100 dark:bg-slate-700 text-slate-500'"
            >
              <CheckCircle2 v-if="buocHienTai > 3" class="h-4 w-4" />
              <span v-else>3</span>
            </div>
            <div>
              <p class="text-xs font-bold" :class="buocHienTai >= 3 ? 'text-slate-800 dark:text-white' : 'text-slate-400'">
                3. Xác nhận
              </p>
              <p class="text-[11px] text-slate-400">Nhân viên nhận ca xác nhận</p>
            </div>
          </div>

          <!-- Step 4 -->
          <div
            class="flex items-center gap-3 p-3 rounded-xl border transition"
            :class="buocHienTai >= 4 ? 'border-emerald-400 bg-emerald-50/70 dark:border-emerald-800 dark:bg-emerald-950/40 shadow-sm' : 'border-slate-100 dark:border-slate-700/60'"
          >
            <div
              class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-bold transition"
              :class="buocHienTai >= 4 ? 'bg-emerald-500 text-white' : 'bg-slate-100 dark:bg-slate-700 text-slate-500'"
            >
              <CheckCircle2 v-if="buocHienTai >= 4" class="h-4 w-4" />
              <span v-else>4</span>
            </div>
            <div>
              <p class="text-xs font-bold" :class="buocHienTai >= 4 ? 'text-slate-800 dark:text-white' : 'text-slate-400'">
                4. Hoàn thành
              </p>
              <p class="text-[11px] text-slate-400">Bàn giao ca thành công</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Layout: 2 Columns -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-5">
        <!-- LEFT 2 COLS: Shift Info, Revenue Summary, Transactions Table -->
        <div class="lg:col-span-2 space-y-5">
          <!-- CARD 1: Thông tin ca làm việc -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200/80 dark:border-slate-700/80 p-5 shadow-sm space-y-4">
            <div class="flex items-center justify-between pb-2 border-b border-slate-100 dark:border-slate-700/60">
              <h3 class="text-sm font-bold text-slate-800 dark:text-white">
                Thông tin ca làm việc
              </h3>
              <span
                class="px-2.5 py-0.5 text-xs font-semibold rounded-full"
                :class="statusBadge.bg"
              >
                {{ statusBadge.label }}
              </span>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-3.5 text-sm">
              <div class="flex justify-between md:justify-start gap-4">
                <span class="text-slate-400 min-w-[105px]">Mã ca:</span>
                <span class="font-bold text-primary">{{ caInfo?.ma }}</span>
              </div>
              <div class="flex justify-between md:justify-start gap-4">
                <span class="text-slate-400 min-w-[105px]">Thời gian vào:</span>
                <span class="font-medium text-slate-700 dark:text-slate-200">
                  {{ formatDateTime(caInfo?.thoiGianVao) || '—' }}
                </span>
              </div>
              <div class="flex justify-between md:justify-start gap-4">
                <span class="text-slate-400 min-w-[105px]">Nhân viên ca:</span>
                <span class="font-bold text-slate-700 dark:text-slate-200">
                  {{ caInfo?.nhanVienTrongCaTen || 'Nhân viên' }}
                </span>
              </div>
              <div class="flex justify-between md:justify-start gap-4">
                <span class="text-slate-400 min-w-[105px]">Trạng thái:</span>
                <span class="font-semibold text-slate-700 dark:text-slate-200">
                  {{ displayShiftStatus }}
                </span>
              </div>
              <div class="flex justify-between md:justify-start gap-4">
                <span class="text-slate-400 min-w-[105px]">Ca làm việc:</span>
                <span class="font-medium text-slate-700 dark:text-slate-200">
                  {{ caInfo?.caLamTen ? `${caInfo.caLamTen} (${caInfo?.gioBatDau?.slice(0, 5) || ''} - ${caInfo?.gioKetThuc?.slice(0, 5) || ''})` : 'Ca làm việc' }}
                </span>
              </div>
              <div class="flex justify-between md:justify-start gap-4">
                <span class="text-slate-400 min-w-[105px]">Thời gian ra:</span>
                <span class="font-medium text-slate-700 dark:text-slate-200">
                  {{ formatDateTime(caInfo?.thoiGianRa) || 'Chưa kết thúc' }}
                </span>
              </div>
            </div>
          </div>

          <!-- CARD 2: Tổng hợp doanh thu -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200/80 dark:border-slate-700/80 p-5 shadow-sm space-y-4">
            <h3 class="text-sm font-bold text-slate-800 dark:text-white pb-2 border-b border-slate-100 dark:border-slate-700/60">
              Tổng hợp doanh thu
            </h3>

            <div class="space-y-1">
              <div class="flex justify-between items-center py-2.5 border-b border-slate-100 dark:border-slate-700/60 text-sm">
                <span class="text-slate-500">Tiền đầu ca</span>
                <span class="font-bold text-slate-700 dark:text-slate-200">{{ formatVND(tienDauCa) }}</span>
              </div>
              <div class="flex justify-between items-center py-2.5 border-b border-slate-100 dark:border-slate-700/60 text-sm">
                <span class="text-slate-500">Doanh thu tiền mặt</span>
                <span class="font-bold text-slate-700 dark:text-slate-200">+{{ formatVND(doanhThuTienMat) }}</span>
              </div>
              <div class="flex justify-between items-center py-2.5 border-b border-slate-100 dark:border-slate-700/60 text-sm">
                <span class="text-slate-500">Doanh thu chuyển khoản</span>
                <span class="font-bold text-slate-700 dark:text-slate-200">+{{ formatVND(doanhThuChuyenKhoan) }}</span>
              </div>
              <div class="flex justify-between items-center py-2.5 border-b border-slate-100 dark:border-slate-700/60 text-sm">
                <span class="text-slate-500">Tổng thu khác</span>
                <span class="font-bold text-slate-700 dark:text-slate-200">+0 đ</span>
              </div>
              <div class="flex justify-between items-center py-2.5 border-b border-slate-100 dark:border-slate-700/60 text-sm">
                <span class="text-slate-500">Tổng chi trong ca</span>
                <span class="font-bold text-slate-700 dark:text-slate-200">0 đ</span>
              </div>
            </div>

            <!-- 3 Highlights Stat Boxes -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-3 pt-2">
              <div class="p-3.5 rounded-xl bg-blue-50/70 dark:bg-blue-950/20 border border-blue-100 dark:border-blue-900/40 text-center space-y-1">
                <p class="text-[11px] font-semibold text-blue-600 dark:text-blue-400">Tiền mặt theo hệ thống</p>
                <p class="text-base font-bold text-blue-700 dark:text-blue-300">{{ formatVND(tienTheoHeThong) }}</p>
              </div>
              <div class="p-3.5 rounded-xl bg-emerald-50/70 dark:bg-emerald-950/20 border border-emerald-100 dark:border-emerald-900/40 text-center space-y-1">
                <p class="text-[11px] font-semibold text-emerald-600 dark:text-emerald-400">Tiền mặt thực tế</p>
                <p class="text-base font-bold text-emerald-700 dark:text-emerald-300">{{ formatVND(tienThucTe) }}</p>
              </div>
              <div
                class="p-3.5 rounded-xl border text-center space-y-1"
                :class="chenhLechDisplay.bg"
              >
                <p class="text-[11px] font-semibold" :class="chenhLechDisplay.text">
                  Chênh lệch
                </p>
                <p class="text-base font-bold" :class="chenhLechDisplay.text">
                  {{ formatVND(Math.abs(tienChenhLech)) }}
                </p>
                <span class="text-[10px] font-medium opacity-80" :class="chenhLechDisplay.text">
                  {{ chenhLechDisplay.note }}
                </span>
              </div>
            </div>
          </div>

          <!-- CARD 3: Chi tiết giao dịch trong ca -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200/80 dark:border-slate-700/80 p-5 shadow-sm space-y-4">
            <div class="flex items-center justify-between pb-2 border-b border-slate-100 dark:border-slate-700/60">
              <h3 class="text-sm font-bold text-slate-800 dark:text-white">
                Chi tiết giao dịch trong ca
              </h3>
              <span class="text-xs text-slate-500 font-medium">
                {{ transactions.length }} giao dịch
              </span>
            </div>

            <!-- Tab badge -->
            <div class="flex gap-2">
              <span class="px-3 py-1.5 text-xs font-semibold rounded-lg bg-primary text-white shadow-sm">
                Doanh thu ca làm việc
              </span>
            </div>

            <!-- Transactions Table -->
            <div class="overflow-x-auto rounded-xl border border-slate-100 dark:border-slate-700/60">
              <table class="w-full text-left text-xs border-collapse">
                <thead>
                  <tr class="border-b border-slate-100 dark:border-slate-700/60 bg-slate-50/80 dark:bg-slate-900/40 text-slate-500 font-semibold text-[11px]">
                    <th class="px-4 py-3">Thời gian</th>
                    <th class="px-4 py-3">Mã hóa đơn</th>
                    <th class="px-4 py-3">Loại doanh thu</th>
                    <th class="px-4 py-3">Số tiền</th>
                    <th class="px-4 py-3">Hình thức</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 dark:divide-slate-700/40">
                  <tr v-if="loadingTransactions" class="text-center">
                    <td colspan="5" class="px-4 py-6 text-slate-400">
                      <RefreshCw class="h-4 w-4 animate-spin inline mr-2 text-primary" />
                      Đang tải danh sách giao dịch...
                    </td>
                  </tr>
                  <tr v-else-if="transactions.length === 0" class="text-center">
                    <td colspan="5" class="px-4 py-8 text-slate-400 italic">
                      Chưa có hóa đơn nào phát sinh trong ca làm việc này.
                    </td>
                  </tr>
                  <tr
                    v-else
                    v-for="tx in transactions"
                    :key="tx.id"
                    class="hover:bg-slate-50/50 dark:hover:bg-slate-900/30 transition text-slate-600 dark:text-slate-300"
                  >
                    <td class="px-4 py-3.5 font-medium">
                      {{ formatTimeOnly(tx.ngayTao) }}
                    </td>
                    <td class="px-4 py-3.5 font-bold text-primary">
                      <router-link
                        v-if="tx.id"
                        :to="{ name: 'admin-hoa-don-chi-tiet', params: { id: tx.id } }"
                        class="hover:underline flex items-center gap-1"
                        title="Xem chi tiết hóa đơn"
                      >
                        {{ tx.maHoaDon || tx.ma }}
                        <ExternalLink class="h-3 w-3 opacity-60" />
                      </router-link>
                      <span v-else>{{ tx.maHoaDon || tx.ma }}</span>
                    </td>
                    <td class="px-4 py-3.5">
                      {{ tx.loaiDon === 'ONLINE' ? 'Đơn hàng online' : 'Bán hàng tại quầy' }}
                    </td>
                    <td class="px-4 py-3.5 font-bold text-emerald-600 dark:text-emerald-400">
                      +{{ formatVND(tx.tongTien) }}
                    </td>
                    <td class="px-4 py-3.5">
                      <span
                        class="px-2.5 py-1 text-[11px] font-semibold rounded-full"
                        :class="formatPaymentBadge(tx.phuongThucThanhToan)"
                      >
                        {{ tx.phuongThucThanhToan || 'Tiền mặt' }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <p class="text-[11px] italic text-slate-400 text-center pt-1">
              Số liệu được cập nhật tự động theo thời gian thực từ các hóa đơn tại quầy.
            </p>
          </div>
        </div>

        <!-- RIGHT COL: Handover Info & Signatures (Read-Only) -->
        <div class="space-y-5">
          <!-- CARD 4: Thông tin bàn giao -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200/80 dark:border-slate-700/80 p-5 shadow-sm space-y-4">
            <h3 class="text-sm font-bold text-slate-800 dark:text-white pb-2 border-b border-slate-100 dark:border-slate-700/60">
              Thông tin bàn giao
            </h3>

            <div class="space-y-4">
              <!-- Số tiền bàn giao thực tế -->
              <div>
                <label class="block text-xs font-semibold text-slate-500 dark:text-slate-400 mb-1.5">
                  Số tiền bàn giao thực tế
                </label>
                <div class="flex items-center gap-2.5">
                  <div class="relative flex-1">
                    <input
                      type="text"
                      :value="formatVND(tienThucTe).replace(' đ', '')"
                      readonly
                      disabled
                      class="w-full h-10 pl-3.5 pr-10 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-900/50 text-sm font-bold text-blue-600 dark:text-blue-400 cursor-not-allowed"
                    />
                    <span class="absolute right-3.5 top-1/2 -translate-y-1/2 text-xs font-semibold text-slate-400">đ</span>
                  </div>
                  <div class="h-10 w-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center shrink-0">
                    <Calculator class="h-4 w-4" />
                  </div>
                </div>
                <p class="mt-1.5 text-[11px] italic text-slate-500 dark:text-slate-400 leading-normal">
                  {{ docTienBangChu(tienThucTe) }}
                </p>
              </div>

              <!-- Nhân viên nhận ca -->
              <div>
                <label class="block text-xs font-semibold text-slate-500 dark:text-slate-400 mb-1.5">
                  Nhân viên nhận ca
                </label>
                <div class="w-full h-10 px-3.5 flex items-center justify-between border border-slate-200 dark:border-slate-700 rounded-xl bg-slate-50 dark:bg-slate-900/50 text-xs font-semibold text-slate-700 dark:text-slate-300">
                  <span>{{ caInfo?.nhanVienNhanTen ? `${caInfo.nhanVienNhanTen} (${caInfo.nhanVienNhanMa || ''})` : '-- Chưa chọn nhân viên nhận ca --' }}</span>
                  <ChevronDown class="h-4 w-4 text-slate-400" />
                </div>
              </div>

              <!-- Lý do chênh lệch (nếu có) -->
              <div v-if="tienChenhLech !== 0">
                <label class="block text-xs font-semibold text-rose-500 mb-1.5">
                  Lý do chênh lệch
                </label>
                <div class="w-full p-3 border border-rose-200 dark:border-rose-900/40 rounded-xl bg-rose-50/50 dark:bg-rose-950/20 text-xs font-semibold text-rose-700 dark:text-rose-300">
                  {{ caInfo?.lyDoChenhLech || 'Không có lý do ghi nhận' }}
                </div>
              </div>

              <!-- Ghi chú bàn giao -->
              <div>
                <label class="block text-xs font-semibold text-slate-500 dark:text-slate-400 mb-1.5">
                  Ghi chú bàn giao
                </label>
                <div class="w-full min-h-[75px] p-3 border border-slate-200 dark:border-slate-700 rounded-xl bg-slate-50 dark:bg-slate-900/50 text-xs text-slate-600 dark:text-slate-300 whitespace-pre-wrap">
                  {{ caInfo?.ghiChu || 'Không có ghi chú bàn giao' }}
                </div>
                <div class="flex justify-end text-[10px] text-slate-400 mt-1">
                  {{ (caInfo?.ghiChu || '').length }}/200
                </div>
              </div>

              <!-- Read-only Button / Banner -->
              <div v-if="buocHienTai === 1" class="pt-1">
                <button
                  type="button"
                  disabled
                  class="w-full py-3 bg-primary/80 text-white font-bold rounded-xl shadow-sm transition text-xs opacity-90 cursor-not-allowed flex items-center justify-center gap-2"
                >
                  <Clock class="h-4 w-4" />
                  Ca đang làm việc (Chỉ xem)
                </button>
              </div>
              <div v-else-if="buocHienTai === 3" class="pt-1">
                <div class="w-full py-3 bg-amber-500 text-white font-bold rounded-xl shadow-sm text-xs text-center flex items-center justify-center gap-2">
                  <Clock class="h-4 w-4" />
                  Đang chờ nhân viên nhận xác nhận
                </div>
              </div>
              <div v-else class="pt-1">
                <div class="w-full py-3 bg-emerald-600 text-white font-bold rounded-xl shadow-sm text-xs text-center flex items-center justify-center gap-2">
                  <CheckCircle2 class="h-4 w-4" />
                  Đã hoàn thành bàn giao ca
                </div>
              </div>
            </div>
          </div>

          <!-- CARD 5: Xác nhận bàn giao -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200/80 dark:border-slate-700/80 p-5 shadow-sm space-y-4">
            <h3 class="text-sm font-bold text-slate-800 dark:text-white pb-2 border-b border-slate-100 dark:border-slate-700/60">
              Xác nhận bàn giao
            </h3>

            <div class="space-y-3.5">
              <!-- Nhân viên giao ca -->
              <div class="border border-slate-100 dark:border-slate-700/80 rounded-xl p-3.5 bg-slate-50/50 dark:bg-slate-900/20 space-y-2.5">
                <div class="flex justify-between items-start">
                  <div>
                    <p class="text-[11px] font-semibold text-slate-400">Nhân viên giao ca</p>
                    <h4 class="font-bold text-xs text-slate-800 dark:text-slate-200 mt-0.5">
                      {{ caInfo?.nhanVienTrongCaTen || 'Nhân viên' }}
                    </h4>
                    <p class="text-[10px] text-slate-400 mt-0.5">
                      Vào ca: {{ formatDateTime(caInfo?.thoiGianVao) || '—' }}
                    </p>
                  </div>
                  <span
                    class="px-2 py-0.5 text-[10px] font-semibold rounded-full"
                    :class="buocHienTai >= 2 ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300' : 'bg-amber-100 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300'"
                  >
                    {{ buocHienTai >= 2 ? 'Đã ký bàn giao' : 'Đang hoạt động' }}
                  </span>
                </div>
                <div class="h-14 rounded-lg border border-dashed border-slate-200 dark:border-slate-700 flex items-center justify-center text-xs italic text-slate-400 bg-white/60 dark:bg-slate-800/60">
                  {{ buocHienTai >= 2 ? '✓ Đã ký điện tử biên bản bàn giao' : 'Chưa có chữ ký' }}
                </div>
              </div>

              <!-- Nhân viên nhận ca -->
              <div class="border border-slate-100 dark:border-slate-700/80 rounded-xl p-3.5 bg-slate-50/50 dark:bg-slate-900/20 space-y-2.5">
                <div class="flex justify-between items-start">
                  <div>
                    <p class="text-[11px] font-semibold text-slate-400">Nhân viên nhận ca</p>
                    <h4 class="font-bold text-xs text-slate-800 dark:text-slate-200 mt-0.5">
                      {{ caInfo?.nhanVienNhanTen || 'Chưa nhận' }}
                    </h4>
                    <p class="text-[10px] text-slate-400 mt-0.5">
                      Nhận ca: {{ formatDateTime(caInfo?.thoiGianXacNhan || caInfo?.thoiGianRa) || 'Chưa nhận' }}
                    </p>
                  </div>
                  <span
                    class="px-2 py-0.5 text-[10px] font-semibold rounded-full"
                    :class="buocHienTai >= 4 ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300' : (buocHienTai === 3 ? 'bg-amber-100 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300' : 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400')"
                  >
                    {{ buocHienTai >= 4 ? 'Đã nhận ca thành công' : (buocHienTai === 3 ? 'Chờ xác nhận' : 'Chưa nhận') }}
                  </span>
                </div>
                <div class="h-14 rounded-lg border border-dashed border-slate-200 dark:border-slate-700 flex items-center justify-center text-xs italic text-slate-400 bg-white/60 dark:bg-slate-800/60">
                  {{ buocHienTai >= 4 ? '✓ Đã ký nhận và tiếp quản két tiền' : 'Chưa có chữ ký' }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  ArrowLeft,
  RefreshCw,
  AlertTriangle,
  CheckCircle2,
  Clock,
  Calculator,
  ChevronDown,
  ExternalLink,
} from "lucide-vue-next";
import { layChiTietGiaoCa } from "../../../services/giao-ca.js";
import { layDanhSachHoaDon } from "../../../services/hoa-don.js";

const route = useRoute();
const router = useRouter();

const caInfo = ref(null);
const transactions = ref([]);
const loading = ref(true);
const loadingTransactions = ref(false);
const error = ref("");

const buocHienTai = computed(() => {
  const st = caInfo.value?.trangThai;
  if (st === "MO_CA" || st === "DANG_LAM" || st === "0" || st === 0) return 1;
  if (st === "CHO_BAN_GIAO" || st === "2" || st === 2) return 3;
  if (st === "DA_BAN_GIAO" || st === "HOAN_TAT" || st === "1" || st === 1 || st === "DA_KET_THUC") return 4;
  return 1;
});

const statusBadge = computed(() => {
  if (buocHienTai.value === 1) {
    return {
      label: "Đang hoạt động",
      bg: "bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300",
    };
  }
  if (buocHienTai.value === 3) {
    return {
      label: "Chờ xác nhận",
      bg: "bg-amber-100 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300",
    };
  }
  return {
    label: "Hoàn thành",
    bg: "bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300",
  };
});

const displayShiftStatus = computed(() => {
  const st = caInfo.value?.trangThai;
  if (st === "MO_CA" || st === "DANG_LAM" || st === "0" || st === 0) return "Đang làm việc";
  if (st === "CHO_BAN_GIAO" || st === "2" || st === 2) return "Chờ xác nhận";
  if (st === "DA_BAN_GIAO" || st === "HOAN_TAT" || st === "1" || st === 1) return "Đã bàn giao";
  if (st === "DA_KET_THUC") return "Đã kết thúc";
  return st || "Đang làm việc";
});

const tienDauCa = computed(() => Number(caInfo.value?.tienDauCa || 0));
const doanhThuTienMat = computed(() => Number(caInfo.value?.tienMatTrongCa || 0));
const doanhThuChuyenKhoan = computed(() => Number(caInfo.value?.tienChuyenKhoanTrongCa || 0));

const tienTheoHeThong = computed(() => {
  if (caInfo.value?.tienCuoiCaHeThong != null) {
    return Number(caInfo.value.tienCuoiCaHeThong);
  }
  return tienDauCa.value + doanhThuTienMat.value;
});

const tienThucTe = computed(() => {
  if (caInfo.value?.tienCuoiCaThucTe != null) {
    return Number(caInfo.value.tienCuoiCaThucTe);
  }
  if (caInfo.value?.tienNhanKiemDem != null) {
    return Number(caInfo.value.tienNhanKiemDem);
  }
  return tienTheoHeThong.value;
});

const tienChenhLech = computed(() => {
  if (caInfo.value?.tienChenhLech != null) {
    return Number(caInfo.value.tienChenhLech);
  }
  if (buocHienTai.value === 1) {
    return 0;
  }
  return tienThucTe.value - tienTheoHeThong.value;
});

const chenhLechDisplay = computed(() => {
  const diff = tienChenhLech.value;
  if (diff === 0) {
    return {
      bg: "bg-slate-50 dark:bg-slate-900/30 border-slate-200 dark:border-slate-700",
      text: "text-slate-600 dark:text-slate-300",
      note: "(Khớp)",
    };
  }
  if (diff > 0) {
    return {
      bg: "bg-amber-50 dark:bg-amber-950/20 border-amber-200 dark:border-amber-900/40",
      text: "text-amber-600 dark:text-amber-400",
      note: "(Thừa)",
    };
  }
  return {
    bg: "bg-rose-50 dark:bg-rose-950/20 border-rose-200 dark:border-rose-900/40",
    text: "text-rose-600 dark:text-rose-400",
    note: "(Thiếu)",
  };
});

function formatVND(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value || 0).replace("₫", "đ");
}

function formatDateTime(timeStr) {
  if (!timeStr) return null;
  try {
    const date = new Date(timeStr);
    const time = date.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit", second: "2-digit" });
    const day = date.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" });
    return `${time} ${day}`;
  } catch (e) {
    return null;
  }
}

function formatTimeOnly(timeStr) {
  if (!timeStr) return "—";
  try {
    return new Date(timeStr).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit", second: "2-digit" });
  } catch (e) {
    return "—";
  }
}

function formatPaymentBadge(method) {
  if (!method || method === "Tiền mặt") {
    return "bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300";
  }
  if (method === "Chuyển khoản") {
    return "bg-blue-100 text-blue-700 dark:bg-blue-950/40 dark:text-blue-300";
  }
  if (method === "Kết hợp") {
    return "bg-amber-100 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300";
  }
  return "bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300";
}

function docTienBangChu(money) {
  if (money == null || Number(money) === 0) return "(Bằng chữ: Không đồng)";
  
  const textDenominations = ["", " nghìn", " triệu", " tỷ"];
  const digits = ["không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"];
  
  let str = "";
  let tempMoney = Math.abs(Number(money));
  let groupCount = 0;
  
  while (tempMoney > 0) {
    let group = tempMoney % 1000;
    tempMoney = Math.floor(tempMoney / 1000);
    
    if (group > 0 || groupCount === 0) {
      let groupStr = "";
      let hundreds = Math.floor(group / 100);
      let tens = Math.floor((group % 100) / 10);
      let units = group % 10;
      
      if (hundreds > 0 || tempMoney > 0) {
        groupStr += digits[hundreds] + " trăm ";
      }
      
      if (tens > 0) {
        if (tens === 1) groupStr += "mười ";
        else groupStr += digits[tens] + " mươi ";
      } else if (hundreds > 0 && units > 0) {
        groupStr += "lẻ ";
      }
      
      if (units > 0) {
        if (units === 1 && tens > 1) groupStr += "mốt";
        else if (units === 5 && tens > 0) groupStr += "lăm";
        else groupStr += digits[units];
      }
      
      str = groupStr + textDenominations[groupCount] + " " + str;
    }
    groupCount++;
  }
  
  str = str.trim();
  if (str.length > 0) {
    str = str.charAt(0).toUpperCase() + str.slice(1);
  }
  return `(Bằng chữ: ${str} đồng)`;
}

async function taiChiTietCa() {
  const id = route.params.id;
  if (!id) {
    error.value = "Không tìm thấy mã định danh ca làm việc.";
    loading.value = false;
    return;
  }

  loading.value = true;
  error.value = "";

  try {
    const data = await layChiTietGiaoCa(id);
    caInfo.value = data;
    await taiGiaoDichTrongCa(id);
  } catch (err) {
    console.error("Lỗi tải chi tiết ca:", err);
    error.value = err.message || "Không thể tải thông tin ca làm việc.";
  } finally {
    loading.value = false;
  }
}

async function taiGiaoDichTrongCa(shiftId) {
  loadingTransactions.value = true;
  try {
    const res = await layDanhSachHoaDon({
      giaoCaId: shiftId,
    });
    if (res && Array.isArray(res)) {
      transactions.value = res.sort((a, b) => new Date(b.ngayTao).getTime() - new Date(a.ngayTao).getTime());
    } else {
      transactions.value = [];
    }
  } catch (err) {
    console.error("Lỗi tải chi tiết giao dịch trong ca:", err);
    transactions.value = [];
  } finally {
    loadingTransactions.value = false;
  }
}

function quayLai() {
  router.push({ name: "admin-lich-su-hoat-dong" });
}

onMounted(() => {
  taiChiTietCa();
});
</script>
