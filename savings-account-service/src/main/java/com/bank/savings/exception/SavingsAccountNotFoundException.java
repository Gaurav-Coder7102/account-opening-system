package com.bank.savings.exception;

import com.bank.common.exception.BusinessException;

public class SavingsAccountNotFoundException extends BusinessException {
    public SavingsAccountNotFoundException(String message) {
        super(message, "SAVINGS_ACCOUNT_NOT_FOUND");
    }
}
