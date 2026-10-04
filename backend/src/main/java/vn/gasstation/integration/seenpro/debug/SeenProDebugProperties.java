package vn.gasstation.integration.seenpro.debug;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("seenpro.debug")
public record SeenProDebugProperties(boolean captureHtml) {}
