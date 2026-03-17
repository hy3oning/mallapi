package com.kh.mallapi.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mallapi.util.CustomJWTException;
import com.kh.mallapi.util.JWTUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@Log4j2
public class APIRefreshController {

	@PostMapping("/api/member/refresh")
	public Map<String, Object> refresh(@RequestHeader(value = "Authorization", required = false) String authHeader,
			@RequestParam String refreshToken) {

		if (refreshToken == null || refreshToken.isBlank()) {
			throw new CustomJWTException("NULL_REFRESH");
		}

		// Authorization 헤더가 없거나 형식이 이상하면
		// accessToken 재사용 검사는 건너뛰고 refreshToken으로만 재발급 진행
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String accessToken = authHeader.substring(7);

			// Access 토큰이 만료되지 않았다면 기존 토큰값 리턴
			if (!checkExpiredToken(accessToken)) {
				return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
			}
		}

		// Access 토큰이 없거나 만료된 경우 refresh token 검증
		Map<String, Object> claims = JWTUtil.validateToken(refreshToken);
		log.info("refresh ... claims: " + claims);

		// 새 access token 생성
		String newAccessToken = JWTUtil.generateToken(claims, 10);

		// refresh 토큰이 1시간 미만 남았으면 새 refresh token도 생성
		String newRefreshToken = checkTime((Integer) claims.get("exp")) ? JWTUtil.generateToken(claims, 60 * 24)
				: refreshToken;

		return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
	}

	// 시간이 1시간 미만으로 남았다면 true
	private boolean checkTime(Integer exp) {
		java.util.Date expDate = new java.util.Date((long) exp * 1000);
		long gap = expDate.getTime() - System.currentTimeMillis();
		long leftMin = gap / (1000 * 60);

		return leftMin < 60;
	}

	private boolean checkExpiredToken(String token) {
		try {
			JWTUtil.validateToken(token);
		} catch (CustomJWTException ex) {
			if ("Expired".equals(ex.getMessage())) {
				return true;
			}
			throw ex;
		}
		return false;
	}
}