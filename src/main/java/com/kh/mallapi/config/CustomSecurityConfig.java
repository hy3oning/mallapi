package com.kh.mallapi.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.kh.mallapi.security.filter.JWTCheckFilter;
import com.kh.mallapi.security.handler.APILoginFailHandler;
import com.kh.mallapi.security.handler.APILoginSuccessHandler;
import com.kh.mallapi.security.handler.CustomAccessDeniedHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
@RequiredArgsConstructor
@EnableMethodSecurity
public class CustomSecurityConfig {

	/**
	 * Spring Security의 핵심 보안 필터 체인을 설정한다.
	 *
	 * 이 메서드에서는 REST API 서버에 맞는 기본 보안 정책을 구성한다.
	 * 세션을 사용하지 않는 Stateless 방식으로 설정하고,
	 * 브라우저에서 오는 요청을 처리할 수 있도록 CORS를 활성화하며,
	 * 폼 로그인 기반 애플리케이션이 아니므로 CSRF 보호는 비활성화한다.
	 *
	 * @param http Spring Security 보안 설정을 구성하기 위한 HttpSecurity 객체
	 * @return 애플리케이션에 적용될 SecurityFilterChain 객체
	 * @throws Exception 보안 설정 구성 중 예외가 발생할 수 있음
	 */
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		log.info("----------------------------securityConfig-------------------------------");
		// 프론트엔드에서 들어오는 교차 출처 요청(CORS)에 대해 아래 설정을 적용한다.
		http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer
				.configurationSource(corsConfigurationSource()));
		// 세션을 생성하지 않는 stateless 방식으로 동작한다.
		// JWT 같은 토큰 기반 인증에서 사용하며, 매 요청마다 인증 정보를 전달받는다.
		http.sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		// REST API 서버이므로 일반적인 폼 기반 CSRF 보호는 비활성화한다.
		http.csrf(config -> config.disable());
		http.formLogin(config -> {
			config.loginPage("/api/member/login");
			// 로그인 성공 시 실행될 핸들러 객체를 지정 코드
			config.successHandler(new APILoginSuccessHandler());
			// 로그인 실패 시 실행될 핸들러 객체를 지정 코드
			config.failureHandler(new APILoginFailHandler());
		});
		// JWT 체크 추가
		http.addFilterBefore(new JWTCheckFilter(), UsernamePasswordAuthenticationFilter.class);
		//권한이허가 되지 않았을때 예외처리메시지처리
		http.exceptionHandling(config -> {
			config.accessDeniedHandler(new CustomAccessDeniedHandler());
		});
		return http.build();
	}

	/**
	 * 애플리케이션에서 사용할 CORS(Cross-Origin Resource Sharing) 정책을 정의한다.
	 *
	 * 허용할 출처, HTTP 메서드, 요청 헤더를 지정하고,
	 * 이 정책을 모든 URL 패턴에 적용할 수 있도록
	 * CorsConfigurationSource 객체로 반환한다.
	 *
	 * @return Spring Security에서 사용할 CORS 설정 소스 객체
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		// CORS 세부 정책을 담을 객체를 생성한다.
		CorsConfiguration configuration = new CorsConfiguration();
		// 모든 출처를 허용한다.
		// 운영 환경에서는 보안을 위해 특정 도메인만 허용하는 것이 더 적절하다.
		configuration.setAllowedOriginPatterns(Arrays.asList("*"));
		// 브라우저에서 허용할 HTTP 메서드를 지정한다.
		configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE"));
		// 클라이언트가 요청 시 보낼 수 있는 헤더를 지정한다.
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
		// 쿠키나 Authorization 헤더 같은 자격 증명 정보를 포함한 요청을 허용한다.
		configuration.setAllowCredentials(true);
		// URL 패턴별로 CORS 설정을 매핑할 수 있는 소스 객체를 생성한다.
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		// 모든 요청 경로에 대해 위에서 정의한 CORS 정책을 적용한다.
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	/**
	 * 비밀번호를 안전하게 암호화하기 위한 PasswordEncoder를 빈으로 등록한다.
	 *
	 * BCrypt 해시 알고리즘을 사용하며,
	 * 회원가입 시 비밀번호 저장이나 로그인 시 비밀번호 검증에 사용된다.
	 *
	 * @return BCrypt 기반 PasswordEncoder 객체
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
