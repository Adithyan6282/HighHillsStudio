package com.example.highhillsstudio.HighHillsStudio.service.user;

import com.example.highhillsstudio.HighHillsStudio.dto.user.*;
import com.example.highhillsstudio.HighHillsStudio.entity.*;
import com.example.highhillsstudio.HighHillsStudio.repository.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartRepository cartRepo;
    private final UserAddressRepository addressRepo;
    private final FitRepository fitRepository;
    private final UserOrderRepository userOrderRepository;
    private final CartService cartService;
    private final CouponRepository couponRepository;

    private final ProductOfferRepository productOfferRepo;
    private final CategoryOfferRepository categoryOfferRepo;

    //  Fetch addresses for logged-in user
    public List<UserAddressDTO> getUserAddresses(User user) {
        return addressRepo.findByUserId(user.getId()).stream()
                .map(addr -> UserAddressDTO.builder()
                        .id(addr.getId())
                        .line1(addr.getLine1())
                        .line2(addr.getLine2())
                        .city(addr.getCity())
                        .state(addr.getState())
                        .country(addr.getCountry())
                        .pincode(addr.getPincode())
                        .phone(addr.getPhone())
                        .isDefault(addr.isDefault())
                        .build())
                .collect(Collectors.toList());
    }




    public CheckoutSummaryDTO getCartSummary(User user) {

        Cart cart = cartRepo.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepo.save(newCart);
                });

        List<CheckoutItemDTO> itemDTOs = cart.getItems().stream().map(cartItem -> {

            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();
            BigDecimal basePrice = product.getBasePrice();

            //  Step 1: Product % discount
            BigDecimal productDiscount = basePrice
                    .multiply(BigDecimal.valueOf(product.getDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100));

            BigDecimal priceAfterProductDiscount = basePrice.subtract(productDiscount);

            //  Step 2: Get best offer (fixed or percentage)
            //   Get both best offer discount and applied offer type
            OfferResult offerResult = getBestOfferDiscount(product); //  returns both discount + offer type
            BigDecimal bestOfferDiscount = offerResult.getDiscount(); //  updated
            String appliedOfferType = offerResult.getOfferType();     //  updated

            //  Step 3: Apply best offer on discounted price
            BigDecimal finalPrice = priceAfterProductDiscount.subtract(bestOfferDiscount);
            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                finalPrice = BigDecimal.ZERO;
            }

            //  Step 4: Calculate total for quantity
            BigDecimal totalPrice = finalPrice.multiply(BigDecimal.valueOf(quantity));

            //  Step 5: Calculate total discount (combined)
            BigDecimal totalDiscount = basePrice.subtract(finalPrice);

            return CheckoutItemDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .imageUrl(product.getImages().isEmpty() ? null : product.getImages().get(0).getImg())
                    .quantity(quantity)
                    .basePrice(basePrice)
                    .discountPercentage(product.getDiscountPercentage())
                    .bestOfferDiscount(bestOfferDiscount)
                    .totalDiscount(totalDiscount)
                    .finalPrice(finalPrice)
                    .totalPrice(totalPrice)
                    .appliedOfferType(appliedOfferType) // 🔹 ADDED field for frontend
                    .build();

        }).collect(Collectors.toList());

        // ------------------ Totals ------------------

        BigDecimal subtotal = itemDTOs.stream()
                .map(CheckoutItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = itemDTOs.stream()
                .map(CheckoutItemDTO::getTotalDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //  Shipping logic
        BigDecimal shipping = subtotal.compareTo(new BigDecimal("1500")) < 0
                ? new BigDecimal("50")
                : BigDecimal.ZERO;



        //  Coupon discount
        BigDecimal couponDiscount = BigDecimal.ZERO;
        Coupon appliedCoupon = cart.getAppliedCoupon(); // read coupon saved in cart
        if (appliedCoupon != null) {
            couponDiscount = subtotal.subtract(applyCoupon(appliedCoupon.getCode(), subtotal));
        }

        //  Final total
        BigDecimal finalTotal = subtotal.add(shipping).subtract(couponDiscount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        return CheckoutSummaryDTO.builder()
                .items(itemDTOs)
                .subtotal(subtotal)
                .totalDiscount(totalDiscount)
                .shipping(shipping)
                .couponDiscount(couponDiscount)
                .finalTotal(finalTotal)
                .build();
    }


    //  Set default address
    public void setDefaultAddress(User user, Long addressId) {
        List<UserAddress> addresses = addressRepo.findByUserId(user.getId());
        for (UserAddress addr : addresses) {
            addr.setDefault(addr.getId().equals(addressId));
        }
        addressRepo.saveAll(addresses);
    }

    //  Add or update address
    public UserAddressDTO saveAddress(User user, UserAddressDTO dto) {
        UserAddress address = new UserAddress();
        if (dto.getId() != null) {
            address = addressRepo.findById(dto.getId()).orElse(new UserAddress());
        }
        address.setUser(user);
        address.setLine1(dto.getLine1());
        address.setLine2(dto.getLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());
        address.setPincode(dto.getPincode());
        address.setPhone(dto.getPhone());
        address.setDefault(dto.isDefault());
        addressRepo.save(address);

        return dto;
    }

    //  Delete an address
    @Transactional
    public void deleteAddress(User user, Long addressId) {
        UserAddress address = addressRepo.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        //  ensure the address belongs to the user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You cannot delete this address");
        }

        addressRepo.delete(address);
    }


    @Transactional
    public UserOrder createOrderFromCart(User user, UserAddress address, OrderStatus status) {
        Cart cart = cartRepo.findByUser(user).orElseThrow();

        if (cart.getItems().isEmpty()) throw new IllegalStateException("Cannot place order with empty cart.");

        UserOrder order = new UserOrder();
        order.setUser(user);
        order.setShippingAddress(address);
        order.setStatus(status);
        order.setOrderCode(generateOrderCode());

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal offerDiscountTotal = BigDecimal.ZERO;
        BigDecimal couponDiscount = BigDecimal.ZERO;

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();

            OfferResult offerResult = getBestOfferDiscount(product);
            BigDecimal offerDiscount = offerResult.getDiscount();
            BigDecimal basePrice = product.getBasePrice();
            BigDecimal productDiscount = basePrice.multiply(BigDecimal.valueOf(product.getDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100));
            BigDecimal priceAfterProductDiscount = basePrice.subtract(productDiscount);

            BigDecimal finalPrice = priceAfterProductDiscount.subtract(offerDiscount);
            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) finalPrice = BigDecimal.ZERO;

            totalAmount = totalAmount.add(finalPrice.multiply(BigDecimal.valueOf(quantity)));
            offerDiscountTotal = offerDiscountTotal.add(offerDiscount.multiply(BigDecimal.valueOf(quantity)));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setPrice(finalPrice);
            orderItem.setQuantity(quantity);
            orderItem.setOfferType(offerResult.getOfferType());
            orderItem.setFit(cartItem.getFit());
            orderItems.add(orderItem);



        }

        //  Apply coupon from cart
        Coupon appliedCoupon = cart.getAppliedCoupon();
        if (appliedCoupon != null) {
            BigDecimal discountedTotal = applyCoupon(appliedCoupon.getCode(), totalAmount);
            couponDiscount = totalAmount.subtract(discountedTotal);
            order.setCoupon(appliedCoupon);
        }


