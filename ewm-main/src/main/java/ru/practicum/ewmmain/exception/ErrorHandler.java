package ru.practicum.ewmmain.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiError handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::toString).collect(Collectors.toList());
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        return new ApiError(
                errors,
                message,
                "For the requested operation the conditions are not met.",
                "BAD_REQUEST",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolationException(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        String message = e.getConstraintViolations()
                .stream().map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        return new ApiError(
                errors,
                message,
                "For the requested operation the conditions are not met.",
                "BAD_REQUEST",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(ValidationException e) {
        return new ApiError(

                new ArrayList<>(),
                e.getMessage(),
                "For the requested operation the conditions are not met.",
                "BAD_REQUEST",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(EntityNotFoundException e) {
        return new ApiError(

                new ArrayList<>(),
                e.getMessage(),
                "The required object was not found.",
                "NOT_FOUND",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return new ApiError(
                new ArrayList<>(),
                e.getMessage(),
                "Integrity constraint has been violated",
                "CONFLICT",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbiddenOperation(ForbiddenOperationException e) {
        return new ApiError(
                new ArrayList<>(),
                e.getMessage(),
                "This operation forbidden.",
                "FORBIDDEN",
                LocalDateTime.now()
        );
    }











}
