/*
  Dong bo lai du lieu tien te cho hoa don demo.
  - tong_tien_hang = tong thanh_tien cua cac dong hoa don hop le
  - tong_tien_thanh_toan = tong_tien_hang + phi_van_chuyen - tien_giam
  - so_tien thanh_toan thanh cong = tong_tien_thanh_toan
  - tien_thoi_lai = 0 de lich su thanh toan tren UI khop tong tien hien thi
*/

WITH invoice_totals AS (
    SELECT
        hd.id,
        ISNULL(SUM(CASE WHEN hdct.trang_thai = 1 THEN hdct.thanh_tien ELSE 0 END), 0) AS tong_tien_hang_dung,
        ISNULL(vc.phi_van_chuyen, 0) AS phi_van_chuyen,
        ISNULL(hd.tien_giam, 0) AS tien_giam,
        CASE
            WHEN ISNULL(SUM(CASE WHEN hdct.trang_thai = 1 THEN hdct.thanh_tien ELSE 0 END), 0)
                 + ISNULL(vc.phi_van_chuyen, 0)
                 - ISNULL(hd.tien_giam, 0) < 0
            THEN 0
            ELSE ISNULL(SUM(CASE WHEN hdct.trang_thai = 1 THEN hdct.thanh_tien ELSE 0 END), 0)
                 + ISNULL(vc.phi_van_chuyen, 0)
                 - ISNULL(hd.tien_giam, 0)
        END AS tong_tien_thanh_toan_dung
    FROM hoa_don hd
    LEFT JOIN hoa_don_chi_tiet hdct
        ON hdct.hoa_don_id = hd.id
    LEFT JOIN van_chuyen vc
        ON vc.hoa_don_id = hd.id
    GROUP BY hd.id, vc.phi_van_chuyen, hd.tien_giam
)
UPDATE hd
SET
    hd.tong_tien_hang = totals.tong_tien_hang_dung,
    hd.tong_tien_thanh_toan = totals.tong_tien_thanh_toan_dung,
    hd.ngay_cap_nhat = SYSDATETIME()
FROM hoa_don hd
INNER JOIN invoice_totals totals
    ON totals.id = hd.id
WHERE
    hd.tong_tien_hang <> totals.tong_tien_hang_dung
    OR hd.tong_tien_thanh_toan <> totals.tong_tien_thanh_toan_dung;

WITH latest_success_payment AS (
    SELECT
        tt.id,
        tt.hoa_don_id,
        ROW_NUMBER() OVER (
            PARTITION BY tt.hoa_don_id
            ORDER BY ISNULL(tt.ngay_thanh_toan, tt.ngay_tao) DESC, tt.id DESC
        ) AS rn
    FROM thanh_toan tt
    WHERE tt.trang_thai = 1
),
invoice_totals AS (
    SELECT
        hd.id,
        CASE
            WHEN ISNULL(SUM(CASE WHEN hdct.trang_thai = 1 THEN hdct.thanh_tien ELSE 0 END), 0)
                 + ISNULL(vc.phi_van_chuyen, 0)
                 - ISNULL(hd.tien_giam, 0) < 0
            THEN 0
            ELSE ISNULL(SUM(CASE WHEN hdct.trang_thai = 1 THEN hdct.thanh_tien ELSE 0 END), 0)
                 + ISNULL(vc.phi_van_chuyen, 0)
                 - ISNULL(hd.tien_giam, 0)
        END AS tong_tien_thanh_toan_dung
    FROM hoa_don hd
    LEFT JOIN hoa_don_chi_tiet hdct
        ON hdct.hoa_don_id = hd.id
    LEFT JOIN van_chuyen vc
        ON vc.hoa_don_id = hd.id
    GROUP BY hd.id, vc.phi_van_chuyen, hd.tien_giam
)
UPDATE tt
SET
    tt.so_tien = totals.tong_tien_thanh_toan_dung,
    tt.tien_thoi_lai = 0
FROM thanh_toan tt
INNER JOIN latest_success_payment latest
    ON latest.id = tt.id
    AND latest.rn = 1
INNER JOIN invoice_totals totals
    ON totals.id = tt.hoa_don_id
WHERE
    tt.so_tien <> totals.tong_tien_thanh_toan_dung
    OR ISNULL(tt.tien_thoi_lai, 0) <> 0;

SELECT
    hd.id,
    hd.ma,
    hd.tong_tien_hang,
    hd.tien_giam,
    ISNULL(vc.phi_van_chuyen, 0) AS phi_van_chuyen,
    hd.tong_tien_thanh_toan,
    ISNULL(tt.so_tien, 0) AS thanh_toan_hien_thi
FROM hoa_don hd
LEFT JOIN van_chuyen vc
    ON vc.hoa_don_id = hd.id
OUTER APPLY (
    SELECT TOP 1 so_tien
    FROM thanh_toan pay
    WHERE pay.hoa_don_id = hd.id
      AND pay.trang_thai = 1
    ORDER BY ISNULL(pay.ngay_thanh_toan, pay.ngay_tao) DESC, pay.id DESC
) tt
ORDER BY hd.id;
