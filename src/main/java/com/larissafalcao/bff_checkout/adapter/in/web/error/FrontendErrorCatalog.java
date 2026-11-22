package com.larissafalcao.bff_checkout.adapter.in.web.error;

import com.larissafalcao.bff_checkout.domain.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static com.larissafalcao.bff_checkout.adapter.in.web.error.FrontendUrls.PAYMENT_PAGE;
import static com.larissafalcao.bff_checkout.adapter.in.web.error.FrontendUrls.PLANS_PAGE;
import static com.larissafalcao.bff_checkout.adapter.in.web.error.FrontendUrls.SUCCESS_PAGE;

public enum FrontendErrorCatalog {
    PAYMENT_DECLINED(HttpStatus.UNPROCESSABLE_CONTENT, DisplayType.BLOCK, "P-1001",
            "Pagamento não aprovado",
            "Não foi possível concluir o pagamento. Tente novamente ou escolha outro meio.",
            new Button("Tentar novamente", "RETRY"),
            new Button("Trocar meio de pagamento", PAYMENT_PAGE)),
    INVALID_PAYMENT_METHOD(HttpStatus.UNPROCESSABLE_CONTENT, DisplayType.INTERVENTION, "P-1002",
            "Meio de pagamento indisponível",
            "O meio de pagamento escolhido não está disponível para este carrinho. Escolha outro meio.",
            new Button("Trocar meio de pagamento", PAYMENT_PAGE), null),
    INVALID_PAYMENT_DATA(HttpStatus.BAD_REQUEST, DisplayType.INTERVENTION, "P-1003",
            "Dados de pagamento inválidos",
            "Confira os dados informados para o meio de pagamento escolhido.",
            new Button("Corrigir dados", PAYMENT_PAGE), null),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, DisplayType.BLOCK, "P-2001",
            "Produto indisponível",
            "Um dos produtos escolhidos não está disponível. Escolha outro plano.",
            new Button("Escolher plano", PLANS_PAGE), null),
    DUPLICATE_PRODUCT(HttpStatus.BAD_REQUEST, DisplayType.INTERVENTION, "P-2002",
            "Produto repetido",
            "Cada plano só pode ser escolhido uma vez. Revise a sua seleção.",
            new Button("Revisar planos", PLANS_PAGE), null),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, DisplayType.BLOCK, "P-3001",
            "Carrinho não encontrado",
            "Não encontramos o seu carrinho. Comece o checkout novamente.",
            new Button("Recomeçar", PLANS_PAGE), null),
    CART_ALREADY_PAID(HttpStatus.CONFLICT, DisplayType.BLOCK, "P-3002",
            "Pagamento já realizado",
            "Este carrinho já foi pago. Nenhuma nova cobrança foi feita.",
            new Button("Ver minha assinatura", SUCCESS_PAGE), null),
    PAYMENT_IN_PROGRESS(HttpStatus.CONFLICT, DisplayType.BLOCK, "P-3003",
            "Pagamento em processamento",
            "Já existe um pagamento em andamento para este carrinho. Aguarde alguns instantes antes de tentar de novo.",
            new Button("Tentar novamente", "RETRY"), null),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, DisplayType.INTERVENTION, "P-0001",
            "Requisição inválida",
            "Alguns dados obrigatórios estão ausentes ou inválidos.",
            new Button("Voltar", PAYMENT_PAGE), null),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, DisplayType.BLOCK, "P-9001",
            "Serviço indisponível",
            "Estamos com instabilidade no momento. Tente novamente em instantes.",
            new Button("Tentar novamente", "RETRY"), null),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, DisplayType.BLOCK, "P-9002",
            "Algo deu errado",
            "Não foi possível concluir a operação. Se o problema continuar, fale com o suporte.",
            new Button("Voltar ao início", PLANS_PAGE), null);

    final HttpStatus status;
    final DisplayType displayType;
    final String code;
    final String title;
    final String description;
    final Button primaryButton;
    final Button secondaryButton;

    FrontendErrorCatalog(HttpStatus status, DisplayType displayType, String code, String title, String description,
                         Button primaryButton, Button secondaryButton) {
        this.status = status;
        this.displayType = displayType;
        this.code = code;
        this.title = title;
        this.description = description;
        this.primaryButton = primaryButton;
        this.secondaryButton = secondaryButton;
    }

    static FrontendErrorCatalog of(ErrorCode code) {
        return switch (code) {
            case PRODUCT_NOT_FOUND -> PRODUCT_NOT_FOUND;
            case DUPLICATE_PRODUCT -> DUPLICATE_PRODUCT;
            case CART_NOT_FOUND -> CART_NOT_FOUND;
            case CART_ALREADY_PAID -> CART_ALREADY_PAID;
            case PAYMENT_IN_PROGRESS -> PAYMENT_IN_PROGRESS;
            case INVALID_PAYMENT_METHOD -> INVALID_PAYMENT_METHOD;
            case INVALID_PAYMENT_DATA -> INVALID_PAYMENT_DATA;
            case PAYMENT_DECLINED -> PAYMENT_DECLINED;
            case SERVICE_UNAVAILABLE -> SERVICE_UNAVAILABLE;
        };
    }
}
