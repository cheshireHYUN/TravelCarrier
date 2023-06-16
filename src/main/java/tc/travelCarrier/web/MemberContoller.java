package tc.travelCarrier.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import tc.travelCarrier.auth.PrincipalDetails;
import tc.travelCarrier.domain.User;
import tc.travelCarrier.repository.MemberRepository;
import tc.travelCarrier.repository.WeeklyRepository;

@Controller
@RequiredArgsConstructor
@RequestMapping("/TravelCarrier")
public class MemberContoller {

    private final MemberRepository memberRepository;
    private final WeeklyRepository weeklyRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/member/login")
    public String memberLogin(Model model,
                              @RequestParam(value = "error", required = false) String error,
                              @RequestParam(value="exception", required = false) String exception){
        model.addAttribute("error",error);
        model.addAttribute("exception",exception);

        return "test/login";
    }

    @GetMapping("/member/sign")
    public String memberSignIn(){
        return "test/sign";
    }

    @PostMapping("/member/sign")
    public String memberSignIn(@RequestParam String email, @RequestParam String pw, @RequestParam String name){
        User user = new User(email,passwordEncoder.encode(pw),name);
        memberRepository.save(user);
        return "test/login";
    }

    // 로그인했는지 확인하기
    @GetMapping("/member/login/check")
    @ResponseBody
    public String checkLogin(@AuthenticationPrincipal PrincipalDetails principalDetails){
        System.out.println("어노테이션 있음");

        return "false";
    }

    // 마이페이지
    @GetMapping("/member/mypage")
    public String myPage(Model model,  @AuthenticationPrincipal PrincipalDetails principalDetails){
/*      본인 프로필(이메일, 프로필사진, 이름, 배경사진) : profile
        내 다이어리(썸네일, 제목, 기간, 링크) : diaryList
        태그된 다이어리 (썸네일, 제목, 기간, 글작성자, 링크) : tagDiaryList
        친구목록 (친구이름, 친구프사, 친구배사, 친구프로필링크) : followers
        */
        User user = principalDetails.getUser();

        model.addAttribute("profile",user);
        model.addAttribute("user",user);
        // 이메일 : profile.email, 프사 : profile.attachUser.thumbPath, 이름 : profile.name, 배경사진 : profile.attachUserBackground.thumbPath

        model.addAttribute("diaryList",user.getWeeklys());
        // (반복문 필요 : 각 요소를 diary로) 썸네일 : diary.attachWeekly.thumbPath,
        // 제목 : diary.title, 기간 : diary.travelDate.sDate ~ diary.travelDate.eDate, 링크 : "TravelCarrier/weekly/"+diary.id

        model.addAttribute("tagDiaryList", weeklyRepository.getTagWeeklys(user));
        // (반복문 필요 : 각 요소를 diary로) 썸네일 : diary.attachWeekly.thumbPath,
        // 제목 : diary.title, 기간 : diary.travelDate.sDate ~ diary.travelDate.eDate, 링크 : "TravelCarrier/weekly/"+diary.id

        model.addAttribute("followers",user.getFollowers());
        // 반복문 필요 : 각 요소를 follower로
        // 친구이름 : follower.follower.name, 친구프사 :  follower.follower.attachUser.thumbPath,
        // 친구배사 :  follower.follower.attachUserBackground.thumbPath, 친구 프로필 링크 : "TravelCarrier/member/"+follower.follower.email,

        // 백그라운드 없으면 오류 날 수 있음 - 타임리프로 null체크해서 해결
/*
        System.out.println("profile "+activeUser.getEmail()+", "+activeUser.getAttachUser().getThumbPath()+", "+activeUser.getName()
        +", "+activeUser.getAttachUserBackground().getThumbPath());
        for(Weekly w : activeUser.getWeeklys()){
            System.out.println(w.getTitle()+", "+w.getTravelDate().getSDate()+", "+w.getTravelDate().getEDate()+", "
            +", TravelCarrier/weekly/"+w.getId());
        }
        for(Weekly w : weeklyRepository.getTagWeeklys(activeUser)){
            System.out.println(w.getTitle()+", "+w.getTravelDate().getSDate()+", "+w.getTravelDate().getEDate()+", "
                    +", TravelCarrier/weekly/"+w.getId());
        }
        for(Follower follower : activeUser.getFollowers()){
            System.out.println( follower.getFollower().getName()+", "+follower.getFollower().getAttachUser().getThumbPath()+", "+
                    // follower.getFollower().getAttachUserBackground().getThumbPath()+
                    ", TravelCarrier/member/"+follower.getFollower().getEmail());
        }
*/

        return "/test/mypage";
    }




}

