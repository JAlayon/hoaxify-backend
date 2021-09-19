package com.alayon.hoaxify.hoax.controller;

import com.alayon.hoaxify.commons.CurrentUser;
import com.alayon.hoaxify.hoax.dto.HoaxRequest;
import com.alayon.hoaxify.hoax.service.HoaxService;
import com.alayon.hoaxify.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/")
public class HoaxController {

	@Autowired
	HoaxService hoaxService;

	@PostMapping("hoaxes")
	public void createHoax(
			@Valid
			@RequestBody HoaxRequest hoaxRequest,
			@CurrentUser User user) {
		hoaxService.saveHoax(hoaxRequest, user);
	}
}
