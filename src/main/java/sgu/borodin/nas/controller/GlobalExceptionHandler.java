package sgu.borodin.nas.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import sgu.borodin.nas.dto.ExecutionStatus;
import sgu.borodin.nas.exception.UserAlreadyExistsException;
import sgu.borodin.nas.exception.UserNotFoundException;

import java.io.IOException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ExecutionStatus> handleIOException(IOException e) {
        log.error("Server encountered with IOException : {}", e.getMessage(), e);
        return new ResponseEntity<>(new ExecutionStatus(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExecutionStatus> handleResponseStatusException(ResponseStatusException e) {
        log.error("Server encountered with ResponseStatusException : {}", e.getReason(), e);
        return new ResponseEntity<>(new ExecutionStatus(e.getReason()), e.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExecutionStatus> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Server encountered with MethodArgumentNotValidException : {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new ExecutionStatus(e.getBindingResult().getFieldError().getDefaultMessage()),
                e.getStatusCode()
        );
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ExecutionStatus> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        log.error("Server encountered with UserAlreadyExistsException : {}", e.getMessage(), e);
        return new ResponseEntity<>(new ExecutionStatus(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExecutionStatus> handleUserNotFoundException(UserNotFoundException e) {
        log.error("Server encountered with UserNotFoundException : {}", e.getMessage(), e);
        return new ResponseEntity<>(new ExecutionStatus(e.getMessage()), HttpStatus.NOT_FOUND);
    }
}
