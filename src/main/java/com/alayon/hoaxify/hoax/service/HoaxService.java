package com.alayon.hoaxify.hoax.service;

import com.alayon.hoaxify.hoax.dto.HoaxRequest;
import com.alayon.hoaxify.hoax.dto.HoaxResponse;
import com.alayon.hoaxify.hoax.model.Hoax;
import com.alayon.hoaxify.hoax.repository.HoaxRepository;
import com.alayon.hoaxify.user.model.User;
import com.alayon.hoaxify.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class HoaxService {

    private HoaxRepository hoaxRepository;
    private UserService userService;

    public HoaxService(HoaxRepository hoaxRepository, UserService userService) {
        this.hoaxRepository = hoaxRepository;
        this.userService = userService;
    }

    public Hoax saveHoax(HoaxRequest hoaxRequest, User user){
        Hoax hoax = new Hoax();
        hoax.setContent(hoaxRequest.getContent());
        hoax.setUser(user);
        hoax.setTimestamp(new Date());
        return hoaxRepository.save(hoax);
    }

    public Page<Hoax> getAllHoaxes(Pageable pageable) {
        return hoaxRepository.findAll(pageable);
    }

    public Page<Hoax> getHoaxesByUsername(String username, Pageable pageable) {
        final User userInDb = userService.getByUsername(username);
        return hoaxRepository.findByUser(userInDb, pageable);
    }

    public Page<Hoax> getOldHoaxes(long id, String username, Pageable pageable) {
        Specification<Hoax> spec = Specification.where(idLessThan(id));
        if (username != null) {
            final User userInDb = userService.getByUsername(username);
            spec = spec.and(userIs(userInDb));
        }
        return hoaxRepository.findAll(spec, pageable);
    }

    public List<Hoax> getNewHoaxes(long id, String username, Pageable pageable) {
        Specification<Hoax> spec = Specification.where(idGreaterThan(id));
        if (username != null){
            final User userInDb = userService.getByUsername(username);
            spec = spec.and(userIs(userInDb));
        }
        return hoaxRepository.findAll(spec, pageable.getSort());
    }

    public Long getNewHoaxesCount(long id, String username) {
        Specification<Hoax> spec = Specification.where(idGreaterThan(id));
        if (username != null){
            final User userInDb = userService.getByUsername(username);
            spec = spec.and(userIs(userInDb));
        }
        return hoaxRepository.count(spec);
    }

    private Specification<Hoax> userIs(User user){
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user);
    }

    private Specification<Hoax> idLessThan(long id){
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("id"), id);
    }

    private Specification<Hoax> idGreaterThan(long id){
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("id"), id);
    }
}
