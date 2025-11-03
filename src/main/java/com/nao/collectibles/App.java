package com.nao.collectibles;

import static spark.Spark.*;

import com.google.gson.Gson;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

// 1. IMPORTAMOS LAS CLASES DE WEBSOCKET (Session y anotaciones)
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket; // LA ANOTACIÓN PRINCIPAL

import java.util.*;
import com.nao.collectibles.errors.*;
import com.nao.collectibles.view.MustacheRenderer;

public class App {

    // --- VARIABLES GLOBALES (ESTÁTICAS) ---
    // Las movemos fuera de main() para que el WebSocket Handler pueda acceder a ellas

    // Mapa para todos los usuarios conectados al WebSocket
    public static Map<Session, Session> connectedUsers = new HashMap<>();

    // Convertidor de JSON
    public static Gson gson = new Gson();

    // --- DATOS EN MEMORIA (simulados) ---
    public static Map<String, Map<String, Object>> users = new HashMap<>();
    public static Map<String, Map<String, Object>> items = new HashMap<>();
    public static List<Map<String, Object>> offers = new ArrayList<>();


    // --- 2. NUEVA CLASE PARA MANEJAR EL WEBSOCKET ---
    // Esta es la clase que Spark SÍ acepta
    @WebSocket
    public static class PriceUpdatesWebSocketHandler {

        @OnWebSocketConnect
        public void onConnect(Session userSession) throws Exception {
            System.out.println("WebSocket: Nuevo usuario conectado! " + userSession.hashCode());
            // Lo agregamos a nuestro mapa estático de conexiones
            connectedUsers.put(userSession, userSession);
        }

        @OnWebSocketClose
        public void onClose(Session userSession, int statusCode, String reason) {
            System.out.println("WebSocket: Usuario desconectado! " + userSession.hashCode());
            // Lo eliminamos del mapa estático
            connectedUsers.remove(userSession);
        }

        @OnWebSocketMessage
        public void onMessage(Session userSession, String message) {
            System.out.println("WebSocket: Mensaje recibido de " + userSession.hashCode() + ": " + message);
        }
    }


    // --- PUNTO DE ENTRADA PRINCIPAL ---
    public static void main(String[] args) {

        // --- CONFIGURACIÓN BASE ---
        port(4567);
        staticFiles.location("/public");
        // Quitamos la creación de 'gson' y 'datos' de aquí, porque ya son estáticos
        MustacheTemplateEngine engine = new MustacheTemplateEngine();

        // --- Cargar los datos iniciales ---
        loadInitialData();


        // --- 3. REGISTRAMOS LA RUTA WEBSOCKET (AHORA CORRECTAMENTE) ---
        webSocket("/price-updates", PriceUpdatesWebSocketHandler.class);


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

        // --- RUTAS DE VISTAS (HTML) ---
        get("/items/new", (req, res) -> {
            var model = new HashMap<String, Object>();
            model.put("title", "New Item");
            model.put("content", MustacheRenderer.partial("new_item.mustache", Map.of()));
            return new ModelAndView(model, "layout.mustache");
        }, engine);

        get("/items", (req, res) -> {
            var model = new HashMap<String, Object>();
            model.put("title", "Collectible Items");
            model.put("content", MustacheRenderer.partial("items.mustache", Map.of("items", items.values())));
            return new ModelAndView(model, "layout.mustache");
        }, engine);

        get("/items/:id/offer", (req, res) -> {
            String id = req.params("id");
            if (!items.containsKey(id)) throw new NotFoundException("Item not found");

            // Obtenemos el precio actual
            double currentPrice = (double) items.get(id).get("price");

            var model = new HashMap<String, Object>();
            model.put("title", "Make an Offer");

            // Preparamos los datos para la plantilla
            var contentModel = new HashMap<String, Object>();
            contentModel.put("id", id);
            contentModel.put("currentPrice", currentPrice); // <-- ¡NUEVO!

            model.put("content", MustacheRenderer.partial("offer_form.mustache", contentModel));
            return new ModelAndView(model, "layout.mustache");
        }, engine);

        // --- RUTA POST PARA CREAR ITEM ---
        post("/items", (req, res) -> {
            String name = req.queryParams("name");
            String priceStr = req.queryParams("price");

            if (name == null || name.isBlank() || priceStr == null) {
                throw new BadRequestException("Name and price are required");
            }

            double price;
            try { price = Double.parseDouble(priceStr); }
            catch (NumberFormatException e) { throw new BadRequestException("Price must be numeric"); }
            if (price <= 0) throw new BadRequestException("Price must be > 0");

            String id = UUID.randomUUID().toString().substring(0, 8);
            items.put(id, new HashMap<>(Map.of("id", id, "name", name, "price", price)));

            res.redirect("/items");
            return null;
        });

        // --- RUTA POST PARA HACER OFERTA (CON LÓGICA WEBSOCKET) ---
        post("/items/:id/offers", (req, res) -> {
            String id = req.params("id");
            if (!items.containsKey(id)) throw new NotFoundException("Item not found");

            // --- Lógica de la oferta ---
            String amountStr = req.queryParams("amount");
            String bidder = req.queryParams("bidder");
            if (amountStr == null || bidder == null || bidder.isBlank())
                throw new BadRequestException("Missing bidder or amount");

            double newOfferAmount;
            try { newOfferAmount = Double.parseDouble(amountStr); }
            catch (NumberFormatException e) { throw new BadRequestException("Amount must be numeric"); }

            // --- Lógica de Validación (¡NUEVA!) ---
            Map<String, Object> item = items.get(id);
            double currentPrice = (double) item.get("price");

            // Preparamos el modelo para la plantilla de respuesta
            var model = new HashMap<String, Object>();
            model.put("title", "Make an Offer");

            var contentModel = new HashMap<String, Object>();
            contentModel.put("id", id);
            contentModel.put("currentPrice", currentPrice); // Pasamos el precio de vuelta

            // VALIDAMOS SI LA OFERTA ES MAYOR
            if (newOfferAmount <= currentPrice) {
                // OFERTA MUY BAJA: Registramos la oferta pero no actualizamos el precio
                Map<String, Object> offer = Map.of("itemId", id, "bidder", bidder, "amount", newOfferAmount, "wasAccepted", false);
                offers.add(offer);

                // Enviamos un mensaje de error de vuelta al formulario
                contentModel.put("error", "Tu oferta de $" + newOfferAmount + " debe ser mayor que el precio actual de $" + currentPrice + ".");
                model.put("content", MustacheRenderer.partial("offer_form.mustache", contentModel));
                return new ModelAndView(model, "layout.mustache");
            }

            // --- OFERTA ACEPTADA ---
            // 1. Guardamos la oferta como aceptada
            Map<String, Object> offer = Map.of("itemId", id, "bidder", bidder, "amount", newOfferAmount, "wasAccepted", true);
            offers.add(offer);

            // 2. Actualizamos el precio del item
            item.put("price", newOfferAmount);

            // 3. Notificamos a todos por WebSocket
            broadcastPriceUpdate(id, newOfferAmount);

            // 4. Mostramos el mensaje de éxito
            contentModel.put("ok", true);
            model.put("content", MustacheRenderer.partial("offer_form.mustache", contentModel));
            return new ModelAndView(model, "layout.mustache");
        }, engine);


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

        // --- HOME ---
        get("/", (req, res) -> "Spark app running at http://localhost:4567");

    } // --- FIN DEL MÉTODO main() ---


