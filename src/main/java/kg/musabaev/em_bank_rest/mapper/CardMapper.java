package kg.musabaev.em_bank_rest.mapper;

import kg.musabaev.em_bank_rest.dto.CreateCardRequest;
import kg.musabaev.em_bank_rest.dto.GetCreateSingleCardResponse;
import kg.musabaev.em_bank_rest.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    Card toEntity(CreateCardRequest dto);

    @Mapping(source = "number", target = "numberMasked")
    GetCreateSingleCardResponse toCreateCardResponse(Card model);

    Card toEntity(GetCreateSingleCardResponse getSingleCardResponse);
    
    @Mapping(source = "number", target = "numberMasked")
    GetCreateSingleCardResponse toGetSingleCardResponse(Card card);
}
