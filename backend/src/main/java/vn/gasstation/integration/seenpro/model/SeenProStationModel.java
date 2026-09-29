package vn.gasstation.integration.seenpro.model;

public record SeenProStationModel(
    String account,
    String name,
    String phone,
    String email,
    String viewUrl,
    String manageUrl
) {}
