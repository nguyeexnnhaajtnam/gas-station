package vn.gasstation.integration.seenpro.model;

public record SeenProTransactionModel(String legacyId, String pumpCode, String pumpColumn, String fuelLabel,
    String unitPriceText, String litersText, String amountText, String completedAtText,
    String customerText, String invoiceStateText, String invoiceNumberText) {}

