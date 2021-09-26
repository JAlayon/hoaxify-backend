package com.alayon.hoaxify.hoax.repository;

import com.alayon.hoaxify.hoax.model.Hoax;
import com.alayon.hoaxify.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoaxRepository extends JpaRepository<Hoax, Long> {
    Page<Hoax> findByUser(User user, Pageable pageable);
}
