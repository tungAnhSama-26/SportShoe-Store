if exists (
    select 1
    from sys.check_constraints
    where name = 'ck_giay_trang_thai'
      and parent_object_id = object_id('dbo.giay')
)
begin
    alter table dbo.giay drop constraint ck_giay_trang_thai;
end;

alter table dbo.giay
add constraint ck_giay_trang_thai
check (trang_thai in (0, 1, 2));
