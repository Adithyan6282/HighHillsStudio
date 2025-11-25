package com.example.highhillsstudio.HighHillsStudio.service.user;

import com.example.highhillsstudio.HighHillsStudio.entity.OrderStatus;
import com.example.highhillsstudio.HighHillsStudio.entity.UserOrder;
import com.example.highhillsstudio.HighHillsStudio.repository.UserOrderRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.razorpay.Order;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserOrderRepository userOrderRepository;

    private RazorpayClient razorpayClient;

    // Getter for public key (used in the frontend)
    // Inject from application.properties
    @Getter
    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    // Initialize RazorpayClient
    @PostConstruct
    public void init() {
        try {
            razorpayClient = new RazorpayClient(razorpayKey, razorpaySecret);
            System.out.println("✅ Razorpay Client Initialized Successfully");
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to initialize Razorpay client: " + e.getMessage());
        }
    }

    // Create payment order
    public Map<String, Object> createPayment(String orderCode, BigDecimal amount) {
        Map<String, Object> data = new HashMap<>();
        try {
            BigDecimal amountInPaise = amount.multiply(BigDecimal.valueOf(100));
            int amountAsInt = amountInPaise.intValueExact();

            JSONObject options = new JSONObject();
            options.put("amount", amountAsInt);
            options.put("currency", "INR");
            options.put("receipt", orderCode);

            Order order = razorpayClient.orders.create(options);

            data.put("orderId", order.get("id"));
            data.put("amount", amountAsInt);
            data.put("currency", "INR");
            data.put("orderCode", orderCode);
        } catch (Exception e) {
            data.put("error", "Payment creation failed: " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }

    // Verify payment signature
    public boolean verifyPayment(Map<String, String> razorpayData) {
        try {
            JSONObject params = new JSONObject();
            params.put("razorpay_order_id", razorpayData.get("razorpay_order_id"));
            params.put("razorpay_payment_id", razorpayData.get("razorpay_payment_id"));
            params.put("razorpay_signature", razorpayData.get("razorpay_signature"));

            Utils.verifyPaymentSignature(params, razorpaySecret);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update order status
    public void updateOrderStatus(String orderCode, Map<String, String> razorpayData) {
        UserOrder order = userOrderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.PAID);
        userOrderRepository.save(order);
    }

}

