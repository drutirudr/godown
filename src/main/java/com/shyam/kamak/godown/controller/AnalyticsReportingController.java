package com.shyam.kamak.godown.controller;

import com.shyam.kamak.godown.dto.DashboardAnalyticsDTO;
import com.shyam.kamak.godown.dto.FabricInventoryReportDTO;
import com.shyam.kamak.godown.service.AnalyticsReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsReportingController {

    private final AnalyticsReportingService reportingService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardAnalyticsDTO> getSummaryDashboardRegistry(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        return ResponseEntity.ok(reportingService.compileCoreOperationalMetrics(startDate, endDate));
    }
//
//    @GetMapping("/fabric-stock-ledger")
//    public ResponseEntity<List<FabricInventoryReportDTO>> getFabricStockLedgerReport() {
//        return ResponseEntity.ok(reportingService.compileFabricInventoryStockReports());
//    }

}
