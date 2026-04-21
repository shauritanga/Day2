package com.zhsf.hospital.config;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SwaggerUiController {

    @GetMapping(value = {"/", "/swagger-ui.html", "/swagger-ui/"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String swaggerUi() {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <title>CMIS Hospital Service API Docs</title>
                  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5.32.2/swagger-ui.css">
                  <style>
                    body { margin: 0; background: #f7f7f7; }
                    .topbar { display: none; }
                    .cmis-header {
                      padding: 14px 24px;
                      background: #17324d;
                      color: white;
                      font-family: Arial, sans-serif;
                    }
                    .cmis-header strong { font-size: 18px; }
                    .cmis-note { margin-top: 4px; font-size: 13px; color: #d9e8f5; }
                  </style>
                </head>
                <body>
                  <div class="cmis-header">
                    <strong>CMIS Hospital Service API Docs</strong>
                    <div class="cmis-note">Extracted hospital endpoints served from hospital-service.</div>
                  </div>
                  <div id="swagger-ui"></div>
                  <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5.32.2/swagger-ui-bundle.js"></script>
                  <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5.32.2/swagger-ui-standalone-preset.js"></script>
                  <script>
                    SwaggerUIBundle({
                      url: '/v3/api-docs',
                      dom_id: '#swagger-ui',
                      presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
                      layout: 'BaseLayout'
                    });
                  </script>
                </body>
                </html>
                """;
    }
}
