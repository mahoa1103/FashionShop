package group4.organicapplication.controller;

import group4.organicapplication.model.*;
import group4.organicapplication.service.CartService;
import group4.organicapplication.service.OrderService;
import group4.organicapplication.service.ProductService;
import group4.organicapplication.service.UserService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.javamail.MimeMessageHelper;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@SessionAttributes("loggedInUser")
@RequestMapping("/purchase")
public class PurchaseController {
    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    ProductService productService;

    @Autowired
    private JavaMailSender javaMailSender;

    @ModelAttribute("loggedInUser")
    public User loggedInUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByEmail(auth.getName());
    }

//    public User getSessionUserPurchase(HttpServletRequest httpServletRequest){
//        return (User) httpServletRequest.getSession().getAttribute("loggedInUser");
//    }
//
//    @PostMapping("/updateInfo")
//    @ResponseBody
//    public ResponseObject commitChangePurchase(HttpServletRequest res, @RequestBody User ng) {
//        User currentUser = getSessionUserPurchase(res);
//        currentUser.setFirstName(ng.getFirstName());
//        currentUser.setLastName(ng.getLastName());
//        currentUser.setPhone(ng.getPhone());
//        currentUser.setAddress(ng.getAddress());
//        userService.updateUser(currentUser);
//        return new ResponseObject();
//    }

    @PostMapping("/place-order")
    public ResponseEntity<String> placeOrder(@RequestBody Orders orders) {

        Orders order = new Orders();

        order.setAddress(orders.getAddress());
        order.setEmail(orders.getEmail());
        order.setNote(orders.getNote());
        order.setOrderDay(orders.getOrderDay());
        order.setTotalPrice(orders.getTotalPrice());
        order.setOrderStatus(orders.getOrderStatus());
        order.setUser(loggedInUser());
        order.setPhone(orders.getPhone());
        order.setReceiveDay(orders.getReceiveDay());
        order.setPaid(orders.getIsPaid());



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

        orderService.placeOrder(order, purchaseOrders);

        List<CartItem> cartItems = cartService.getCartItems();
        cartItems.clear();


        return ResponseEntity.ok().build();

    }


    @PostMapping("/sendEmail")
    public ResponseEntity<String> sendEmail(@RequestBody Map<String, Object> emailData) {
        String email = (String) emailData.get("email");
        String name = (String) emailData.get("name");
        String orderDay = (String) emailData.get("orderDay");
        String phone = (String) emailData.get("phone");
        String address = (String) emailData.get("address");
        String totalPrice = (String) emailData.get("totalPrice");
        int fullPrice = Integer.parseInt(totalPrice) + 5000;// Phí vận chuyển

        List<Map<String, Object>> cartItems = (List<Map<String, Object>>) emailData.get("cartItems");


        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage,  "utf-8");

        try {
            message.setTo(email);
            message.setSubject("Xác nhận đơn hàng");

            StringBuilder emailContent = new StringBuilder();
            emailContent.append("Xin chào " + name + ",");
            emailContent.append("<br>");
            emailContent.append("Đơn hàng của bạn đã được đặt thành công.<br> Đơn hàng của bạn sẽ được giao trong ngày hôm nay, vui lòng liên hệ với chúng tôi qua email này nếu bạn không nhận được đơn hàng.");
            emailContent.append("<br><br>");
            emailContent.append("<b>THÔNG TIN ĐƠN HÀNG:</b><hr>");
            emailContent.append("<br>");
            emailContent.append("Ngày đặt hàng: " +orderDay + "<br>");
            emailContent.append("Số điện thoại: " + phone +"<br>");
            emailContent.append("Địa chỉ giao hàng: " + address + "<br><br>");


            for (Map<String, Object> cartItem : cartItems) {
                emailContent.append("Tên sản phẩm: ").append(cartItem.get("productName")).append("<br>");
                emailContent.append("<img src='https://mahoa1103.github.io/FashionShop/src/main/resources/static/images/"+ cartItem.get("productId") + '/').append(cartItem.get("imageProduct")).append("' width='100' height='100'>").append("<br>");
                emailContent.append("Số lượng: ").append(cartItem.get("quantity")).append("<br>");
                emailContent.append("Thành tiền: ").append(cartItem.get("totalPrice")).append(" VNĐ").append("<br>");
                emailContent.append("<hr>");
            }
            emailContent.append("<b>Tổng tiền: </b>" + totalPrice +" VNĐ").append("<br>");
            emailContent.append("<b>Phí vận chuyển:</b> 5000" +" VNĐ</b>").append("<br>");
            emailContent.append("<b>Tổng thanh toán: </b> " + fullPrice +" VNĐ").append("<br>");
            emailContent.append("<br>");
            emailContent.append("Fashion Shop cảm ơn bạn đã mua hàng!");

            message.setText(emailContent.toString(), true);
            javaMailSender.send(mimeMessage);
            return ResponseEntity.ok("Gửi email thành công.");

        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Gửi email thất bại.");
        }


    }
}
