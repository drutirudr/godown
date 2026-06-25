package com.shyam.kamak.godown.dto;

import com.shyam.kamak.godown.model.TypeOfBill.BillGroupType;
import lombok.Data;

@Data
public class TypeOfBillResponseDTO {
    private Long id;
    private String name;
    private String code;
    private BillGroupType groupType;
}

