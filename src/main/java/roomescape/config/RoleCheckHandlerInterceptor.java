package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.service.AuthService;
import roomescape.exception.ForbiddenException;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.Member;

import java.util.Arrays;

@Component
public class RoleCheckHandlerInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public RoleCheckHandlerInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        if (request.getCookies() == null) {
            throw new UnauthorizedException("사용자 인증 정보가 없습니다.");
        }

        String accessToken = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("token"))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("사용자 인증 정보가 없습니다."))
                .getValue();

        Member member = authService.findMemberByToken(accessToken);

        checkAdminUser(member);
        return true;
    }

    private void checkAdminUser(Member member) {
        if (!member.isAdminUser()) {
            throw new ForbiddenException("허용되지 않는 사용자입니다.");
        }
    }
}
