package com.alayon.hoaxify.hoax.dto;

import com.alayon.hoaxify.hoax.model.Hoax;
import com.alayon.hoaxify.user.dto.UserResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HoaxResponse {

    private Long id;
    private String content;
    private UserResponse user;
    private long date;

    public HoaxResponse(Hoax hoax){
        this.setId(hoax.getId());
        this.setContent(hoax.getContent());
        this.setDate(hoax.getTimestamp().getTime());
        this.setUser(new UserResponse(hoax.getUser()));
    }
}
