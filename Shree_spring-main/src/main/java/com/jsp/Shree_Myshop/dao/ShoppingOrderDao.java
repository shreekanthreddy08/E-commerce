package com.jsp.Shree_Myshop.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jsp.Shree_Myshop.dto.ShoppingOrder;
import com.jsp.Shree_Myshop.repository.ShoppingOrderRepository;

@Repository
public class ShoppingOrderDao {
	@Autowired
	ShoppingOrderRepository orderRepository;

	public void saveOrder(ShoppingOrder order) {
		orderRepository.save(order);
		
	}

	public ShoppingOrder findOrderById(int id) {
		return orderRepository.findById(id).orElseThrow();
	}
}
