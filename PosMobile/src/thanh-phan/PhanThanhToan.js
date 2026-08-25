import React, { useState, useEffect, useRef } from 'react';
import { View, Text, TouchableOpacity, TextInput, StyleSheet, ActivityIndicator, Image, Modal } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { suDungBanHang } from '../ngu-canh/NguCanhBanHang';
import { PHUONG_THUC_THANH_TOAN } from '../features/ban-hang-tai-quay/Enum';
import { dinhDangSo } from '../features/ban-hang-tai-quay/TienTe';
import { showWarning } from '../utils/alert';

export default function PhanThanhToan() {
  const { 
    gioHangLogic, 
    phieuGiamGiaLogic, 
    giaoHangLogic,
    thanhToanLogic,
    hoaDonChoDaChon,
    khachCanTra, 
    dangThanhToan, 
    xuLyThanhToanNgay
  } = suDungBanHang();

  const { cartItems, tongTien } = gioHangLogic;
  const { phieuGiamGiaDaApDung } = phieuGiamGiaLogic;
  const tienGiam = phieuGiamGiaDaApDung ? phieuGiamGiaDaApDung.soTienGiam : 0;
  const phiGiaoHang = (giaoHangLogic?.phiVanChuyenHienThi) || 0;
  
  const {
    phuongThucThanhToan,
    setPhuongThucThanhToan,
    tienKhachDua,
    xuLyTienKhachDuaInput,
    tienMatKetHop,
    xuLyTienMatKetHopInput,
    tienChuyenKhoanKetHop,
    xuLyTienChuyenKhoanKetHopInput,
    thongBaoLoiThanhToan,
    tienThua,
    hienThiMaQrLon: showLargeQr,
    setHienThiMaQrLon: setShowLargeQr
  } = thanhToanLogic;

  // Countdown timer for QR
  const [timeLeft, setTimeLeft] = useState(300);
  const timerRef = useRef(null);

  const startTimer = () => {
    if (timerRef.current) clearInterval(timerRef.current);
    setTimeLeft(300);
    timerRef.current = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          clearInterval(timerRef.current);
          timerRef.current = null;
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  const stopTimer = () => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
  };

  useEffect(() => {
    if (phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN || showLargeQr) {
      startTimer();
    } else {
      stopTimer();
    }
    return () => stopTimer();
  }, [phuongThucThanhToan, showLargeQr]);

  const formattedTimeLeft = `${Math.floor(timeLeft / 60)}:${(timeLeft % 60).toString().padStart(2, '0')}`;

  // VietQR URL generator
  const getSepayQrUrl = () => {
    const bank = 'MB';
    const acc = '894932828';
    const prefix = 'SHOE';
    let amount = Math.max(Number(khachCanTra) || 0, 0);
    if (phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.KET_HOP) {
      const raw = String(tienChuyenKhoanKetHop || 0).replace(/\D/g, '');
      amount = raw !== '' ? (Number(raw) || 0) : 0;
    }
    const maHd = hoaDonChoDaChon?.ma || hoaDonChoDaChon?.maHoaDon || '';
    const description = encodeURIComponent(`${prefix}${maHd}`);
    const accountName = encodeURIComponent('TRAN VU TUNG ANH');
    return `https://img.vietqr.io/image/${bank}-${acc}-compact2.png?amount=${amount}&addInfo=${description}&accountName=${accountName}`;
  };

  const handleOpenCombinedQr = () => {
    const cashNum = Number(String(tienMatKetHop || 0).replace(/\D/g, ''));
    const transferNum = Number(String(tienChuyenKhoanKetHop || 0).replace(/\D/g, ''));
    const needed = Number(khachCanTra) || 0;

    if (cashNum >= needed && needed > 0) {
      showWarning("Tiền mặt đã đủ số tiền cần trả. Số tiền chuyển khoản là 0đ, không cần quét QR!");
      return;
    }
    if (transferNum <= 0) {
      showWarning("Số tiền chuyển khoản hiện tại là 0đ. Vui lòng nhập số tiền cần chuyển khoản!");
      return;
    }
    setShowLargeQr(true);
  };

  // Quick cash options
  const quickCashOptions = React.useMemo(() => {
    if (khachCanTra <= 0) return [];
    const options = [khachCanTra];
    const baseStep = khachCanTra >= 500000 ? 100000 : 50000;
    const roundedUp = Math.ceil(khachCanTra / baseStep) * baseStep;
    if (roundedUp > khachCanTra && !options.includes(roundedUp)) {
      options.push(roundedUp);
    }
    const higherRound = roundedUp + (khachCanTra >= 500000 ? 200000 : 50000);
    if (higherRound > roundedUp && !options.includes(higherRound) && options.length < 3) {
      options.push(higherRound);
    }
    return options;
  }, [khachCanTra]);

  return (
    <View style={styles.container}>
      {/* Price breakdown */}
      <View style={styles.breakdownBox}>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>
            Tổng tiền hàng {cartItems.length > 0 ? `(${cartItems.reduce((acc, item) => acc + item.soLuong, 0)} sản phẩm)` : ''}:
          </Text>
          <Text style={styles.summaryValue}>{tongTien.toLocaleString('vi-VN')} đ</Text>
        </View>

        {tienGiam > 0 && (
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Tiền giảm:</Text>
            <Text style={styles.discountValue}>-{tienGiam.toLocaleString('vi-VN')} đ</Text>
          </View>
        )}

        {phiGiaoHang > 0 && (
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Phí vận chuyển:</Text>
            <Text style={styles.shippingValue}>+{phiGiaoHang.toLocaleString('vi-VN')} đ</Text>
          </View>
        )}

        <View style={[styles.summaryRow, styles.totalRow]}>
          <Text style={styles.summaryLabelBold}>Khách cần trả:</Text>
          <Text style={styles.summaryValueBold}>{khachCanTra.toLocaleString('vi-VN')} đ</Text>
        </View>
      </View>

      {/* Payment methods */}
      <View style={styles.paymentSection}>
        <Text style={styles.paymentTitle}>Hình thức thanh toán</Text>
        <View style={styles.paymentMethodsRow}>
          {/* 1. Tiền mặt */}
          <TouchableOpacity 
            style={[styles.paymentMethodBtn, phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT && styles.paymentMethodBtnActive]}
            onPress={() => setPhuongThucThanhToan(PHUONG_THUC_THANH_TOAN.TIEN_MAT)}
          >
            <Ionicons 
              name="cash-outline" 
              size={16} 
              color={phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT ? "#ef4444" : "#64748b"} 
            />
            <Text style={[styles.paymentMethodText, phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT && styles.paymentMethodTextActive]}>
              Tiền mặt
            </Text>
          </TouchableOpacity>

          {/* 2. Chuyển khoản */}
          <TouchableOpacity 
            style={[styles.paymentMethodBtn, phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN && styles.paymentMethodBtnActive]}
            onPress={() => {
              setPhuongThucThanhToan(PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN);
              setShowLargeQr(true);
            }}
          >
            <Ionicons 
              name="qr-code-outline" 
              size={16} 
              color={phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN ? "#ef4444" : "#64748b"} 
            />
            <Text style={[styles.paymentMethodText, phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN && styles.paymentMethodTextActive]}>
              Chuyển khoản
            </Text>
          </TouchableOpacity>

          {/* 3. Kết hợp */}
          <TouchableOpacity 
            style={[styles.paymentMethodBtn, phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.KET_HOP && styles.paymentMethodBtnActive]}
            onPress={() => setPhuongThucThanhToan(PHUONG_THUC_THANH_TOAN.KET_HOP)}
          >
            <Ionicons 
              name="swap-horizontal-outline" 
              size={16} 
              color={phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.KET_HOP ? "#ef4444" : "#64748b"} 
            />
            <Text style={[styles.paymentMethodText, phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.KET_HOP && styles.paymentMethodTextActive]}>
              Kết hợp
            </Text>
          </TouchableOpacity>
        </View>

        {/* Phương thức: Tiền mặt */}
        {phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT && (
          <View style={styles.amountInputContainer}>
            <View style={styles.amountLabelRow}>
              <Text style={styles.amountInputLabel}>Số tiền khách đưa</Text>
              {quickCashOptions.length > 0 && (
                <View style={styles.quickChipsRow}>
                  {quickCashOptions.map((amount, idx) => (
                    <TouchableOpacity
                      key={idx}
                      style={styles.quickChip}
                      onPress={() => xuLyTienKhachDuaInput(dinhDangSo(amount))}
                    >
                      <Text style={styles.quickChipText}>
                        {idx === 0 ? "Đúng số tiền" : `${(amount / 1000).toLocaleString('vi-VN')}k`}
                      </Text>
                    </TouchableOpacity>
                  ))}
                </View>
              )}
            </View>
            <TextInput
              style={[styles.amountInput, thongBaoLoiThanhToan ? styles.amountInputError : null]}
              value={tienKhachDua}
              onChangeText={xuLyTienKhachDuaInput}
              keyboardType="numeric"
              placeholder="Nhập số tiền khách đưa..."
              placeholderTextColor="#94a3b8"
            />
            {!!thongBaoLoiThanhToan && (
              <Text style={styles.errorText}>{thongBaoLoiThanhToan}</Text>
            )}
            {tienThua > 0 && !thongBaoLoiThanhToan && (
              <View style={styles.changeBadge}>
                <Ionicons name="checkmark-circle" size={16} color="#15803d" />
                <Text style={styles.changeText}>
                  Tiền thừa: <Text style={{ fontWeight: '700' }}>{tienThua.toLocaleString('vi-VN')} đ</Text>
                </Text>
              </View>
            )}
          </View>
        )}

        {/* Phương thức: Chuyển khoản (Inline QR) */}
        {phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN && (
          <View style={styles.qrContainer}>
            <Text style={styles.qrTitle}>Quét mã QR để thanh toán</Text>
            {timeLeft > 0 ? (
              <View style={styles.qrContentBox}>
                <TouchableOpacity onPress={() => setShowLargeQr(true)} activeOpacity={0.85}>
                  <Image
                    source={{ uri: getSepayQrUrl() }}
                    style={styles.qrImage}
                    resizeMode="contain"
                  />
                </TouchableOpacity>
                <Text style={styles.qrTimerText}>
                  QR sẽ hết hạn sau: <Text style={{ fontWeight: '700', color: '#ef4444' }}>{formattedTimeLeft}</Text>
                </Text>
                <TouchableOpacity
                  style={styles.paidBtn}
                  onPress={xuLyThanhToanNgay}
                  disabled={dangThanhToan}
                >
                  <Text style={styles.paidBtnText}>
                    {dangThanhToan ? "Đang xử lý..." : "Đã thanh toán"}
                  </Text>
                </TouchableOpacity>
              </View>
            ) : (
              <View style={styles.qrExpiredBox}>
                <Ionicons name="alert-circle-outline" size={32} color="#ef4444" />
                <Text style={styles.qrExpiredText}>Mã QR đã hết hạn</Text>
                <TouchableOpacity style={styles.refreshQrBtn} onPress={startTimer}>
                  <Text style={styles.refreshQrBtnText}>Tạo lại QR</Text>
                </TouchableOpacity>
              </View>
            )}
          </View>
        )}

        {/* Phương thức: Kết hợp */}
        {phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.KET_HOP && (
          <View style={styles.combinedBox}>
            <View style={styles.combinedField}>
              <Text style={styles.combinedLabel}>Tiền mặt</Text>
              <TextInput
                style={styles.amountInput}
                value={tienMatKetHop}
                onChangeText={xuLyTienMatKetHopInput}
                keyboardType="numeric"
                placeholder="Nhập số tiền mặt..."
                placeholderTextColor="#94a3b8"
              />
            </View>

            <View style={styles.combinedField}>
              <Text style={styles.combinedLabel}>Chuyển khoản</Text>
              <View style={styles.transferInputWrapper}>
                <TextInput
                  style={[styles.amountInput, { paddingRight: 40 }]}
                  value={tienChuyenKhoanKetHop}
                  onChangeText={xuLyTienChuyenKhoanKetHopInput}
                  keyboardType="numeric"
                  placeholder="Nhập số tiền chuyển khoản..."
                  placeholderTextColor="#94a3b8"
                />
                <TouchableOpacity 
                  style={styles.qrIconBtn}
                  onPress={handleOpenCombinedQr}
                  title="Mở mã QR chuyển khoản"
                >
                  <Ionicons name="qr-code" size={20} color="#ef4444" />
                </TouchableOpacity>
              </View>
            </View>

            {!!thongBaoLoiThanhToan && (
              <Text style={styles.errorText}>{thongBaoLoiThanhToan}</Text>
            )}

            {tienThua > 0 && !thongBaoLoiThanhToan && (
              <View style={styles.changeBadge}>
                <Ionicons name="checkmark-circle" size={16} color="#15803d" />
                <Text style={styles.changeText}>
                  Tiền thừa: <Text style={{ fontWeight: '700' }}>{tienThua.toLocaleString('vi-VN')} đ</Text>
                </Text>
              </View>
            )}
          </View>
        )}
      </View>

      {/* Checkout Button */}
      <TouchableOpacity 
        style={[styles.checkoutButton, (cartItems.length === 0 || dangThanhToan) && styles.checkoutButtonDisabled]}
        disabled={cartItems.length === 0 || dangThanhToan}
        onPress={xuLyThanhToanNgay}
      >
        {dangThanhToan ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <View style={styles.checkoutBtnContent}>
            <Ionicons name="card-outline" size={20} color="#fff" />
            <Text style={styles.checkoutText}>Thanh Toán</Text>
          </View>
        )}
      </TouchableOpacity>

      {/* Modal QR phóng to */}
      <Modal
        visible={showLargeQr}
        transparent={true}
        animationType="fade"
        onRequestClose={() => setShowLargeQr(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalCard}>
            <TouchableOpacity 
              style={styles.modalCloseBtn}
              onPress={() => setShowLargeQr(false)}
            >
              <Ionicons name="close" size={24} color="#64748b" />
            </TouchableOpacity>

            <Text style={styles.modalTitle}>Quét mã QR để thanh toán</Text>

            {timeLeft > 0 ? (
              <View style={styles.modalContent}>
                <Image
                  source={{ uri: getSepayQrUrl() }}
                  style={styles.modalQrImage}
                  resizeMode="contain"
                />
                <Text style={styles.modalTimerText}>
                  QR sẽ hết hạn sau: <Text style={{ fontWeight: '700', color: '#ef4444' }}>{formattedTimeLeft}</Text>
                </Text>
                <TouchableOpacity
                  style={styles.modalPayBtn}
                  onPress={() => {
                    setShowLargeQr(false);
                    xuLyThanhToanNgay();
                  }}
                  disabled={dangThanhToan}
                >
                  <Text style={styles.modalPayBtnText}>
                    {dangThanhToan ? "Đang xử lý..." : "Đã thanh toán"}
                  </Text>
                </TouchableOpacity>
              </View>
            ) : (
              <View style={styles.modalExpiredBox}>
                <Ionicons name="alert-circle-outline" size={48} color="#ef4444" />
                <Text style={styles.modalExpiredText}>Mã QR đã hết hạn</Text>
                <TouchableOpacity style={styles.refreshQrBtn} onPress={startTimer}>
                  <Text style={styles.refreshQrBtnText}>Tạo lại QR</Text>
                </TouchableOpacity>
              </View>
            )}
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginTop: 6,
  },
  breakdownBox: {
    backgroundColor: '#f8fafc',
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: '#f1f5f9',
    gap: 8,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  totalRow: {
    marginTop: 4,
    paddingTop: 8,
    borderTopWidth: 1,
    borderTopColor: '#e2e8f0',
  },
  summaryLabel: {
    fontSize: 13,
    color: '#64748b',
  },
  summaryValue: {
    fontSize: 13,
    fontWeight: '600',
    color: '#334155',
  },
  discountValue: {
    fontSize: 13,
    fontWeight: '600',
    color: '#16a34a',
  },
  shippingValue: {
    fontSize: 13,
    fontWeight: '600',
    color: '#3b82f6',
  },
  summaryLabelBold: {
    fontSize: 15,
    fontWeight: '700',
    color: '#1e293b',
  },
  summaryValueBold: {
    fontSize: 18,
    fontWeight: '800',
    color: '#ef4444',
  },
  paymentSection: {
    marginTop: 14,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#f1f5f9',
  },
  paymentTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: '#475569',
    marginBottom: 8,
  },
  paymentMethodsRow: {
    flexDirection: 'row',
    gap: 6,
    marginBottom: 12,
  },
  paymentMethodBtn: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 4,
    paddingVertical: 9,
    paddingHorizontal: 6,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    backgroundColor: '#f8fafc',
  },
  paymentMethodBtnActive: {
    borderColor: '#fca5a5',
    backgroundColor: '#fef2f2',
  },
  paymentMethodText: {
    fontSize: 12,
    color: '#64748b',
    fontWeight: '600',
  },
  paymentMethodTextActive: {
    color: '#ef4444',
    fontWeight: '700',
  },
  amountInputContainer: {
    marginBottom: 6,
  },
  amountLabelRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
    flexWrap: 'wrap',
    gap: 6,
  },
  amountInputLabel: {
    fontSize: 13,
    fontWeight: '600',
    color: '#475569',
  },
  quickChipsRow: {
    flexDirection: 'row',
    gap: 4,
  },
  quickChip: {
    backgroundColor: '#eff6ff',
    borderWidth: 1,
    borderColor: '#bfdbfe',
    borderRadius: 6,
    paddingHorizontal: 6,
    paddingVertical: 2,
  },
  quickChipText: {
    fontSize: 11,
    fontWeight: '600',
    color: '#2563eb',
  },
  amountInput: {
    minWidth: 0,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 8,
    fontSize: 14,
    fontWeight: '600',
    color: '#0f172a',
    backgroundColor: '#fff',
  },
  amountInputError: {
    borderColor: '#ef4444',
    backgroundColor: '#fef2f2',
  },
  errorText: {
    color: '#ef4444',
    fontSize: 12,
    marginTop: 4,
  },
  changeBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    backgroundColor: '#f0fdf4',
    borderWidth: 1,
    borderColor: '#bbf7d0',
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 4,
    marginTop: 6,
  },
  changeText: {
    color: '#15803d',
    fontSize: 13,
  },
  qrContainer: {
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: '#e2e8f0',
    borderRadius: 12,
    padding: 12,
    alignItems: 'center',
    marginBottom: 6,
  },
  qrTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: '#1e293b',
    marginBottom: 8,
  },
  qrContentBox: {
    alignItems: 'center',
    width: '100%',
  },
  qrImage: {
    width: 170,
    height: 170,
    borderRadius: 8,
    backgroundColor: '#fff',
  },
  qrTimerText: {
    fontSize: 12,
    color: '#64748b',
    marginTop: 6,
    marginBottom: 8,
  },
  paidBtn: {
    backgroundColor: '#ef4444',
    width: '100%',
    paddingVertical: 10,
    borderRadius: 8,
    alignItems: 'center',
  },
  paidBtnText: {
    color: '#fff',
    fontSize: 13,
    fontWeight: '700',
  },
  qrExpiredBox: {
    paddingVertical: 16,
    alignItems: 'center',
  },
  qrExpiredText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#ef4444',
    marginTop: 4,
    marginBottom: 8,
  },
  refreshQrBtn: {
    backgroundColor: '#fee2e2',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 6,
  },
  refreshQrBtnText: {
    color: '#ef4444',
    fontWeight: '700',
    fontSize: 12,
  },
  combinedBox: {
    backgroundColor: '#f8fafc',
    borderWidth: 1,
    borderColor: '#e2e8f0',
    borderRadius: 10,
    padding: 10,
    gap: 10,
    marginBottom: 6,
  },
  combinedField: {
    gap: 4,
  },
  combinedLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: '#64748b',
  },
  transferInputWrapper: {
    position: 'relative',
    justifyContent: 'center',
  },
  qrIconBtn: {
    position: 'absolute',
    right: 8,
    padding: 4,
  },
  checkoutButton: {
    backgroundColor: '#ef4444',
    paddingVertical: 14,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 12,
    shadowColor: '#ef4444',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 6,
    elevation: 3,
  },
  checkoutBtnContent: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  checkoutText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '700',
  },
  checkoutButtonDisabled: {
    backgroundColor: '#fca5a5',
    shadowOpacity: 0,
    elevation: 0,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.65)',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 16,
  },
  modalCard: {
    backgroundColor: '#ffffff',
    borderRadius: 24,
    padding: 20,
    width: '100%',
    maxWidth: 380,
    alignItems: 'center',
    position: 'relative',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.25,
    shadowRadius: 20,
    elevation: 10,
  },
  modalCloseBtn: {
    position: 'absolute',
    top: 14,
    right: 14,
    padding: 6,
    borderRadius: 20,
    backgroundColor: '#f1f5f9',
    zIndex: 10,
  },
  modalTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#1e293b',
    marginTop: 6,
    marginBottom: 14,
  },
  modalContent: {
    alignItems: 'center',
    width: '100%',
  },
  modalQrImage: {
    width: 260,
    height: 260,
    borderRadius: 12,
    backgroundColor: '#fff',
  },
  modalTimerText: {
    fontSize: 13,
    color: '#64748b',
    marginTop: 12,
    marginBottom: 14,
  },
  modalPayBtn: {
    backgroundColor: '#ef4444',
    width: '100%',
    paddingVertical: 12,
    borderRadius: 12,
    alignItems: 'center',
    shadowColor: '#ef4444',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 6,
    elevation: 3,
  },
  modalPayBtnText: {
    color: '#fff',
    fontSize: 15,
    fontWeight: '700',
  },
  modalExpiredBox: {
    alignItems: 'center',
    paddingVertical: 30,
  },
  modalExpiredText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#ef4444',
    marginTop: 8,
    marginBottom: 12,
  },
});
