package com.nao.collectibles;

import static spark.Spark.*;

import com.google.gson.Gson;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.*;
import com.nao.collectibles.errors.*;
import com.nao.collectibles.view.MustacheRenderer;

public class App {
    public static void main(String[] args) {
        // --- CONFIGURACIÓN BASE ---
        port(4567);
        staticFiles.location("/public");
        Gson gson = new Gson();
        MustacheTemplateEngine engine = new MustacheTemplateEngine();

        // --- DATOS EN MEMORIA (simulados) ---
        Map<String, Map<String, Object>> users = new HashMap<>();
        Map<String, Map<String, Object>> items = new HashMap<>();
        List<Map<String, Object>> offers = new ArrayList<>();

        // Semilla para probar la vista
        items.put("1", new HashMap<>(Map.of("id","1","name","Gohan","price", 500.0)));
        items.put("2", new HashMap<>(Map.of("id","2","name","Trunks","price", 600.0)));

        // --- MANEJO DE ERRORES Y EXCEPCIONES ---
        notFound((req, res) -> {
            res.type("application/json");
            return gson.toJson(new ApiError("Route not found", "NOT_FOUND"));
        });

        internalServerError((req, res) -> {
            res.type("application/json");
            return gson.toJson(new ApiError("Internal server error", "INTERNAL"));
        });

        exception(BadRequestException.class, (ex, req, res) -> {
            res.type("application/json");
            res.status(400);
            res.body(gson.toJson(new ApiError(ex.getMessage(), "BAD_REQUEST")));
        });

        exception(NotFoundException.class, (ex, req, res) -> {
            res.type("application/json");
            res.status(404);
            res.body(gson.toJson(new ApiError(ex.getMessage(), "NOT_FOUND")));
        });

        // --- RUTAS REST DE USUARIOS (Sprint 1) ---
        get("/users", (req, res) -> gson.toJson(users.values()));
        get("/users/:id", (req, res) -> {
            String id = req.params("id");
            if (!users.containsKey(id)) throw new NotFoundException("User not found");
            return gson.toJson(users.get(id));
        });
        post("/users/:id", (req, res) -> {
            String id = req.params("id");
            Map<String, Object> data = gson.fromJson(req.body(), Map.class);
            users.put(id, data);
            res.status(201);
            return gson.toJson(data);
        });
        put("/users/:id", (req, res) -> {
            String id = req.params("id");
            if (!users.containsKey(id)) throw new NotFoundException("User not found");
            Map<String, Object> data = gson.fromJson(req.body(), Map.class);
            users.put(id, data);
            return gson.toJson(data);
        });
        delete("/users/:id", (req, res) -> {
            String id = req.params("id");
            users.remove(id);
            res.status(204);
            return "";
        });
        options("/users/:id", (req, res) -> gson.toJson(Map.of("exists", users.containsKey(req.params("id")))));

        // --- RUTAS DE ITEMS Y OFERTAS (Sprint 2) ---
        get("/items", (req, res) -> {
            var model = new HashMap<String, Object>();
            model.put("title", "Collectible Items");
            model.put("content", MustacheRenderer.partial("items.mustache", Map.of("items", items.values())));
            return new ModelAndView(model, "layout.mustache");
        }, engine);

        get("/items/:id/offer", (req, res) -> {
            String id = req.params("id");
            if (!items.containsKey(id)) throw new NotFoundException("Item not found");
            var model = new HashMap<String, Object>();
            model.put("title", "Make an Offer");
            model.put("content", MustacheRenderer.partial("offer_form.mustache", Map.of("id", id)));
            return new ModelAndView(model, "layout.mustache");
        }, engine);

        post("/items/:id/offers", (req, res) -> {
            String id = req.params("id");
            if (!items.containsKey(id)) throw new NotFoundException("Item not found");

            String amountStr = req.queryParams("amount");
            String bidder = req.queryParams("bidder");
            if (amountStr == null || bidder == null || bidder.isBlank())
                throw new BadRequestException("Missing bidder or amount");

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                throw new BadRequestException("Amount must be numeric");
            }
            if (amount <= 0) throw new BadRequestException("Amount must be greater than zero");

            Map<String, Object> offer = Map.of("itemId", id, "bidder", bidder, "amount", amount);
            offers.add(offer);

            var model = new HashMap<String, Object>();
            model.put("title", "Make an Offer");
            model.put("content", MustacheRenderer.partial("offer_form.mustache", Map.of("id", id, "ok", true)));
            return new ModelAndView(model, "layout.mustache");
        }, engine);

        // --- HOME ---
        get("/", (req, res) -> "Spark app running at http://localhost:4567");
    }
}
