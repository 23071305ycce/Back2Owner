LoginForm_Backend

Project Description
This is a Spring Boot backend for a login and registration system with JWT-based authentication.
It includes:

* User registration via `/register`
* User login via `/login` with JWT token generation
* Session timeout handling (via JWT expiry)
* Password hashing with BCrypt
* Spring Security configuration
  

Tech Stack
* Java 17+
* Spring Boot 3.x
* Spring Security
* PostgreSQL
* Maven

Backend Setup Instructions

1. Clone the repository

Open terminal and run:
git clone [https://github.com/](https://github.com/)<username>/LoginForm_Backend.git
cd LoginForm_Backend

2. Configure database

* Install PostgreSQL if not already installed.
* Create a database named `loginform_db`.
* Create a table named `app_user` with the following columns:

  * `user_id` : Auto-increment primary key
  * `firstname` : Text, not null
  * `lastname` : Text, not null
  * `email` : Text, unique, not null
  * `username` : Text, unique, not null
  * `password` : Text, not null
  * `role` : Text, default 'ROLE_USER'

Example SQL:
CREATE DATABASE loginform_db;
CREATE TABLE app_user (user_id SERIAL PRIMARY KEY, firstname VARCHAR(100) NOT NULL, lastname VARCHAR(100) NOT NULL, email VARCHAR(150) UNIQUE NOT NULL, username VARCHAR(100) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER');

4. Configure application.properties
   
Set your database connection details:
spring.datasource.url=jdbc:postgresql://localhost:5432/loginform_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
server.port=8080

5. Build and run the backend
Run the following commands:
mvn clean install
mvn spring-boot:run

API Endpoints

| Method | Endpoint         | Description                | Request Body                                   |
| ------ | ---------------- | -------------------------- | ---------------------------------------------- |
| POST   | /register        | Register a new user        | firstname, lastname, email, username, password |
| POST   | /login           | Login user and get JWT     | username, password                             |
| GET    | /session-expired | Message after token expiry | N/A                                            |


5. Test the API
* Use Postman or any HTTP client
* Register a user: Send a POST request to [http://localhost:8080/register](http://localhost:8080/register) with JSON body:
  firstname, lastname, email, username, password
* Login a user: Send a POST request to [http://localhost:8080/login](http://localhost:8080/login) with JSON body:
  username, password

6. Notes
* JWT token expires in 15 minutes
* All protected endpoints require Authorization header: Bearer <token>
* For production, use environment variables to store the secret key in JWTUtil

