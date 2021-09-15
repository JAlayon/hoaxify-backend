package com.alayon.hoaxify.user.error;

import com.alayon.hoaxify.error.ApiError;
import com.alayon.hoaxify.user.controller.UserController;
import org.springframework.http.HttpStatus;
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
public class UserErrorHandler {

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
}
