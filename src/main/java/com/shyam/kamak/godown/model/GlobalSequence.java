package com.shyam.kamak.godown.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "global_sequences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSequence {
    @Id
    private String entityName;
    private String financialYear;
    private int runningNumber;
}
