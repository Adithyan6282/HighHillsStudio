package com.example.highhillsstudio.HighHillsStudio.service.user;

import com.example.highhillsstudio.HighHillsStudio.entity.TransactionType;
import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.entity.UserWallet;
import com.example.highhillsstudio.HighHillsStudio.entity.WalletTransaction;
import com.example.highhillsstudio.HighHillsStudio.repository.UserWalletRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class WalletService {

    private final UserWalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;





    // Initialize wallet for new user
    public void createWalletForUser(User user) {
        walletRepository.findByUserId(user.getId()).orElseGet(() -> {
            UserWallet wallet = UserWallet.builder()
                    .user(user)
                    .balance(BigDecimal.ZERO)
                    .build();
            return walletRepository.save(wallet);
        });
    }

    // Get or create wallet safely
    private UserWallet getOrCreateWallet(User user) {
        return walletRepository.findByUserId(user.getId()).orElseGet(() -> {
            UserWallet newWallet = UserWallet.builder()
                    .user(user)
                    .balance(BigDecimal.ZERO)
                    .build();
            return walletRepository.save(newWallet);
        });
    }

    // Refund for canceled orders (auto credit)
    public void refundForCanceledOrder(User user, BigDecimal amount) {
        UserWallet wallet = getOrCreateWallet(user);

        wallet.credit(amount);
        walletRepository.save(wallet);

        WalletTransaction tx = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(TransactionType.CREDIT)
                .description("Refund for canceled order")
                .build();

        transactionRepository.save(tx);
    }

    // Refund for returned orders (admin confirmation required)
    public void refundForReturnedOrder(User user, BigDecimal amount) {
        UserWallet wallet = getOrCreateWallet(user);

        wallet.credit(amount);
        walletRepository.save(wallet);

        WalletTransaction tx = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(TransactionType.CREDIT)
                .description("Refund for returned order (admin approved)")
                .build();

        transactionRepository.save(tx);
    }

    // Pay using wallet balance
    public boolean payWithWallet(User user, BigDecimal amount) {
        UserWallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found for user"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            return false; // Insufficient balance
        }

        wallet.debit(amount);
        walletRepository.save(wallet);

        WalletTransaction tx = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(TransactionType.DEBIT)
                .description("Payment using wallet")
                .build();

        transactionRepository.save(tx);
        return true;
    }

    public BigDecimal getWalletBalance(User user) {
        return walletRepository.findByUserId(user.getId())
                .map(UserWallet::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    public List<WalletTransaction> getTransactionsByUser(User user) {
        UserWallet wallet = getOrCreateWallet(user);
        return transactionRepository.findByWalletOrderByTransactionDateDesc(wallet);
    }





    public List<WalletTransaction> getRecentTransactions(User user) {
        UserWallet wallet = getOrCreateWallet(user);
        return transactionRepository.findTop5ByWallet_User_IdOrderByTransactionDateDesc(user.getId());
    }



}

