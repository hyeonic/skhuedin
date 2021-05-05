package com.skhuedin.skhuedin.social;

import com.skhuedin.skhuedin.controller.response.BasicResponse;

import com.skhuedin.skhuedin.controller.response.TokenWithCommonResopnse;
import com.skhuedin.skhuedin.domain.Provider;

import com.skhuedin.skhuedin.domain.User;
import com.skhuedin.skhuedin.dto.user.UserMainResponseDto;
import com.skhuedin.skhuedin.dto.user.UserSaveRequestDto;
import com.skhuedin.skhuedin.service.UserService;
import com.skhuedin.skhuedin.social.kakao.OAuthToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(value = "/auth")
@Slf4j
public class OauthController {
    private final OauthService oauthService;
    private final UserService userService;


    /**
     * Social Login API Server 요청에 의한 callback 을 처리
     *
     * @param socialLoginType (GOOGLE, FACEBOOK, NAVER, KAKAO)
     * @return SNS Login 요청 결과로 받은 Json 형태의 String 문자열 (access_token, refresh_token 등)
     */

    @GetMapping("/{socialLoginType}/callback")
    public ResponseEntity<? extends BasicResponse> callback(
            @PathVariable("socialLoginType") String socialLoginType,
            @RequestParam("access_token") String access_token) {

        OAuthToken oAuthToken = new OAuthToken();
        oAuthToken.setAccess_token(access_token);

        log.info(">> 소셜 로그인 API 서버로부터 받은 code :: {}", oAuthToken.getAccess_token());
        // 소셜 로그인을 통해서 사용자의 값을 반환받
        UserSaveRequestDto user = oauthService.requestAccessToken(socialLoginType, oAuthToken);
        String token = Strings.EMPTY;

        Long id = null;
        // 사용자가 현재 회원인지 아닌지 확인 작업. 회원이 아니면 회원 가입을 시키고
        if (userService.findByEmail(user.getEmail()) == null) {
            userService.signUp(user);
        }
        //회원이면 로그인을 시킴
        token = userService.signIn(user);

        UserMainResponseDto responseDto = new UserMainResponseDto(userService.findByEmail(user.getEmail()));
        // user 인증을 위한 자체 토큰을 발급받아  저장,데이터에 user 값도 저장 해서 보냄
        return ResponseEntity.status(HttpStatus.OK).body((new TokenWithCommonResopnse(responseDto, token)));
    }
}