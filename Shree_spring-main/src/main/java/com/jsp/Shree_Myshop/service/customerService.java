package com.jsp.Shree_Myshop.service;

import java.io.IOException;

import org.springframework.ui.ModelMap;

import org.springframework.validation.BindingResult;

import com.jsp.Shree_Myshop.dto.Customer;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

public interface customerService {

	String save(Customer customer, BindingResult result);

	String verifyOtp(int id, int otp, ModelMap map);

	String sendOtp(int id, ModelMap map);

	String resendOtp(int id, ModelMap map);

	String login(String email, String password, ModelMap map, HttpSession session);

	String viewProducts(HttpSession session, ModelMap map);

	String addToCart(int id, HttpSession session);

	String viewCart(ModelMap map, HttpSession session);

	String removeFromCart(int id, HttpSession session);

	String paymentPage(HttpSession session, ModelMap map);

	String confirmOrder(HttpSession session, int id, String razorpay_payment_id);

	String viewOrders(HttpSession session, ModelMap map);

}
