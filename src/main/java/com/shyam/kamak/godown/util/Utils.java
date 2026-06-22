package com.shyam.kamak.godown.util;

import com.shyam.kamak.godown.exception.BusinessException;
import com.shyam.kamak.godown.exception.ResourceNotFoundException;
import com.shyam.kamak.godown.model.BillFabricSnapshot;
import com.shyam.kamak.godown.model.Bundle;
import com.shyam.kamak.godown.model.Fabric;
import com.shyam.kamak.godown.model.SalesBill;
import com.shyam.kamak.godown.repository.BundleRepository;
import com.shyam.kamak.godown.repository.SalesBillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import java.time.LocalDate;

public class Utils {
    public static String getCurrentFinancialYear() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        // Indian Financial Year begins April 1st
        if (now.getMonthValue() < 4) {
            return (currentYear - 1) + "-" + String.format("%02d", (currentYear % 100));
        }
        return currentYear + "-" + String.format("%02d", ((currentYear + 1) % 100));
    }

}