//        BigDecimal finalAmount = totalAmount.subtract(offerDiscountTotal).subtract(couponDiscount);
        BigDecimal finalAmount = totalAmount.subtract(couponDiscount);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) finalAmount = BigDecimal.ZERO;

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setOfferDiscount(offerDiscountTotal);
        order.setCouponDiscount(couponDiscount);
        order.setFinalAmount(finalAmount);

        userOrderRepository.save(order);

        // Clear cart and applied coupon
        cart.getItems().clear();
        cart.setAppliedCoupon(null);
        cartRepo.save(cart);

        return order;
    }


    @Transactional
    public void deductStockForOrder(UserOrder order) {
        for (OrderItem item : order.getItems()) {
            Fit fit = item.getFit();
            if (fit != null && fit.getStock() != null) {
                int currentQty = fit.getStock().getQuantity();
                int newQty = currentQty - item.getQuantity();
                if (newQty < 0) {
                    throw new IllegalStateException(
                            "Insufficient stock while deducting for product: " + item.getProductName()
                    );
                }
                fit.getStock().setQuantity(newQty);
                fitRepository.save(fit);
            }
        }
    }


    public UserAddress getAddressByIdForUser(User user, Long addressId) {
        UserAddress address = addressRepo.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("This address does not belong to the user");
        }
        return address;
    }


    // Generate unique order code
    private String generateOrderCode() {
        String prefix = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = String.format("%04d", new Random().nextInt(10000));
        return prefix + uniquePart;

    }


    public void applyCouponToCart(User user, String couponCode) {
        Cart cart = cartRepo.findByUser(user).orElseThrow();
        Coupon coupon = couponRepository.findByCodeAndActiveTrue(couponCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired coupon"));
        cart.setAppliedCoupon(coupon);
        cartRepo.save(cart);
    }


    // Apply coupon
    public BigDecimal applyCoupon(String code, BigDecimal currentTotal) {
        Coupon coupon = couponRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired coupon"));

        // For simplicity, fixed discount amount. You can also implement percentage logic
        BigDecimal discountedTotal = currentTotal.subtract(coupon.getDiscountAmount());
        return discountedTotal.compareTo(BigDecimal.ZERO) > 0 ? discountedTotal : BigDecimal.ZERO;
    }

    // Remove coupon (just return original total)
    public BigDecimal removeCoupon(BigDecimal originalTotal) {
        return originalTotal;
    }


    //  returns both discount and offer type
    private OfferResult getBestOfferDiscount(Product product) {
        BigDecimal basePrice = product.getBasePrice();
        LocalDate today = LocalDate.now();

        BigDecimal productDiscount = BigDecimal.ZERO;
        BigDecimal categoryDiscount = BigDecimal.ZERO;

        // Product offer discount
        if (productOfferRepo.findByProductAndActiveTrue(product).isPresent()) {
            var offer = productOfferRepo.findByProductAndActiveTrue(product).get();
            if ((offer.getStartDate() == null || !today.isBefore(offer.getStartDate())) &&
                    (offer.getEndDate() == null || !today.isAfter(offer.getEndDate()))) {
                productDiscount = offer.isPercentage()
                        ? basePrice.multiply(offer.getDiscountAmount()).divide(BigDecimal.valueOf(100))
                        : offer.getDiscountAmount();
            }
        }

        // Category offer discount
        if (product.getCategory() != null && categoryOfferRepo.findByCategoryAndActiveTrue(product.getCategory()).isPresent()) {
            var offer = categoryOfferRepo.findByCategoryAndActiveTrue(product.getCategory()).get();
            if ((offer.getStartDate() == null || !today.isBefore(offer.getStartDate())) &&
                    (offer.getEndDate() == null || !today.isAfter(offer.getEndDate()))) {
                categoryDiscount = offer.isPercentage()
                        ? basePrice.multiply(offer.getDiscountAmount()).divide(BigDecimal.valueOf(100))
                        : offer.getDiscountAmount();
            }
        }

        //  Decide which offer wins
        if (productDiscount.compareTo(categoryDiscount) > 0) {
            return new OfferResult(productDiscount, "Product Offer");
        } else if (categoryDiscount.compareTo(productDiscount) > 0) {
            return new OfferResult(categoryDiscount, "Category Offer");
        } else {
            return new OfferResult(BigDecimal.ZERO, "None");
        }
    }


    //  NEW HELPER CLASS
    @Getter
    @AllArgsConstructor
    private static class OfferResult {
        private final BigDecimal discount;
        private final String offerType;


    }

}


