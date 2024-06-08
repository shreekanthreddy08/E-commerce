package com.jsp.Shree_Myshop.service.implement;

import java.io.IOException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;


import com.jsp.Shree_Myshop.dao.CustomerDao;
import com.jsp.Shree_Myshop.dao.ItemDao;
import com.jsp.Shree_Myshop.dao.ProductDao;
import com.jsp.Shree_Myshop.dao.ShoppingOrderDao;
import com.jsp.Shree_Myshop.dto.Cart;
import com.jsp.Shree_Myshop.dto.Customer;
import com.jsp.Shree_Myshop.dto.Item;
import com.jsp.Shree_Myshop.dto.Product;
import com.jsp.Shree_Myshop.dto.ShoppingOrder;
import com.jsp.Shree_Myshop.helper.AES;
import com.jsp.Shree_Myshop.helper.MialSendingHelper;
import com.jsp.Shree_Myshop.service.customerService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Service
public class CustomerServiceImpl implements customerService {
	@Autowired
	CustomerDao customerDao;
	@Autowired
	ProductDao productDao;
	@Autowired
	ItemDao itemDao;
	@Autowired
	ShoppingOrderDao orderDao;
	@Autowired
	MialSendingHelper mailSend;
	@Override
	public String save(Customer customer, BindingResult result) {
		if (customerDao.checkEmailDuplicate(customer.getEmail()))
			result.rejectValue("email", "error.email", "* Account Already Exists with this Email");
		if (customerDao.checkMobileDuplicate(customer.getMobile()))
			result.rejectValue("mobile", "error.mobile", "* Account Already Exists with this Mobile");

		if (result.hasErrors())
			return "Signup";
		else {
			customer.setPassword(AES.encrypt(customer.getPassword(), "123"));
			customer.setRole("USER");
			customer.setOtp(new Random().nextInt(100000, 999999));
			customerDao.save(customer);
			// Sending Mail Logic
			return "redirect:/send-otp/" + customer.getId();
		}
	}

	@Override
	public String verifyOtp(int id, int otp, ModelMap map) {
		Customer customer = customerDao.findById(id);
		if (customer.getOtp() == otp) {
			customer.setVerified(true);
			customerDao.save(customer);
			return "redirect:/signin";
		} else {
			map.put("failmessage", "Invalid Otp,Try agian");
			map.put("id", id);
			return "VerifyOtp";
		}
	}

	@Override
	public String sendOtp(int id, ModelMap map) {
		Customer customer = customerDao.findById(id);
		customer.setOtp(new Random().nextInt(100000, 999999));
		customerDao.save(customer);
		mailSend.sendOtp(customer);
		// Logic for Sending Otp
		map.put("id", id);
		map.put("successMessage", "Otp Sent Success");
		return "VerifyOtp";
	}

	@Override
	public String resendOtp(int id, ModelMap map) {
		Customer customer = customerDao.findById(id);
		customer.setOtp(new Random().nextInt(100000, 999999));
		customerDao.save(customer);
		mailSend.resendOtp(customer);
		map.put("id", id);
		map.put("successMessage", "Otp Resent Success");
		return "VerifyOtp";
	}

	@Override
	public String login(String email, String password, ModelMap map, HttpSession session) {
		Customer customer = customerDao.findByEmail(email);
		if (customer == null)
			session.setAttribute("failMessage", "Invalid Email");
		else {
			if (AES.decrypt(customer.getPassword(), "123").equals(password)) {
				if (customer.isVerified()) {
					session.setAttribute("customer", customer);
					session.setAttribute("successMessage", "Login Success");
					return "redirect:/";
				} else {
					return resendOtp(customer.getId(), map);
				}
			} else
				session.setAttribute("failMessage", "Invalid Password");
		}
		return "Login";
	}

	@Override
	public String viewProducts(HttpSession session, ModelMap map) {
		List<Product> products = productDao.fetchAll();
		if (products.isEmpty()) {
			session.setAttribute("failMessage", "No Products Present");
			return "redirect:/";
		} else {
			map.put("products", products);
			return "ViewProducts";
		}
	}

