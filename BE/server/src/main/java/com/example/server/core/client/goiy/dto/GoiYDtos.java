package com.example.server.core.client.goiy.dto;

import java.math.BigDecimal;
import java.util.List;

/** Các DTO cho tính năng gợi ý giày bằng AI (trắc nghiệm + quét form bàn chân). */
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

    /**
     * Yêu cầu gợi ý.
     *
     * @param traLoi  danh sách đáp án đã chọn theo từng câu.
     * @param anhChan ảnh bàn chân dạng data URI base64 để AI quét & đánh giá form chân
     *                (có thể null - khách không quét chân).
     */
    public record GoiYRequest(
            List<TraLoiRequest> traLoi,
            String anhChan
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
            String sizeGoiY,        // size AI ước lượng từ ảnh chân (null nếu không gửi ảnh / không đoán được)
            String danhGiaChan,
            List<SanPhamGoiYResponse> sanPhams
    ) {}
}
