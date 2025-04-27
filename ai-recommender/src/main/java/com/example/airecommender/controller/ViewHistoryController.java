package com.example.airecommender.controller;

import com.example.airecommender.dto.ViewHistoryRequestDto;
import com.example.airecommender.service.ViewHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/view-history")
public class ViewHistoryController {

    private final ViewHistoryService viewHistoryService;

    public ViewHistoryController(ViewHistoryService viewHistoryService) {
        this.viewHistoryService = viewHistoryService;
    }

    @PostMapping
    public ResponseEntity<Void> recordViewHistory(@RequestBody ViewHistoryRequestDto requestDto) {
        viewHistoryService.recordViewHistory(requestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
