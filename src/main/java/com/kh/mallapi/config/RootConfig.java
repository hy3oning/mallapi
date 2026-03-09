package com.kh.mallapi.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RootConfig {

	@Bean
	// DTO ↔ Entity 자동 매핑을 더 자유롭게 하기 위한 설정
	ModelMapper getMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setFieldMatchingEnabled(true) // getter/setter 없이 필드 기준 매핑 허용
				.setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE) // private 필드까지 접근하여 매핑
				.setMatchingStrategy(MatchingStrategies.LOOSE); // 필드명이 완전히 같지 않아도 유사하면 매핑
		return modelMapper;
	}

}
