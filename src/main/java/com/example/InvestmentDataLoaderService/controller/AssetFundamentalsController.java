package com.example.InvestmentDataLoaderService.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.InvestmentDataLoaderService.dto.AssetFundamentalDto;
import com.example.InvestmentDataLoaderService.dto.AssetFundamentalsRequestDto;
import com.example.InvestmentDataLoaderService.service.AssetFundamentalService;
import com.example.InvestmentDataLoaderService.scheduler.AssetFundamentalsSchedulerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/asset-fundamentals")
public class AssetFundamentalsController {

    private static final Logger log = LoggerFactory.getLogger(AssetFundamentalsController.class);
    private final AssetFundamentalService assetFundamentalService;


    public AssetFundamentalsController(AssetFundamentalService assetFundamentalService,
                                     AssetFundamentalsSchedulerService schedulerService) {
        this.assetFundamentalService = assetFundamentalService;
    }


    /**
     * Получить фундаментальные показатели для одного актива
     */
    @GetMapping("/{assetUid}")
    public ResponseEntity<Map<String, Object>> getAssetFundamentals(
        @PathVariable String assetUid
    ) {
        List<AssetFundamentalDto> listOfAssetsFundsaFundamentals = assetFundamentalService.getFundamentalsForAsset(assetUid);
        
        Map<String, Object> response = new HashMap<>();
        response.put("succsess", true);
        response.put("uid", assetUid);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", "/api/asset-fundamentals/" + assetUid);
        response.put("assets", listOfAssetsFundsaFundamentals);
        return ResponseEntity.ok().body(response);
    }

    /**
     * Сохранить фундаментальные показатели для одного актива
     */
    @PostMapping("/{assetUid}")
    public ResponseEntity<Map<String, Object>> saveAssetFundamentals(@PathVariable String assetUid) {
        List<AssetFundamentalDto> listOfAssetsFundsaFundamentals = assetFundamentalService.getFundamentalsForAsset(assetUid);
        if (listOfAssetsFundsaFundamentals.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("succsess", false);
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("path", "/api/asset-fundamentals/" + assetUid);
            response.put("error", "Не получены фундаментальные показатели для актива");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        assetFundamentalService.saveAssetFundamentals(listOfAssetsFundsaFundamentals);
        Map<String, Object> response = new HashMap<>();
        response.put("succsess", true);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", "/api/asset-fundamentals/" + assetUid);
        return ResponseEntity.ok().body(response);
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> getFundamentalsForAssets(@RequestBody AssetFundamentalsRequestDto requestDto) {
        List<AssetFundamentalDto> listOfAssetsFundsaFundamentals = assetFundamentalService.getFundamentalsForAssets(requestDto.getAssets());
        Map<String, Object> response = new HashMap<>();
        response.put("succsess", true);
        response.put("assets", listOfAssetsFundsaFundamentals);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", "/api/asset-fundamentals");
        response.put("infoOfInstruments", listOfAssetsFundsaFundamentals.size());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/load")
    public ResponseEntity<Map<String, Object>> loadFundamentalsForAssets(@RequestBody AssetFundamentalsRequestDto requestDto) {
        List<AssetFundamentalDto> listOfAssetsFundsaFundamentals = assetFundamentalService.getFundamentalsForAssets(requestDto.getAssets());
        assetFundamentalService.saveAssetFundamentals(listOfAssetsFundsaFundamentals);
        Map<String, Object> response = new HashMap<>();
        response.put("succsess", true);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", "/api/asset-fundamentals/load");
        response.put("instrumentsLoaded", listOfAssetsFundsaFundamentals.size());
        return ResponseEntity.ok().body(response);
    }
    
}
