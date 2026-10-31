# 🔨 LiveBid — Real-Time Online Auction Platform

LiveBid is a real-time online auction platform where users can create auctions, place bids, and compete to win products through a secure bidding system.

Built using **Java 21, Spring Boot, MySQL, Thymeleaf, and WebSockets**, LiveBid provides an interactive auction experience with automated auction lifecycle management and winner selection.

## 🚀 Features

### 👤 User Management
- User registration and authentication.
- Secure password hashing using BCrypt.
- Role-based access control.
- Admin user management.
- Suspend and reactivate user accounts.

### 🏷️ Auction Management
- Create and manage auctions.
- Save auctions as drafts.
- Schedule auctions for future dates.
- Automatically activate scheduled auctions.
- Automatically end expired auctions.
- View auction details and bidding history.
- Personal auction dashboard with status filters.

### 💰 Real-Time Bidding
- Place bids on live auctions.
- Real-time bid updates using WebSockets.
- Prevent sellers from bidding on their own auctions.
- Validate bid amounts.
- Prevent bidding after an auction ends.
- Protect concurrent bidding operations.

### 🏆 Automatic Winner Selection
- Automatically select the highest bidder when an auction ends.
- Earliest bid wins in case of equal highest bids.
- Auctions without bids end with no winner.
- Store the winning bid in the database.
- Display winner information and winning amount on the auction details page.

### 🔐 Security
- Spring Security integration.
- BCrypt password encryption.
- Role-based authorization.
- CSRF protection.
- Secure session-based authentication.

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming |
| Spring Boot 4.0.9 | Application framework |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database operations |
| Hibernate | ORM |
| MySQL 8 | Relational database |
| Flyway | Database migrations |
| Thymeleaf | Server-side templates |
| WebSockets | Real-time communication |
| Maven | Dependency management |
| HTML5 & CSS3 | Frontend |
| JavaScript | Client-side functionality |
| Git & GitHub | Version control |

## 📋 Prerequisites

Install the following before running the project:

- Java Development Kit (JDK) 21
- Apache Maven 3.9+
- MySQL Server 8+
- IntelliJ IDEA or another Java IDE
- Git

## ⚙️ Installation and Setup

### 1. Clone the repository

```bash
git clone https://github.com/Arbazkhann2109/livebid.git
```

### 2. Navigate to the project directory

```bash
cd livebid
```

### 3. Create the MySQL database

Open MySQL Workbench and execute:

```sql
CREATE DATABASE livebid_db;
```

Create a dedicated application database user and grant access to the database according to your local MySQL configuration.

### 4. Configure environment variables

Configure the following environment variables:

```properties
DB_URL=jdbc:mysql://localhost:3306/livebid_db
DB_USERNAME=livebid_app
DB_PASSWORD=your_database_password
```

Do not commit database passwords or other secrets to GitHub.

### 5. Run the application

Using Maven:

```bash
mvn spring-boot:run
```

Or build the project:

```bash
mvn clean package
```

### 6. Access LiveBid

Open your browser:

**http://localhost:8080**

Flyway automatically manages database migrations when the application starts.

## 🧪 Build and Verification

Compile the project:

```bash
mvn clean compile
```

Run automated tests:

```bash
mvn test
```

Build the executable JAR:

```bash
mvn clean package
```

## 📁 Project Architecture

LiveBid follows a modular monolith architecture with domain-based package organization.

```text
livebid/
│
├── src/
│   ├── main/
│   │   ├── java/com/livebid/
│   │   │   ├── auction/
│   │   │   │   ├── controller/
│   │   │   │   ├── entity/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   │
│   │   │   ├── bidding/
│   │   │   │   ├── controller/
│   │   │   │   ├── entity/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   │
│   │   │   ├── user/
│   │   │   ├── admin/
│   │   │   ├── common/
│   │   │   ├── scheduler/
│   │   │   └── config/
│   │   │
│   │   └── resources/
│   │       ├── db/migration/
│   │       ├── templates/
│   │       ├── static/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   │
│   └── test/
│
├── pom.xml
└── README.md
```

## 🔄 Auction Lifecycle

```text
       DRAFT
         |
         | Seller schedules auction
         v
     SCHEDULED
         |
         | Scheduled start time
         v
        LIVE
         |
         | Auction end time reached
         v
       ENDED
         |
         | Highest valid bid selected
         v
       WINNER
```

If no bids are placed, the auction ends without a winner.

## 🔮 Future Enhancements

Planned improvements include:

- Email notifications for auction winners.
- Notifications when users are outbid.
- Winner and seller communication.
- Payment integration.
- Product image uploads.
- Advanced auction search and filtering.
- Public deployment and production monitoring.

## 👨‍💻 Author

**Arbaz Khan**

GitHub: [@Arbazkhann2109](https://github.com/Arbazkhann2109)

Project: [LiveBid](https://github.com/Arbazkhann2109/livebid)

---

## 📄 License

This project is currently developed as a personal software project. A formal open-source license has not yet been specified.

---

⭐ If you find this project interesting, consider starring the repository!
