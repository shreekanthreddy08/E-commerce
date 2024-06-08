package com.jsp.Shree_Myshop.dto;

import org.springframework.stereotype.Component;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Component
@Data
@Entity
public class Item {
	 
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private int id;
		private String name;
		private String category;
		private double price;
		private String description;
		private int quantity;
		private String imagePath;
}
