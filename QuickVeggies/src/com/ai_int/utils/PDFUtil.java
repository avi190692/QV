package com.ai_int.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceCmyk;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.Image;

import com.quickveggies.dao.DatabaseClient;
import com.quickveggies.entities.MoneyPaidRecd;
import com.quickveggies.entities.Buyer;
import com.quickveggies.entities.Expenditure;
import com.quickveggies.entities.Supplier;

public class PDFUtil {

    private static final String fileName = System.getProperty("java.io.tmpdir");

    private static final Color footRowColor = new DeviceRgb(114, 159, 207);
    private static final Color headRowColor = new DeviceCmyk(39, 30, 16, 39);

    public static void main(String[] args) {
        String[] columnHeads = new String[]{"Size", "Cases", "Rates", "Amount"};
        String[][] values = new String[5][4];
        int i = -1;
        values[++i] = columnHeads;
        values[++i] = new String[]{"Small", "12", "123", "14"};
        values[++i] = new String[]{"Big", "12", "123", "14"};
        values[++i] = new String[]{"", "APMC", "", "14"};
        values[++i] = new String[]{"", "Total Amount", "", "13"};
        //buildBuyerInvoicePdf("Buyer Invoice", "Pawan", "23", values);
        //prepareListPdf("Pawan", values, new float[] { 2, 3, 2, 4 });
        prepareListPdf("Abd", testData, colWidth);
    }

