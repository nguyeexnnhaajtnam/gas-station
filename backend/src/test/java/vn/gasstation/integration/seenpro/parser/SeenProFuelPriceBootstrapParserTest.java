package vn.gasstation.integration.seenpro.parser;
import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.assertThat;
class SeenProFuelPriceBootstrapParserTest {@Test void extractsOneDescriptorPerFuelWithoutDuplicatingTwoPollingCalls(){String html="""
 <script>setInterval("waitTimeUpdate('trunghieuna', 'DO 0,05S', 'DO 0,05S')",1000);
 setInterval("giaUpdate('trunghieuna', 'DO 0,05S', 'DO 0,05S-G')",1000);
 setInterval("waitTimeUpdate('trunghieuna', 'E10 RON 95-III', 'E10 RON 95-III')",1000);
 setInterval("giaUpdate('trunghieuna', 'E10 RON 95-III', 'E10 RON 95-III-G')",1000);</script>""";
 var result=new SeenProFuelPriceBootstrapParser().parse(html);assertThat(result).hasSize(2);
 assertThat(result.getFirst().user()).isEqualTo("trunghieuna");assertThat(result).extracting(r->r.fuelName()).containsExactly("DO 0,05S","E10 RON 95-III");}}
