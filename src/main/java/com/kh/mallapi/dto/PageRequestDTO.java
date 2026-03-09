package com.kh.mallapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
// Lombok Builder 확장 버전 (상속 클래스에서도 builder 사용 가능)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PageRequestDTO {
	@Builder.Default
	private int page = 1;
	@Builder.Default
	private int size = 10;
}
