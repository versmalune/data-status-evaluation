package kr.co.data_status_evaluation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256CodeEncoder implements PasswordEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SHA256CodeEncoder.class);

    public String encodePassword(String rawPass) {
        String encodePassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] byteArray = rawPass.getBytes();
            byte[] digestByteArray = md.digest(byteArray);
            encodePassword = new String(Base64.encode(digestByteArray));

        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("[" + e.getClass() + "] NoSuchAlgorithmException Error Occured.");
        }
        return encodePassword;
    }

    public boolean isPasswordValid(String encPass, String rawPass) {
        String pass1 = "" + encPass;
        String pass2 = encodePassword(rawPass);
        return PasswordEncoderUtils.equals(pass1, pass2);
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return this.encodePassword(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return isPasswordValid(encodedPassword, rawPassword.toString());
    }


    static class PasswordEncoderUtils {
        /**
         * Constant time comparison to prevent against timing attacks.
         *
         * @param expected
         * @param actual
         * @return
         */

        static boolean equals(String expected, String actual) {
            byte[] expectedBytes = bytesUtf8(expected);
            byte[] actualBytes = bytesUtf8(actual);

            int expectedLength = -1;

            if (expectedBytes != null) {
                expectedLength = expectedBytes.length;
            }

            int actualLength = -1;

            if (actualBytes != null) {
                actualLength = actualBytes.length;
            }

            if (expectedLength != actualLength) {
                return false;
            }

            int result = 0;

            for (int i = 0; i < expectedLength; i++) {
                result |= expectedBytes[i] ^ actualBytes[i];
            }

            return result == 0;
        }

        private static byte[] bytesUtf8(String s) {
            if (s == null) {
                return null;
            }

            return Utf8.encode(s);
        }

        private PasswordEncoderUtils() {
        }
    }

}
