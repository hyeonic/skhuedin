package com.skhuedin.skhuedin.controller;

import com.skhuedin.skhuedin.controller.response.BasicResponse;
import com.skhuedin.skhuedin.controller.response.CommonResponse;
import com.skhuedin.skhuedin.controller.response.ErrorResponse;
import com.skhuedin.skhuedin.dto.question.QuestionMainResponseDto;
import com.skhuedin.skhuedin.dto.question.QuestionSaveRequestDto;
import com.skhuedin.skhuedin.service.QuestionService;
import com.skhuedin.skhuedin.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestionApiController {

    private final QuestionService questionService;

    @PostMapping("questions")
    public ResponseEntity<? extends BasicResponse> save(@RequestBody QuestionSaveRequestDto requestDto) {
        Long saveId = questionService.save(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CommonResponse<>(questionService.findById(saveId)));
    }

    @GetMapping("questions/{questionId}")
    public ResponseEntity<? extends BasicResponse> findById(@PathVariable("questionId") Long id) {
        QuestionMainResponseDto responseDto = questionService.findById(id);

        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(responseDto));
    }

    @GetMapping("users/{targetUserId}/questions")
    public ResponseEntity<? extends BasicResponse> findByTargetId(@PathVariable("targetUserId") Long id) {
        List<QuestionMainResponseDto> questions = questionService.findByTargetUserId(id);

        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(questions));
    }

    @PutMapping("questions/{questionId}")
    public ResponseEntity<? extends BasicResponse> update(@PathVariable("questionId") Long id,
                                                          @RequestBody QuestionSaveRequestDto updateDto) {
        Long questionId = questionService.update(id, updateDto);
        QuestionMainResponseDto responseDto = questionService.findById(questionId);

        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(responseDto));
    }

    @DeleteMapping("questions/{questionId}")
    public ResponseEntity<? extends BasicResponse> delete(@PathVariable("questionId") Long id) {
        questionService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new CommonResponse<>("삭제에 성공하였습니다."));
    }

    @PostMapping("questions/{questionId}/views")
    public ResponseEntity<? extends BasicResponse> addView(@PathVariable("questionId") Long id) {
        questionService.addView(id);
        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(id + " 게시물의 조회수가 증가하였습니다."));
    }
}