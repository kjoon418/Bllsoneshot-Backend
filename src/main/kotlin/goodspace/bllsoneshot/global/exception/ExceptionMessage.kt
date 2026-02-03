package goodspace.bllsoneshot.global.exception

enum class ExceptionMessage(
    val message: String
) {
    LOGIN_FAILED("아이디 혹은 비밀번호를 확인해 주세요."),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    TASK_NOT_FOUND("할 일을 찾을 수 없습니다."),
    FEEDBACK_NOT_FOUND("피드백이 존재하지 않습니다."),
    TASK_ACCESS_DENIED("해당 할 일에 대한 권한이 없습니다."),
    FILE_NOT_FOUND("파일을 찾을 수 없습니다."),
    ANNOTATION_COUNT_MISMATCH("코멘트와 어노테이션 개수가 일치해야 합니다."),
    CANNOT_COMPLETE_WITHOUT_ACTUAL_MINUTES("시간을 기록하지 않은 할 일은 완료할 수 없습니다."),
    TASK_NOT_SUBMITTABLE("제출할 수 없는 할 일입니다.")
}
