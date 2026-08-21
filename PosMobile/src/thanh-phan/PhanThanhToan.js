import React from 'react';
import { View, Text, TouchableOpacity, TextInput, StyleSheet, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { suDungBanHang } from '../ngu-canh/NguCanhBanHang';
import { PHUONG_THUC_THANH_TOAN } from '../features/ban-hang-tai-quay/Enum';
import { dinhDangSo } from '../features/ban-hang-tai-quay/TienTe';

export default function PhanThanhToan() {
  const { 
    gioHangLogic, 
    phieuGiamGiaLogic, 
    giaoHangLogic,
    thanhToanLogic,
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
    thongBaoLoiThanhToan,
    tienThua
  } = thanhToanLogic;

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
          <Text style={styles.summaryLabel}>Tổng tiền hàng:</Text>
          <Text style={styles.summaryValue}>{tongTien.toLocaleString('vi-VN')} đ</Text>
        </View>

        {tienGiam > 0 && (
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Giảm giá:</Text>
            <Text style={styles.discountValue}>-{tienGiam.toLocaleString('vi-VN')} đ</Text>
          </View>
        )}

        {phiGiaoHang > 0 && (
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Phí giao hàng:</Text>
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
          <TouchableOpacity 
            style={[styles.paymentMethodBtn, phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT && styles.paymentMethodBtnActive]}
            onPress={() => setPhuongThucThanhToan(PHUONG_THUC_THANH_TOAN.TIEN_MAT)}
          >
            <Ionicons 
              name="cash-outline" 
              size={18} 
              color={phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT ? "#ef4444" : "#64748b"} 
            />
            <Text style={[styles.paymentMethodText, phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT && styles.paymentMethodTextActive]}>
              Tiền mặt
            </Text>
          </TouchableOpacity>
          <TouchableOpacity 
            style={[styles.paymentMethodBtn, phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN && styles.paymentMethodBtnActive]}
            onPress={() => setPhuongThucThanhToan(PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN)}
          >
            <Ionicons 
              name="qr-code-outline" 
              size={18} 
              color={phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN ? "#ef4444" : "#64748b"} 
            />
            <Text style={[styles.paymentMethodText, phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN && styles.paymentMethodTextActive]}>
              Chuyển khoản
            </Text>
          </TouchableOpacity>
        </View>

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
    gap: 10,
    marginBottom: 12,
  },
  paymentMethodBtn: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
    paddingVertical: 10,
    paddingHorizontal: 12,
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
    fontSize: 13,
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
    fontSize: 15,
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
});
