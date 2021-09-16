package com.alayon.hoaxify.user.error;

import com.alayon.hoaxify.error.ApiError;
import com.alayon.hoaxify.user.controller.UserController;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = UserController.class)
public class UserExceptionHandler {

    @ExceptionHandler({ MethodArgumentNotValidException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationError(final MethodArgumentNotValidException e, final HttpServletRequest request) {
        final ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), "Validation Error",
                request.getServletPath());
        final BindingResult result = e.getBindingResult();
        final Map<String, String> validationErrors = new HashMap<>();
        for (final FieldError fieldError : result.getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        error.setValidationErrors(validationErrors);
        return error;
    }

    @ExceptionHandler({UserNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleUserNotFoundError(final UserNotFoundException e, final HttpServletRequest request){
        final ApiError error = new ApiError(HttpStatus.NOT_FOUND.value(), e.getMessage(),
                request.getServletPath());
        return error;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDeniedError(final AccessDeniedException e, final HttpServletRequest request){
        final ApiError error = new ApiError(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                request.getServletPath());
        return error;
    }
}
