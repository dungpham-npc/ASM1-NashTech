package com.dungpham.asm1.common.util;

import org.mapstruct.Named;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Util {
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIALS = "@$!%*#?&";
    private static final String ALL_ALLOWED = LETTERS + DIGITS + SPECIALS;

    private static final SecureRandom random = new SecureRandom();


    @Named("convertTimestampToLocalDateTime")
    public static LocalDateTime convertTimestampToLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
        );
    }

    public static String generateSecurePassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8 characters");
        }

        List<Character> passwordChars = new ArrayList<>();

        passwordChars.add(LETTERS.charAt(random.nextInt(LETTERS.length())));
        passwordChars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        passwordChars.add(SPECIALS.charAt(random.nextInt(SPECIALS.length())));

        for (int i = 3; i < length; i++) {
            passwordChars.add(ALL_ALLOWED.charAt(random.nextInt(ALL_ALLOWED.length())));
        }

        Collections.shuffle(passwordChars, random);

        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }
}
