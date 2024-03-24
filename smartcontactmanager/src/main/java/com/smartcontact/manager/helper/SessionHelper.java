package com.smartcontact.manager.helper;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

@Component
public class SessionHelper {
    public void removeMessageFromSession() {
    	try {
    		HttpSession session = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getSession();
    	    session.removeAttribute("message");
    	}catch(Exception e) {
    		System.out.println(e.getMessage());
    	}
    }
    public void removeValidationFromSession() {
    	try {
    		HttpSession session = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getSession();
    	    session.removeAttribute("validation");
    	}catch(Exception e) {
    		System.out.println(e.getMessage());
    	}
    }
}
