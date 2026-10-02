package vn.gasstation.integration.seenpro.online;

/**
 * SeenPro-only polling descriptor. Legacy transport identifiers must never cross the adapter boundary.
 */
public record SeenProPumpDescriptor(
    String pumpCode,
    String pumpName,
    String standardizedMAC,
    String master,
    String slave,
    String fuelId,
    String user,
    String connectElementId,
    String pumpElementId,
    String initialPumpState
) {}
