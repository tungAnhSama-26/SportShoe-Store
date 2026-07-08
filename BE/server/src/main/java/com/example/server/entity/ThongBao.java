package com.example.server.entity;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ThongBao {
    private UUID id;
    private String tieuDe;
    private String noiDung;
    private String loai;
    private String link;
    private Boolean daDoc = false;
    private Instant ngayTao = Instant.now();
    private Instant ngayCapNhat;
}
