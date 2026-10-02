package vn.gasstation.integration.seenpro.station;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeenProStationContextVerifierTest {
    private final SeenProStationContextVerifier verifier = new SeenProStationContextVerifier();

    @Test
    void matchesSelectedStationInsideRealBusinessElementIgnoringCaseWhitespaceAndDiacritics() {
        String html = "<html><body><div class='tenCuaHang'> CUA HANG XANG DAU   HOA BINH </div></body></html>";

        assertThat(verifier.verify(html, "CỬA HÀNG XĂNG DẦU HÒA BÌNH").matches()).isTrue();
    }

    @Test
    void acceptsShortenedStationNameInsideBusinessElement() {
        String html = "<html><body><div class='tenCuaHang'>TRUNG HIEU</div></body></html>";

        assertThat(verifier.verify(html, "CÔNG TY TNHH XD & TM TRUNG HIẾU").matches()).isTrue();
    }

    @Test
    void fallsBackToBodyWhenBusinessElementIsAbsent() {
        String html = "<html><body><main>Thong tin van hanh - TRUNG HIEU</main></body></html>";

        assertThat(verifier.verify(html, "CÔNG TY TNHH XD & TM TRUNG HIẾU").matches()).isTrue();
    }

    @Test
    void rejectsDifferentStationInBusinessElementAndBody() {
        String html = "<html><body><div class='tenCuaHang'>CUA HANG XANG DAU KHAC</div></body></html>";

        assertThat(verifier.verify(html, "CỬA HÀNG XĂNG DẦU HÒA BÌNH").matches()).isFalse();
    }

    @Test
    void doesNotAcceptGenericBusinessPrefixWithoutDistinctiveStationName() {
        String html = "<html><body><div class='tenCuaHang'>CONG TY TNHH</div></body></html>";

        assertThat(verifier.verify(html, "CÔNG TY TNHH XD & TM TRUNG HIẾU").matches()).isFalse();
    }
}
