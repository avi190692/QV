package com.ai_int.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPrintPage;

public class PrintUtil {

    private static final DocFlavor myFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;

    @Deprecated
    public static void print(String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            // Create a Doc
            Doc myDoc = new SimpleDoc(fis, myFormat, null);
            // Build a set of attributes
            PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
            aset.add(new Copies(1));
            aset.add(MediaSizeName.ISO_A4);
//            aset.add(Sides.ONE_SIDED);
            PrintService ps = PrintServiceLookup.lookupDefaultPrintService();
            DocPrintJob job = ps.createPrintJob();
            System.out.println("Flavor " + myDoc.getDocFlavor());
            job.print(myDoc, aset);
        } catch (FileNotFoundException | PrintException ex) {
            ex.printStackTrace();
        } finally {
            try {
                File file = new File(fileName);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public static void printPDF(String filePath) {
        printPDF(filePath, false);
    }
    
    public static void printPDF(String filePath, boolean orientationLandscape) {
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            byte[] pdfContent = new byte[fileInputStream.available()];
            fileInputStream.read(pdfContent, 0, fileInputStream.available());
            ByteBuffer buffer = ByteBuffer.wrap(pdfContent);
            final PDFFile pdfFile = new PDFFile(buffer);
            PDFPrintPage pages = new PDFPrintPage(pdfFile);

            PrinterJob printJob = PrinterJob.getPrinterJob();
            PageFormat pageFormat = PrinterJob.getPrinterJob().defaultPage();
//            printJob.setJobName(jobName);
            Book book = new Book();
            book.append(pages, pageFormat, pdfFile.getNumPages());
            printJob.setPageable(book);
            Paper paper = new Paper();
            paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
            pageFormat.setPaper(paper);
            pageFormat.setOrientation(orientationLandscape ? PageFormat.LANDSCAPE : PageFormat.PORTRAIT);
            PrintService ps = PrintServiceLookup.lookupDefaultPrintService();
            printJob.setPrintService(ps);
            printJob.print();
        }
        catch (PrinterException | IOException ex) {
            Logger.getLogger(PrintUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
