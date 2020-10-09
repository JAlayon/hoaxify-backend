package com.alayon.hoaxify.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http.csrf().disable();

		http.httpBasic().authenticationEntryPoint(new BasicAuthenticationEntryPoint());

		http.authorizeRequests().antMatchers(HttpMethod.POST, "/api/v1/login").authenticated().and().authorizeRequests()
				.anyRequest().permitAll();
	}
}
