package com.example.highhillsstudio.HighHillsStudio.service.user;

import com.example.highhillsstudio.HighHillsStudio.dto.user.OrderDTO;
import com.example.highhillsstudio.HighHillsStudio.dto.user.OrderItemDTO;
import com.example.highhillsstudio.HighHillsStudio.entity.*;
import com.example.highhillsstudio.HighHillsStudio.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;
    private final UserOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final StockRepository stockRepository;

    @Value("${profile.upload.dir}")
    private String PROFILE_UPLOAD_DIR;



    // Get user by id
    public User getUser(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User getUserByEmail(String email) {
       return userRepository.findByEmail(email).orElseThrow(
               () -> new RuntimeException("User not found with email: " + email)
       );
    }

    public String saveProfileImage(MultipartFile profileImage) {
        if(profileImage == null || profileImage.isEmpty()) {
            return null;
        }

        try{
            // Generate a unique filename
            String ext = profileImage.getOriginalFilename()
                    .substring(profileImage.getOriginalFilename().lastIndexOf("."));
            String filename = UUID.randomUUID() + ext;



            Files.createDirectories(Paths.get(PROFILE_UPLOAD_DIR));

            // Save the file

            profileImage.transferTo(Paths.get(PROFILE_UPLOAD_DIR, filename).toFile());

            return filename;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Update user profile
    @Transactional
    public User updateUserProfile(User updatedUser, MultipartFile profileImage) {
        User user = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(updatedUser.getFullName());
        user.setPhone(updatedUser.getPhone());
        // add other fields if needed

        // Handle profile image if uploaded
        if(profileImage != null && !profileImage.isEmpty()) {
            // Delete old image if exists
            if (user.getProfileImage() != null) {

                File oldFile = Paths.get(PROFILE_UPLOAD_DIR, user.getProfileImage()).toFile();
                if (oldFile.exists()) oldFile.delete();
            }
            String fileName = saveProfileImage(profileImage); // implement this method
            user.setProfileImage(fileName);
        }

        return userRepository.save(user);
    }

    // Get user addresses
    public List<UserAddress> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    // Add/update address
    @Transactional
    public UserAddress saveAddress(UserAddress address) {
        return addressRepository.save(address);
    }

    // Delete address
    @Transactional
    public void deleteAddress(Long addressId) {
        addressRepository.deleteById(addressId);
    }

    // Get user orders
    public List<OrderDTO> getUserOrders(Long userId) {
        List<UserOrder> orders = orderRepository.findByUserIdOrderByPlacedAtDesc(userId);

        return orders.stream().map(order -> {
            List<OrderItemDTO> items = order.getItems().stream()
                    .map(item -> OrderItemDTO.builder()
                            .id(item.getId())
                            .productName(item.getProductName())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .totalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))) //  compute totalPrice
                            .cancellable(order.getStatus() == OrderStatus.PLACED) // only PLACED orders can cancel
                            .imageUrl(item.getProduct() != null && !item.getProduct().getImages().isEmpty()
                                    ? item.getProduct().getImages().get(0).getImg()
                                    : null)
                            .build())
                    .collect(Collectors.toList());

            return OrderDTO.builder()
                    .orderCode(order.getOrderCode())
                    .status(order.getStatus().name())
                    .totalAmount(order.getTotalAmount())
                    .items(items)
                    .address(order.getShippingAddress())  //  Add address here too
                    .build();
        }).collect(Collectors.toList());


    }

    // Cancel entire order
    @Transactional
    public boolean cancelOrder(String orderCode, Long userId) {
        Optional<UserOrder> optionalOrder = orderRepository.findByOrderCode(orderCode);
        if(optionalOrder.isPresent()) {
            UserOrder order = optionalOrder.get();

            // Ensure the order belongs to the user and is cancellable
            if(order.getUser().getId().equals(userId) && order.getStatus() == OrderStatus.PLACED) {

                // Cancel all items in the order
                for(OrderItem item : order.getItems()) {
                    item.setStatus(OrderStatus.CANCELED);
                    orderItemRepository.save(item);

                    // Update stock for each item's fit
                    Fit fit = item.getFit();
                    if(fit != null && fit.getStock() != null) {
                        Stock stock = fit.getStock();
                        stock.setQuantity(stock.getQuantity() + item.getQuantity());
                        stock.setLastUpdated(LocalDateTime.now());
                        stockRepository.save(stock);
                    }
                }

                // Cancel the whole order
                order.setStatus(OrderStatus.CANCELED);
                orderRepository.save(order);

                return true;
            }
        }
        return false;
    }


    public OrderDTO getUserOrderByCode(String orderCode, Long userId) {
        UserOrder order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to order");
        }

        List<OrderItemDTO> items = order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .totalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))) //  compute totalPrice
                        .cancellable(order.getStatus() == OrderStatus.PLACED)
                        .imageUrl(item.getProduct() != null && !item.getProduct().getImages().isEmpty()
                                ? item.getProduct().getImages().get(0).getImg()
                                : null)
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .orderCode(order.getOrderCode())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .items(items)
                .address(order.getShippingAddress())  //  Add address here too
                .build();
    }


    @Transactional
    public boolean returnOrder(String orderCode, Long userId, String reason) {
        Optional<UserOrder> optionalOrder = orderRepository.findByOrderCode(orderCode);
        if(optionalOrder.isPresent()) {
            UserOrder order = optionalOrder.get();

            // Only allow return if delivered
            if(order.getUser().getId().equals(userId) && order.getStatus() == OrderStatus.DELIVERED) {
                order.setStatus(OrderStatus.RETURN_REQUESTED);
                order.setReturnReason(reason); // Add this field in UserOrder entity
                orderRepository.save(order);
                return true;
            }
        }
        return false;
    }



    public ByteArrayOutputStream generateInvoicePdf(String orderCode, Long userId) throws IOException {
        UserOrder order = orderRepository.findByOrderCodeAndUserId(orderCode, userId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font subFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL);

            // Title
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Invoice", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(title);
            document.add(new com.itextpdf.text.Paragraph(" ")); // empty line

            // Invoice date
            document.add(new com.itextpdf.text.Paragraph("Invoice Date: " + java.time.LocalDate.now(), subFont));
            document.add(new com.itextpdf.text.Paragraph(" "));

            // Customer details
            User user = order.getUser();
            document.add(new com.itextpdf.text.Paragraph("Customer Name: " + user.getFullName(), subFont));
            document.add(new com.itextpdf.text.Paragraph("Email: " + user.getEmail(), subFont));
            document.add(new com.itextpdf.text.Paragraph("Phone: " + (order.getShippingAddress() != null ? order.getShippingAddress().getPhone() : ""), subFont));
            document.add(new com.itextpdf.text.Paragraph(" "));

            // Shipping address
            if (order.getShippingAddress() != null) {
                UserAddress addr = order.getShippingAddress();
                document.add(new com.itextpdf.text.Paragraph("Shipping Address:", subFont));
                document.add(new com.itextpdf.text.Paragraph(addr.getLine1(), subFont));
                if (addr.getLine2() != null && !addr.getLine2().isEmpty()) {
                    document.add(new com.itextpdf.text.Paragraph(addr.getLine2(), subFont));
                }
                document.add(new com.itextpdf.text.Paragraph(addr.getCity() + ", " + addr.getState() + " - " + addr.getPincode(), subFont));
                document.add(new com.itextpdf.text.Paragraph(addr.getCountry(), subFont));
                document.add(new com.itextpdf.text.Paragraph(" "));
            }

            // Order details
            document.add(new com.itextpdf.text.Paragraph("Order Code: " + order.getOrderCode(), subFont));
            document.add(new com.itextpdf.text.Paragraph("Status: " + order.getStatus(), subFont));
            document.add(new com.itextpdf.text.Paragraph(" "));

            // Items table
            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(4); // 4 columns: Product, Qty, Price, Total
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Table headers
            table.addCell("Product");
            table.addCell("Quantity");
            table.addCell("Unit Price (₹)");
            table.addCell("Total (₹)");

            for (OrderItem item : order.getItems()) {
                table.addCell(item.getProductName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(String.valueOf(item.getPrice()));
                table.addCell(String.valueOf(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
            }
            document.add(table);

            document.add(new com.itextpdf.text.Paragraph(" "));
            document.add(new com.itextpdf.text.Paragraph("Total Amount: ₹" + order.getTotalAmount(), subFont));

            document.close();
        } catch (Exception e) {
            throw new IOException("Error generating PDF invoice", e);
        }

        return out;
    }




    public List<OrderDTO> searchUserOrders(Long userId, String searchTerm) {
        List<UserOrder> orders = orderRepository.findByUserIdOrderByPlacedAtDesc(userId);

        return orders.stream()
                .filter(o -> o.getOrderCode().toLowerCase().contains(searchTerm.toLowerCase()))
                .map(order -> {
                    List<OrderItemDTO> items = order.getItems().stream()
                            .map(item -> OrderItemDTO.builder()
                                    .id(item.getId())
                                    .productName(item.getProductName())
                                    .quantity(item.getQuantity())
                                    .price(item.getPrice())
                                    .totalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))) // add totalPrice
                                    .cancellable(order.getStatus() == OrderStatus.PLACED)
                                    .imageUrl(item.getProduct() != null && !item.getProduct().getImages().isEmpty()
                                            ? item.getProduct().getImages().get(0).getImg()
                                            : null)
                                    .build())
                            .collect(Collectors.toList());

                    return OrderDTO.builder()
                            .orderCode(order.getOrderCode())
                            .status(order.getStatus().name())
                            .totalAmount(order.getTotalAmount())
                            .items(items)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public Page<OrderDTO> getUserOrdersPaginated(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<OrderDTO> allOrders = getUserOrders(userId); // existing method
        int start = Math.min((int) pageable.getOffset(), allOrders.size());
        int end = Math.min((start + pageable.getPageSize()), allOrders.size());

        List<OrderDTO> ordersPage = allOrders.subList(start, end);
        return new PageImpl<>(ordersPage, pageable, allOrders.size());
    }



}
