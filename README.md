# FindBack – Lost and Found Management System

A web-based Lost and Found Management System that helps organizations manage lost-item reports, found-item verification, item searches, claims, and physical handovers through one centralized platform.

## 1. Project Overview

In colleges, universities, offices, and other organizations, lost-and-found information is often maintained through registers, notices, or informal communication. This can make it difficult to search for reported items, identify their owners, track claim requests, and maintain proper records.

**FindBack** digitizes this process by providing a centralized platform where members can report lost or found belongings, search verified found items, submit claims, and track their activity. Organization administrators can verify found-item reports, review claims, and record completed handovers.

The application supports multiple organizations through separate workspaces. Each organization accesses its own records and users.

## 2. Key Features

### Organization Management
- Organization registration with automatic organization-code generation.
- Creation of the first organization administrator during registration.
- Separate organization workspaces.
- Member registration using an organization code.

### Authentication and Security
- Role-based login for organization administrators and members.
- BCrypt password hashing.
- Secure random session-token generation.
- SHA-256 hashing of session tokens before database storage.
- Session validation and logout.
- First-login email acknowledgment.

### Member Features
- Report lost items.
- Report found items.
- Search verified and available found items.
- Submit claims with ownership details.
- View personal reports and claim history.
- View possible matches for reported lost items.

### Administrator Features
- View organization-level dashboard statistics.
- Review reported items.
- Verify found-item reports.
- Review and approve or reject claims.
- Confirm physical handovers.
- View completed return records.
- View analytics and item-status information.

### Smart Item Matching
FindBack includes a weighted, rule-based matching mechanism that compares lost-item reports with available found items using:
- Item name similarity.
- Category.
- Location.
- Date.

Potential matches are assigned scores and displayed in descending order. This is a rule-based matching feature, not an AI model or a probability prediction.

## 3. Technology Stack

| Area | Technologies |
|---|---|
| Frontend | HTML5, CSS3, JavaScript |
| Backend | Java 21, Java Servlets |
| Architecture | MVC-based layered architecture |
| Application Server | Apache Tomcat 9 |
| Database | MySQL |
| Database Connectivity | JDBC, PreparedStatement |
| Password Security | jBCrypt |
| Email | JavaMail 1.6.2, JavaBeans Activation |
| Data Exchange | HTTP, JSON |
| Development Tools | Eclipse IDE, Visual Studio Code, MySQL |

## 4. System Architecture

FindBack follows a layered MVC-based architecture.

1. **Presentation Layer:** HTML, CSS, and JavaScript pages provide registration, login, dashboards, reporting forms, search, and management screens.
2. **Controller Layer:** Java Servlets receive HTTP requests, validate inputs and sessions, invoke service operations, and return JSON responses.
3. **Service Layer:** Interfaces define the operations available to the application.
4. **Service Implementation Layer:** Implements business logic, validation, password verification, matching, and workflow operations.
5. **Data Access Layer:** DAO classes execute SQL queries using JDBC and PreparedStatement.
6. **Model Layer:** Java classes represent application entities such as User, Organization, Item, and Claim.
7. **Database Layer:** MySQL stores organization, user, item, claim, session, status-history, and return records.

### Request Flow

`Frontend → Servlet Controller → Service Interface → Service Implementation → DAO → JDBC → MySQL`

The response travels back through the application layers and is returned to the frontend as JSON.

## 5. Project Structure

```text
FindBack/
│
├── frontend/
│   ├── index.html
│   ├── OrgRegister.html
│   ├── Login.html
│   ├── Register.html
│   ├── UserDashboard.html
│   ├── AdminDashboard.html
│   ├── AdminManagement.html
│   ├── Lost.html
│   ├── Found.html
│   ├── SearchItems.html
│   ├── MyActivity.html
│   ├── Welcome.html
│   ├── css/
│   │   └── style.css
│   └── js/
│       ├── Auth.js
│       ├── Common.js
│       ├── Items.js
│       ├── Admin.js
│       └── Welcome.js
│
├── backend/
│   └── FindBack/
│       └── src/
│           └── main/
│               ├── java/
│               │   ├── Controller/
│               │   ├── Data_Layer/
│               │   ├── Model_Layer/
│               │   ├── Service_Layer/
│               │   └── Service_Implementation/
│               └── webapp/
│                   └── WEB-INF/
│                       └── web.xml
│
├── database/
│   ├── schema.sql
│   └── demo-data.sql
│
├── .gitignore
└── README.md
```

