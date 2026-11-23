package com.larissafalcao.bff_checkout.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI bffCheckoutOpenApi() {
        return new OpenAPI().info(new Info()
                .title("BFF de Checkout")
                .version("1.0.0")
                .description("""
                        BFF do checkout de assinatura: pré-checkout (monta o carrinho) e checkout (simula a cobrança).
                        Toda falha usa o mesmo objeto de erro (displayType, errorCode, cartId, frontendError).

                        Para simular: cartão terminado em 0002 é recusado; qualquer outro cartão válido é \
                        aprovado; Pix é enviado sem o objeto card."""));
    }
}
