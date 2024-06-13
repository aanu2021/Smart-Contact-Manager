package com.smartcontact.manager.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.catalina.WebResourceRoot.ArchiveIndexStrategy;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.smartcontact.manager.dao.ContactRepository;
import com.smartcontact.manager.dao.MyOrderRepository;
import com.smartcontact.manager.dao.UserRepository;
import com.smartcontact.manager.entities.Contact;
import com.smartcontact.manager.entities.MyOrder;
import com.smartcontact.manager.entities.User;
import com.smartcontact.manager.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private MyOrderRepository myOrderRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@ModelAttribute
	public void addCommon(Model model, Principal principal) {
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		System.out.println("Logged in user : " + user);
		model.addAttribute("user", user);
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model, Principal principal, HttpSession session) {
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(user.getValidated() == 0) {
			System.out.println("User is not yet validated !!!");
			session.setAttribute("validation", "fail");
			return "redirect:/signin";
		}
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	@GetMapping("/add-contact-form")
	public String addContactForm(Model model, Principal principal, HttpSession session) {
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(user.getValidated() == 0) {
			System.out.println("User is not yet validated !!!");
			session.setAttribute("validation", "fail");
			return "redirect:/signin";
		}
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processContactForm(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session, Model model) {
		try {

			String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);

			System.out.println(contact);
//			System.out.println(result);

//			if(result.hasErrors()) {
//				System.out.println("Something went wrong spring starter validation...");
//				System.out.println(result);
//				model.addAttribute("contact", contact);
//				return "normal/add-contact-form";
//			}

			if (file.isEmpty()) {
				System.out.println("File is empty !!!");
				contact.setImage("default.png");
//				throw new Exception("File is empty !!!");
			} else if (!file.getContentType().equals("image/jpeg") && !file.getContentType().equals("image/png")
					&& !file.getContentType().equals("image/jpg")) {
				System.out.println("Please upload images !!!");
				contact.setImage("default.png");
//				throw new Exception("Please upload images !!!");
			} else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}

			user.getContacts().add(contact);
			contact.setUser(user);

			userRepository.save(user);
			System.out.println(contact);
			model.addAttribute("contact", new Contact());
			model.addAttribute("message", new Message("Contact added successfully ...", "alert-success"));
			session.setAttribute("message", new Message("Contact added successfully ...", "alert-success"));

		} catch (Exception e) {
			System.out.println(e.getMessage());
			model.addAttribute("contact", contact);
			model.addAttribute("message", new Message(e.getMessage(), "alert-danger"));
			session.setAttribute("message", new Message(e.getMessage(), "alert-danger"));
		}
		return "normal/add_contact_form";
	}

	@GetMapping("/show-contacts/{page}")
	public String showContactsHandler(@PathVariable("page") Integer page, Model model, Principal principal, HttpSession session) {

		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		
		if(user.getValidated() == 0) {
			System.out.println("User is not yet validated !!!");
			session.setAttribute("validation", "fail");
			return "redirect:/signin";
		}

		Pageable pageable = PageRequest.of(page, 3);
		Page<Contact> contacts = this.contactRepository.findContactsByUserId(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("title", "All Contacts");
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	@GetMapping("/{cId}/contact")
	public String handleContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal, HttpSession session) {
		System.out.println(cId);
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		if(user.getValidated() == 0) {
			System.out.println("User is not yet validated !!!");
			session.setAttribute("validation", "fail");
			return "redirect:/signin";
		}
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
		}
		return "normal/contact_details";
	}

	@GetMapping("/delete/{cId}")
	public String deleteContactHandler(@PathVariable("cId") Integer cId, Principal principal, HttpSession session) {
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		if(user.getValidated() == 0) {
			System.out.println("User is not yet validated !!!");
			session.setAttribute("validation", "fail");
			return "redirect:/signin";
		}
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		if (user.getId() == contact.getUser().getId()) {
			contact.setUser(null);
			this.contactRepository.delete(contact);
			session.setAttribute("message", new Message("Contact deleted successfully...", "alert-success"));
		} else {
			session.setAttribute("message", new Message("You don't have required permissions !!!", "alert-danger"));
		}
		return "redirect:/user/show-contacts/0";
	}

	@PostMapping("/update-contact/{cId}")
	public String updateFormHandler(@PathVariable("cId") Integer cId, Model model) {
		model.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cId).get();
		model.addAttribute("contact", contact);
		return "normal/update_contact_form";
	}

	@PostMapping("/process-contact-update")
	public String processUpdateContactHandler(@ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile file, Model model, Principal principal, HttpSession session) {
		System.out.println(contact);

		try {

			Optional<Contact> prevContactOptional = this.contactRepository.findById(contact.getCid());
			Contact prevContact = prevContactOptional.get();
			String prevImage = prevContact.getImage();

			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);

			if (file.isEmpty()) {
				contact.setImage(prevImage);
			} else {
//				File deleteFile = new ClassPathResource("static/img").getFile();
//				File file2 = new File(deleteFile, prevImage);
//				file2.delete();
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Contact updated successfully ...", "alert-success"));

		} catch (Exception e) {
			System.out.println(e.getMessage());
			session.setAttribute("message", new Message(e.getMessage(), "alert-danger"));
		}

		return "redirect:/user/show-contacts/0";
	}

	@GetMapping("/profile")
	public String profileHandler(Model model, Principal principal, HttpSession session) {
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(user.getValidated() == 0) {
			System.out.println("User is not yet validated !!!");
			session.setAttribute("validation", "fail");
			return "redirect:/signin";
		}
//		User user = this.userRepository.getUserByUserName(principal.getName());
//		model.addAttribute("user", user);
		model.addAttribute("title", "User Profile");
//		System.out.println(user);
		return "normal/profile";
	}
	
	@GetMapping("/update-profile")
	public String updateProfileHandler(Model model) {
		model.addAttribute("title", "Edit Profile");
		return "normal/update_profile_form";
	}
	
	@PostMapping("/process-update-profile")
	public String processEditProfileForm(@RequestParam("name") String name, @RequestParam("about") String about, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) throws IOException {
		
		System.out.println(name);
		System.out.println(about);
		if(!file.isEmpty()) {
			System.out.println(file.getOriginalFilename());
		}
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		user.setName(name);
		user.setAbout(about);
		
		if(!file.isEmpty()) {
			
			if(file.getContentType().equals("image/jpeg") || file.getContentType().equals("image/png")
					|| file.getContentType().equals("image/jpg")) {
				
				user.setImageUrl(file.getOriginalFilename());
				
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
			}
			
		}
		
		this.userRepository.save(user);
		session.setAttribute("message", new Message("Profile updated successfully ...", "alert-success"));
		return "normal/profile";
	}

	@GetMapping("/settings")
	public String openSettings(Model model, Principal principal, HttpSession session) {
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(user.getValidated() == 0) {
			System.out.println("User is not yet validated !!!");
			session.setAttribute("validation", "fail");
			return "redirect:/signin";
		}
		model.addAttribute("title", "Settings");
		return "normal/settings";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
		System.out.println("Old Password : " + oldPassword);
		System.out.println("New Password : " + newPassword);
		User user = this.userRepository.getUserByUserName(principal.getName());
		String existingPassword = user.getPassword();
		System.out.println(existingPassword);
		if (passwordEncoder.matches(oldPassword, existingPassword)) {
			user.setPassword(passwordEncoder.encode(newPassword));
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Password is updated successfully...", "alert-success"));
		} else {
			session.setAttribute("message", new Message("Wrong old password !!!", "alert-danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/dashboard";
	}
	
	@PostMapping("/create-order")
	@ResponseBody
	public String handleCreateOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception {

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		// System.out.println("Hey order function executed ...");
		System.out.println(data);
		int amt = Integer.parseInt(data.get("amount").toString());
		System.out.println("Amount is : " + amt);
		
		RazorpayClient razorpayClient = new RazorpayClient("rzp_test_aioxHHMyOq8spx", "xFrh0TgszYONkcv5vWZbSyUP");
		
		JSONObject options = new JSONObject();
		options.put("amount",amt*100);
		options.put("currency","INR");
		options.put("receipt","txn_12345");
		
		// Creating new order 
		Order order = razorpayClient.Orders.create(options);
		System.out.println(order);
		
		// Save orders in our DB
		MyOrder myOrder = new MyOrder();
		myOrder.setAmount(order.get("amount") + "");
		myOrder.setOrderId(order.get("id"));
		myOrder.setStatus("created");
		myOrder.setUser(user);
		myOrder.setReceipt(order.get("receipt"));
		
		this.myOrderRepository.save(myOrder);
		
		return order.toString();
	}
	
	@PostMapping("/update-order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
		System.out.println(data);
		MyOrder myOrder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		myOrder.setStatus(data.get("status").toString());
		myOrder.setPaymentId(data.get("payment_id").toString());
		this.myOrderRepository.save(myOrder);
		return ResponseEntity.ok(Map.of("msg","updated..."));
	}
}
