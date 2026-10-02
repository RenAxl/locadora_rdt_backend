package com.locadora_rdt_backend.modules.financial.receivables.service;

import com.locadora_rdt_backend.modules.financial.receivables.constants.ReceivableConstants;
import com.locadora_rdt_backend.modules.financial.receivables.model.Receivable;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;


@Service
public class ReceivableDocumentPdfService {

    private final ReceivableCalculationService calculationService;

    public ReceivableDocumentPdfService(ReceivableCalculationService calculationService) {
        this.calculationService = calculationService;
    }

    public byte[] buildReceiptPdf(Receivable receivable) throws DocumentException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 34, 34, 28, 28);
        PdfWriter.getInstance(document, output);
        document.open();

        PdfPTable receiptBox = new PdfPTable(1);
        receiptBox.setWidthPercentage(100);

        PdfPCell box = new PdfPCell();
        box.setBorder(Rectangle.BOX);
        box.setBorderWidth(0.8f);
        box.setPadding(12f);

        box.addElement(buildReceiptHeader(receivable));

        Paragraph number = new Paragraph();
        number.setSpacingBefore(18f);
        number.add(new Chunk("Número: ", font(11, Font.BOLD, Color.BLACK)));
        number.add(new Chunk(buildReceiptNumber(receivable), font(11, Font.NORMAL, Color.BLACK)));
        box.addElement(number);

        Paragraph body = new Paragraph();
        body.setSpacingBefore(24f);
        body.setLeading(18f);
        body.add(new Chunk("Recebi(emos) de ", font(11, Font.NORMAL, Color.BLACK)));
        body.add(new Chunk(getCustomerName(receivable), font(11, Font.BOLD, Color.BLACK)));
        body.add(new Chunk(" a quantia de ", font(11, Font.NORMAL, Color.BLACK)));
        body.add(new Chunk(formatCurrency(calculationService.getReceiptAmount(receivable)), font(11, Font.BOLD, Color.BLACK)));
        body.add(new Chunk(" reais na data ", font(11, Font.NORMAL, Color.BLACK)));
        body.add(new Chunk(formatDate(receivable.getPaymentDate()), font(11, Font.BOLD, Color.BLACK)));
        body.add(new Chunk(" correspondente a(o) ", font(11, Font.NORMAL, Color.BLACK)));
        body.add(new Chunk(nullToDash(receivable.getDescription()), font(11, Font.NORMAL, Color.BLACK)));
        body.add(new Chunk(".", font(11, Font.NORMAL, Color.BLACK)));
        box.addElement(body);

        box.addElement(buildReceiptDetails(receivable));

        Paragraph simulatedSignature = new Paragraph("RDT Financeiro", font(24, Font.ITALIC, Color.DARK_GRAY));
        simulatedSignature.setAlignment(Element.ALIGN_CENTER);
        simulatedSignature.setSpacingBefore(28f);
        box.addElement(simulatedSignature);

        Paragraph signature = new Paragraph("____________________________________________", font(12, Font.NORMAL, Color.DARK_GRAY));
        signature.setAlignment(Element.ALIGN_CENTER);
        signature.setSpacingBefore(-8f);
        box.addElement(signature);

        Paragraph signatureLabel = new Paragraph("(ASSINATURA DO RESPONSÁVEL)", font(9, Font.BOLD, Color.BLACK));
        signatureLabel.setAlignment(Element.ALIGN_CENTER);
        signatureLabel.setSpacingBefore(2f);
        box.addElement(signatureLabel);

        Paragraph footer = new Paragraph("LOCADORA RDT", font(9, Font.NORMAL, Color.BLACK));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(18f);
        box.addElement(footer);

        receiptBox.addCell(box);
        document.add(receiptBox);
        document.close();

        return output.toByteArray();
    }

    public byte[] buildFiscalCouponPdf(Receivable receivable) throws DocumentException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(new Rectangle(226f, 620f), 10f, 10f, 12f, 12f);
        PdfWriter.getInstance(document, output);
        document.open();

        addCouponLine(document, "LOCADORA RDT", 10f, Font.BOLD, Element.ALIGN_CENTER);
        addCouponLine(document, "CUPOM FISCAL", 9f, Font.BOLD, Element.ALIGN_CENTER);
        addCouponLine(document, "Documento para conferencia e impressao", 7f, Font.NORMAL, Element.ALIGN_CENTER);
        addCouponSeparator(document);

        addCouponLine(document, "Cupom: " + buildReceiptNumber(receivable), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponLine(document, "Conta: #" + receivable.getId(), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponLine(document, "Emissao: " + formatDate(LocalDate.now()), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponLine(document, "Pagamento: " + formatDate(receivable.getPaymentDate()), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponLine(document, "Cliente: " + getCustomerName(receivable), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponLine(document, "CPF: " + getCustomerCpf(receivable), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponSeparator(document);

        addCouponLine(document, "ITEM DESCRICAO", 8f, Font.BOLD, Element.ALIGN_LEFT);
        addCouponLine(document, "001 " + limitCouponText(nullToDash(receivable.getDescription()), 28), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponLine(document, "Valor original: " + formatCurrency(receivable.getAmount()), 8f, Font.NORMAL, Element.ALIGN_RIGHT);

        BigDecimal fees = calculationService.valueOrZero(receivable.getFee())
                .add(calculationService.valueOrZero(receivable.getLateFee()))
                .add(calculationService.valueOrZero(receivable.getLateInterest()));

        if (fees.compareTo(ReceivableConstants.ZERO) > 0) {
            addCouponLine(document, "Acrescimos: " + formatCurrency(fees), 8f, Font.NORMAL, Element.ALIGN_RIGHT);
        }

        if (calculationService.valueOrZero(receivable.getDiscount()).compareTo(ReceivableConstants.ZERO) > 0) {
            addCouponLine(document, "Desconto: " + formatCurrency(receivable.getDiscount()), 8f, Font.NORMAL, Element.ALIGN_RIGHT);
        }

        addCouponSeparator(document);
        addCouponLine(document, "TOTAL " + formatCurrency(calculationService.getReceiptAmount(receivable)), 10f, Font.BOLD, Element.ALIGN_RIGHT);
        addCouponLine(document, "Forma: " + getPaymentMethodName(receivable), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponLine(document, "Recebido por: " + getPaidByName(receivable), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponSeparator(document);
        addCouponLine(document, "Obrigado pela preferencia", 8f, Font.BOLD, Element.ALIGN_CENTER);
        addCouponLine(document, "LOCADORA RDT", 8f, Font.NORMAL, Element.ALIGN_CENTER);

        document.close();
        return output.toByteArray();
    }

    private PdfPTable buildReceiptHeader(Receivable receivable) throws DocumentException {
        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{2.2f, 3.2f, 2.2f});

        PdfPCell logo = noBorderCell("LOCADORA RDT", font(14, Font.BOLD, ReceivableConstants.DARK_BLUE));
        logo.setHorizontalAlignment(Element.ALIGN_LEFT);
        header.addCell(logo);

        PdfPCell title = noBorderCell("RECIBO DE PAGAMENTO", font(16, Font.BOLD, Color.BLACK));
        title.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(title);

        PdfPCell amount = noBorderCell("VALOR " + formatCurrency(calculationService.getReceiptAmount(receivable)), font(13, Font.BOLD, Color.BLACK));
        amount.setHorizontalAlignment(Element.ALIGN_RIGHT);
        header.addCell(amount);

        return header;
    }

    private PdfPTable buildReceiptDetails(Receivable receivable) throws DocumentException {
        PdfPTable details = new PdfPTable(4);
        details.setWidthPercentage(100);
        details.setWidths(new float[]{1.4f, 2.6f, 1.4f, 2.6f});
        details.setSpacingBefore(22f);

        addDetail(details, "Conta", "#" + receivable.getId());
        addDetail(details, "Status", "Pago");
        addDetail(details, "Vencimento", formatDate(receivable.getDueDate()));
        addDetail(details, "Pagamento", formatDate(receivable.getPaymentDate()));
        String paymentMethodName = ReceivableConstants.EMPTY_VALUE;
        if (receivable.getPaymentMethod() != null) {
            paymentMethodName = nullToDash(receivable.getPaymentMethod().getName());
        }

        String paymentFrequencyName = ReceivableConstants.EMPTY_VALUE;
        if (receivable.getPaymentFrequency() != null) {
            paymentFrequencyName = nullToDash(receivable.getPaymentFrequency().getFrequency());
        }

        BigDecimal subtotal = receivable.getAmount();
        if (calculationService.valueOrZero(receivable.getSubtotal()).compareTo(ReceivableConstants.ZERO) > 0) {
            subtotal = receivable.getSubtotal();
        }

        String paidByName = ReceivableConstants.EMPTY_VALUE;
        if (receivable.getPaidBy() != null) {
            paidByName = nullToDash(receivable.getPaidBy().getName());
        }

        addDetail(details, "Forma Pgto", paymentMethodName);
        addDetail(details, "Frequência", paymentFrequencyName);
        addDetail(details, "Subtotal", formatCurrency(subtotal));
        addDetail(details, "Multa/Juros", formatCurrency(calculationService.valueOrZero(receivable.getLateFee()).add(calculationService.valueOrZero(receivable.getLateInterest()))));
        addDetail(details, "Desconto", formatCurrency(receivable.getDiscount()));
        addDetail(details, "Valor Pago", formatCurrency(calculationService.getReceiptAmount(receivable)));
        addDetail(details, "Recebido por", paidByName);
        addDetail(details, "Emitido em", formatDate(LocalDate.now()));

        return details;
    }

    private void addDetail(PdfPTable table, String label, String value) {
        PdfPCell labelCell = detailCell(label + ":", font(9, Font.BOLD, ReceivableConstants.DARK_BLUE));
        labelCell.setBackgroundColor(new Color(245, 248, 252));
        table.addCell(labelCell);
        table.addCell(detailCell(nullToDash(value), font(9, Font.NORMAL, Color.BLACK)));
    }

    private PdfPCell detailCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.3f);
        cell.setPadding(5f);
        return cell;
    }

    private PdfPCell noBorderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0);
        return cell;
    }

    private void addCouponLine(Document document, String text, float size, int style, int alignment) throws DocumentException {
        Paragraph line = new Paragraph(nullToDash(text), new Font(Font.COURIER, size, style, Color.BLACK));
        line.setAlignment(alignment);
        line.setLeading(size + 2f);
        line.setSpacingAfter(2f);
        document.add(line);
    }

    private void addCouponSeparator(Document document) throws DocumentException {
        addCouponLine(document, "--------------------------------", 8f, Font.NORMAL, Element.ALIGN_CENTER);
    }

    private String limitCouponText(String value, int maxLength) {
        String normalized = nullToDash(value);

        if (normalized.length() <= maxLength) {
            return normalized;
        }

        return normalized.substring(0, maxLength - 3) + "...";
    }

    private String buildReceiptNumber(Receivable receivable) {
        LocalDate date;

        if (receivable.getPaymentDate() == null) {
            date = LocalDate.now();
        } else {
            date = receivable.getPaymentDate();
        }

        return date.getYear() + "/" + receivable.getId();
    }

    private String getCustomerName(Receivable receivable) {
        if (receivable.getCustomer() == null) {
            return ReceivableConstants.EMPTY_VALUE;
        }

        return nullToDash(receivable.getCustomer().getName());
    }

    private String getCustomerCpf(Receivable receivable) {
        if (receivable.getCustomer() == null) {
            return ReceivableConstants.EMPTY_VALUE;
        }

        return nullToDash(receivable.getCustomer().getCpf());
    }

    private String getPaymentMethodName(Receivable receivable) {
        if (receivable.getPaymentMethod() == null) {
            return ReceivableConstants.EMPTY_VALUE;
        }

        return nullToDash(receivable.getPaymentMethod().getName());
    }

    private String getPaidByName(Receivable receivable) {
        if (receivable.getPaidBy() == null) {
            return ReceivableConstants.EMPTY_VALUE;
        }

        return nullToDash(receivable.getPaidBy().getName());
    }

    private String formatCurrency(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(ReceivableConstants.BRAZIL).format(calculationService.valueOrZero(value));
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return ReceivableConstants.EMPTY_VALUE;
        }

        return date.format(ReceivableConstants.DATE_FORMATTER);
    }

    private String nullToDash(String value) {
        if (value == null) {
            return ReceivableConstants.EMPTY_VALUE;
        }

        if (value.trim().isEmpty()) {
            return ReceivableConstants.EMPTY_VALUE;
        }

        return value.trim();
    }

    private Font font(float size, int style, Color color) {
        return new Font(Font.TIMES_ROMAN, size, style, color);
    }
}
