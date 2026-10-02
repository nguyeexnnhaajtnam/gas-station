package vn.gasstation.integration.seenpro.model;

import vn.gasstation.integration.seenpro.online.SeenProPumpDescriptor;

public record SeenProOnlinePayload(
    SeenProPumpDescriptor descriptor,
    String money,
    String liters,
    String prices,
    String totals,
    String connectionStates,
    String pumpStates
) {}
