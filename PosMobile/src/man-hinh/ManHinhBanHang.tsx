import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import Toast from 'react-native-toast-message';
import CotTrai from '../thanh-phan/CotTrai';
import CotPhai from '../thanh-phan/CotPhai';
import PhanHoaDonCho from '../thanh-phan/PhanHoaDonCho';
import { ProviderBanHang, suDungBanHang } from '../ngu-canh/NguCanhBanHang';

function ManHinhBanHangContent() {
  const {
    danhSachHoaDonCho,
    hoaDonChoDaChon,
    dangTaiHoaDonCho,
    chonHoaDonCho,
    xuLyTaoHoaDonCho,
  } = suDungBanHang();

  return (
    <View style={styles.container}>
      {/* Khung iPad */}
      <View style={styles.ipadFrame}>
        {/* Nút cứng của iPad */}
        <View style={[styles.hardwareButton, styles.volumeUp]} />
        <View style={[styles.hardwareButton, styles.volumeDown]} />
        <View style={[styles.hardwareButton, styles.powerButton]} />
        
        {/* Camera trước */}
        <View style={styles.cameraContainer}>
          <View style={styles.camera} />
        </View>

        {/* Màn hình hiển thị bên trong */}
        <View style={styles.ipadScreen}>

          {/* Khu vực nội dung */}
          <View style={styles.contentArea}>
            {/* Phần trên: Hóa đơn chờ */}
            <View style={styles.topSection}>
              <PhanHoaDonCho 
                danhSachHoaDonCho={danhSachHoaDonCho} 
                hoaDonChoDaChon={hoaDonChoDaChon}
                chonHoaDonCho={chonHoaDonCho}
                xuLyTaoHoaDonChoMoi={xuLyTaoHoaDonCho}
                dangTaiHoaDonCho={dangTaiHoaDonCho}
                maxPendingInvoices={5}
              />
            </View>

            {/* Lưới chính: Chia 2 cột */}
            <View style={styles.mainGrid}>
              <View style={styles.leftColumn}>
                <CotTrai />
              </View>
              <View style={styles.rightColumn}>
                <CotPhai />
              </View>
            </View>
          </View>
        </View>

        {/* Thanh Home Indicator dưới đáy */}
        <View style={styles.homeIndicator} />
      </View>
    </View>
  );
}

export default function ManHinhBanHang() {
  return (
    <ProviderBanHang>
      <ManHinhBanHangContent />
    </ProviderBanHang>
  );
}


const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'transparent',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
  },
  ipadFrame: {
    width: '100%',
    maxWidth: 1280,
    height: '100%',
    maxHeight: 900,
    backgroundColor: '#1c1c1e',
    borderRadius: 48,
    padding: 16,
    position: 'relative',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 24 },
    shadowOpacity: 0.2,
    shadowRadius: 80,
    elevation: 20,
    borderWidth: 2,
    borderColor: '#1e293b',
  },
  hardwareButton: {
    position: 'absolute',
    backgroundColor: '#1e293b',
  },
  volumeUp: {
    left: -4,
    top: 120,
    width: 4,
    height: 50,
    borderTopLeftRadius: 6,
    borderBottomLeftRadius: 6,
  },
  volumeDown: {
    left: -4,
    top: 190,
    width: 4,
    height: 50,
    borderTopLeftRadius: 6,
    borderBottomLeftRadius: 6,
  },
  powerButton: {
    right: 100,
    top: -4,
    width: 50,
    height: 4,
    borderTopLeftRadius: 6,
    borderTopRightRadius: 6,
  },
  cameraContainer: {
    position: 'absolute',
    left: '50%',
    top: 6,
    transform: [{ translateX: -5 }],
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 10,
    display: 'flex',
  },
  camera: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: '#000',
    borderWidth: 1,
    borderColor: '#1e293b',
  },
  homeIndicator: {
    position: 'absolute',
    bottom: 8,
    left: '50%',
    transform: [{ translateX: -64 }],
    width: 128,
    height: 6,
    backgroundColor: 'rgba(71, 85, 105, 0.5)',
    borderRadius: 3,
    zIndex: 10,
  },
  ipadScreen: {
    flex: 1,
    backgroundColor: '#f4f4f9',
    borderRadius: 32,
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column',
  },
  headerContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 24,
    paddingVertical: 12,
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(226, 232, 240, 0.6)',
    zIndex: 10,
  },
  headerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 16,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: '800',
    color: '#1e293b',
    letterSpacing: -0.5,
  },
  headerSubtitle: {
    fontSize: 14,
    fontWeight: '500',
    color: '#64748b',
    marginTop: 4,
  },
  headerRight: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 16,
  },
  posBadge: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: '#eef2ff',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#e0e7ff',
  },
  posBadgeText: {
    color: '#4f46e5',
    fontWeight: 'bold',
  },
  contentArea: {
    flex: 1,
    padding: 24,
    display: 'flex',
    flexDirection: 'column',
  },
  topSection: {
    marginBottom: 16,
  },
  mainGrid: {
    flex: 1,
    flexDirection: 'row',
    gap: 16,
    minHeight: 0,
  },
  leftColumn: {
    flex: 2,
    display: 'flex',
  },
  rightColumn: {
    flex: 1,
    display: 'flex',
  }
});
