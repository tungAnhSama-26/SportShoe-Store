USE giay;
GO

SET NOCOUNT ON;
GO

-- ============================================================
-- SPORTSHOE STORE - FULL SAMPLE DATA
-- Run after: 01_schema_tables_rules.sql
--
-- This script resets demo data and inserts 10 rows for every table.
-- Default login samples:
--   Admin: nv001 / 123456
--   Staff: nv002 / 123456
--   Customer: khach1 / 123456
-- ============================================================

DELETE FROM phieu_tra_hang_chi_tiet;
DELETE FROM phieu_tra_hang;
DELETE FROM thanh_toan;
DELETE FROM van_chuyen;
DELETE FROM hoa_don_chi_tiet;
DELETE FROM lich_su_hoa_don;
DELETE FROM hoa_don;
DELETE FROM phieu_giam_gia_khach_hang;
DELETE FROM phieu_giam_gia;
DELETE FROM dot_giam_gia_san_pham;
DELETE FROM hinh_anh_giay;
DELETE FROM giay_chi_tiet;
DELETE FROM giay_thuoc_tinh;
DELETE FROM giay;
DELETE FROM dot_giam_gia;
DELETE FROM cong_nghe_dem;
DELETE FROM trong_luong;
DELETE FROM chat_lieu_giay;
DELETE FROM co_giay;
DELETE FROM de_giay;
DELETE FROM loai_giay;
DELETE FROM thuong_hieu;
DELETE FROM mau_sac;
DELETE FROM kich_co;
DELETE FROM dia_chi_khach_hang;
DELETE FROM khach_hang;
DELETE FROM lich_lam_viec;
DELETE FROM nhan_vien;
GO

DBCC CHECKIDENT ('phieu_tra_hang_chi_tiet', RESEED, 0);
DBCC CHECKIDENT ('phieu_tra_hang', RESEED, 0);
DBCC CHECKIDENT ('thanh_toan', RESEED, 0);
DBCC CHECKIDENT ('van_chuyen', RESEED, 0);
DBCC CHECKIDENT ('hoa_don_chi_tiet', RESEED, 0);
DBCC CHECKIDENT ('lich_su_hoa_don', RESEED, 0);
DBCC CHECKIDENT ('hoa_don', RESEED, 0);
DBCC CHECKIDENT ('phieu_giam_gia_khach_hang', RESEED, 0);
DBCC CHECKIDENT ('phieu_giam_gia', RESEED, 0);
DBCC CHECKIDENT ('dot_giam_gia_san_pham', RESEED, 0);
DBCC CHECKIDENT ('hinh_anh_giay', RESEED, 0);
DBCC CHECKIDENT ('giay_chi_tiet', RESEED, 0);
DBCC CHECKIDENT ('giay_thuoc_tinh', RESEED, 0);
DBCC CHECKIDENT ('giay', RESEED, 0);
DBCC CHECKIDENT ('dot_giam_gia', RESEED, 0);
DBCC CHECKIDENT ('cong_nghe_dem', RESEED, 0);
DBCC CHECKIDENT ('trong_luong', RESEED, 0);
DBCC CHECKIDENT ('chat_lieu_giay', RESEED, 0);
DBCC CHECKIDENT ('co_giay', RESEED, 0);
DBCC CHECKIDENT ('de_giay', RESEED, 0);
DBCC CHECKIDENT ('loai_giay', RESEED, 0);
DBCC CHECKIDENT ('thuong_hieu', RESEED, 0);
DBCC CHECKIDENT ('mau_sac', RESEED, 0);
DBCC CHECKIDENT ('kich_co', RESEED, 0);
DBCC CHECKIDENT ('dia_chi_khach_hang', RESEED, 0);
GO

INSERT INTO nhan_vien
(ma, ten_dang_nhap, ho_ten, email, mat_khau, sdt, cccd, gioi_tinh, ngay_sinh, dia_chi, vai_tro, trang_thai, hinh_anh)
VALUES
('NV001', 'nv001', N'Nguyễn Văn An', 'an.nguyen@giay.com', '123456', '0912345678', '001086000001', N'Nam', '1986-02-12', N'12 Láng Hạ, Đống Đa, Hà Nội', 1, 1, N'/assets/avatar/nv001.png'),
('NV002', 'nv002', N'Trần Thị Bình', 'binh.tran@giay.com', '123456', '0987654321', '001092000002', N'Nữ', '1992-05-20', N'45 Nguyễn Trãi, Thanh Xuân, Hà Nội', 2, 1, N'/assets/avatar/nv002.png'),
('NV003', 'nv003', N'Lê Hoàng Cường', 'cuong.le@giay.com', '123456', '0978123456', '048090000003', N'Nam', '1990-09-11', N'78 Trần Phú, Hải Châu, Đà Nẵng', 2, 1, N'/assets/avatar/nv003.png'),
('NV004', 'nv004', N'Phạm Thị Dung', 'dung.pham@giay.com', '123456', '0901234567', '079094000004', N'Nữ', '1994-12-01', N'67 Nguyễn Lương Bằng, Quận 7, Hồ Chí Minh', 2, 1, N'/assets/avatar/nv004.png'),
('NV005', 'nv005', N'Hoàng Minh Đức', 'duc.hoang@giay.com', '123456', '0934567890', '001088000005', N'Nam', '1988-07-07', N'161 Thái Hà, Đống Đa, Hà Nội', 1, 1, N'/assets/avatar/nv005.png'),
('NV006', 'nv006', N'Vũ Thị Hương', 'huong.vu@giay.com', '123456', '0918765432', '092096000006', N'Nữ', '1996-03-25', N'89 Võ Văn Tần, Ninh Kiều, Cần Thơ', 2, 1, N'/assets/avatar/nv006.png'),
('NV007', 'nv007', N'Đặng Văn Khải', 'khai.dang@giay.com', '123456', '0945678901', '001091000007', N'Nam', '1991-10-18', N'14 Phan Tây Nhạc, Nam Từ Liêm, Hà Nội', 3, 1, N'/assets/avatar/nv007.png'),
('NV008', 'nv008', N'Bùi Ngọc Linh', 'linh.bui@giay.com', '123456', '0963456789', '031097000008', N'Nữ', '1997-04-09', N'23 Lạch Tray, Ngô Quyền, Hải Phòng', 2, 1, N'/assets/avatar/nv008.png'),
('NV009', 'nv009', N'Đỗ Quốc Minh', 'minh.do@giay.com', '123456', '0923456789', '074089000009', N'Nam', '1989-08-15', N'10 Hùng Vương, Nha Trang, Khánh Hòa', 2, 1, N'/assets/avatar/nv009.png'),
('NV010', 'nv010', N'Ngô Thanh Tâm', 'tam.ngo@giay.com', '123456', '0956789012', '001095000010', N'Nữ', '1995-11-29', N'5 Lê Lợi, Huế, Thừa Thiên Huế', 3, 1, N'/assets/avatar/nv010.png');
GO

INSERT INTO khach_hang
(ten_dang_nhap, ho_ten, email, sdt, ngay_sinh, gioi_tinh, hinh_anh, mat_khau, trang_thai)
VALUES
('khach1', N'Nguyễn Thị Lan', 'lan.nguyen@gmail.com', '0911111111', '1995-03-15', 0, N'/assets/avatar/kh001.png', '123456', 1),
('khach2', N'Trần Văn Hải', 'hai.tran@yahoo.com', '0988888888', '1998-07-20', 1, N'/assets/avatar/kh002.png', '123456', 1),
('khach3', N'Lê Thị Mai', 'mai.le@hotmail.com', '0977777777', '2000-11-05', 0, N'/assets/avatar/kh003.png', '123456', 1),
('khach4', N'Phạm Minh Quân', 'quan.pham@gmail.com', '0909999999', '1997-01-30', 1, N'/assets/avatar/kh004.png', '123456', 1),
('khach5', N'Hoàng Thị Ngọc', 'ngoc.hoang@gmail.com', '0933333333', '1996-09-12', 0, N'/assets/avatar/kh005.png', '123456', 1),
('khach6', N'Vũ Văn Long', 'long.vu@yahoo.com', '0912222222', '1999-04-18', 1, N'/assets/avatar/kh006.png', '123456', 1),
('khach7', N'Đặng Thị Hạnh', 'hanh.dang@gmail.com', '0944444444', '2001-12-25', 0, N'/assets/avatar/kh007.png', '123456', 1),
('khach8', N'Bùi Anh Tuấn', 'tuan.bui@gmail.com', '0966666666', '1994-06-03', 1, N'/assets/avatar/kh008.png', '123456', 1),
('khach9', N'Đỗ Khánh Vy', 'vy.do@gmail.com', '0922223333', '2002-02-14', 0, N'/assets/avatar/kh009.png', '123456', 1),
('khach10', N'Ngô Gia Bảo', 'bao.ngo@gmail.com', '0955555555', '1993-10-10', 1, N'/assets/avatar/kh010.png', '123456', 1);
GO

