package com.alayon.hoaxify.user;

import java.io.IOException;

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

	public User save(final User user) {
		userRepository.findByUsername(user.getUsername());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
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
				userInDb.setImage(savedImageName);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		return userRepository.save(userInDb);
	}

}
