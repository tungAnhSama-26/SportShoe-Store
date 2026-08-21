package com.example.server.core.client.goiy.dto;

import java.math.BigDecimal;
import java.util.List;

/** Các DTO cho tính năng gợi ý giày bằng AI và trắc nghiệm. */
public final class GoiYDtos {

    private GoiYDtos() {
    }

    /** Một câu hỏi trắc nghiệm gửi cho FE hiển thị (chọn được NHIỀU đáp án). */
    public record CauHoiResponse(
            String ma,
            String cauHoi,
            String moTa,
            List<String> luaChon
    ) {}

    /** Đáp án khách chọn cho 1 câu hỏi. */
    public record TraLoiRequest(
            String ma,
            List<String> daChon
    ) {}

    /** Yêu cầu gợi ý từ danh sách đáp án đã chọn theo từng câu. */
    public record GoiYRequest(
            List<TraLoiRequest> traLoi
    ) {}

    /** Một sản phẩm được gợi ý kèm lý do. */
    public record SanPhamGoiYResponse(
            Integer giayId,
            String ma,
            String ten,
            BigDecimal giaBan,
            String hinhAnh,
            String lyDo
    ) {}

    /** Kết quả gợi ý trả về cho khách. */
    public record GoiYResponse(
            String loiKhuyen,
            List<SanPhamGoiYResponse> sanPhams
    ) {}
}
