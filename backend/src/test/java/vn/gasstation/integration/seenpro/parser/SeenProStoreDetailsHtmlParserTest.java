package vn.gasstation.integration.seenpro.parser;
import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.assertThat;
class SeenProStoreDetailsHtmlParserTest {@Test void parsesReadOnlyBusinessSectionAndIgnoresEditForm(){String html="""
 <div id="thongTinCuaHang"><div class="noiDung"><div class="rowx"><div class="label">Tên Công Ty</div><div class="value">Công ty A</div></div>
 <div class="rowx"><div class="label">Mã Số Thuế</div><div class="value">123</div></div><div class="rowx"><div class="label">Tên Cửa Hàng</div><div class="value">Trạm A</div></div>
 <div class="rowx"><div class="label">Email Cửa Hàng</div><div class="value">Chưa cập nhật</div></div></div></div>
 <div id="chinhSuaThongTinCuaHang"><input name="tenCuaHang" value="Không lấy"></div>""";
 var result=new SeenProStoreDetailsHtmlParser().parse(html);assertThat(result.companyName()).isEqualTo("Công ty A");assertThat(result.taxCode()).isEqualTo("123");
 assertThat(result.storeName()).isEqualTo("Trạm A");assertThat(result.storeEmail()).isNull();}}
