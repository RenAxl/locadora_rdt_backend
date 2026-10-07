package com.locadora_rdt_backend.modules.reports.stock_reports.constants;

import java.time.LocalDate;
import java.util.List;

public final class StockReportConstants {
    public static final String ALL = "ALL";
    public static final LocalDate DISABLED_DATE = LocalDate.of(1970, 1, 1);
    public static final String PDF_CONTENT_TYPE = "application/pdf";
    public static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String INVALID_REPORT_TYPE = "Tipo de relatório de estoque inválido.";
    public static final String INVALID_FORMAT = "Informe um formato válido: PDF ou XLSX.";
    public static final String INVALID_PERIOD = "A data inicial não pode ser maior que a data final.";
    public static final String INVALID_FILTER = "Verifique os filtros do relatório de estoque.";
    public static final String PDF_GENERATION_ERROR = "Erro ao gerar o relatório de estoque em PDF.";
    public static final List<String> BALANCE_COLUMNS = List.of(
            "Item", "Categoria", "Total", "Disponíveis", "Indisponíveis", "Manutenção",
            "Danificados", "Não localizados", "Mínimo", "Abaixo do mínimo");
    public static final List<String> UNIT_COLUMNS = List.of(
            "Item", "Categoria", "Código patrimonial", "Situação", "Conservação", "Compra", "Ativa");
    public static final List<String> MOVEMENT_COLUMNS = List.of(
            "Data", "Item", "Código patrimonial", "Tipo", "Quantidade / total ajustado",
            "Motivo", "Responsável", "Situação anterior", "Nova situação");

    private StockReportConstants() {
    }
}
