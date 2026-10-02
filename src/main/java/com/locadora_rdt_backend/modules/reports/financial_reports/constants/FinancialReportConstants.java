package com.locadora_rdt_backend.modules.reports.financial_reports.constants;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class FinancialReportConstants {

    // Valores financeiros e períodos
    public static final BigDecimal ZERO = BigDecimal.ZERO;
    public static final int FIRST_MONTH = 1;
    public static final int LAST_MONTH = 12;
    public static final int FIRST_DAY_OF_MONTH = 1;
    public static final int LAST_DAY_OF_YEAR = 31;

    // Filtros
    public static final String STATUS_ALL = "ALL";
    public static final String STATUS_PAID = "PAID";
    public static final String PERIOD_DUE_DATE = "DUE_DATE";
    public static final String PERIOD_PAYMENT_DATE = "PAYMENT_DATE";
    public static final String PERIOD_CREATED_DATE = "CREATED_DATE";

    public static final long FILTER_ID_DISABLED = -1L;
    public static final BigDecimal FILTER_AMOUNT_DISABLED = BigDecimal.valueOf(-1);
    public static final LocalDate FILTER_DATE_DISABLED = LocalDate.of(1970, 1, 1);

    // Arquivos de relatório
    public static final String PDF_FORMAT = "pdf";
    public static final String XLSX_FORMAT = "xlsx";
    public static final String PDF_CONTENT_TYPE = "application/pdf";
    public static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String DATE_PATTERN = "dd/MM/yyyy";

    // Mensagens de erro
    public static final String REPORT_TYPE_REQUIRED = "Tipo do relatório não informado.";
    public static final String INVALID_REPORT_TYPE = "Tipo de relatório inválido.";
    public static final String FORMAT_REQUIRED = "Formato do relatório não informado.";
    public static final String INVALID_FORMAT = "Formato de relatório inválido.";
    public static final String PDF_GENERATION_ERROR = "Erro ao gerar relatório PDF.";

    // Nomes de arquivos
    public static final String FILE_EXTENSION_SEPARATOR = ".";

    // Títulos e rótulos
    public static final String RECEIVABLES_REPORT_TITLE = "Relatório de Contas a Receber";
    public static final String PAYABLES_REPORT_TITLE = "Relatório de Contas a Pagar";
    public static final String FINANCIAL_REPORT_TITLE = "Relatório Financeiro";
    public static final String CUSTOMER_SUMMARY_REPORT_TITLE = "Relatório Sintético por Cliente";
    public static final String SUPPLIER_SUMMARY_REPORT_TITLE = "Relatório Sintético por Fornecedor";
    public static final String EMPLOYEE_SUMMARY_REPORT_TITLE = "Relatório Sintético por Funcionário";
    public static final String ANNUAL_BALANCE_REPORT_TITLE = "Balanço Anual ";
    public static final String CUSTOMER_LABEL = "Cliente";
    public static final String SUPPLIER_LABEL = "Fornecedor";
    public static final String EMPLOYEE_LABEL = "Funcionário";
    public static final String TOTAL_LABEL = "Total";
    public static final String YEAR_TOTAL_LABEL = "Total do ano";
    public static final String REVENUE_LABEL = "Receitas";
    public static final String EXPENSE_LABEL = "Despesas";
    public static final String RECEIVED_TOTAL_LABEL = "Total recebido";
    public static final String PAID_TOTAL_LABEL = "Total pago";
    public static final String BALANCE_LABEL = "Saldo";
    public static final String CUSTOMER_NOT_INFORMED = "Sem cliente";
    public static final String SUPPLIER_NOT_INFORMED = "Sem fornecedor";
    public static final String EMPLOYEE_NOT_INFORMED = "Sem funcionário";

    // Status exibidos no relatório
    public static final String CANCELED_STATUS_LABEL = "Cancelada";
    public static final String PAID_STATUS_LABEL = "Paga";
    public static final String PARTIALLY_PAID_STATUS_LABEL = "Pago parcialmente";
    public static final String OVERDUE_STATUS_LABEL = "Vencida";
    public static final String OPEN_STATUS_LABEL = "Em aberto";

    // Colunas
    public static final List<String> RECEIVABLE_COLUMNS = List.of(
            "ID", "Descrição", CUSTOMER_LABEL, "Vencimento", "Pagamento", "Valor", "Status"
    );
    public static final List<String> PAYABLE_COLUMNS = List.of(
            "ID", "Descrição", SUPPLIER_LABEL, EMPLOYEE_LABEL, "Vencimento", "Pagamento", "Valor", "Status"
    );
    public static final List<String> FINANCIAL_COLUMNS = List.of("Indicador", "Valor");
    public static final List<String> SUMMARY_COLUMNS = List.of(
            "Quantidade", TOTAL_LABEL, "Total pago/recebido", OPEN_STATUS_LABEL
    );
    public static final List<String> ANNUAL_BALANCE_COLUMNS = List.of(
            "Mês", RECEIVED_TOTAL_LABEL, PAID_TOTAL_LABEL, BALANCE_LABEL
    );

    // Meses
    public static final List<String> SHORT_MONTH_NAMES = List.of(
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    );
    public static final List<String> MONTH_NAMES = List.of(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    );

    private FinancialReportConstants() {
    }

}
