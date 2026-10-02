package vn.gasstation.integration.seenpro.dev;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev")
@RequestMapping("/api/v1/dev/seenpro/explorer")
public class SeenProDevExplorerController {
    private final SeenProDevExplorer explorer;

    public SeenProDevExplorerController(SeenProDevExplorer explorer) {
        this.explorer = explorer;
    }

    @GetMapping("/online-scripts")
    public SeenProDevExplorer.Exploration onlineScripts() {
        return explorer.exploreOnlineJavaScript();
    }
}
