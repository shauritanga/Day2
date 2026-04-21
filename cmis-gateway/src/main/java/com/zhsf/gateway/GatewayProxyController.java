package com.zhsf.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Set;

@RestController
public class GatewayProxyController {

    private static final Logger log = LoggerFactory.getLogger(GatewayProxyController.class);

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade",
            "host",
            "content-length"
    );

    private final GatewayRouteProperties routeProperties;
    private final RestClient restClient;

    public GatewayProxyController(GatewayRouteProperties routeProperties, RestClient restClient) {
        this.routeProperties = routeProperties;
        this.restClient = restClient;
    }

    @RequestMapping("/api/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request,
                                        @RequestBody(required = false) byte[] body) {
        String path = request.getRequestURI();
        if (isInternalRoute(path)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String upstreamBaseUrl = upstreamBaseUrl(path);

        URI targetUri = buildTargetUri(upstreamBaseUrl, request);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        log.info("Routing {} {} -> {}", method, path, upstreamBaseUrl);

        RestClient.RequestBodySpec requestSpec = restClient.method(method)
                .uri(targetUri)
                .headers(headers -> copyRequestHeaders(request, headers));

        if (body != null && body.length > 0) {
            requestSpec.body(body);
        }

        return requestSpec.exchange((clientRequest, clientResponse) -> {
            HttpHeaders responseHeaders = new HttpHeaders();
            clientResponse.getHeaders().forEach((name, values) -> {
                if (!isHopByHopHeader(name)) {
                    responseHeaders.put(name, values);
                }
            });

            return new ResponseEntity<>(
                    StreamUtils.copyToByteArray(clientResponse.getBody()),
                    responseHeaders,
                    clientResponse.getStatusCode()
            );
        });
    }

    private boolean isHospitalRoute(String path) {
        return path.equals("/api/hospitals") || path.startsWith("/api/hospitals/");
    }

    private boolean isClaimsRoute(String path) {
        return path.equals("/api/claims") || path.startsWith("/api/claims/");
    }

    private boolean isInternalRoute(String path) {
        return path.startsWith("/api/hospitals/internal/")
                || path.startsWith("/api/members/internal/")
                || path.startsWith("/api/claims/internal/");
    }

    private String upstreamBaseUrl(String path) {
        if (isHospitalRoute(path)) {
            return routeProperties.hospitalServiceUrl();
        }

        // Instructor solution for the next refactoring exercise:
        // Uncomment this block after claims-service is running on port 8083.
        // Then /api/claims/** will route to claims-service instead of the monolith.
        //
        // if (isClaimsRoute(path)) {
        //     return routeProperties.claimsServiceUrl();
        // }

        return routeProperties.monolithUrl();
    }

    private URI buildTargetUri(String upstreamBaseUrl, HttpServletRequest request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(upstreamBaseUrl)
                .path(request.getRequestURI());

        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            builder.query(request.getQueryString());
        }

        return builder.build(true).toUri();
    }

    private void copyRequestHeaders(HttpServletRequest request, HttpHeaders headers) {
        request.getHeaderNames().asIterator().forEachRemaining(name -> {
            if (!isHopByHopHeader(name)) {
                request.getHeaders(name).asIterator()
                        .forEachRemaining(value -> headers.add(name, value));
            }
        });
    }

    private boolean isHopByHopHeader(String headerName) {
        return HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase());
    }
}
