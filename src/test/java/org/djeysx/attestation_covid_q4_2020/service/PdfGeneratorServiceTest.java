package org.djeysx.attestation_covid_q4_2020.service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

import org.djeysx.attestation_covid_q4_2020.security.UserProfile;
import org.junit.jupiter.api.Test;

public class PdfGeneratorServiceTest {

    PdfGeneratorService pdfGeneratorService = new PdfGeneratorService();

    @Test
    public void testGeneratePdf() throws Exception {
        UserProfile userProfile = new UserProfile();
        userProfile.prenom = "[Prénom]";
        userProfile.nom = "[Nom]";
        userProfile.dateNaissance = "12/01/2000";
        userProfile.lieuNaissance = "Sainte Choucroute en boite";
        userProfile.adresse = "99 chemin du petit chaperon rouge";
        userProfile.codePostal = "12345";
        userProfile.ville = "La Mote Beuvron 1234 12312342 1234 1234 1234";

        byte[] pdf = pdfGeneratorService.generatePdf(userProfile, Reason.achats, LocalDateTime.now());
        Files.write(Paths.get("target/result.pdf"), pdf, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

}
