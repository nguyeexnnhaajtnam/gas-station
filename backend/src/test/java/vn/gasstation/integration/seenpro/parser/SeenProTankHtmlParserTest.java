package vn.gasstation.integration.seenpro.parser;
import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.assertThat;
class SeenProTankHtmlParserTest {@Test void parsesConfirmedTankCards(){String html="""
 <html><body><div class="noiDung2"><div class="boxBon"><div class="tenBon"><b>BỒN 1 - E10</b></div><div class="tonKho">Tồn kho ước tính: -199357.265 lít</div><a href="caidat.php?mb=1">Cài đặt</a></div>
 <div class="boxBon"><div class="tenBon"><b>BỒN 2 - DO 0,05S-II</b></div><div class="tonKho">Tồn kho ước tính: -162025.71 lít</div><a href="nhaphang.php?mb=2">Nhập hàng</a></div></div></body></html>""";
 var rows=new SeenProTankHtmlParser().parse(html);assertThat(rows).hasSize(2);assertThat(rows.getFirst().tankId()).isEqualTo("1");
 assertThat(rows.getFirst().tankName()).isEqualTo("BỒN 1");assertThat(rows.getFirst().fuelName()).isEqualTo("E10");assertThat(rows.getFirst().estimatedVolumeText()).isEqualTo("-199357.265");}}
