package com.example.server.core.admin.khachHang.dto.request;

import com.example.server.infrastructure.address.DiaChiHaiCapRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DiaChiRequest(
        @NotBlank(message = "Họ tên người nhận không được để trống")
        @Size(min = 3, max = 100, message = "Họ tên người nhận phải có từ 3 đến 100 ký tự")
        @Pattern(regexp = "^[a-zA-Z\\sàáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ]+$",
                message = "Họ tên người nhận không được chứa số hoặc ký tự đặc biệt")
        String hoTen,

        @NotBlank(message = "SĐT người nhận không được để trống")
        @Pattern(regexp = "^(0|\\+84)[35789]\\d{8}$", message = "Số điện thoại không đúng định dạng")
        @Size(max = 20)
        String sdt,

        @NotNull(message = "Địa chỉ không được để trống")
        @Valid
        DiaChiHaiCapRequest diaChi,

        @NotNull
        Boolean laMacDinh
) {}
