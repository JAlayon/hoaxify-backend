package com.alayon.hoaxify.hoax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoaxRequest {

    @NotNull
    @Size(min = 10, max = 5000)
    private String content;
}
