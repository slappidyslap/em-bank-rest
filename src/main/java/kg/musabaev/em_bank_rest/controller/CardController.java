package kg.musabaev.em_bank_rest.controller;

import kg.musabaev.em_bank_rest.dto.CreateCardRequest;
import kg.musabaev.em_bank_rest.dto.GetCreateSingleCardResponse;
import kg.musabaev.em_bank_rest.service.impl.SimpleCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final SimpleCardService cardService;

    // админ
    @PostMapping
    public ResponseEntity<GetCreateSingleCardResponse> create(@RequestBody CreateCardRequest dto) {
        return ResponseEntity.ok(cardService.create(dto.userId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetCreateSingleCardResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<GetCreateSingleCardResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    // админ
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
