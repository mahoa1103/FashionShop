package group4.organicapplication.controller;


import group4.organicapplication.model.*;
import group4.organicapplication.service.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@SessionAttributes("loggedInUser")
@RequestMapping("")
public class PaymentController {
    @Autowired
    private UserService userService;
    @Autowired
    private PaymentService paymentService;

    @Autowired
    ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;



    @ModelAttribute("loggedInUser")
    public User loggedInUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByEmail(auth.getName());
    }

    @PostMapping("/payment/submitOrder")
    public ResponseEntity<String> submitOrder(@RequestBody Orders orders, HttpServletRequest request) {

        Orders order = new Orders();

        order.setAddress(orders.getAddress());
        order.setEmail(orders.getEmail());
        order.setNote(orders.getNote());
        order.setOrderDay(orders.getOrderDay());
        order.setTotalPrice(orders.getTotalPrice());
        order.setOrderStatus(orders.getOrderStatus());
        order.setUser(loggedInUser());
        order.setPhone(orders.getPhone());


        List<PurchaseOrder> purchaseOrders = new ArrayList<>();
        for (PurchaseOrder purchaseOrder : orders.getOrderDetailList()) {
            PurchaseOrder purchaseOrder1 = new PurchaseOrder();
            purchaseOrder1.setProduct(purchaseOrder.getProduct());
            purchaseOrder1.setQuantity(purchaseOrder.getQuantity());
            purchaseOrder1.setTotalAmount(purchaseOrder.getTotalAmount());
            purchaseOrders.add(purchaseOrder1);

            List<Product> products = productService.getAllProduct();
            for (Product product : products) {
                if (product.getProductID() == purchaseOrder.getProduct().getProductID()) {
                    int newQuantity = (int) (product.getQuantity() - purchaseOrder.getQuantity());
                    product.setQuantity(newQuantity);
                    break;
                }
            }
        }

        Orders savedOrder = orderService.placeOrder(order, purchaseOrders);

        //xóa cart
        List<CartItem> cartItems = cartService.getCartItems();
        cartItems.clear();

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = paymentService.createOrder(orders.getTotalPrice(),savedOrder.getId().toString(), baseUrl);

        return ResponseEntity.ok(vnpayUrl);
    }

    @GetMapping("/vnpay-payment")
    public String GetMapping(HttpServletRequest request, Model model) throws ParseException {
        //xử lý thông tin thanh toán trong yêu cầu và trả về mã trạng thái thanh toán.
        int paymentStatus =paymentService.orderReturn(request);

        //Trích xuất các thông số khác nhau từ yêu cầu liên quan đến thanh toán
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPriceStr = request.getParameter("vnp_Amount");
        int totalPrice = Integer.parseInt(totalPriceStr) / 100;

        //Cập nhật trạng thái đơn hàng
        Orders order = orderService.findById(Long.parseLong(orderInfo));
        if(paymentStatus == 1) {    // thanh toán thành công
            order.setPaid(true);
            orderService.save(order);
        }

        //Định dạng ngày
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = inputFormat.parse(paymentTime);

        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = outputFormat.format(date);

        model.addAttribute("orderId", orderInfo);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("paymentTime", formattedDate);
        model.addAttribute("transactionId", transactionId);

        // nếu paymentstatus khác 1 thì thanh toán thất bại
        return paymentStatus == 1 ? "ordersuccess" : "orderfail";

    }
}
