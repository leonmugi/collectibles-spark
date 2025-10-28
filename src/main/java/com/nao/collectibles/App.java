package com.nao.collectibles;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.nao.collectibles.model.User;
import com.nao.collectibles.store.UserStore;

public class App {
    public static void main(String[] args) {
        port(getHerokuAssignedPort());

        // Middlewares simples
        before((req, res) -> res.type("application/json"));
        after((req, res) -> res.header("Cache-Control", "no-store"));

        // CORS básico (por si luego usas front separado)
        options("/*", (req, res) -> {
            String reqHeaders = req.headers("Access-Control-Request-Headers");
            if (reqHeaders != null) res.header("Access-Control-Allow-Headers", reqHeaders);
            String reqMethod = req.headers("Access-Control-Request-Method");
            if (reqMethod != null) res.header("Access-Control-Allow-Methods", reqMethod);
            res.header("Access-Control-Allow-Origin", "*");
            return "OK";
        });
        before((req, res) -> res.header("Access-Control-Allow-Origin", "*"));

        final Gson gson = new Gson();
        final UserStore users = new UserStore();

        // Rutas (verbo + ruta + callback) :contentReference[oaicite:2]{index=2}

        // GET /users — lista todos
        get("/users", (req, res) -> users.findAll(), gson::toJson);

        // GET /users/:id — uno por id
        get("/users/:id", (req, res) -> {
            String id = req.params(":id");
            User u = users.findById(id);
            if (u == null) {
                res.status(404);
                return message("User not found");
            }
            return u;
        }, gson::toJson);

        // POST /users/:id — crea (según requerimiento)
        post("/users/:id", (req, res) -> {
            String id = req.params(":id");
            User payload = gson.fromJson(req.body(), User.class);
            if (payload == null) payload = new User();
            boolean created = users.create(id, payload);
            if (!created) {
                res.status(409);
                return message("User already exists");
            }
            res.status(201);
            return users.findById(id);
        }, gson::toJson);

        // PUT /users/:id — actualiza
        put("/users/:id", (req, res) -> {
            String id = req.params(":id");
            User payload = gson.fromJson(req.body(), User.class);
            boolean ok = users.update(id, payload);
            if (!ok) {
                res.status(404);
                return message("User not found");
            }
            return users.findById(id);
        }, gson::toJson);

        // OPTIONS /users/:id — verifica existencia
        options("/users/:id", (req, res) -> {
            String id = req.params(":id");
            if (users.exists(id)) {
                res.status(200);
                return message("Exists");
            } else {
                res.status(404);
                return message("Not exists");
            }
        }, gson::toJson);

        // DELETE /users/:id — elimina
        delete("/users/:id", (req, res) -> {
            String id = req.params(":id");
            boolean ok = users.delete(id);
            if (!ok) {
                res.status(404);
                return message("User not found");
            }
            res.status(204); // No Content
            return "";
        });

        // Manejo simple para 404
        notFound((req, res) -> {
            res.type("application/json");
            return gson.toJson(message("Route not found"));
        });

        // Manejo simple de errores
        internalServerError((req, res) -> {
            res.type("application/json");
            return gson.toJson(message("Internal server error"));
        });
    }

    private static int getHerokuAssignedPort() {
        String port = System.getenv("PORT");
        return port != null ? Integer.parseInt(port) : 4567;
    }

    private static java.util.Map<String, String> message(String m) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("message", m);
        return map;
    }
}
