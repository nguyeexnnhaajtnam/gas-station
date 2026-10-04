package vn.gasstation.integration.seenpro.parser;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SeenProPumpCodeHistoryHtmlParserTest {
    @Test void parsesPumpCodeRowsFromLegacyDivGrid() {
        String html="""
            <html><head><title>Theo dõi bán hàng</title></head><body>
              <div class="boxTong"><div class="rowx rowxheader"><div class="maBom">Mã bơm</div></div></div>
              <div class="boxMaBom">
                <div class="rowx color4">
                  <div class="maBom"><p>CB01-1235-00002</p></div>
                  <div class="tenCotBom"><p>Cột 01</p></div>
                  <div class="nhienLieu"><p>Xăng E10 RON 95 Mức 3</p></div>
                  <div class="donGia"><p>27.720</p></div>
                  <div class="soLit"><p>1,08</p></div>
                  <div class="thanhTien"><p>30.000</p></div>
                  <div class="thoiGianKetThucBom"><p>21:31:59<br>03/10/2026</p></div>
                  <div class="khachHang"><p>Bán cho người tiêu dùng</p></div>
                  <div class="trangThaiHD"><p>-</p></div>
                  <div class="eHD"><p>001</p></div>
                </div>
              </div>
            </body></html>""";
        var rows=new SeenProPumpCodeHistoryHtmlParser().parse(html);
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().pumpCode()).isEqualTo("CB01-1235-00002");
        assertThat(rows.getFirst().dispenser()).isEqualTo("Cột 01");
        assertThat(rows.getFirst().amountText()).isEqualTo("30.000");
        assertThat(rows.getFirst().completedAtText()).isEqualTo("21:31:59 03/10/2026");
    }
    @Test void ignoresDocumentWithoutPumpCodeRoot() {
        assertThat(new SeenProPumpCodeHistoryHtmlParser().parse("<div class='rowx color4'>X</div>")).isEmpty();
    }
}
