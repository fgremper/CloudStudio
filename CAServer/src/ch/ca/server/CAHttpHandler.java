package ch.ca.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import com.sun.net.httpserver.*;

public class CAHttpHandler implements HttpHandler {
   public void handle(HttpExchange exchange) throws IOException {
       URI uri = exchange.getRequestURI();
       String requestMethod = exchange.getRequestMethod();
       System.out.println("Incoming request: " + requestMethod + " " + uri.getPath());
       
       
       if (requestMethod.equalsIgnoreCase("PUT")) {
    	   System.out.println(exchange.getRequestBody().toString());
       }
       
       String response = "Path: " + uri.getPath() + "\n";
       exchange.sendResponseHeaders(200, response.length());
       OutputStream os = exchange.getResponseBody();
       os.write(response.getBytes());
       os.close();
   }
}
