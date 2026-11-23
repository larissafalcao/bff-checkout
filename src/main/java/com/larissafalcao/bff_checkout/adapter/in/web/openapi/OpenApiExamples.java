package com.larissafalcao.bff_checkout.adapter.in.web.openapi;

final class OpenApiExamples {

    static final String CHECKOUT_CARD_REQUEST = """
            {
              "cartId": "5a7ed441-567e-4f63-825e-dbdf07e640d6",
              "paymentMethod": {
                "name": "Cartão de Crédito",
                "card": {"holderName": "Ana Souza", "number": "4111111111114242", "expiry": "12/35", "cvv": "123"}
              }
            }""";

    static final String CHECKOUT_DECLINED_REQUEST = """
            {
              "cartId": "5a7ed441-567e-4f63-825e-dbdf07e640d6",
              "paymentMethod": {
                "name": "Cartão de Crédito",
                "card": {"holderName": "Ana Souza", "number": "4111111111110002", "expiry": "12/35", "cvv": "123"}
              }
            }""";

    static final String CHECKOUT_PIX_REQUEST = """
            {"cartId": "5a7ed441-567e-4f63-825e-dbdf07e640d6", "paymentMethod": {"name": "Pix"}}""";

    static final String CHECKOUT_CARD_RESPONSE = """
            {
              "cartId": "5a7ed441-567e-4f63-825e-dbdf07e640d6",
              "status": "APROVADO",
              "paidAt": "2026-10-14T16:00:00Z",
              "payment": {"method": "Cartão de Crédito", "lastDigits": "4242", "amount": 39.90, "currency": "BRL"},
              "products": [{"slug": "premium", "name": "Plano Premium", "price": 39.90}]
            }""";

    static final String CHECKOUT_PIX_RESPONSE = """
            {
              "cartId": "5a7ed441-567e-4f63-825e-dbdf07e640d6",
              "status": "APROVADO",
              "paidAt": "2026-10-14T16:00:00Z",
              "payment": {"method": "Pix", "pixCode": "PIX-4D8A3401", "amount": 39.90, "currency": "BRL"},
              "products": [{"slug": "premium", "name": "Plano Premium", "price": 39.90}]
            }""";

    static final String PAYMENT_DECLINED = """
            {
              "displayType": "BLOCK",
              "errorCode": "PAYMENT_DECLINED",
              "cartId": "5a7ed441-567e-4f63-825e-dbdf07e640d6",
              "frontendError": {
                "code": "P-1001",
                "title": "Pagamento não aprovado",
                "description": "Não foi possível concluir o pagamento. Tente novamente ou escolha outro meio.",
                "primaryButton": {"label": "Tentar novamente", "url": "RETRY"},
                "secondaryButton": {"label": "Trocar meio de pagamento", "url": "/checkout/pagamento"}
              }
            }""";

    static final String INVALID_PAYMENT_METHOD = """
            {
              "displayType": "INTERVENTION",
              "errorCode": "INVALID_PAYMENT_METHOD",
              "cartId": "5a7ed441-567e-4f63-825e-dbdf07e640d6",
              "frontendError": {
                "code": "P-1002",
                "title": "Meio de pagamento indisponível",
                "description": "O meio de pagamento escolhido não está disponível para este carrinho. \
            Escolha outro meio.",
                "primaryButton": {"label": "Trocar meio de pagamento", "url": "/checkout/pagamento"},
                "secondaryButton": null
              }
            }""";

    static final String INVALID_PAYMENT_DATA = """
            {
              "displayType": "INTERVENTION",
              "errorCode": "INVALID_PAYMENT_DATA",
              "cartId": "5a7ed441-567e-4f63-825e-dbdf07e640d6",
              "frontendError": {
                "code": "P-1003",
                "title": "Dados de pagamento inválidos",
                "description": "Confira os dados informados para o meio de pagamento escolhido.",
                "primaryButton": {"label": "Corrigir dados", "url": "/checkout/pagamento"},
                "secondaryButton": null
              }
            }""";

