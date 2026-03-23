package com._404s.attireflow.security;

public enum AppRole {
    ADMIN("Admin"),
    INVENTORY_MANAGER("Inventory Manager"),
    DELIVERY_MANAGER("Delivery Manager");

    private final String displayName;

    AppRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}