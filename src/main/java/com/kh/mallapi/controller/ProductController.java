package com.kh.mallapi.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.mallapi.dto.PageRequestDTO;
import com.kh.mallapi.dto.PageResponseDTO;
import com.kh.mallapi.dto.ProductDTO;
import com.kh.mallapi.service.ProductService;
import com.kh.mallapi.util.CustomFileUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/products")
public class ProductController {
	private final CustomFileUtil fileUtil;
	private final ProductService productService;

	@PostMapping("/")
	public Map<String, Long> register(ProductDTO productDTO) {
		log.info("rgister: " + productDTO);
		List<MultipartFile> files = productDTO.getFiles();
		List<String> uploadFileNames = fileUtil.saveFiles(files);
		productDTO.setUploadFileNames(uploadFileNames);
		log.info(uploadFileNames);
		// 서비스 호출
		Long pno = productService.register(productDTO);
		try {
			Thread.sleep(500);
			} catch (InterruptedException e){
			e.printStackTrace();
			}
		return Map.of("result", pno);
	}

	@GetMapping("/view/{fileName}")
	public ResponseEntity<Resource> viewFileGET(@PathVariable String fileName) {
		return fileUtil.getFile(fileName);
	}

	@GetMapping("/list")
	public PageResponseDTO<ProductDTO> list(PageRequestDTO pageRequestDTO) {
		log.info("list............." + pageRequestDTO);
		try {
			Thread.sleep(500);
			} catch (InterruptedException e) {
			e.printStackTrace();
			}
		return productService.getList(pageRequestDTO);
	}

	@GetMapping("/{pno}")
	public ProductDTO read(@PathVariable Long pno) {
		return productService.get(pno);
	}

	@PutMapping("/{pno}")
	public Map<String, String> modify(@PathVariable Long pno, ProductDTO productDTO) {
		productDTO.setPno(pno);

		// 수정 전 기존 상품 조회
		ProductDTO oldProductDTO = productService.get(pno);
		// 기존 파일 목록
		List<String> oldFileNames = oldProductDTO.getUploadFileNames();

		// 새로 업로드된 파일 저장
		List<MultipartFile> files = productDTO.getFiles();
		List<String> newUploadedFileNames = fileUtil.saveFiles(files);

		// 화면에서 유지할 파일 목록
		List<String> keptFileNames = productDTO.getUploadFileNames();
		if (keptFileNames == null) {
			keptFileNames = new ArrayList<>();
		}

		// 최종 파일 목록 = 유지 파일 + 새 업로드 파일
		List<String> finalFileNames = new ArrayList<>(keptFileNames);
		if (newUploadedFileNames != null && !newUploadedFileNames.isEmpty()) {
			finalFileNames.addAll(newUploadedFileNames);
		}

		// 최종 파일 목록을 DTO에 반영
		productDTO.setUploadFileNames(finalFileNames);

		// DB 수정
		productService.modify(productDTO);

		// 삭제할 기존 파일 찾기
		if (oldFileNames != null && !oldFileNames.isEmpty()) {
			List<String> removeFiles = oldFileNames.stream().filter(fileName -> !finalFileNames.contains(fileName))
					.collect(Collectors.toList());

			fileUtil.deleteFiles(removeFiles);
		}

		return Map.of("RESULT", "SUCCESS");
	}

	@DeleteMapping("/{pno}")
	public Map<String, String> remove(@PathVariable Long pno) {
		// 삭제해야 할 파일들 알아내기 논리삭제할때는필요없음 물리삭제원하면 따로 메서드 생성
		List<String> oldFileNames = productService.get(pno).getUploadFileNames();
		productService.remove(pno);
		fileUtil.deleteFiles(oldFileNames);
		return Map.of("RESULT", "SUCCESS");
	}

}
