package kg.musabaev.em_bank_rest.mapper;

import kg.musabaev.em_bank_rest.dto.CreateCardRequest;
import kg.musabaev.em_bank_rest.dto.GetCreateSingleCardResponse;
import kg.musabaev.em_bank_rest.entity.Card;
import kg.musabaev.em_bank_rest.mapper.util.CardMapperUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = CardMapperUtil.class)
public interface CardMapper {

    Card toEntity(CreateCardRequest dto);

    @Mapping(source = "number", target = "numberMasked", qualifiedByName = "maskCardNumber")
    GetCreateSingleCardResponse toCreateCardResponse(Card model);

    Card toEntity(GetCreateSingleCardResponse getSingleCardResponse);
    
    @Mapping(source = "number", target = "numberMasked", qualifiedByName = "maskCardNumber")
    GetCreateSingleCardResponse toGetSingleCardResponse(Card card);
}
