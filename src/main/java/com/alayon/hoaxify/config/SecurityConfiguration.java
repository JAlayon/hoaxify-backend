package com.alayon.hoaxify.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthUserService authUserService;

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http.csrf().disable();

		http.httpBasic().authenticationEntryPoint(new BasicAuthenticationEntryPoint());

		http
				.authorizeRequests()
					.antMatchers(HttpMethod.POST, "/api/v1/login").authenticated()
					.antMatchers(HttpMethod.PUT, "/api/v1/users/{id:[0-9]+}").authenticated()
					.antMatchers(HttpMethod.POST, "/api/v1/hoaxes").authenticated()
				.and()
				.authorizeRequests().anyRequest().permitAll();

		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(authUserService).passwordEncoder(passwordEncoder());
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
