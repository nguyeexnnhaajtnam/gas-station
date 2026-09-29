package vn.gasstation.integration.seenpro.auth;

import java.net.http.HttpHeaders;

public record SeenProAuthResponse(int statusCode, HttpHeaders headers, String body) {}

