package com.shyam.kamak.godown.service;

import com.shyam.kamak.godown.dto.DashboardAnalyticsDTO;
import com.shyam.kamak.godown.dto.FabricStockMetricDTO;
import com.shyam.kamak.godown.dto.TopCustomerMetricDTO;
import com.shyam.kamak.godown.model.TypeOfBill;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsReportingService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public DashboardAnalyticsDTO compileCoreOperationalMetrics(String startDateStr, String endDateStr) {
        // Enforce all-time fallback dates boundaries if parameters arrive unpopulated
        LocalDate start = (startDateStr != null && !startDateStr.trim().isEmpty()) ? LocalDate.parse(startDateStr.trim()) : LocalDate.of(1970, 1, 1);
        LocalDate end = (endDateStr != null && !endDateStr.trim().isEmpty()) ? LocalDate.parse(endDateStr.trim()) : LocalDate.of(2099, 12, 31);

        Object[] dateArgs = new Object[]{java.sql.Date.valueOf(start), java.sql.Date.valueOf(end)};

        // 📊 Query 1: Basic Invoices Counts
        Long totalBills = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sales_bills WHERE bill_date BETWEEN ? AND ?", dateArgs, Long.class);
        if (totalBills == null) totalBills = 0L;

        // 📊 Query 2: Gross Financial Summation
        Object grossRevObj = jdbcTemplate.queryForObject(
                "SELECT SUM(grand_total) FROM sales_bills WHERE bill_date BETWEEN ? AND ?", dateArgs, Object.class);
        BigDecimal grossRev = grossRevObj != null ? new BigDecimal(grossRevObj.toString()) : BigDecimal.ZERO;

        // 📊 Query 3: Active Client Base Connections count
        Long uniqueCustomers = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT customer_id) FROM sales_bills WHERE bill_date BETWEEN ? AND ?", dateArgs, Long.class);
        if (uniqueCustomers == null) uniqueCustomers = 0L;

        BigDecimal avgInvoice = totalBills > 0 ? grossRev.divide(BigDecimal.valueOf(totalBills), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // 📦 Inventory Totals (Unbounded by specific bill date parameters since stocks track current physical counts)
        Long totalBundles = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM bundles", Long.class);
        Long soldBundles = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM bundles WHERE is_sold = true", Long.class);
        if (totalBundles == null) totalBundles = 0L;
        if (soldBundles == null) soldBundles = 0L;
        Long availableBundles = totalBundles - soldBundles;

        // 📊 Query 4: Category Distribution Matrix
        Map<String, Long> distribution = new HashMap<>();
        String distributionSql = "SELECT UPPER(t.group_type), COUNT(b.id) FROM sales_bills b " +
                "JOIN type_of_bills t ON b.type_of_bill_id = t.id " +
                "WHERE b.bill_date BETWEEN ? AND ? GROUP BY t.group_type";
        jdbcTemplate.query(distributionSql, dateArgs, rs -> {
            distribution.put(rs.getString(1).trim().toUpperCase(), rs.getLong(2));
        });
        for (TypeOfBill.BillGroupType group : TypeOfBill.BillGroupType.values()) { distribution.putIfAbsent(group.name(), 0L); }

        // 📊 Query 5: Top Customers Leaderboard
        String customerSql = "SELECT c.name, COUNT(b.id) as bills_count, SUM(b.grand_total) as total_val " +
                "FROM sales_bills b JOIN customers c ON b.customer_id = c.id " +
                "WHERE b.bill_date BETWEEN ? AND ? " +
                "GROUP BY c.name ORDER BY total_val DESC LIMIT 5";
        List<TopCustomerMetricDTO> customersList = jdbcTemplate.query(customerSql, dateArgs, (rs, rn) ->
                TopCustomerMetricDTO.builder()
                        .customerName(rs.getString("name"))
                        .totalInvoices(rs.getLong("bills_count"))
                        .totalContribution(rs.getBigDecimal("total_val").setScale(2, RoundingMode.HALF_UP))
                        .build()
        );

        // 📊 Query 6: Consolidated Fabric Stock Ledger (Only attributes sold rolls to the dates range)
        // 📊 PASS 1: Pull current static physical warehouse stock levels (independent of bill date ranges)
        String availableStockSql = "SELECT f.id, f.name, f.width, " +
                "SUM(CASE WHEN b.is_sold = false THEN bi.number_of_rolls ELSE 0 END) as rolls_avail, " +
                "SUM(CASE WHEN b.is_sold = false THEN (bi.number_of_rolls * bi.meters_per_roll) ELSE 0 END) as meters_avail " +
                "FROM fabrics f " +
                "LEFT JOIN bundle_items bi ON bi.fabric_id = f.id " +
                "LEFT JOIN bundles b ON bi.bundle_id = b.id " +
                "GROUP BY f.id, f.name, f.width";

        // Map base lines into a tracking index layout map
        Map<Long, FabricStockMetricDTO> ledgerMap = new HashMap<>();

        jdbcTemplate.query(availableStockSql, rs -> {
            Long fabricId = rs.getLong("id");
            BigDecimal mAvail = rs.getBigDecimal("meters_avail");

            FabricStockMetricDTO dto = FabricStockMetricDTO.builder()
                    .fabricId(fabricId)
                    .fabricName(rs.getString("name"))
                    .fabricWidth(rs.getBigDecimal("width") != null ? rs.getDouble("width") : 0.0)
                    .rollsSold(0L)
                    .metersSold(BigDecimal.ZERO)
                    .rollsAvailable(rs.getLong("rolls_avail"))
                    .metersAvailable(mAvail != null ? mAvail.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                    .build();

            ledgerMap.put(fabricId, dto);
        });

        // 📊 PASS 2: Pull dynamic items sold parameters isolated within your calendar dates range
        String soldStockSql;
        try {
            // Test standard singular table structure first
            jdbcTemplate.execute("SELECT 1 FROM sales_bill_item LIMIT 1");

            soldStockSql = "SELECT bi_inner.fabric_id, " +
                    "SUM(bi_inner.number_of_rolls) as rolls_sold, " +
                    "SUM(bi_inner.number_of_rolls * bi_inner.meters_per_roll) as meters_sold " +
                    "FROM bundles b_inner " +
                    "JOIN bundle_items bi_inner ON bi_inner.bundle_id = b_inner.id " +
                    "JOIN sales_bill_item sbi_inner ON sbi_inner.bundle_id = b_inner.id " +
                    "JOIN sales_bills sb_inner ON sbi_inner.sales_bill_id = sb_inner.id " +
                    "WHERE sb_inner.bill_date BETWEEN ? AND ? " +
                    "GROUP BY bi_inner.fabric_id";
        } catch (Exception e) {
            // 🛡️ PLURALIZATION FALLBACK: Executes automatically if Hibernate created pluralized tables
            soldStockSql = "SELECT bi_inner.fabric_id, " +
                    "SUM(bi_inner.number_of_rolls) as rolls_sold, " +
                    "SUM(bi_inner.number_of_rolls * bi_inner.meters_per_roll) as meters_sold " +
                    "FROM bundles b_inner " +
                    "JOIN bundle_items bi_inner ON bi_inner.bundle_id = b_inner.id " +
                    "JOIN sales_bill_items sbi_inner ON sbi_inner.bundle_id = b_inner.id " + // Pluralized table fallback
                    "JOIN sales_bills sb_inner ON sbi_inner.sales_bill_id = sb_inner.id " +
                    "WHERE sb_inner.bill_date BETWEEN ? AND ? " +
                    "GROUP BY bi_inner.fabric_id";
        }

        // Safely overlay the dynamic date criteria records unto your tracker layout map
        jdbcTemplate.query(soldStockSql, dateArgs, rs -> {
            Long fabricId = rs.getLong("fabric_id");
            FabricStockMetricDTO existingDto = ledgerMap.get(fabricId);

            if (existingDto != null) {
                BigDecimal mSold = rs.getBigDecimal("meters_sold");
                existingDto.setRollsSold(rs.getLong("rolls_sold"));
                existingDto.setMetersSold(mSold != null ? mSold.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            }
        });
        // Convert the clean map values directly into a sorted array list
        List<FabricStockMetricDTO> stockLedgerList = new ArrayList<>(ledgerMap.values());
        stockLedgerList.sort((a, b) -> a.getFabricName().compareToIgnoreCase(b.getFabricName()));

        return DashboardAnalyticsDTO.builder()
                .totalInvoicesIssued(totalBills)
                .grossRevenueCollected(grossRev.setScale(2, RoundingMode.HALF_UP))
                .averageInvoiceValue(avgInvoice.setScale(2, RoundingMode.HALF_UP))
                .activeClientAccountsCount(uniqueCustomers)
                .totalBundlesProduced(totalBundles)
                .bundlesSoldInventoryCount(soldBundles)
                .stockAvailableInventoryCount(availableBundles)
                .productCategoryDistributionMatrix(distribution)
                .topCustomers(customersList)
                .fabricStockLedger(stockLedgerList) // ➕ Connected cleanly
                .build();
    }
}
//public class AnalyticsReportingService {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    @Transactional(readOnly = true)
//    public DashboardAnalyticsDTO compileCoreOperationalMetrics(String startDateStr, String endDateStr) {
//        // Enforce all-time fallback dates boundaries if parameters arrive unpopulated
//        LocalDate start = (startDateStr != null && !startDateStr.trim().isEmpty()) ? LocalDate.parse(startDateStr.trim()) : LocalDate.of(2020, 1, 1);
//        LocalDate end = (endDateStr != null && !endDateStr.trim().isEmpty()) ? LocalDate.parse(endDateStr.trim()) : LocalDate.of(2099, 12, 31);
//
//        // 📊 Financial & Invoicing Performance Counters
//        Long totalBills = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM sales_bills", Long.class);
//        BigDecimal grossRev = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(grand_total), 0) FROM sales_bills", BigDecimal.class);
//        Long uniqueCustomers = jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT customer_id) FROM sales_bills", Long.class);
//
//        // 📦 Inventory Stock Capacities
//        Long totalBundles = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM bundles", Long.class);
//        Long soldBundles = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM bundles WHERE is_sold = true", Long.class);
//        Long availableBundles = (totalBundles != null ? totalBundles : 0L) - (soldBundles != null ? soldBundles : 0L);
//
//        BigDecimal avgInvoice = (totalBills != null && totalBills > 0)
//                ? grossRev.divide(BigDecimal.valueOf(totalBills), 2, java.math.RoundingMode.HALF_UP)
//                : BigDecimal.ZERO;
//
//        // 🧵 Product Volume Distribution Map: Groups orders by FABRIC, YARN, or GRANULES
//        Map<String, Long> distribution = new HashMap<>();
//        jdbcTemplate.query(
//                "SELECT t.group_type, COUNT(b.id) FROM sales_bills b JOIN type_of_bills t ON b.type_of_bill_id = t.id GROUP BY t.group_type",
//                (rs, rowNum) -> distribution.put(rs.getString(1), rs.getLong(2))
//        );
//        distribution.putIfAbsent("FABRIC", 0L);
//        distribution.putIfAbsent("YARN", 0L);
//        distribution.putIfAbsent("GRANULES", 0L);
//
////        // 🏆 Top 5 Performing Customers Leaderboard Array
////        List<DashboardAnalyticsDTO.TopCustomerMetric> topCustomers = jdbcTemplate.query(
////                "SELECT c.name, COUNT(b.id), SUM(b.grand_total) FROM sales_bills b " +
////                        "JOIN customers c ON b.customer_id = c.id GROUP BY c.name ORDER BY SUM(b.grand_total) DESC LIMIT 5",
////                (rs, rowNum) -> new DashboardAnalyticsDTO.TopCustomerMetric(
////                        rs.getString(1), rs.getLong(2), rs.getBigDecimal(3)
////                )
////        );
////
////        // 📈 Top 5 Highest Moving Fabric Assets Groupings
////        List<DashboardAnalyticsDTO.FabricInventoryMetric> topFabrics = jdbcTemplate.query(
////                "SELECT f.name, SUM(bi.number_of_rolls), SUM(bi.number_of_rolls * bi.meters_per_roll) FROM sales_bill_items sbi " +
////                        "JOIN bundles b ON sbi.bundle_id = b.id JOIN bundle_items bi ON bi.bundle_id = b.id " +
////                        "JOIN fabrics f ON bi.fabric_id = f.id GROUP BY f.name ORDER BY SUM(bi.number_of_rolls * bi.meters_per_roll) DESC LIMIT 5",
////                (rs, rowNum) -> new DashboardAnalyticsDTO.FabricInventoryMetric(
////                        rs.getString(1), rs.getLong(2), rs.getDouble(3)
////                )
////        );
//
//        String customerSql = "SELECT c.name, COUNT(b.id) as bills_count, SUM(b.grand_total) as total_val " +
//                "FROM sales_bills b JOIN customers c ON b.customer_id = c.id " +
//                "GROUP BY c.name ORDER BY total_val DESC LIMIT 5";
//        List<TopCustomerMetricDTO> customersList = jdbcTemplate.query(customerSql, (rs, rn) ->
//                TopCustomerMetricDTO.builder()
//                        .customerName(rs.getString("name"))
//                        .totalInvoices(rs.getLong("bills_count"))
//                        .totalContribution(rs.getBigDecimal("total_val").setScale(2, RoundingMode.HALF_UP))
//                        .build()
//        );
//
//        // 🛡️ REPORT 2: FABRIC STOCK LEDGER EXTRACTION (Fixed Math and Case Typo Errors)
////        String fabricSql = "SELECT f.name, " +
////                "SUM(CASE WHEN b.is_sold = true THEN bi.number_of_rolls ELSE 0 END) as rolls_sold, " +
////                "SUM(CASE WHEN b.is_sold = true THEN (bi.number_of_rolls * bi.meters_per_roll) ELSE 0 END) as meters_sold, " +
////                "SUM(CASE WHEN b.is_sold = false THEN bi.number_of_rolls ELSE 0 END) as rolls_avail, " +
////                "SUM(CASE WHEN b.is_sold = false THEN (bi.number_of_rolls * bi.meters_per_roll) ELSE 0 END) as meters_avail " +
////                "FROM fabrics f " +
////                "LEFT JOIN bundle_items bi ON bi.fabric_id = f.id " +
////                "LEFT JOIN bundles b ON bi.bundle_id = b.id " +
////                "GROUP BY f.name ORDER BY f.name ASC";
//        String fabricSql = "SELECT f.id, f.name, f.width, " + // 🛡️ Added f.id and f.width columns to select mapping parameters
//                "SUM(CASE WHEN b.is_sold = true THEN bi.number_of_rolls ELSE 0 END) as rolls_sold, " +
//                "SUM(CASE WHEN b.is_sold = true THEN (bi.number_of_rolls * bi.meters_per_roll) ELSE 0 END) as meters_sold, " +
//                "SUM(CASE WHEN b.is_sold = false THEN bi.number_of_rolls ELSE 0 END) as rolls_avail, " +
//                "SUM(CASE WHEN b.is_sold = false THEN (bi.number_of_rolls * bi.meters_per_roll) ELSE 0 END) as meters_avail " +
//                "FROM fabrics f " +
//                "LEFT JOIN bundle_items bi ON bi.fabric_id = f.id " +
//                "LEFT JOIN bundles b ON bi.bundle_id = b.id " +
//                "GROUP BY f.id, f.name, f.width " + // 🛡️ CRITICAL FIX: Explicitly grouped all selected non-aggregate criteria fields
//                "ORDER BY f.name ASC";
//        List<FabricStockMetricDTO> stockLedgerList = jdbcTemplate.query(fabricSql, (rs, rn) -> {
//            BigDecimal mSold = rs.getBigDecimal("meters_sold");
//            BigDecimal mAvail = rs.getBigDecimal("meters_avail");
//            return FabricStockMetricDTO.builder()
//                    .fabricId(rs.getLong("id"))
//                    .fabricName(rs.getString("name"))
//                    .fabricWidth(rs.getDouble("width"))
//                    .rollsSold(rs.getLong("rolls_sold"))
//                    .metersSold(mSold != null ? mSold.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
//                    .rollsAvailable(rs.getLong("rolls_avail"))
//                    .metersAvailable(mAvail != null ? mAvail.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
//                    .build();
//        });
//
//        return DashboardAnalyticsDTO.builder()
//                .totalInvoicesIssued(totalBills)
//                .grossRevenueCollected(grossRev)
//                .averageInvoiceValue(avgInvoice)
//                .activeClientAccountsCount(uniqueCustomers)
//                .totalBundlesProduced(totalBundles)
//                .bundlesSoldInventoryCount(soldBundles)
//                .stockAvailableInventoryCount(availableBundles)
//                .productCategoryDistributionMatrix(distribution)
//                .topCustomers(customersList)      // ➕ Packed securely
//                .fabricStockLedger(stockLedgerList)
////                .topPerformingCustomers(topCustomers)
////                .highestMovingFabrics(topFabrics)
//                .build();
//    }

//    @Transactional(readOnly = true)
//    public List<FabricInventoryReportDTO> compileFabricInventoryStockReports() {
//        String sql = "SELECT f.id, f.name, f.width, " +
//                // 🔴 Sold Metric Calculations
//                "SUM(CASE WHEN b.is_sold = true THEN bi.number_of_rolls ELSE 0 END) as rolls_sold, " +
//                "SUM(CASE WHEN b.is_sold = true THEN (bi.number_of_rolls * bi.meters_per_roll) ELSE 0 END) as meters_sold, " +
//                // 🟢 Available Stock Metric Calculations
//                "SUM(CASE WHEN b.is_sold = false THEN bi.number_of_rolls ELSE 0 END) as rolls_avail, " +
//                "SUM(CASE WHEN b.is_sold = false THEN (bi.number_of_rolls * bi.meters_per_roll) ELSE 0 END) as meters_avail " +
//                "FROM fabrics f " +
//                "LEFT JOIN bundle_items bi ON bi.fabric_id = f.id " +
//                "LEFT JOIN bundles b ON bi.bundle_id = b.id " +
//                "GROUP BY f.id, f.name, f.width " +
//                "ORDER BY f.name ASC";
//
//        return jdbcTemplate.query(sql, (rs, rowNum) -> {
//            BigDecimal metersSold = rs.getBigDecimal("meters_sold");
//            BigDecimal metersAvail = rs.getBigDecimal("meters_avail");
//
//            return FabricInventoryReportDTO.builder()
//                    .fabricId(rs.getLong("id"))
//                    .fabricName(rs.getString("name"))
//                    .fabricWidth(rs.getDouble("width"))
//                    .totalRollsSold(rs.getLong("rolls_sold"))
//                    .totalMetersSold(metersSold != null ? metersSold.setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO)
//                    .totalRollsAvailable(rs.getLong("rolls_avail"))
//                    .totalMetersAvailable(metersAvail != null ? metersAvail.setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO)
//                    .build();
//        });
//    }
// }

