package vn.gasstation.integration.seenpro.online;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeenProOnlineParserTest {
    private final SeenProOnlineParser parser = new SeenProOnlineParser();

    @Test
    void mapsTheConfirmedNineArgumentBootstrapContract() {
        String html = """
            <html><body>
              <article><h3>Cột 01</h3><span id='pump-01'></span></article>
              <script>
                getValue1('AA:BB','CB01','operator','RON95','1','2','connect-01','pump-01','idle');
                getValue2('CC:DD','CB02','operator','DO','3','4','connect-02','pump-02','fueling');
              </script>
            </body></html>
            """;

        var descriptors = parser.parse(html);

        assertThat(descriptors).hasSize(2);
        assertThat(descriptors.get(0)).isEqualTo(new SeenProPumpDescriptor(
            "CB01", "Cột 01", "AA:BB", "1", "2", "RON95", "operator",
            "connect-01", "pump-01", "idle"));
        assertThat(descriptors.get(1)).isEqualTo(new SeenProPumpDescriptor(
            "CB02", "CB02", "CC:DD", "3", "4", "DO", "operator",
            "connect-02", "pump-02", "fueling"));
    }

    @Test
    void ignoresRecurringInvocationInsideFunctionAndCreatesOnePhysicalPump() {
        String html = """
            <script>
              function getValue1(standMAC,maCot,nguoiQL,maNL,macmaster,macslave,idConnect,idPump,trangThaix) {
                setTimeout("getValue1('" + standMAC + "','" + maCot + "','" + nguoiQL + "','" +
                  maNL + "','" + macmaster + "','" + macslave + "','" + idConnect + "','" +
                  idPump + "','" + trangThaix + "')", 1000);
              }
              getValue1('AA:BB','CB01','operator','RON95','1','2','connect-01','pump-01','idle');
            </script>
            """;

        assertThat(parser.parse(html)).containsExactly(new SeenProPumpDescriptor(
            "CB01", "CB01", "AA:BB", "1", "2", "RON95", "operator",
            "connect-01", "pump-01", "idle"));
    }

    @Test
    void prefersStandalonePumpBootstrapScriptsOverRuntimeInvocations() {
        String html = """
            <script>
              function getValue1(standMAC,maCot,nguoiQL,maNL,macmaster,macslave,idConnect,idPump,trangThaix) {
                setTimeout("getValue1('" + standMAC + "','" + maCot + "','" + nguoiQL + "','" +
                  maNL + "','" + macmaster + "','" + macslave + "','" + idConnect + "','" +
                  idPump + "','" + trangThaix + "')", 1000);
              }
              getValue1('AA:BB','CB01','operator','RON95','1','2','connect-01','pump-01','idle');
            </script>
            <script>
              getValue1('AA:BB','CB01','operator','RON95','1','2','connect-01','pump-01','idle');
            </script>
            """;

        assertThat(parser.parse(html)).containsExactly(new SeenProPumpDescriptor(
            "CB01", "CB01", "AA:BB", "1", "2", "RON95", "operator",
            "connect-01", "pump-01", "idle"));
    }

    @Test
    void createsOneDescriptorWhenOneBootstrapScriptSchedulesTheSamePumpTwice() {
        String html = """
            <script>
              getValue1('AA:BB','CB01','operator','RON95','1','2','connect-01','pump-01','idle');
              setTimeout("getValue1('AA:BB','CB01','operator','RON95','1','2','connect-01','pump-01','idle')", 1000);
            </script>
            """;

        assertThat(parser.parse(html)).containsExactly(new SeenProPumpDescriptor(
            "CB01", "CB01", "AA:BB", "1", "2", "RON95", "operator",
            "connect-01", "pump-01", "idle"));
    }

    @Test
    void keepsDifferentPumpInvocationsFromTheSameBootstrapScript() {
        String html = """
            <script>
              getValue1('AA:BB','CB01','operator','RON95','1','2','connect-01','pump-01','idle');
              getValue2('CC:DD','CB02','operator','DO','3','4','connect-02','pump-02','fueling');
            </script>
            """;

        assertThat(parser.parse(html)).extracting(SeenProPumpDescriptor::pumpCode)
            .containsExactly("CB01", "CB02");
    }

    @Test
    void acceptsUnknownTrailingExtensionArguments() {
        String html = """
            <script>getValueABC('AA','CB09','user','E10','1','2','c9','p9','idle','future')</script>
            """;

        assertThat(parser.parse(html)).containsExactly(new SeenProPumpDescriptor(
            "CB09", "CB09", "AA", "1", "2", "E10", "user", "c9", "p9", "idle"));
    }

    @Test
    void failsWhenRequiredBootstrapArgumentsAreMissing() {
        String html = "<script>getValue1('AA','CB01')</script>";

        assertThatThrownBy(() -> parser.parse(html))
            .isInstanceOfSatisfying(SeenProOnlineBootstrapIncompleteException.class, error ->
                assertThat(error.missingIdentifiers())
                    .containsExactly("getValueRequiredArguments(minimum=9,actual=2)"));
    }

    @Test
    void failsWhenNoBootstrapInvocationExists() {
        String html = "<script>function helper() { return true; }</script>";

        assertThatThrownBy(() -> parser.parse(html))
            .isInstanceOfSatisfying(SeenProOnlineBootstrapIncompleteException.class, error ->
                assertThat(error.missingIdentifiers()).containsExactly("getValueInvocation"));
    }
}
