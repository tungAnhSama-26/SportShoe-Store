package com.example.server.infrastructure.security;

import com.example.server.entity.NhanVien;
import com.example.server.entity.KhachHang;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.NhanVienRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Lý do cụ thể khiến token bị từ chối (tài khoản bị khóa, không còn tồn tại...).
     * SecurityConfig đọc thuộc tính này để trả đúng thông điệp 401 cho FE thay vì
     * câu chung chung "Vui lòng đăng nhập để tiếp tục".
     */
    public static final String THUOC_TINH_LY_DO_TU_CHOI = "sportshoe.lyDoTuChoiToken";

    /** Khách hàng đang đăng nhập bị admin khóa -> FE dựa vào cờ này để đưa về giao diện khách. */
    public static final String THUOC_TINH_PHAM_VI = "sportshoe.phamViToken";

    private final JwtService jwtService;
    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepository khachHangRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            NhanVienRepository nhanVienRepository,
            KhachHangRepository khachHangRepository
    ) {
        this.jwtService = jwtService;
        this.nhanVienRepository = nhanVienRepository;
        this.khachHangRepository = khachHangRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorization.substring(7).trim();
            ParsedSubjectToken subjectToken = jwtService.parseSubjectToken(token);
            if ("CUSTOMER".equals(subjectToken.role())) {
                authenticateCustomer(token, request);
                filterChain.doFilter(request, response);
                return;
            }

            ParsedAdminToken parsedToken = jwtService.parseAdminToken(token);
            AdminPrincipal principal = parsedToken.principal();
            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findById(principal.id());
            if (nhanVienOpt.isEmpty()
                    || (nhanVienOpt.get().getTrangThai() != 1
                    && nhanVienOpt.get().getTrangThai() != 2)) {
                SecurityContextHolder.clearContext();
                ghiLyDoTuChoi(request, "admin", nhanVienOpt.isPresent()
                        ? "Tài khoản nhân viên đã bị khóa. Vui lòng liên hệ quản trị viên."
                        : "Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại.");
            } else {
                NhanVien nhanVien = nhanVienOpt.get();
                Integer currentVaiTro = normalizeVaiTro(nhanVien.getVaiTro());
                if (!currentVaiTro.equals(normalizeVaiTro(principal.vaiTro()))) {
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                String currentRole = resolveRole(nhanVien.getVaiTro());
                AdminPrincipal currentPrincipal = new AdminPrincipal(
                        nhanVien.getId(),
                        nhanVien.getMa(),
                        nhanVien.getTenDangNhap(),
                        nhanVien.getHoTen(),
                        currentVaiTro,
                        currentRole
                );
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        currentPrincipal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + currentRole))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (IllegalArgumentException ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateCustomer(String token, HttpServletRequest request) {
        CustomerPrincipal tokenPrincipal = jwtService.parseCustomerToken(token).principal();
        Optional<KhachHang> customerOpt = khachHangRepository.findById(tokenPrincipal.id());
        if (customerOpt.isEmpty() || customerOpt.get().getTrangThai() != 1) {
            SecurityContextHolder.clearContext();
            ghiLyDoTuChoi(request, "customer", customerOpt.isPresent()
                    ? "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ cửa hàng để được hỗ trợ."
                    : "Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại.");
            return;
        }

        KhachHang customer = customerOpt.get();
        CustomerPrincipal currentPrincipal = new CustomerPrincipal(
                customer.getId(),
                customer.getTenDangNhap(),
                customer.getHoTen(),
                "CUSTOMER"
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                currentPrincipal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /** Ghi lại lý do + phạm vi (admin/customer) của token bị từ chối để trả về cho FE. */
    private void ghiLyDoTuChoi(HttpServletRequest request, String phamVi, String lyDo) {
        request.setAttribute(THUOC_TINH_LY_DO_TU_CHOI, lyDo);
        request.setAttribute(THUOC_TINH_PHAM_VI, phamVi);
    }

    private String resolveRole(Integer vaiTro) {
        return Integer.valueOf(1).equals(vaiTro) ? "ADMIN" : "STAFF";
    }

    private Integer normalizeVaiTro(Integer vaiTro) {
        return Integer.valueOf(1).equals(vaiTro) ? 1 : 2;
    }
}
