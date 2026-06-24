import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

export default function PhanHoaDonCho({ 
  danhSachHoaDonCho = [], 
  hoaDonChoDaChon = null, 
  chonHoaDonCho,
  xuLyTaoHoaDonChoMoi,
  dangTaiHoaDonCho = false,
  maxPendingInvoices = 5
}) {
  const pendingInvoiceLimitReached = danhSachHoaDonCho.length >= maxPendingInvoices;

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity 
          style={[styles.btnAdd, pendingInvoiceLimitReached && styles.btnAddDisabled]}
          onPress={xuLyTaoHoaDonChoMoi}
          disabled={pendingInvoiceLimitReached}
        >
          <Ionicons name="add" size={20} color={pendingInvoiceLimitReached ? "#94a3b8" : "#334155"} />
          <Text style={[styles.txtBtnAdd, pendingInvoiceLimitReached && styles.txtBtnAddDisabled]}>
            Thêm hóa đơn chờ
          </Text>
        </TouchableOpacity>
        
        <View style={styles.badge}>
          <Text style={styles.badgeText}>
            {dangTaiHoaDonCho ? "Đang tải..." : `${danhSachHoaDonCho.length}/${maxPendingInvoices} hóa đơn`}
          </Text>
        </View>
      </View>

      {pendingInvoiceLimitReached && (
        <Text style={styles.limitText}>
          Đã đạt giới hạn tối đa {maxPendingInvoices} hóa đơn chờ.
        </Text>
      )}

      <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.list}>
        {danhSachHoaDonCho.length > 0 ? danhSachHoaDonCho.map((invoice) => {
          const isActive = hoaDonChoDaChon?.id === invoice.id;
          return (
            <TouchableOpacity 
              key={invoice.id} 
              style={[styles.invoiceItem, isActive ? styles.invoiceItemActive : styles.invoiceItemInactive]}
              onPress={() => chonHoaDonCho(invoice)}
            >
              <Text style={styles.invoiceText}>{invoice.ma}</Text>
            </TouchableOpacity>
          );
        }) : (
          !dangTaiHoaDonCho && (
            <View style={styles.emptyContainer}>
              <Text style={styles.emptyText}>Chưa có hóa đơn chờ nào.</Text>
            </View>
          )
        )}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    borderRadius: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.7)',
    padding: 12,
    shadowColor: '#0f172a',
    shadowOffset: { width: 0, height: 12 },
    shadowOpacity: 0.08,
    shadowRadius: 30,
    elevation: 4,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  btnAdd: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 36,
    paddingHorizontal: 16,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    backgroundColor: '#fff',
    gap: 8,
  },
  btnAddDisabled: {
    backgroundColor: '#f1f5f9',
  },
  txtBtnAdd: {
    fontSize: 14,
    fontWeight: '600',
    color: '#334155',
  },
  txtBtnAddDisabled: {
    color: '#94a3b8',
  },
  badge: {
    backgroundColor: '#f1f5f9',
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 999,
  },
  badgeText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#475569',
  },
  limitText: {
    fontSize: 12,
    fontWeight: '500',
    color: '#d97706',
    marginBottom: 16,
  },
  list: {
    flexDirection: 'row',
  },
  invoiceItem: {
    minWidth: 150,
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 6,
    borderWidth: 1,
    marginRight: 12,
  },
  invoiceItemActive: {
    borderColor: '#ef4444',
    backgroundColor: '#fef2f2',
    shadowColor: '#ef4444',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.15,
    shadowRadius: 15,
    elevation: 3,
  },
  invoiceItemInactive: {
    borderColor: '#e2e8f0',
    backgroundColor: '#f8fafc',
  },
  invoiceText: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#0f172a',
  },
  emptyContainer: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    borderStyle: 'dashed',
    borderRadius: 6,
    paddingHorizontal: 16,
    paddingVertical: 24,
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 14,
    color: '#64748b',
  }
});
