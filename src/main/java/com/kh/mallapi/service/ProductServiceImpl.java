package com.kh.mallapi.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mallapi.domain.Product;
import com.kh.mallapi.domain.ProductImage;
import com.kh.mallapi.dto.PageRequestDTO;
import com.kh.mallapi.dto.PageResponseDTO;
import com.kh.mallapi.dto.ProductDTO;
import com.kh.mallapi.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;

	@Override
	public PageResponseDTO<ProductDTO> getList(PageRequestDTO pageRequestDTO) {
		log.info("getList ");
		// PageRequest는 페이지 번호를 0부터 처리하므로 사용자가 요청한 페이지 번호에서 1을 뺀다.
		Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize(),
				Sort.by("pno").descending());
		// 상품 + 상품이미지 목록을 페이징 조회
		Page<Object[]> result = productRepository.selectList(pageable);

		// 조회 결과(Entity)를 화면 전송용 DTO로 변환
		List<ProductDTO> dtoList = result.get().map(arr -> {
			Product product = (Product) arr[0];
			ProductImage productImage = (ProductImage) arr[1];

			// Product 엔티티에서 필요한 값만 꺼내서 DTO 생성
			ProductDTO productDTO = ProductDTO.builder().pno(product.getPno()) // 상품 번호
					.pname(product.getPname()) // 상품명
					.pdesc(product.getPdesc()) // 상품 설명
					.price(product.getPrice()) // 상품 가격
					.build();
			if (productImage != null) {
				// 상품 이미지 객체에서 파일명만 꺼냄
				String imageStr = productImage.getFileName();
				// DTO의 uploadFileNames 필드에 파일명 저장
				productDTO.setUploadFileNames(List.of(imageStr));
			}
			return productDTO;
		}).collect(Collectors.toList());
		// 전체 데이터 개수
		long totalCount = result.getTotalElements();
		// 페이지 응답 DTO 생성 후 반환
		return PageResponseDTO.<ProductDTO>withAll().dtoList(dtoList) // 현재 페이지 데이터 목록
				.totalCount(totalCount)// 전체 데이터 수
				.pageRequestDTO(pageRequestDTO)// 사용자가 요청한 페이지 정보
				.build();

	}

	@Override
	public Long register(ProductDTO productDTO) {
		Product product = dtoToEntity(productDTO);
		Product result = productRepository.save(product);
		return result.getPno();

	}

	private Product dtoToEntity(ProductDTO productDTO) {
		Product product = Product.builder().pno(productDTO.getPno()).pname(productDTO.getPname())
				.pdesc(productDTO.getPdesc()).price(productDTO.getPrice()).build();
		// 업로드 처리가 끝난 파일들의 이름 리스트
		List<String> uploadFileNames = productDTO.getUploadFileNames();
		if (uploadFileNames == null) {
			return product;
		}
		uploadFileNames.stream().forEach(uploadName -> {
			product.addImageString(uploadName);
		});
		return product;
	}

	@Override
	public ProductDTO get(Long pno) {
		Optional<Product> result = productRepository.selectOne(pno);
		Product product = result.orElseThrow();
		ProductDTO productDTO = entityToDTO(product);
		return productDTO;
	}

	private ProductDTO entityToDTO(Product product) {
		ProductDTO productDTO = ProductDTO.builder().pno(product.getPno()).pname(product.getPname())
				.pdesc(product.getPdesc()).price(product.getPrice()).build();
		List<ProductImage> imageList = product.getImageList();
		if (imageList == null || imageList.isEmpty()) {
			return productDTO;
		}
		List<String> fileNameList = imageList.stream().map(productImage -> productImage.getFileName()).toList();
		productDTO.setUploadFileNames(fileNameList);
		return productDTO;
	}

	@Override
	public void modify(ProductDTO productDTO) {
		// 수정할 상품 조회
		Optional<Product> result = productRepository.findById(productDTO.getPno());
		Product product = result.orElseThrow();

		// 기본 정보 수정
		product.changeName(productDTO.getPname());
		product.changeDesc(productDTO.getPdesc());
		product.changePrice(productDTO.getPrice());

		// 기존 이미지 목록 비우기
		product.clearList();

		// 새 이미지 목록 다시 추가
		List<String> uploadFileNames = productDTO.getUploadFileNames();
		if (uploadFileNames != null && !uploadFileNames.isEmpty()) {
			uploadFileNames.forEach(uploadName -> {
				product.addImageString(uploadName);
			});
		}

		// 수정 내용 저장
		productRepository.save(product);
	}

	@Override
	public void remove(Long pno) {
		productRepository.updateToDelete(pno, true);
	}
}
