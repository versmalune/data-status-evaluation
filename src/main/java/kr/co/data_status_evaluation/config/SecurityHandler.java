package kr.co.data_status_evaluation.config;

import kr.co.data_status_evaluation.model.Account;
import kr.co.data_status_evaluation.model.Institution;
import kr.co.data_status_evaluation.repository.AccountRepository;
import kr.co.data_status_evaluation.repository.InstitutionRepository;
import kr.co.data_status_evaluation.service.LogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SecurityHandler implements AuthenticationSuccessHandler {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String TAG = "SecurityHandler=====";
    private final AccountRepository accountRepository;
    private final InstitutionRepository institutionRepository;
    private final LogService logService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        String targetUrl = httpServletRequest.getContextPath();

//        Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
//        boolean isAdmin = false;
//        logger.debug(TAG + "checking roles");
//        for (SimpleGrantedAuthority authority : authorities) {
//            logger.debug(TAG + authority.getAuthority());
//            if (authority.getAuthority().equals(Author.ADMIN.authority())) {
//                logger.debug(TAG + "it is admin!");
//                isAdmin = true;
//                break;
//            }
//        }

        Account account = accountRepository.findOneWithRolesAndInstitutionByUserId(authentication.getName());
        if (!Objects.isNull(account)) {
            account.setLastAccessDt(Instant.now());
            logService.logLogin(account);
            accountRepository.save(account);
            if (account.isInstitution() && !Objects.isNull(account.getInstitution())) {
                Institution institution = account.getInstitution();
                institution.setLastAccessDt(Instant.now());
                institutionRepository.save(institution);
            }
        }

        httpServletResponse.sendRedirect(targetUrl);
//        if (isAdmin) {
//            httpServletResponse.sendRedirect(String.format("%s/%s", targetUrl, "/"));
//        } else {
//            httpServletResponse.sendRedirect(String.format("%s/%s", targetUrl, "/"));
//        }
    }
}
