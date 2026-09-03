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
        return resolve(customer, bankAccountId, null, null, null, required);
    }

    public TaiKhoanNganHang resolve(
            KhachHang customer,
            Integer bankAccountId,
            String manualBank,
            String manualAccountNumber,
            String manualAccountHolder,
            boolean required
    ) {
        if (!required) {
            return null;
        }
        if (bankAccountId != null) {
            if (customer == null) {
                throw new BusinessException("Hóa đơn không có khách hàng để nhận tiền hoàn");
            }
            return repository.findByIdAndKhachHangId(bankAccountId, customer.getId())
                    .orElseThrow(() -> new BusinessException(
                            "Tài khoản ngân hàng không tồn tại hoặc không thuộc khách hàng của hóa đơn"
                    ));
        }

        if (manualAccountNumber != null && !manualAccountNumber.isBlank()
                && manualBank != null && !manualBank.isBlank()) {
            TaiKhoanNganHang manualAccount = new TaiKhoanNganHang();
            manualAccount.setKhachHang(customer);
            manualAccount.setTenNganHang(manualBank.trim());
            manualAccount.setSoTaiKhoan(manualAccountNumber.trim());
            manualAccount.setTenChuTaiKhoan(manualAccountHolder != null ? manualAccountHolder.trim() : "");
            return manualAccount;
        }

        throw new BusinessException("Vui lòng chọn hoặc nhập thông tin tài khoản ngân hàng nhận tiền hoàn");
    }
}
