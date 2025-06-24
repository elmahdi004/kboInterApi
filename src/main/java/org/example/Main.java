package org.example;

import be.fgov.economie.kbopub.webservices.v1.WSKBOPub;
import be.fgov.economie.kbopub.webservices.v1.WSKBOPubService;
import be.fgov.economie.kbopub.webservices.v1.messages.*;
import be.fgov.economie.kbopub.wsclient.service.SettingsService;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

//Endpoint : http://localhost:8080/readEnterprise?langue=fr&entrepriseNumber=782372801

public class Main {

    public static void main(String[] args) throws IOException {

        // Set up a basic HTTP server
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/readEnterprise", new ReadEnterpriseHandler());
        server.setExecutor(null); // Use the default executor
        System.out.println("Server is running on http://localhost:"+port);
        server.start();
        server.createContext("/", exchange -> {
            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
    }

    // HTTP Handler for /readEnterprise
    static class ReadEnterpriseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Received request to /readEnterprise");
            String apiKey = exchange.getRequestHeaders().getFirst("API_KEY");
            System.out.println("API_KEY header: " + apiKey);

            if (apiKey == null || !apiKey.equals(ApiKeyConfig.SECRET_API_KEY)) {
                System.out.println("Unauthorized: API key missing or invalid");
                String errorResponse = "Unauthorized";
                exchange.sendResponseHeaders(401, errorResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(errorResponse.getBytes());
                os.close();
                return;
            }
            System.out.println("API key validated");

            if ("GET".equals(exchange.getRequestMethod())) {
                // Parse query parameters
                Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
                System.out.println("Query params: " + queryParams);

                String langue = queryParams.get("langue");
                String entrepriseNumberStr = queryParams.get("entrepriseNumber");

                if (langue == null || entrepriseNumberStr == null) {
                    System.out.println("Missing required parameters");
                    String errorResponse = "Missing required parameters: langue and entrepriseNumber.";
                    exchange.sendResponseHeaders(400, errorResponse.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(errorResponse.getBytes());
                    os.close();
                    return;
                }

                try {
                    int entrepriseNumber = Integer.parseInt(entrepriseNumberStr);
                    System.out.println("Parsed entrepriseNumber: " + entrepriseNumber);

                    // Execute the SOAP service call with parameters
                    String response = callKboService(langue, entrepriseNumber);
                    System.out.println("SOAP service call response: " + response);
                    // Send the response
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (Exception e) {
                    System.out.println("Exception in handler: " + e.getMessage());
                    e.printStackTrace();
                    String errorResponse = "Error: " + e.getMessage();
                    exchange.sendResponseHeaders(500, errorResponse.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(errorResponse.getBytes());
                    os.close();
                }
            } else {
                System.out.println("Method not allowed: " + exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private String callKboService(String langue, int entrepriseNumber) throws MalformedURLException, JsonProcessingException {
            System.out.println("callKboService called with langue=" + langue + ", entrepriseNumber=" + entrepriseNumber);
//            SettingsService settingsService = SettingsService.getInstance();
            String wsaddress = "https://kbopub-acc.economie.fgov.be/kbopubws110000/services/wsKBOPub?wsdl";
            System.out.println("kbopubws.address: " + wsaddress);
            System.out.println("About to create WSKBOPubService...");
try{
            WSKBOPubService webService = new WSKBOPubService(new URL(wsaddress), new QName(
                    "http://economie.fgov.be/kbopub/webservices/v1", "WSKBOPubService"));
} catch (Exception e) {
    System.out.println("Exception creating SOAP service: " + e.getMessage());
    e.printStackTrace();
    throw e;
}
                    System.out.println("WSKBOPubService created successfully");

//            WSKBOPub service = webService.getWSKBOPub();
            System.out.println("WSKBOPub service obtained successfully");

            Holder<ReadEnterpriseReplyType> reply = new Holder<>();
            ReadEnterpriseRequestType request = new ReadEnterpriseRequestType();
            RequestContextType requestContext = new RequestContextType();
            Holder<ReplyContextType> replyContext = new Holder<>();

            requestContext.setId("a generated uuid");
            requestContext.getLanguage().add(langue);
            request.setEnterpriseNumber(entrepriseNumber);

//            service.readEnterprise(requestContext, request, reply, replyContext);

            ReplyStatusType replyStatus = replyContext.value.getReplyStatus();

            System.out.println("Reply code is :" + replyStatus.getCode());
            if (Objects.equals(replyStatus.getCode(), "00001")) {
                ObjectMapper mapper = new ObjectMapper();
                String jsonResponse = mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(reply.value.getEnterprise());
                return jsonResponse;
            }
            return "null";
        }

        private Map<String, String> parseQueryParams(String query) {
            Map<String, String> queryParams = new HashMap<>();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=", 2);
                    String key = keyValue[0];
                    String value = keyValue.length > 1 ? keyValue[1] : ""; // Default value to empty string if no value provided
                    queryParams.put(key, value);
                }
            }
            return queryParams;
        }

    }
}
