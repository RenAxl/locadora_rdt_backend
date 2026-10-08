package com.locadora_rdt_backend.modules.rentals.rental.constants;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class RentalConstants {

    // Status
    public static final String STATUS_RENTED = "RENTED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_RESERVED = "RESERVED";
    public static final List<String> ACTIVE_UNIT_STATUSES = List.of("RESERVED", "DELIVERED");

    // Valores financeiros
    public static final int MONEY_SCALE = 2;
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    public static final BigDecimal PIX_AND_BANK_SLIP_DISCOUNT = new BigDecimal("0.05");
    public static final String PAYMENT_PIX = "pix";
    public static final String PAYMENT_BANK_SLIP = "boleto banc";

    // Filtros e identificação
    public static final long FILTER_ID_DISABLED = -1L;
    public static final Instant FILTER_FIRST_DATE = Instant.parse("1900-01-01T00:00:00Z");
    public static final Instant FILTER_LAST_DATE = Instant.parse("2999-12-31T23:59:59Z");
    public static final String EMPTY_TEXT = "";
    public static final String RENTAL_NUMBER_PREFIX = "LOC-";
    public static final DateTimeFormatter RENTAL_NUMBER_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");



    // Documentos
    public static final Locale BRAZIL_LOCALE = new Locale("pt", "BR");
    public static final DateTimeFormatter DOCUMENT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final Color DOCUMENT_DARK_BLUE = new Color(13, 42, 77);

    // Mensagens e motivos
    public static final String RESERVATION_SUCCESS = "Locação reservada com sucesso. "
            + "Uma mensagem com os detalhes da locação foi enviada para o celular do cliente. "
            + "Retire os itens na loja. O envio por aplicativo, como Uber, pode ser solicitado à loja.";
    public static final String CHECKOUT_SUCCESS = "Locação finalizada com sucesso. "
            + "Uma mensagem com os detalhes da locação foi enviada para o celular do cliente.";
    public static final String RESERVATION_REASON = "Reserva realizada pela locação.";
    public static final String RETURN_REASON = "Devolução realizada na baixa da locação.";
    public static final String CANCELLATION_REASON = "Cancelamento realizado pela exclusão da locação.";
    public static final String RENTAL_CREATED_HISTORY = "Locação realizada e unidades reservadas.";
    public static final String RENTAL_DELIVERED_HISTORY =
            "Locação baixada e unidades devolvidas ao estoque.";

    // Validações
    public static final int MIN_ITEM_QUANTITY = 1;
    public static final String FIELD_REQUIRED = "Campo requerido";

    // Mensagens de erro
    public static final String AUTHENTICATED_CUSTOMER_NOT_FOUND =
            "O usuário autenticado não possui um cliente cadastrado com o mesmo e-mail.";
    public static final String DOCUMENT_REQUIRES_DELIVERED_RENTAL =
            "Documento disponível apenas para locações entregues.";
    public static final String RENTAL_NOT_FOUND = "Locação não encontrada.";
    public static final String ITEM_NOT_FOUND = "Item não encontrado.";
    public static final String RENTAL_TYPE_NOT_FOUND = "Tipo de locação não encontrado.";
    public static final String PAYMENT_METHOD_NOT_FOUND = "Forma de pagamento não encontrada.";
    public static final String ACTIVE_ITEM_REQUIRED = "Somente itens ativos podem ser adicionados.";
    public static final String ACTIVE_RENTAL_TYPE_REQUIRED = "O tipo de locação deve estar ativo.";
    public static final String ACTIVE_CUSTOMER_REQUIRED = "O cliente do usuário autenticado deve estar ativo.";
    public static final String RENTED_RENTAL_REQUIRED = "A locação não está alugada.";
    public static final String DELIVERY_RENTED_RENTAL_REQUIRED =
            "Somente uma locação alugada pode ser entregue.";
    public static final String AT_LEAST_ONE_ITEM_REQUIRED = "Adicione pelo menos um item.";
    public static final String STOCK_BALANCE_NOT_FOUND = "O item não possui saldo de estoque cadastrado.";
    public static final String RECEIPT_GENERATION_ERROR = "Erro ao gerar recibo da locação.";
    public static final String FISCAL_COUPON_GENERATION_ERROR = "Erro ao gerar cupom fiscal da locação.";
    public static final String AUTHENTICATED_USER_NOT_FOUND = "Usuário autenticado não encontrado.";
    public static final String USER_IS_NOT_CUSTOMER_SUFFIX = " não é um Cliente da Locadora RDT.";
    public static final String RETURN_FORECAST_DATE_INVALID =
            "A previsão de devolução deve ser posterior à data inicial.";
    public static final String NEGATIVE_RENTAL_TOTAL = "O total da locação não pode ser negativo.";
    public static final String INVALID_RENTAL_DAYS =
            "A quantidade de dias da locação deve ser maior que zero.";
    public static final String DUPLICATE_RENTAL_ITEM = "Um item não pode aparecer duas vezes.";
    public static final String INVALID_ITEM_QUANTITY = "A quantidade deve ser maior que zero.";
    public static final String NEGATIVE_ITEM_SUBTOTAL = "O subtotal do item não pode ser negativo.";
    public static final String ITEM_QUANTITY_UNAVAILABLE_PREFIX = "Quantidade indisponível para o item ";
    public static final String LINKED_UNITS_QUANTITY_INVALID =
            "A quantidade de unidades vinculadas não corresponde à quantidade solicitada.";
    public static final String ALL_UNITS_MUST_BE_RESERVED =
            "Todas as unidades devem estar reservadas antes de iniciar a locação.";
    public static final String UNIT_MESSAGE_PREFIX = "A unidade ";
    public static final String UNIT_NOT_RESERVED_SUFFIX = " não está reservada.";
    public static final String UNIT_NOT_FROM_RENTAL_ITEM =
            "A unidade selecionada não pertence ao item da locação.";
    public static final String UNIT_NOT_AVAILABLE_SUFFIX = " não está disponível.";
    public static final String UNIT_ALREADY_LINKED_SUFFIX = " já está vinculada a uma locação ativa.";
    public static final String RESERVED_UNITS_QUANTITY_INVALID =
            "A quantidade de unidades reservadas não corresponde aos itens da locação.";

    private RentalConstants() {
    }
}
