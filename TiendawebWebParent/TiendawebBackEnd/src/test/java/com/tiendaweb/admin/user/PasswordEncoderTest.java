package com.tiendaweb.admin.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class PasswordEncoderTest {
    @Test
    public void testEncodePassword() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawPassword = "deivid2021";
        String passwordEncoded = passwordEncoder.encode(rawPassword);

        System.out.println(passwordEncoded);

        boolean matches =  passwordEncoder.matches(rawPassword, passwordEncoded);

        assertThat(matches).isTrue();
    }
}
