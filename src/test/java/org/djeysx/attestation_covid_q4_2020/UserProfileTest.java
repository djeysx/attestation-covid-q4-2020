package org.djeysx.attestation_covid_q4_2020;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class UserProfileTest {

    @Test
    public void testGetPassword() throws Exception {
        UserProfile up = new UserProfile();
        up.dateNaissance = "01/02/2000";
        assertThat(up.getPassword()).isEqualTo("2000");
    }

}
