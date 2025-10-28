# 🧾 Collectibles Spark API

## 🏗️ Project Overview

**Collectibles Spark API** is a web service developed in **Java** using the **Spark Framework**, designed to manage collectible items and users in an online store scenario.  
This project represents the first stage (Sprint 1) of a three-phase challenge focused on building a complete web application that streamlines Java web development by reducing configuration files through the use of Spark’s minimalist microframework.

The goal of Sprint 1 is to implement a functional **RESTful API** capable of handling user operations with complete CRUD functionality (Create, Read, Update, Delete) using HTTP routes and JSON responses.

---

## 🧠 Competence and Learning Integration

This project demonstrates the **implementation of Spark Framework** within a Maven-based environment, applying Java web development principles to create modular, maintainable, and scalable applications.

It integrates the following areas of knowledge:

- **Java OOP principles** (encapsulation, classes, and methods)
- **HTTP routing and REST design**
- **API development using Spark Java**
- **Dependency management with Maven**
- **Logging using Logback**
- **Data serialization with Gson**

Through this challenge, I learned to create an API from scratch, manage dependencies, configure routes, and handle requests independently — reflecting **C2-level mastery** as defined in the evaluation rubric.

---

## ⚙️ Technical Configuration

### 🧩 Maven Setup

The project was configured as a Maven application with the following dependencies:

```xml
<dependencies>
  <dependency>
    <groupId>com.sparkjava</groupId>
    <artifactId>spark-core</artifactId>
    <version>2.9.4</version>
  </dependency>

  <dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
  </dependency>

  <dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.2.13</version>
  </dependency>
</dependencies>
```

✅ **Why Spark?**  
Because it eliminates XML configuration files and boilerplate code, allowing fast API setup using concise Java functions.

✅ **Why Gson?**  
To serialize and deserialize JSON responses, enabling consistent REST communication.

✅ **Why Logback?**  
To log HTTP activity and exceptions, enhancing debugging and traceability.

---

## 🚀 Running the Project

### ▶️ From IntelliJ IDEA
1. Open `App.java`.
2. Run → **Run 'App.main()'**
3. Access `http://localhost:4567/users`

### ▶️ From Terminal
```bash
mvn clean package
java -jar target/collectibles-spark-0.1.0.jar
```

Server starts at:
```
== Spark has ignited ...
>> Listening on 0.0.0.0:4567
```

---

## 🔀 Route Definition

According to Spark’s documentation, each route consists of **three parts: a verb, a route, and a callback**.

| HTTP Verb | Route | Description |
|------------|--------|--------------|
| `GET` | `/users` | Retrieves all users |
| `GET` | `/users/:id` | Retrieves a user by ID |
| `POST` | `/users/:id` | Creates a new user |
| `PUT` | `/users/:id` | Updates user information |
| `OPTIONS` | `/users/:id` | Checks if a user exists |
| `DELETE` | `/users/:id` | Deletes a specific user |
| `GET` | `/health` | Health check route (returns `OK`) |

Each route returns a **JSON-formatted response**, structured through **Gson serialization**, ensuring lightweight data handling.

---

## 💡 Example Requests

### 🧠 Create User
```bash
curl -X POST http://localhost:4567/users/123   -H "Content-Type: application/json"   -d '{"name":"Rafael","email":"rafa@example.com"}'
```

### 📋 Get All Users
```bash
curl http://localhost:4567/users
```

### 🧾 Update User
```bash
curl -X PUT http://localhost:4567/users/123   -H "Content-Type: application/json"   -d '{"name":"Rafael A.","email":"rafa.a@example.com"}'
```

### ❌ Delete User
```bash
curl -X DELETE http://localhost:4567/users/123
```

**Browser evidence:**  
Accessing `http://localhost:4567/users` shows the JSON list `[ ]` initially, and then displays the created user object after a POST operation.

---

## 🧱 Project Structure

```
collectibles-spark/
│
├── src/
│   ├── main/
│   │   ├── java/com/nao/collectibles/
│   │   │   ├── model/User.java
│   │   │   ├── store/UserStore.java
│   │   │   └── App.java
│   │   └── resources/logback.xml
│   └── test/
├── pom.xml
└── README.md
```

- `App.java`: Contains main method and route definitions.  
- `UserStore.java`: Handles in-memory user CRUD operations.  
- `User.java`: Model class defining user structure.  
- `logback.xml`: Manages log output.  
- `pom.xml`: Dependency management and build configuration.

---

## 🧩 Exception and Error Handling

Implemented using Spark’s built-in mechanisms:

```java
notFound((req, res) -> {
  res.type("application/json");
  return gson.toJson(Map.of("message", "Route not found"));
});

internalServerError((req, res) -> {
  res.type("application/json");
  return gson.toJson(Map.of("message", "Internal server error"));
});
```

✅ Ensures descriptive error responses instead of generic 404/500 pages.

---

## 🌍 Innovation and Impact

### ⚙️ Technical Innovation
This project integrates Spark’s lightweight framework to minimize configuration overhead, drastically improving setup time and readability compared to traditional Java EE approaches.

### 🌐 Educational Impact
The project demonstrates how microframeworks can simplify backend development for beginners while maintaining professional-level API standards.

### 🧩 Future Expansion
- Persistent database integration (SQLite/MySQL)
- Real-time price updates via WebSockets
- Frontend templates using Mustache

---

## 📊 Impact Analysis

| Sphere | Description |
|---------|--------------|
| **Technical** | Simplifies the Java web app workflow using Spark, reducing boilerplate code. |
| **Educational** | Enhances understanding of REST API fundamentals, JSON serialization, and backend routing. |
| **Professional** | Prepares for full-stack integration and deployment pipelines in real-world projects. |
| **Social** | Contributes to the development of accessible e-commerce prototypes for small collectors and entrepreneurs. |

---

## 🧠 Lessons Learned

- How to configure Maven for dependency management.  
- Defining routes and callbacks in Spark.  
- Testing REST endpoints with `curl`, Postman, and browser.  
- Understanding in-memory storage vs. persistent databases.  
- Importance of structured documentation for project evaluation.

---

## 📽️ Final Deliverable Summary

- ✅ Functional REST API for user management.  
- ✅ Maven configuration and dependency setup.  
- ✅ Exception handling for 404 and 500 errors.  
- ✅ Evidence screenshots (browser, Postman, console).  
- ✅ Ready for integration with templates and WebSockets in Sprint 2.  

---

## 🧑‍💻 Author
**Leonel Campos**  
Biomedical Engineer & Software Developer  
[GitHub Repository Link](https://github.com/your-username/collectibles-spark)
