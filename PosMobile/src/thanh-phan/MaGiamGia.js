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
    xuLyApDungPhieu,
    phieuGiamGiaHangMucTiepTheo,
    soTienThieuDeDatHangMuc,
    soTienGiamCuaHangMucTiepTheo
  } = phieuGiamGiaLogic;

  return (
    <View style={styles.container}>
      {/* 1. Best Voucher Applied Card */}
      {phieuGiamGiaDaApDung ? (
        <View style={styles.appliedCard}>
          <TouchableOpacity 
            style={styles.removeBtn}
            onPress={() => {
              setPhieuGiamGiaDaApDung(null);
              setMaPhieuGiamGia("");
            }}
            hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
          >
            <Ionicons name="close" size={16} color="#059669" />
          </TouchableOpacity>

          <View style={styles.appliedHeaderRow}>
            <Ionicons name="checkmark-circle" size={18} color="#059669" />
            <Text style={styles.appliedHeaderTitle}>Đang áp dụng voucher tốt nhất</Text>
          </View>

          <View style={styles.appliedBody}>
            <View style={styles.appliedCodeRow}>
              <Text style={styles.appliedCodeText}>{phieuGiamGiaDaApDung.ma}</Text>
              {phieuGiamGiaDaApDung.loai === 1 && (
                <View style={styles.percentBadge}>
                  <Text style={styles.percentBadgeText}>{phieuGiamGiaDaApDung.giaTri}%</Text>
                </View>
              )}
            </View>
            <View style={styles.appliedDiscountRow}>
              <Text style={styles.appliedDiscountLabel}>Giá trị giảm:</Text>
              <Text style={styles.appliedDiscountValue}>
                -{(phieuGiamGiaDaApDung.soTienGiam || 0).toLocaleString('vi-VN')} đ
              </Text>
            </View>
          </View>
        </View>
      ) : (
        /* 2. Coupon Input / Apply */
        <View style={styles.inputContainer}>
          <View style={styles.searchRow}>
            <TextInput
              style={styles.input}
              placeholder="Nhập hoặc chọn mã giảm giá..."
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
        </View>
      )}

      {/* 3. Next Tier Suggested Coupon */}
      {phieuGiamGiaHangMucTiepTheo && (
        <View style={styles.suggestedContainer}>
          <View style={styles.suggestedHeaderRow}>
            <Text style={styles.suggestedTitle}>Gợi ý mua thêm</Text>
            <View style={styles.suggestedBadge}>
              <Text style={styles.suggestedBadgeText}>1 đề xuất</Text>
            </View>
          </View>
          <View style={styles.suggestedCard}>
            <View style={styles.suggestedCodeRow}>
              <View style={styles.suggestedPercentBadge}>
                <Text style={styles.suggestedPercentText}>
                  {phieuGiamGiaHangMucTiepTheo.loai === 1 
                    ? `${phieuGiamGiaHangMucTiepTheo.giaTri}%` 
                    : `${(phieuGiamGiaHangMucTiepTheo.giaTri || 0).toLocaleString('vi-VN')} đ`}
                </Text>
              </View>
              <Text style={styles.suggestedCodeText}>{phieuGiamGiaHangMucTiepTheo.ma}</Text>
            </View>
            <View style={styles.suggestedDetails}>
              <View style={styles.suggestedDetailRow}>
                <Text style={styles.suggestedDetailLabel}>Cần mua thêm:</Text>
                <Text style={styles.suggestedDetailValue}>
                  {(soTienThieuDeDatHangMuc || 0).toLocaleString('vi-VN')} đ
                </Text>
              </View>
              <View style={styles.suggestedDetailRow}>
                <Text style={styles.suggestedDetailLabel}>Sẽ được giảm:</Text>
                <Text style={styles.suggestedDiscountValue}>
                  {(soTienGiamCuaHangMucTiepTheo || 0).toLocaleString('vi-VN')} đ
                </Text>
              </View>
            </View>
          </View>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 12,
  },
  inputContainer: {
    marginBottom: 6,
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
    backgroundColor: '#ffffff',
    borderRadius: 10,
    paddingHorizontal: 12,
    height: 40,
    fontSize: 13,
    fontWeight: '600',
    color: '#0f172a',
  },
  applyBtn: {
    backgroundColor: '#ef4444',
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 14,
    borderRadius: 10,
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
    backgroundColor: '#F2F9F4',
    borderWidth: 1,
    borderColor: '#E3F2E8',
    borderRadius: 12,
    padding: 12,
    position: 'relative',
    marginBottom: 6,
  },
  removeBtn: {
    position: 'absolute',
    top: 10,
    right: 10,
    padding: 4,
    backgroundColor: '#d1fae5',
    borderRadius: 12,
    zIndex: 10,
  },
  appliedHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginBottom: 8,
  },
  appliedHeaderTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: '#1e293b',
  },
  appliedBody: {
    paddingLeft: 24,
    gap: 6,
  },
  appliedCodeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  appliedCodeText: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0f172a',
  },
  percentBadge: {
    backgroundColor: '#d1fae5',
    borderWidth: 1,
    borderColor: '#a7f3d0',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 6,
  },
  percentBadgeText: {
    fontSize: 11,
    fontWeight: '700',
    color: '#059669',
  },
  appliedDiscountRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  appliedDiscountLabel: {
    fontSize: 12,
    color: '#64748b',
  },
  appliedDiscountValue: {
    fontSize: 13,
    fontWeight: '700',
    color: '#059669',
  },
  suggestedContainer: {
    marginTop: 8,
  },
  suggestedHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 6,
  },
  suggestedTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: '#059669',
  },
  suggestedBadge: {
    backgroundColor: '#FFF8ED',
    borderWidth: 1,
    borderColor: '#fde68a',
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 12,
  },
  suggestedBadgeText: {
    fontSize: 10,
    fontWeight: '700',
    color: '#d97706',
  },
  suggestedCard: {
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: '#f1f5f9',
    borderRadius: 10,
    padding: 10,
    gap: 6,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.03,
    shadowRadius: 4,
    elevation: 1,
  },
  suggestedCodeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  suggestedPercentBadge: {
    backgroundColor: '#ecfdf5',
    borderWidth: 1,
    borderColor: '#d1fae5',
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 12,
  },
  suggestedPercentText: {
    fontSize: 11,
    fontWeight: '700',
    color: '#059669',
  },
  suggestedCodeText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#1e293b',
  },
  suggestedDetails: {
    paddingLeft: 40,
    gap: 4,
  },
  suggestedDetailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  suggestedDetailLabel: {
    fontSize: 12,
    color: '#64748b',
  },
  suggestedDetailValue: {
    fontSize: 12,
    fontWeight: '700',
    color: '#334155',
  },
  suggestedDiscountValue: {
    fontSize: 12,
    fontWeight: '700',
    color: '#059669',
  },
});
