# AI-Powered Product Recommendation System

## Project Overview

This project is a simple product recommendation system built using Java 21, Spring Boot, and AI technologies. It recommends products based on user viewing history using user-based and item-based collaborative filtering algorithms.

## Technologies Used

* **Language:** Java 21
* **Framework:** Spring Boot
* **Database:** PostgreSQL (Configuration Required)
* **API Documentation:** Swagger/OpenAPI
* **Recommendation Algorithms:**
    * Basic (Random) Recommendation
    * User-Based Collaborative Filtering
    * Item-Based Collaborative Filtering

## How to Run

1.  **Install JDK 21:** Ensure you have Java 21 installed.
2.  **Configure PostgreSQL:** Modify the PostgreSQL connection settings in the `src/main/resources/application.properties` file according to your environment.
3.  **Run with Gradle:** Execute the following command in the project root directory to run the application.

    ```bash
    ./gradlew bootRun
    ```

4.  **Access API:** Once the application is running, you can access the API documentation via Swagger UI at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

## API Endpoints (For Testing)

* `GET /api/recommendations/{userId}`: Get basic (random) product recommendations for a specific user ID.
* `GET /api/recommendations/user-based/{userId}`: Get user-based collaborative filtering recommendations for a specific user ID.
* `GET /api/recommendations/item-based/{userId}`: Get item-based collaborative filtering recommendations for a specific user ID.
* `POST /api/view-history`: Create a user's product view history (Request Body: `{"userId": 1, "productId": 10}`).

## Future Plans

* Implement a hybrid recommendation system.
* Integrate more sophisticated AI models (e.g., deep learning-based recommendations).
* Evaluate and improve recommendation performance.
* Expand user behavior data (purchases, likes, etc.).
