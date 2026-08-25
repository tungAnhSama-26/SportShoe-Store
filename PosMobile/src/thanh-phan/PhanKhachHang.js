import React from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, ScrollView } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

export default function PhanKhachHang({ khachHangLogic }) {
  const {
    tuKhoaKhachHang, setTuKhoaKhachHang,
    khachHangDuocChon, setKhachHangDuocChon,
    ketQuaTimKiemKhachHang,
    dangTaiKhachHang, timKiemKhachHang,
    chonKhachHang, chonKhachVangLai,
    hienThiDanhSachKhachHang,
    laKhachVangLai,
    tenKhachHangHienThi,
    soDienThoaiKhachHangHienThi
  } = khachHangLogic;

  return (
    <View style={styles.container}>
      {!khachHangDuocChon ? (
        <View style={styles.searchSection}>
          <View style={styles.searchRow}>
            <View style={styles.searchIconContainer}>
              <Ionicons name="search" size={18} color="#94a3b8" />
            </View>
            <TextInput
              style={styles.input}
              placeholder="Tìm khách hàng (SĐT, Tên)..."
              placeholderTextColor="#94a3b8"
              value={tuKhoaKhachHang}
              onChangeText={setTuKhoaKhachHang}
              keyboardType="default"
              onSubmitEditing={() => timKiemKhachHang(tuKhoaKhachHang)}
            />
            {dangTaiKhachHang ? (
              <View style={styles.loadingWrapper}>
                <ActivityIndicator color="#ef4444" size="small" />
              </View>
            ) : (
              tuKhoaKhachHang.trim().length > 0 && (
                <TouchableOpacity style={styles.loadingWrapper} onPress={() => setTuKhoaKhachHang("")}>
                  <Ionicons name="close-circle" size={18} color="#94a3b8" />
                </TouchableOpacity>
              )
            )}
          </View>

          {/* Dropdown search results */}
          {hienThiDanhSachKhachHang && ketQuaTimKiemKhachHang.length > 0 && (
            <View style={styles.dropdownResults}>
              <ScrollView style={styles.dropdownScroll} nestedScrollEnabled keyboardShouldPersistTaps="handled">
                {ketQuaTimKiemKhachHang.map((customer) => (
                  <TouchableOpacity
                    key={customer.id || customer.sdt}
                    style={styles.dropdownItem}
                    onPress={() => chonKhachHang(customer)}
                  >
                    <View style={styles.dropdownItemAvatar}>
                      <Ionicons name="person" size={14} color="#4f46e5" />
                    </View>
                    <View style={styles.dropdownItemInfo}>
                      <Text style={styles.dropdownItemName}>{customer.hoTen || "Khách hàng"}</Text>
                      <Text style={styles.dropdownItemPhone}>{customer.sdt || "Không có SĐT"}</Text>
                    </View>
                  </TouchableOpacity>
                ))}
              </ScrollView>
            </View>
          )}

          {/* Quick select guest */}
          {!laKhachVangLai && (
            <View style={styles.quickGuestRow}>
              <TouchableOpacity style={styles.guestBtn} onPress={chonKhachVangLai}>
                <Ionicons name="person-outline" size={14} color="#64748b" />
                <Text style={styles.guestBtnText}>Chọn khách lẻ</Text>
              </TouchableOpacity>
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
          <TouchableOpacity 
            style={styles.clearButton} 
            onPress={() => {
              setKhachHangDuocChon(null);
              setTuKhoaKhachHang("");
            }}
          >
            <Ionicons name="close-circle" size={22} color="#94a3b8" />
          </TouchableOpacity>
        </View>
      )}

      {/* Guest display */}
      {!khachHangDuocChon && laKhachVangLai && (
        <View style={styles.guestContainer}>
          <View style={styles.guestAvatar}>
            <Ionicons name="person-outline" size={16} color="#64748b" />
          </View>
          <Text style={styles.guestText}>Khách lẻ (Khách vãng lai)</Text>
          <TouchableOpacity 
            style={styles.clearButton} 
            onPress={() => setTuKhoaKhachHang("")}
          >
            <Ionicons name="close-circle" size={18} color="#94a3b8" />
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 0,
    gap: 8,
  },
  searchSection: {
    position: 'relative',
    gap: 6,
  },
  searchRow: {
    flexDirection: 'row',
    position: 'relative',
    alignItems: 'center',
  },
  searchIconContainer: {
    position: 'absolute',
    left: 10,
    zIndex: 1,
    height: '100%',
    justifyContent: 'center',
  },
  input: {
    flex: 1,
    minWidth: 0,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    backgroundColor: '#f8fafc',
    borderRadius: 8,
    paddingLeft: 34,
    paddingRight: 34,
    height: 40,
    fontSize: 13,
    color: '#0f172a',
  },
  loadingWrapper: {
    position: 'absolute',
    right: 10,
    zIndex: 1,
  },
  dropdownResults: {
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#e2e8f0',
    borderRadius: 8,
    maxHeight: 160,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 6,
    elevation: 4,
    zIndex: 20,
  },
  dropdownScroll: {
    padding: 4,
  },
  dropdownItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 8,
    paddingHorizontal: 8,
    borderRadius: 6,
    gap: 8,
  },
  dropdownItemAvatar: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: '#e0e7ff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  dropdownItemInfo: {
    flex: 1,
  },
  dropdownItemName: {
    fontSize: 13,
    fontWeight: '700',
    color: '#1e293b',
  },
  dropdownItemPhone: {
    fontSize: 11,
    color: '#64748b',
  },
  quickGuestRow: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginTop: 2,
  },
  guestBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
    backgroundColor: '#f1f5f9',
  },
  guestBtnText: {
    fontSize: 11,
    fontWeight: '600',
    color: '#64748b',
  },
  selectedCustomerCard: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#eef2ff',
    borderWidth: 1,
    borderColor: '#e0e7ff',
    borderRadius: 12,
    padding: 10,
  },
  customerInfoContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    flex: 1,
  },
  avatarWrapper: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#e0e7ff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  customerTextContainer: {
    justifyContent: 'center',
    flex: 1,
  },
  customerName: {
    fontSize: 14,
    fontWeight: '700',
    color: '#1e293b',
  },
  customerPhone: {
    fontSize: 12,
    color: '#64748b',
    marginTop: 1,
  },
  clearButton: {
    padding: 4,
  },
  guestContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#f8fafc',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  guestAvatar: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: '#e2e8f0',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  guestText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#475569',
    flex: 1,
  },
});
