import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import PhanSanPham from './PhanSanPham';
import BangGioHang from './BangGioHang';
import PhanGiaoHang from './PhanGiaoHang';
import { suDungBanHang } from '../ngu-canh/NguCanhBanHang';

export default function CotTrai() {
  const { gioHangLogic } = suDungBanHang();
  const tongSoSanPham = gioHangLogic?.tongSoLuong || 0;

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <View style={styles.headerTitleRow}>
          <Text style={styles.title}>Giỏ hàng</Text>
          <View style={styles.badge}>
            <Text style={styles.badgeText}>{tongSoSanPham} sản phẩm</Text>
          </View>
        </View>

        <PhanSanPham />
      </View>

      <BangGioHang />
      <PhanGiaoHang />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#ffffff',
    borderRadius: 24,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: 'rgba(226, 232, 240, 0.6)',
    shadowColor: '#0f172a',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 6,
    elevation: 2,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    backgroundColor: 'rgba(248, 250, 252, 0.5)',
    borderBottomWidth: 1,
    borderBottomColor: '#f1f5f9',
    zIndex: 10,
  },
  headerTitleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  title: {
    fontSize: 18,
    fontWeight: '700',
    color: '#1e293b',
  },
  badge: {
    backgroundColor: '#fef2f2',
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 16,
  },
  badgeText: {
    color: '#dc2626',
    fontSize: 12,
    fontWeight: '700',
  },
});
