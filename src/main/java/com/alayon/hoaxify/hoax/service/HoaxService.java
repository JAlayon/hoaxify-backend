package com.alayon.hoaxify.hoax.service;

import com.alayon.hoaxify.hoax.dto.HoaxRequest;
import com.alayon.hoaxify.hoax.model.Hoax;
import com.alayon.hoaxify.hoax.repository.HoaxRepository;
import com.alayon.hoaxify.user.model.User;
import com.alayon.hoaxify.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class HoaxService {

    private HoaxRepository hoaxRepository;
    private UserService userService;

    public HoaxService(HoaxRepository hoaxRepository, UserService userService) {
        this.hoaxRepository = hoaxRepository;
        this.userService = userService;
    }

    public Hoax saveHoax(HoaxRequest hoaxRequest, User user){
        Hoax hoax = new Hoax();
        hoax.setContent(hoaxRequest.getContent());
        hoax.setUser(user);
        hoax.setTimestamp(new Date());
        return hoaxRepository.save(hoax);
    }

    public Page<Hoax> getAllHoaxes(Pageable pageable) {
        return hoaxRepository.findAll(pageable);
    }

    public Page<Hoax> getHoaxesByUsername(String username, Pageable pageable) {
        final User userInDb = userService.getByUsername(username);
        return hoaxRepository.findByUser(userInDb, pageable);
    }
}
