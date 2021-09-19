package com.alayon.hoaxify.hoax.repository;

import com.alayon.hoaxify.hoax.model.Hoax;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoaxRepository extends JpaRepository<Hoax, Long> {
}
