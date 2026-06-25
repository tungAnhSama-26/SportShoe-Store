import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Modal, Switch } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { suDungBanHang } from '../ngu-canh/NguCanhBanHang';

export default function PhanGiaoHang() {
  const logic = suDungBanHang();
  const {
    hoaDonChoDaChon,
    choPhepGiaoHang: giaoHang,
    setChoPhepGiaoHang: chuyenDoiGiaoHang,
    giaoHangLogic
  } = logic;

  const {
    thongTinGiaoHang = {},
    capNhatThongTinGiaoHang
  } = giaoHangLogic || {};

  const tenNguoiNhanGiaoHang = thongTinGiaoHang.tenNguoiNhan || '';
  const sdtNguoiNhanGiaoHang = thongTinGiaoHang.soDienThoaiNguoiNhan || '';
  const diaChiGiaoHang = thongTinGiaoHang.diaChiGiaoHang || '';
  const phiGiaoHang = thongTinGiaoHang.phiVanChuyen || 0;

  const setTenNguoiNhanGiaoHang = (val) => capNhatThongTinGiaoHang?.({ tenNguoiNhan: val });
  const setSdtNguoiNhanGiaoHang = (val) => capNhatThongTinGiaoHang?.({ soDienThoaiNguoiNhan: val });
  const setDiaChiGiaoHang = (val) => capNhatThongTinGiaoHang?.({ diaChiGiaoHang: val });
  const setPhiGiaoHang = (val) => capNhatThongTinGiaoHang?.({ phiVanChuyen: val });

  const [isModalVisible, setModalVisible] = useState(false);
  const [tempName, setTempName] = useState(tenNguoiNhanGiaoHang);
  const [tempPhone, setTempPhone] = useState(sdtNguoiNhanGiaoHang);
  const [tempAddress, setTempAddress] = useState(diaChiGiaoHang);

  const openModal = () => {
    setTempName(tenNguoiNhanGiaoHang);
    setTempPhone(sdtNguoiNhanGiaoHang);
    setTempAddress(diaChiGiaoHang);
    setModalVisible(true);
  };

  const saveAddress = () => {
    setTenNguoiNhanGiaoHang(tempName);
    setSdtNguoiNhanGiaoHang(tempPhone);
    setDiaChiGiaoHang(tempAddress);
    setModalVisible(false);
  };

  if (!hoaDonChoDaChon) return null;

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <View style={styles.headerLeft}>
          <Ionicons name="car-outline" size={20} color="#1e293b" />
          <Text style={styles.title}>Giao hàng</Text>
        </View>
        <Switch
          value={giaoHang}
          onValueChange={chuyenDoiGiaoHang}
          trackColor={{ false: "#cbd5e1", true: "#fca5a5" }}
          thumbColor={giaoHang ? "#ef4444" : "#f8fafc"}
        />
      </View>

      {giaoHang && (
        <View style={styles.content}>
          <View style={styles.addressBox}>
            <View style={styles.addressHeader}>
              <Text style={styles.addressTitle}>Thông tin nhận hàng</Text>
              <TouchableOpacity onPress={openModal}>
                <Text style={styles.editBtn}>Sửa</Text>
              </TouchableOpacity>
            </View>
            
            {tenNguoiNhanGiaoHang || sdtNguoiNhanGiaoHang ? (
              <View style={styles.addressInfo}>
                <Text style={styles.textBold}>{tenNguoiNhanGiaoHang} - {sdtNguoiNhanGiaoHang}</Text>
                <Text style={styles.textMuted}>{diaChiGiaoHang || "Chưa có địa chỉ cụ thể"}</Text>
              </View>
            ) : (
              <Text style={styles.textEmpty}>Chưa có thông tin nhận hàng</Text>
            )}
          </View>

          <View style={styles.feeBox}>
            <Text style={styles.feeTitle}>Phí giao hàng</Text>
            <TextInput
              style={styles.feeInput}
              keyboardType="numeric"
              placeholder="Nhập phí..."
              value={phiGiaoHang ? phiGiaoHang.toString() : ""}
              onChangeText={(text) => {
                const numeric = parseInt(text.replace(/[^0-9]/g, ''), 10);
                setPhiGiaoHang(isNaN(numeric) ? 0 : numeric);
              }}
            />
          </View>
        </View>
      )}

      {/* Modal Chinh Sua Giao Hang */}
      <Modal
        visible={isModalVisible}
        animationType="fade"
        transparent={true}
        onRequestClose={() => setModalVisible(false)}
      >
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>Cập nhật địa chỉ giao hàng</Text>
            
            <Text style={styles.label}>Tên người nhận</Text>
            <TextInput
              style={styles.input}
              value={tempName}
              onChangeText={setTempName}
              placeholder="Nhập tên người nhận"
            />

            <Text style={styles.label}>Số điện thoại</Text>
            <TextInput
              style={styles.input}
              value={tempPhone}
              onChangeText={setTempPhone}
              placeholder="Nhập số điện thoại"
              keyboardType="phone-pad"
            />

            <Text style={styles.label}>Địa chỉ cụ thể</Text>
            <TextInput
              style={[styles.input, { height: 80, textAlignVertical: 'top' }]}
              value={tempAddress}
              onChangeText={setTempAddress}
              placeholder="Nhập địa chỉ nhận hàng"
              multiline
            />

            <View style={styles.modalActions}>
              <TouchableOpacity style={styles.btnCancel} onPress={() => setModalVisible(false)}>
                <Text style={styles.txtBtnCancel}>Hủy</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.btnSave} onPress={saveAddress}>
                <Text style={styles.txtBtnSave}>Lưu</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#f8fafc',
    padding: 16,
    borderTopWidth: 1,
    borderTopColor: '#f1f5f9',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  headerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  title: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1e293b',
  },
  content: {
    gap: 12,
  },
  addressBox: {
    backgroundColor: '#fff',
    padding: 12,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  addressHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  addressTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#64748b',
  },
  editBtn: {
    color: '#3b82f6',
    fontWeight: 'bold',
  },
  textBold: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#1e293b',
    marginBottom: 4,
  },
  textMuted: {
    fontSize: 14,
    color: '#64748b',
  },
  textEmpty: {
    fontSize: 14,
    color: '#94a3b8',
    fontStyle: 'italic',
  },
  feeBox: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#fff',
    padding: 12,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  feeTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1e293b',
  },
  feeInput: {
    borderWidth: 1,
    borderColor: '#e2e8f0',
    borderRadius: 6,
    paddingHorizontal: 12,
    paddingVertical: 6,
    width: 120,
    textAlign: 'right',
  },
  modalBackground: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  modalContainer: {
    backgroundColor: '#fff',
    width: '100%',
    maxWidth: 400,
    borderRadius: 16,
    padding: 24,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1e293b',
    marginBottom: 16,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#475569',
    marginBottom: 4,
    marginTop: 12,
  },
  input: {
    borderWidth: 1,
    borderColor: '#cbd5e1',
    borderRadius: 8,
    padding: 10,
    fontSize: 14,
    backgroundColor: '#f8fafc',
  },
  modalActions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    gap: 12,
    marginTop: 24,
  },
  btnCancel: {
    paddingVertical: 10,
    paddingHorizontal: 16,
    borderRadius: 8,
  },
  txtBtnCancel: {
    color: '#64748b',
    fontWeight: 'bold',
  },
  btnSave: {
    backgroundColor: '#ef4444',
    paddingVertical: 10,
    paddingHorizontal: 20,
    borderRadius: 8,
  },
  txtBtnSave: {
    color: '#fff',
    fontWeight: 'bold',
  }
});
