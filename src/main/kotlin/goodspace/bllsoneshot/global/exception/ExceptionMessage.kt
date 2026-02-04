package goodspace.bllsoneshot.global.exception

enum class ExceptionMessage(
    val message: String
) {
    LOGIN_FAILED("아이디 혹은 비밀번호를 확인해 주세요."),
    INVALID_REFRESH_TOKEN("유효하지 않거나 만료된 리프레시 토큰입니다."),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    TASK_NOT_FOUND("할 일을 찾을 수 없습니다."),
    FEEDBACK_NOT_FOUND("피드백이 존재하지 않습니다."),
    TASK_ACCESS_DENIED("해당 할 일에 대한 권한이 없습니다."),
    FILE_NOT_FOUND("파일을 찾을 수 없습니다."),
    CANNOT_COMPLETE_WITHOUT_ACTUAL_MINUTES("시간을 기록하지 않은 할 일은 완료할 수 없습니다."),
    CANNOT_COMPLETE_FUTURE_TASK("미래의 할 일은 완료할 수 없습니다."),
    TASK_NOT_SUBMITTABLE("제출할 수 없는 할 일입니다."),
    START_OR_END_DATE_REQUIRED("시작일과 마감일 중 하나는 반드시 입력해야 합니다."),
    DATE_INVALID("시작일은 마감일보다 이후일 수 없습니다.")
}
