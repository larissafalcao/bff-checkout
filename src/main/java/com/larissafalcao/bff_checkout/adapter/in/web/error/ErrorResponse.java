package com.larissafalcao.bff_checkout.adapter.in.web.error;

import java.util.UUID;

public record ErrorResponse(DisplayType displayType, String errorCode, UUID cartId, FrontendError frontendError) {

    static ErrorResponse of(FrontendErrorCatalog error, UUID cartId) {
        FrontendError frontendError = new FrontendError(error.code, error.title, error.description,
                error.primaryButton, error.secondaryButton);
        return new ErrorResponse(error.displayType, error.name(), cartId, frontendError);
    }
}
