package com.shyam.kamak.godown.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bundles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bundle extends UserAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bundle_number", nullable = false, length = 50)
    private String bundleNumber;

    @Column(name = "bundle_date", nullable = false)
    private LocalDate bundleDate;

    @Column(name = "manufacturer_code", nullable = false, length = 50)
    private String manufacturerCode;

    @Column(name = "is_sold", nullable = false)
    private boolean isSold = false;

    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BundleItem> items = new ArrayList<>();

    @Version
    private Long version;

    @Transient
    public String getFinancialYear() {
        if (this.bundleDate == null) return null;

        int year = this.bundleDate.getYear();
        int month = this.bundleDate.getMonthValue();

        if (month < 4) {
            return "FY" + ((year - 1) % 100) + "-" + (year % 100);
        } else {
            return "FY" + (year % 100) + "-" + ((year + 1) % 100);
        }
    }

    public void addItem(BundleItem item) {
        items.add(item);
        item.setBundle(this);
    }

    public void removeItem(BundleItem item) {
        items.remove(item);
        item.setBundle(null);
    }
}