    public static String buildBuyerInvoicePdf(String title, String partyName,
            String invoiceNo, String dealDate, Integer dealCases, String data[][]) {
        String newFileName = fileName + File.separator + partyName + "_"
                + invoiceNo + "_" + (new Date()).getTime() + ".pdf";
        File pdfFile = new File(newFileName);
        PdfWriter writer = null;
        PdfDocument pdf = null;
        Document doc = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pdfFile);
            writer = new PdfWriter(fos);
            pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4.rotate());
            doc = new Document(pdf, PageSize.A4.rotate());
            
            setPageProperties(pdf, doc);
            doc.add(new Paragraph("\nBuyer Name:" + partyName));
            doc.add(new Paragraph("Invoice No:" + invoiceNo));
            doc.add(new Paragraph("Date :" + dealDate));
            doc.add(new Paragraph("Balance Cases :" + dealCases));
            doc.add(new Paragraph("\n\n"));
            doc.add(new Paragraph("\n\n"));
            float[] colWidthArr = new float[]{2f, 2f, 4f, 3f};

            doc.add(prepareTable(data, true, colWidthArr));
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            cleanUp(writer, doc, fos);
        }
        return newFileName;
    }

    public static String buildSupplierInvoicePdf(String title, String partyName,
            String invoiceNo, String dealDate, String data[][]) {
        String newFileName = fileName + File.separator + partyName + "_"
                + invoiceNo + "_" + (new Date()).getTime() + ".pdf";
        File pdfFile = new File(newFileName);
        PdfWriter writer = null;
        PdfDocument pdf = null;
        Document doc = null;
        FileOutputStream fos = null;
        try {
            PdfFont bold = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD);
            fos = new FileOutputStream(pdfFile);
            writer = new PdfWriter(fos);
            pdf = new PdfDocument(writer);
            doc = new Document(pdf, PageSize.A4.rotate());
            setPageProperties(pdf, doc);
            doc.add(new Paragraph("MS:" + partyName));
            doc.add(new Paragraph("Invoice No:" + invoiceNo));
            doc.add(new Paragraph("Date :" + dealDate));
            doc.add(new Paragraph("\n\n"));
            float[] colWidthArr = new float[]{17.0f, 17.0f, 7.0f, 7.0f, 7.0f, 15.0f, 15.0f, 15.0f};
            doc.add(prepareTable(data, true, colWidthArr));
            Double grossAmt = Double.valueOf(data[data.length - 1][4]);
            Double expGrossAmt = Double.valueOf(data[data.length - 1][7]);
            Double totalAmount = grossAmt - expGrossAmt;
            doc.add(new Paragraph("\nRs:" + totalAmount).setFont(bold));
            doc.add(new Paragraph(String.format("\nSum of %s has been credited to your account", totalAmount.toString())));
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            cleanUp(writer, doc, fos);
        }
        return newFileName;
    }

    public static String buildMailCashInvoicePdf(MoneyPaidRecd line, String data[][]) {
        String newFileName = fileName + File.separator + line.getTitle()
                + "_" + (new Date()).getTime() + ".pdf";
        File pdfFile = new File(newFileName);
        Integer partyCode = 0;
        String partyName = "";
        try {
            if ("buyer".equalsIgnoreCase(line.getPartyType())) {
                Buyer buyer = DatabaseClient.getInstance().getBuyerByName(line.getTitle());
                partyCode = buyer.getId();
                partyName = buyer.getFirstName() + " " + buyer.getLastName();
            }
            else {
                Supplier supplier = DatabaseClient.getInstance().getSupplierByName(line.getTitle());
                partyCode = supplier.getId();
                partyName = supplier.getFirstName() + " " + supplier.getLastName();
            }
        } catch (SQLException | NoSuchElementException ex) {
            Logger.getLogger(PDFUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        PdfWriter writer = null;
        PdfDocument pdf;
        Document doc = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pdfFile);
            writer = new PdfWriter(fos);
            pdf = new PdfDocument(writer);
            doc = new Document(pdf, PageSize.A4.rotate());
            setPageProperties(pdf, doc);
            Paragraph par = new Paragraph();
            Table table = new Table(new float[] { 1.0f, 1.0f }, false);
            table.setFontSize(14);
            if (line.getReceipt() != null
                    && line.getReceipt().available() > 0) {
                line.getReceipt().reset();
                byte[] imageArray = new byte[line.getReceipt().available()];
                line.getReceipt().read(imageArray);
                Image receipt = new Image(ImageDataFactory.create(imageArray));
                receipt.scaleToFit(200f, 200f);
                Cell cell = new Cell();
                cell.add(receipt);
                cell.setBorder(Border.NO_BORDER);
                table.addCell(cell);
            }
            par.add("Details on the transaction\n\n");
            par.add("Type: " + line.getPartyType() + "\n");
            par.add(line.getPartyType() + " Name: " + partyName + "\n");
            par.add(line.getPartyType() + " Code: " + String.valueOf(partyCode) + "\n");
            par.add("Date: " + line.getDate() + "\n");
            par.add("Amount: " + (Integer.valueOf(line.getPaid())
                    + Integer.valueOf(line.getReceived())) + "\n");
            par.add("Payment type: " + line.getPaymentMode() + "\n");
            Cell cell = new Cell();
            cell.add(par);
            cell.setBorder(Border.NO_BORDER);
            cell.setPaddingLeft(100);
            table.addCell(cell);
            doc.add(table);
            doc.add(new Paragraph("\n\n"));
            float[] colWidthArr = new float[] { 0.7f, 0.7f, 1.2f, 0.7f, 0.7f };
            doc.add(prepareTable(data, true, colWidthArr));
        }
        catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            cleanUp(writer, doc, fos);
        }
        return newFileName;
    }
    
    public static String buildExpenseInvoicePdf(Expenditure line) {
        String newFileName = fileName + File.separator + line.getId()
                + "_" + (new Date()).getTime() + ".pdf";
        File pdfFile = new File(newFileName);
        Integer partyCode = 0;
        String partyName = "";
        String partyType = "";
        try {
            Buyer buyer = DatabaseClient.getInstance().getBuyerByName(line.getPayee());
            partyCode = buyer.getId();
            partyName = buyer.getFirstName() + " " + buyer.getLastName();
            partyType = "Buyer";
        }
        catch (SQLException | NoSuchElementException ex) {
            Logger.getLogger(PDFUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            if ("".equals(partyType)) {
                Supplier supplier = DatabaseClient.getInstance().getSupplierByName(line.getPayee());
                partyCode = supplier.getId();
                partyName = supplier.getFirstName() + " " + supplier.getLastName();
                partyType = "Supplier";
            }
        }
        catch (SQLException | NoSuchElementException ex) {
            Logger.getLogger(PDFUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        PdfWriter writer = null;
        PdfDocument pdf;
        Document doc = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pdfFile);
            writer = new PdfWriter(fos);
            pdf = new PdfDocument(writer);
            doc = new Document(pdf, PageSize.A4.rotate());
            setPageProperties(pdf, doc);
            Paragraph par = new Paragraph();
            Table table = new Table(new float[] { 1.0f, 2.0f }, false);
            table.setFontSize(14);
            par.add("Details of the transaction\n\n");
            par.add("Type: " + "Expenditure" + "\n");
            par.add("Expenditure Type: " + line.getType() + "\n");
            par.add(partyType + " Name: " + partyName + "\n");
            par.add(partyType + " Code: " + String.valueOf(partyCode) + "\n");
            par.add("Date: " + line.getDate() + "\n");
            par.add("Amount: " + line.getAmount() + "\n");
//            par.add("Payment type: " + line.getType()+ "\n");
            Cell cell = new Cell();
            cell.add(par);
            cell.setBorder(Border.NO_BORDER);
            table.addCell(cell);
            cell = new Cell();
            cell.setBorder(Border.NO_BORDER);
            if (line.getReceipt() != null) {
                line.getReceipt().reset();
                if (line.getReceipt().available() > 0) {
                    byte[] imageArray = new byte[line.getReceipt().available()];
                    line.getReceipt().read(imageArray);
                    Image receipt = new Image(ImageDataFactory.create(imageArray));
                    float width = receipt.getImageWidth() > pdf.getDefaultPageSize().getWidth()
                            ? pdf.getDefaultPageSize().getWidth() : receipt.getImageWidth();
                    float height = receipt.getImageHeight() > pdf.getDefaultPageSize().getHeight()
                            ? pdf.getDefaultPageSize().getHeight() : receipt.getImageHeight();
                    receipt.scaleToFit(width, height);
                    cell.add(receipt);
                    cell.setPaddingLeft(100);
                }
            }
            table.addCell(cell);
            doc.add(table);
        }
        catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            cleanUp(writer, doc, fos);
        }
        return newFileName;
    }
    
    public static String prepareListPdf(String title, String data[][], float[] colWidthArr) {
        String newFileName = fileName + File.separator + title + "_" + System.currentTimeMillis() + ".pdf";
        File pdfFile = new File(newFileName);
        PdfWriter writer = null;
        PdfDocument pdf = null;
        Document doc = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pdfFile);
            writer = new PdfWriter(fos);
            pdf = new PdfDocument(writer);
            doc = new Document(pdf, PageSize.A4.rotate());
            setPageProperties(pdf, doc);
            doc.add(new Paragraph("\n\n"));
            Table table = prepareTable(data, false, colWidthArr);
            doc.add(table);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            cleanUp(writer, doc, fos);
        }
        return newFileName;
    }

    private static void cleanUp(PdfWriter writer, Document doc, FileOutputStream fos) {
        try {
            if (doc != null) {
                doc.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setPageProperties(PdfDocument pdf, Document doc) throws IOException {
        doc.setMargins(20, 20, 20, 20);
        pdf.addEventHandler(PdfDocumentEvent.START_PAGE, (event) -> {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
//            PdfPage page = docEvent.getPage();
            Rectangle pageSize = docEvent.getDocument().getFirstPage().getPageSizeWithRotation();
//            PdfDocument pdfDoc = docEvent.getDocument();
//            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
//            new Canvas(pdfCanvas, pdfDoc, pageSize) //FIXME: table on the second page shadows the page heading
//            header
//                    .showTextAligned("title heading", 70, pageSize.getTop() - 20, TextAlignment.CENTER, VerticalAlignment.MIDDLE, 0)
//                    .showTextAligned(headerText, pageSize.getWidth() / 2, pageSize.getTop() - 20, TextAlignment.CENTER,
//                            VerticalAlignment.MIDDLE, 0).setFont(font).add(new Paragraph("\n"))
//                    // footer
//                    .showTextAligned(Integer.toString(pdfDoc.getPageNumber(page)), pageSize.getWidth() / 2, 30,
//                            TextAlignment.CENTER, VerticalAlignment.MIDDLE, 0)
//                    .showTextAligned(headerText, pageSize.getWidth() - 60, 30, TextAlignment.CENTER,
//                            VerticalAlignment.MIDDLE, 0)
//                    .setFont(font);
        });
    }

    private static Table prepareTable(String[][] data, boolean formatFooter, float[] percentWdthArr) throws IOException {
        PdfFont font = PdfFontFactory.createFont(FontConstants.HELVETICA);
        PdfFont bold = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD);

        float[] widthPercents = new float[percentWdthArr.length];
        System.arraycopy(percentWdthArr, 0, widthPercents, 0, percentWdthArr.length);
        Table table = new Table(UnitValue.createPercentArray(widthPercents));
        table.setWidthPercent(100);
        String[] colNameArr = data[0];
        addTableLine(table, colNameArr, bold, true);

        for (int i = 1; i < data.length - 1; i++) {
            String[] value = data[i];
            addTableLine(table, value, font, false);
        }
        for (String value : data[data.length - 1]) {
            if (value == null || value == "null") {
                value = "";
            }
            Cell cell = new Cell().add(new Paragraph(value));
            if (formatFooter) {
                cell.setFont(bold);
                cell.setBackgroundColor(footRowColor);
                table.addFooterCell(cell);
            }
            else {
                cell.setFont(font);
                table.addCell(cell);
            }
        }
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);
        table.setTextAlignment(TextAlignment.LEFT);

        return table;
    }

    public static void addTableLine(Table table, String[] cellValues, PdfFont font, boolean isHeader) {
        for (String value : cellValues) {
            if (value == null || value == "null") {
                value = "";
            }
            Cell cell = new Cell().add(new Paragraph(value).setFont(font));
            if (isHeader) {
                cell.setBackgroundColor(headRowColor);
                table.addHeaderCell(cell);
            } else {
                table.addCell(cell);
            }
        }
    }

    private static final String[][] testData = {
        {"null", "S.No.", "Date", "Invoice No", "Buyer", "Net Amount", "Cases", "null"},
        {"null", "1", "2017-01-14", "19", "0 Chicken", "8878", "300", "null"},
        {"null", "2", "2017-01-14", "21", "0 Chicken", "528", "1", "null"},
        {"null", "3", "2017-01-15", "22", "2 Murgh", "273", "10", "null"},
        {"null", "4", "2017-01-15", "25", "1 Tandoori", "409", "15", "null"},
        {"null", "5", "2017-01-23", "26", "1 Tandoori", "1536", "50", "null"},
        {"null", "6", "2017-01-23", "27", "1 Tandoori", "1818", "100", "null"},
        {"null", "7", "2017-02-06", "28", "0 Chicken", "4439", "150", "null"},
        {"null", "8", "2017-02-10", "30", "2 Murgh", "25", "1", "null"},
        {"null", "9", "2017-02-10", "31", "0 Chicken", "52", "2", "null"},
        {"null", "10", "2017-02-10", "32", "0 Chicken", "765", "21", "null"},
        {"null", "11", "2017-02-14", "30", "0 Chicken", "56", "1", "null"},
        {"null", "12", "2017-02-14", "34", "1 Tandoori", "25", "1", "null"},
        {"null", "13", "2017-02-14", "34", "1 Tandoori", "255", "7", "null"},
        {"null", "14", "2017-02-14", "33", "1 Tandoori", "876", "35", "null"},
        {"null", "15", "2017-02-14", "33", "0 Chicken", "563", "15", "null"},
        {"null", "16", "2017-02-16", "39", "1 Tandoori", "170", "5", "null"},
        {"null", "17", "2017-02-16", "43", "0 Chicken", "88", "3", "null"},
        {"null", "18", "2017-02-16", "43", "1 Tandoori", "142", "5", "null"},
        {"null", "19", "2017-02-16", "42", "1 Tandoori", "102", "5", "null"},
        {"null", "20", "2017-02-16", "42", "2 Murgh", "50", "2", "null"},
        {"null", "21", "2017-02-16", "43", "1 Tandoori", "113", "4", "null"},
        {"null", "22", "2017-02-16", "41", "1 Tandoori", "207", "7", "null"},
        {"null", "23", "2017-02-16", "41", "1 Tandoori", "175", "7", "null"},
        {"null", "24", "2017-02-16", "44", "0 Chicken", "300", "12", "null"},
        {"null", "25", "2017-02-18", "45", "0 Chicken", "770", "13", "null"},
        {"null", "26", "2017-02-18", "45", "1 Tandoori", "59", "2", "null"},
        {"null", "27", "2017-02-18", "45", "2 Murgh", "255", "4", "null"},
        {"null", "28", "2017-02-21", "46", "2 Murgh", "6967", "31", "null"},
        {"null", "29", "2017-04-06", "49", "1 Tandoori", "341", "12", "null"},
        {"null", "30", "2017-04-08", "50", "1 Tandoori", "34", "1", "null"},
        {"null", "31", "2017-04-09", "51", "2 Murgh", "108", "5", "null"},
        {"null", "32", "2017-04-14", "52", "0 Chicken", "64", "3", "null"},
        {"null", "33", "2017-04-14", "52", "1 Tandoori", "82", "2", "null"},
        {"null", "34", "2017-04-14", "52", "2 Murgh", "47", "1", "null"},
        {"null", "35", "2017-04-14", "53", "1 Tandoori", "118", "4", "null"},
        {"null", "36", "2017-04-14", "54", "1 Tandoori", "300", "4", "null"},
        {"null", "37", "2017-04-14", "54", "0 Chicken", "84", "1", "null"}};

    private static float[] colWidth = {6.0f, 9.0f, 15.0f, 15.0f, 14.0f, 22.0f, 15.0f, 0f};

}