	@Override
	public String addToCart(int id, HttpSession session) {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/";
		} else {
			Product product = productDao.findById(id);
			if (product.getStock() > 0) {
				Cart cart = customer.getCart();
				List<Item> items = cart.getItems();
				if (items.isEmpty()) {
					Item item = new Item();
					item.setCategory(product.getCategory());
					item.setDescription(product.getDescription());
					item.setImagePath(product.getImagePath());
					item.setName(product.getName());
					item.setPrice(product.getPrice());
					item.setQuantity(1);
					items.add(item);
					session.setAttribute("successMessage", "Item added to Cart Success");
				} else {
					boolean flag = true;
					for (Item item : items) {
						if (item.getName().equals(product.getName())) {
							flag = false;
							if (item.getQuantity() < product.getStock()) {
								item.setQuantity(item.getQuantity() + 1);
								item.setPrice(item.getPrice() + product.getPrice());
								session.setAttribute("successMessage", "Item added to Cart Success");
							} else {
								session.setAttribute("failMessage", "Out of Stock");
							}
							break;
						}
					}
					if (flag) {
						Item item = new Item();
						item.setCategory(product.getCategory());
						item.setDescription(product.getDescription());
						item.setImagePath(product.getImagePath());
						item.setName(product.getName());
						item.setPrice(product.getPrice());
						item.setQuantity(1);
						items.add(item);
						session.setAttribute("successMessage", "Item added to Cart Success");
					}
				}
				customerDao.save(customer);
				session.setAttribute("customer", customerDao.findById(customer.getId()));
				return "redirect:/products";
			} else {
				session.setAttribute("failMessage", "Out of Stock");
				return "redirect:/";
			}
		}
	}

	@Override
	public String viewCart(ModelMap map, HttpSession session) {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/signin";
		} else {
			Cart cart = customer.getCart();
			List<Item> items = cart.getItems();
			if (items.isEmpty()) {
				session.setAttribute("failMessage", "No Items in cart");
				return "redirect:/";
			} else {
				map.put("items", items);
				return "ViewCart";
			}
		}
	}

	@Override
	public String removeFromCart(int id, HttpSession session) {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/signin";
		} else {
			Item item = itemDao.findById(id);
			if (item.getQuantity() == 1) {
				customer.getCart().getItems().remove(item);
				customerDao.save(customer);
				session.setAttribute("customer", customerDao.findById(customer.getId()));
				itemDao.delete(item);
				session.setAttribute("successMessage", "Item Removed from Cart");

			} else {
				item.setPrice(item.getPrice() - (item.getPrice() / item.getQuantity()));
				item.setQuantity(item.getQuantity() - 1);
				itemDao.save(item);
				session.setAttribute("successMessage", "Item Quantity Reduced By 1");
			}
			session.setAttribute("customer", customerDao.findById(customer.getId()));
			return "redirect:/cart";
		}
	}

	@Override
	public String paymentPage(HttpSession session, ModelMap map) {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/signin";
		} else {

			List<Item> items = customer.getCart().getItems();
			if (items.isEmpty()) {
				session.setAttribute("failMessage", "Nothing to Buy");
				return "redirect:/";
			} else {
				double price = items.stream().mapToDouble(x -> x.getPrice()).sum();
				try {
					RazorpayClient razorpay = new RazorpayClient("rzp_test_NL2VDewmKxugHZ", "OYO8g3i8aCPLCfn8piTOYlbi");

					JSONObject orderRequest = new JSONObject();
					orderRequest.put("amount", price * 100);
					orderRequest.put("currency", "INR");

					Order order = razorpay.orders.create(orderRequest);

					ShoppingOrder myOrder = new ShoppingOrder();
					myOrder.setDateTime(LocalDateTime.now());
					myOrder.setItems(items);
					myOrder.setOrderId(order.get("id"));
					myOrder.setStatus(order.get("status"));
					myOrder.setTotalPrice(price);

					orderDao.saveOrder(myOrder);

					map.put("key", "rzp_test_NL2VDewmKxugHZ");
					map.put("myOrder", myOrder);
					map.put("customer", customer);

					customer.getOrders().add(myOrder);
					customerDao.save(customer);
					session.setAttribute("customer", customerDao.findById(customer.getId()));
					return "PaymentPage";

				} catch (RazorpayException e) {
					e.printStackTrace();
					return "redirect:/";
				}
			}
		}
	}

	@Override
	public String confirmOrder(HttpSession session, int id, String razorpay_payment_id) {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/signin";
		} else {
			for (Item item : customer.getCart().getItems()) {
				Product product = productDao.findByName(item.getName());
				product.setStock(product.getStock() - item.getQuantity());
				productDao.save(product);
			}
			ShoppingOrder order = orderDao.findOrderById(id);
			order.setPaymnetId(razorpay_payment_id);
			order.setStatus("success");
			orderDao.saveOrder(order);
			customer.getCart().setItems(new ArrayList<Item>());
			customerDao.save(customer);
			session.setAttribute("customer", customerDao.findById(customer.getId()));
			session.setAttribute("successMessage", "Order Placed Success");
			return "redirect:/";
		}
	}

	@Override
	public String viewOrders(HttpSession session, ModelMap map) {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/signin";
		} else {
			List<ShoppingOrder> orders = customer.getOrders();
			if (orders == null || orders.isEmpty()) {
				session.setAttribute("failMessage", "No Orders Yet");
				return "redirect:/";
			} else {
				map.put("orders", orders);
				return "ViewOrders";
			}
		}
	}

	}


