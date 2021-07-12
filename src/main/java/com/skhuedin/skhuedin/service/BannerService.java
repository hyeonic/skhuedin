package com.skhuedin.skhuedin.service;

import com.skhuedin.skhuedin.controller.admin.form.BannerForm;
import com.skhuedin.skhuedin.domain.Banner;
import com.skhuedin.skhuedin.domain.UploadFile;
import com.skhuedin.skhuedin.dto.banner.BannerAdminMainResponseDto;
import com.skhuedin.skhuedin.file.FileStore;
import com.skhuedin.skhuedin.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;
    private final FileStore fileStore;

    @Transactional
    public Long save(BannerForm bannerForm) throws IOException {
        UploadFile uploadFile = fileStore.storeFile(bannerForm.getImageFile());

        Banner banner = Banner.builder()
                .title(bannerForm.getTitle())
                .content(bannerForm.getContent())
                .uploadFile(uploadFile)
                .build();

        return bannerRepository.save(banner).getId();
    }

    public Page<BannerAdminMainResponseDto> findAll(Pageable pageable) {
        return bannerRepository.findAll(pageable)
                .map(banner -> new BannerAdminMainResponseDto(banner));
    }

    public Banner findById(Long id) {
        return bannerRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 banner 가 존재하지 않습니다. id=" + id));
    }
}