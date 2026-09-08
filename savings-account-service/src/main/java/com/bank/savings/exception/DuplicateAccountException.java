package com.bank.savings.exception;

import com.bank.common.exception.BusinessException;

public class DuplicateAccountException extends BusinessException {
    public DuplicateAccountException(String message) {
        super(message, "DUPLICATE_ACCOUNT");
    }
}
