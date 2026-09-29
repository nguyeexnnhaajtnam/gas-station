package vn.gasstation.company.api;
import vn.gasstation.company.domain.Company;
import vn.gasstation.company.domain.CompanyStatus;
public record CompanyResponse(String id, String name, String code, String phone, String email, CompanyStatus status) {
    static CompanyResponse from(Company company) { return new CompanyResponse(company.id(), company.name(), company.code(), company.phone(), company.email(), company.status()); }
}

