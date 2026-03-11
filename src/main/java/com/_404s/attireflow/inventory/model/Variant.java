package com._404s.attireflow.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "variant")
public class Variant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariantStock> stocks = new ArrayList<>();

    public Variant() {}

    public Variant(String title, String category, String size, String color, BigDecimal unitPrice) {
        this.title = title;
        this.category = category;
        this.size = size;
        this.color = color;
        this.unitPrice = unitPrice;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public List<VariantStock> getStocks() { return stocks; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setSize(String size) { this.size = size; }
    public void setColor(String color) { this.color = color; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}