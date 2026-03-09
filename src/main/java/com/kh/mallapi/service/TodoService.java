package com.kh.mallapi.service;

import com.kh.mallapi.dto.PageRequestDTO;
import com.kh.mallapi.dto.PageResponseDTO;
import com.kh.mallapi.dto.TodoDTO;

public interface TodoService {
	// insert(save)
	Long register(TodoDTO todoDTO);

	// select
	TodoDTO get(Long tno);

	// update
	void modify(TodoDTO todoDTO);

	// delete
	void remove(Long tno);

	// 페이징기법
	PageResponseDTO<TodoDTO> list(PageRequestDTO pageRequestDTO);

}
