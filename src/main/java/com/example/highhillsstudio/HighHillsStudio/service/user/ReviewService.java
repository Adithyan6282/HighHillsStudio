package com.example.highhillsstudio.HighHillsStudio.service.user;

import com.example.highhillsstudio.HighHillsStudio.entity.Product;
import com.example.highhillsstudio.HighHillsStudio.entity.Review;
import com.example.highhillsstudio.HighHillsStudio.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByProduct(Product product) {
        return reviewRepository.findByProductOrderByCreatedAtDesc(product);
    }


}
