package kg.musabaev.em_bank_rest.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

// https://medium.com/@gaddamnaveen192/encrypt-decrypt-database-records-at-field-level-in-spring-boot-096e21049559
@Converter
public class CardNumberConverter implements AttributeConverter<String, String> {

    private final String ALGORITHM = "AES";
    private final String SECRET_KEY = "MySecretKey12345";

    @Override
    public String convertToDatabaseColumn(String cardNumber) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(cardNumber.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting: ", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String encryptedCardNumber) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedCardNumber)));
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting: ", e);
        }
    }
}
