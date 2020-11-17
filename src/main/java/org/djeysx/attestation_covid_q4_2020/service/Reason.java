package org.djeysx.attestation_covid_q4_2020.service;

public enum Reason {
    /* https://github.com/LAB-MI/attestation-deplacement-derogatoire-q4-2020/blob/main/src/js/pdf-util.js*/

    travail(488), //
    achats(417), //
    sante(347), //
    famille(325), //
    handicap(291), //
    sport_animaux(269), //
    convocation(199), //
    missions(178), //
    enfants(157);

    private int pdfPos;

    private Reason(int pdfPos) {
        this.pdfPos = pdfPos;
    }

    public int getPdfPos() {
        return pdfPos;
    }
}
