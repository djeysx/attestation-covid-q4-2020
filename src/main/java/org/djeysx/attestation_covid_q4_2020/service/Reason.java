package org.djeysx.attestation_covid_q4_2020.service;

public enum Reason {
    /* https://github.com/LAB-MI/attestation-deplacement-derogatoire-q4-2020/blob/main/src/js/pdf-util.js*/

    travail(578), //
    achats(533), //
    sante(477), //
    famille(435), //
    handicap(396), //
    sport_animaux(358), //
    convocation(295), //
    missions(255), //
    enfants(211);

    private int pdfPos;

    private Reason(int pdfPos) {
        this.pdfPos = pdfPos;
    }

    public int getPdfPos() {
        return pdfPos;
    }
}
