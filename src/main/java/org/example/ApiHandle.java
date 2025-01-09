package org.example;

import be.fgov.economie.kbopub.webservices.v1.WSKBOPub;
import be.fgov.economie.kbopub.webservices.v1.WSKBOPubService;
import be.fgov.economie.kbopub.webservices.v1.datamodel.EnterpriseType;
import be.fgov.economie.kbopub.webservices.v1.messages.*;
import be.fgov.economie.kbopub.wsclient.service.SettingsService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

public class ApiHandle implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        SettingsService settingsService = SettingsService.getInstance();
        String wsaddress = settingsService.getSetting("kbopubws.address");
        WSKBOPubService webService = new WSKBOPubService(new URL(wsaddress), new QName(
                "http://economie.fgov.be/kbopub/webservices/v1", "WSKBOPubService"));
        WSKBOPub service = webService.getWSKBOPub();
        Holder<ReadEnterpriseReplyType> reply = new Holder < ReadEnterpriseReplyType >();
        ReadEnterpriseRequestType request = new ReadEnterpriseRequestType();
        RequestContextType requestContext = new RequestContextType();
        Holder <ReplyContextType> replyContext = new Holder <ReplyContextType>();
// TODO replace the parameter by a generated uuid
        requestContext.setId("a generated uuid");
        requestContext.getLanguage().add("nl");
// 314595348L = KBO nummer van de FOD Economie
        request.setEnterpriseNumber(782372801);
        service.readEnterprise(requestContext, request, reply, replyContext);
        ReplyStatusType replyStatus = (ReplyStatusType) replyContext.value.getReplyStatus();
//        LOG.info("RESULT: Statuscode: " + replyStatus.getCode() + " " + replyStatus.getDescription() + ", KBOnr: " +
//                reply.value.getEnterprise().getNumber());
        System.out.println("Reply Status is : " + replyStatus.getCode());
        System.out.println("description : " + reply.value.getEnterprise().getDenomination().get(0).getDescription().get(0).getValue());

//        EnterpriseType response = reply.value.getEnterprise();
//        exchange.sendResponseHeaders(200, response.length());
//        OutputStream os = exchange.getResponseBody();
//        os.write(response.getBytes());
//        os.close();
    }
}
