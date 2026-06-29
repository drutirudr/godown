package com.shyam.kamak.godown.components;

import com.shyam.kamak.godown.model.Bundle;
import com.shyam.kamak.godown.model.BundleItem;
import com.shyam.kamak.godown.model.SalesBill;
import com.shyam.kamak.godown.model.SalesBillItem;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component // 💡 This tells Spring Boot to create a managed bean for this class automatically
public class SalesBillCalculationEngine {

    public SalesBillItem buildSalesBillItem(SalesBill salesBill, Bundle bundle) {
        int totalRolls = bundle.getItems().stream()
                .mapToInt(BundleItem::getNumberOfRolls)
                .sum();

        BigDecimal totalMeters = bundle.getItems().stream()
                .map(item -> BigDecimal.valueOf(item.getNumberOfRolls()).multiply(item.getMetersPerRoll()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal itemSubtotal = bundle.getItems().stream()
                .map(item -> {
                    BigDecimal meters = BigDecimal.valueOf(item.getNumberOfRolls()).multiply(item.getMetersPerRoll());
                    BigDecimal priceToApply;

                    if (salesBill.getId() == null) {
                        priceToApply = item.getFabric().getCurrentCostPerMeter();
                        item.setFrozenCostPerMeter(priceToApply);
                    } else {
                        priceToApply = item.getFrozenCostPerMeter();
                    }

                    return meters.multiply(priceToApply);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SalesBillItem.builder()
                .salesBill(salesBill)
                .bundle(bundle)
                .totalRolls(totalRolls)
                .totalMeters(totalMeters)
                .subtotal(itemSubtotal)
                .build();
    }

    public void applyFinancialCalculations(SalesBill salesBill) {
        BigDecimal runningSubtotal = salesBill.getItems().stream()
                .map(SalesBillItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        salesBill.setSubtotalAmount(runningSubtotal);

        BigDecimal discountAmount = calculateDeduction(
                runningSubtotal, salesBill.getDiscountType(), salesBill.getDiscountRate()
        );
        salesBill.setDiscountAmount(discountAmount);

        BigDecimal netAfterDiscount = runningSubtotal.subtract(discountAmount);
        if (netAfterDiscount.compareTo(BigDecimal.ZERO) < 0) netAfterDiscount = BigDecimal.ZERO;

        BigDecimal taxAmount = calculateDeduction(
                netAfterDiscount, salesBill.getTaxType(), salesBill.getTaxRate()
        );
        salesBill.setTaxAmount(taxAmount);

        salesBill.setGrandTotal(netAfterDiscount.add(taxAmount));
    }

    private BigDecimal calculateDeduction(BigDecimal base, SalesBill.CalculationType type, BigDecimal rate) {
        if (base == null || rate == null) return BigDecimal.ZERO;
        return type == SalesBill.CalculationType.PERCENT
                ? base.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : rate;
    }
}