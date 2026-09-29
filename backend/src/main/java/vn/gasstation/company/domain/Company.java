package vn.gasstation.company.domain;

public record Company(String id, String name, String code, String phone, String email, CompanyStatus status) {}

