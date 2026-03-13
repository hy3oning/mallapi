package com.kh.mallapi.service;

import org.springframework.transaction.annotation.Transactional;

import com.kh.mallapi.dto.PageRequestDTO;
import com.kh.mallapi.dto.PageResponseDTO;
import com.kh.mallapi.dto.ProductDTO;

@Transactional
public interface ProductService {
	PageResponseDTO<ProductDTO> getList(PageRequestDTO pageRequestDTO);

	Long register(ProductDTO productDTO);

	ProductDTO get(Long pno);

	void modify(ProductDTO productDTO);

	void remove(Long pno);
}
