package com.larissafalcao.bff_checkout.adapter.in.web.error;

public record FrontendError(String code, String title, String description, Button primaryButton,
                            Button secondaryButton) {
}
