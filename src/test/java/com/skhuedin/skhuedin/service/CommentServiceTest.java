package com.skhuedin.skhuedin.service;

import com.skhuedin.skhuedin.domain.comment.Comment;
import com.skhuedin.skhuedin.domain.user.Provider;
import com.skhuedin.skhuedin.domain.question.Question;
import com.skhuedin.skhuedin.domain.user.User;
import com.skhuedin.skhuedin.dto.comment.CommentMainResponseDto;
import com.skhuedin.skhuedin.dto.comment.CommentSaveRequestDto;
import com.skhuedin.skhuedin.repository.CommentRepository;
import com.skhuedin.skhuedin.repository.QuestionRepository;
import com.skhuedin.skhuedin.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql("/truncate.sql")
class CommentServiceTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentService commentService;

    Question question;
    User targetUser;
    User writerUser;

    @BeforeEach
    void beforeEach() {
        targetUser = User.builder()
                .email("target@email.com")
                .name("target")
                .userImageUrl("/img")
                .provider(Provider.SELF)
                .build();

        writerUser = User.builder()
                .email("writer@email.com")
                .name("writer")
                .userImageUrl("/img")
                .provider(Provider.SELF)
                .build();

        userRepository.save(targetUser);
        userRepository.save(writerUser);

        question = Question.builder()
                .targetUser(targetUser)
                .writerUser(writerUser)
                .title("질문 1")
                .content("질문1의 질문 내용")
                .build();

        questionRepository.save(question);
    }

    @Test
    @DisplayName("dto 를 받아 comment 를 저장하고 조회하는 테스트")
    void save() {

        // given
        CommentSaveRequestDto requestDto = generateDto("parent 댓글");

        // when
        Long saveId = commentService.save(requestDto);
        Comment comment = commentRepository.findById(saveId).orElse(null);

        // then
        assertAll(
                () -> assertEquals(comment.getId(), saveId),
                () -> assertEquals(comment.getQuestion().getId(), requestDto.getQuestionId()),
                () -> assertEquals(comment.getWriterUser().getId(), requestDto.getWriterUserId())
        );
    }

    @Test
    @DisplayName("존재하지 않는 question 으로 인하여 저장하던 중 예외를 던지는 테스트")
    void save_question_false() {

        // given
        CommentSaveRequestDto requestDto = CommentSaveRequestDto.builder()
                .questionId(0L) // 존재하지 않는 question
                .writerUserId(writerUser.getId())
                .content("parent 댓글")
                .build();

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                commentService.save(requestDto)
        );
    }

    @Test
    @DisplayName("존재하지 않는 user 로 인하여 저장하던 중 예외를 던지는 테스트")
    void save_user_false() {

        // given
        CommentSaveRequestDto requestDto = CommentSaveRequestDto.builder()
                .questionId(question.getId())
                .writerUserId(0L) // 존재하지 않는 user
                .content("parent 댓글")
                .build();

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                commentService.save(requestDto)
        );
    }

    @Test
    @DisplayName("comment 를 갱신하고 조회하는 테스트")
    void update() {

        // given
        CommentSaveRequestDto requestDto = generateDto("parent 댓글");
        CommentSaveRequestDto updateDto = generateDto("수정된 parent 댓글");

        // when
        Long saveId = commentService.save(requestDto);
        Long updateId = commentService.update(saveId, updateDto);

        Comment updateComment = commentRepository.findById(updateId).get();

        // then
        assertAll(
                () -> assertEquals(updateComment.getId(), saveId),
                () -> assertEquals(updateComment.getQuestion().getId(), updateDto.getQuestionId()),
                () -> assertEquals(updateComment.getWriterUser().getId(), updateDto.getWriterUserId()),
                () -> assertEquals(updateComment.getContent(), updateDto.getContent())
        );
    }

    @Test
    @DisplayName("존재하지 않는 question 으로 인하여 갱신하던 중 예외를 던지는 테스트")
    void update_question_false() {

        // given
        CommentSaveRequestDto requestDto = generateDto("parent 댓글");
        CommentSaveRequestDto updateDto = CommentSaveRequestDto.builder()
                .questionId(0L) // 존재하지 않는 question
                .writerUserId(writerUser.getId())
                .content("수정된 parent 댓글")
                .build();
        Long saveId = commentService.save(requestDto);

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                commentService.update(saveId, updateDto)
        );
    }

    @Test
    @DisplayName("존재하지 않는 user 로 인하여 갱신하던 중 예외를 던지는 테스트")
    void update_user_false() {

        // given
        CommentSaveRequestDto requestDto = generateDto("parent 댓글");
        CommentSaveRequestDto updateDto = CommentSaveRequestDto.builder()
                .questionId(question.getId())
                .writerUserId(0L) // 존재하지 않는 user
                .content("수정된 parent 댓글")
                .build();
        Long saveId = commentService.save(requestDto);

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                commentService.update(saveId, updateDto)
        );
    }

    @Test
    @DisplayName("comment 를 삭제하는 테스트")
    void delete() {

        // given
        CommentSaveRequestDto requestDto = generateDto("parent 댓글");
        Long saveId = commentService.save(requestDto);

        // when
        commentService.delete(saveId);

        // then
        assertEquals(commentRepository.findAll().size(), 0);
    }

    @Test
    @DisplayName("존재하지 않는 comment 를 삭제하여 예외를 던지는 테스트")
    void delete_false() {

        // given & when & then
        assertThrows(IllegalArgumentException.class, () ->
                commentService.delete(0L)
        );
    }

    @Test
    @DisplayName("question 별 댓글 목록 조회하는 테스트")
    void findByQuestionId() {

        // given
        CommentSaveRequestDto requestDto = generateDto("댓글");
        commentService.save(requestDto);

        // when
        List<CommentMainResponseDto> comments = commentService.findByQuestionId(question.getId());

        // then
        assertAll(
                () -> assertEquals(comments.size(), 1)
        );
    }

    private CommentSaveRequestDto generateDto(String content) {
        return CommentSaveRequestDto.builder()
                .questionId(question.getId())
                .writerUserId(writerUser.getId())
                .content(content)
                .build();
    }

    @AfterEach
    void afterEach() {
        commentRepository.deleteAll();
        questionRepository.deleteAll();
        userRepository.deleteAll();
    }
}