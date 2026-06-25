import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Modal, Platform } from 'react-native';
import { CameraView, useCameraPermissions } from 'expo-camera';
import { Ionicons } from '@expo/vector-icons';
import { suDungBanHang } from '../ngu-canh/NguCanhBanHang';
import { trichXuatTuKhoaSanPhamTuQr } from '../features/ban-hang-tai-quay/SanPhamQR';

export default function ModalQuetQR({ visible, onClose }) {
  const [permission, requestPermission] = useCameraPermissions();
  const [scanned, setScanned] = useState(false);
  const { sanPhamLogic } = suDungBanHang();
  const { tuKhoaSanPham, setTuKhoaSanPham, taiSanPham } = sanPhamLogic;

  useEffect(() => {
    if (visible) {
      setScanned(false);
    }
  }, [visible]);

  if (!visible) return null;

  if (!permission) {
    return <View />;
  }

  if (!permission.granted) {
    return (
      <Modal visible={visible} animationType="slide" transparent={true} onRequestClose={onClose}>
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={{ textAlign: 'center', marginBottom: 20 }}>Chúng tôi cần quyền truy cập camera để quét mã QR</Text>
            <TouchableOpacity style={styles.btnScan} onPress={requestPermission}>
              <Text style={styles.txtBtnScan}>Cấp quyền Camera</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.btnScan, { marginTop: 10, backgroundColor: '#e2e8f0' }]} onPress={onClose}>
              <Text style={{ color: '#1e293b', fontWeight: 'bold' }}>Đóng</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    );
  }

  const handleBarcodeScanned = ({ type, data }) => {
    setScanned(true);
    const result = trichXuatTuKhoaSanPhamTuQr(data);
    if (result) {
      setTuKhoaSanPham(result);
      taiSanPham(result);
    }
    onClose();
  };

  return (
    <Modal visible={visible} animationType="slide" transparent={true} onRequestClose={onClose}>
      <View style={styles.modalBackground}>
        <View style={styles.modalContainerFull}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Quét mã QR Sản phẩm</Text>
            <TouchableOpacity onPress={onClose}>
              <Ionicons name="close" size={24} color="#64748b" />
            </TouchableOpacity>
          </View>
          
          <View style={styles.cameraContainer}>
            {Platform.OS === 'web' ? (
               <Text style={{ textAlign: 'center', marginTop: 40 }}>Tính năng quét Camera chưa hỗ trợ hoàn toàn trên Web qua expo-camera.</Text>
            ) : (
              <CameraView
                style={StyleSheet.absoluteFillObject}
                facing="back"
                onBarcodeScanned={scanned ? undefined : handleBarcodeScanned}
                barcodeScannerSettings={{
                  barcodeTypes: ["qr", "ean13", "ean8", "code128"],
                }}
              />
            )}
            
            <View style={styles.overlay}>
              <View style={styles.scanFrame} />
            </View>
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
    maxWidth: 500,
    padding: 24,
    borderRadius: 16,
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column',
  },
  modalContainerFull: {
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
  cameraContainer: {
    flex: 1,
    position: 'relative',
    backgroundColor: '#000',
  },
  btnScan: {
    backgroundColor: '#ef4444',
    padding: 12,
    borderRadius: 8,
    alignItems: 'center',
  },
  txtBtnScan: {
    color: '#fff',
    fontWeight: 'bold',
  },
  overlay: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'center',
    alignItems: 'center',
  },
  scanFrame: {
    width: 250,
    height: 250,
    borderWidth: 2,
    borderColor: '#ef4444',
    backgroundColor: 'transparent',
  }
});
