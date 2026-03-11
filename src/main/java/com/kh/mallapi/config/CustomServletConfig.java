package com.kh.mallapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.kh.mallapi.controller.formatter.LocalDateFormatter;

@Configuration
public class CustomServletConfig implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addFormatter(new LocalDateFormatter());
	}

	/**
	 * CORS(Cross-Origin Resource Sharing) 설정
	 *
	 * 1. addMapping("/**")
	 * : 모든 URL 경로에 대해 CORS를 허용
	 * 예) /api/**, /user/**, /todo/** 등 전체 허용
	 *
	 * 2. allowedOrigins("*")
	 * : 모든 출처(Origin)에서 오는 요청을 허용
	 * 예) http://localhost:3000, http://127.0.0.1:5173, https://mydomain.com
	 *
	 * 3. allowedMethods("HEAD", "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
	 * : 허용할 HTTP 메서드를 지정
	 * - GET : 조회
	 * - POST : 등록
	 * - PUT : 전체 수정
	 * - PATCH : 일부 수정
	 * - DELETE : 삭제
	 * - OPTIONS : 브라우저의 사전 요청(Preflight)
	 * - HEAD : 응답 본문 없이 헤더만 확인하는 요청
	 *
	 * 4. maxAge(300)
	 * : 브라우저가 Preflight(OPTIONS) 요청 결과를 300초(5분) 동안 캐시
	 *
	 * 5. allowedHeaders("Authorization", "Cache-Control", "Content-Type")
	 * : 클라이언트가 요청 시 사용할 수 있는 헤더를 지정
	 * - Authorization : JWT, Access Token 등 인증 정보
	 * - Cache-Control : 캐시 제어
	 * - Content-Type : 요청 본문의 데이터 형식 지정
	 * 예) application/json
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOrigins("*")
				.allowedMethods("HEAD", "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH").maxAge(300)
				.allowedHeaders("Authorization", "Cache-Control", "Content-Type");
	}

}
