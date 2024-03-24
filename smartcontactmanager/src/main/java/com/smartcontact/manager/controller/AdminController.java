package com.smartcontact.manager.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartcontact.manager.dao.ContactRepository;
import com.smartcontact.manager.dao.MyOrderRepository;
import com.smartcontact.manager.dao.UserRepository;
import com.smartcontact.manager.entities.Contact;
import com.smartcontact.manager.entities.MyOrder;
import com.smartcontact.manager.entities.User;
import com.smartcontact.manager.helper.Message;
import com.smartcontact.manager.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private MyOrderRepository myOrderRepository;

	@Autowired
	private EmailService emailService;

	@GetMapping("/dashboard/{page}")
	public String getDashboard(@PathVariable("page") Integer page, Principal principal, Model model) {
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
//		List<User> users = this.userRepository.findAll();
		
		Pageable pageable = PageRequest.of(page, 4);
		Page<User> users = this.userRepository.findAllUsers(pageable);
		
		model.addAttribute("user", user);
		model.addAttribute("users", users);
		model.addAttribute("title", "Admin Dashboard");
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", users.getTotalPages());
		return "admin/admin_dashboard";
	}

	@GetMapping("/user-profile/{uid}")
	public String getUserProfile(@PathVariable("uid") Integer uid, Model model) {
		User user = this.userRepository.findById(uid).get();
		model.addAttribute("title", "User Profile");
		model.addAttribute("user", user);
		return "admin/user_profile";
	}

	@GetMapping("/user-delete/{uid}")
	public String deleteUserHandler(@PathVariable("uid") Integer uid, Model model, HttpSession session) {
		User deletedUser = this.userRepository.findById(uid).get();
		List<Contact> contacts = this.contactRepository.findByUser(deletedUser);
		for (Contact contact : contacts) {
			this.contactRepository.delete(contact);
		}
		List<MyOrder> myOrders = this.myOrderRepository.findByUser(deletedUser);
		for (MyOrder myOrder : myOrders) {
			this.myOrderRepository.delete(myOrder);
		}
		this.userRepository.delete(deletedUser);
		session.setAttribute("message", new Message("User has been deleted successfully ...", "alert-success"));
		return "redirect:/admin/dashboard/0";
	}

	@PostMapping("/send-email-user/{user_id}")
	public String sendNotificationHandler(@PathVariable("user_id") Integer user_id,
			@RequestParam("notification") String notification, Principal principal, HttpSession session, Model model) {
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		String from = "anumoynandyanunan2019@gmail.com";
		User notifiedUser = this.userRepository.findById(user_id).get();
		String to = notifiedUser.getEmail();
		String message = "<h2>" + notification + "</h2>";
		String subject = "Notification from Smart Contact Manager Admin [ " + currentUser.getName() + " ]";
		boolean flag = this.emailService.sendEmail(message, subject, to, from);
		if (flag) {
			session.setAttribute("message",
					new Message("Notification has been sent successfully ...", "alert-success"));
		} else {
			session.setAttribute("message",
					new Message("Notification couldn't be sent ...", "alert-danger"));
		}
		return "redirect:/admin/user-profile/" + user_id;
	}

}
