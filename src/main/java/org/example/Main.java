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
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/readEnterprise", new ReadEnterpriseHandler());
        server.setExecutor(null); // Use the default executor
        System.out.println("Server is running on http://localhost:8080");
        server.start();
    }

    // HTTP Handler for /readEnterprise
    static class ReadEnterpriseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                // Parse query parameters
                Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());

                String langue = queryParams.get("langue");
                String entrepriseNumberStr = queryParams.get("entrepriseNumber");

                if (langue == null || entrepriseNumberStr == null) {
                    String errorResponse = "Missing required parameters: langue and entrepriseNumber.";
                    exchange.sendResponseHeaders(400, errorResponse.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(errorResponse.getBytes());
                    os.close();
                    return;
                }

                try {
                    int entrepriseNumber = Integer.parseInt(entrepriseNumberStr);

                    // Execute the SOAP service call with parameters
                    String response = callKboService(langue, entrepriseNumber);

                    // Send the response
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (Exception e) {
                    // Handle errors
                    String errorResponse = "Error: " + e.getMessage();
                    exchange.sendResponseHeaders(500, errorResponse.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(errorResponse.getBytes());
                    os.close();
                }
            } else {
                // Method not allowed
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private String callKboService(String langue, int entrepriseNumber) throws MalformedURLException, JsonProcessingException {
            SettingsService settingsService = SettingsService.getInstance();
            String wsaddress = settingsService.getSetting("kbopubws.address");

            WSKBOPubService webService = new WSKBOPubService(new URL(wsaddress), new QName(
                    "http://economie.fgov.be/kbopub/webservices/v1", "WSKBOPubService"));
            WSKBOPub service = webService.getWSKBOPub();

            Holder<ReadEnterpriseReplyType> reply = new Holder<>();
            ReadEnterpriseRequestType request = new ReadEnterpriseRequestType();
            RequestContextType requestContext = new RequestContextType();
            Holder<ReplyContextType> replyContext = new Holder<>();

            requestContext.setId("a generated uuid");
            requestContext.getLanguage().add(langue);
            request.setEnterpriseNumber(entrepriseNumber);

            service.readEnterprise(requestContext, request, reply, replyContext);

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
