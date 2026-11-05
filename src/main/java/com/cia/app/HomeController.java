package com.cia.app;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @Value("${app.version}")
    private String version;

    @Value("${app.message}")
    private String message;

    @Value("${BUILD_SHA:local}")
    private String buildSha;

    @Value("${HOSTNAME:localhost}")
    private String podName;

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return """
            <!doctype html><html><head><meta charset="utf-8"><title>CIA 2 DevOps App</title>
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <style>body{font-family:system-ui,sans-serif;background:#0f172a;color:#e2e8f0;display:grid;place-items:center;min-height:100vh;margin:0}
            .card{background:#1e293b;padding:2rem 2.5rem;border-radius:12px;max-width:520px}h1{margin-top:0;color:#38bdf8}
            td{padding:.3rem 1rem .3rem 0}td:first-child{color:#94a3b8}</style></head>
            <body><div class="card"><h1>%s</h1><table>
            <tr><td>Version</td><td><b>%s</b></td></tr>
            <tr><td>Build commit</td><td>%s</td></tr>
            <tr><td>Served by pod</td><td>%s</td></tr>
            <tr><td>Pipeline</td><td>Git &rarr; Maven &rarr; Docker &rarr; ACR &rarr; AKS</td></tr>
            </table></div></body></html>
            """.formatted(message, version, buildSha, podName);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "version", version);
    }
}
