package com.example.highhillsstudio.HighHillsStudio.repository;

import com.example.highhillsstudio.HighHillsStudio.entity.Fit;
import com.example.highhillsstudio.HighHillsStudio.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.ls.LSInput;

import java.util.List;

@Repository
public interface FitRepository extends JpaRepository <Fit, Long> {

    // delete all fits by product
    @Transactional
    void deleteAllByProduct(Product product);

    List<Fit> findByProduct_Id(Long productId);

}
