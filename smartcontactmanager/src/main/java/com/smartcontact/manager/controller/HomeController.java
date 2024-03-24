package com.smartcontact.manager.controller;

import java.security.Principal;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.smartcontact.manager.dao.UserRepository;
import com.smartcontact.manager.entities.User;
import com.smartcontact.manager.helper.Message;
import com.smartcontact.manager.service.EmailService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private EmailService emailService;
	
	@GetMapping("/")
	public String home(Model model, Principal principal) {
		if(principal == null) {
//			System.out.println("No principal");
			model.addAttribute("user", null);
			return "home";
		}
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		System.out.println(user);
		model.addAttribute("user", user);
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	@GetMapping("/about")
	public String about(Model model, Principal principal) {
		if(principal == null) {
			model.addAttribute("user", null);
			return "about";
		}
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("message", null);
		return "signup";
	}
	
	@GetMapping("/signin")
	public String login(Model model, HttpSession session) {
		model.addAttribute("userLogin", true);
		model.addAttribute("title", "Login - Smart Contact Manager");
		if(session.getAttribute("validation") != null) {
			String validationStatus = (String)session.getAttribute("validation");
			System.out.println(validationStatus);	
		}
		return "login";
	}
	
	@PostMapping("/do-register")
	public String handleRegistration(@Valid @ModelAttribute("user") User user, BindingResult result1, @RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model, HttpSession session) {
		try {
			if(!agreement) {
				System.out.println("You haven't agreed terms and conditions...");
				throw new Exception("You haven't agreed terms and conditions...");
			}
			if(result1.hasErrors()) {
				System.out.println("Something went wrong spring starter validation...");
				System.out.println(result1);
				model.addAttribute("user", user);
				return "signup";
			}
			user.setEnabled(true);
			user.setRole("ROLE_USER");
			user.setImageUrl("default.webp");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			user.setValidated(0);
			User result = userRepository.save(user);
			System.out.println("User : " + result);
			model.addAttribute("user",new User());
			model.addAttribute("message", new Message("Successfully registered... " ,"alert-primary"));
		}catch(Exception e) {
			System.out.println(e.getMessage());
			model.addAttribute("user",user);
			model.addAttribute("message", new Message("Something went wrong !!! " + e.getMessage(),"alert-danger"));
		}
		return "signup";	
	}
	
	@GetMapping("/forgot-password")
	public String forgetPasswordHandler(Model model) {
		model.addAttribute("title", "Forgot Password");
		return "forgot_password_form";
	}
	
	@GetMapping("/verify-email")
	public String verifyEmailHandler(Model model) {
		model.addAttribute("title", "Email Verification");
		return "verify_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTPHandler(@RequestParam("email") String email, Model model, HttpSession session) {
		model.addAttribute("title", "Verify OTP");
		System.out.println("Email : " + email);
		Random random = new Random();
        int min = 1000; 
        int max = 9999;
        int otp = random.nextInt(max - min + 1) + min;
		System.out.println("OTP : " + otp);
		String from = "anumoynandyanunan2019@gmail.com";
		String to = email;
		String message = "<h2>Your OTP is here : <b>" + otp + "</b></h2>";
		String subject = "OTP From Smart Contact Manager";
		boolean flag = this.emailService.sendEmail(message, subject, to, from);
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			session.setAttribute("message", new Message("OTP has been sent successfully ...", "alert-success"));
			model.addAttribute("title", "Verify OTP");
			return "verify_otp";
		}
		else {
			session.setAttribute("message", new Message("Email id is incorrect !!!","alert-danger"));
			model.addAttribute("title", "Forgot Password");
			return "forgot_password_form";
		}
	}
	
	@PostMapping("/send-otp-to-email")
	public String sendOTPToEmailHandler(@RequestParam("email") String email, Model model, HttpSession session) {
		model.addAttribute("title", "Verify OTP");
		System.out.println("Email : " + email);
		Random random = new Random();
        int min = 1000; 
        int max = 9999;
        int otp = random.nextInt(max - min + 1) + min;
		System.out.println("OTP : " + otp);
		String from = "anumoynandyanunan2019@gmail.com";
		String to = email;
		String message = "<h2>Your OTP is here : <b>" + otp + "</b></h2>";
		String subject = "OTP From Smart Contact Manager";
		boolean flag = this.emailService.sendEmail(message, subject, to, from);
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			session.setAttribute("message", new Message("OTP has been sent successfully ...", "alert-success"));
			model.addAttribute("title", "Verify OTP");
			return "verify_otp_from_email";
		}
		else {
			session.setAttribute("message", new Message("Email id is incorrect !!!","alert-danger"));
			model.addAttribute("title", "Email Verification");
			return "varify_email_form";
		}
	}
	
	@PostMapping("/verify-otp")
	public String verifyOTPHandler(@RequestParam("otp") int otp, HttpSession session, Model model) {
		int myOtp = (int)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		if(otp == myOtp) {
			User user = this.userRepository.getUserByUserName(email);
			if(user == null) {
				model.addAttribute("title", "Forgot Password");
				session.setAttribute("message", new Message("User doesn't exist with this email !!!","alert-danger"));
				return "forgot_password_form";
			}
			else {
				model.addAttribute("title", "Change Password");
				System.out.println("User exists with this mail ...");
				return "password_change_form";
			}
		}
		else {
			model.addAttribute("title", "Verify OTP");
			session.setAttribute("message", new Message("You have entered wrong otp !!!", "alert-danger"));
			return "verify_otp";
		}
	}
	
	@PostMapping("/verify-otp-from-email")
	public String verifyOTPFromEmailHandler(@RequestParam("otp") int otp, HttpSession session, Model model) {
		int myOtp = (int)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		if(otp == myOtp) {
			User user = this.userRepository.getUserByUserName(email);
			if(user == null) {
				model.addAttribute("title", "Email Verification");
				session.setAttribute("message", new Message("User doesn't exist with this email !!!","alert-danger"));
				return "verify_email_form";
			}
			else {
				user.setValidated(1);
				this.userRepository.save(user);
				model.addAttribute("title", "Login - Smart Contact Manager");
				return "login";
			}
		}
		else {
			model.addAttribute("title", "Verify OTP");
			session.setAttribute("message", new Message("You have entered wrong otp !!!", "alert-danger"));
			return "verify_otp_from_email";
		}
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session) {
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.passwordEncoder.encode(newPassword));
		this.userRepository.save(user);
		session.setAttribute("message", new Message("Password changed successfully ...","alert-success"));
		return "login";
	}
}
