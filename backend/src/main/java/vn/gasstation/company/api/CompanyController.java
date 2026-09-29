package vn.gasstation.company.api;
import org.springframework.web.bind.annotation.*;
import vn.gasstation.company.application.CompanyService;
import java.util.List;
@RestController @RequestMapping("/api/v1/companies")
public class CompanyController {
    private final CompanyService service;
    public CompanyController(CompanyService service) { this.service = service; }
    public record CompanyListResponse(List<CompanyResponse> items) {}
    @GetMapping public CompanyListResponse all() {
        return new CompanyListResponse(service.findAll().stream().map(CompanyResponse::from).toList());
    }
}

