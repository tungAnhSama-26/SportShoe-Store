TÀI LIỆU ĐẶC TẢ HỆ THỐNG AI CHATBOT
**Phiên bản:** v1.0 (Bản hoàn chỉnh)  
**Nền tảng phát triển:** Spring Boot & Vue.js  
**Hệ thống tích hợp:** Cơ sở dữ liệu Sản phẩm & Đơn hàng  
**Mô hình AI:** OpenAI GPT-4o-mini / Spring AI  
1. Tổng Quan Kế Hoạch & Định Hướng Giải Pháp
Tài liệu đặc tả này thiết lập giải pháp xây dựng hệ thống Trợ lý ảo AI Chatbot toàn diện cho nền tảng kinh
doanh giày trực tuyến SportShoe. Nhằm tối ưu hóa tỷ lệ chuyển đổi, giải pháp này là sự kết hợp chặt chẽ
giữa hai phương pháp tiếp cận cốt lõi:
Kiến trúc Hướng dữ liệu dựa trên LLM (Function Calling): Cho phép AI tự động nhận diện ý định,
kết nối trực tiếp với Database thông qua các dịch vụ Backend để tra cứu thời gian thực, đảm bảo
thông tin sản phẩm và lượng tồn kho chính xác tuyệt đối.
Mô hình Kịch bản Chăm sóc Khách hàng Chuẩn hóa: Trích xuất và tối ưu hóa từ quy trình xử lý
hội thoại của các hệ thống tăng trưởng doanh số chuyên nghiệp, phân loại khách hàng theo phễu
hành vi từ bước tiếp cận, tư vấn chuyên sâu, xử lý từ chối đến chốt đơn bán hàng.

2. Phân Lớp Kiến Trúc Kỹ Thuật (System Architecture)
Hệ thống vận hành đồng bộ qua 4 phân lớp cốt lõi không qua các kết nối trực tiếp nguy hiểm vào tầng
lưu trữ:
2.1. Lớp Giao Diện (Frontend - Vue.js)
Tích hợp một Chat Component linh hoạt dưới góc màn hình. Đảm nhiệm nhận dữ liệu đầu vào văn bản
từ người dùng, truyền tải phi đồng bộ qua Axios và phân rã các định dạng phức tạp (Markdown, danh
sách sản phẩm cấu trúc) từ AI trả về để hiển thị một cách trực quan, tối ưu trải nghiệm giao diện người
dùng.
2.2. Lớp Nghiệp Vụ Trung Gian (Backend - Spring Boot & Spring AI)
Đóng vai trò trung tâm điều phối luồng thông tin. Tầng này đăng ký các hàm nghiệp vụ (Java Methods)
dưới dạng các "Tools" và cung cấp mô tả nghiệp vụ cụ thể cho AI. Khi AI yêu cầu thực thi dữ liệu, Spring
Boot sẽ xử lý logic, gọi cơ sở dữ liệu và trả về kết quả định dạng chuẩn cho mô hình ngôn ngữ lớn.
2.3. Lớp Trí Tuệ Nhân Tạo (AI Engine - OpenAI GPT-4o-mini API)
Phân tích cú pháp ngôn ngữ tự nhiên, trích xuất thực thể (Entity Extraction) như dòng giày, kích cỡ, màu
sắc, phân tích cảm xúc khách hàng và đưa ra quyết định hội thoại tiếp theo dựa trên System Prompt và
kết quả dữ liệu trả về từ hệ thống.
•

•
Tài liệu Đặc tả Kỹ thuật & Kịch bản Chatbot AI — Dự án SportShoe

Trang 1

2.4. Lớp Lưu Trữ Dữ Liệu (Database Layer - SQL Server )
Lưu trữ thực thể kinh doanh bao gồm danh mục sản phẩm, biến thể (size, màu), trạng thái tồn kho, số
lượng hàng đã bán (sales_count) để phục vụ cho các logic truy vấn hàng bán chạy và tìm kiếm sản
phẩm.

