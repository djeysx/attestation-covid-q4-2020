package org.djeysx.attestation_covid_q4_2020.web;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import org.djeysx.attestation_covid_q4_2020.security.UserProfile;
import org.djeysx.attestation_covid_q4_2020.service.PdfGeneratorService;
import org.djeysx.attestation_covid_q4_2020.service.Reason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

    @RequestMapping(path = "/attestationCovid/", method = RequestMethod.POST, produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] generatePdf(HttpServletRequest request, Model model,
            @RequestParam(required = true, name = "field-reason") Reason reason,
            @RequestParam(required = false, name = "field-time") int modTime) throws Exception {
        log.info("POST");
        UserProfile userProfile = getCurrentUserProfile();
        LocalDateTime dateTime = LocalDateTime.now();
        if (modTime < 0)
            dateTime.minusMinutes(Math.abs(modTime));
        if (modTime > 0)
            dateTime.plusMinutes(modTime);

        return pdfGeneratorService.generatePdf(userProfile, dateTime);
    }

    protected UserProfile getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserProfile userProfile = (UserProfile) authentication.getDetails();
        return userProfile;
    }
}
