package vn.gasstation.integration.seenpro.model;

// Deliberately has no password field. navigationUrl never crosses the integration boundary.
public record SeenProCompanyModel(String legacyAccount, String name, String phone,
                                  String email, String navigationUrl) {}

