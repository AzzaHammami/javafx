package com.example.rendez_vous.controllers.api;

import com.example.rendez_vous.services.Servicedisponibilite;
import com.example.rendez_vous.models.Disponibilite;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;

public class DisponibiliteRestController {

    private final Servicedisponibilite service = new Servicedisponibilite();
    private final Gson gson = new Gson();
    private HttpServer server;

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // Create contexts for each endpoint
        server.createContext("/api/disponibilites", new DisponibiliteHandler());
        server.createContext("/api/disponibilites/medecin", new MedecinDisponibiliteHandler());

        server.start();
        System.out.println("Server started on port 8081");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private class DisponibiliteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String response = "";
            int statusCode = 200;

            // Set CORS headers
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if (method.equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                switch (method) {
                    case "GET":
                        if (path.equals("/api/disponibilites")) {
                            List<Disponibilite> disponibilites = service.getAllDisponibilites();
                            response = gson.toJson(disponibilites);
                        } else {
                            int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                            Disponibilite disponibilite = service.getDisponibiliteById(id);
                            if (disponibilite != null) {
                                response = gson.toJson(disponibilite);
                            } else {
                                statusCode = 404;
                                response = gson.toJson(new ErrorResponse("Disponibilité non trouvée"));
                            }
                        }
                        break;

                    case "POST":
                        String requestBody = new String(exchange.getRequestBody().readAllBytes());
                        Disponibilite newDisponibilite = gson.fromJson(requestBody, Disponibilite.class);
                        service.ajouterDisponibilite(newDisponibilite);
                        response = gson.toJson(newDisponibilite);
                        break;

                    case "PUT":
                        int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                        requestBody = new String(exchange.getRequestBody().readAllBytes());
                        Disponibilite updatedDisponibilite = gson.fromJson(requestBody, Disponibilite.class);
                        updatedDisponibilite.setId(id);
                        service.modifierDisponibilite(updatedDisponibilite);
                        response = gson.toJson(updatedDisponibilite);
                        break;

                    case "DELETE":
                        id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                        service.supprimerDisponibilite(id);
                        statusCode = 204;
                        break;

                    default:
                        statusCode = 405;
                        response = gson.toJson(new ErrorResponse("Méthode non autorisée"));
                }
            } catch (SQLException e) {
                statusCode = 500;
                response = gson.toJson(new ErrorResponse(e.getMessage()));
            } catch (NumberFormatException e) {
                statusCode = 400;
                response = gson.toJson(new ErrorResponse("Format d'ID invalide"));
            }

            byte[] responseBytes = response.getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    private class MedecinDisponibiliteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            int medecinId = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
            
            try {
                List<Disponibilite> disponibilites = service.getDisponibilitesByMedecin(medecinId);
                String response = gson.toJson(disponibilites);
                
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (SQLException e) {
                String error = gson.toJson(new ErrorResponse(e.getMessage()));
                exchange.sendResponseHeaders(500, error.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(error.getBytes());
                }
            }
        }
    }

    private static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
