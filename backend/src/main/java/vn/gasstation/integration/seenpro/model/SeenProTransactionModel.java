package vn.gasstation.integration.seenpro.model;

public record SeenProTransactionModel(String pumpCode, String dispenser, String fuelLabel,
    String unitPriceText, String litersText, String amountText, String completedAtText,
    String customerText, String invoiceStateText, String invoiceNumberText) {
    public SeenProTransactionModel(String legacyId, String pumpCode, String dispenser, String fuelLabel,
        String unitPriceText, String litersText, String amountText, String completedAtText,
        String customerText, String invoiceStateText, String invoiceNumberText) {
        this(pumpCode, dispenser, fuelLabel, unitPriceText, litersText, amountText, completedAtText,
            customerText, invoiceStateText, invoiceNumberText);
    }
}

