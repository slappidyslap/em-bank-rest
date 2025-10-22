package kg.musabaev.em_bank_rest.mapper;

import kg.musabaev.em_bank_rest.dto.CreateCardRequest;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchCardResponse;
import kg.musabaev.em_bank_rest.dto.GetCardDetailsResponse;
import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.mapper.util.CardMapperUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = CardMapperUtil.class)
public interface CardMapper {

    @Mapping(source = "number", target = "numberMasked", qualifiedByName = "maskCardNumber")
    GetCreatePatchCardResponse toCreateCardResponse(Card model);

    @Mapping(source = "number", target = "number", qualifiedByName = "unmaskCardNumber")
    GetCardDetailsResponse toGetCardDetailsResponse(Card card);

    @Mapping(source = "number", target = "numberMasked", qualifiedByName = "maskCardNumber")
    GetCreatePatchCardResponse toPatchCardResponse(Card card);

    @Mapping(source = "number", target = "numberMasked", qualifiedByName = "maskCardNumber")
    GetCreatePatchCardResponse toGetCardResponse(Card card);
}
