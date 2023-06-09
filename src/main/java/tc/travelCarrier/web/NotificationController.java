package tc.travelCarrier.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tc.travelCarrier.auth.PrincipalDetails;
import tc.travelCarrier.domain.Notification;
import tc.travelCarrier.domain.User;
import tc.travelCarrier.dto.NotificationDTO;
import tc.travelCarrier.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Controller
public class NotificationController {

    private final NotificationService notificationService;

    public static Map<Integer, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    @GetMapping(value = "/sub", produces = "text/event-stream")
    public SseEmitter subscribe(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        System.out.println("액티브 : "+principalDetails.getUser().getId());
        int userId = principalDetails.getUser().getId();

        return notificationService.subscribe(userId);
    }

    @GetMapping(value = "/TravelCarrier/notification")
    public ResponseEntity<List<NotificationDTO>> getNotification(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<Notification> list = notificationService.findNotificationByUserIdAndRead(principalDetails.getUser());
        // sender와 receiver의 User 그자체(email,thumbPath,닉네임)
        // id와 isread와 content까지
        // 시간, 제목, url도 필요!!!
        List<NotificationDTO> dtoList = new ArrayList<NotificationDTO>();
        for(Notification nt : list){
            User sender = nt.getSender();
            if( nt.getNotificationType().equals("comment") || nt.getNotificationType().equals("recomment") || nt.getNotificationType().equals("gowith")  ){
                dtoList.add(
                        NotificationDTO.builder()
                                .type(nt.getNotificationType())
                                .id(nt.getId())
                                .senderName(sender.getName())
                                .senderThumbPath(sender.getAttachUser().getThumbPath())
                                .isRead(nt.getIsRead())
                                .url(nt.getUrl())
                                .time(nt.getCdate())
                                .title(nt.getTitle())
                                .build());
            }else if( nt.getNotificationType().equals("f_req") || nt.getNotificationType().equals("f_com") ){
                dtoList.add(
                        NotificationDTO.builder()
                                .type(nt.getNotificationType())
                                .id(nt.getId())
                                .senderName(sender.getName())
                                .senderThumbPath(sender.getAttachUser().getThumbPath())
                                .isRead(nt.getIsRead())
                                .url(nt.getUrl())
                                .time(nt.getCdate())
                                .build());
            }
        }


        return ResponseEntity.ok(dtoList);
    }

    @GetMapping(value = "/TravelCarrier/notification/{notificationId}")
    public ResponseEntity deleteNotification(@PathVariable("notificationId") Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(null);
    }

}