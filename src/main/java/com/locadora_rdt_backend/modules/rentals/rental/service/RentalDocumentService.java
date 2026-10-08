package com.locadora_rdt_backend.modules.rentals.rental.service;

import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.rentals.rental.constants.RentalConstants;
import com.locadora_rdt_backend.modules.rentals.rental.model.Rental;
import com.locadora_rdt_backend.modules.rentals.rental.model.RentalItem;
import com.locadora_rdt_backend.modules.rentals.rental.repository.RentalItemRepository;
import com.locadora_rdt_backend.modules.rentals.rental.repository.RentalRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class RentalDocumentService {

    private final RentalRepository repository;
    private final RentalItemRepository itemRepository;

    public RentalDocumentService(
            RentalRepository repository,
            RentalItemRepository itemRepository
    ) {
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public byte[] receipt(Long id) {

        Rental rental = findDeliveredRental(id);
        List<RentalItem> items = itemRepository.findByRentalIdOrderById(id);

        try {

            byte[] document = buildReceiptPdf(rental, items);

            return document;

        } catch (DocumentException e) {

            throw new IllegalStateException(RentalConstants.RECEIPT_GENERATION_ERROR, e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] fiscalCoupon(Long id) {

        Rental rental = findDeliveredRental(id);
        List<RentalItem> items = itemRepository.findByRentalIdOrderById(id);

        try {

            byte[] document = buildFiscalCouponPdf(rental, items);

            return document;

        } catch (DocumentException e) {

            throw new IllegalStateException(RentalConstants.FISCAL_COUPON_GENERATION_ERROR, e);
        }
    }

    private Rental findDeliveredRental(Long id) {

        Optional<Rental> rentalOptional = repository.findById(id);

        if (!rentalOptional.isPresent()) {
            throw new ResourceNotFoundException(RentalConstants.RENTAL_NOT_FOUND);
        }

        Rental rental = rentalOptional.get();

        if (!RentalConstants.STATUS_DELIVERED.equals(rental.getStatus())) {
            throw new IllegalArgumentException(RentalConstants.DOCUMENT_REQUIRES_DELIVERED_RENTAL);
        }

        return rental;
    }

    private byte[] buildReceiptPdf(Rental rental, List<RentalItem> items) throws DocumentException {

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

        box.addElement(buildReceiptHeader(rental));

        Paragraph number = new Paragraph();
        number.setSpacingBefore(18f);
        number.add(new Chunk("Número: ", font(11, Font.BOLD, Color.BLACK)));
        number.add(new Chunk(buildReceiptNumber(rental), font(11, Font.NORMAL, Color.BLACK)));
        box.addElement(number);

        Paragraph body = new Paragraph();
        body.setSpacingBefore(24f);
        body.setLeading(18f);
        body.add(new Chunk("Recebi(emos) de ", font(11, Font.NORMAL, Color.BLACK)));
        body.add(new Chunk(getCustomerName(rental), font(11, Font.BOLD, Color.BLACK)));
        body.add(new Chunk(" a quantia de ", font(11, Font.NORMAL, Color.BLACK)));
        body.add(new Chunk(formatCurrency(calculateTotal(rental)), font(11, Font.BOLD, Color.BLACK)));
        body.add(new Chunk(" referente à locação ", font(11, Font.NORMAL, Color.BLACK)));
        body.add(new Chunk(nullToDash(rental.getRentalNumber()), font(11, Font.BOLD, Color.BLACK)));
        body.add(new Chunk(".", font(11, Font.NORMAL, Color.BLACK)));
        box.addElement(body);

        box.addElement(buildReceiptDetails(rental, items));

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

    private byte[] buildFiscalCouponPdf(Rental rental, List<RentalItem> items) throws DocumentException {

        int itemCount = items == null ? 0 : items.size();
        Document document = new Document(new Rectangle(226f, 520f + (itemCount * 35f)), 10f, 10f, 12f, 12f);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, output);
        document.open();

        addCouponLine(document, "LOCADORA RDT", 10f, Font.BOLD, Element.ALIGN_CENTER);
        addCouponLine(document, "CUPOM FISCAL", 9f, Font.BOLD, Element.ALIGN_CENTER);
        addCouponLine(document, "Documento para conferencia e impressao", 7f, Font.NORMAL, Element.ALIGN_CENTER);
        addCouponSeparator(document);
        addCouponLine(document, "Cupom: " + buildReceiptNumber(rental), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponLine(document, "Locacao: " + nullToDash(rental.getRentalNumber()), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponLine(document, "Emissao: " + formatDate(today()), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponLine(document, "Devolução efetiva: " + formatDate(rental.getEffectiveReturnDate()), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponLine(document, "Cliente: " + getCustomerName(rental), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponLine(document, "CPF: " + getCustomerCpf(rental), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponSeparator(document);
        addCouponLine(document, "ITEM DESCRICAO", 8f, Font.BOLD, Element.ALIGN_LEFT);

        if (items != null) {
            int number = 1;
            for (RentalItem item : items) {
                String itemName = item.getItem() == null ? "-" : item.getItem().getName();
                addCouponLine(document, String.format("%03d %s", number, limitCouponText(itemName, 28)), 8f, Font.NORMAL, Element.ALIGN_LEFT);
                addCouponLine(document, item.getQuantity() + " x " + formatCurrency(item.getUnitPrice())
                        + " = " + formatCurrency(item.getSubtotal()), 8f, Font.NORMAL, Element.ALIGN_RIGHT);
                number++;
            }
        }

        addCouponSeparator(document);
        addCouponLine(document, "Valor: " + formatCurrency(rental.getTotalAmount()), 8f, Font.NORMAL, Element.ALIGN_RIGHT);
        if (valueOrZero(rental.getLateFee()).compareTo(RentalConstants.ZERO) > 0) {
            addCouponLine(document, "Multa: " + formatCurrency(rental.getLateFee()), 8f, Font.NORMAL, Element.ALIGN_RIGHT);
        }
        if (valueOrZero(rental.getDiscount()).compareTo(RentalConstants.ZERO) > 0) {
            addCouponLine(document, "Desconto: " + formatCurrency(rental.getDiscount()), 8f, Font.NORMAL, Element.ALIGN_RIGHT);
        }
        addCouponLine(document, "TOTAL " + formatCurrency(calculateTotal(rental)), 10f, Font.BOLD, Element.ALIGN_RIGHT);
        addCouponLine(document, "Forma: " + getPaymentMethodName(rental), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponLine(document, "Recebido por: " + nullToDash(rental.getUpdatedBy()), 8f, Font.NORMAL, Element.ALIGN_LEFT);
        addCouponSeparator(document);
        addCouponLine(document, "Obrigado pela preferencia", 8f, Font.BOLD, Element.ALIGN_CENTER);
        addCouponLine(document, "LOCADORA RDT", 8f, Font.NORMAL, Element.ALIGN_CENTER);

        document.close();
        return output.toByteArray();
    }

    private PdfPTable buildReceiptHeader(Rental rental) throws DocumentException {

        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{2.2f, 3.2f, 2.2f});
        PdfPCell logo = noBorderCell("LOCADORA RDT", font(14, Font.BOLD, RentalConstants.DOCUMENT_DARK_BLUE));
        logo.setHorizontalAlignment(Element.ALIGN_LEFT);
        header.addCell(logo);
        PdfPCell title = noBorderCell("RECIBO DE PAGAMENTO", font(16, Font.BOLD, Color.BLACK));
        title.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(title);
        PdfPCell amount = noBorderCell("VALOR " + formatCurrency(calculateTotal(rental)), font(13, Font.BOLD, Color.BLACK));
        amount.setHorizontalAlignment(Element.ALIGN_RIGHT);
        header.addCell(amount);
        return header;
    }

    private PdfPTable buildReceiptDetails(Rental rental, List<RentalItem> items) throws DocumentException {

        PdfPTable details = new PdfPTable(4);
        details.setWidthPercentage(100);
        details.setWidths(new float[]{1.4f, 2.6f, 1.4f, 2.6f});
        details.setSpacingBefore(22f);
        addDetail(details, "Locação", rental.getRentalNumber());
        addDetail(details, "Status", "Entregue");
        addDetail(details, "Início da locação", formatDate(rental.getRentalStartDate()));
        addDetail(details, "Devolução efetiva", formatDate(rental.getEffectiveReturnDate()));
        addDetail(details, "Forma Pgto", getPaymentMethodName(rental));
        addDetail(details, "Quantidade itens", String.valueOf(items == null ? 0 : items.size()));
        addDetail(details, "Valor", formatCurrency(rental.getTotalAmount()));
        addDetail(details, "Multa", formatCurrency(rental.getLateFee()));
        addDetail(details, "Desconto", formatCurrency(rental.getDiscount()));
        addDetail(details, "Valor Pago", formatCurrency(calculateTotal(rental)));
        addDetail(details, "Recebido por", rental.getUpdatedBy());
        addDetail(details, "Emitido em", formatDate(today()));
        return details;
    }

    private void addDetail(PdfPTable table, String label, String value) {

        PdfPCell labelCell = detailCell(label + ":", font(9, Font.BOLD, RentalConstants.DOCUMENT_DARK_BLUE));
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

    private String buildReceiptNumber(Rental rental) {

        LocalDate date = rental.getEffectiveReturnDate() == null
                ? today() : LocalDate.ofInstant(rental.getEffectiveReturnDate(), ZoneId.systemDefault());
        return date.getYear() + "/" + rental.getId();
    }

    private BigDecimal calculateTotal(Rental rental) {

        return valueOrZero(rental.getTotalAmount())
                .add(valueOrZero(rental.getLateFee()))
                .subtract(valueOrZero(rental.getDiscount()));
    }

    private String getCustomerName(Rental rental) {

        return rental.getCustomer() == null ? "-" : nullToDash(rental.getCustomer().getName());
    }

    private String getCustomerCpf(Rental rental) {

        return rental.getCustomer() == null ? "-" : nullToDash(rental.getCustomer().getCpf());
    }

    private String getPaymentMethodName(Rental rental) {

        return rental.getPaymentMethod() == null ? "-" : nullToDash(rental.getPaymentMethod().getName());
    }

    private String limitCouponText(String value, int maxLength) {

        String text = nullToDash(value);
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }

    private String formatCurrency(BigDecimal value) {

        return NumberFormat.getCurrencyInstance(RentalConstants.BRAZIL_LOCALE).format(valueOrZero(value));
    }

    private String formatDate(Instant date) {

        return date == null ? "-" : formatDate(LocalDate.ofInstant(date, ZoneId.systemDefault()));
    }

    private String formatDate(LocalDate date) {

        return date == null ? "-" : date.format(RentalConstants.DOCUMENT_DATE_FORMATTER);
    }

    private String nullToDash(String value) {

        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private Font font(float size, int style, Color color) {

        return new Font(Font.TIMES_ROMAN, size, style, color);
    }

    private BigDecimal valueOrZero(BigDecimal value) {

        return value == null ? RentalConstants.ZERO : value;
    }

    private LocalDate today() {

        return LocalDate.now();
    }
}
