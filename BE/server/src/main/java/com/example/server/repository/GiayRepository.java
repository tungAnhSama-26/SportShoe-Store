package com.example.server.repository;

import com.example.server.entity.Giay;
import java.util.Collection;
import java.util.List;
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
}
