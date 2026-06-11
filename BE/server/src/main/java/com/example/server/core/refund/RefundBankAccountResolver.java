package com.example.server.core.refund;

import com.example.server.entity.KhachHang;
import com.example.server.entity.TaiKhoanNganHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.TaiKhoanNganHangRepository;
import org.springframework.stereotype.Component;

@Component
public class RefundBankAccountResolver {

    private final TaiKhoanNganHangRepository repository;

    public RefundBankAccountResolver(TaiKhoanNganHangRepository repository) {
        this.repository = repository;
    }

    public TaiKhoanNganHang resolve(
            KhachHang customer,
            Integer bankAccountId,
            boolean required
    ) {
        if (bankAccountId == null && !required) {
            return null;
        }
        if (customer == null) {
            throw new BusinessException("Hóa đơn không có khách hàng để nhận tiền hoàn");
        }
        if (bankAccountId == null) {
            throw new BusinessException("Vui lòng chọn tài khoản ngân hàng nhận tiền hoàn");
        }

        return repository.findByIdAndKhachHangId(bankAccountId, customer.getId())
                .orElseThrow(() -> new BusinessException(
                        "Tài khoản ngân hàng không tồn tại hoặc không thuộc khách hàng của hóa đơn"
                ));
    }
}
