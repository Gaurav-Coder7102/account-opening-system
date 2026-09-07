package com.bank.account.exception;

import com.bank.common.exception.BusinessException;

public class InvalidStateTransitionException extends BusinessException {
    public InvalidStateTransitionException(String message) {
        super(message, "INVALID_STATE_TRANSITION");
    }
}
