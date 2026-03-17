package com.kh.mallapi.security.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.gson.Gson;
import com.kh.mallapi.dto.MemberDTO;
import com.kh.mallapi.util.JWTUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JWTCheckFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		log.info("-------------------- JWTCheckFilter------------------------------------------------------ ");
		String authHeaderStr = request.getHeader("Authorization");

		try {
			// Bearer [공백] + accessToken 형태인지 먼저 체크
			if (authHeaderStr == null || !authHeaderStr.startsWith("Bearer ")) {
				throw new RuntimeException("Invalid Authorization Header");
			}

			// "Bearer " 제외한 토큰값만 추출
			String accessToken = authHeaderStr.substring(7);
			Map<String, Object> claims = JWTUtil.validateToken(accessToken);
			log.info("JWT claims: " + claims);
			// filterChain.doFilter(request, response); //이하 추가
			String email = (String) claims.get("email");
			String pw = (String) claims.get("pw");
			String nickname = (String) claims.get("nickname");
			Boolean social = (Boolean) claims.get("social");

			Object roleNamesObj = claims.get("roleNames");
			List<String> roleNames = List.of();

			if (roleNamesObj instanceof List<?> tempList) {
				roleNames = tempList.stream().filter(item -> item != null).map(String::valueOf).toList();
			}

			MemberDTO memberDTO = new MemberDTO(email, pw, nickname, social != null && social, roleNames);

			log.info("-----------------------------------------------------------------");
			log.info(memberDTO);
			log.info(memberDTO.getAuthorities());
			// 스프링 시큐리티에서 인증 정보를 담는 객체
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(memberDTO,
					pw, memberDTO.getAuthorities());
			// 이 객체를 SecurityContextHolder에 넣으면,
			// 해당 요청은 인증된 사용자로 처리됨
			SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			filterChain.doFilter(request, response);

		} catch (Exception e) {
			log.error("JWT Check Error.................................... ");
			log.error(e.getMessage());
			Gson gson = new Gson();
			String msg = gson.toJson(Map.of("error", "ERROR_ACCESS_TOKEN"));
			response.setContentType("application/json");
			PrintWriter printWriter = response.getWriter();
			printWriter.println(msg);
			printWriter.close();
		}

	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		// Preflight (지금 보내는 요청이 유효한지를 확인하기 위해 OPTIONS 메서드로 예비 요청을 보내는 것)
		if (request.getMethod().equals("OPTIONS")) {
			return true;
		}
		String path = request.getRequestURI();
		log.info("check uri.............." + path);

		// api/member/ 경로의 호출은 체크하지 않음
		if (path.startsWith("/api/member/")) {
			return true;
		}
		// 이미지 조회 경로는 체크하지 않고 싶을때
		if (path.startsWith("/api/products/view/")) {
			return true;
		}
		return false;
	}

}
