import React from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native';

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
          placeholder="Nhập mã giảm giá..."
          value={maPhieuGiamGia}
          onChangeText={setMaPhieuGiamGia}
          autoCapitalize="characters"
          onSubmitEditing={() => xuLyApDungPhieu(true)}
        />
        <TouchableOpacity style={styles.applyBtn} onPress={() => xuLyApDungPhieu(true)}>
          {dangApDungPhieu ? (
            <ActivityIndicator color="#fff" size="small" />
          ) : (
            <Text style={{ color: '#fff', fontWeight: 'bold' }}>Áp dụng</Text>
          )}
        </TouchableOpacity>
      </View>
      {phieuGiamGiaDaApDung && (
        <View style={styles.infoRow}>
          <Text style={{ color: '#4CAF50', fontWeight: 'bold' }}>Mã: {phieuGiamGiaDaApDung.ma}</Text>
          <TouchableOpacity onPress={() => {
            setPhieuGiamGiaDaApDung(null);
            setMaPhieuGiamGia("");
          }}>
            <Text style={styles.clearText}>Hủy</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 10,
  },
  sectionTitle: {
    fontSize: 15,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#1e293b',
  },
  searchRow: {
    flexDirection: 'row',
    marginBottom: 10,
  },
  input: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    backgroundColor: '#f8fafc',
    borderRadius: 6,
    paddingHorizontal: 10,
    height: 40,
    marginRight: 8,
  },
  applyBtn: {
    backgroundColor: '#ef4444',
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 15,
    borderRadius: 6,
    height: 40,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 5,
  },
  clearText: {
    color: '#ef4444',
    textDecorationLine: 'underline',
    fontSize: 13,
  },
});
