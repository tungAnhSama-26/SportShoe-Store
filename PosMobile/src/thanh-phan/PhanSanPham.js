import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { suDungBanHang } from '../ngu-canh/NguCanhBanHang';
import ModalSanPham from './ModalSanPham';
import ModalQuetQR from './ModalQuetQR';

export default function PhanSanPham() {
  const { sanPhamLogic } = suDungBanHang();
  const {
    tuKhoaSanPham, setTuKhoaSanPham,
    dangTaiSanPham,
    ketQuaBienTheSanPham,
    hienThiDanhSachSanPham,
    moDanhSachSanPham,
    dongDanhSachSanPham,
    themTrucTiepBienThe,
    taiSanPham
  } = sanPhamLogic;

  const [hienThiQuetQR, setHienThiQuetQR] = React.useState(false);

  const xuLyTimKiem = () => {
    taiSanPham(tuKhoaSanPham);
  };

  const xuLyThem = (sanPham) => {
    themTrucTiepBienThe(sanPham);
    dongDanhSachSanPham();
  };

  return (
    <View style={styles.container}>
      <TouchableOpacity style={styles.btnChonSanPham} onPress={moDanhSachSanPham}>
        <Ionicons name="add" size={20} color="#dc2626" />
        <Text style={styles.txtBtnChon}>Chọn sản phẩm</Text>
      </TouchableOpacity>
      
      <TouchableOpacity style={styles.btnScan} onPress={() => setHienThiQuetQR(true)}>
        <Ionicons name="qr-code-outline" size={20} color="#dc2626" />
      </TouchableOpacity>

      <ModalQuetQR 
        visible={hienThiQuetQR} 
        onClose={() => setHienThiQuetQR(false)} 
      />

      <ModalSanPham 
        hienThiDanhSachSanPham={hienThiDanhSachSanPham}
        dongDanhSachSanPham={dongDanhSachSanPham}
        tuKhoaSanPham={tuKhoaSanPham}
        setTuKhoaSanPham={setTuKhoaSanPham}
        xuLyTimKiem={xuLyTimKiem}
        dangTaiSanPham={dangTaiSanPham}
        ketQuaBienTheSanPham={ketQuaBienTheSanPham}
        xuLyThem={xuLyThem}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  btnChonSanPham: {
    backgroundColor: '#fef2f2',
    borderWidth: 1,
    borderColor: '#fecaca',
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 8,
    gap: 4,
  },
  txtBtnChon: {
    color: '#dc2626',
    fontWeight: 'bold',
    fontSize: 14,
  },
  btnScan: {
    backgroundColor: '#ffffff',
    padding: 10,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#e2e8f0',
  }
});
