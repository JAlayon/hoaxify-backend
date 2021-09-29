package com.alayon.hoaxify.hoax.controller;

import com.alayon.hoaxify.commons.CurrentUser;
import com.alayon.hoaxify.hoax.dto.HoaxRequest;
import com.alayon.hoaxify.hoax.dto.HoaxResponse;
import com.alayon.hoaxify.hoax.model.Hoax;
import com.alayon.hoaxify.hoax.service.HoaxService;
import com.alayon.hoaxify.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

	@GetMapping({
			"hoaxes/{id:[0-9]+}",
			"users/{username}/hoaxes/{id:[0-9]+}"
			})
	public ResponseEntity<?> getHoaxesRelative
			(@PathVariable long id,
			 @PathVariable(required = false) String username,
			 @RequestParam(name="direction", defaultValue = "after") String direction,
			 @RequestParam(name="count", defaultValue = "false", required = false) boolean count,
			 Pageable pageable){
		if (!direction.equalsIgnoreCase("after"))
			return ResponseEntity.ok(hoaxService.getOldHoaxes(id, username, pageable).map(HoaxResponse::new));

		if (count) {
			Long newHoaxCount = hoaxService.getNewHoaxesCount(id, username);
			return ResponseEntity.ok(Collections.singletonMap("count", newHoaxCount));
		}
		List<HoaxResponse> newHoaxes = hoaxService.getNewHoaxes(id,username, pageable).stream().map(HoaxResponse::new)
				.collect(Collectors.toList());
		return ResponseEntity.ok(newHoaxes);
	}
}
