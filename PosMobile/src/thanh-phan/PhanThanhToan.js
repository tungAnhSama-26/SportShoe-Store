import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native';
import { suDungBanHang } from '../ngu-canh/NguCanhBanHang';

export default function PhanThanhToan() {
  const { 
    gioHangLogic, 
    phieuGiamGiaLogic, 
    khachCanTra, 
    dangThanhToan, 
    xuLyThanhToanNgay 
  } = suDungBanHang();

  const { cartItems, tongTien } = gioHangLogic;
  const { phieuGiamGiaDaApDung } = phieuGiamGiaLogic;
  const tienGiam = phieuGiamGiaDaApDung ? phieuGiamGiaDaApDung.soTienGiam : 0;

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
