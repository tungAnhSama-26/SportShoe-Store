import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import PhanKhachHang from './PhanKhachHang';
import MaGiamGia from './MaGiamGia';
import PhanThanhToan from './PhanThanhToan';
import { suDungBanHang } from '../ngu-canh/NguCanhBanHang';

export default function CotPhai() {
  const {
    khachHangLogic,
    phieuGiamGiaLogic,
    thanhToanLogic
  } = suDungBanHang();

  return (
    <View style={styles.container}>
      <View style={styles.card}>
        <Text style={styles.headerText}>Khách hàng</Text>
        <PhanKhachHang 
          khachHangLogic={khachHangLogic} 
        />
      </View>

      <View style={styles.card}>
        <View style={styles.header}>
          <Text style={styles.headerText}>Thông tin đơn hàng</Text>
        </View>
        <MaGiamGia 
          phieuGiamGiaLogic={phieuGiamGiaLogic} 
        />
        <PhanThanhToan 
          thanhToanLogic={thanhToanLogic}
          khachHangLogic={khachHangLogic}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    gap: 16,
    paddingRight: 4,
  },
  card: {
    backgroundColor: '#ffffff',
    borderRadius: 24,
    padding: 16,
    borderWidth: 1,
    borderColor: 'rgba(226, 232, 240, 0.6)',
    shadowColor: '#0f172a',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 6,
    elevation: 2,
  },
  header: {
    marginBottom: 12,
  },
  headerText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#1e293b',
    marginBottom: 12,
  },
});
