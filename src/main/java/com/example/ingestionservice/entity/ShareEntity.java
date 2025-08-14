package com.example.ingestionservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shares")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareEntity {
    @Id
    private String figi;
    private String ticker;
    private String name;
    private String currency;
    private String exchange;
}
