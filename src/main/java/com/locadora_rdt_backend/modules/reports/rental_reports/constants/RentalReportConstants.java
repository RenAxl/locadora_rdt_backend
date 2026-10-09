package com.locadora_rdt_backend.modules.reports.rental_reports.constants;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class RentalReportConstants {

    // Filtros
    public static final String STATUS_ALL = "ALL";
    public static final String PERIOD_REGISTRATION_DATE = "REGISTRATION_DATE";
    public static final String PERIOD_RENTAL_START_DATE = "RENTAL_START_DATE";
    public static final String PERIOD_RETURN_FORECAST_DATE = "RETURN_FORECAST_DATE";
    public static final String PERIOD_EFFECTIVE_RETURN_DATE = "EFFECTIVE_RETURN_DATE";
    public static final long FILTER_ID_DISABLED = -1L;
    public static final BigDecimal FILTER_AMOUNT_DISABLED = BigDecimal.valueOf(-1);
    public static final LocalDate FILTER_DATE_DISABLED = LocalDate.of(1970, 1, 1);

    // Arquivos
    public static final String PDF_FORMAT = "pdf";
    public static final String XLSX_FORMAT = "xlsx";
    public static final String PDF_CONTENT_TYPE = "application/pdf";
    public static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final String REPORT_TYPE_REQUIRED = "Tipo do relatório não informado.";
    public static final String INVALID_REPORT_TYPE = "Tipo de relatório inválido.";
    public static final String FORMAT_REQUIRED = "Formato do relatório não informado.";
    public static final String INVALID_FORMAT = "Formato de relatório inválido.";
    public static final String PDF_GENERATION_ERROR = "Erro ao gerar relatório PDF.";
    public static final String FILE_EXTENSION_SEPARATOR = ".";

    // Títulos e colunas
    public static final String RENTALS_REPORT_TITLE = "Relatório de Locações";
    public static final String CUSTOMER_SUMMARY_REPORT_TITLE = "Locações por Cliente";
    public static final String ANNUAL_SUMMARY_REPORT_TITLE = "Resumo Anual de Locações ";
    public static final List<String> RENTAL_COLUMNS = List.of(
            "Número", "Cliente", "Tipo", "Situação", "Data do registro", "Início da locação",
            "Previsão de devolução", "Devolução efetiva", "Forma de pagamento", "Pago", "Valor registrado"
    );
    public static final List<String> SUMMARY_COLUMNS = List.of(
            "Cliente", "Quantidade", "Valor registrado", "Valor pago"
    );
    public static final List<String> ANNUAL_COLUMNS = List.of("Mês", "Valor registrado", "Valor pago");
    public static final List<String> SHORT_MONTH_NAMES = List.of(
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    );
    public static final List<String> MONTH_NAMES = List.of(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    );

    private RentalReportConstants() {
    }
}
