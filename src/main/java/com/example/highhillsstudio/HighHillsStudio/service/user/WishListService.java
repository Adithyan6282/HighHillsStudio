package com.example.highhillsstudio.HighHillsStudio.service.user;

import com.example.highhillsstudio.HighHillsStudio.entity.Product;
import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.entity.WishListItem;
import com.example.highhillsstudio.HighHillsStudio.repository.WishListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishListRepository wishListRepository;

    public void removeFromWishList(User user, Long productId) {

        wishListRepository.findByUserAndProductId(user, productId)

                .ifPresent(wishListRepository::delete);
    }

    public void addToWishList(User user, Product product) {
        if(wishListRepository.findByUserAndProduct(user, product).isEmpty()) {
            WishListItem item = WishListItem.builder()
                    .user(user)
                    .product(product)
                    .build();
            wishListRepository.save(item);
        }
    }



    public List<WishListItem> getWishlistItemsByUser(User user) {
        return wishListRepository.findByUser(user);
    }


    public WishListItem findByUserAndProduct(User user, Long productId) {
        return wishListRepository.findByUserAndProductId(user, productId).orElse(null);
    }

}
