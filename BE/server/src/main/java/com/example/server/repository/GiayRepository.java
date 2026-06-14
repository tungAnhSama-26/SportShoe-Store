package com.example.server.repository;

import com.example.server.entity.Giay;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GiayRepository extends JpaRepository<Giay, Integer>, JpaSpecificationExecutor<Giay> {

    /** Số sản phẩm theo trạng thái (1 = đang bán) - cho thống kê trang chủ. */
    long countByTrangThai(Integer trangThai);

    /** (giayId, ảnh đại diện sản phẩm) - dùng cho danh sách sản phẩm phía khách hàng. */
    @Query("select g.id, g.hinhAnh from Giay g where g.id in :ids")
    List<Object[]> findHinhAnhByIds(@Param("ids") Collection<Integer> ids);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, Integer id);

    boolean existsByTenIgnoreCase(String ten);

    boolean existsByTenIgnoreCaseAndIdNot(String ten, Integer id);
    
    Optional<Giay> findFirstByTenIgnoreCase(String ten);
    
    Optional<Giay> findFirstByMaIgnoreCase(String ma);

    @Query("SELECT g FROM Giay g LEFT JOIN g.giayThuocTinh t WHERE " +
           "(:thuongHieuId IS NULL OR g.thuongHieu.id = :thuongHieuId) AND " +
           "(:loaiGiayId IS NULL OR g.loaiGiay.id = :loaiGiayId) AND " +
           "(:gioiTinh IS NULL OR g.gioiTinh = :gioiTinh) AND " +
           "(:chatLieuGiayId IS NULL AND t.chatLieuGiay.id IS NULL OR t.chatLieuGiay.id = :chatLieuGiayId) AND " +
           "(:deGiayId IS NULL AND t.deGiay.id IS NULL OR t.deGiay.id = :deGiayId) AND " +
           "(:coGiayId IS NULL AND t.coGiay.id IS NULL OR t.coGiay.id = :coGiayId) AND " +
           "(:congNgheDemId IS NULL AND t.congNgheDem.id IS NULL OR t.congNgheDem.id = :congNgheDemId) AND " +
           "(:trongLuongId IS NULL AND t.trongLuong.id IS NULL OR t.trongLuong.id = :trongLuongId)")
    List<Giay> findByThuocTinh(
            @Param("thuongHieuId") Integer thuongHieuId,
            @Param("loaiGiayId") Integer loaiGiayId,
            @Param("gioiTinh") Integer gioiTinh,
            @Param("chatLieuGiayId") Integer chatLieuGiayId,
            @Param("deGiayId") Integer deGiayId,
            @Param("coGiayId") Integer coGiayId,
            @Param("congNgheDemId") Integer congNgheDemId,
            @Param("trongLuongId") Integer trongLuongId
    );
}
