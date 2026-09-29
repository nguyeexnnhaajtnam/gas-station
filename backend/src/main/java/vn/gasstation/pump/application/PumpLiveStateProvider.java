package vn.gasstation.pump.application;
import vn.gasstation.pump.domain.PumpLiveState;
import java.util.List;
public interface PumpLiveStateProvider { List<PumpLiveState> currentStates(); }

