-- V1: Initial database structural setup configuration

-- 1. Fabric Catalog Table
CREATE TABLE fabric (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    width DECIMAL(10, 2) NOT NULL,
    cost_per_meter DECIMAL(12, 2) NOT NULL,
    CONSTRAINT uk_fabric_name_width UNIQUE (name, width)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Sales Bill Invoice Ledger
CREATE TABLE sales_bill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_number VARCHAR(100) NOT NULL,
    customer_name VARCHAR(255) NULL,
    bill_date DATE NOT NULL,
    tax_percentage DECIMAL(5, 2) NOT NULL,
    total_meters DECIMAL(12, 2) NOT NULL,
    sub_total DECIMAL(12, 2) NOT NULL,
    tax_amount DECIMAL(12, 2) NOT NULL,
    final_amount DECIMAL(12, 2) NOT NULL,
    CONSTRAINT uk_sales_bill_number UNIQUE (bill_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Production Ingestion Bundle Table
CREATE TABLE bundle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bundle_number VARCHAR(100) NOT NULL,
    fabric_id BIGINT NOT NULL,
    number_of_rolls INT NOT NULL,
    meters_per_roll DECIMAL(10, 2) NOT NULL,
    color VARCHAR(100) NOT NULL,
    manufacturer_code VARCHAR(100) NULL,
    bill_id BIGINT NULL,
    CONSTRAINT uk_bundle_number UNIQUE (bundle_number),
    CONSTRAINT fk_bundle_fabric_id FOREIGN KEY (fabric_id) REFERENCES fabric(id),
    CONSTRAINT fk_bundle_bill_id FOREIGN KEY (bill_id) REFERENCES sales_bill(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Historical Financial Snapshot Records
CREATE TABLE bill_fabric_snapshot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    fabric_name VARCHAR(255) NOT NULL,
    fabric_width DECIMAL(10, 2) NOT NULL,
    historical_cost DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_snapshot_bill_id FOREIGN KEY (bill_id) REFERENCES sales_bill(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Operational Lookups Indices
CREATE INDEX idx_bundle_search_number ON bundle(bundle_number);
CREATE INDEX idx_sales_bill_chronological ON sales_bill(bill_date);
