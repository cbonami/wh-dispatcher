package be.acerta.webhook.dispatcher.restcontrollers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class RestExceptionHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestExceptionHandler.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElementException(HttpServletRequest request, Exception ex) {
        log.info("Exception {} on Request {}", ex.getMessage(), request.getRequestURL());
        log.debug("Rootcause", ex);
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentExceptionn(HttpServletRequest request, Exception ex) {
        log.info("Exception {} on Request {}", ex.getMessage(), request.getRequestURL());
        log.debug("Rootcause", ex);
        return ex.getMessage();
    }
}
