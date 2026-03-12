package com.kh.mallapi.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductImage {
	
	// 이미지 파일명
	private String fileName;
	// 이미지 파일순서
	private int ord;

	public void setOrd(int ord) {
		this.ord = ord;
	}

}
