package org.djeysx.attestation_covid_q4_2020.web;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletRequest;

import org.djeysx.attestation_covid_q4_2020.security.ProfileAuthenticationProvider;
import org.djeysx.attestation_covid_q4_2020.security.UserProfile;
import org.djeysx.attestation_covid_q4_2020.service.PdfGeneratorService;
import org.djeysx.attestation_covid_q4_2020.service.Reason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HelloController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PdfGeneratorService pdfGeneratorService;

    @Autowired
    ProfileAuthenticationProvider profileAuthenticationProvider;

    final DateTimeFormatter filenameDateFormat = DateTimeFormatter.ofPattern("YYYY-MM-dd_HH-mm");

    @RequestMapping("/")
    public String index() {
        return "redirect:/attestationCovid/";
    }

    @RequestMapping(value = "/logout")
    public String logout() {
        return "redirect:/login?logout";
    }

    @RequestMapping(path = "/attestationCovid/", method = RequestMethod.GET)
    public String index(Model model) {
        log.info("GET");
        UserProfile userProfile = getCurrentUserProfile();
        String userName = userProfile.prenom + " " + userProfile.nom;
        model.addAttribute("userName", userName);
        return "index";
    }

    @RequestMapping(path = "/attestationCovid/", method = RequestMethod.POST)
    public ResponseEntity<byte[]> generatePdf(HttpServletRequest request, Model model,
            @RequestParam(required = true, name = "field-reason") Reason reason,
            @RequestParam(required = false, name = "field-time") int modTime) throws Exception {
        log.info("POST");
        UserProfile userProfile = getCurrentUserProfile();
        LocalDateTime dateTime = LocalDateTime.now();
        if (modTime < 0)
            dateTime = dateTime.minusMinutes(Math.abs(modTime));
        if (modTime > 0)
            dateTime = dateTime.plusMinutes(modTime);

        byte[] pdf = pdfGeneratorService.generatePdf(userProfile, reason, dateTime);

        // build response
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(pdf.length);
        headers.set("Content-Type", APPLICATION_PDF_VALUE);
        String filename = "attestation-" + dateTime.format(filenameDateFormat) + ".pdf";
        headers.set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    protected UserProfile getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        return profileAuthenticationProvider.readUserProfile(userName);
    }
}
