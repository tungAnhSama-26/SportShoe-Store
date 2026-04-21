
if exists (
    select 1
    from sys.check_constraints
    where name = 'ck_hoa_don_trang_thai'
      and parent_object_id = object_id('dbo.hoa_don')
)
begin
    alter table dbo.hoa_don drop constraint ck_hoa_don_trang_thai;
end;

alter table dbo.hoa_don
add constraint ck_hoa_don_trang_thai
check (trang_thai in (1, 2, 3, 4, 5, 6, 7, 8));

update hd
set hd.trang_thai = 5,
    hd.ngay_cap_nhat = sysdatetime()
from hoa_don hd
where hd.kenh_ban = 1
  and hd.trang_thai = 2
  and exists (
      select 1
      from thanh_toan tt
      where tt.hoa_don_id = hd.id
        and isnull(tt.trang_thai, 0) = 1
  );

update hd
set hd.trang_thai = 6,
    hd.ngay_cap_nhat = sysdatetime()
from hoa_don hd
where hd.kenh_ban = 1
  and hd.trang_thai = 5
  and not exists (
      select 1
      from thanh_toan tt
      where tt.hoa_don_id = hd.id
        and isnull(tt.trang_thai, 0) = 1
  );
