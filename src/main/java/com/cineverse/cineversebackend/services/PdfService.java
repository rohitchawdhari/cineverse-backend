package com.cineverse.cineversebackend.services;

import com.cineverse.cineversebackend.models.Booking;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class PdfService {

    public byte[] generateTicketPdf(Booking booking) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A5);
            PdfWriter.getInstance(document, baos);
            
            document.open();

            // Font styles
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font boldBodyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

            // Title
            Paragraph title = new Paragraph("CINEVERSE TICKET RECEIPT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            // Ticket Details Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(15);

            // Table Header style
            PdfPCell headerCell1 = new PdfPCell(new Paragraph("Ticket Property", headerFont));
            headerCell1.setBackgroundColor(new Color(63, 81, 181)); // Material Blue
            headerCell1.setPadding(6);
            
            PdfPCell headerCell2 = new PdfPCell(new Paragraph("Details", headerFont));
            headerCell2.setBackgroundColor(new Color(63, 81, 181));
            headerCell2.setPadding(6);

            table.addCell(headerCell1);
            table.addCell(headerCell2);

            // Add fields
            addTableRow(table, "Booking Reference:", booking.getId(), bodyFont, boldBodyFont);
            addTableRow(table, "Ticket Number:", booking.getTicketNumber(), bodyFont, bodyFont);
            addTableRow(table, "Movie Title:", booking.getMovieTitle(), bodyFont, boldBodyFont);
            addTableRow(table, "Theater:", booking.getTheaterName(), bodyFont, bodyFont);
            addTableRow(table, "Screen:", booking.getScreenNumber(), bodyFont, bodyFont);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            addTableRow(table, "Show Time:", booking.getShowStartTime().format(formatter), bodyFont, bodyFont);
            addTableRow(table, "Seats Booked:", String.join(", ", booking.getSeats()), bodyFont, boldBodyFont);
            addTableRow(table, "Amount Paid:", "INR " + booking.getTotalAmount(), bodyFont, boldBodyFont);
            addTableRow(table, "Booking Status:", booking.getStatus(), bodyFont, bodyFont);

            document.add(table);

            // Embed QR Code
            if (booking.getQrCodeBase64() != null && booking.getQrCodeBase64().contains(",")) {
                String base64Data = booking.getQrCodeBase64().substring(booking.getQrCodeBase64().indexOf(",") + 1);
                byte[] qrBytes = Base64.getDecoder().decode(base64Data);
                Image qrImage = Image.getInstance(qrBytes);
                qrImage.setAlignment(Element.ALIGN_CENTER);
                qrImage.scaleToFit(120, 120);
                qrImage.setSpacingBefore(5);
                qrImage.setSpacingAfter(5);
                document.add(qrImage);
            }

            // Footer info
            Paragraph footer = new Paragraph("Please show the QR Code at the theater entrance.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(10);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF ticket: " + e.getMessage(), e);
        }
    }

    private void addTableRow(PdfPTable table, String prop, String val, Font regular, Font bold) {
        PdfPCell cellProp = new PdfPCell(new Paragraph(prop, regular));
        cellProp.setPadding(5);
        PdfPCell cellVal = new PdfPCell(new Paragraph(val != null ? val : "N/A", bold));
        cellVal.setPadding(5);
        table.addCell(cellProp);
        table.addCell(cellVal);
    }
}
