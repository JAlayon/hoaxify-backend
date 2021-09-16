package com.alayon.hoaxify.user.error;

public class UserNotFoundException extends RuntimeException{

    private static final long serialVersionUID = 1397133487266320578L;

    public UserNotFoundException(final String message) {
        super(message);
    }
}
