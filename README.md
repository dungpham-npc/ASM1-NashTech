# ASM1 NashTech

This repository contains **ShopFun**, an eCommerce web application built with modern Java and web technologies. It was developed as part of an assignment to demonstrate backend development, RESTful APIs, authentication, and basic CRUD operations.

---

## 📚 Table of Contents

1. [Introduction](#introduction)  
2. [Features](#features)  
3. [Technologies Used](#technologies-used)  
4. [Setup and Installation](#setup-and-installation)  
5. [Usage](#usage)  
6. [Contributing](#contributing)  
7. [License](#license)  
8. [Contact](#contact)

---

## 🚀 Introduction

**ShopFun** is a full-stack eCommerce system with role-based access and essential functionality for customers and administrators. It is designed for educational purposes to demonstrate backend API development with Spring Boot.

---

## 🧹 Features

### 👤 For Customers
- Home page with category menu and featured products
- Browse products by category
- View detailed product information (name, images, description, price, average rating)
- Rate a product
- Register and log in/out
- *(Optional)* Shopping cart and order placement

### 🔐 For Admins
- Secure admin login (with role-based access control)
- Manage product categories (name, description)
- Manage products (name, category, description, price, images, isFeatured, timestamps)
- Manage customers (email, name, registration info)

---

## 🛠 Technologies Used

### 🗞️ Backend
- **Java 21**
- **Spring Boot**
- **Spring Security with JWT**
- **JPA/Hibernate**
- **PostgreSQL**
- **Swagger UI** for API documentation

## ⚙️ Setup and Installation

### 1. Clone the Repository

```bash
git clone https://github.com/dungpham-npc/ASM1-NashTech.git
cd ASM1-NashTech
```

### 2. Configure the Database

Make sure PostgreSQL is running locally or in Docker. Use the following config in your `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecom
spring.datasource.username=sa
spring.datasource.password=123456
```

> You can also use IntelliJ’s database UI to connect if preferred.

### 3. Build the Project

Using Maven:

```bash
./mvnw clean install
```

Or open the project in **IntelliJ IDEA**, build using the IDE.

### 4. Run the Application

```bash
./mvnw spring-boot:run
```

Or run the generated jar:

```bash
java -jar target/asm1-nashtech.jar
```

Visit `http://localhost:8080/swagger-ui/index.html` to explore the API.

---

## 💡 Usage

Once the backend is running:
- Admin users can access category and product management APIs
- Customers can register, browse products, and rate them
- Auth is handled using JWT
- Swagger UI is available for testing all endpoints

---

## 🧪 Unit Testing

- Written with JUnit 5
- Covers core services and utilities
- Demonstrates testing principles (not full coverage)

---

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repo
2. Create a branch:  
   ```bash
   git checkout -b feature/your-feature
   ```
3. Make your changes, then commit:  
   ```bash
   git commit -m "Add: your feature description"
   ```
4. Push to your fork:  
   ```bash
   git push origin feature/your-feature
   ```
5. Open a pull request 🚀

---

## 📄 License

This project is for educational and assignment purposes. License to be added later.

---

## 📨 Contact

- **Author:** Dung Pham  
- **GitHub:** [@dungpham-npc](https://github.com/dungpham-npc)

## Database Schema
![DB](asset/BE_db.png)

