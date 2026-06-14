package com.example.server.repository;

import com.example.server.entity.ThuongHieu;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {

    List<ThuongHieu> findByTrangThaiOrderByTenAsc(Integer trangThai);

    /**
     * Các hãng đang hoạt động, sắp xếp theo số sản phẩm đang bán nhiều nhất (hãng nổi bật trước).
     * Dùng cho trang chủ phía khách hàng.
     */
    @Query(value = "SELECT t.* FROM thuong_hieu t "
            + "WHERE t.trang_thai = 1 "
            + "ORDER BY (SELECT COUNT(*) FROM giay g WHERE g.thuong_hieu_id = t.id AND g.trang_thai = 1) DESC, t.ten ASC",
            nativeQuery = true)
    List<ThuongHieu> findThuongHieuNoiBat();

    @Query("select t from ThuongHieu t where (:kw is null or lower(t.ma) like lower(concat('%',:kw,'%')) or lower(t.ten) like lower(concat('%',:kw,'%')))")
    Page<ThuongHieu> search(@Param("kw") String kw, Pageable pageable);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, Integer id);

    boolean existsByTenIgnoreCase(String ten);

    boolean existsByTenIgnoreCaseAndIdNot(String ten, Integer id);
    Optional<ThuongHieu> findByTenIgnoreCase(String ten);
}