## 6. Database Design

The application uses three separate logical MySQL databases.

### A. `findback_user_db`

Stores organization and authentication-related information.

| Table | Purpose |
|---|---|
| `organizations` | Organization details and organization codes |
| `users` | Member and administrator accounts |
| `sessions` | Hashed session tokens and expiry details |

### B. `findback_item_db`

Stores item reports and their status history.

| Table | Purpose |
|---|---|
| `items` | Lost and found item details |
| `item_status_history` | Tracks changes in item status |

### C. `findback_claim_db`

Stores claim and physical return information.

| Table | Purpose |
|---|---|
| `claims` | Member claim requests and administrator decisions |
| `returns` | Completed physical handovers |

Organization IDs are used to isolate records across workspaces. Since the databases are separate, relationships spanning databases are validated through application logic rather than cross-database foreign keys.

## 7. Item and Claim Workflows

### Item Status Workflow

- `LOST_OPEN` – A lost item has been reported.
- `FOUND_REPORTED` – A found item has been reported and is awaiting verification.
- `AVAILABLE` – An administrator has verified the found item.
- `CLAIM_PENDING` – A member has submitted a claim.
- `CLAIM_APPROVED` – The administrator has approved a claim.
- `RETURNED` – Physical handover has been confirmed.

### Claim Status Workflow

- `PENDING` – Awaiting administrator review.
- `APPROVED` – Claim accepted.
- `REJECTED` – Claim declined.
- `COLLECTED` – Item physically handed over to the claimant.

Every item-status transition is recorded in `item_status_history`.

## 8. How to Run the Project

### Prerequisites

Install the following:

- Java Development Kit 21.
- Apache Tomcat 9.
- MySQL Server.
- Eclipse IDE with Enterprise Java and Web Developer Tools.
- Visual Studio Code.
- VS Code Live Server extension.

### Step 1 – Clone the Repository

```bash
git clone https://github.com/YOUR-USERNAME/FindBack.git
cd FindBack
```

### Step 2 – Set Up the Databases

Start MySQL and create the three databases:

```sql
CREATE DATABASE findback_user_db;
CREATE DATABASE findback_item_db;
CREATE DATABASE findback_claim_db;
```

Import the database schema:

```bash
mysql -u root -p < database/schema.sql
```

Optional: import demonstration records:

```bash
mysql -u root -p < database/demo-data.sql
```

Use your own local MySQL credentials. Never commit database passwords.

### Step 3 – Configure Backend Dependencies

Import `backend/FindBack` into Eclipse as an existing project.

Configure the project to use:
- Java 21.
- Apache Tomcat 9.
- MySQL Connector/J.
- jBCrypt 0.4.
- JavaMail 1.6.2.
- JavaBeans Activation 1.2.0.

Backend Dependencies and JAR Files
FindBack uses external JAR libraries to establish database connectivity, handle password security, and send email notifications.
JAR file	Purpose
mysql-connector-j	MySQL JDBC driver used to connect Java with MySQL databases.
DbConnJar.jar	Reusable custom JDBC connection JAR containing JarJdbc.DBConn and the MyConnection(String databaseName) method.
jbcrypt-0.4.jar	Password hashing and password verification using BCrypt.
javax.mail-1.6.2.jar	Sending email notifications through Gmail SMTP.
javax.activation-1.2.0.jar	Supporting dependency for JavaMail.

Database Connection Implementation
The project uses a reusable JDBC connection utility instead of repeating database connection code in every DAO class.

The DBConn class provides:
DBConn.MyConnection("findback_user_db");
DBConn.MyConnection("findback_item_db");
DBConn.MyConnection("findback_claim_db");

