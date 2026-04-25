package com.prl.smartexpensetracker.controller;

import com.prl.smartexpensetracker.dto.PredictionDTO;
import com.prl.smartexpensetracker.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/predictions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping("/run")
    public ResponseEntity<List<PredictionDTO>> generateForecast(@RequestParam Long userId) {
        List<PredictionDTO> predictions = predictionService.generateForecast(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(predictions);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<PredictionDTO>> getAllForecasts(@PathVariable Long userId) {
        List<PredictionDTO> predictions = predictionService.getAllForecasts(userId);
        return ResponseEntity.ok(predictions);
    }

    @GetMapping("/{userId}/latest")
    public ResponseEntity<PredictionDTO> getLatestForecast(@PathVariable Long userId) {
        PredictionDTO prediction = predictionService.getLatestForecast(userId);
        return ResponseEntity.ok(prediction);
    }

    @GetMapping("/{userId}/next-month")
    public ResponseEntity<Map<String, Object>> predictNextMonth(@PathVariable Long userId) {
        BigDecimal prediction = predictionService.predictNextMonthSpending(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "predictedAmount", prediction));
    }
}
