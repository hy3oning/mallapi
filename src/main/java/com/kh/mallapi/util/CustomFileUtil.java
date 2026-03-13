package com.kh.mallapi.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;

@Component
@Log4j2
@RequiredArgsConstructor
public class CustomFileUtil {

	@Value("${com.kh.upload.path}")
	private String uploadPath;

	@PostConstruct
	/**
	 * 파일 업로드용 폴더가 없으면 자동으로 생성하는 초기화 로직
	 * 생성자 → 의존성 주입(DI) → @PostConstruct 메서드 실행
	 * uploadPath 값이 주입된 이후에 실행됨.
	 */
	public void init() {
		File tempFolder = new File(uploadPath);
		if (!tempFolder.exists()) {
			tempFolder.mkdirs();
		}
		uploadPath = tempFolder.getAbsolutePath();
		log.info("Upload Path initialized: " + uploadPath);
	}

	public List<String> saveFiles(List<MultipartFile> files) throws RuntimeException {
		// 업로드할 파일이 없으면 빈 리스트 반환
		if (files == null || files.isEmpty()) {
			return Collections.emptyList();
		}
		// 저장된 파일명들을 담을 리스트
		List<String> uploadNames = new ArrayList<>();
		// 전달받은 파일들을 하나씩 처리
		for (MultipartFile multipartFile : files) {
			// 비어 있는 파일은 건너뜀
			if (multipartFile.isEmpty()) {
				continue;
			}
			// 원본 파일명 가져오기
			String originalName = multipartFile.getOriginalFilename();
			// 파일명이 없으면 기본 이름 사용, 있으면 경로 제거
			if (originalName == null || originalName.isBlank()) {
				originalName = "unknown";
			} else {
				originalName = Paths.get(originalName).getFileName().toString();
			}
			// UUID를 붙여 중복되지 않는 저장 파일명 생성
			String savedName = UUID.randomUUID() + "_" + originalName;
			// 실제 저장 경로 생성
			Path savePath = Paths.get(uploadPath, savedName);

			try {
				// 원본 파일 저장
				Files.copy(multipartFile.getInputStream(), savePath);
				// 저장 정보 로그 출력
				log.info("saved file: {}", savedName);
				log.info("original name: {}", originalName);
				log.info("content type: {}", multipartFile.getContentType());
				log.info("size: {}", multipartFile.getSize());

				// 이미지 파일이면 썸네일 생성 시도
				String contentType = multipartFile.getContentType();
				if (contentType != null && contentType.startsWith("image/")) {
					try {
						Path thumbnailPath = Paths.get(uploadPath, "s_" + savedName);
						Thumbnails.of(savePath.toFile()).size(400, 400).toFile(thumbnailPath.toFile());
					} catch (Exception e) {
						// 썸네일 생성 실패는 로그만 남기고 계속 진행
						log.warn("Thumbnail creation failed for file: {}", savedName, e);
					}
				}
				// 저장된 파일명 리스트에 추가
				uploadNames.add(savedName);

			} catch (IOException e) {
				// 원본 파일 저장 실패 시 예외 발생
				throw new RuntimeException("File save error", e);
			}
		}
		// 저장 완료된 파일명 목록 반환
		return uploadNames;
	}

	public ResponseEntity<Resource> getFile(String fileName) {
		try {
			// 경로 제거, 파일명만 남김
			fileName = Paths.get(fileName).getFileName().toString();
			// 업로드 폴더에서 파일 찾기
			Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);
			// 파일 없으면 기본 이미지로 대체
			if (!resource.exists()) {
				resource = new FileSystemResource(uploadPath + File.separator + "default.jpg");
			}
			// 파일 타입 확인
			String contentType = Files.probeContentType(resource.getFile().toPath());
			// 타입을 못 찾으면 기본 타입 사용
			if (contentType == null) {
				contentType = "application/octet-stream";
			}
			// 응답 헤더에 파일 타입 추가
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, contentType);
			// 파일 응답 반환
			return ResponseEntity.ok().headers(headers).body(resource);

		} catch (Exception e) {
			// 예외 발생 시 500 에러 반환
			return ResponseEntity.internalServerError().build();
		}
	}

	public void deleteFiles(List<String> fileNames) {
		// 삭제할 파일이 없으면 종료
		if (fileNames == null || fileNames.isEmpty()) {
			return;
		}
		fileNames.forEach(fileName -> {
			try {
				// 경로 조작 방지: 전달받은 값에서 파일명만 추출
				// 예: ../../test.jpg -> test.jpg
				String safeFileName = Paths.get(fileName).getFileName().toString();
				// 원본 파일 경로
				Path filePath = Paths.get(uploadPath, safeFileName);
				// 썸네일 파일 경로
				Path thumbnailPath = Paths.get(uploadPath, "s_" + safeFileName);

				// 원본 파일이 있으면 삭제
				Files.deleteIfExists(filePath);

				// 썸네일 파일이 있으면 삭제
				Files.deleteIfExists(thumbnailPath);

			} catch (IOException e) {
				// 체크 예외를 런타임 예외로 변환
				throw new RuntimeException("파일 삭제 중 오류 발생: " + fileName, e);
			}
		});
	}
}
