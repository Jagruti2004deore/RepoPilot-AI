package com.repolens.backend.ai.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Service
public class McpContextService {
    private static final Logger log = LoggerFactory.getLogger(McpContextService.class);

    private final RestClient restClient;
    private final boolean enabled;
    private final String serverName;
    private final String endpoint;
    private final String context;

    public McpContextService(
            @Value("${app.ai.mcp.enabled:false}") boolean enabled,
            @Value("${app.ai.mcp.server-name:local-repopilot-context}") String serverName,
            @Value("${app.ai.mcp.endpoint:}") String endpoint,
            @Value("${app.ai.mcp.context:}") String context
    ) {
        this.restClient = RestClient.create();
        this.enabled = enabled;
        this.serverName = serverName;
        this.endpoint = endpoint == null ? "" : endpoint.trim();
        this.context = context == null ? "" : context.trim();
    }

    public String promptContext() {
        if (!enabled) {
            return "MCP context is disabled.";
        }

        String remoteContext = remoteContext();
        if (!remoteContext.isBlank()) {
            return "MCP server: " + serverName + "\nRemote context:\n" + remoteContext;
        }
        if (!context.isBlank()) {
            return "MCP server: " + serverName + "\nConfigured context:\n" + context;
        }
        return "MCP server " + serverName + " is enabled, but no external MCP context is configured.";
    }

    public McpStatus status() {
        return new McpStatus(enabled, serverName, !endpoint.isBlank(), !context.isBlank());
    }

    private String remoteContext() {
        if (endpoint.isBlank()) {
            return "";
        }
        try {
            String response = restClient.get()
                    .uri(endpoint)
                    .retrieve()
                    .body(String.class);
            return response == null ? "" : response.trim();
        } catch (RuntimeException ex) {
            log.debug("MCP endpoint {} did not return context within {} seconds.", endpoint, Duration.ofSeconds(5).toSeconds(), ex);
            return "";
        }
    }

    public record McpStatus(boolean enabled, String serverName, boolean endpointConfigured, boolean staticContextConfigured) {
    }
}