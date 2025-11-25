package com.example.highhillsstudio.HighHillsStudio.controller.user;

import com.example.highhillsstudio.HighHillsStudio.entity.OrderStatus;
import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.entity.UserAddress;
import com.example.highhillsstudio.HighHillsStudio.entity.UserOrder;
import com.example.highhillsstudio.HighHillsStudio.repository.UserOrderRepository;
import com.example.highhillsstudio.HighHillsStudio.service.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/users/payment")
@RequiredArgsConstructor
public class
PaymentController {


    private final PaymentService paymentService;
    private final UserOrderRepository userOrderRepository;
    private final UsersService usersService;
    private final CheckoutService checkoutService;
    private final CartService cartService;
    private final WalletService walletService;




    // step 1: Start payment process
    @GetMapping("/{orderCode}")
    public String initiatePayment(@PathVariable String orderCode,
                                  @RequestParam(value = "addressId") Long addressId,
                                  Model model, @AuthenticationPrincipal UserDetails userDetails) {

        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));


        //  Fetch Wallet Balance here
//        BigDecimal walletBalance = walletService.getWalletBalance(user);
//        model.addAttribute("walletBalance", walletBalance); // <-- Add to Model


        //  Get user's selected shipping address
        UserAddress address = checkoutService.getAddressByIdForUser(user, addressId);

        //  Create full order with items & total
        UserOrder order = checkoutService.createOrderFromCart(user, address, OrderStatus.PENDING_PAYMENT
        );

        //  Create Razorpay order using the new orderCode & total
        Map<String, Object> paymentData = paymentService.createPayment(order.getOrderCode(), order.getTotalAmount());

        if (paymentData.containsKey("error")) {
            model.addAttribute("message", paymentData.get("error"));
            return "user/payment-failure";
        }

        model.addAttribute("orderId", paymentData.get("orderId"));
        model.addAttribute("amount", ((Number) paymentData.get("amount")).longValue());
        model.addAttribute("currency", paymentData.get("currency"));
//        model.addAttribute("razorpayKey", /* inject from properties or config */ "rzp_test_RVZmkkeKFeUwyK");
        model.addAttribute("razorpayKey", paymentService.getRazorpayKey()); // dynamic
        model.addAttribute("orderCode", order.getOrderCode());
        model.addAttribute("addressId", addressId);


        System.out.println("Currency passed to frontend: " + paymentData.get("currency"));

        return "user/payment-page";
    }

    @PostMapping("/success")
    public String paymentSuccess(@RequestParam Map<String, String> razorpayData, Model model,
                                 @AuthenticationPrincipal UserDetails userDetails) {



        // Fetch the logged-in User entity
        User user = usersService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("Razorpay callback data: " + razorpayData);
        boolean isValid = paymentService.verifyPayment(razorpayData);
        if (isValid) {
            String orderCode = razorpayData.get("orderCode");
            String paymentId = razorpayData.get("razorpay_payment_id");

            paymentService.updateOrderStatus(orderCode, razorpayData);

            // ✅ Clear the cart after successful payment
            cartService.clearCart(user);

            UserOrder order = userOrderRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            model.addAttribute("order", order);
            model.addAttribute("paymentMethod", "ONLINE");
            model.addAttribute("paymentId", paymentId);
            model.addAttribute("message", "Payment successful! Your order has been placed.");
        } else {
            model.addAttribute("message", "Payment verification failed.");
            return "user/payment-failure";
        }

        return "user/order-success";


    }


    @GetMapping("/failure")
    public String paymentFailure(@RequestParam(required = false) String orderCode,
                                 @RequestParam(required = false) Long addressId,
                                 Model model) {
        model.addAttribute("message", "Payment failed or cancelled.");
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("addressId", addressId);
        return "user/payment-failure";
    }


}
