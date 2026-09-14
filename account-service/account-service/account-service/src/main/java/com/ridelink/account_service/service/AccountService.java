package com.ridelink.account_service.service;

import com.ridelink.account_service.dto.LoginRequest;
import com.ridelink.account_service.dto.LoginResponse;
import com.ridelink.account_service.dto.RegisterRequest;
import com.ridelink.account_service.entity.Account;
import com.ridelink.account_service.repository.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AccountService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Account registerAccount(RegisterRequest request) {

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Account account = new Account();

        account.setFullName(request.getFullName());
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setPhoneNumber(request.getPhoneNumber());
        account.setRole(request.getRole());
        account.setStatus("ACTIVE");

        return accountRepository.save(account);
    }

    public Optional<Account> getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public LoginResponse login(LoginRequest request) {

        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                account.getPassword())) {

            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new IllegalArgumentException("Account is not active");
        }

        String token = jwtService.generateToken(
                account.getEmail(),
                account.getRole()
        );

        return new LoginResponse(
                token,
                account.getId(),
                account.getFullName(),
                account.getEmail(),
                account.getRole()
        );
    }

    public Account updateAccountStatus(String email, String status) {

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found"));

        if (!status.equals("ACTIVE")
                && !status.equals("INACTIVE")
                && !status.equals("SUSPENDED")) {

            throw new IllegalArgumentException(
                    "Status must be ACTIVE, INACTIVE, or SUSPENDED"
            );
        }

        account.setStatus(status);

        return accountRepository.save(account);
    }
}