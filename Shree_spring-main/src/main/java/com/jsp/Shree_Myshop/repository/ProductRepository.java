package com.jsp.Shree_Myshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.Shree_Myshop.dto.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

	Product findByName(String name);

	boolean existsByName(String name);

}
