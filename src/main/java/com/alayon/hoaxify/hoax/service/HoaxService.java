package com.alayon.hoaxify.hoax.service;

import com.alayon.hoaxify.hoax.dto.HoaxRequest;
import com.alayon.hoaxify.hoax.model.Hoax;
import com.alayon.hoaxify.hoax.repository.HoaxRepository;
import com.alayon.hoaxify.user.model.User;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class HoaxService {

    private HoaxRepository hoaxRepository;

    public HoaxService(HoaxRepository hoaxRepository) {
        this.hoaxRepository = hoaxRepository;
    }

    public void saveHoax(HoaxRequest hoaxRequest, User user){
        Hoax hoax = new Hoax();
        hoax.setContent(hoaxRequest.getContent());
        hoax.setUser(user);
        hoax.setTimestamp(new Date());
        hoaxRepository.save(hoax);
    }
}
