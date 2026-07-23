import React from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

export default function PhanKhachHang({ khachHangLogic }) {
  const {
    tuKhoaKhachHang, setTuKhoaKhachHang,
    khachHangDuocChon, setKhachHangDuocChon,
    dangTaiKhachHang, timKiemKhachHang,
    laKhachVangLai,
    tenKhachHangHienThi,
    soDienThoaiKhachHangHienThi
  } = khachHangLogic;

  return (
    <View style={styles.container}>
      {!khachHangDuocChon ? (
        <View style={styles.searchRow}>
          <View style={styles.searchIconContainer}>
            <Ionicons name="search" size={18} color="#94a3b8" />
          </View>
          <TextInput
            style={styles.input}
            placeholder="Tìm kiếm khách hàng (SĐT, Tên)"
            placeholderTextColor="#94a3b8"
            value={tuKhoaKhachHang}
            onChangeText={setTuKhoaKhachHang}
            keyboardType="default"
            onSubmitEditing={() => timKiemKhachHang()}
          />
          {dangTaiKhachHang && (
            <View style={styles.loadingWrapper}>
              <ActivityIndicator color="#64748b" size="small" />
            </View>
          )}
        </View>
      ) : (
        <View style={styles.selectedCustomerCard}>
          <View style={styles.customerInfoContainer}>
            <View style={styles.avatarWrapper}>
              <Ionicons name="person" size={20} color="#4f46e5" />
            </View>
            <View style={styles.customerTextContainer}>
              <Text style={styles.customerName}>{tenKhachHangHienThi}</Text>
              <Text style={styles.customerPhone}>{soDienThoaiKhachHangHienThi}</Text>
            </View>
          </View>
          <TouchableOpacity style={styles.clearButton} onPress={() => setKhachHangDuocChon(null)}>
            <Ionicons name="close-circle" size={24} color="#94a3b8" />
          </TouchableOpacity>
        </View>
      )}

      {/* Guest fallback if not selected */}
      {!khachHangDuocChon && laKhachVangLai && (
        <View style={styles.guestContainer}>
          <View style={styles.guestAvatar}>
            <Ionicons name="person-outline" size={16} color="#64748b" />
          </View>
          <Text style={styles.guestText}>Khách lẻ</Text>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 0,
    gap: 12,
  },
  searchRow: {
    flexDirection: 'row',
    position: 'relative',
    alignItems: 'center',
  },
  searchIconContainer: {
    position: 'absolute',
    left: 12,
    zIndex: 1,
    height: '100%',
    justifyContent: 'center',
  },
  input: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    backgroundColor: '#f8fafc',
    borderRadius: 8,
    paddingLeft: 36,
    paddingRight: 16,
    paddingVertical: 10,
    fontSize: 14,
    color: '#0f172a',
  },
  loadingWrapper: {
    position: 'absolute',
    right: 12,
  },
  selectedCustomerCard: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#eef2ff',
    borderWidth: 1,
    borderColor: '#e0e7ff',
    borderRadius: 12,
    padding: 12,
  },
  customerInfoContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  avatarWrapper: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#e0e7ff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  customerTextContainer: {
    justifyContent: 'center',
  },
  customerName: {
    fontSize: 15,
    fontWeight: '700',
    color: '#1e293b',
  },
  customerPhone: {
    fontSize: 13,
    color: '#64748b',
    marginTop: 2,
  },
  clearButton: {
    padding: 4,
  },
  guestContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingHorizontal: 4,
  },
  guestAvatar: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: '#f1f5f9',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  guestText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#64748b',
  },
});
