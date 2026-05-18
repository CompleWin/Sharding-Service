package ru.complewin.shardingservice.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.complewin.shardingservice.domain.exceptions.ShardAlreadyExistsException;
import ru.complewin.shardingservice.domain.exceptions.ShardNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ShardNotFoundException.class)
    public ProblemDetail handleNotFound(ShardNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ShardAlreadyExistsException.class)
    public ProblemDetail handleConflict(ShardAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }
}
