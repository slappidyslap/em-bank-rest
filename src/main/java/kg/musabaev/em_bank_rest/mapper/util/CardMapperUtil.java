package kg.musabaev.em_bank_rest.mapper.util;

import kg.musabaev.em_bank_rest.util.SomePaymentSystemProvider;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@Named("CardMapperUtil")
@RequiredArgsConstructor
public class CardMapperUtil {

    private final SomePaymentSystemProvider paymentSystemProvider;

    @Named("maskCardNumber")
    public String maskCardNumber(String encryptedCardNumber) {
        return paymentSystemProvider.maskCardNumber(encryptedCardNumber);
    }

    @Named("unmaskCardNumber")
    public String unmaskCardNumber(String encryptedCardNumber) {
        return paymentSystemProvider.decryptCardNumber(encryptedCardNumber);

    }
}
