package com.kh.mallapi.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.kh.mallapi.domain.Todo;
import com.kh.mallapi.dto.PageRequestDTO;
import com.kh.mallapi.dto.PageResponseDTO;
import com.kh.mallapi.dto.TodoDTO;
import com.kh.mallapi.repository.TodoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@Log4j2
//final 필드를 대상으로 생성자를 자동 생성하여 의존성 주입(Constructor Injection)
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {
	// 자동주입 대상은 final로
	private final ModelMapper modelMapper;
	private final TodoRepository todoRepository;

	@Override
	public Long register(TodoDTO todoDTO) {
		log.info("register todo: {}", todoDTO);

		// DTO → Entity 변환
		Todo todo = modelMapper.map(todoDTO, Todo.class);
		// 반대 TodoDTO todoDTO = modelMapper.map(todo, TodoDTO.class);

		// DB 저장
		Todo savedTodo = todoRepository.save(todo);

		// 생성된 PK 반환
		return savedTodo.getTno();
	}

	@Override
	public TodoDTO get(Long tno) {
		Optional<Todo> result = todoRepository.findById(tno);
		Todo todo = result.orElseThrow();
		TodoDTO dto = modelMapper.map(todo, TodoDTO.class);
		return dto;
	}

	@Override
	public void modify(TodoDTO todoDTO) {
		Optional<Todo> result = todoRepository.findById(todoDTO.getTno());
		Todo todo = result.orElseThrow();
		todo.changeTitle(todoDTO.getTitle());
		todo.changeDueDate(todoDTO.getDueDate());
		todo.changeComplete(todoDTO.isComplete());
		todoRepository.save(todo);

	}

	@Override
	public void remove(Long tno) {
		todoRepository.deleteById(tno);

	}

	@Override
	public PageResponseDTO<TodoDTO> list(PageRequestDTO pageRequestDTO) {
		Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize(),
				Sort.by("tno").descending());
		// 1페이지에 해당되는 레코드 10개를 가져온다.
		Page<Todo> result = todoRepository.findAll(pageable);
		// 1페이지에 해당되는 10개레코드를 가져온다.
		List<TodoDTO> dtoList = result.getContent().stream().map(todo -> modelMapper.map(todo, TodoDTO.class))
				.collect(Collectors.toList());
		// 전체레코드수를 구함
		long totalCount = result.getTotalElements();

		PageResponseDTO<TodoDTO> responseDTO = PageResponseDTO.<TodoDTO>withAll().dtoList(dtoList)
				.pageRequestDTO(pageRequestDTO).totalCount(totalCount).build();
		return responseDTO;

	}

}
