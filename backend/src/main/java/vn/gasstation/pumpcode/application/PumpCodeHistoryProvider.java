package vn.gasstation.pumpcode.application;
import org.springframework.data.domain.Page;
import vn.gasstation.pumpcode.domain.PumpCodeHistory;
public interface PumpCodeHistoryProvider { Page<PumpCodeHistory> find(PumpCodeHistorySearchRequest request); }
