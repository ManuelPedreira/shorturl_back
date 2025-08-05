package com.manuelpedreira.shorturl.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manuelpedreira.shorturl.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
