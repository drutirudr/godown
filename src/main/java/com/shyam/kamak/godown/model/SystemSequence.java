package com.shyam.kamak.godown.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_sequences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemSequence {
    @Id
    @Column(name = "sequence_key", length = 50)
    private String sequenceKey;

    @Column(name = "next_value", nullable = false)
    private Integer nextValue = 1;
}

//@Entity @Table(name = "system_sequences")
//@Getter @Setter @NoArgsConstructor @AllArgsConstructor
//public class SystemSequence {
//    @Id @Column(name = "sequence_key", length = 50) private String sequenceKey;
//    @Column(name = "next_value", nullable = false) private Integer nextValue = 1;
//}
