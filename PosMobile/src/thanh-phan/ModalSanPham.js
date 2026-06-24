import React from 'react';
import { View, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, Text, ScrollView, Modal, Platform } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

export default function ModalSanPham({ 
  hienThiDanhSachSanPham,
  dongDanhSachSanPham,
  tuKhoaSanPham,
  setTuKhoaSanPham,
  xuLyTimKiem,
  dangTaiSanPham,
  ketQuaBienTheSanPham,
  xuLyThem
}) {
  return (
    <Modal
      visible={hienThiDanhSachSanPham}
      animationType="slide"
      transparent={true}
      onRequestClose={dongDanhSachSanPham}
    >
      <View style={styles.modalBackground}>
        <View style={styles.modalContainer}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Chọn sản phẩm</Text>
            <TouchableOpacity onPress={dongDanhSachSanPham}>
              <Ionicons name="close" size={24} color="#64748b" />
            </TouchableOpacity>
          </View>

          <View style={styles.searchContainer}>
            <TextInput 
              style={styles.searchInput}
              placeholder="Tìm tên sản phẩm, mã SKU..."
              value={tuKhoaSanPham}
              onChangeText={setTuKhoaSanPham}
              onSubmitEditing={xuLyTimKiem}
            />
            <TouchableOpacity style={styles.searchButton} onPress={xuLyTimKiem}>
              <Ionicons name="search" size={20} color="#fff" />
            </TouchableOpacity>
          </View>

          <View style={styles.listContainer}>
            {dangTaiSanPham ? (
              <ActivityIndicator size="large" color="#3b82f6" style={{ marginTop: 20 }} />
            ) : (
              <ScrollView>
                {ketQuaBienTheSanPham.length > 0 ? ketQuaBienTheSanPham.map(item => (
                  <View key={item.chiTietId} style={styles.productItem}>
                    <View style={{ flex: 1 }}>
                      <Text style={styles.productName}>{item.tenSanPham}</Text>
                      <Text style={styles.productVariant}>{item.mauSac} - {item.kichCo}</Text>
                      <Text style={styles.productQty}>Tồn kho: {item.soLuongTonKhaDung || item.soLuongTon || 0}</Text>
                    </View>
                    <View style={{ alignItems: 'flex-end', justifyContent: 'center' }}>
                      <Text style={styles.productPrice}>{(item.giaBan || 0).toLocaleString('vi-VN')} đ</Text>
                      <TouchableOpacity style={styles.btnAdd} onPress={() => xuLyThem(item)}>
                        <Text style={styles.txtBtnAdd}>Thêm</Text>
                      </TouchableOpacity>
                    </View>
                  </View>
                )) : (
                  <Text style={{ textAlign: 'center', marginTop: 20, color: '#64748b' }}>
                    Không có sản phẩm nào
                  </Text>
                )}
              </ScrollView>
            )}
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  modalBackground: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
    ...Platform.select({
      web: { padding: 40 },
      default: { padding: 20 }
    })
  },
  modalContainer: {
    backgroundColor: '#fff',
    width: '100%',
    maxWidth: 900,
    height: '90%',
    borderRadius: 16,
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column',
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#f1f5f9',
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1e293b',
  },
  searchContainer: {
    flexDirection: 'row',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#f1f5f9',
    gap: 8,
  },
  searchInput: {
    flex: 1,
    backgroundColor: '#f8fafc',
    borderWidth: 1,
    borderColor: '#e2e8f0',
    borderRadius: 8,
    paddingHorizontal: 16,
    paddingVertical: 10,
  },
  searchButton: {
    backgroundColor: '#ef4444',
    justifyContent: 'center',
    paddingHorizontal: 20,
    borderRadius: 8,
  },
  listContainer: {
    flex: 1,
    backgroundColor: '#f8fafc',
    padding: 16,
  },
  productItem: {
    flexDirection: 'row',
    backgroundColor: '#fff',
    padding: 16,
    borderRadius: 12,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  productName: {
    fontSize: 15,
    fontWeight: 'bold',
    color: '#1e293b',
  },
  productVariant: {
    fontSize: 13,
    color: '#64748b',
    marginTop: 4,
  },
  productQty: {
    fontSize: 13,
    color: '#10b981',
    marginTop: 4,
  },
  productPrice: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ef4444',
    marginBottom: 8,
  },
  btnAdd: {
    backgroundColor: '#fef2f2',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#fecaca',
  },
  txtBtnAdd: {
    color: '#dc2626',
    fontWeight: 'bold',
  }
});
