package kr.co.data_status_evaluation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final SecurityHandler securityHandler;

    @Bean
    public SHA256CodeEncoder sha256CodeEncoder() {
        return new SHA256CodeEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http

                .headers().frameOptions().sameOrigin()
                .and()
                    .csrf()
                    .csrfTokenRepository(new HttpSessionCsrfTokenRepository())
                    .ignoringAntMatchers("/rest/**", "/lms/login")
                .and()
                    .authorizeRequests()
//                    .antMatchers("/private/**").authenticated() //일반 사용자의 로그인이 필요한 모든 action 은 private 으로 시작
//                    .antMatchers("/admin/**").hasRole("ADMIN")
//                    .antMatchers("/committee/**").hasAnyRole("COMMITTEE", "ADMIN")
//                    .antMatchers("/instt/**").hasAnyRole("COMMITTEE", "ADMIN", "INSTITUTION")
//                    .antMatchers("/commons/**").hasAnyRole("COMMITTEE", "ADMIN", "INSTITUTION")
//                    .antMatchers("/rest/**").permitAll()
//                    .anyRequest().authenticated()
                    .antMatchers("/admin/**").authenticated()
                    .antMatchers("/committee/**").authenticated()
                    .antMatchers("/instt/**").authenticated()
                    .antMatchers("/commons/**").authenticated()
                    .anyRequest().permitAll()
                .and()
                    .formLogin()
                    .loginPage("/account/login")
                    .permitAll()
                    .defaultSuccessUrl("/")
                    .successHandler(securityHandler)
                    .failureUrl("/account/login?error=1")
                    .loginProcessingUrl("/account/authenticate")
                .and()
                    .logout()
                    .logoutUrl("/account/logout")
                    .permitAll()
                    .logoutSuccessUrl("/")
                .and()
                .addFilterAt(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() throws Exception {
        TokenAuthenticationFilter authenticationFilter = new TokenAuthenticationFilter();
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        authenticationFilter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/account/login?error=1"));

        return authenticationFilter;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/static/**", "/js/**", "/sass/**", "/css/**", "/fonts/**", "/error");
    }
}

