package com.shyam.kamak.godown.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "type_of_bills", uniqueConstraints = {
        @UniqueConstraint(name = "uk_name_code_group", columnNames = {"name", "code", "group_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeOfBill {

    public enum BillGroupType { FABRIC, YARN, GRANULES }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", nullable = false, length = 30)
    private BillGroupType groupType;
}
