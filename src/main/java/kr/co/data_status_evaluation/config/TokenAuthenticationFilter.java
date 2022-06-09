package kr.co.data_status_evaluation.config;

import kr.co.data_status_evaluation.model.Account;
import kr.co.data_status_evaluation.model.Institution;
import kr.co.data_status_evaluation.repository.AccountRepository;
import kr.co.data_status_evaluation.repository.InstitutionRepository;
import kr.co.data_status_evaluation.service.AccountService;
import kr.co.data_status_evaluation.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final String TOKEN = "LMS_EVAL_SYSTEM_LOGIN";

    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher(
            "/lms/login", "GET");

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private InstitutionRepository institutionRepository;
    @Autowired
    private LogService logService;

    public TokenAuthenticationFilter() {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
    }

    public TokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        String username = getUserId(request);
        String token = request.getParameter("token");

        validateUsernameAndToken(username, token);

        UserDetails userDetails = this.accountService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

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

        return authentication;
    }

    private String getUserId(HttpServletRequest httpRequest) {
        String userId = httpRequest.getParameter("userId");
        if (userId == null) {
            return "";
        }

        return userId;
    }

    private boolean validateUsernameAndToken(String username, String token) {
        try {
            accountService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            this.logger.debug("Failed to find user '" + username + "'");
            throw new BadCredentialsException(this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "해당 사용자가 존재하지 않습니다."));
        }

        if (token.equals(TOKEN)) {
            return true;
        }

        this.logger.debug("token match Failure" + token);
        throw new BadCredentialsException(this.messages
                .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "잘못된 접근 방식입니다."));
    }
}
