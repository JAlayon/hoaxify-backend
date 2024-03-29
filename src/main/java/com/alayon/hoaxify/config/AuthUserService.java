package com.alayon.hoaxify.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.alayon.hoaxify.user.model.User;
import com.alayon.hoaxify.user.repository.UserRepository;

@Service
public class AuthUserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		final User user = userRepository.findByUsername(username);
		if (user == null)
			throw new UsernameNotFoundException("User not found");

		return user;
	}

}
