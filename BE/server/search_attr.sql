SELECT 'thuong_hieu' AS tbl, id, ten FROM thuong_hieu WHERE ten = 'sdfdsfdsfsfs'
UNION ALL
SELECT 'loai_giay' AS tbl, id, ten FROM loai_giay WHERE ten = 'sdfdsfdsfsfs'
UNION ALL
SELECT 'chat_lieu_giay' AS tbl, id, ten FROM chat_lieu_giay WHERE ten = 'sdfdsfdsfsfs'
UNION ALL
SELECT 'de_giay' AS tbl, id, ten FROM de_giay WHERE ten = 'sdfdsfdsfsfs'
UNION ALL
SELECT 'co_giay' AS tbl, id, ten FROM co_giay WHERE ten = 'sdfdsfdsfsfs'
UNION ALL
SELECT 'cong_nghe_dem' AS tbl, id, ten FROM cong_nghe_dem WHERE ten = 'sdfdsfdsfsfs'
UNION ALL
SELECT 'mau_sac' AS tbl, id, ten FROM mau_sac WHERE ten = 'sdfdsfdsfsfs'
UNION ALL
SELECT 'kich_co' AS tbl, id, gia_tri FROM kich_co WHERE gia_tri = 'sdfdsfdsfsfs'
UNION ALL
SELECT 'trong_luong' AS tbl, id, CAST(gia_tri AS NVARCHAR(100)) FROM trong_luong WHERE CAST(gia_tri AS NVARCHAR(100)) = 'sdfdsfdsfsfs';
