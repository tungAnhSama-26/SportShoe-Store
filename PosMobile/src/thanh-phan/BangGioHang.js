import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { suDungBanHang } from '../ngu-canh/NguCanhBanHang';

export default function BangGioHang() {
  const { gioHangLogic } = suDungBanHang();
  const { cartItems: sanPhamTrongGio, capNhatSoLuong, xoaSanPham: xoaKhoiGio } = gioHangLogic;

  return (
    <View style={styles.container}>
      <View style={styles.cartList}>
        {sanPhamTrongGio.length === 0 ? (
          <View style={styles.emptyCart}>
            <Text style={styles.emptyText}>Chưa có sản phẩm nào trong giỏ</Text>
          </View>
        ) : (
          sanPhamTrongGio.map(item => {
            const giaGoc = Number(item?.giaGoc || 0);
            const giaBan = Number(item?.giaBan || 0);
            const isDiscounted = giaBan < giaGoc;
            let discountText = "";
            if (isDiscounted && giaGoc > 0) {
              const pct = ((giaGoc - giaBan) / giaGoc) * 100;
              discountText = pct % 1 === 0 ? `-${pct.toFixed(0)}%` : `-${pct.toFixed(1)}%`;
            }

            return (
              <View key={item.chiTietId} style={styles.cartCard}>
                <View style={styles.itemHeader}>
                  <Text style={styles.itemName} numberOfLines={2}>{item.tenSanPham}</Text>
                  <TouchableOpacity style={styles.removeBtn} onPress={() => xoaKhoiGio(item.chiTietId)}>
                    <Ionicons name="trash-outline" size={20} color="#ef4444" />
                  </TouchableOpacity>
                </View>
                
                <Text style={styles.itemVariant}>{item.mauSac} - {item.kichCo}</Text>
                
                <View style={styles.itemFooter}>
                  <View>
                    <Text style={styles.itemPrice}>
                      {giaBan.toLocaleString('vi-VN')} đ
                    </Text>
                    {isDiscounted && (
                      <View style={{ flexDirection: 'row', alignItems: 'center', marginTop: 2, gap: 4 }}>
                        <Text style={{ fontSize: 11, color: '#94a3b8', textDecorationLine: 'line-through' }}>
                          {giaGoc.toLocaleString('vi-VN')} đ
                        </Text>
                        <View style={{ backgroundColor: '#ffe4e6', paddingHorizontal: 4, paddingVertical: 2, borderRadius: 4 }}>
                          <Text style={{ fontSize: 10, color: '#e11d48', fontWeight: 'bold' }}>
                            {discountText}
                          </Text>
                        </View>
                      </View>
                    )}
                  </View>
                  
                  <View style={styles.quantityControl}>
                    <TouchableOpacity style={styles.qtyBtn} onPress={() => capNhatSoLuong(item.chiTietId, item.soLuong - 1)}>
                      <Ionicons name="remove" size={16} color="#333" />
                    </TouchableOpacity>
                    <Text style={styles.qtyText}>{item.soLuong}</Text>
                    <TouchableOpacity style={styles.qtyBtn} onPress={() => capNhatSoLuong(item.chiTietId, item.soLuong + 1)}>
                      <Ionicons name="add" size={16} color="#333" />
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
    padding: 10,
    gap: 12,
  },
  cartCard: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  itemHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 4,
  },
  itemName: {
    fontWeight: 'bold',
    color: '#1e293b',
    fontSize: 15,
    flex: 1,
    paddingRight: 8,
  },
  removeBtn: {
    padding: 4,
  },
  itemVariant: {
    color: '#64748b',
    fontSize: 13,
    marginBottom: 12,
  },
  itemFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  itemPrice: {
    color: '#64748b',
    fontSize: 14,
  },
  quantityControl: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f1f5f9',
    borderRadius: 8,
    paddingHorizontal: 4,
    paddingVertical: 2,
  },
  qtyBtn: {
    padding: 6,
  },
  qtyText: {
    marginHorizontal: 12,
    fontWeight: 'bold',
    fontSize: 15,
    color: '#1e293b',
  },
  itemTotal: {
    color: '#ef4444',
    fontWeight: 'bold',
    fontSize: 15,
  },
  emptyCart: {
    padding: 40,
    alignItems: 'center',
  },
  emptyText: {
    color: '#94a3b8',
    fontSize: 16,
  }
});
