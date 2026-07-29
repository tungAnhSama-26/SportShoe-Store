-- Cache phân loại loại giày cho tính năng GỢI Ý bằng AI (cách hybrid).
-- CHẠY TAY trên DB đang có sẵn (giữ nguyên dữ liệu). Chạy lại nhiều lần vẫn an toàn.
--
-- nhom_muc_dich  : con của câu "Bạn mua giày để dùng vào việc gì?"  -> di-lam | the-thao | dao-pho | du-tiec
-- nhom_phong_cach: con của câu "Phong cách bạn thích là gì?"        -> nang-dong | toi-gian | ca-tinh | co-dien
-- NULL = chưa phân loại, lần gợi ý tới AI sẽ tự điền.
IF COL_LENGTH('dbo.loai_giay', 'nhom_muc_dich') IS NULL
    ALTER TABLE dbo.loai_giay ADD nhom_muc_dich VARCHAR(200) NULL;
GO

IF COL_LENGTH('dbo.loai_giay', 'nhom_phong_cach') IS NULL
    ALTER TABLE dbo.loai_giay ADD nhom_phong_cach VARCHAR(200) NULL;
GO
