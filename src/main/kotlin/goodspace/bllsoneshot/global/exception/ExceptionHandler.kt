package goodspace.bllsoneshot.global.exception

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    private val log: Logger = LoggerFactory.getLogger(ExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    fun handleBadRequest(exception: Exception): ResponseEntity<String> {
        log.info(exception.stackTraceToString())

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(exception.message)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(exception: BadCredentialsException): ResponseEntity<String> {
        log.info(exception.stackTraceToString())

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(exception.message)
    }
}
