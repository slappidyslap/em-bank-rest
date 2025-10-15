package kg.musabaev.em_bank_rest.util;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

// https://medium.com/@gaddamnaveen192/encrypt-decrypt-database-records-at-field-level-in-spring-boot-096e21049559
@Component
public class SomePaymentSystemProvider {

    private final String ALGORITHM = "AES";
    private final String SECRET_KEY = "MySecretKey12345";

    public String generateEncryptedRandomCardNumber() {
        StringBuilder sb = new StringBuilder(16);
        sb.append("6");
        for (int i = 1; i < 16; i++) {
            int d = ThreadLocalRandom.current().nextInt(0, 10);
            sb.append(d);
        }
        return encryptCardNumber(sb.toString());
    }

    public boolean compareEncrypterCardNumbers(String n1, String n2) {
        if (n1 == null || n2 == null) return false;
        return n1.equals(n2);
    }

    public String encryptCardNumber(String cardNumber) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(cardNumber.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting: ", e);
        }
    }

    public String decryptCardNumber(String encryptedCardNumber) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedCardNumber)));
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting: ", e);
        }
    }

    public String maskCardNumber(String encryptedCardNumber) {
        return "**** **** **** " + decryptCardNumber(encryptedCardNumber).substring(12);
    }
}
