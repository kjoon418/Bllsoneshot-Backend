package goodspace.bllsoneshot.notification.controller

import goodspace.bllsoneshot.global.response.NO_CONTENT
import goodspace.bllsoneshot.global.security.userId
import goodspace.bllsoneshot.notification.dto.request.FcmTokenRequest
import goodspace.bllsoneshot.notification.dto.response.NotificationResponse
import goodspace.bllsoneshot.notification.dto.response.UnreadCountResponse
import goodspace.bllsoneshot.notification.service.NotificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/notifications")
@Tag(name = "알림", description = "알림 관련 API")
class NotificationController(
    private val notificationService: NotificationService
) {

    @GetMapping
    @Operation(
        summary = "알림 목록 조회",
        description = """
            로그인한 사용자의 알림 목록을 최신순으로 조회합니다.
            조회 시 NEW(새 알림) 상태의 알림이 UNREAD(확인했으나 읽지 않음)로 전이됩니다.
            
            [알림 상태]
            NEW: 새로 온 알림 (아직 목록 화면을 열지 않음)
            UNREAD: 목록에서 확인했으나 클릭하지 않음
            READ: 알림을 클릭하여 읽음
            
            [알림 타입 (멘티 대상)]
            REMINDER: 리마인더 (미완료 할일)
            FEEDBACK: 피드백 도착
            LEARNING_REPORT: 학습 리포트 도착
            
            [응답]
            notificationId: 알림 ID
            type: 알림 타입
            title: 알림 제목
            message: 알림 내용
            status: 알림 상태(NEW, UNREAD, READ)
            taskId: 관련 할일 ID (없으면 null)
            learningReportId: 관련 리포트 ID (없으면 null)
            createdAt: 알림 생성 시각
        """
    )
    fun getNotifications(principal: Principal): ResponseEntity<List<NotificationResponse>> {
        val response = notificationService.getNotifications(principal.userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/unread-count")
    @Operation(
        summary = "읽지 않은 알림 개수 조회",
        description = """
            NEW 상태인 알림의 총 개수를 반환합니다.
            알림 뱃지 표시 등에 활용됩니다.
            
            [응답]
            count: NEW 상태인 알림의 개수
        """
    )
    fun getUnreadCount(principal: Principal): ResponseEntity<UnreadCountResponse> {
        val response = notificationService.getNewNotificationCount(principal.userId)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{notificationId}/read")
    @Operation(
        summary = "알림 읽음 처리",
        description = """
            특정 알림을 READ 상태로 변경합니다.
            알림을 클릭하여 상세 페이지로 이동할 때 호출합니다.
            본인의 알림만 읽음 처리할 수 있습니다.
            
            [요청]
            notificationId: 알림 ID (Path)
            
            [응답]
            204 NO CONTENT
        """
    )
    fun markAsRead(
        principal: Principal,
        @PathVariable notificationId: Long
    ): ResponseEntity<Void> {
        notificationService.markAsRead(principal.userId, notificationId)
        return NO_CONTENT
    }

    @PutMapping("/fcm-token")
    @Operation(
        summary = "FCM 토큰 등록",
        description = """
            푸시 알림 수신을 위한 FCM 토큰을 등록합니다.
            웹 브라우저에서 발급받은 FCM 토큰을 서버에 저장합니다.
            이미 토큰이 존재하면 덮어씁니다.
            
            [요청]
            token: FCM 토큰 (필수)
            
            [응답]
            204 NO CONTENT
        """
    )
    fun registerFcmToken(
        principal: Principal,
        @Valid @RequestBody request: FcmTokenRequest
    ): ResponseEntity<Void> {
        notificationService.registerFcmToken(principal.userId, request.token)
        return NO_CONTENT
    }
}
