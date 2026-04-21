package com.zhsf.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.net.URI;

@RestController
public class OpenApiDocsProxyController {

    private final GatewayRouteProperties routeProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OpenApiDocsProxyController(GatewayRouteProperties routeProperties,
                                      RestClient restClient,
                                      ObjectMapper objectMapper) {
        this.routeProperties = routeProperties;
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/docs/hospital/v3/api-docs")
    public ResponseEntity<byte[]> hospitalOpenApiDocs(HttpServletRequest request) {
        return fetchOpenApiDocs(routeProperties.hospitalServiceUrl(), gatewayBaseUrl(request));
    }

    @GetMapping("/docs/monolith/v3/api-docs")
    public ResponseEntity<byte[]> monolithOpenApiDocs(HttpServletRequest request) {
        return fetchOpenApiDocs(routeProperties.monolithUrl(), gatewayBaseUrl(request));
    }

    private ResponseEntity<byte[]> fetchOpenApiDocs(String upstreamBaseUrl, String gatewayBaseUrl) {
        URI uri = URI.create(upstreamBaseUrl + "/v3/api-docs");
        return restClient.get()
                .uri(uri)
                .exchange((request, response) -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    return new ResponseEntity<>(
                            rewriteServers(response.getBody().readAllBytes(), gatewayBaseUrl),
                            headers,
                            response.getStatusCode()
                    );
                });
    }

    private byte[] rewriteServers(byte[] openApiJson, String gatewayBaseUrl) throws java.io.IOException {
        ObjectNode root = (ObjectNode) objectMapper.readTree(openApiJson);
        ArrayNode servers = objectMapper.createArrayNode();
        ObjectNode gatewayServer = objectMapper.createObjectNode();
        gatewayServer.put("url", gatewayBaseUrl);
        gatewayServer.put("description", "Gateway URL");
        servers.add(gatewayServer);
        root.set("servers", servers);
        return objectMapper.writeValueAsBytes(root);
    }

    private String gatewayBaseUrl(HttpServletRequest request) {
        int port = request.getServerPort();
        boolean defaultPort = ("http".equals(request.getScheme()) && port == 80)
                || ("https".equals(request.getScheme()) && port == 443);

        return request.getScheme() + "://" + request.getServerName()
                + (defaultPort ? "" : ":" + port);
    }
}
