import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { suDungBanHang } from '../ngu-canh/NguCanhBanHang';

export default function BangGioHang() {
  const { gioHangLogic } = suDungBanHang();
  const { cartItems: sanPhamTrongGio, capNhatSoLuong, xoaSanPham: xoaKhoiGio, isOutdatedPrice } = gioHangLogic;

  return (
    <View style={styles.container}>
      <View style={styles.cartList}>
        {sanPhamTrongGio.length === 0 ? (
          <View style={styles.emptyCart}>
            <View style={styles.emptyIconWrapper}>
              <Ionicons name="cart-outline" size={36} color="#94a3b8" />
            </View>
            <Text style={styles.emptyText}>Giỏ hàng đang trống</Text>
            <Text style={styles.emptySubText}>Vui lòng chọn hoặc quét mã sản phẩm</Text>
          </View>
        ) : (
          sanPhamTrongGio.map(item => {
            const giaGoc = Number(item?.giaGoc || 0);
            const giaBan = Number(item?.giaBan || 0);
            const isDiscounted = giaBan < giaGoc;
            const isOutdated = isOutdatedPrice ? isOutdatedPrice(item) : (item?.isOutdatedPrice || false);
            let discountText = "";
            if (isDiscounted && giaGoc > 0) {
              const pct = Math.round(((giaGoc - giaBan) / giaGoc) * 100);
              discountText = `-${pct}%`;
            }

            return (
              <View key={item.chiTietId || item.cartItemId} style={styles.cartCard}>
                <View style={styles.itemHeader}>
                  <Text style={styles.itemName} numberOfLines={2}>{item.tenSanPham}</Text>
                  <TouchableOpacity 
                    style={styles.removeBtn} 
                    onPress={() => xoaKhoiGio(item.chiTietId)}
                    hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
                  >
                    <Ionicons name="trash-outline" size={16} color="#ef4444" />
                  </TouchableOpacity>
                </View>
                
                <View style={styles.variantRow}>
                  <View style={styles.variantTag}>
                    <Text style={styles.variantTagText}>Màu: {item.mauSac || 'Tiêu chuẩn'}</Text>
                  </View>
                  <View style={styles.variantTag}>
                    <Text style={styles.variantTagText}>Size: {item.kichCo || 'N/A'}</Text>
                  </View>
                </View>
                
                <View style={styles.itemFooter}>
                  <View style={styles.priceContainer}>
                    <Text style={styles.itemPrice}>{giaBan.toLocaleString('vi-VN')} đ</Text>
                    {isDiscounted && (
                      <View style={styles.discountRow}>
                        <Text style={styles.itemOriginalPrice}>{giaGoc.toLocaleString('vi-VN')} đ</Text>
                        <View style={styles.discountBadge}>
                          <Text style={styles.discountBadgeText}>{discountText}</Text>
                        </View>
                      </View>
                    )}
                  </View>
                  
                  <View style={styles.quantityControl}>
                    <TouchableOpacity 
                      style={styles.qtyBtn} 
                      onPress={() => capNhatSoLuong(item.chiTietId, item.soLuong - 1)}
                    >
                      <Ionicons name="remove" size={14} color="#334155" />
                    </TouchableOpacity>
                    <Text style={styles.qtyText}>{item.soLuong}</Text>
                    <TouchableOpacity 
                      style={[styles.qtyBtn, isOutdated && styles.qtyBtnDisabled]} 
                      onPress={() => !isOutdated && capNhatSoLuong(item.chiTietId, item.soLuong + 1)}
                      disabled={isOutdated}
                    >
                      <Ionicons name="add" size={14} color={isOutdated ? "#94a3b8" : "#334155"} />
                    </TouchableOpacity>
                  </View>

                  <Text style={styles.itemTotal}>
                    {(giaBan * item.soLuong).toLocaleString('vi-VN')} đ
                  </Text>
                </View>
              </View>
            );
          })
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  cartList: {
    padding: 12,
    gap: 10,
  },
  cartCard: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    shadowColor: '#0f172a',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 3,
    elevation: 1,
  },
  itemHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 4,
  },
  itemName: {
    fontWeight: '700',
    color: '#1e293b',
    fontSize: 14,
    flex: 1,
    paddingRight: 8,
  },
  removeBtn: {
    padding: 2,
  },
  variantRow: {
    flexDirection: 'row',
    gap: 6,
    marginBottom: 10,
  },
  variantTag: {
    backgroundColor: '#f1f5f9',
    borderRadius: 4,
    paddingHorizontal: 6,
    paddingVertical: 2,
  },
  variantTagText: {
    color: '#475569',
    fontSize: 11,
    fontWeight: '600',
  },
  itemFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  priceContainer: {
    justifyContent: 'center',
  },
  itemPrice: {
    color: '#334155',
    fontSize: 14,
    fontWeight: '600',
  },
  discountRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 2,
    gap: 4,
  },
  originPrice: {
    fontSize: 11,
    color: '#94a3b8',
    textDecorationLine: 'line-through',
  },
  discountBadge: {
    backgroundColor: '#ffe4e6',
    paddingHorizontal: 4,
    paddingVertical: 1,
    borderRadius: 4,
  },
  discountBadgeText: {
    fontSize: 10,
    color: '#e11d48',
    fontWeight: '700',
  },
  quantityControl: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f8fafc',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    paddingHorizontal: 2,
    paddingVertical: 2,
  },
  qtyBtn: {
    padding: 6,
    borderRadius: 4,
  },
  qtyText: {
    marginHorizontal: 8,
    fontWeight: '700',
    fontSize: 14,
    color: '#1e293b',
    minWidth: 20,
    textAlign: 'center',
  },
  itemTotal: {
    color: '#ef4444',
    fontWeight: '800',
    fontSize: 15,
  },
  emptyCart: {
    paddingVertical: 32,
    paddingHorizontal: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  emptyIconWrapper: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: '#f1f5f9',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 10,
  },
  emptyText: {
    color: '#475569',
    fontSize: 15,
    fontWeight: '700',
  },
  emptySubText: {
    color: '#94a3b8',
    fontSize: 12,
    marginTop: 4,
  },
  outdatedBadge: {
    backgroundColor: '#fef3c7',
    paddingHorizontal: 4,
    paddingVertical: 1,
    borderRadius: 4,
    borderWidth: 1,
    borderColor: '#fde68a',
    marginTop: 2,
    alignSelf: 'flex-start',
  },
  outdatedBadgeText: {
    color: '#b45309',
    fontSize: 10,
    fontWeight: '700',
  },
  qtyBtnDisabled: {
    backgroundColor: '#f1f5f9',
    borderColor: '#e2e8f0',
    opacity: 0.5,
  },
});
