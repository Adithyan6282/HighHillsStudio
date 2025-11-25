package com.example.highhillsstudio.HighHillsStudio.service.admin;



import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public Page<User> getUsers(String keyword, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if(keyword == null || keyword.isEmpty()) {
            return userRepository.findAll(pageable);
        } else {
            return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword,keyword,pageable);
        }

    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void blockUser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setBlocked(true);
            userRepository.save(user);
        });

    }

    public void unblockUser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setBlocked(false);
            userRepository.save(user);
        });
    }

}
