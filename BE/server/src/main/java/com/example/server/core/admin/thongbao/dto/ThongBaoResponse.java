package com.example.server.core.admin.thongbao.dto;

import java.time.Instant;
import java.util.UUID;

public record ThongBaoResponse(
    UUID id,
    String tieuDe,
    String noiDung,
    String loai,
    String link,
    Boolean daDoc,
    Instant ngayTao
) {}