3. Đặc Tả 6 Kịch Bản Hội Thoại Kinh Doanh Chuyên Nghiệp
Dựa trên các thực tế vận hành chatbot thương mại điện tử tối ưu, hệ thống AI Chatbot của SportShoe được
cấu hình để bao phủ trọn vẹn 6 kịch bản chuyển đổi cốt lõi dưới đây:

Kịch Bản Mục Tiêu

Nguyên Tắc Điều Hướng Hội
Thoại của AI

Hành Động Hệ Thống / Tool Gọi

1. Tiếp Đón & Khởi
Động Hội Thoại

Chào hỏi cá nhân hóa, khẳng định
vai trò là trợ lý ảo SportShoe. Chủ động
đưa ra các tùy chọn gợi ý lớn (Tìm
giày theo nhu cầu, Xem mẫu hot
tuần này) để định hướng khách
hàng nhập câu hỏi.

Không gọi DB. Trả về text chào hỏi tiêu
chuẩn định dạng sẵn.

2. Tra Cứu Sản
Phẩm Theo Yêu Cầu

Trích xuất các thuộc tính: tên giày,
kiểu dáng, màu sắc từ câu hỏi.
Chuyển đổi ngôn ngữ tự nhiên
thành tham số tìm kiếm có cấu trúc.
Nếu thiếu thông tin quan trọng, AI
chủ động hỏi thêm một cách khéo
léo.

Kích hoạt hàm:
searchProductsTool(keyword,
color, category) để truy vấn bảng
sản phẩm.

3. Đề Xuất Giày Bán
Chạy (Best Seller)

