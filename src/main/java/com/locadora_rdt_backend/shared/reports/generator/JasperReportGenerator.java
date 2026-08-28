package com.locadora_rdt_backend.shared.reports.generator;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JasperReportGenerator {

    public byte[] generateExcel(String title, List<String> columns, List<Map<String, ?>> rows) {
        try {
            JasperDesign design = createDesign(title, columns);
            JasperReport report = JasperCompileManager.compileReport(design);
            Map<String, Object> params = new HashMap<>();
            params.put("REPORT_TITLE", title);
            Collection<Map<String, ?>> data = rows;
            JasperPrint print = JasperFillManager.fillReport(report, params, new JRMapCollectionDataSource(data));

            return exportXlsx(print);
        } catch (JRException e) {
            throw new IllegalStateException("Erro ao gerar relatório.", e);
        }
    }

    private JasperDesign createDesign(String title, List<String> columns) throws JRException {
        JasperDesign design = new JasperDesign();
        design.setName(title.replace(" ", "_"));
        design.setPageWidth(842);
        design.setPageHeight(595);
        design.setColumnWidth(802);
        design.setLeftMargin(20);
        design.setRightMargin(20);
        design.setTopMargin(20);
        design.setBottomMargin(20);

        JRDesignStyle normalStyle = new JRDesignStyle();
        normalStyle.setName("Normal");
        normalStyle.setDefault(true);
        normalStyle.setFontName("SansSerif");
        normalStyle.setFontSize(9f);
        design.addStyle(normalStyle);

        JRDesignParameter titleParam = new JRDesignParameter();
        titleParam.setName("REPORT_TITLE");
        titleParam.setValueClass(String.class);
        design.addParameter(titleParam);

        for (int i = 0; i < columns.size(); i++) {
            JRDesignField field = new JRDesignField();
            field.setName("column" + i);
            field.setValueClass(String.class);
            design.addField(field);
        }

        design.setTitle(createTitleBand());
        design.setColumnHeader(createHeaderBand(columns));
        ((JRDesignSection) design.getDetailSection()).addBand(createDetailBand(columns.size()));

        return design;
    }

    private JRDesignBand createTitleBand() {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(42);

        JRDesignTextField title = new JRDesignTextField();
        title.setX(0);
        title.setY(0);
        title.setWidth(802);
        title.setHeight(30);
        title.setFontSize(18f);
        title.setBold(true);
        title.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        title.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        title.setExpression(new JRDesignExpression("$P{REPORT_TITLE}"));
        band.addElement(title);

        return band;
    }

    private JRDesignBand createHeaderBand(List<String> columns) {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(24);

        int width = 802 / columns.size();
        int lastWidth = 802 - (width * (columns.size() - 1));

        for (int i = 0; i < columns.size(); i++) {
            JRDesignStaticText text = new JRDesignStaticText();
            text.setX(i * width);
            text.setY(0);
            text.setWidth(i == columns.size() - 1 ? lastWidth : width);
            text.setHeight(22);
            text.setText(columns.get(i));
            text.setBold(true);
            text.setMode(ModeEnum.OPAQUE);
            text.setBackcolor(new Color(230, 230, 230));
            text.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
            text.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
            band.addElement(text);
        }

        return band;
    }

    private JRDesignBand createDetailBand(int columns) {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(22);

        int width = 802 / columns;
        int lastWidth = 802 - (width * (columns - 1));

        for (int i = 0; i < columns; i++) {
            JRDesignTextField text = new JRDesignTextField();
            text.setX(i * width);
            text.setY(0);
            text.setWidth(i == columns - 1 ? lastWidth : width);
            text.setHeight(20);
            text.setBlankWhenNull(true);
            text.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
            text.setExpression(new JRDesignExpression("$F{column" + i + "}"));
            band.addElement(text);
        }

        return band;
    }

    private byte[] exportXlsx(JasperPrint print) throws JRException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        JRXlsxExporter exporter = new JRXlsxExporter();
        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setDetectCellType(true);
        configuration.setOnePagePerSheet(false);
        configuration.setRemoveEmptySpaceBetweenRows(true);
        configuration.setWhitePageBackground(false);

        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
        exporter.setConfiguration(configuration);
        exporter.exportReport();

        return output.toByteArray();
    }
}
