package com.skhuedin.skhuedin.controller.api;

import com.skhuedin.skhuedin.controller.response.BasicResponse;
import com.skhuedin.skhuedin.controller.response.CommonResponse;
import com.skhuedin.skhuedin.dto.question.QuestionMainResponseDto;
import com.skhuedin.skhuedin.dto.question.QuestionSaveRequestDto;
import com.skhuedin.skhuedin.security.AuthService;
import com.skhuedin.skhuedin.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestionApiController {

    private final QuestionService questionService;
    private final AuthService authService;

    @PostMapping("questions")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<? extends BasicResponse> save(@Valid @RequestBody QuestionSaveRequestDto requestDto) {

        if (!authService.isSameUser(requestDto.getWriterUserId())) {
            throw new RuntimeException("동일하지 않은 유저 정보입니다.");
        }

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
    public ResponseEntity<? extends BasicResponse> findByTargetId(
            @PathVariable("targetUserId") Long id, Pageable pageable) {

        Page<QuestionMainResponseDto> questions = questionService.findByTargetUserId(id, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(questions));
    }

    @PutMapping("questions/{questionId}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<? extends BasicResponse> update(@PathVariable("questionId") Long id,
                                                          @Valid @RequestBody QuestionSaveRequestDto updateDto) {

        if (!authService.isSameUser(updateDto.getWriterUserId())) {
            throw new RuntimeException("동일하지 않은 유저 정보입니다.");
        }

        Long questionId = questionService.update(id, updateDto);
        QuestionMainResponseDto responseDto = questionService.findById(questionId);

        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(responseDto));
    }

    @DeleteMapping("questions/{questionId}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<? extends BasicResponse> delete(@PathVariable("questionId") Long id) {

        QuestionMainResponseDto responseDto = questionService.findById(id);

        if (!authService.isSameUser(responseDto.getWriterUser().getId())) {
            throw new RuntimeException("동일하지 않은 유저 정보입니다.");
        }

        questionService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("questions/{questionId}/views")
    public ResponseEntity<? extends BasicResponse> addView(@PathVariable("questionId") Long id) {
        questionService.addView(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}