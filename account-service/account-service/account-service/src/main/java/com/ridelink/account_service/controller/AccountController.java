package com.ridelink.account_service.controller;

import com.ridelink.account_service.dto.AccountResponse;
import com.ridelink.account_service.dto.LoginRequest;
import com.ridelink.account_service.dto.LoginResponse;
import com.ridelink.account_service.dto.RegisterRequest;
import com.ridelink.account_service.entity.Account;
import com.ridelink.account_service.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    public ResponseEntity<AccountResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        Account savedAccount = accountService.registerAccount(request);

        AccountResponse response = new AccountResponse(
                savedAccount.getId(),
                savedAccount.getFullName(),
                savedAccount.getEmail(),
                savedAccount.getPhoneNumber(),
                savedAccount.getRole(),
                savedAccount.getStatus()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = accountService.login(request);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER')")
    @GetMapping("/{email}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String email) {

        return accountService.getAccountByEmail(email)
                .map(account -> new AccountResponse(
                        account.getId(),
                        account.getFullName(),
                        account.getEmail(),
                        account.getPhoneNumber(),
                        account.getRole(),
                        account.getStatus()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER')")
    @PutMapping("/{email}/status")
    public ResponseEntity<AccountResponse> updateStatus(
            @PathVariable String email,
            @RequestParam String status) {

        Account account = accountService.updateAccountStatus(email, status);

        AccountResponse response = new AccountResponse(
                account.getId(),
                account.getFullName(),
                account.getEmail(),
                account.getPhoneNumber(),
                account.getRole(),
                account.getStatus()
        );

        return ResponseEntity.ok(response);
    }
}