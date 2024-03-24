package com.smartcontact.manager.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartcontact.manager.entities.Contact;
import com.smartcontact.manager.entities.User;

public interface UserRepository extends JpaRepository<User, Integer>{
	@Query("select u from User u where u.email = :email")
	public User getUserByUserName(@Param("email") String email);	
	
	public List<User> findByNameContaining(String name);
	
	@Query("from User u")
	public Page<User> findAllUsers(Pageable pageable);
} 

