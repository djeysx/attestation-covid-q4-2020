package org.djeysx.attestation_covid_q4_2020.service;

public enum Reason {
    /* https://github.com/LAB-MI/attestation-deplacement-derogatoire-q4-2020/blob/main/src/js/pdf-util.js*/

    travail(553), //
    achats_culturel_cultuel(482), //
    sante(434), //
    famille(410), //
    handicap(373), //
    sport_animaux(349), //
    convocation(276), //
    missions(252), //
    enfants(228);

    private int pdfPos;

    private Reason(int pdfPos) {
        this.pdfPos = pdfPos;
    }

    public int getPdfPos() {
        return pdfPos;
    }
}
