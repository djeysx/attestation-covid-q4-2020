package org.djeysx.attestation_covid_q4_2020.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.djeysx.attestation_covid_q4_2020.security.UserProfile;
import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

@Component
/* https://github.com/LAB-MI/attestation-deplacement-derogatoire-q4-2020/blob/main/src/js/pdf-util.js */
public class PdfGeneratorService {

	protected static final String RESOURCE_PDF = "/certificate.pdf";
	final PDFont font = PDType1Font.HELVETICA;
	final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd/MM/YYYY");
	final DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm");

	public byte[] generatePdf(UserProfile userProfile, Reason reason, LocalDateTime fromDate) {
		try (InputStream resInput = getClass().getResourceAsStream(RESOURCE_PDF);
				PDDocument pdfTemplate = PDDocument.load(resInput);
				PDDocument document = new PDDocument()) {
			document.importPage(pdfTemplate.getPage(0));
			// Meta-data
			document.getDocumentInformation().setTitle("COVID-19 - Déclaration de déplacement");
			document.getDocumentInformation().setSubject("Attestation de déplacement dérogatoire");
			document.getDocumentInformation()
					.setKeywords("covid19 covid-19 attestation déclaration déplacement officielle gouvernement");
			document.getDocumentInformation().setProducer("DNUM/SDIT");
			document.getDocumentInformation().setCreator("");
			document.getDocumentInformation().setAuthor("Ministère de l'intérieur");
			Calendar creationCalendar = Calendar.getInstance();
			creationCalendar.setTime(Timestamp.valueOf(fromDate));
			document.getDocumentInformation().setCreationDate(creationCalendar);
			document.setVersion(1.7f);

			// qr
			String qrData = buildQrData(userProfile, reason, fromDate);
			PDImageXObject pdImage = LosslessFactory.createFromImage(document, createQrImage(qrData));

			// formulaire
			PDPage page1 = document.getPage(0);

			// String qrTitle1 = "QR-code contenant les informations ";
			// String qrTitle2 = "de votre attestation numérique";

			try (PDPageContentStream contentStream = new PDPageContentStream(document, page1, AppendMode.APPEND,
					true)) {
				drawText(contentStream, userProfile.prenom + " " + userProfile.nom, 92, 702);
				drawText(contentStream, userProfile.dateNaissance, 92, 684);
				drawText(contentStream, userProfile.lieuNaissance, 214, 684);
				String adresseFq = userProfile.adresse + " " + userProfile.codePostal + " " + userProfile.ville;
				int adresseFqSize = getIdealFontSize(adresseFq, 380, 7, 11);
				drawText(contentStream, adresseFq, 104, 665, adresseFqSize);
				drawText(contentStream, "x", 47, reason.getPdfPos(), 12);
				int villeSize = getIdealFontSize(userProfile.ville, 250, 7, 11);
				drawText(contentStream, userProfile.ville, 78, 76, villeSize);
				drawText(contentStream, fromDate.format(formatterDate), 63, 58);
				drawText(contentStream, fromDate.format(formatterTime), 227, 58);
				// qr original size = 92f modified=115f
				// drawText(contentStream, qrTitle1 + "\n" + qrTitle2, 415, 135, 9);
				contentStream.drawImage(pdImage, page1.getMediaBox().getWidth() - 156f, 25f, 92f, 92f);
			}
			// page 2
			PDPage page2 = new PDPage(page1.getMediaBox());
			document.addPage(page2);
			try (PDPageContentStream contentStream = new PDPageContentStream(document, page2, AppendMode.APPEND,
					true)) {
				// drawText(contentStream, qrTitle1 + "\n" + qrTitle2, 50, (int) page2.getMediaBox().getHeight() - 70,
				// 11);
				// qr pos source= 50,Height()-350 , 300x300
				contentStream.drawImage(pdImage, 50f, page2.getMediaBox().getHeight() - 390f, 320f, 320f);
			}

			ByteArrayOutputStream pdfBuffer = new ByteArrayOutputStream();

			document.save(pdfBuffer);
			return pdfBuffer.toByteArray();

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	protected String buildQrData(UserProfile userProfile, Reason reason, LocalDateTime fromDate) {
		StringBuilder sb = new StringBuilder();
		sb.append("Cree le: " + fromDate.format(formatterDate) + " a "
				+ fromDate.format(formatterTime).replace(":", "h"));
		sb.append(";\n");
		sb.append("Nom: " + userProfile.nom);
		sb.append(";\n");
		sb.append("Prenom: " + userProfile.prenom);
		sb.append(";\n");
		sb.append("Naissance: " + userProfile.dateNaissance + " a " + userProfile.lieuNaissance);
		sb.append(";\n");
		sb.append("Adresse: " + userProfile.adresse + " " + userProfile.codePostal + " " + userProfile.ville);
		sb.append(";\n");
		sb.append("Sortie: " + fromDate.format(formatterDate) + " a " + fromDate.format(formatterTime));
		sb.append(";\n");
		sb.append("Motifs: " + reason);

		return sb.toString();
	}

	protected BufferedImage createQrImage(String qrData) {
		try {
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			Map<EncodeHintType, Object> hints = new HashMap<>();
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
			hints.put(EncodeHintType.MARGIN, 1);
			BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 220, 220, hints);
			return MatrixToImageWriter.toBufferedImage(bitMatrix);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void drawText(PDPageContentStream contentStream, String text, int x, int y) throws IOException {
		drawText(contentStream, text, x, y, 11);
	}

	protected void drawText(PDPageContentStream contentStream, String text, int x, int y, int fontSize)
			throws IOException {
		// en cas de multi line
		String[] lines = text.split("\n");
		contentStream.beginText();
		contentStream.setNonStrokingColor(0f, 0f, 0f);
		contentStream.setFont(font, fontSize);
		contentStream.newLineAtOffset(x, y);
		contentStream.showText(lines[0]);
		for (int i = 1, len = lines.length; i < len; i++) {
			contentStream.newLine();
			contentStream.showText(lines[0]);
		}
		contentStream.endText();
	}

	protected int getIdealFontSize(String text, int maxWidth, int minSize, int defaultSize) throws IOException {
		int currentSize = defaultSize;
		// https://stackoverflow.com/questions/13701017/calculation-string-width-in-pdfbox-seems-only-to-count-characters
		float textWidth = font.getStringWidth(text) / 1000 * defaultSize;

		while (textWidth > maxWidth && currentSize > minSize) {
			textWidth = font.getStringWidth(text) / 1000 * (--currentSize);
		}
		return textWidth > maxWidth ? minSize : currentSize;
	}

}
