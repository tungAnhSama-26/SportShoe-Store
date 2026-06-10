package com.example.server.core.client.sanpham.controller;

import com.example.server.core.admin.quanLySanPham.dto.response.GiayDetailResponse;
import com.example.server.core.admin.quanLySanPham.dto.response.GiayListItemResponse;
import com.example.server.core.admin.quanLySanPham.dto.response.ThuocTinhResponse;
import com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService;
import com.example.server.core.client.sanpham.dto.ClientChiTietSanPhamResponse;
import com.example.server.core.client.sanpham.dto.ClientChiTietSanPhamResponse.BienTheItem;
import com.example.server.core.client.sanpham.dto.ClientSanPhamResponse;
import com.example.server.entity.Giay;
import com.example.server.entity.GiayChiTiet;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.GiayRepository;
import com.example.server.repository.HinhAnhGiayRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API sản phẩm cho phía khách hàng (storefront). Public - không cần đăng nhập
 * (path ngoài /api/v1/admin/** nên rơi vào anyRequest().permitAll() trong SecurityConfig).
 * Tái dùng QuanLySanPhamService để không lặp lại logic tính giá/ảnh/đợt giảm giá.
 */
@RestController
@RequestMapping("/api/v1/client/san-pham")
public class ClientSanPhamController {

    private final QuanLySanPhamService service;
    private final GiayChiTietRepository giayChiTietRepository;
    private final GiayRepository giayRepository;
    private final HinhAnhGiayRepository hinhAnhGiayRepository;

    public ClientSanPhamController(
            QuanLySanPhamService service,
            GiayChiTietRepository giayChiTietRepository,
            GiayRepository giayRepository,
            HinhAnhGiayRepository hinhAnhGiayRepository
    ) {
        this.service = service;
        this.giayChiTietRepository = giayChiTietRepository;
        this.giayRepository = giayRepository;
        this.hinhAnhGiayRepository = hinhAnhGiayRepository;
    }

    /** Tất cả sản phẩm đang bán cho trang danh sách sản phẩm (public), kèm màu sắc & kích cỡ để lọc. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClientSanPhamResponse>>> danhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer thuongHieuId,
            @RequestParam(required = false) Integer loaiGiayId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        var pageable = PageRequest.of(0, 1000,
                Sort.by(Sort.Direction.DESC, "ngayTao").and(Sort.by(Sort.Direction.DESC, "id")));
        PageResponse<GiayListItemResponse> page =
                service.danhSachGiay(keyword, thuongHieuId, loaiGiayId, 1, minPrice, maxPrice, pageable);

        List<GiayListItemResponse> items = page.items();
        List<Integer> ids = items.stream().map(GiayListItemResponse::id).toList();
        Map<Integer, List<String>> mauMap = nhomTheoGiay(ids.isEmpty()
                ? List.of() : giayChiTietRepository.findMauSacByGiayIds(ids));
        Map<Integer, List<String>> sizeMap = nhomTheoGiay(ids.isEmpty()
                ? List.of() : giayChiTietRepository.findKichCoByGiayIds(ids));
        // Tính giá sau giảm + tìm biến thể có giá thấp nhất cho từng sản phẩm.
        List<GiayChiTiet> allCts = ids.isEmpty() ? List.of() : giayChiTietRepository.findActiveByGiayIds(ids);
        Map<Integer, BigDecimal> giaSauGiamMap = service.layGiaSauGiam(allCts);
        Map<Integer, BigDecimal> giaHienThiMinMap = new HashMap<>();
        Map<Integer, Boolean> coGiamMap = new HashMap<>();
        Map<Integer, Integer> bienTheReNhatMap = new HashMap<>(); // giayId -> id biến thể giá thấp nhất
        for (GiayChiTiet gct : allCts) {
            Integer giayId = gct.getGiay().getId();
            BigDecimal gia = giaSauGiamMap.getOrDefault(gct.getId(), gct.getGiaBan());
            BigDecimal min = giaHienThiMinMap.get(giayId);
            if (min == null || gia.compareTo(min) < 0) {
                giaHienThiMinMap.put(giayId, gia);
                bienTheReNhatMap.put(giayId, gct.getId());
            }
            if (giaSauGiamMap.containsKey(gct.getId())) {
                coGiamMap.put(giayId, true);
            }
        }

        // Ảnh hiển thị = ảnh chính của biến thể giá thấp nhất. Nếu biến thể đó chưa
        // có ảnh thì lùi về ảnh gốc của sản phẩm để card không bị trống.
        Map<Integer, String> anhBienTheRe = new HashMap<>();
        List<Integer> bienTheReNhatIds = new ArrayList<>(bienTheReNhatMap.values());
        if (!bienTheReNhatIds.isEmpty()) {
            for (Object[] row : hinhAnhGiayRepository.findMainImageUrlsByGiayChiTietIds(bienTheReNhatIds)) {
                // Đã ORDER BY laHinhChinh DESC -> dòng đầu của mỗi biến thể là ảnh chính.
                anhBienTheRe.putIfAbsent((Integer) row[0], (String) row[1]);
            }
        }
        Map<Integer, String> anhGocMap = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] row : giayRepository.findHinhAnhByIds(ids)) {
                anhGocMap.put((Integer) row[0], (String) row[1]);
            }
        }
        Map<Integer, String> anhMap = new HashMap<>();
        for (Map.Entry<Integer, Integer> e : bienTheReNhatMap.entrySet()) {
            String anh = anhBienTheRe.get(e.getValue());
            anhMap.put(e.getKey(), anh != null ? anh : anhGocMap.get(e.getKey()));
        }

        List<ClientSanPhamResponse> data = items.stream()
                .map(it -> new ClientSanPhamResponse(
                        it,
                        anhMap.get(it.id()),
                        giaHienThiMinMap.get(it.id()),
                        coGiamMap.getOrDefault(it.id(), false),
                        mauMap.getOrDefault(it.id(), List.of()),
                        sizeMap.getOrDefault(it.id(), List.of())))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm thành công", data));
    }

    /** Gom các dòng (giayId, giá trị) thành map giayId -> danh sách giá trị. */
    private Map<Integer, List<String>> nhomTheoGiay(List<Object[]> rows) {
        Map<Integer, List<String>> map = new HashMap<>();
        for (Object[] row : rows) {
            Integer giayId = (Integer) row[0];
            String giaTri = (String) row[1];
            map.computeIfAbsent(giayId, k -> new ArrayList<>()).add(giaTri);
        }
        return map;
    }

    /** Chi tiết sản phẩm: thông tin + ảnh đại diện + danh sách biến thể (màu, size, giá, tồn, ảnh). */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientChiTietSanPhamResponse>> chiTiet(@PathVariable Integer id) {
        // chiTietGiay ném 404 nếu sản phẩm không tồn tại.
        GiayDetailResponse detail = service.chiTietGiay(id);
        String hinhAnhSanPham = giayRepository.findById(id).map(Giay::getHinhAnh).orElse(null);

        List<GiayChiTiet> bienThes = giayChiTietRepository.findByGiayIdEager(id).stream()
                .filter(gct -> Integer.valueOf(1).equals(gct.getKichHoat()))
                .toList();
        List<Integer> bienTheIds = bienThes.stream().map(GiayChiTiet::getId).toList();

        Map<Integer, String> anhBienThe = new HashMap<>();
        if (!bienTheIds.isEmpty()) {
            for (Object[] row : hinhAnhGiayRepository.findMainImageUrlsByGiayChiTietIds(bienTheIds)) {
                // Đã ORDER BY laHinhChinh DESC -> dòng đầu của mỗi biến thể là ảnh chính.
                anhBienThe.putIfAbsent((Integer) row[0], (String) row[1]);
            }
        }

        // giaBan của BienTheItem = giá hiện tại (sau giảm); giaGoc = giá niêm yết trước giảm.
        Map<Integer, BigDecimal> giaSauGiamMap = service.layGiaSauGiam(bienThes);
        List<BienTheItem> items = bienThes.stream()
                .map(gct -> {
                    BigDecimal niemYet = gct.getGiaBan();
                    BigDecimal giaHienThi = giaSauGiamMap.getOrDefault(gct.getId(), niemYet);
                    return new BienTheItem(
                            gct.getId(),
                            gct.getMauSac().getTen(),
                            gct.getMauSac().getMaMauHex(),
                            gct.getKichCo().getGiaTri(),
                            giaHienThi,
                            niemYet,
                            gct.getSoLuong(),
                            anhBienThe.get(gct.getId()));
                })
                .toList();

        ThuocTinhResponse tt = detail.thuocTinh();
        ClientChiTietSanPhamResponse data = new ClientChiTietSanPhamResponse(
                detail.id(), detail.ten(), detail.moTa(),
                detail.thuongHieu(), detail.loaiGiay(), detail.chatLieu(),
                tt != null ? tt.deGiay() : null,
                tt != null ? tt.coGiay() : null,
                tt != null ? tt.congNgheDem() : null,
                tt != null ? tt.trongLuong() : null,
                detail.gioiTinh(), hinhAnhSanPham, items);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết sản phẩm thành công", data));
    }

    /** Sản phẩm nổi bật cho trang chủ: chỉ lấy sản phẩm đang bán, mới nhất trước. */
    @GetMapping("/noi-bat")
    public ResponseEntity<ApiResponse<List<GiayListItemResponse>>> sanPhamNoiBat(
            @RequestParam(defaultValue = "8") int limit
    ) {
        int size = Math.max(1, Math.min(limit, 50));
        var pageable = PageRequest.of(0, size,
                Sort.by(Sort.Direction.DESC, "ngayTao").and(Sort.by(Sort.Direction.DESC, "id")));
        PageResponse<GiayListItemResponse> page =
                service.danhSachGiay(null, null, null, 1, null, null, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm nổi bật thành công", page.items()));
    }
}
