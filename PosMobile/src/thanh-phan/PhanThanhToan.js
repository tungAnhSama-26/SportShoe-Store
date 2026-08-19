import React, { useState } from 'react';
import { View, Text, TouchableOpacity, TextInput, StyleSheet, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { suDungBanHang } from '../ngu-canh/NguCanhBanHang';
import { PHUONG_THUC_THANH_TOAN } from '../features/ban-hang-tai-quay/Enum';

export default function PhanThanhToan() {
  const { 
    gioHangLogic, 
    phieuGiamGiaLogic, 
    thanhToanLogic,
    khachCanTra, 
    dangThanhToan, 
    xuLyThanhToanNgay,
    coTheThanhToan
  } = suDungBanHang();

  const { cartItems, tongTien } = gioHangLogic;
  const { phieuGiamGiaDaApDung } = phieuGiamGiaLogic;
  const tienGiam = phieuGiamGiaDaApDung ? phieuGiamGiaDaApDung.soTienGiam : 0;
  
  const {
    phuongThucThanhToan,
    setPhuongThucThanhToan,
    tienKhachDua,
    xuLyTienKhachDuaInput,
    thongBaoLoiThanhToan,
    tienThua
  } = thanhToanLogic;

  return (
    <View style={styles.container}>
      <View style={styles.summaryRow}>
        <Text style={styles.summaryLabel}>Tổng tiền hàng:</Text>
        <Text style={styles.summaryValue}>{tongTien.toLocaleString('vi-VN')} đ</Text>
      </View>
      <View style={styles.summaryRow}>
        <Text style={styles.summaryLabel}>Giảm giá:</Text>
        <Text style={styles.summaryValue}>-{tienGiam.toLocaleString('vi-VN')} đ</Text>
      </View>
      <View style={styles.summaryRow}>
        <Text style={styles.summaryLabelBold}>Khách cần trả:</Text>
        <Text style={styles.summaryValueBold}>{khachCanTra.toLocaleString('vi-VN')} đ</Text>
      </View>

      <View style={styles.paymentSection}>
        <Text style={styles.paymentTitle}>Hình thức thanh toán</Text>
        <View style={styles.paymentMethodsRow}>
          <TouchableOpacity 
            style={[styles.paymentMethodBtn, phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT && styles.paymentMethodBtnActive]}
            onPress={() => setPhuongThucThanhToan(PHUONG_THUC_THANH_TOAN.TIEN_MAT)}
          >
            <Ionicons name={phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT ? "radio-button-on" : "radio-button-off"} size={18} color={phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT ? "#ef4444" : "#64748b"} />
            <Text style={[styles.paymentMethodText, phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT && styles.paymentMethodTextActive]}>Tiền mặt</Text>
          </TouchableOpacity>
          <TouchableOpacity 
            style={[styles.paymentMethodBtn, phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN && styles.paymentMethodBtnActive]}
            onPress={() => setPhuongThucThanhToan(PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN)}
          >
            <Ionicons name={phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN ? "radio-button-on" : "radio-button-off"} size={18} color={phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN ? "#ef4444" : "#64748b"} />
            <Text style={[styles.paymentMethodText, phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN && styles.paymentMethodTextActive]}>Chuyển khoản</Text>
          </TouchableOpacity>
        </View>

        {phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT && (
          <View style={styles.amountInputContainer}>
            <Text style={styles.amountInputLabel}>Số tiền khách đưa</Text>
            <TextInput
              style={[styles.amountInput, thongBaoLoiThanhToan ? styles.amountInputError : null]}
              value={tienKhachDua}
              onChangeText={xuLyTienKhachDuaInput}
              keyboardType="numeric"
              placeholder="Nhập số tiền..."
            />
            {!!thongBaoLoiThanhToan && (
              <Text style={styles.errorText}>{thongBaoLoiThanhToan}</Text>
            )}
            {tienThua > 0 && !thongBaoLoiThanhToan && (
              <Text style={styles.changeText}>Tiền thừa: <Text style={{fontWeight: 'bold'}}>{tienThua.toLocaleString('vi-VN')} đ</Text></Text>
            )}
          </View>
        )}
      </View>

      <TouchableOpacity 
        style={[styles.checkoutButton, (cartItems.length === 0 || dangThanhToan) && styles.checkoutButtonDisabled]}
        disabled={cartItems.length === 0 || dangThanhToan}
        onPress={xuLyThanhToanNgay}
      >
        {dangThanhToan ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Text style={styles.checkoutText}>Thanh Toán</Text>
        )}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginTop: 10,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 10,
  },
  summaryLabel: {
    fontSize: 14,
    color: '#64748b',
  },
  summaryValue: {
    fontSize: 14,
    fontWeight: '500',
    color: '#334155',
  },
  summaryLabelBold: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1e293b',
  },
  summaryValueBold: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ef4444',
  },
  paymentSection: {
    marginTop: 15,
    borderTopWidth: 1,
    borderTopColor: '#f1f5f9',
    paddingTop: 15,
  },
  paymentTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#475569',
    marginBottom: 10,
  },
  paymentMethodsRow: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 15,
  },
  paymentMethodBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingVertical: 8,
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
    fontSize: 14,
    color: '#64748b',
    fontWeight: '500',
  },
  paymentMethodTextActive: {
    color: '#ef4444',
  },
  amountInputContainer: {
    marginBottom: 10,
  },
  amountInputLabel: {
    fontSize: 13,
    color: '#64748b',
    marginBottom: 6,
  },
  amountInput: {
    borderWidth: 1,
    borderColor: '#e2e8f0',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 15,
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
    marginTop: 6,
  },
  changeText: {
    color: '#10b981',
    fontSize: 13,
    marginTop: 6,
  },
  checkoutButton: {
    backgroundColor: '#ef4444',
    paddingVertical: 15,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 15,
    shadowColor: '#ef4444',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 6,
    elevation: 4,
  },
  checkoutText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  checkoutButtonDisabled: {
    backgroundColor: '#fecaca',
    shadowOpacity: 0,
    elevation: 0,
  },
});
