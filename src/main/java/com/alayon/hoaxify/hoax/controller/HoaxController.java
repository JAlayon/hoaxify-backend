package com.alayon.hoaxify.hoax.controller;

import com.alayon.hoaxify.commons.CurrentUser;
import com.alayon.hoaxify.hoax.dto.HoaxRequest;
import com.alayon.hoaxify.hoax.dto.HoaxResponse;
import com.alayon.hoaxify.hoax.service.HoaxService;
import com.alayon.hoaxify.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/")
public class HoaxController {

	@Autowired
	HoaxService hoaxService;

	@PostMapping("hoaxes")
	public HoaxResponse createHoax(
			@Valid
			@RequestBody HoaxRequest hoaxRequest,
			@CurrentUser User user) {
		return new HoaxResponse(hoaxService.saveHoax(hoaxRequest, user));
	}

	@GetMapping("hoaxes")
	public Page<HoaxResponse> getAllHoaxes(Pageable pageable){
		return hoaxService.getAllHoaxes(pageable).map(HoaxResponse::new);
	}

	@GetMapping("users/{username}/hoaxes")
	public Page<HoaxResponse> getHoaxesByUsername(@PathVariable String username, Pageable pageable){
		return hoaxService.getHoaxesByUsername(username, pageable).map(HoaxResponse::new);
	}

	@GetMapping("hoaxes/{id:[0-9]+}")
	public Page<HoaxResponse> getHoaxesRelative(@PathVariable long id, Pageable pageable){
		return hoaxService.getOldHoaxes(id, pageable).map(HoaxResponse::new);
	}

	@GetMapping("users/{username}/hoaxes/{id:[0-9]+}")
	public Page<HoaxResponse> getHoaxesRelativeForUser(
			@PathVariable String username,
			@PathVariable long id,
			Pageable pageable
	){
		return hoaxService.getOldHoaxesOfUser(id, username, pageable).map(HoaxResponse::new);
	}


}
