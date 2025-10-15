package kg.musabaev.em_bank_rest.mapper;

import kg.musabaev.em_bank_rest.dto.CreateCardRequest;
import kg.musabaev.em_bank_rest.dto.GetCreateSingleCardResponse;
import kg.musabaev.em_bank_rest.entity.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {

    Card toEntity(CreateCardRequest dto);

    GetCreateSingleCardResponse toCreateCardResponse(Card model);

    Card toEntity(GetCreateSingleCardResponse getSingleCardResponse);

    GetCreateSingleCardResponse toGetSingleCardResponse(Card card);
}