This allows the application to connect independently to its three logical databases.

The project also uses a custom JDBC helper JAR that provides `JarJdbc.DBConn` and the `MyConnection(String databaseName)` method. This helper is not included in the public repository. Provide a compatible implementation and configure it locally to connect to the three databases.

Add the required JARs to the project's build path and deployment configuration. Avoid machine-specific absolute paths.

### Step 4 – Configure Email

Create a local `Email.properties` file on the backend classpath:

```properties
mail.username=your-email@gmail.com
mail.password=your-google-app-password
```

Use a Google App Password with Gmail SMTP enabled. Do not commit this file or share its contents.

The email acknowledgment is sent on the user's first successful login.

### Step 5 – Configure the Frontend API Origin

The frontend is configured to communicate with the backend at:

```text
http://localhost:8080/FindBack
```

The current CORS configuration allows the development frontend origin:

```text
http://127.0.0.1:5500
```

If you use another Live Server address or port, update the backend CORS configuration accordingly.

### Step 6 – Run the Backend

1. Start MySQL.
2. Open the backend project in Eclipse.
3. Configure Tomcat 9.
4. Run the project on the Tomcat server.
5. Confirm that the application is available at:

```text
http://localhost:8080/FindBack
```

### Step 7 – Run the Frontend

1. Open the `frontend` folder in Visual Studio Code.
2. Install or enable the Live Server extension.
3. Right-click `index.html`.
4. Select **Open with Live Server**.

The frontend should open at an address similar to:

```text
http://127.0.0.1:5500/index.html
```

Both the backend and frontend must be running for the complete application workflow.

## 9. Main Application Endpoints

The backend exposes servlet-based endpoints including:

| Endpoint | Method | Purpose |
|---|---|---|
| `/OrganizationRegisterServlet` | POST | Register organization and first admin |
| `/MemberRegisterServlet` | POST | Register organization member |
| `/LoginServlet` | POST | Authenticate user |
| `/SessionServlet` | GET | Validate session |
| `/LogoutServlet` | POST | End session |
| `/ItemServlet` | POST | Report lost or found item |
| `/AdminItemsServlet` | GET | Retrieve organization item reports |
| `/VerifyItemServlet` | POST | Verify found item |
| `/SearchItemsServlet` | GET | Search available found items |
| `/MatchingServlet` | GET | Retrieve possible item matches |
| `/ClaimServlet` | POST | Submit a claim |
| `/AdminClaimsServlet` | GET | Retrieve pending claims |
| `/UpdateClaimServlet` | POST | Approve or reject a claim |
| `/HandoverServlet` | POST | Confirm physical handover |
| `/AdminReturnsServlet` | GET | Retrieve completed returns |
| `/MyActivityServlet` | GET | Retrieve member activity |
| `/AdminDashboardServlet` | GET | Retrieve dashboard statistics |

## 10. Security Considerations

- Passwords are hashed using BCrypt.
- Session tokens are generated using secure random bytes.
- Only token hashes are stored in the sessions database.
- Session expiry is checked during validation.
- Role-based authorization protects member and administrator operations.
- Organization scope is derived from the authenticated session.
- SQL queries use PreparedStatement for parameterized database operations.
- Email and database credentials are configured locally and must not be committed.

## 11. Current Scope and Future Enhancements

FindBack focuses on digitizing the reporting, discovery, verification, and claim-management process while retaining physical verification and handover by the organization.

Possible future enhancements include:
- Email notifications for claim status changes.
- Item image uploads.
- Audit log exports and richer analytics.
- More advanced item similarity matching.
- Production deployment with HTTPS and stronger operational monitoring.

## 12. Author

**Poojitha Reddy**  
B.Tech – Information Technology

## 13. Project Summary

FindBack provides a structured way for organizations to manage lost and found belongings. By combining role-based access, organization-specific workspaces, item verification, rule-based matching, claim management, and traceable handovers, it makes the lost-and-found process easier to manage and follow.

**FindBack – Helping belongings find their way back.**