INSERT INTO dia_chi_khach_hang
(khach_hang_id, ho_ten, sdt, tinh_thanh, quan_huyen, phuong_xa, dia_chi_cu_the, la_mac_dinh, trang_thai)
VALUES
((SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach1'), N'Nguyễn Thị Lan', '0911111111', N'Hà Nội', N'Hoàn Kiếm', N'Phường Hàng Đào', N'12 Hàng Đào', 1, 1),
((SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach2'), N'Trần Văn Hải', '0988888888', N'Hồ Chí Minh', N'Quận 1', N'Phường Bến Nghé', N'45 Nguyễn Huệ', 1, 1),
((SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach3'), N'Lê Thị Mai', '0977777777', N'Đà Nẵng', N'Hải Châu', N'Phường Hải Châu 1', N'78 Trần Phú', 1, 1),
((SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach4'), N'Phạm Minh Quân', '0909999999', N'Hà Nội', N'Cầu Giấy', N'Phường Yên Hòa', N'123 Xuân Thủy', 1, 1),
((SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach5'), N'Hoàng Thị Ngọc', '0933333333', N'Hồ Chí Minh', N'Quận 7', N'Phường Tân Phong', N'67 Nguyễn Lương Bằng', 1, 1),
((SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach6'), N'Vũ Văn Long', '0912222222', N'Cần Thơ', N'Ninh Kiều', N'Phường An Bình', N'89 Võ Văn Tần', 1, 1),
((SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach7'), N'Đặng Thị Hạnh', '0944444444', N'Hà Nội', N'Ba Đình', N'Phường Trúc Bạch', N'34 Trúc Bạch', 1, 1),
((SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach8'), N'Bùi Anh Tuấn', '0966666666', N'Hải Phòng', N'Ngô Quyền', N'Phường Máy Tơ', N'23 Lạch Tray', 1, 1),
((SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach9'), N'Đỗ Khánh Vy', '0922223333', N'Khánh Hòa', N'Nha Trang', N'Phường Lộc Thọ', N'10 Hùng Vương', 1, 1),
((SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach10'), N'Ngô Gia Bảo', '0955555555', N'Thừa Thiên Huế', N'Huế', N'Phường Phú Hội', N'5 Lê Lợi', 1, 1);
GO

INSERT INTO kich_co (gia_tri, ghi_chu, trang_thai)
VALUES
(N'36', N'Size nữ nhỏ', 1),
(N'37', N'Size nữ phổ biến', 1),
(N'38', N'Size nữ và unisex', 1),
(N'39', N'Size unisex phổ biến', 1),
(N'40', N'Size unisex phổ biến', 1),
(N'41', N'Size nam phổ biến', 1),
(N'42', N'Size nam phổ biến', 1),
(N'43', N'Size nam lớn', 1),
(N'44', N'Size nam lớn', 1),
(N'45', N'Size nam rất lớn', 1);
GO

INSERT INTO mau_sac (ma, ten, ma_mau_hex, trang_thai)
VALUES
(N'MS001', N'Trắng', '#FFFFFF', 1),
(N'MS002', N'Đen', '#000000', 1),
(N'MS003', N'Đỏ', '#D71920', 1),
(N'MS004', N'Xanh Navy', '#001F3F', 1),
(N'MS005', N'Xám', '#808080', 1),
(N'MS006', N'Kem', '#F5E6C8', 1),
(N'MS007', N'Xanh Lá', '#15803D', 1),
(N'MS008', N'Hồng', '#F9A8D4', 1),
(N'MS009', N'Cam', '#F97316', 1),
(N'MS010', N'Nâu', '#7C2D12', 1);
GO

INSERT INTO thuong_hieu (ma, ten, xuat_xu, mo_ta, logo_url, website, trang_thai)
VALUES
(N'TH001', N'Nike', N'Mỹ', N'Thương hiệu giày thể thao hiệu năng và lifestyle.', N'/assets/brand/nike.png', N'https://www.nike.com', 1),
(N'TH002', N'Adidas', N'Đức', N'Thương hiệu thể thao với các dòng chạy bộ và thời trang.', N'/assets/brand/adidas.png', N'https://www.adidas.com', 1),
(N'TH003', N'Converse', N'Mỹ', N'Dòng giày canvas cổ điển, dễ phối đồ.', N'/assets/brand/converse.png', N'https://www.converse.com', 1),
(N'TH004', N'Vans', N'Mỹ', N'Giày skateboarding và streetwear.', N'/assets/brand/vans.png', N'https://www.vans.com', 1),
(N'TH005', N'Puma', N'Đức', N'Giày thể thao phong cách trẻ trung.', N'/assets/brand/puma.png', N'https://www.puma.com', 1),
(N'TH006', N'New Balance', N'Mỹ', N'Giày lifestyle và running chú trọng độ êm.', N'/assets/brand/new-balance.png', N'https://www.newbalance.com', 1),
(N'TH007', N'Asics', N'Nhật Bản', N'Giày chạy bộ ổn định, hỗ trợ chuyển động.', N'/assets/brand/asics.png', N'https://www.asics.com', 1),
(N'TH008', N'Skechers', N'Mỹ', N'Giày đi bộ nhẹ, êm và tiện dụng.', N'/assets/brand/skechers.png', N'https://www.skechers.com', 1),
(N'TH009', N'Biti''s', N'Việt Nam', N'Thương hiệu Việt với dòng Hunter nổi bật.', N'/assets/brand/bitis.png', N'https://bitis.com.vn', 1),
(N'TH010', N'Ananas', N'Việt Nam', N'Giày sneaker Việt Nam phong cách tối giản.', N'/assets/brand/ananas.png', N'https://ananas.vn', 1);
GO

INSERT INTO loai_giay (ma, ten, mo_ta, trang_thai)
VALUES
(N'LG001', N'Lifestyle', N'Giày thời trang hằng ngày.', 1),
(N'LG002', N'Running', N'Giày chạy bộ.', 1),
(N'LG003', N'Basketball', N'Giày bóng rổ.', 1),
(N'LG004', N'Skateboarding', N'Giày trượt ván.', 1),
(N'LG005', N'Training', N'Giày tập luyện đa năng.', 1),
(N'LG006', N'Walking', N'Giày đi bộ.', 1),
(N'LG007', N'Tennis', N'Giày tennis.', 1),
(N'LG008', N'Football', N'Giày bóng đá.', 1),
(N'LG009', N'Sandal Sport', N'Sandal thể thao.', 1),
(N'LG010', N'Kids Sport', N'Giày thể thao trẻ em.', 1);
GO

INSERT INTO de_giay (ma, ten, mo_ta, trang_thai)
VALUES
(N'DG001', N'Cao su lưu hóa', N'Đế cao su bền và bám tốt.', 1),
(N'DG002', N'EVA nhẹ', N'Đế EVA nhẹ, hấp thụ lực.', 1),
(N'DG003', N'Air Cushion', N'Đế có túi khí hỗ trợ đàn hồi.', 1),
(N'DG004', N'Boost Foam', N'Đế foam hoàn trả năng lượng.', 1),
(N'DG005', N'Gel Cushion', N'Đế gel hỗ trợ chạy bộ.', 1),
(N'DG006', N'Cupsole', N'Đế cupsole chắc chắn cho skate.', 1),
(N'DG007', N'Phylon', N'Đế Phylon cân bằng trọng lượng và độ êm.', 1),
(N'DG008', N'Rubber Traction', N'Đế bám sân tốt.', 1),
(N'DG009', N'Cloud Foam', N'Đế foam mềm cho đi bộ.', 1),
(N'DG010', N'OrthoLite', N'Đế lót kháng khuẩn, thoáng khí.', 1);
GO

INSERT INTO co_giay (ma, ten, mo_ta, trang_thai)
VALUES
(N'CG001', N'Cổ thấp', N'Linh hoạt, dễ phối đồ.', 1),
(N'CG002', N'Cổ trung', N'Hỗ trợ cổ chân vừa phải.', 1),
(N'CG003', N'Cổ cao', N'Bảo vệ cổ chân tốt.', 1),
(N'CG004', N'Slip-on', N'Dễ mang tháo, không dây.', 1),
(N'CG005', N'Sandal', N'Thoáng chân, dùng ngoài trời.', 1),
(N'CG006', N'Boot thể thao', N'Cứng cáp, cá tính.', 1),
(N'CG007', N'Cổ chun', N'Ôm chân, tiện đi bộ.', 1),
(N'CG008', N'Cổ knit', N'Co giãn, thoáng khí.', 1),
(N'CG009', N'Cổ padded', N'Đệm quanh cổ chân.', 1),
(N'CG010', N'Cổ classic', N'Dáng sneaker truyền thống.', 1);
GO

INSERT INTO chat_lieu_giay (ma, ten, mo_ta, trang_thai)
VALUES
(N'CL001', N'Da tổng hợp', N'Dễ vệ sinh, giữ form tốt.', 1),
(N'CL002', N'Da thật', N'Bền, cao cấp và mềm sau thời gian sử dụng.', 1),
(N'CL003', N'Canvas', N'Nhẹ, thoáng, hợp sneaker cổ điển.', 1),
(N'CL004', N'Mesh', N'Thoáng khí cho chạy bộ.', 1),
(N'CL005', N'Knit', N'Co giãn và ôm chân.', 1),
(N'CL006', N'Suede', N'Bề mặt nhung, phong cách lifestyle.', 1),
(N'CL007', N'Nylon', N'Nhẹ và bền.', 1),
(N'CL008', N'TPU', N'Gia cường ổn định thân giày.', 1),
(N'CL009', N'Vải dệt', N'Mềm, thoáng và thân thiện.', 1),
(N'CL010', N'Rubber Upper', N'Chống nước nhẹ, dễ làm sạch.', 1);
GO

INSERT INTO trong_luong (ma, gia_tri, mo_ta, trang_thai)
VALUES
(N'TL001', 220, N'Rất nhẹ cho đi bộ.', 1),
(N'TL002', 250, N'Nhẹ cho chạy bộ.', 1),
(N'TL003', 280, N'Nhẹ vừa.', 1),
(N'TL004', 310, N'Cân bằng.', 1),
(N'TL005', 340, N'Đầm chân.', 1),
(N'TL006', 370, N'Chắc chắn.', 1),
(N'TL007', 400, N'Dành cho cổ cao.', 1),
(N'TL008', 430, N'Đế dày.', 1),
(N'TL009', 460, N'Bóng rổ/training.', 1),
(N'TL010', 500, N'Phiên bản bền chắc.', 1);
GO

INSERT INTO cong_nghe_dem (ma, ten, mo_ta, trang_thai)
VALUES
(N'CND001', N'Air Max', N'Túi khí giảm chấn.', 1),
(N'CND002', N'Boost', N'Hoàn trả năng lượng tốt.', 1),
(N'CND003', N'Zoom Air', N'Phản hồi nhanh.', 1),
(N'CND004', N'Gel', N'Ổn định và giảm chấn khi chạy.', 1),
(N'CND005', N'Cloudfoam', N'Mềm mại cho đi bộ.', 1),
(N'CND006', N'Fresh Foam', N'Êm và nhẹ.', 1),
(N'CND007', N'React', N'Đàn hồi bền bỉ.', 1),
(N'CND008', N'ComfyCush', N'Êm cho skate/lifestyle.', 1),
(N'CND009', N'LiteRide', N'Nhẹ, mềm và linh hoạt.', 1),
(N'CND010', N'Standard EVA', N'Đệm EVA tiêu chuẩn.', 1);
GO

INSERT INTO dot_giam_gia
(ma, ten, mo_ta, loai_giam, gia_tri_giam, ngay_bat_dau, ngay_ket_thuc, kich_hoat)
VALUES
(N'DGG001', N'Summer Sneaker 10%', N'Giảm 10% cho giày lifestyle mùa hè.', 1, 10, '2026-05-01', '2026-06-30', 1),
(N'DGG002', N'Clearance Tháng 3', N'Đợt giảm đã hết hạn.', 1, 15, '2026-03-01', '2026-03-31', 2),
(N'DGG003', N'Back To School', N'Đợt giảm sắp bắt đầu.', 1, 12, '2026-06-01', '2026-08-31', 4),
(N'DGG004', N'Flash Sale 20%', N'Giảm 20% cho sản phẩm Flash Sale.', 1, 20, '2026-05-10', '2026-05-20', 1),
(N'DGG005', N'Running Week', N'Giảm 8% cho giày chạy bộ.', 1, 8, '2026-05-05', '2026-05-25', 1),
(N'DGG006', N'Skate Day 10%', N'Giảm 10% cho giày skate.', 1, 10, '2026-05-01', '2026-05-31', 1),
(N'DGG007', N'VIP Lifestyle', N'Đợt tạm dừng thủ công.', 1, 20, '2026-05-01', '2026-12-31', 0),
(N'DGG008', N'Women Sport', N'Giảm 7% cho sản phẩm nữ.', 1, 7, '2026-05-12', '2026-07-12', 1),
(N'DGG009', N'Local Brand Deal 25%', N'Ưu đãi thương hiệu Việt giảm 25%.', 1, 25, '2026-05-01', '2026-06-15', 1),
(N'DGG010', N'End Month Sale', N'Đợt giảm cuối tháng.', 1, 5, '2026-05-20', '2026-05-31', 4);
GO

INSERT INTO giay
(ma, ten, thuong_hieu_id, loai_giay_id, gioi_tinh, chat_lieu, mo_ta, trang_thai)
VALUES
(N'G001', N'Nike Air Force 1 Low White', (SELECT id FROM thuong_hieu WHERE ma = N'TH001'), (SELECT id FROM loai_giay WHERE ma = N'LG001'), 3, N'Da tổng hợp', N'Sneaker trắng biểu tượng, dễ phối đồ.', 1),
(N'G002', N'Adidas Ultraboost Light', (SELECT id FROM thuong_hieu WHERE ma = N'TH002'), (SELECT id FROM loai_giay WHERE ma = N'LG002'), 3, N'Mesh', N'Giày chạy bộ nhẹ, đệm Boost êm.', 1),
(N'G003', N'Converse Chuck 70 High', (SELECT id FROM thuong_hieu WHERE ma = N'TH003'), (SELECT id FROM loai_giay WHERE ma = N'LG001'), 3, N'Canvas', N'Phiên bản cổ cao kinh điển.', 1),
(N'G004', N'Vans Old Skool Black White', (SELECT id FROM thuong_hieu WHERE ma = N'TH004'), (SELECT id FROM loai_giay WHERE ma = N'LG004'), 3, N'Canvas/Suede', N'Giày skate cổ điển với sọc jazz.', 1),
(N'G005', N'Puma RS-X Reinvention', (SELECT id FROM thuong_hieu WHERE ma = N'TH005'), (SELECT id FROM loai_giay WHERE ma = N'LG001'), 1, N'Mesh/Da tổng hợp', N'Sneaker chunky nổi bật.', 1),
(N'G006', N'New Balance 574 Core', (SELECT id FROM thuong_hieu WHERE ma = N'TH006'), (SELECT id FROM loai_giay WHERE ma = N'LG001'), 3, N'Suede/Mesh', N'Dòng lifestyle êm và bền.', 1),
(N'G007', N'Asics Gel-Kayano 30', (SELECT id FROM thuong_hieu WHERE ma = N'TH007'), (SELECT id FROM loai_giay WHERE ma = N'LG002'), 1, N'Mesh', N'Giày chạy bộ ổn định cho cự ly dài.', 1),
(N'G008', N'Skechers Go Walk 6', (SELECT id FROM thuong_hieu WHERE ma = N'TH008'), (SELECT id FROM loai_giay WHERE ma = N'LG006'), 2, N'Knit', N'Giày đi bộ nhẹ và êm.', 1),
(N'G009', N'Biti''s Hunter X Midnight', (SELECT id FROM thuong_hieu WHERE ma = N'TH009'), (SELECT id FROM loai_giay WHERE ma = N'LG001'), 3, N'Vải dệt', N'Sneaker Việt Nam năng động.', 1),
(N'G010', N'Ananas Basas Bumper Gum', (SELECT id FROM thuong_hieu WHERE ma = N'TH010'), (SELECT id FROM loai_giay WHERE ma = N'LG001'), 3, N'Canvas', N'Sneaker tối giản, đế gum.', 1);
GO

INSERT INTO giay_thuoc_tinh
(giay_id, de_giay_id, co_giay_id, chat_lieu_giay_id, trong_luong_id, cong_nghe_dem_id, trang_thai)
VALUES
((SELECT id FROM giay WHERE ma = N'G001'), (SELECT id FROM de_giay WHERE ma = N'DG003'), (SELECT id FROM co_giay WHERE ma = N'CG001'), (SELECT id FROM chat_lieu_giay WHERE ma = N'CL001'), (SELECT id FROM trong_luong WHERE ma = N'TL005'), (SELECT id FROM cong_nghe_dem WHERE ma = N'CND001'), 1),
((SELECT id FROM giay WHERE ma = N'G002'), (SELECT id FROM de_giay WHERE ma = N'DG004'), (SELECT id FROM co_giay WHERE ma = N'CG008'), (SELECT id FROM chat_lieu_giay WHERE ma = N'CL004'), (SELECT id FROM trong_luong WHERE ma = N'TL003'), (SELECT id FROM cong_nghe_dem WHERE ma = N'CND002'), 1),
((SELECT id FROM giay WHERE ma = N'G003'), (SELECT id FROM de_giay WHERE ma = N'DG001'), (SELECT id FROM co_giay WHERE ma = N'CG003'), (SELECT id FROM chat_lieu_giay WHERE ma = N'CL003'), (SELECT id FROM trong_luong WHERE ma = N'TL006'), (SELECT id FROM cong_nghe_dem WHERE ma = N'CND010'), 1),
((SELECT id FROM giay WHERE ma = N'G004'), (SELECT id FROM de_giay WHERE ma = N'DG006'), (SELECT id FROM co_giay WHERE ma = N'CG001'), (SELECT id FROM chat_lieu_giay WHERE ma = N'CL006'), (SELECT id FROM trong_luong WHERE ma = N'TL005'), (SELECT id FROM cong_nghe_dem WHERE ma = N'CND008'), 1),
((SELECT id FROM giay WHERE ma = N'G005'), (SELECT id FROM de_giay WHERE ma = N'DG007'), (SELECT id FROM co_giay WHERE ma = N'CG009'), (SELECT id FROM chat_lieu_giay WHERE ma = N'CL001'), (SELECT id FROM trong_luong WHERE ma = N'TL007'), (SELECT id FROM cong_nghe_dem WHERE ma = N'CND010'), 1),
((SELECT id FROM giay WHERE ma = N'G006'), (SELECT id FROM de_giay WHERE ma = N'DG002'), (SELECT id FROM co_giay WHERE ma = N'CG001'), (SELECT id FROM chat_lieu_giay WHERE ma = N'CL006'), (SELECT id FROM trong_luong WHERE ma = N'TL004'), (SELECT id FROM cong_nghe_dem WHERE ma = N'CND006'), 1),
((SELECT id FROM giay WHERE ma = N'G007'), (SELECT id FROM de_giay WHERE ma = N'DG005'), (SELECT id FROM co_giay WHERE ma = N'CG008'), (SELECT id FROM chat_lieu_giay WHERE ma = N'CL004'), (SELECT id FROM trong_luong WHERE ma = N'TL004'), (SELECT id FROM cong_nghe_dem WHERE ma = N'CND004'), 1),
((SELECT id FROM giay WHERE ma = N'G008'), (SELECT id FROM de_giay WHERE ma = N'DG009'), (SELECT id FROM co_giay WHERE ma = N'CG004'), (SELECT id FROM chat_lieu_giay WHERE ma = N'CL005'), (SELECT id FROM trong_luong WHERE ma = N'TL002'), (SELECT id FROM cong_nghe_dem WHERE ma = N'CND005'), 1),
((SELECT id FROM giay WHERE ma = N'G009'), (SELECT id FROM de_giay WHERE ma = N'DG002'), (SELECT id FROM co_giay WHERE ma = N'CG002'), (SELECT id FROM chat_lieu_giay WHERE ma = N'CL009'), (SELECT id FROM trong_luong WHERE ma = N'TL004'), (SELECT id FROM cong_nghe_dem WHERE ma = N'CND010'), 1),
((SELECT id FROM giay WHERE ma = N'G010'), (SELECT id FROM de_giay WHERE ma = N'DG001'), (SELECT id FROM co_giay WHERE ma = N'CG001'), (SELECT id FROM chat_lieu_giay WHERE ma = N'CL003'), (SELECT id FROM trong_luong WHERE ma = N'TL005'), (SELECT id FROM cong_nghe_dem WHERE ma = N'CND010'), 1);
GO

INSERT INTO giay_chi_tiet
(giay_id, ma_bien_the, mau_sac_id, kich_co_id, so_luong, gia_goc, gia_ban, sku, kich_hoat)
VALUES
((SELECT id FROM giay WHERE ma = N'G001'), N'GCT001', (SELECT id FROM mau_sac WHERE ma = N'MS001'), (SELECT id FROM kich_co WHERE gia_tri = N'40'), 50, 1350000, 1690000, N'NIKE-AF1-WHT-40', 1),
((SELECT id FROM giay WHERE ma = N'G002'), N'GCT002', (SELECT id FROM mau_sac WHERE ma = N'MS002'), (SELECT id FROM kich_co WHERE gia_tri = N'41'), 35, 3600000, 4290000, N'ADIDAS-UB-LGT-41', 1),
((SELECT id FROM giay WHERE ma = N'G003'), N'GCT003', (SELECT id FROM mau_sac WHERE ma = N'MS006'), (SELECT id FROM kich_co WHERE gia_tri = N'39'), 40, 1250000, 1690000, N'CONVERSE-C70-CREAM-39', 1),
((SELECT id FROM giay WHERE ma = N'G004'), N'GCT004', (SELECT id FROM mau_sac WHERE ma = N'MS002'), (SELECT id FROM kich_co WHERE gia_tri = N'42'), 45, 1180000, 1650000, N'VANS-OS-BLK-42', 1),
((SELECT id FROM giay WHERE ma = N'G005'), N'GCT005', (SELECT id FROM mau_sac WHERE ma = N'MS003'), (SELECT id FROM kich_co WHERE gia_tri = N'43'), 25, 1750000, 2250000, N'PUMA-RSX-RED-43', 1),
((SELECT id FROM giay WHERE ma = N'G006'), N'GCT006', (SELECT id FROM mau_sac WHERE ma = N'MS005'), (SELECT id FROM kich_co WHERE gia_tri = N'40'), 30, 1650000, 2190000, N'NB-574-GRY-40', 1),
((SELECT id FROM giay WHERE ma = N'G007'), N'GCT007', (SELECT id FROM mau_sac WHERE ma = N'MS004'), (SELECT id FROM kich_co WHERE gia_tri = N'42'), 20, 2950000, 3590000, N'ASICS-KAYANO-NAVY-42', 1),
((SELECT id FROM giay WHERE ma = N'G008'), N'GCT008', (SELECT id FROM mau_sac WHERE ma = N'MS008'), (SELECT id FROM kich_co WHERE gia_tri = N'37'), 38, 1290000, 1790000, N'SKECHERS-GOWALK-PINK-37', 1),
((SELECT id FROM giay WHERE ma = N'G009'), N'GCT009', (SELECT id FROM mau_sac WHERE ma = N'MS002'), (SELECT id FROM kich_co WHERE gia_tri = N'41'), 60, 890000, 1290000, N'BITIS-HUNTER-BLK-41', 1),
((SELECT id FROM giay WHERE ma = N'G010'), N'GCT010', (SELECT id FROM mau_sac WHERE ma = N'MS010'), (SELECT id FROM kich_co WHERE gia_tri = N'40'), 55, 620000, 890000, N'ANANAS-BASAS-GUM-40', 1);
GO

INSERT INTO hinh_anh_giay
(giay_chi_tiet_id, loai_hinh, url, mo_ta, la_hinh_chinh, trang_thai)
VALUES
((SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT001'), 1, N'/assets/products/nike-air-force-1-white.png', N'Ảnh chính Nike Air Force 1 trắng', 1, 1),
((SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT002'), 1, N'/assets/products/adidas-ultraboost-light-black.png', N'Ảnh chính Adidas Ultraboost Light', 1, 1),
((SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT003'), 1, N'/assets/products/converse-chuck-70-cream.png', N'Ảnh chính Converse Chuck 70', 1, 1),
((SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT004'), 1, N'/assets/products/vans-old-skool-black.png', N'Ảnh chính Vans Old Skool', 1, 1),
((SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT005'), 1, N'/assets/products/puma-rsx-red.png', N'Ảnh chính Puma RS-X', 1, 1),
((SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT006'), 1, N'/assets/products/new-balance-574-grey.png', N'Ảnh chính New Balance 574', 1, 1),
((SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT007'), 1, N'/assets/products/asics-gel-kayano-navy.png', N'Ảnh chính Asics Gel-Kayano', 1, 1),
((SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT008'), 1, N'/assets/products/skechers-go-walk-pink.png', N'Ảnh chính Skechers Go Walk', 1, 1),
((SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT009'), 1, N'/assets/products/bitis-hunter-x-black.png', N'Ảnh chính Biti''s Hunter X', 1, 1),
((SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT010'), 1, N'/assets/products/ananas-basas-gum.png', N'Ảnh chính Ananas Basas', 1, 1);
GO

INSERT INTO dot_giam_gia_san_pham
(dot_giam_gia_id, giay_chi_tiet_id, trang_thai)
VALUES
((SELECT id FROM dot_giam_gia WHERE ma = N'DGG001'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT001'), 1),
((SELECT id FROM dot_giam_gia WHERE ma = N'DGG005'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT002'), 1),
((SELECT id FROM dot_giam_gia WHERE ma = N'DGG003'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT003'), 1),
((SELECT id FROM dot_giam_gia WHERE ma = N'DGG006'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT004'), 1),
((SELECT id FROM dot_giam_gia WHERE ma = N'DGG004'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT005'), 1),
((SELECT id FROM dot_giam_gia WHERE ma = N'DGG001'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT006'), 1),
((SELECT id FROM dot_giam_gia WHERE ma = N'DGG005'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT007'), 1),
((SELECT id FROM dot_giam_gia WHERE ma = N'DGG008'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT008'), 1),
((SELECT id FROM dot_giam_gia WHERE ma = N'DGG009'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT009'), 1),
((SELECT id FROM dot_giam_gia WHERE ma = N'DGG010'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT010'), 1);
GO

INSERT INTO phieu_giam_gia
(ma, ten, loai, loai_phieu, gia_tri, gia_tri_toi_thieu, giam_toi_da, ngay_bat_dau, ngay_ket_thuc, so_luong, so_luong_da_dung, trang_thai)
VALUES
(N'PGG001', N'Giảm 10% đơn đầu', 1, 1, 10, 500000, 300000, '2026-05-01', '2026-06-30', 100, 7, 1),
(N'PGG002', N'Giảm 200K đơn từ 2 triệu', 2, 1, 200000, 2000000, 200000, '2026-05-01', '2026-06-15', 80, 12, 1),
(N'PGG003', N'Miễn phí ship nội thành', 3, 1, 0, 300000, 50000, '2026-05-01', '2026-12-31', 200, 30, 1),
(N'PGG004', N'Giảm 15% thành viên', 1, 2, 15, 1000000, 500000, '2026-05-10', '2026-07-10', 50, 8, 1),
(N'PGG005', N'Giảm 150K sneaker Việt', 2, 1, 150000, 1000000, 150000, '2026-05-05', '2026-06-05', 60, 9, 1),
(N'PGG006', N'Voucher đã hết hạn', 1, 1, 20, 1000000, 400000, '2026-03-01', '2026-03-31', 40, 18, 2),
(N'PGG007', N'Voucher hết lượt dùng', 2, 2, 100000, 700000, 100000, '2026-05-01', '2026-07-01', 10, 10, 3),
(N'PGG008', N'Voucher sắp diễn ra', 1, 1, 12, 800000, 250000, '2026-06-01', '2026-08-01', 120, 0, 4),
(N'PGG009', N'Voucher tạm ngưng', 2, 2, 250000, 2500000, 250000, '2026-05-01', '2026-12-31', 30, 1, 0),
(N'PGG010', N'Giảm 5% đơn nhỏ', 1, 1, 5, 300000, 100000, '2026-05-01', '2026-05-31', 150, 21, 1);
GO

INSERT INTO phieu_giam_gia_khach_hang
(phieu_giam_gia_id, khach_hang_id, ngay_su_dung, trang_thai)
VALUES
((SELECT id FROM phieu_giam_gia WHERE ma = N'PGG004'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach1'), NULL, 1),
((SELECT id FROM phieu_giam_gia WHERE ma = N'PGG004'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach2'), NULL, 1),
((SELECT id FROM phieu_giam_gia WHERE ma = N'PGG004'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach3'), '2026-05-03T10:30:00', 0),
((SELECT id FROM phieu_giam_gia WHERE ma = N'PGG007'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach4'), NULL, 3),
((SELECT id FROM phieu_giam_gia WHERE ma = N'PGG008'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach5'), NULL, 4),
((SELECT id FROM phieu_giam_gia WHERE ma = N'PGG009'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach6'), NULL, 0),
((SELECT id FROM phieu_giam_gia WHERE ma = N'PGG004'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach7'), NULL, 1),
((SELECT id FROM phieu_giam_gia WHERE ma = N'PGG006'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach8'), NULL, 2),
((SELECT id FROM phieu_giam_gia WHERE ma = N'PGG004'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach9'), NULL, 1),
((SELECT id FROM phieu_giam_gia WHERE ma = N'PGG009'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach10'), NULL, 0);
GO

INSERT INTO hoa_don
(ma, kenh_ban, khach_hang_id, nhan_vien_id, phieu_giam_gia_id, ten_nguoi_nhan, sdt_nguoi_nhan, dia_chi_giao_hang, ngay_lap, ngay_thanh_toan, trang_thai, tong_tien_hang, tien_giam, tong_tien_thanh_toan, ghi_chu, ngay_tao, ngay_cap_nhat)
VALUES
(N'HD001', 2, (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach1'), (SELECT id FROM nhan_vien WHERE ma = 'NV002'), (SELECT id FROM phieu_giam_gia WHERE ma = N'PGG001'), N'Nguyễn Thị Lan', '0911111111', N'12 Hàng Đào, Hoàn Kiếm, Hà Nội', '2026-05-01T08:15:00', NULL, 1, 1690000, 100000, 1620000, N'Đơn online chờ xác nhận', '2026-05-01T08:15:00', NULL),
(N'HD002', 1, (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach2'), (SELECT id FROM nhan_vien WHERE ma = 'NV003'), (SELECT id FROM phieu_giam_gia WHERE ma = N'PGG002'), N'Trần Văn Hải', '0988888888', N'45 Nguyễn Huệ, Quận 1, Hồ Chí Minh', '2026-05-02T09:20:00', '2026-05-02T09:25:00', 2, 4290000, 200000, 4125000, N'Đơn tại quầy đã thanh toán', '2026-05-02T09:20:00', '2026-05-02T09:25:00'),
(N'HD003', 2, (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach3'), (SELECT id FROM nhan_vien WHERE ma = 'NV004'), (SELECT id FROM phieu_giam_gia WHERE ma = N'PGG004'), N'Lê Thị Mai', '0977777777', N'78 Trần Phú, Hải Châu, Đà Nẵng', '2026-05-03T10:30:00', '2026-05-03T10:35:00', 3, 3380000, 150000, 3270000, N'Đang vận chuyển GHN', '2026-05-03T10:30:00', '2026-05-03T14:00:00'),
(N'HD004', 2, (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach4'), (SELECT id FROM nhan_vien WHERE ma = 'NV002'), NULL, N'Phạm Minh Quân', '0909999999', N'123 Xuân Thủy, Cầu Giấy, Hà Nội', '2026-05-04T11:45:00', '2026-05-04T11:50:00', 4, 1650000, 0, 1680000, N'Đã giao hàng', '2026-05-04T11:45:00', '2026-05-05T16:20:00'),
(N'HD005', 1, (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach5'), (SELECT id FROM nhan_vien WHERE ma = 'NV005'), (SELECT id FROM phieu_giam_gia WHERE ma = N'PGG002'), N'Hoàng Thị Ngọc', '0933333333', N'67 Nguyễn Lương Bằng, Quận 7, Hồ Chí Minh', '2026-05-05T13:05:00', '2026-05-05T13:07:00', 5, 2250000, 250000, 2035000, N'Đơn hoàn thành', '2026-05-05T13:05:00', '2026-05-06T17:00:00'),
(N'HD006', 2, (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach6'), (SELECT id FROM nhan_vien WHERE ma = 'NV006'), NULL, N'Vũ Văn Long', '0912222222', N'89 Võ Văn Tần, Ninh Kiều, Cần Thơ', '2026-05-06T14:10:00', NULL, 6, 2190000, 0, 2190000, N'Khách hủy trước khi giao', '2026-05-06T14:10:00', '2026-05-06T14:35:00'),
(N'HD007', 2, (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach7'), (SELECT id FROM nhan_vien WHERE ma = 'NV003'), (SELECT id FROM phieu_giam_gia WHERE ma = N'PGG001'), N'Đặng Thị Hạnh', '0944444444', N'34 Trúc Bạch, Ba Đình, Hà Nội', '2026-05-07T15:20:00', '2026-05-07T15:25:00', 7, 3590000, 300000, 3335000, N'Khách yêu cầu hủy, chờ xác nhận', '2026-05-07T15:20:00', '2026-05-07T16:00:00'),
(N'HD008', 2, (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach8'), (SELECT id FROM nhan_vien WHERE ma = 'NV004'), (SELECT id FROM phieu_giam_gia WHERE ma = N'PGG002'), N'Bùi Anh Tuấn', '0966666666', N'23 Lạch Tray, Ngô Quyền, Hải Phòng', '2026-05-08T16:30:00', '2026-05-08T16:35:00', 5, 3580000, 200000, 3410000, N'Cần hoàn tiền do trả hàng', '2026-05-08T16:30:00', '2026-05-09T09:30:00'),
(N'HD009', 1, (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach9'), (SELECT id FROM nhan_vien WHERE ma = 'NV005'), (SELECT id FROM phieu_giam_gia WHERE ma = N'PGG010'), N'Đỗ Khánh Vy', '0922223333', N'10 Hùng Vương, Nha Trang, Khánh Hòa', '2026-05-09T17:40:00', '2026-05-09T17:43:00', 9, 1290000, 50000, 1265000, N'Đã hoàn tiền sau trả hàng', '2026-05-09T17:40:00', '2026-05-10T10:15:00'),
(N'HD010', 2, (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach10'), (SELECT id FROM nhan_vien WHERE ma = 'NV002'), (SELECT id FROM phieu_giam_gia WHERE ma = N'PGG005'), N'Ngô Gia Bảo', '0955555555', N'5 Lê Lợi, Huế, Thừa Thiên Huế', '2026-05-10T19:00:00', '2026-05-10T19:05:00', 5, 1780000, 100000, 1710000, N'Đơn hoàn thành online', '2026-05-10T19:00:00', '2026-05-12T12:00:00');
GO

INSERT INTO hoa_don_chi_tiet
(hoa_don_id, giay_chi_tiet_id, so_luong, gia_don_vi, thanh_tien, trang_thai)
VALUES
((SELECT id FROM hoa_don WHERE ma = N'HD001'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT001'), 1, 1690000, 1690000, 1),
((SELECT id FROM hoa_don WHERE ma = N'HD002'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT002'), 1, 4290000, 4290000, 1),
((SELECT id FROM hoa_don WHERE ma = N'HD003'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT003'), 2, 1690000, 3380000, 1),
((SELECT id FROM hoa_don WHERE ma = N'HD004'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT004'), 1, 1650000, 1650000, 1),
((SELECT id FROM hoa_don WHERE ma = N'HD005'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT005'), 1, 2250000, 2250000, 1),
((SELECT id FROM hoa_don WHERE ma = N'HD006'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT006'), 1, 2190000, 2190000, 0),
((SELECT id FROM hoa_don WHERE ma = N'HD007'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT007'), 1, 3590000, 3590000, 1),
((SELECT id FROM hoa_don WHERE ma = N'HD008'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT008'), 2, 1790000, 3580000, 1),
((SELECT id FROM hoa_don WHERE ma = N'HD009'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT009'), 1, 1290000, 1290000, 1),
((SELECT id FROM hoa_don WHERE ma = N'HD010'), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT010'), 2, 890000, 1780000, 1);
GO

INSERT INTO lich_su_hoa_don
(hoa_don_id, nhan_vien_id, trang_thai, ghi_chu, ngay_tao)
VALUES
((SELECT id FROM hoa_don WHERE ma = N'HD001'), (SELECT id FROM nhan_vien WHERE ma = 'NV002'), N'Chờ xác nhận', N'Tạo đơn online từ website.', '2026-05-01T08:15:00'),
((SELECT id FROM hoa_don WHERE ma = N'HD002'), (SELECT id FROM nhan_vien WHERE ma = 'NV003'), N'Đang giao hàng', N'Đơn tại quầy đã thanh toán.', '2026-05-02T09:25:00'),
((SELECT id FROM hoa_don WHERE ma = N'HD003'), (SELECT id FROM nhan_vien WHERE ma = 'NV004'), N'Đang vận chuyển', N'Đẩy đơn sang GHN.', '2026-05-03T14:00:00'),
((SELECT id FROM hoa_don WHERE ma = N'HD004'), (SELECT id FROM nhan_vien WHERE ma = 'NV002'), N'Đã giao hàng', N'Khách đã nhận hàng.', '2026-05-05T16:20:00'),
((SELECT id FROM hoa_don WHERE ma = N'HD005'), (SELECT id FROM nhan_vien WHERE ma = 'NV005'), N'Hoàn thành', N'Đơn đã hoàn tất.', '2026-05-06T17:00:00'),
((SELECT id FROM hoa_don WHERE ma = N'HD006'), (SELECT id FROM nhan_vien WHERE ma = 'NV006'), N'Đã hủy', N'Khách hủy trước khi giao.', '2026-05-06T14:35:00'),
((SELECT id FROM hoa_don WHERE ma = N'HD007'), (SELECT id FROM nhan_vien WHERE ma = 'NV003'), N'Yêu cầu hủy', N'Khách yêu cầu hủy đơn.', '2026-05-07T16:00:00'),
((SELECT id FROM hoa_don WHERE ma = N'HD008'), (SELECT id FROM nhan_vien WHERE ma = 'NV004'), N'Hoàn thành', N'Khách trả hàng, giao dịch cần hoàn tiền.', '2026-05-09T09:30:00'),
((SELECT id FROM hoa_don WHERE ma = N'HD009'), (SELECT id FROM nhan_vien WHERE ma = 'NV005'), N'Đã hoàn tiền', N'Hoàn tiền thành công.', '2026-05-10T10:15:00'),
((SELECT id FROM hoa_don WHERE ma = N'HD010'), (SELECT id FROM nhan_vien WHERE ma = 'NV002'), N'Hoàn thành', N'Đơn hoàn thành online.', '2026-05-12T12:00:00');
GO

INSERT INTO van_chuyen
(hoa_don_id, don_vi_van_chuyen, ma_van_don, phi_van_chuyen, ngay_gui, ngay_du_kien, ngay_giao_that, trang_thai, ghi_chu)
VALUES
((SELECT id FROM hoa_don WHERE ma = N'HD001'), N'GHN', N'GHN20260501001', 30000, NULL, '2026-05-04T18:00:00', NULL, 1, N'Chờ xác nhận để tạo vận đơn.'),
((SELECT id FROM hoa_don WHERE ma = N'HD002'), N'GHN', N'GHN20260502002', 35000, '2026-05-02T10:00:00', '2026-05-05T18:00:00', NULL, 1, N'Chờ lấy hàng.'),
((SELECT id FROM hoa_don WHERE ma = N'HD003'), N'GHN', N'GHN20260503003', 40000, '2026-05-03T14:05:00', '2026-05-06T18:00:00', NULL, 2, N'Đang vận chuyển.'),
((SELECT id FROM hoa_don WHERE ma = N'HD004'), N'GHN', N'GHN20260504004', 30000, '2026-05-04T12:00:00', '2026-05-06T18:00:00', '2026-05-05T16:10:00', 3, N'Giao thành công.'),
((SELECT id FROM hoa_don WHERE ma = N'HD005'), N'GHN', N'GHN20260505005', 35000, '2026-05-05T13:20:00', '2026-05-08T18:00:00', '2026-05-06T16:55:00', 3, N'Giao thành công.'),
((SELECT id FROM hoa_don WHERE ma = N'HD006'), N'GHN', N'GHN20260506006', 0, NULL, NULL, NULL, 5, N'Đơn hủy, không giao hàng.'),
((SELECT id FROM hoa_don WHERE ma = N'HD007'), N'GHN', N'GHN20260507007', 45000, '2026-05-07T15:40:00', '2026-05-10T18:00:00', NULL, 1, N'Đang chờ xử lý yêu cầu hủy.'),
((SELECT id FROM hoa_don WHERE ma = N'HD008'), N'GHN', N'GHN20260508008', 30000, '2026-05-08T17:00:00', '2026-05-11T18:00:00', '2026-05-09T09:00:00', 3, N'Giao xong, khách yêu cầu trả hàng.'),
((SELECT id FROM hoa_don WHERE ma = N'HD009'), N'GHN', N'GHN20260509009', 25000, '2026-05-09T18:00:00', '2026-05-12T18:00:00', '2026-05-10T09:45:00', 3, N'Đã giao, đã hoàn tiền.'),
((SELECT id FROM hoa_don WHERE ma = N'HD010'), N'GHN', N'GHN20260510010', 30000, '2026-05-10T19:30:00', '2026-05-13T18:00:00', '2026-05-12T11:30:00', 3, N'Giao thành công.');
GO

INSERT INTO thanh_toan
(hoa_don_id, nhan_vien_id, ma_giao_dich, hinh_thuc, so_tien, tien_thoi_lai, ngan_hang, noi_dung_ck, cong_thanh_toan, ngay_thanh_toan, trang_thai, loai_giao_dich, ghi_chu)
VALUES
((SELECT id FROM hoa_don WHERE ma = N'HD001'), (SELECT id FROM nhan_vien WHERE ma = 'NV002'), N'TT20260501001', 2, 1620000, NULL, N'Vietcombank', N'Thanh toán HD001', N'Bank Transfer', NULL, 2, 1, N'Chờ khách thanh toán.'),
((SELECT id FROM hoa_don WHERE ma = N'HD002'), (SELECT id FROM nhan_vien WHERE ma = 'NV003'), N'TT20260502002', 1, 4125000, 75000, NULL, NULL, N'POS', '2026-05-02T09:25:00', 1, 1, N'Thanh toán tiền mặt tại quầy.'),
((SELECT id FROM hoa_don WHERE ma = N'HD003'), (SELECT id FROM nhan_vien WHERE ma = 'NV004'), N'TT20260503003', 2, 3270000, NULL, N'Techcombank', N'Thanh toán HD003', N'Bank Transfer', '2026-05-03T10:35:00', 1, 1, N'Chuyển khoản thành công.'),
((SELECT id FROM hoa_don WHERE ma = N'HD004'), (SELECT id FROM nhan_vien WHERE ma = 'NV002'), N'TT20260504004', 3, 1680000, NULL, NULL, N'Thanh toán ví điện tử HD004', N'Momo', '2026-05-04T11:50:00', 1, 1, N'Ví điện tử.'),
((SELECT id FROM hoa_don WHERE ma = N'HD005'), (SELECT id FROM nhan_vien WHERE ma = 'NV005'), N'TT20260505005', 1, 2035000, 65000, NULL, NULL, N'POS', '2026-05-05T13:07:00', 1, 1, N'Thanh toán tại quầy.'),
((SELECT id FROM hoa_don WHERE ma = N'HD006'), (SELECT id FROM nhan_vien WHERE ma = 'NV006'), N'TT20260506006', 2, 2190000, NULL, N'BIDV', N'Thanh toán HD006', N'Bank Transfer', NULL, 0, 1, N'Giao dịch bị hủy theo đơn.'),
((SELECT id FROM hoa_don WHERE ma = N'HD007'), (SELECT id FROM nhan_vien WHERE ma = 'NV003'), N'TT20260507007', 3, 3335000, NULL, NULL, N'Thanh toán HD007', N'VNPay', '2026-05-07T15:25:00', 1, 1, N'Đã thanh toán, đang chờ xử lý hủy.'),
((SELECT id FROM hoa_don WHERE ma = N'HD008'), (SELECT id FROM nhan_vien WHERE ma = 'NV004'), N'TT20260508008', 2, 3410000, NULL, N'ACB', N'Thanh toán HD008', N'Bank Transfer', '2026-05-08T16:35:00', 4, 1, N'Cần hoàn tiền sau trả hàng.'),
((SELECT id FROM hoa_don WHERE ma = N'HD009'), (SELECT id FROM nhan_vien WHERE ma = 'NV005'), N'TT20260509009', 1, 1265000, 35000, NULL, NULL, N'POS', '2026-05-09T17:43:00', 1, 1, N'Đã hoàn tiền cho khách.'),
((SELECT id FROM hoa_don WHERE ma = N'HD010'), (SELECT id FROM nhan_vien WHERE ma = 'NV002'), N'TT20260510010', 2, 1710000, NULL, N'MB Bank', N'Thanh toán HD010', N'Bank Transfer', '2026-05-10T19:05:00', 1, 1, N'Chuyển khoản thành công.');
GO

INSERT INTO phieu_tra_hang
(ma, hoa_don_id, khach_hang_id, nhan_vien_id, ly_do, tong_tien_hoan, hinh_thuc_hoan, trang_thai, ngay_tao, ngay_cap_nhat)
VALUES
(N'TH001', (SELECT id FROM hoa_don WHERE ma = N'HD001'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach1'), (SELECT id FROM nhan_vien WHERE ma = 'NV002'), N'Khách đổi ý trước xác nhận.', 1690000, 2, 1, '2026-05-01T09:00:00', NULL),
(N'TH002', (SELECT id FROM hoa_don WHERE ma = N'HD002'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach2'), (SELECT id FROM nhan_vien WHERE ma = 'NV003'), N'Đổi size sau khi mua tại quầy.', 4290000, 1, 2, '2026-05-03T10:00:00', '2026-05-03T11:00:00'),
(N'TH003', (SELECT id FROM hoa_don WHERE ma = N'HD003'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach3'), (SELECT id FROM nhan_vien WHERE ma = 'NV004'), N'Sản phẩm giao chậm, khách yêu cầu trả.', 1690000, 2, 1, '2026-05-04T08:00:00', NULL),
(N'TH004', (SELECT id FROM hoa_don WHERE ma = N'HD004'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach4'), (SELECT id FROM nhan_vien WHERE ma = 'NV002'), N'Không vừa chân.', 1650000, 3, 2, '2026-05-06T09:30:00', '2026-05-06T10:30:00'),
(N'TH005', (SELECT id FROM hoa_don WHERE ma = N'HD005'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach5'), (SELECT id FROM nhan_vien WHERE ma = 'NV005'), N'Khách muốn đổi màu.', 2250000, 1, 3, '2026-05-07T14:00:00', '2026-05-07T15:00:00'),
(N'TH006', (SELECT id FROM hoa_don WHERE ma = N'HD006'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach6'), (SELECT id FROM nhan_vien WHERE ma = 'NV006'), N'Đơn đã hủy trước giao.', 2190000, 2, 2, '2026-05-06T15:00:00', '2026-05-06T15:30:00'),
(N'TH007', (SELECT id FROM hoa_don WHERE ma = N'HD007'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach7'), (SELECT id FROM nhan_vien WHERE ma = 'NV003'), N'Khách yêu cầu hủy sau thanh toán.', 3590000, 3, 1, '2026-05-07T16:05:00', NULL),
(N'TH008', (SELECT id FROM hoa_don WHERE ma = N'HD008'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach8'), (SELECT id FROM nhan_vien WHERE ma = 'NV004'), N'Sản phẩm bị lỗi keo.', 1790000, 2, 1, '2026-05-09T09:35:00', NULL),
(N'TH009', (SELECT id FROM hoa_don WHERE ma = N'HD009'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach9'), (SELECT id FROM nhan_vien WHERE ma = 'NV005'), N'Đã hoàn tiền do trả hàng.', 1290000, 1, 2, '2026-05-10T10:00:00', '2026-05-10T10:15:00'),
(N'TH010', (SELECT id FROM hoa_don WHERE ma = N'HD010'), (SELECT id FROM khach_hang WHERE ten_dang_nhap = 'khach10'), (SELECT id FROM nhan_vien WHERE ma = 'NV002'), N'Khách muốn đổi sang size 41.', 890000, 2, 1, '2026-05-12T13:00:00', NULL);
GO

INSERT INTO phieu_tra_hang_chi_tiet
(phieu_tra_hang_id, hoa_don_chi_tiet_id, giay_chi_tiet_id, so_luong_tra, gia_ban, thanh_tien, trang_thai, ghi_chu)
VALUES
((SELECT id FROM phieu_tra_hang WHERE ma = N'TH001'), (SELECT id FROM hoa_don_chi_tiet WHERE hoa_don_id = (SELECT id FROM hoa_don WHERE ma = N'HD001')), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT001'), 1, 1690000, 1690000, 1, N'Trả toàn bộ sản phẩm.'),
((SELECT id FROM phieu_tra_hang WHERE ma = N'TH002'), (SELECT id FROM hoa_don_chi_tiet WHERE hoa_don_id = (SELECT id FROM hoa_don WHERE ma = N'HD002')), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT002'), 1, 4290000, 4290000, 1, N'Đổi size.'),
((SELECT id FROM phieu_tra_hang WHERE ma = N'TH003'), (SELECT id FROM hoa_don_chi_tiet WHERE hoa_don_id = (SELECT id FROM hoa_don WHERE ma = N'HD003')), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT003'), 1, 1690000, 1690000, 1, N'Trả một trong hai đôi.'),
((SELECT id FROM phieu_tra_hang WHERE ma = N'TH004'), (SELECT id FROM hoa_don_chi_tiet WHERE hoa_don_id = (SELECT id FROM hoa_don WHERE ma = N'HD004')), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT004'), 1, 1650000, 1650000, 1, N'Trả do không vừa.'),
((SELECT id FROM phieu_tra_hang WHERE ma = N'TH005'), (SELECT id FROM hoa_don_chi_tiet WHERE hoa_don_id = (SELECT id FROM hoa_don WHERE ma = N'HD005')), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT005'), 1, 2250000, 2250000, 0, N'Phiếu bị từ chối.'),
((SELECT id FROM phieu_tra_hang WHERE ma = N'TH006'), (SELECT id FROM hoa_don_chi_tiet WHERE hoa_don_id = (SELECT id FROM hoa_don WHERE ma = N'HD006')), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT006'), 1, 2190000, 2190000, 1, N'Hủy trước giao.'),
((SELECT id FROM phieu_tra_hang WHERE ma = N'TH007'), (SELECT id FROM hoa_don_chi_tiet WHERE hoa_don_id = (SELECT id FROM hoa_don WHERE ma = N'HD007')), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT007'), 1, 3590000, 3590000, 1, N'Chờ xử lý hủy.'),
((SELECT id FROM phieu_tra_hang WHERE ma = N'TH008'), (SELECT id FROM hoa_don_chi_tiet WHERE hoa_don_id = (SELECT id FROM hoa_don WHERE ma = N'HD008')), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT008'), 1, 1790000, 1790000, 1, N'Lỗi keo.'),
((SELECT id FROM phieu_tra_hang WHERE ma = N'TH009'), (SELECT id FROM hoa_don_chi_tiet WHERE hoa_don_id = (SELECT id FROM hoa_don WHERE ma = N'HD009')), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT009'), 1, 1290000, 1290000, 1, N'Đã hoàn tiền.'),
((SELECT id FROM phieu_tra_hang WHERE ma = N'TH010'), (SELECT id FROM hoa_don_chi_tiet WHERE hoa_don_id = (SELECT id FROM hoa_don WHERE ma = N'HD010')), (SELECT id FROM giay_chi_tiet WHERE ma_bien_the = N'GCT010'), 1, 890000, 890000, 1, N'Đổi size.');
GO

-- Quick count check: every table below should return 10.
SELECT N'nhan_vien' AS bang, COUNT(*) AS so_ban_ghi FROM nhan_vien UNION ALL
SELECT N'khach_hang', COUNT(*) FROM khach_hang UNION ALL
SELECT N'dia_chi_khach_hang', COUNT(*) FROM dia_chi_khach_hang UNION ALL
SELECT N'kich_co', COUNT(*) FROM kich_co UNION ALL
SELECT N'mau_sac', COUNT(*) FROM mau_sac UNION ALL
SELECT N'thuong_hieu', COUNT(*) FROM thuong_hieu UNION ALL
SELECT N'loai_giay', COUNT(*) FROM loai_giay UNION ALL
SELECT N'de_giay', COUNT(*) FROM de_giay UNION ALL
SELECT N'co_giay', COUNT(*) FROM co_giay UNION ALL
SELECT N'chat_lieu_giay', COUNT(*) FROM chat_lieu_giay UNION ALL
SELECT N'trong_luong', COUNT(*) FROM trong_luong UNION ALL
SELECT N'cong_nghe_dem', COUNT(*) FROM cong_nghe_dem UNION ALL
SELECT N'dot_giam_gia', COUNT(*) FROM dot_giam_gia UNION ALL
SELECT N'giay', COUNT(*) FROM giay UNION ALL
SELECT N'giay_thuoc_tinh', COUNT(*) FROM giay_thuoc_tinh UNION ALL
SELECT N'giay_chi_tiet', COUNT(*) FROM giay_chi_tiet UNION ALL
SELECT N'hinh_anh_giay', COUNT(*) FROM hinh_anh_giay UNION ALL
SELECT N'dot_giam_gia_san_pham', COUNT(*) FROM dot_giam_gia_san_pham UNION ALL
SELECT N'phieu_giam_gia', COUNT(*) FROM phieu_giam_gia UNION ALL
SELECT N'phieu_giam_gia_khach_hang', COUNT(*) FROM phieu_giam_gia_khach_hang UNION ALL
SELECT N'hoa_don', COUNT(*) FROM hoa_don UNION ALL
SELECT N'lich_su_hoa_don', COUNT(*) FROM lich_su_hoa_don UNION ALL
SELECT N'hoa_don_chi_tiet', COUNT(*) FROM hoa_don_chi_tiet UNION ALL
SELECT N'van_chuyen', COUNT(*) FROM van_chuyen UNION ALL
SELECT N'thanh_toan', COUNT(*) FROM thanh_toan UNION ALL
SELECT N'phieu_tra_hang', COUNT(*) FROM phieu_tra_hang UNION ALL
SELECT N'phieu_tra_hang_chi_tiet', COUNT(*) FROM phieu_tra_hang_chi_tiet UNION ALL
SELECT N'lich_lam_viec', COUNT(*) FROM lich_lam_viec;
GO

-- ============================================================
-- Seed lich_lam_viec dynamically for current week
-- ============================================================
DECLARE @Monday DATE = DATEADD(wk, DATEDIFF(wk, 0, GETDATE()), 0);

INSERT INTO lich_lam_viec (nhan_vien_id, ngay, ca)
VALUES
((SELECT id FROM nhan_vien WHERE ma = 'NV001'), @Monday, 'sang'),
((SELECT id FROM nhan_vien WHERE ma = 'NV001'), DATEADD(day, 1, @Monday), 'sang'),
((SELECT id FROM nhan_vien WHERE ma = 'NV001'), DATEADD(day, 2, @Monday), 'sang'),
((SELECT id FROM nhan_vien WHERE ma = 'NV001'), DATEADD(day, 4, @Monday), 'sang'),

((SELECT id FROM nhan_vien WHERE ma = 'NV002'), DATEADD(day, 1, @Monday), 'chieu'),
((SELECT id FROM nhan_vien WHERE ma = 'NV002'), DATEADD(day, 2, @Monday), 'chieu'),
((SELECT id FROM nhan_vien WHERE ma = 'NV002'), DATEADD(day, 3, @Monday), 'chieu'),
((SELECT id FROM nhan_vien WHERE ma = 'NV002'), DATEADD(day, 5, @Monday), 'chieu'),

((SELECT id FROM nhan_vien WHERE ma = 'NV003'), @Monday, 'toi'),
((SELECT id FROM nhan_vien WHERE ma = 'NV003'), DATEADD(day, 1, @Monday), 'toi'),
((SELECT id FROM nhan_vien WHERE ma = 'NV003'), DATEADD(day, 3, @Monday), 'toi'),
((SELECT id FROM nhan_vien WHERE ma = 'NV003'), DATEADD(day, 6, @Monday), 'toi'),

((SELECT id FROM nhan_vien WHERE ma = 'NV004'), @Monday, 'sang'),
((SELECT id FROM nhan_vien WHERE ma = 'NV004'), DATEADD(day, 2, @Monday), 'chieu'),
((SELECT id FROM nhan_vien WHERE ma = 'NV005'), DATEADD(day, 3, @Monday), 'toi'),
((SELECT id FROM nhan_vien WHERE ma = 'NV006'), DATEADD(day, 4, @Monday), 'sang');
GO

