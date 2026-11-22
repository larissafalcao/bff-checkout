package com.larissafalcao.bff_checkout.adapter.in.web.openapi;

import com.larissafalcao.bff_checkout.adapter.in.web.dto.CheckoutRequest;
import com.larissafalcao.bff_checkout.adapter.in.web.dto.CheckoutResponse;
import com.larissafalcao.bff_checkout.adapter.in.web.dto.PreCheckoutRequest;
import com.larissafalcao.bff_checkout.adapter.in.web.dto.PreCheckoutResponse;
import com.larissafalcao.bff_checkout.adapter.in.web.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.CART_ALREADY_PAID;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.CART_NOT_FOUND;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.CHECKOUT_CARD_REQUEST;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.CHECKOUT_CARD_RESPONSE;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.CHECKOUT_DECLINED_REQUEST;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.CHECKOUT_PIX_REQUEST;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.CHECKOUT_PIX_RESPONSE;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.DUPLICATE_PRODUCT;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.INTERNAL_ERROR;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.INVALID_PAYMENT_DATA;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.INVALID_PAYMENT_METHOD;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.INVALID_REQUEST;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.PAYMENT_DECLINED;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.PAYMENT_IN_PROGRESS;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.PRODUCT_NOT_FOUND;
import static com.larissafalcao.bff_checkout.adapter.in.web.openapi.OpenApiExamples.SERVICE_UNAVAILABLE;

@Tag(name = "Operações de checkout", description = "Fluxo de duas etapas: pré-checkout e checkout")
public interface OperationsApi {

    String JSON = "application/json";

    @Operation(summary = "Pré-checkout",
            description = "Consulta os serviços de produtos e de meios de pagamento e cria o carrinho "
                    + "para a tela de pagamento.")
    @ApiResponse(responseCode = "200", description = "Carrinho criado")
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou produto repetido",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class), examples = {
                    @ExampleObject(name = "DUPLICATE_PRODUCT", value = DUPLICATE_PRODUCT),
                    @ExampleObject(name = "INVALID_REQUEST", value = INVALID_REQUEST)}))
    @ApiResponse(responseCode = "404", description = "Produto inexistente no catálogo",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "PRODUCT_NOT_FOUND", value = PRODUCT_NOT_FOUND)))
    @ApiResponse(responseCode = "500", description = "Erro inesperado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "INTERNAL_ERROR", value = INTERNAL_ERROR)))
    @ApiResponse(responseCode = "503", description = "Serviço de produtos ou de meios de pagamento indisponível",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "SERVICE_UNAVAILABLE", value = SERVICE_UNAVAILABLE)))
    PreCheckoutResponse preCheckout(PreCheckoutRequest request);

    @Operation(summary = "Checkout",
            description = "Simula a cobrança do carrinho. Cartão terminado em 0002 é recusado. "
                    + "Pix não leva o objeto card.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = CheckoutRequest.class),
                            examples = {
                            @ExampleObject(name = "Cartão aprovado", value = CHECKOUT_CARD_REQUEST),
                            @ExampleObject(name = "Cartão recusado (final 0002)", value = CHECKOUT_DECLINED_REQUEST),
                            @ExampleObject(name = "Pix", value = CHECKOUT_PIX_REQUEST)})))
    @ApiResponse(responseCode = "200", description = "Pagamento aprovado. Cartão traz lastDigits; Pix traz pixCode.",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = CheckoutResponse.class), examples = {
                    @ExampleObject(name = "Cartão", value = CHECKOUT_CARD_RESPONSE),
                    @ExampleObject(name = "Pix", value = CHECKOUT_PIX_RESPONSE)}))
    @ApiResponse(responseCode = "400", description = "Dados de pagamento inválidos ou requisição malformada",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class), examples = {
                    @ExampleObject(name = "INVALID_PAYMENT_DATA", value = INVALID_PAYMENT_DATA),
                    @ExampleObject(name = "INVALID_REQUEST", value = INVALID_REQUEST)}))
    @ApiResponse(responseCode = "404", description = "Carrinho não criado no pré-checkout",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "CART_NOT_FOUND", value = CART_NOT_FOUND)))
    @ApiResponse(responseCode = "409", description = "Carrinho já pago ou com pagamento em andamento",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class), examples = {
                    @ExampleObject(name = "CART_ALREADY_PAID", value = CART_ALREADY_PAID),
                    @ExampleObject(name = "PAYMENT_IN_PROGRESS", value = PAYMENT_IN_PROGRESS)}))
    @ApiResponse(responseCode = "422",
            description = "Pagamento recusado ou meio de pagamento não oferecido para o carrinho",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class), examples = {
                    @ExampleObject(name = "PAYMENT_DECLINED", value = PAYMENT_DECLINED),
                    @ExampleObject(name = "INVALID_PAYMENT_METHOD", value = INVALID_PAYMENT_METHOD)}))
    @ApiResponse(responseCode = "500", description = "Erro inesperado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "INTERNAL_ERROR", value = INTERNAL_ERROR)))
    CheckoutResponse checkout(CheckoutRequest request);
}
