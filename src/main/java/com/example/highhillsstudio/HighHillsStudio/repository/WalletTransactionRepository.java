package com.example.highhillsstudio.HighHillsStudio.repository;

import com.example.highhillsstudio.HighHillsStudio.entity.UserWallet;
import com.example.highhillsstudio.HighHillsStudio.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findByWalletOrderByTransactionDateDesc(UserWallet wallet);

    List<WalletTransaction> findTop5ByWallet_User_IdOrderByTransactionDateDesc(Long userId);

}
