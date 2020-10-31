package org.djeysx.attestation_covid_q4_2020;

public class UserProfile {
    public String prenom;
    public String nom;
    /** format jj/mm/aaaa */
    public String dateNaissance;
    public String lieuNaissance;
    public String adresse;
    public String ville;
    public String codePostal;

    public String getPassword() {
        return dateNaissance.split("/")[2];
    }
}
