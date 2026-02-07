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
    CANNOT_COMPLETE_FUTURE_TASK("미래의 할 일은 완료할 수 없습니다."),
    INCOMPLETED_TASK("완료되지 않은 할 일입니다."),
    TASK_NOT_SUBMITTABLE("제출할 수 없는 할 일입니다."),
    CANNOT_DELETE_MENTOR_CREATED_TASK("멘토가 만든 할 일은 삭제할 수 없습니다."),
    CANNOT_UPDATE_MENTOR_CREATED_TASK("멘토가 만든 할 일은 수정할 수 없습니다."),
    START_OR_END_DATE_REQUIRED("시작일과 마감일 중 하나는 반드시 입력해야 합니다."),
    DATE_INVALID("시작일은 마감일보다 이후일 수 없습니다."),
    NEGATIVE_ACTUAL_MINUTES("학습 시간은 음수일 수 없습니다."),
    DATES_REQUIRED("날짜는 최소 1개 이상 선택해야 합니다."),
    DUPLICATE_DATES_NOT_ALLOWED("중복된 날짜는 선택할 수 없습니다."),
    PAST_DATES_NOT_ALLOWED("과거 날짜는 선택할 수 없습니다."),
    MENTOR_MENTEE_RELATION_DENIED("해당 멘티에 대한 권한이 없습니다."),
    TASK_NAMES_REQUIRED("할 일 이름은 최소 1개 이상이어야 합니다."),
    TASK_NAME_BLANK("할 일 이름이 비어 있습니다."),
    TASK_NAME_TOO_LONG("할 일 이름은 50자를 초과할 수 없습니다."),
    MENTEE_ACCESS_DENIED("담당 멘티가 아닙니다."),
    REPORT_DUPLICATE("동일한 기간과 과목의 학습 리포트가 이미 존재합니다."),
    REPORT_DATE_INVALID("시작일은 종료일 이후일 수 없습니다."),
    REPORT_CONTENT_REQUIRED("총평, 잘한 점, 보완할 점은 최소 1개 이상입니다."),
    REPORT_CONTENT_CANNOT_BLANK("총평, 잘한 점, 보완할 점은 공백일 수 없습니다."),
    REPORT_SUBJECT_INVALID("리포트에 사용할 수 없는 과목입니다."),
    REPORT_NOT_FOUND("학습 리포트를 찾을 수 없습니다."),
    RESOURCE_SUBJECT_INVALID("자료 과목은 KOREAN, ENGLISH, MATH 중 하나여야 합니다."),
    RESOURCE_ACCESS_DENIED("해당 멘티의 자료에 대한 권한이 없습니다.")
}
