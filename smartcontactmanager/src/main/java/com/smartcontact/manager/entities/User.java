package com.smartcontact.manager.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "user_id")
	private int id;
	@NotBlank(message = "Name field is required")
	@Size(min = 2, max = 20, message = "minimum 2 and maximum 20 characters are allowed !!!")
	private String name;
	@Column(unique = true)
	@NotBlank(message = "Email field is required !!!")
	@Email(regexp ="^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$", message = "Invalid Email !!!")
	private String email;
	@NotBlank(message = "Password should not be empty !!!")
	@Size(min = 8, message = "Password should be atleast 8 characters long !!!")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$", message = "Password should be strong !!!")
	private String password;
	private String role;
	private boolean enabled;
	@Column(name = "image_url")
	private String imageUrl;
	@Column(length = 500)
	private String about;
	private Integer validated;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
	private List<Contact> contacts = new ArrayList<>();

	public List<Contact> getContacts() {
		return contacts;
	}

	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}
	
	public Integer getValidated() {
		return validated;
	}

	public void setValidated(Integer validated) {
		this.validated = validated;
	}

	public User() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", email=" + email + ", password=" + password + ", role=" + role
				+ ", enabled=" + enabled + ", imageUrl=" + imageUrl + ", about=" + about + ", validated=" + validated + "]";
	}
	
	

}