    static final String CART_NOT_FOUND = """
            {
              "displayType": "BLOCK",
              "errorCode": "CART_NOT_FOUND",
              "cartId": "5a7ed441-567e-4f63-825e-dbdf07e640d6",
              "frontendError": {
                "code": "P-3001",
                "title": "Carrinho não encontrado",
                "description": "Não encontramos o seu carrinho. Comece o checkout novamente.",
                "primaryButton": {"label": "Recomeçar", "url": "/planos"},
                "secondaryButton": null
              }
            }""";

    static final String CART_ALREADY_PAID = """
            {
              "displayType": "BLOCK",
              "errorCode": "CART_ALREADY_PAID",
              "cartId": "5a7ed441-567e-4f63-825e-dbdf07e640d6",
              "frontendError": {
                "code": "P-3002",
                "title": "Pagamento já realizado",
                "description": "Este carrinho já foi pago. Nenhuma nova cobrança foi feita.",
                "primaryButton": {"label": "Ver minha assinatura", "url": "/checkout/sucesso"},
                "secondaryButton": null
              }
            }""";

    static final String PAYMENT_IN_PROGRESS = """
            {
              "displayType": "BLOCK",
              "errorCode": "PAYMENT_IN_PROGRESS",
              "cartId": "5a7ed441-567e-4f63-825e-dbdf07e640d6",
              "frontendError": {
                "code": "P-3003",
                "title": "Pagamento em processamento",
                "description": "Já existe um pagamento em andamento para este carrinho. Aguarde alguns instantes antes de tentar de novo.",
                "primaryButton": {"label": "Tentar novamente", "url": "RETRY"},
                "secondaryButton": null
              }
            }""";

    static final String PRODUCT_NOT_FOUND = """
            {
              "displayType": "BLOCK",
              "errorCode": "PRODUCT_NOT_FOUND",
              "cartId": null,
              "frontendError": {
                "code": "P-2001",
                "title": "Produto indisponível",
                "description": "Um dos produtos escolhidos não está disponível. Escolha outro plano.",
                "primaryButton": {"label": "Escolher plano", "url": "/planos"},
                "secondaryButton": null
              }
            }""";

    static final String DUPLICATE_PRODUCT = """
            {
              "displayType": "INTERVENTION",
              "errorCode": "DUPLICATE_PRODUCT",
              "cartId": null,
              "frontendError": {
                "code": "P-2002",
                "title": "Produto repetido",
                "description": "Cada plano só pode ser escolhido uma vez. Revise a sua seleção.",
                "primaryButton": {"label": "Revisar planos", "url": "/planos"},
                "secondaryButton": null
              }
            }""";

    static final String INVALID_REQUEST = """
            {
              "displayType": "INTERVENTION",
              "errorCode": "INVALID_REQUEST",
              "cartId": null,
              "frontendError": {
                "code": "P-0001",
                "title": "Requisição inválida",
                "description": "Alguns dados obrigatórios estão ausentes ou inválidos.",
                "primaryButton": {"label": "Voltar", "url": "/checkout/pagamento"},
                "secondaryButton": null
              }
            }""";

    static final String SERVICE_UNAVAILABLE = """
            {
              "displayType": "BLOCK",
              "errorCode": "SERVICE_UNAVAILABLE",
              "cartId": null,
              "frontendError": {
                "code": "P-9001",
                "title": "Serviço indisponível",
                "description": "Estamos com instabilidade no momento. Tente novamente em instantes.",
                "primaryButton": {"label": "Tentar novamente", "url": "RETRY"},
                "secondaryButton": null
              }
            }""";

    static final String INTERNAL_ERROR = """
            {
              "displayType": "BLOCK",
              "errorCode": "INTERNAL_ERROR",
              "cartId": null,
              "frontendError": {
                "code": "P-9002",
                "title": "Algo deu errado",
                "description": "Não foi possível concluir a operação. Se o problema continuar, fale com o suporte.",
                "primaryButton": {"label": "Voltar ao início", "url": "/planos"},
                "secondaryButton": null
              }
            }""";

    private OpenApiExamples() {
    }
}
