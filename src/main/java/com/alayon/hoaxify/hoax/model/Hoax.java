package com.alayon.hoaxify.hoax.model;

import com.alayon.hoaxify.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
public class Hoax {

	@Id
	@GeneratedValue
	private Long id;

	@Column(length = 5000)
	private String content;

	@ManyToOne
	@JsonIgnore
	private User user;

	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;
}
