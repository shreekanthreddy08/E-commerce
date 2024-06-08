package com.jsp.Shree_Myshop.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;



import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Component
@Data
@Entity
public class ShoppingOrder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String orderId;
	private LocalDateTime dateTime;
	private double totalPrice;
	private String status;
	private String paymnetId;

	@OneToMany(fetch = FetchType.EAGER)
	private List<Item> items = new ArrayList<Item>();


	}


