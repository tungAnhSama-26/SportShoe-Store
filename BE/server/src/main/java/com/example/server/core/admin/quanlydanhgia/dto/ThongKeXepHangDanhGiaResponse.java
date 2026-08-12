package com.example.server.core.admin.quanlydanhgia.dto;

import java.util.List;

/** Hai danh sách xếp hạng cao nhất và thấp nhất từ cùng một nguồn dữ liệu đánh giá. */
public record ThongKeXepHangDanhGiaResponse(
        List<XepHangDanhGiaResponse> caoNhat,
        List<XepHangDanhGiaResponse> thapNhat
) {
}
