package com.alayon.hoaxify.user.service;

import java.io.IOException;

import com.alayon.hoaxify.user.dto.UserRequest;
import com.alayon.hoaxify.user.model.User;
import com.alayon.hoaxify.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.alayon.hoaxify.error.NotFoundException;
import com.alayon.hoaxify.file.FileService;
import com.alayon.hoaxify.user.dto.UserUpdateDto;

@Service
public class UserService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final FileService fileService;

	public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder,
			final FileService fileService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.fileService = fileService;
	}

	public User save(final UserRequest userRequest) {
		final User userToSave = User.builder()
								.username(userRequest.getUsername())
								.displayname(userRequest.getDisplayname())
								.password(passwordEncoder.encode(userRequest.getPassword()))
								.build();
		return userRepository.save(userToSave);
	}

	public Page<User> getUsers(final User loggedInUser, final Pageable pageable) {
		if (loggedInUser != null) {
			return userRepository.findByUsernameNot(loggedInUser.getUsername(), pageable);
		}
		return userRepository.findAll(pageable);
	}

	public User getByUsername(final String username) {
		final User userInDb = userRepository.findByUsername(username);
		if (userInDb == null)
			throw new NotFoundException(username + " not found");
		return userInDb;
	}

	public User update(final long id, final UserUpdateDto userUpdate) {
		final User userInDb = userRepository.getOne(id);
		userInDb.setDisplayname(userUpdate.getDisplayName());
		if (userUpdate.getImage() != null) {
			final String savedImageName;

			try {
				savedImageName = fileService.saveProfileImage(userUpdate.getImage());
				fileService.deleteProfileImage(userInDb.getImage());
				userInDb.setImage(savedImageName);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		return userRepository.save(userInDb);
	}

}
