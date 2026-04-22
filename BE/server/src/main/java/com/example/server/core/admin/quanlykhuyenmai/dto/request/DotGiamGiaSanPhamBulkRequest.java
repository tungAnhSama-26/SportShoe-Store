package com.example.server.core.admin.quanlykhuyenmai.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class DotGiamGiaSanPhamBulkRequest {
    private Integer dotGiamGiaId;
    private List<Integer> giayChiTietIds;
}
