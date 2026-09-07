package com.bank.account.exception;

import com.bank.common.exception.BusinessException;

public class ApplicationNotFoundException extends BusinessException {
    public ApplicationNotFoundException(String message) {
        super(message, "APPLICATION_NOT_FOUND");
    }
}
