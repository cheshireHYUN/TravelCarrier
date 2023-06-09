package tc.travelCarrier.auth;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tc.travelCarrier.domain.AttachUser;
import tc.travelCarrier.domain.AttachUserBackground;
import tc.travelCarrier.domain.Role;
import tc.travelCarrier.domain.User;
import tc.travelCarrier.repository.AttachRepository;
import tc.travelCarrier.repository.MemberRepository;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
    //DefaultOAuth2UserService는 OAuth2로그인 시 loadUserByUsername메서드로 로그인한 유저가 DB에 저장되어있는지를 찾는다.
    private final AttachRepository attachRepository;
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${file.dir}")
    private String fileDir;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        OAuth2UserInfo oAuth2UserInfo = null;	//
        String provider = userRequest.getClientRegistration().getRegistrationId();

        if(provider.equals("google")){
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if(provider.equals("naver")){
            oAuth2UserInfo = new NaverUserInfo(oAuth2User.getAttributes());
        } else if(provider.equals("kakao")){	//추가
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        }
        Role role = Role.ROLE_USER;
        String uuid = UUID.randomUUID().toString().substring(0, 6);
        String providerId =  oAuth2UserInfo.getProviderId();
        String password = bCryptPasswordEncoder.encode("패스워드"+uuid);
        String email = provider+"_"+providerId; //

        User byUsername = memberRepository.findUserByEmail(email);
        //DB에 없는 사용자라면 회원가입처리
        if(byUsername == null){
            byUsername = User.oauth2Register()
                    .username(generateRandomName()).password(password).email(email).role(role)
                    .provider(provider).providerId(providerId)
                    .build();
            memberRepository.save(byUsername);
            attachRepository.saveProfilePic(AttachUser.builder().title("default_profile.jpg").user(byUsername).path(fileDir+"mypage/default_profile.jpg").build());
            attachRepository.saveBgPic(AttachUserBackground.builder().title("default_bg.jpg").user(byUsername).path(fileDir+"mypage/default_bg.jpg").build());
        }

        return new PrincipalDetails(byUsername, oAuth2UserInfo);
    }

    // 랜덤 이름 발생 메소드
    private enum Prefix {
        야생의, 행복한, 자유로운, 모험적인, 열정적인, 탐험하는, 도전적인, 여유로운, 감성적인, 유쾌한
    }
    public String generateRandomName() {
        Prefix[] prefixes = Prefix.values();
        Random random = new Random();
        Prefix randomPrefix = prefixes[random.nextInt(prefixes.length)];
        return randomPrefix.name() + " 여행자";
    }
}
