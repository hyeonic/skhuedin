package com.skhuedin.skhuedin.controller.api;

import com.skhuedin.skhuedin.controller.response.BasicResponse;
import com.skhuedin.skhuedin.controller.response.CommonResponse;
import com.skhuedin.skhuedin.controller.response.ErrorResponse;
import com.skhuedin.skhuedin.dto.blog.BlogMainResponseDto;
import com.skhuedin.skhuedin.dto.blog.BlogSaveRequestDto;
import com.skhuedin.skhuedin.dto.file.FileSaveRequestDto;
import com.skhuedin.skhuedin.infra.MyRole;
import com.skhuedin.skhuedin.service.BlogService;
import com.skhuedin.skhuedin.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BlogApiController {

    private final BlogService blogService;
    private final FileService fileService;

    @Value("${resources.storage_location}")
    private String resourcesLocation;

    //    @MyRole
    @PostMapping("blogs")
    public ResponseEntity<? extends BasicResponse> save(
            @RequestParam(name = "file", required = false) MultipartFile files,
            @Valid BlogSaveRequestDto requestDto) throws NoSuchAlgorithmException, IOException {

        if (blogService.existsByUserId(requestDto.getUserId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse<>("이미 책장이 존재합니다.", "400"));
        }

        Long saveId;
        if (files != null && !files.isEmpty()) {
            String originalFileName = files.getOriginalFilename();
            String extension = originalFileName.split("\\.")[1];
            String fileName = UUID.randomUUID() + "." + extension;
            String savePath = resourcesLocation;
            if (!new File(savePath).exists()) {
                new File(savePath).mkdir();
            }
            String filePath = savePath + "/" + fileName;
            files.transferTo(new File(filePath));

            FileSaveRequestDto profile = FileSaveRequestDto
                    .builder()
                    .originalName(originalFileName)
                    .name(fileName)
                    .path("/profile/" + fileName)
                    .build();

            Long fileId = fileService.save(profile);
            saveId = blogService.save(requestDto, fileId);
        } else {
            // default image 처리
            saveId = blogService.save(requestDto, 1L);
        }

        PageRequest pageRequest = PageRequest.of(0, 10);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CommonResponse<>(blogService.findById(saveId, pageRequest)));
    }

    @GetMapping("blogs/{blogId}")
    public ResponseEntity<? extends BasicResponse> findById(@PathVariable("blogId") Long id, Pageable pageable) {
        BlogMainResponseDto responseDto = blogService.findById(id, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(responseDto));
    }

    @GetMapping("blogs")
    public ResponseEntity<? extends BasicResponse> findAll(@RequestParam(value = "cmd", defaultValue = "") String cmd,
                                                           Pageable pageable) {
        Page<BlogMainResponseDto> blogs;
        if (cmd.equals("view")) {
            blogs = blogService.findAllOrderByPostsView(pageable);
        } else {
            blogs = blogService.findAll(pageable);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(blogs));
    }

    //    @MyRole
    @PutMapping("blogs/{blogId}")
    public ResponseEntity<? extends BasicResponse> update(
            @PathVariable("blogId") Long id,
            @RequestParam(name = "file", required = false) MultipartFile files,
            BlogSaveRequestDto updateDto) throws NoSuchAlgorithmException, IOException {

        Long blogId;
        if (files != null && !files.isEmpty()) {
            String originalFileName = files.getOriginalFilename();
            String extension = originalFileName.split("\\.")[1];
            String fileName = UUID.randomUUID() + "." + extension;
            String savePath = resourcesLocation;
            if (!new File(savePath).exists()) {
                new File(savePath).mkdir();
            }
            String filePath = savePath + "/" + fileName;
            files.transferTo(new File(filePath));

            FileSaveRequestDto profile = FileSaveRequestDto
                    .builder()
                    .originalName(originalFileName)
                    .name(fileName)
                    .path("/profile/" + fileName)
                    .build();

            Long fileId = fileService.save(profile);
            blogId = blogService.update(id, updateDto, fileId);
        } else {
            // default image 처리
            blogId = blogService.update(id, updateDto, 1L);
        }
        PageRequest pageRequest = PageRequest.of(0, 10);
        BlogMainResponseDto responseDto = blogService.findById(blogId, pageRequest);

        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(responseDto));
    }

    @MyRole
    @DeleteMapping("blogs/{blogId}")
    public ResponseEntity<? extends BasicResponse> delete(@PathVariable("blogId") Long id) {
        blogService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}