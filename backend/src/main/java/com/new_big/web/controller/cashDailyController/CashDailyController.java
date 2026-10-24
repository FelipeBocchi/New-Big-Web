package com.new_big.web.controller.cashDailyController;

import com.new_big.web.controller.cashDailyController.dto.CashDailyClosedRequest;
import com.new_big.web.controller.cashDailyController.dto.CashDailyClosedResponse;
import com.new_big.web.controller.cashDailyController.dto.CashDailyOpenedRequest;
import com.new_big.web.controller.cashDailyController.dto.CashDailyOpenedResponse;
import com.new_big.web.entity.CashDailyLog;
import com.new_big.web.service.CashDailyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cash")
@CrossOrigin("*")
public class CashDailyController {

    private final CashDailyService service;

    public CashDailyController(CashDailyService service) {
        this.service = service;
    }

    // OPEN
    // POST LOCALHOST:8080/API/CASH
    @PostMapping()
    public ResponseEntity<CashDailyOpenedResponse> open(
            @Valid @RequestBody CashDailyOpenedRequest request
    ) {

        CashDailyLog cash = this.service.open(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CashDailyOpenedResponse.de(cash));
    }

    // CLOSE
    // PATCH LOCALHOST:8080/API/CASH
    @PatchMapping("/{id}")
    public ResponseEntity<CashDailyClosedResponse> close(
            @Valid @RequestBody CashDailyClosedRequest request,
            @PathVariable Long id
    ) {

        CashDailyLog cash = this.service.close(request, id);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CashDailyClosedResponse.de(cash));
    }

    // FIND_ALL
    // GET LOCALHOST:8080/API/CASH
    @GetMapping()
    public ResponseEntity<List<CashDailyClosedResponse>> findAll() {

        List<CashDailyClosedResponse> cashList = this.service.findAll().stream()
                .map(CashDailyClosedResponse::de)
                .toList();

        return ResponseEntity.ok(cashList);
    }

    // FIND_INFO_BY_ID
    // GET LOCALHOST:8080/API/CASH
    @GetMapping("/{id}")
    public ResponseEntity<CashDailyClosedResponse> findInfoById(
            @PathVariable Long id
    ) {

        CashDailyLog cash = this.service.findInfoById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CashDailyClosedResponse.de(cash));
    }

}
