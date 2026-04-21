package com.zhsf.gateway;

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
                  <title>CMIS API Docs</title>
                  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5.32.2/swagger-ui.css">
                  <style>
                    body { margin: 0; background: #f7f7f7; }
                    .topbar { display: none; }
                    .cmis-header {
                      display: flex;
                      align-items: center;
                      gap: 16px;
                      flex-wrap: wrap;
                      padding: 14px 24px;
                      background: #17324d;
                      color: white;
                      font-family: Arial, sans-serif;
                    }
                    .cmis-title { font-size: 18px; }
                    .cmis-tabs { display: flex; gap: 8px; }
                    .cmis-tab {
                      border: 1px solid #9db3c7;
                      background: #ffffff;
                      color: #17324d;
                      border-radius: 4px;
                      padding: 8px 12px;
                      cursor: pointer;
                      font-weight: 600;
                    }
                    .cmis-tab.active {
                      background: #7cc6ff;
                      border-color: #7cc6ff;
                    }
                    .cmis-note {
                      width: 100%;
                      font-size: 13px;
                      color: #d9e8f5;
                    }
                  </style>
                </head>
                <body>
                  <div class="cmis-header">
                    <strong class="cmis-title">CMIS API Docs</strong>
                    <div class="cmis-tabs">
                      <button class="cmis-tab active" data-url="/docs/hospital/v3/api-docs">
                        Hospital Service - refactored
                      </button>
                      <button class="cmis-tab" data-url="/docs/monolith/v3/api-docs">
                        Monolith - not yet refactored
                      </button>
                    </div>
                    <div id="cmis-note" class="cmis-note">
                      Showing Hospital Service - refactored. Use the buttons above to switch API docs.
                    </div>
                  </div>
                  <div id="swagger-ui"></div>
                  <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5.32.2/swagger-ui-bundle.js"></script>
                  <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5.32.2/swagger-ui-standalone-preset.js"></script>
                  <script>
                    let ui;
                    function loadDocs(url) {
                      document.getElementById('swagger-ui').innerHTML = '';
                      ui = null;
                      SwaggerUIBundle({
                        url: url,
                        dom_id: '#swagger-ui',
                        presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
                        layout: 'BaseLayout'
                      });
                    }
                    const tabs = document.querySelectorAll('.cmis-tab');
                    const note = document.getElementById('cmis-note');
                    tabs.forEach(function (tab) {
                      tab.addEventListener('click', function () {
                        tabs.forEach(function (item) { item.classList.remove('active'); });
                        tab.classList.add('active');
                        note.textContent = 'Showing ' + tab.textContent.trim() + '. Use the buttons above to switch API docs.';
                        loadDocs(tab.dataset.url);
                      });
                    });
                    loadDocs('/docs/hospital/v3/api-docs');
                  </script>
                </body>
                </html>
                """;
    }
}
