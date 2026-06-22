package com.example.server.core.client.sanpham.dto;

import java.util.List;

/** Yêu cầu đồng bộ giá giỏ hàng: danh sách id biến thể (giayChiTietId) đang có trong giỏ. */
public record DongBoGiaGioRequest(List<Integer> ids) {
}