    // --- 4. FUNCIÓN PARA ENVIAR MENSAJES A TODOS (BROADCAST) ---
    // La sacamos de main() y la hacemos estática
    public static void broadcastPriceUpdate(String itemId, double newPrice) {
        System.out.println("Broadcasting: " + itemId + " -> " + newPrice);

        String jsonMessage = gson.toJson(Map.of(
                "type", "PRICE_UPDATE",
                "itemId", itemId,
                "newPrice", newPrice
        ));

        for (Session session : connectedUsers.keySet()) {
            try {
                if (session.isOpen()) {
                    session.getRemote().sendString(jsonMessage);
                }
            } catch (Exception e) {
                System.err.println("Error enviando mensaje a " + session.hashCode() + ": " + e.getMessage());
            }
        }
    }

    // --- 5. MÉTODO PARA CARGAR DATOS INICIALES ---
    // (Solo para limpiar el main)
    public static void loadInitialData() {
        items.put("item1", new HashMap<>(Map.of(
                "id", "item1", "name", "Gorra autografiada por Peso Pluma",
                "description", "Una gorra autografiada por el famoso Peso Pluma.", "price", 621.34
        )));
        items.put("item2", new HashMap<>(Map.of(
                "id", "item2", "name", "Casco autografiado por Rosalía",
                "description", "Un casco autografiado por la famosa cantante Rosalía, una verdadera MOTOMAMI!", "price", 734.57
        )));
        items.put("item3", new HashMap<>(Map.of(
                "id", "item3", "name", "Chamarra de Bad Bunny",
                "description", "Una chamarra de la marca favorita de Bad Bunny, autografiada por el propio artista.", "price", 521.89
        )));
        items.put("item4", new HashMap<>(Map.of(
                "id", "item4", "name", "Guitarra de Fernando Delgadillo",
                "description", "Una guitarra acústica de alta calidad utilizada por el famoso cantautor Fernando Delgadillo.", "price", 823.12
        )));
        items.put("item5", new HashMap<>(Map.of(
                "id", "item5", "name", "Jersey firmado por Snoop Dogg",
                "description", "Un jersey autografiado por el legendario rapero Snoop Dogg.", "price", 355.67
        )));
        items.put("item6", new HashMap<>(Map.of(
                "id", "item6", "name", "Prenda de Cardi B autografiada",
                "description", "Un crop-top usado y autografiado por la famosa rapera Cardi B. en su última visita a México", "price", 674.23
        )));
        items.put("item7", new HashMap<>(Map.of(
                "id", "item7", "name", "Guitarra autografiada por Coldplay",
                "description", "Una guitarra eléctrica autografiada por la popular banda británica Coldplay, un día antes de su concierto en Monterrey en 2022.", "price", 458.91
        )));
    }

} // --- FIN DE LA CLASE App ---