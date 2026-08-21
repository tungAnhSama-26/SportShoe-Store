import React from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

export default function MaGiamGia({ phieuGiamGiaLogic }) {
  const { 
    maPhieuGiamGia, 
    setMaPhieuGiamGia, 
    phieuGiamGiaDaApDung, 
    setPhieuGiamGiaDaApDung,
    dangApDungPhieu, 
    xuLyApDungPhieu 
  } = phieuGiamGiaLogic;

  return (
    <View style={styles.container}>
      <Text style={styles.sectionTitle}>Mã giảm giá</Text>
      <View style={styles.searchRow}>
        <TextInput
          style={styles.input}
          placeholder="Nhập mã..."
          placeholderTextColor="#94a3b8"
          value={maPhieuGiamGia}
          onChangeText={setMaPhieuGiamGia}
          autoCapitalize="characters"
          onSubmitEditing={() => xuLyApDungPhieu(true)}
        />
        <TouchableOpacity 
          style={[styles.applyBtn, (!maPhieuGiamGia.trim() || dangApDungPhieu) && styles.applyBtnDisabled]} 
          onPress={() => xuLyApDungPhieu(true)}
          disabled={!maPhieuGiamGia.trim() || dangApDungPhieu}
        >
          {dangApDungPhieu ? (
            <ActivityIndicator color="#fff" size="small" />
          ) : (
            <Text style={styles.applyBtnText}>Áp dụng</Text>
          )}
        </TouchableOpacity>
      </View>
      {phieuGiamGiaDaApDung && (
        <View style={styles.appliedCard}>
          <View style={styles.appliedLeft}>
            <Ionicons name="pricetag" size={16} color="#16a34a" />
            <Text style={styles.appliedCode}>
              {phieuGiamGiaDaApDung.ma}
              {phieuGiamGiaDaApDung.soTienGiam > 0 ? ` (-${phieuGiamGiaDaApDung.soTienGiam.toLocaleString('vi-VN')} đ)` : ''}
            </Text>
          </View>
          <TouchableOpacity 
            style={styles.removeBtn}
            onPress={() => {
              setPhieuGiamGiaDaApDung(null);
              setMaPhieuGiamGia("");
            }}
          >
            <Ionicons name="close-circle" size={18} color="#ef4444" />
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 12,
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '700',
    marginBottom: 8,
    color: '#334155',
  },
  searchRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  input: {
    flex: 1,
    minWidth: 0,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    backgroundColor: '#f8fafc',
    borderRadius: 8,
    paddingHorizontal: 12,
    height: 40,
    fontSize: 14,
    color: '#0f172a',
  },
  applyBtn: {
    backgroundColor: '#ef4444',
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 14,
    borderRadius: 8,
    height: 40,
    flexShrink: 0,
  },
  applyBtnDisabled: {
    backgroundColor: '#fca5a5',
  },
  applyBtnText: {
    color: '#fff',
    fontWeight: '700',
    fontSize: 13,
  },
  appliedCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#f0fdf4',
    borderWidth: 1,
    borderColor: '#bbf7d0',
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 6,
    marginTop: 8,
  },
  appliedLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    flex: 1,
  },
  appliedCode: {
    color: '#15803d',
    fontWeight: '700',
    fontSize: 13,
  },
  removeBtn: {
    padding: 2,
  },
});
