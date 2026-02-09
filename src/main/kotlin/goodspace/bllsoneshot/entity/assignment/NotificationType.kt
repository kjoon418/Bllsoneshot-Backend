package goodspace.bllsoneshot.entity.assignment

enum class NotificationType {
    // 멘티 대상 알림
    REMINDER,           // 멘티 리마인더: 미완료 할일 존재
    FEEDBACK,           // 멘토가 할일에 피드백 남김
    LEARNING_REPORT    // 멘토가 학습 리포트 작성 완료
}
