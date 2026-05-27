1. HD_TEST_01
   Luồng: Khách đặt online, chưa ai xử lý.

Loại đơn: Trực tuyến
Trạng thái hóa đơn: Chờ xác nhận
Thanh toán: COD, trạng thái Chờ thanh toán
Test: vào chi tiết đơn, thử chuyển Chờ xác nhận -> Đã xác nhận -> Chờ lấy hàng -> Chờ giao hàng. 2. HD_TEST_02
Luồng: Nhân viên bán tại cửa hàng, thu tiền xong.

Loại đơn: Cửa hàng
Nhân viên: NV002
Trạng thái hóa đơn: Hoàn thành
Thanh toán: Tiền mặt, Đã thanh toán
Test: đơn đã hoàn thành thì không nên sửa sản phẩm/trạng thái nữa. 3. HD_TEST_03
Luồng: Khách online đã chuyển khoản, shop đang giao.

Loại đơn: Trực tuyến
Trạng thái hóa đơn: Chờ giao hàng
Vận chuyển: Đang giao
Thanh toán: Chuyển khoản, Đã thanh toán
Test: chuyển sang Đã giao hàng, hoặc chuyển sang Giao hàng thất bại.
Nếu chọn Giao hàng thất bại, thanh toán phải thành Cần hoàn tiền. 4. HD_TEST_04
Luồng: COD đã giao tới khách nhưng chưa xác nhận thu tiền.

Loại đơn: Trực tuyến
Trạng thái hóa đơn: Đã giao hàng
Thanh toán: COD, Chờ thanh toán
Test: trong khối Lịch sử thanh toán phải hiện nút Thanh toán.
Bấm Thanh toán, chọn Tiền mặt hoặc Chuyển khoản, xác nhận xong thanh toán thành Đã thanh toán.
Sau đó mới chuyển được hóa đơn sang Hoàn thành. 5. HD_TEST_05
Luồng: COD nhưng khách không nhận hàng.

Loại đơn: Trực tuyến
Trạng thái hóa đơn: Giao hàng thất bại
Vận chuyển: Giao thất bại
Thanh toán: COD, trạng thái Đã hủy
Test: không cần hoàn tiền vì chưa thu tiền.
Có thể xử lý tiếp sang Hủy. 6. HD_TEST_06
Luồng: Khách đã thanh toán online trước, nhưng giao thất bại.

Loại đơn: Trực tuyến
Trạng thái hóa đơn: Giao hàng thất bại
Thanh toán: Chuyển khoản, trạng thái Cần hoàn tiền
Test: khối Lịch sử thanh toán phải hiện nút Hoàn tiền.
Bấm hoàn tiền, nhập thông tin, xác nhận xong thanh toán thành Đã hoàn tiền. 7. HD_TEST_07
Luồng: Đơn đã được đánh dấu cần hoàn tiền.

Loại đơn: Trực tuyến
Trạng thái hóa đơn: Cần hoàn tiền
Thanh toán: Chuyển khoản, trạng thái Cần hoàn tiền
Test: dùng để test riêng modal Hoàn tiền.
Sau khi xác nhận hoàn tiền, hóa đơn chuyển về Hủy, thanh toán thành Đã hoàn tiền. 8. HD_TEST_08
Luồng: Đơn đã xử lý hoàn tiền xong.

Loại đơn: Trực tuyến
Trạng thái hóa đơn: Hủy
Thanh toán: Chuyển khoản, trạng thái Đã hoàn tiền
Vận chuyển: Đã hủy/không giao tiếp
Test: kiểm tra lịch sử thanh toán hiển thị đúng Đã hoàn tiền, không hiện nút hoàn tiền nữa. 9. HD_TEST_09
Luồng: Khách yêu cầu hủy đơn.

Loại đơn: Trực tuyến
Trạng thái hóa đơn: Yêu cầu hủy
Thanh toán: COD, Chờ thanh toán
Test: màn chi tiết phải hiện khu vực xử lý yêu cầu hủy.
Nếu xác nhận hủy: hóa đơn sang Hủy, thanh toán COD sang Đã hủy.
Nếu từ chối: hóa đơn quay về Chờ xác nhận. 10. HD_TEST_10
Luồng: Nhân viên tạo đơn ở cửa hàng nhưng có giao hàng cho khách.

Loại đơn: Cửa hàng
Nhân viên: NV002
Trạng thái hóa đơn: Chờ giao hàng
Thanh toán: Tiền mặt, Đã thanh toán
Test: vẫn dùng logic giao hàng/GHN.
Có thể chuyển Đã giao hàng -> Hoàn thành.
Nếu khách không nhận, có thể chuyển Giao hàng thất bại, lúc đó vì đã thu tiền nên phải phát sinh Cần hoàn tiền.
