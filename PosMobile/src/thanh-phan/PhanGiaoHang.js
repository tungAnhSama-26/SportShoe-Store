import React, { useEffect, useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Modal, Switch, ScrollView } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { suDungBanHang } from '../ngu-canh/NguCanhBanHang';
import { chuanHoaDiaChi, dinhDangDiaChi, layMaDonViDiaChi, timDonViDiaChi } from '../utils/diaChi';
import { layPhuongXaHaiCap, layTinhThanhHaiCap } from '../api/diaChi';
import { dinhDangSo, dinhDangTienNhap, layChuSoTien } from '../features/ban-hang-tai-quay/TienTe';

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
  const diaChiGiaoHang = chuanHoaDiaChi(thongTinGiaoHang.diaChiGiaoHang);
  const phiGiaoHang = thongTinGiaoHang.phiVanChuyen || 0;

  const setTenNguoiNhanGiaoHang = (val) => capNhatThongTinGiaoHang?.({ tenNguoiNhan: val });
  const setSdtNguoiNhanGiaoHang = (val) => capNhatThongTinGiaoHang?.({ soDienThoaiNguoiNhan: val });
  const setDiaChiGiaoHang = (val) => capNhatThongTinGiaoHang?.({ diaChiGiaoHang: val });
  const setPhiGiaoHang = (val) => capNhatThongTinGiaoHang?.({ phiVanChuyen: val });

  const [isModalVisible, setModalVisible] = useState(false);
  const [tempName, setTempName] = useState(tenNguoiNhanGiaoHang);
  const [tempPhone, setTempPhone] = useState(sdtNguoiNhanGiaoHang);
  const [tempAddress, setTempAddress] = useState(chuanHoaDiaChi(diaChiGiaoHang));
  const [dsTinh, setDsTinh] = useState([]);
  const [dsPhuongXa, setDsPhuongXa] = useState([]);
  const [loaiDanhSach, setLoaiDanhSach] = useState(null);
  const [phiNhapTay, setPhiNhapTay] = useState(dinhDangSo(phiGiaoHang));
  const [dangSuaPhi, setDangSuaPhi] = useState(false);

  useEffect(() => {
    if (!dangSuaPhi) {
      setPhiNhapTay(dinhDangSo(phiGiaoHang));
    }
  }, [phiGiaoHang, dangSuaPhi]);

  const luuPhiNhapTay = () => {
    const numeric = parseInt(layChuSoTien(phiNhapTay), 10);
    const phiMoi = Number.isNaN(numeric) ? 0 : numeric;
    setDangSuaPhi(false);
    setPhiNhapTay(dinhDangSo(phiMoi));
    if (phiMoi !== phiGiaoHang) {
      setPhiGiaoHang(phiMoi);
    }
  };

  useEffect(() => {
    layTinhThanhHaiCap()
      .then((data) => setDsTinh(Array.isArray(data) ? data : []))
      .catch(() => setDsTinh([]));
  }, []);

  useEffect(() => {
    if (!dsTinh.length || !diaChiGiaoHang.tinhThanh) return undefined;
    if (diaChiGiaoHang.tinhThanhCode && diaChiGiaoHang.phuongXaCode) return undefined;
    let active = true;
    const recoverCodes = async () => {
      const tinh = timDonViDiaChi(dsTinh, diaChiGiaoHang.tinhThanhCode, diaChiGiaoHang.tinhThanh);
      if (!tinh) return;
      const tinhThanhCode = layMaDonViDiaChi(tinh);
      try {
        const data = await layPhuongXaHaiCap(tinhThanhCode);
        if (!active) return;
        const danhSachPhuongXa = Array.isArray(data) ? data : [];
        const phuongXa = timDonViDiaChi(danhSachPhuongXa, diaChiGiaoHang.phuongXaCode, diaChiGiaoHang.phuongXa);
        setDsPhuongXa(danhSachPhuongXa);
        setDiaChiGiaoHang({
          ...diaChiGiaoHang,
          tinhThanhCode,
          tinhThanh: tinh.ten,
          phuongXaCode: phuongXa ? layMaDonViDiaChi(phuongXa) : '',
          phuongXa: phuongXa?.ten || diaChiGiaoHang.phuongXa,
        });
      } catch {
        // Giữ tên địa chỉ để người dùng có thể chọn lại khi danh mục khả dụng.
      }
    };
    recoverCodes();
    return () => { active = false; };
  }, [dsTinh, diaChiGiaoHang.tinhThanhCode, diaChiGiaoHang.tinhThanh, diaChiGiaoHang.phuongXaCode, diaChiGiaoHang.phuongXa]);

  const chonTinh = async (tinh) => {
    setTempAddress((current) => ({ ...current, tinhThanhCode: String(tinh.code), tinhThanh: tinh.ten, phuongXaCode: '', phuongXa: '' }));
    setLoaiDanhSach(null);
    try {
      const data = await layPhuongXaHaiCap(tinh.code);
      setDsPhuongXa(Array.isArray(data) ? data : []);
    } catch {
      setDsPhuongXa([]);
    }
  };

  const chonPhuongXa = (item) => {
    setTempAddress((current) => ({ ...current, phuongXaCode: String(item.code), phuongXa: item.ten }));
    setLoaiDanhSach(null);
  };

  const openModal = async () => {
    setTempName(tenNguoiNhanGiaoHang);
    setTempPhone(sdtNguoiNhanGiaoHang);
    const diaChiHienTai = chuanHoaDiaChi(diaChiGiaoHang);
    let danhSachTinh = dsTinh;
    if (!danhSachTinh.length) {
      try {
        const data = await layTinhThanhHaiCap();
        danhSachTinh = Array.isArray(data) ? data : [];
        setDsTinh(danhSachTinh);
      } catch {
        danhSachTinh = [];
      }
    }
    const tinh = timDonViDiaChi(danhSachTinh, diaChiHienTai.tinhThanhCode, diaChiHienTai.tinhThanh);
    if (!tinh) {
      setTempAddress({ ...diaChiHienTai, tinhThanhCode: '', phuongXaCode: '' });
      setDsPhuongXa([]);
      setModalVisible(true);
      return;
    }
    const tinhThanhCode = layMaDonViDiaChi(tinh);
    try {
      const data = await layPhuongXaHaiCap(tinhThanhCode);
      const danhSachPhuongXa = Array.isArray(data) ? data : [];
      const phuongXa = timDonViDiaChi(danhSachPhuongXa, diaChiHienTai.phuongXaCode, diaChiHienTai.phuongXa);
      setDsPhuongXa(danhSachPhuongXa);
      setTempAddress({
        ...diaChiHienTai,
        tinhThanhCode,
        tinhThanh: tinh.ten,
        phuongXaCode: phuongXa ? layMaDonViDiaChi(phuongXa) : '',
        phuongXa: phuongXa?.ten || diaChiHienTai.phuongXa,
      });
    } catch {
      setDsPhuongXa([]);
      setTempAddress({ ...diaChiHienTai, tinhThanhCode, tinhThanh: tinh.ten, phuongXaCode: '' });
    }
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
                <Text style={styles.textMuted}>{dinhDangDiaChi(diaChiGiaoHang) || "Chưa có địa chỉ cụ thể"}</Text>
              </View>
            ) : (
              <Text style={styles.textEmpty}>Chưa có thông tin nhận hàng</Text>
            )}
          </View>

          <View style={styles.feeBox}>
            <Text style={styles.feeTitle}>Phí giao hàng</Text>
            <View style={styles.feeInputWrapper}>
              <TextInput
                style={styles.feeInput}
                keyboardType="numeric"
                placeholder="0"
                value={phiNhapTay}
                onFocus={() => setDangSuaPhi(true)}
                onChangeText={(text) => setPhiNhapTay(dinhDangTienNhap(text))}
                onBlur={luuPhiNhapTay}
                onSubmitEditing={luuPhiNhapTay}
              />
              <Text style={styles.feeCurrency}>đ</Text>
            </View>
            {thongTinGiaoHang.moTaPhi ? (
              <Text style={thongTinGiaoHang.nguonTinhPhi === 'GHN_LIVE' ? styles.liveFeeNote : styles.offlineFeeNote}>
                {thongTinGiaoHang.moTaPhi}
              </Text>
            ) : null}
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

            <Text style={styles.label}>Tỉnh/Thành phố</Text>
            <TextInput
              style={styles.input}
              value={tempAddress.tinhThanh}
              editable={false}
              onPressIn={() => setLoaiDanhSach('tinh')}
              placeholder="Nhập tỉnh/thành phố"
            />

            <Text style={styles.label}>Phường/Xã</Text>
            <TextInput
              style={styles.input}
              value={tempAddress.phuongXa}
              editable={false}
              onPressIn={() => tempAddress.tinhThanhCode && setLoaiDanhSach('phuongXa')}
              placeholder="Nhập phường/xã"
            />

            <Text style={styles.label}>Địa chỉ cụ thể</Text>
            <TextInput
              style={[styles.input, { height: 80, textAlignVertical: 'top' }]}
              value={tempAddress.diaChiCuThe}
              onChangeText={(value) => setTempAddress((current) => ({ ...current, diaChiCuThe: value }))}
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
      <Modal visible={Boolean(loaiDanhSach)} animationType="slide" transparent onRequestClose={() => setLoaiDanhSach(null)}>
        <View style={styles.modalBackground}>
          <View style={styles.listContainer}>
            <Text style={styles.modalTitle}>{loaiDanhSach === 'tinh' ? 'Chọn tỉnh/thành phố' : 'Chọn phường/xã'}</Text>
            <ScrollView>
              {(loaiDanhSach === 'tinh' ? dsTinh : dsPhuongXa).map((item) => (
                <TouchableOpacity
                  key={String(item.code)}
                  style={styles.listItem}
                  onPress={() => loaiDanhSach === 'tinh' ? chonTinh(item) : chonPhuongXa(item)}
                >
                  <Text>{item.ten}</Text>
                </TouchableOpacity>
              ))}
            </ScrollView>
            <TouchableOpacity style={styles.btnCancel} onPress={() => setLoaiDanhSach(null)}>
              <Text style={styles.txtBtnCancel}>Đóng</Text>
            </TouchableOpacity>
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
    flexWrap: 'wrap',
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
  feeInputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#e2e8f0',
    borderRadius: 6,
    backgroundColor: '#f8fafc',
    paddingHorizontal: 8,
  },
  feeInput: {
    minWidth: 0,
    width: 100,
    paddingVertical: 6,
    fontSize: 14,
    fontWeight: '600',
    color: '#0f172a',
    textAlign: 'right',
  },
  feeCurrency: {
    fontSize: 13,
    fontWeight: '600',
    color: '#64748b',
    marginLeft: 4,
  },
  liveFeeNote: {
    width: '100%',
    marginTop: 6,
    fontSize: 11,
    color: '#94a3b8',
  },
  offlineFeeNote: {
    width: '100%',
    marginTop: 6,
    fontSize: 11,
    color: '#d97706',
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
  listContainer: {
    backgroundColor: '#fff',
    width: '100%',
    maxWidth: 400,
    maxHeight: '75%',
    borderRadius: 16,
    padding: 20,
  },
  listItem: {
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: '#e2e8f0',
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
