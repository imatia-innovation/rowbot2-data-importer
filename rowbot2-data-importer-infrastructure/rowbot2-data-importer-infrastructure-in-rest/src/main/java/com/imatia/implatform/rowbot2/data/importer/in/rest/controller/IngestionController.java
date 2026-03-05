package com.imatia.implatform.rowbot2.data.importer.in.rest.controller;

import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jdbc.async.AsyncIngestionService;
import com.imatia.implatform.rowbot2.data.importer.model.tenant.DataSourceConnectionSettings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingestion")
public class IngestionController {

    private final AsyncIngestionService asyncService;

    public IngestionController(AsyncIngestionService asyncService) {
        this.asyncService = asyncService;
    }

    @PostMapping("/start")
    public ResponseEntity<String> start(@RequestBody DataSourceConnectionSettings cs) {
        asyncService.runAsync(cs);  // returns immediately (async)
        return ResponseEntity.ok("Ingestion started");
    }
}