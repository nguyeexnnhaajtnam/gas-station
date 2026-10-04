package vn.gasstation.integration.seenpro.parser;
import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.assertThat;
class SeenProPumpColumnHtmlParserTest {
    @Test void parsesPumpColumnCardByBusinessLabels(){String html="""
        <html><head><title>Danh Sách Cột Bơm</title></head><body><div class="boxContent"><div class="cotBom">
        <a href="change-cotbom.php?maCot=CB01">Sửa</a><div class="cotBomRight">
        <div class="row1"><div class="rowLeft">Địa Chỉ MAC</div><div class="rowRight">NA:TH:TH:TH:TH:11</div></div>
        <div class="row1"><div class="rowLeft">Tên Cột</div><div class="rowRight">Cột 01</div></div>
        <div class="row1"><div class="rowLeft">Nhiên Liệu</div><div class="rowRight">E10 RON 95-III</div></div>
        <div class="row1"><div class="rowLeft">Hiển thị</div><div class="rowRight">Chưa cài đặt</div></div>
        <div class="row1"><div class="rowLeft">Số Serial</div><div class="rowRight">Chưa cài đặt</div></div>
        <div class="row1"><div class="rowLeft">Kết Nối</div><div class="rowRight">OFFLINE</div></div>
        <div class="row1"><div class="rowLeft">Total</div><div class="rowRight">251272,75</div></div>
        </div></div></div></body></html>""";
        var rows=new SeenProPumpColumnHtmlParser().parse(html);
        assertThat(rows).hasSize(1);assertThat(rows.getFirst().pumpCode()).isEqualTo("CB01");
        assertThat(rows.getFirst().name()).isEqualTo("Cột 01");assertThat(rows.getFirst().fuelLabel()).isEqualTo("E10 RON 95-III");
        assertThat(rows.getFirst().deviceAddress()).isEqualTo("NA:TH:TH:TH:TH:11");
    }
}