Khi khách hàng thể hiện sự phân
vân hoặc yêu cầu tư vấn xu hướng
("mẫu nào đang hot", "đôi nào bán
chạy"), AI sẽ dẫn dắt bằng các số
liệu mua sắm thực tế từ hệ thống để
tăng tính thuyết phục.

Kích hoạt hàm:
getBestSellingShoesTool(). Thực
thi câu lệnh sắp xếp theo số lượng bán
giảm dần từ DB.

4. Tư Vấn Kích Cỡ &
Chọn Size

Khi khách hàng hỏi về size, AI
hướng dẫn khách hàng đo chiều dài
bàn chân, đối chiếu bảng quy đổi
chuẩn của shop. Nhắc nhở về form
dáng của từng loại giày cụ thể (ôm
chân hay rộng rãi).

Truy xuất tài liệu hướng dẫn tĩnh hoặc
thuộc tính form dáng của dòng sản phẩm
đó.

Tài liệu Đặc tả Kỹ thuật & Kịch bản Chatbot AI — Dự án SportShoe

Trang 2

Kịch Bản Mục Tiêu

Nguyên Tắc Điều Hướng Hội
Thoại của AI

Hành Động Hệ Thống / Tool Gọi

5. Xử Lý Tình
Huống Hết Hàng /
Hết Size

Tuyệt đối không đưa ra câu trả lời
cụt lủn "Hết hàng". AI phải lịch sự
thông báo trạng thái hiện tại, sau đó
lập tức chuyển đổi sang kịch bản
thay thế bằng cách gợi ý sản phẩm
tương đồng về kiểu dáng hoặc phân
khúc giá.

Hệ thống trả về mảng rỗng từ DB -> AI
tự động kích hoạt luồng đề xuất sản
phẩm cùng danh mục liên quan.

6. Thu Thập Thông
Tin & Chốt Đơn

Khi khách hàng đồng ý chốt sản
phẩm, AI chuyển sang chế độ thu
thập thông tin có cấu trúc bao gồm:
Tên người nhận, Số điện thoại, Địa
chỉ giao hàng. Tổng hợp lại đơn
hàng trước khi chuyển tiếp dữ liệu.

Tạo cấu trúc JSON đơn hàng tạm thời,
hiển thị nút xác nhận hoặc chuyển tiếp
thông tin về hệ thống CRM/Nhân viên
trực.

Lưu ý nghiệp vụ chuyển đổi: Trong mọi tình huống hội thoại gặp phản hồi tiêu cực sâu sắc từ
khách hàng hoặc yêu cầu can thiệp kỹ thuật phức tạp ngoài phạm vi mua sắm, AI được cấu hình để

nhận diện ý định và đề xuất chuyển cuộc gọi đến nhân viên tư vấn là con người (Human-in-the-
loop) để đảm bảo không đứt gãy trải nghiệm.

4. Kiến Trúc Thiết Kế System Prompt Gốc (AI Prompt Architecture)
Hệ thống sử dụng mô hình thiết kế Prompt chặt chẽ theo cấu trúc R-C-T-T-C (Role - Context - Task -
Tone - Constraints). Đoạn mã Prompt dưới đây được nạp trực tiếp vào cấu hình khởi tạo của
ChatClient trong Spring Boot:

# ROLE & CONTEXT
Bạn là một chuyên gia tư vấn bán hàng ảo, đại diện chính thức cho thương hiệu
giày trực tuyến SportShoe. Nhiệm vụ tối cao của bạn là hỗ trợ khách hàng tìm kiếm,
chọn lựa và đưa ra quyết định mua sắm các sản phẩm giày dép phù hợp nhất với nhu
cầu của họ.
# CORE TASKS
1. Phân tích nhu cầu của khách hàng dựa trên ngôn ngữ tự nhiên để xác định đúng
loại sản phẩm cần tìm.
2. LUÔN LUÔN sử dụng các công cụ hệ thống (Tools) được cung cấp để truy vấn dữ
liệu thực tế từ cơ sở dữ liệu khi có yêu cầu tra cứu sản phẩm hoặc danh sách bán
Tài liệu Đặc tả Kỹ thuật & Kịch bản Chatbot AI — Dự án SportShoe

Trang 3

chạy. Không bao giờ tự suy đoán kho hàng.
3. Trình bày sản phẩm rõ ràng, mạch lạc bao gồm các thông tin thiết yếu: Tên sản
phẩm, Giá bán niêm yết, các size hiện có và liên kết hình ảnh trực quan (nếu có).
# CONSTRAINTS & BEHAVIORAL RULES (TUYỆT ĐỐI TUÂN THỦ)
- KHÔNG ẢO TƯỞNG (Zero Hallucination): Chỉ tư vấn và khẳng định sự tồn tại của
các sản phẩm có dữ liệu trả về từ kết quả gọi hàm hệ thống. Tuyệt đối không tự
bịa ra tên sản phẩm, mức giá hay các chương trình khuyến mãi không có trong ngữ
cảnh dữ liệu.
- ĐỊNH HƯỚNG TÍCH CỰC: Nếu một sản phẩm cụ thể được tìm kiếm đã hết hàng hoặc hết
size, hãy thông báo lịch sự và ngay lập tức sử dụng dữ liệu bán chạy hoặc sản
phẩm tương tự để gợi ý thay thế.
- TRUNG LẬP VÀ ĐÚNG PHẠM VI: Từ chối một cách khéo léo và lịch sự đối với tất cả
các câu hỏi nằm ngoài phạm vi thời trang, giày dép, hoặc các dịch vụ vận hành của
cửa hàng SportShoe.
# RESPONSE FORMATTING
- Trả lời bằng ngôn ngữ Tiếng Việt, ngắn gọn, súc tích, tập trung thẳng vào câu
hỏi (Tối đa 150 từ cho mỗi lượt phản hồi).
- Sử dụng cú pháp định dạng Markdown: In đậm **[Tên sản phẩm]**, định dạng giá rõ
ràng (Ví dụ: *550.000đ*). Sử dụng danh sách gạch đầu dòng (-) khi liệt kê từ 2
sản phẩm trở lên để khách hàng dễ theo dõi trên giao diện di động và máy tính.

5. Quy Trình Gọi Hàm Tự Động & Đồng Bộ Cơ Sở Dữ Liệu
Sự thông minh của chatbot nằm ở khả năng tương tác trực tiếp với tầng dữ liệu nghiệp vụ của doanh
nghiệp mà không làm ảnh hưởng đến tính bảo mật. Quy trình này được vận hành tự động qua mô hình
tuần tự khép kín dưới đây:
Giai đoạn Phân tích Ý định (Intent Parsing): Khi người dùng gửi chuỗi văn bản yêu cầu (Ví dụ: "Tìm
cho anh đôi giày chạy bộ màu trắng size 42"), hệ thống Spring AI sẽ gửi nội dung này kèm các mô tả
định danh của các hàm Java hiện có sang OpenAI API. Mô hình AI phân tích cấu trúc ngôn ngữ và
ánh xạ yêu cầu này vào hàm hệ thống searchProductsTool, đồng thời tự động bóc tách các tham
số đầu vào: keyword="chạy bộ", color="trắng".
Giai đoạn Tạm dừng Phản hồi & Gọi hàm Ngược (Function Call Execution): Mô hình AI sẽ không
trực tiếp trả lời khách hàng ở lượt này, mà gửi ngược lại một yêu cầu phản hồi kỹ thuật chứa cấu trúc
JSON chứa tên hàm và giá trị tham số cần thiết. Tầng Spring Boot đánh chặn yêu cầu này, kích hoạt
ProductRepository thực thi câu lệnh SQL tìm kiếm có điều kiện tương ứng trong cơ sở dữ liệu
 SQL Server của SportShoe.
Giai đoạn Chuyển đổi Ngữ cảnh & Hoàn thiện Hội thoại (Context Enrichment): Dữ liệu thực tế
thô thu được từ Database (dưới dạng danh sách các thực thể hoặc chuỗi JSON thô chứa tên, mã, giá,
số lượng tồn kho thực tế) được Spring Boot gửi ngược lại cho AI làm dữ liệu nền tảng (Context). AI
1.

2.

3.
Tài liệu Đặc tả Kỹ thuật & Kịch bản Chatbot AI — Dự án SportShoe

Trang 4

tiếp nhận dữ liệu này, kết hợp cùng System Prompt ban đầu để biên dịch cấu trúc thô thành một câu
trả lời tự nhiên, thân thiện và đầy đủ định dạng hiển thị để gửi tới Client.

6. Yêu Cầu Phi Chức Năng & Tiêu Chuẩn Bảo Mật
Bảo mật kiến trúc dữ liệu: AI tuyệt đối không tiếp cận trực tiếp chuỗi kết nối Database (Connection
String) hoặc cấu trúc bảng Schema thô. Toàn bộ dữ liệu trao đổi được bọc qua các lớp đối tượng
trung gian DTO (Data Transfer Object), loại bỏ hoàn toàn các trường dữ liệu nội bộ như giá gốc nhập
hàng, thông tin nhà cung cấp hay doanh thu chi tiết.
Kiểm soát thời gian phản hồi (Latency): Tổng thời gian xử lý một chuỗi hội thoại bao gồm vòng lặp
gọi hàm và phản hồi từ dịch vụ AI bên ngoài không được vượt quá 3.5 giây nhằm duy trì mạch tương
tác mượt mà của khách hàng trên website.
Cơ chế xử lý lỗi ngoại lệ chủ động (Fault Tolerance): Trong trường hợp dịch vụ API bên ngoài gặp
sự cố kết nối hoặc mất tín hiệu, tầng Backend của Spring Boot phải tự động đánh chặn lỗi (Catch
Exception) và trả về một chuỗi văn bản thay thế an toàn cho giao diện Vue.js, thông báo hệ thống tư
vấn đang nâng cấp và chủ động hiển thị danh sách hotline hỗ trợ trực tiếp.

## 7 Tổng Quan Kế Hoạch & Định Hướng Giải Pháp

Tài liệu đặc tả này thiết lập giải pháp xây dựng hệ thống Trợ lý ảo AI Chatbot toàn diện cho nền tảng kinh doanh giày trực tuyến **SportShoe**. Nhằm tối ưu hóa tỷ lệ chuyển đổi, giải pháp này là sự kết hợp chặt chẽ giữa hai phương pháp tiếp cận cốt lõi: 

* **Kiến trúc Hướng dữ liệu dựa trên LLM (Function Calling):** Cho phép AI tự động nhận diện ý định, kết nối trực tiếp với Database thông qua các dịch vụ Backend để tra cứu thời gian thực, đảm bảo thông tin sản phẩm và lượng tồn kho chính xác tuyệt đối.
* **Mô hình Kịch bản Chăm sóc Khách hàng Chuẩn hóa:** Trích xuất và tối ưu hóa từ quy trình xử lý hội thoại của các hệ thống tăng trưởng doanh số chuyên nghiệp, phân loại khách hàng theo phễu hành vi từ bước tiếp cận, tư vấn chuyên sâu, xử lý từ chối đến chốt đơn bán hàng.

## 8Phân Lớp Kiến Trúc Kỹ Thuật (System Architecture)

Hệ thống vận hành đồng bộ qua 4 phân lớp cốt lõi không qua các kết nối trực tiếp nguy hiểm vào tầng lưu trữ:

###8.1. Lớp Giao Diện (Frontend - Vue.js)
Tích hợp một Chat Component linh hoạt dưới góc màn hình. Đảm nhiệm nhận dữ liệu đầu vào văn bản từ người dùng, truyền tải phi đồng bộ qua Axios và phân rã các định dạng phức tạp (Markdown, danh sách sản phẩm cấu trúc) từ AI trả về để hiển thị một cách trực quan, tối ưu trải nghiệm giao diện người dùng.

### 8.2. Lớp Nghiệp Vụ Trung Gian (Backend - Spring Boot & Spring AI)
Đóng vai trò trung tâm điều phối luồng thông tin. Tầng này đăng ký các hàm nghiệp vụ (Java Methods) dưới dạng các "Tools" và cung cấp mô tả nghiệp vụ cụ thể cho AI. Khi AI yêu cầu thực thi dữ liệu, Spring Boot sẽ xử lý logic, gọi cơ sở dữ liệu và trả về kết quả định dạng chuẩn cho mô hình ngôn ngữ lớn.

### 8.3. Lớp Trí Tuệ Nhân Tạo (AI Engine - OpenAI GPT-4o-mini API, gemine, gork, v.v.)
Đảm nhiệm các chức năng phân tích ngôn ngữ tự nhiên, nhận diện ý định người dùng, trích xuất thực thể (Entity Extraction) như dòng giày, kích cỡ, màu sắc, phân tích cảm xúc khách hàng và đưa ra quyết định hội thoại tiếp theo dựa trên System Prompt và kết quả dữ liệu trả về từ hệ thống.  
Phân tích cú pháp ngôn ngữ tự nhiên, trích xuất thực thể (Entity Extraction) như dòng giày, kích cỡ, màu sắc, phân tích cảm xúc khách hàng và đưa ra quyết định hội thoại tiếp theo dựa trên System Prompt và kết quả dữ liệu trả về từ hệ thống.

### 8.4. Lớp Lưu Trữ Dữ Liệu (Database Layer -  SQL Server
Lưu trữ thực thể kinh doanh bao gồm danh mục sản phẩm, biến thể (size, màu), trạng thái tồn kho, số lượng hàng đã bán (`sales_count`) để phục vụ cho các logic truy vấn hàng bán chạy và tìm kiếm sản phẩm.

## 9 Đặc Tả 6 Kịch Bản Hội Thoại Kinh Doanh Chuyên Nghiệp

Dựa trên các thực tế vận hành chatbot thương mại điện tử tối ưu, hệ thống AI Chatbot của SportShoe được cấu hình để bao phủ trọn vẹn 6 kịch bản chuyển đổi cốt lõi dưới đây:

| Kịch Bản Mục Tiêu | Nguyên Tắc Điều Hướng Hội Thoại của AI | Hành Động Hệ Thống / Tool Gọi |
| :--- | :--- | :--- |
| **1. Tiếp Đón & Khởi Động Hội Thoại** | Chào hỏi cá nhân hóa, khẳng định vai trò là trợ lý ảo SportShoe. Chủ động đưa ra các tùy chọn gợi ý lớn (Tìm giày theo nhu cầu, Xem mẫu hot tuần này) để định hướng khách hàng nhập câu hỏi. | Không gọi DB. Trả về text chào hỏi tiêu chuẩn định dạng sẵn. |
| **2. Tra Cứu Sản Phẩm Theo Yêu Cầu** | Trích xuất các thuộc tính: tên giày, kiểu dáng, màu sắc từ câu hỏi. Chuyển đổi ngôn ngữ tự nhiên thành tham số tìm kiếm có cấu trúc. Nếu thiếu thông tin quan trọng, AI chủ động hỏi thêm một cách khéo léo. | Kích hoạt hàm: `searchProductsTool(keyword, color, category)` để truy vấn bảng sản phẩm. |
| **3. Đề Xuất Giày Bán Chạy (Best Seller)** | Khi khách hàng thể hiện sự phân vân hoặc yêu cầu tư vấn xu hướng ("mẫu nào đang hot", "đôi nào bán chạy"), AI sẽ dẫn dắt bằng các số liệu mua sắm thực tế từ hệ thống để tăng tính thuyết phục. | Kích hoạt hàm: `getBestSellingShoesTool()`. Thực thi câu lệnh sắp xếp theo số lượng bán giảm dần từ DB. |
| **4. Tư Vấn Kích Cỡ & Chọn Size** | Khi khách hàng hỏi về size, AI hướng dẫn khách hàng đo chiều dài bàn chân, đối chiếu bảng quy đổi chuẩn của shop. Nhắc nhở về form dáng của từng loại giày cụ thể (ôm chân hay rộng rãi). | Truy xuất tài liệu hướng dẫn tĩnh hoặc thuộc tính form dáng của dòng sản phẩm đó. |
| **5. Xử Lý Tình Huống Hết Hàng / Hết Size** | Tuyệt đối không đưa ra câu trả lời cụt lủn "Hết hàng". AI phải lịch sự thông báo trạng thái hiện tại, sau đó lập tức chuyển đổi sang kịch bản thay thế bằng cách gợi ý sản phẩm tương đồng về kiểu dáng hoặc phân khúc giá. | Hệ thống trả về mảng rỗng từ DB -> AI tự động kích hoạt luồng đề xuất sản phẩm cùng danh mục liên quan. |
| **6. Thu Thập Thông Tin & Chốt Đơn** | Khi khách hàng đồng ý chốt sản phẩm, AI chuyển sang chế độ thu thập thông tin có cấu trúc bao gồm: Tên người nhận, Số điện thoại, Địa chỉ giao hàng. Tổng hợp lại đơn hàng trước khi chuyển tiếp dữ liệu. | Tạo cấu trúc JSON đơn hàng tạm thời, hiển thị nút xác nhận hoặc chuyển tiếp thông tin về hệ thống CRM/Nhân viên trực. |

> **Lưu ý nghiệp vụ chuyển đổi:** Trong mọi tình huống hội thoại gặp phản hồi tiêu cực sâu sắc từ khách hàng hoặc yêu cầu can thiệp kỹ thuật phức tạp ngoài phạm vi mua sắm, AI được cấu hình để nhận diện ý định và đề xuất chuyển cuộc gọi đến nhân viên tư vấn là con người (Human-in-the-loop) để đảm bảo không đứt gãy trải nghiệm.

## 10Kiến Trúc Thiết Kế System Prompt Gốc (AI Prompt Architecture)

Hệ thống sử dụng mô hình thiết kế Prompt chặt chẽ theo cấu trúc **R-C-T-T-C (Role - Context - Task - Tone - Constraints)**. Đoạn mã Prompt dưới đây được nạp trực tiếp vào cấu hình khởi tạo của `ChatClient` trong Spring Boot:
