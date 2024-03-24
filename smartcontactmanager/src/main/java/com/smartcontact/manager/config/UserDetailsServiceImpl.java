package com.smartcontact.manager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smartcontact.manager.dao.UserRepository;
import com.smartcontact.manager.entities.User;

public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		User user = userRepository.getUserByUserName(username);
		if(user == null) {
//			System.out.println("Username not found !!!");
			throw new UsernameNotFoundException("Username not found !!!");
		}
//		if(user.getValidated() == 0) {
//			System.out.println("User is not validated yet !!!");
//			throw new UsernameNotFoundException("You not validated yet !!!");
//		}
//		System.out.println("User logged in : " + user);
		CustomUserDetails customUserDetails = new CustomUserDetails(user);
		return customUserDetails;
	}

}
