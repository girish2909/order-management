# Order Management System (OMS) Backend

A robust, production-ready RESTful API for managing orders and items, built with **Spring Boot 3.4** and **Java 21**. This application demonstrates modern backend development practices including JWT authentication, caching, AOP logging, containerization, and Kubernetes deployment.

## 🚀 Tech Stack

*   **Language:** Java 21
*   **Framework:** Spring Boot 3.4.2
*   **Database:** H2 (In-Memory)
*   **Security:** Spring Security + JWT (Access & Refresh Tokens)
*   **Documentation:** OpenAPI 3 (Swagger UI)
*   **Build Tool:** Maven
*   **Containerization:** Docker
*   **Orchestration:** Helm & Kubernetes
*   **Code Quality:** Spotless (Google Java Style), JaCoCo (Coverage), SonarQube ready
*   **Utilities:** Lombok, MapStruct

## 🛠️ Getting Started

### Prerequisites
*   Java 21 SDK
*   Maven 3.8+
*   Docker (optional)

### Build the Application
We have provided convenience scripts to build the project, apply code formatting, and run tests.

**Linux/macOS:**
```bash
./build.sh
```

**Windows:**
```cmd
build.bat
```

Or manually:
```bash
mvn spotless:apply clean install
```

### Run the Application
```bash
java -jar target/order-management-0.0.1-SNAPSHOT.jar
```
The application will start on `http://localhost:8080`.

## 🔐 Authentication (JWT)

The API is secured using JWT Bearer tokens.

1.  **Default Users** (Created on startup):
    *   **Admin:** `admin` / `admin`
    *   **User:** `user` / `password`

2.  **Login Flow:**
    *   POST `/api/auth/login` with `{"username": "admin", "password": "admin"}`.
    *   Response contains `accessToken` and `refreshToken`.

3.  **Accessing Protected Endpoints:**
    *   Add header: `Authorization: Bearer <your_access_token>`

4.  **Refresh Token:**
    *   POST `/api/auth/refreshToken` with `{"token": "<your_refresh_token>"}` to get a new access token.

## 📚 API Documentation

Once the application is running, you can explore the API via:

*   **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
    *   Click the **Authorize** button and enter your Bearer token to test secured endpoints.
*   **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## 🧪 Testing & Code Quality

*   **Run Unit Tests:**
    ```bash
    mvn test
    ```
*   **Code Coverage Report:**
    After running tests, open `target/site/jacoco/index.html` in your browser.
*   **Code Formatting:**
    The project enforces Google Java Style. Run `mvn spotless:apply` to fix formatting issues automatically.

## 🐳 Docker Support

**Build the Image:**
```bash
docker build -t order-management .
```

**Run the Container:**
```bash
docker run -p 8080:8080 order-management
```

## ☸️ Kubernetes (Helm)

A Helm chart is included in the `helm/` directory.

**Install Chart:**
```bash
helm install order-backend ./helm
```

## 🗄️ Database Console

Access the H2 Database Console at:
*   **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
*   **JDBC URL:** `jdbc:h2:mem:ordersdb`
*   **User:** `sa`
*   **Password:** *(leave blank)*

## 📂 Project Structure

*   `controller`: REST endpoints
*   `service`: Business logic & transaction management
*   `repository`: Data access layer (JPA)
*   `entity`: JPA entities
*   `dto`: Data Transfer Objects
*   `mapper`: MapStruct interfaces
*   `config`: Security, Swagger, Web configurations
*   `aop`: Logging aspects
*   `exception`: Global exception handling

---
*Developed by Girish*