package com.example.server.core.client.sanpham.controller;

import com.example.server.core.admin.quanLySanPham.dto.response.GiayDetailResponse;
import com.example.server.core.admin.quanLySanPham.dto.response.GiayListItemResponse;
import com.example.server.core.admin.quanLySanPham.dto.response.ThuocTinhResponse;
import com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService;
import com.example.server.core.client.sanpham.dto.ClientChiTietSanPhamResponse;
import com.example.server.core.client.sanpham.dto.ClientChiTietSanPhamResponse.BienTheItem;
import com.example.server.core.client.sanpham.dto.ClientSanPhamResponse;
import com.example.server.core.client.sanpham.dto.DongBoGiaGioRequest;
import com.example.server.core.client.sanpham.dto.GiaBienTheGioResponse;
import com.example.server.entity.Giay;
import com.example.server.entity.GiayChiTiet;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.repository.DanhGiaRepository;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.GiayRepository;
import com.example.server.repository.HinhAnhGiayRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final DanhGiaRepository danhGiaRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    public ClientSanPhamController(
            QuanLySanPhamService service,
            GiayChiTietRepository giayChiTietRepository,
            GiayRepository giayRepository,
            HinhAnhGiayRepository hinhAnhGiayRepository,
            DanhGiaRepository danhGiaRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository
    ) {
        this.service = service;
        this.giayChiTietRepository = giayChiTietRepository;
        this.giayRepository = giayRepository;
        this.hinhAnhGiayRepository = hinhAnhGiayRepository;
        this.danhGiaRepository = danhGiaRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
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
        List<ClientSanPhamResponse> data = taoDanhSach(keyword, thuongHieuId, loaiGiayId, minPrice, maxPrice);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm thành công", data));
    }

    /** Dựng danh sách sản phẩm đầy đủ (giá sau giảm, ảnh biến thể rẻ nhất, sao TB, lượt bán). */
    private List<ClientSanPhamResponse> taoDanhSach(
            String keyword, Integer thuongHieuId, Integer loaiGiayId, BigDecimal minPrice, BigDecimal maxPrice
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

        // Sao trung bình + số lượt đánh giá và tổng đã bán theo từng sản phẩm.
        Map<Integer, Double> saoMap = new HashMap<>();
        Map<Integer, Long> soDanhGiaMap = new HashMap<>();
        Map<Integer, Long> daBanMap = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] row : danhGiaRepository.thongKeSaoTheoGiay(ids)) {
                saoMap.put((Integer) row[0], row[1] == null ? 0d : ((Number) row[1]).doubleValue());
                soDanhGiaMap.put((Integer) row[0], ((Number) row[2]).longValue());
            }
            for (Object[] row : hoaDonChiTietRepository.tongDaBanTheoGiay(ids)) {
                daBanMap.put((Integer) row[0], row[1] == null ? 0L : ((Number) row[1]).longValue());
            }
        }

        return items.stream()
                .map(it -> new ClientSanPhamResponse(
                        it,
                        anhMap.get(it.id()),
                        giaHienThiMinMap.get(it.id()),
                        coGiamMap.getOrDefault(it.id(), false),
                        mauMap.getOrDefault(it.id(), List.of()),
                        sizeMap.getOrDefault(it.id(), List.of()),
                        saoMap.getOrDefault(it.id(), 0d),
                        soDanhGiaMap.getOrDefault(it.id(), 0L),
                        daBanMap.getOrDefault(it.id(), 0L)))
                .toList();
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

        // Tổng số lượng đã bán của sản phẩm (mọi biến thể, bỏ giỏ và đơn hủy).
        long daBan = hoaDonChiTietRepository.tongDaBanTheoGiay(List.of(id)).stream()
                .findFirst()
                .map(row -> row[1] == null ? 0L : ((Number) row[1]).longValue())
                .orElse(0L);

        ThuocTinhResponse tt = detail.thuocTinh();
        ClientChiTietSanPhamResponse data = new ClientChiTietSanPhamResponse(
                detail.id(), detail.ten(), detail.moTa(),
                detail.thuongHieu(), detail.loaiGiay(), detail.chatLieu(),
                tt != null ? tt.deGiay() : null,
                tt != null ? tt.coGiay() : null,
                tt != null ? tt.congNgheDem() : null,
                tt != null ? tt.trongLuong() : null,
                detail.gioiTinh(), hinhAnhSanPham, daBan, items, detail.trangThai());
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết sản phẩm thành công", data));
    }

    /**
     * Đồng bộ giá giỏ hàng: trả giá niêm yết + giá sau giảm HIỆN TẠI + tồn của các biến thể.
     * Giỏ hàng (lưu cục bộ) gọi để cập nhật lại giá khi đợt giảm giá thay đổi sau lúc thêm vào giỏ.
     */
    @PostMapping("/dong-bo-gia")
    @Transactional(readOnly = true) // mở transaction để lazy-load trọng lượng (OSIV đang tắt)
    public ResponseEntity<ApiResponse<List<GiaBienTheGioResponse>>> dongBoGiaGio(
            @RequestBody DongBoGiaGioRequest request
    ) {
        List<Integer> ids = request == null || request.ids() == null ? List.of() : request.ids();
        if (ids.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Đồng bộ giá thành công", List.of()));
        }
        List<GiayChiTiet> bienThes = giayChiTietRepository.findAllById(ids);
        Map<Integer, BigDecimal> giaSauGiamMap = service.layGiaSauGiam(bienThes);
        List<GiaBienTheGioResponse> data = bienThes.stream()
                .map(gct -> new GiaBienTheGioResponse(
                        gct.getId(),
                        gct.getGiaBan(),
                        giaSauGiamMap.getOrDefault(gct.getId(), gct.getGiaBan()),
                        gct.getSoLuong(),
                        coTheBan(gct),
                        layCanNangSanPham(gct)))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Đồng bộ giá thành công", data));
    }

    /** Biến thể còn bán được: biến thể đang bán (kichHoat=1) VÀ sản phẩm cha đang kinh doanh (trangThai=1). */
    private boolean coTheBan(GiayChiTiet gct) {
        return Integer.valueOf(1).equals(gct.getKichHoat())
                && gct.getGiay() != null
                && Integer.valueOf(1).equals(gct.getGiay().getTrangThai());
    }

    /** Cân nặng 1 sản phẩm (gram) từ thuộc tính trọng lượng; mặc định 500g khi chưa set (khớp GHN). */
    private Integer layCanNangSanPham(GiayChiTiet gct) {
        if (gct.getGiay() != null
                && gct.getGiay().getGiayThuocTinh() != null
                && gct.getGiay().getGiayThuocTinh().getTrongLuong() != null
                && gct.getGiay().getGiayThuocTinh().getTrongLuong().getGiaTri() != null) {
            return gct.getGiay().getGiayThuocTinh().getTrongLuong().getGiaTri();
        }
        return 500;
    }

    /**
     * Sản phẩm nổi bật cho trang chủ: cùng dữ liệu với trang danh sách nhưng xếp theo
     * mức độ nổi bật - bán chạy nhất trước, rồi giảm giá sâu hơn, rồi đánh giá cao & nhiều hơn.
     */
    @GetMapping("/noi-bat")
    public ResponseEntity<ApiResponse<List<ClientSanPhamResponse>>> sanPhamNoiBat(
            @RequestParam(defaultValue = "8") int limit
    ) {
        int size = Math.max(1, Math.min(limit, 50));
        List<ClientSanPhamResponse> data = taoDanhSach(null, null, null, null, null).stream()
                .sorted(Comparator
                        .comparingLong((ClientSanPhamResponse sp) -> sp.daBan() == null ? 0L : sp.daBan()).reversed()
                        .thenComparing(Comparator.comparingInt(ClientSanPhamController::phanTramGiam).reversed())
                        .thenComparing(Comparator.comparingDouble(
                                (ClientSanPhamResponse sp) -> sp.soSaoTrungBinh() == null ? 0d : sp.soSaoTrungBinh()).reversed())
                        .thenComparing(Comparator.comparingLong(
                                (ClientSanPhamResponse sp) -> sp.soDanhGia() == null ? 0L : sp.soDanhGia()).reversed()))
                .limit(size)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm nổi bật thành công", data));
    }

    /** % giảm giá của sản phẩm = (niêm yết thấp nhất - giá hiển thị thấp nhất) / niêm yết. */
    private static int phanTramGiam(ClientSanPhamResponse sp) {
        BigDecimal niemYet = sp.thongTin() != null ? sp.thongTin().giaMin() : null;
        BigDecimal hienThi = sp.giaHienThiMin();
        if (!sp.coGiam() || niemYet == null || hienThi == null || niemYet.signum() <= 0
                || hienThi.compareTo(niemYet) >= 0) {
            return 0;
        }
        return niemYet.subtract(hienThi)
                .multiply(BigDecimal.valueOf(100))
                .divide(niemYet, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }
}
