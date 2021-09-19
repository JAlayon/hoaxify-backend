package com.alayon.hoaxify.user.model;

import java.beans.Transient;
import java.util.Collection;
import java.util.List;

import javax.persistence.*;

import com.alayon.hoaxify.hoax.model.Hoax;
import com.alayon.hoaxify.user.jsonviews.Views;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class User implements UserDetails {

	private static final long serialVersionUID = -7487763369923363604L;

	@Id
	@GeneratedValue
	@JsonView(Views.Base.class)
	private Long id;

	@Column(unique = true, nullable = false, length = 255)
	@JsonView(Views.Base.class)
	private String username;

	@Column(nullable = false, length = 255)
	@JsonView(Views.Base.class)
	private String displayname;

	@Column(nullable = false, length = 255)
	private String password;

	@JsonView(Views.Base.class)
	private String image;

	@OneToMany(mappedBy = "user")
	private List<Hoax> hoaxes;

	@Override
	@Transient
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AuthorityUtils.createAuthorityList("Role_User");
	}

	@Override
	@Transient
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	@Transient
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	@Transient
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	@Transient
	public boolean isEnabled() {
		return true;
	}

}
