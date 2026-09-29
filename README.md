# CampusFind - Lost and Found Item Tracker for Campus

Spring Boot REST backend + MySQL, with a small HTML/CSS/JS front end served by the same app.

## What you need
- JDK 17 or newer (21 is fine)
- IntelliJ IDEA (Community is enough); it bundles Maven
- XAMPP (MySQL) or a normal MySQL server
- Postman

## Run it (5 steps)
1. **Start MySQL** in the XAMPP Control Panel (click *Start* next to MySQL). Apache is optional.
2. **Open the project**: IntelliJ > *File > Open* > select the `campusfind` folder (the one with `pom.xml`) > *Open as Project*. Wait for Maven to download dependencies (bottom-right progress bar).
3. Check `src/main/resources/application.properties`. The defaults match XAMPP (user `root`, empty password, port 3306). Change them if your MySQL differs. The `campusfind` database is **created automatically**.
4. Run `CampusFindApplication` (green play button next to `main`). Look for `Started CampusFindApplication` in the console.
5. Open **http://localhost:8080** in your browser, or use Postman against `http://localhost:8080/api`.

Tables (`users`, `categories`, `lost_reports`, `found_items`) are created by Hibernate on first start. You can see them in phpMyAdmin (`http://localhost/phpmyadmin`).

No IntelliJ? From this folder run: `mvn spring-boot:run`

## Demo accounts (created on first start)
| Role | Email | Password |
|---|---|---|
| Admin | admin@campus.com | admin123 |
| Staff | staff@campus.com | staff123 |
| Student | student@campus.com | student123 |

## Sample data (created on first start)
Besides the three demo accounts above, the app seeds extra users (`ravi.staff@campus.com` / `staff123`;
`priya@campus.com`, `arjun@campus.com`, `meena@campus.com` / `student123`), 8 found items and 8 lost reports
covering every status (OPEN, MATCHED, RETURNED; AVAILABLE, CLAIMED, RETURNED), so matching and the admin dashboard
have something to show. Sample reports are only added when the `lost_reports` and `found_items` tables are empty.
To reload them: delete the rows (or drop the `campusfind` database) and restart.

To register a new **ADMIN** through the API/UI you must send `adminCode` = `CAMPUS2026` (set in `application.properties`). Students and staff register freely.

## Database design
| Table | Columns | Relationships |
|---|---|---|
| users | id (PK), name, email (unique), password (BCrypt), role | 1 user -> many lost_reports, 1 staff -> many found_items |
| categories | id (PK), name (unique) | 1 category -> many lost_reports / found_items |
| lost_reports | id (PK), description, location, date_lost, status (OPEN/MATCHED/RETURNED), created_at, reported_by (FK users), category_id (FK categories), matched_found_item_id (FK found_items, nullable) | many-to-one to User, Category, FoundItem |
| found_items | id (PK), description, location, date_found, status (AVAILABLE/CLAIMED/RETURNED), created_at, reported_by (FK users), category_id (FK categories) | many-to-one to User, Category |

## Authentication
Login returns the user (`id`, `name`, `email`, `role`). Every other request sends that id in the header
`X-User-Id: <id>`. The service layer checks the user's role for each action. (Kept simple on purpose; a real
system would use JWT / Spring Security.)

## Endpoints
| Feature | Method and path | Who |
|---|---|---|
| Register | POST `/api/auth/register` | anyone |
| Login | POST `/api/auth/login` | anyone |
| List categories | GET `/api/categories` | anyone |
| Add category | POST `/api/categories` | admin |
| Create lost report | POST `/api/lost` | any logged-in user |
| List lost reports (filters: `categoryId`, `status`, `date`) | GET `/api/lost` | students see only their own |
| One lost report / delete (OPEN only) | GET, DELETE `/api/lost/{id}` | owner or admin |
| Create found item | POST `/api/found` | staff, admin |
| List found items (filters: `categoryId`, `status`, `date`) | GET `/api/found` | any logged-in user |
| Change found status | PUT `/api/found/{id}/status` body `{"status":"CLAIMED"}` | admin or the staff member who logged it |
| Delete found item (AVAILABLE only) | DELETE `/api/found/{id}` | admin or reporting staff |
| Possible matches | GET `/api/matches` or `/api/matches?lostId=1` | any logged-in user |
| Confirm a match | POST `/api/matches/confirm` body `{"lostReportId":1,"foundItemId":1}` | staff, admin |
| Admin dashboard counts | GET `/api/admin/dashboard` | admin |
| List users | GET `/api/admin/users` | admin |

**Dashboard meaning:** `pendingItems` = OPEN lost reports, `matchedItems` = MATCHED lost reports,
`returnedItems` = found items marked RETURNED.

**Matching rule:** same category AND at least one shared keyword in the description (or shared place word in the
location). Score = 2 x shared keywords + shared location words. Best score first.

## Business rules (enforced in the service layer, before saving)
1. A found item cannot be marked `RETURNED` unless it is `CLAIMED` first -> `400 Bad Request` with a clear message.
2. Only an admin or the staff member who logged the item can change its status -> `403 Forbidden`.

When an item is marked RETURNED, the lost report matched to it is automatically set to RETURNED as well.

## Error format (no stack traces)
```json
{ "timestamp": "...", "status": 400, "error": "Bad Request",
  "message": "A found item cannot be marked RETURNED unless it is first marked CLAIMED (current status: AVAILABLE)" }
```
Validation errors add a `fieldErrors` object, e.g. `{"description": "description is required"}`.

## Test in Postman
Import `CampusFind.postman_collection.json`. Run the requests from top to bottom (fresh database: admin id = 1,
staff id = 2, student id = 3; these are collection variables). The folder "Business rule checks" shows each rule
being violated and the error returned.

## Troubleshooting
- **`Communications link failure` / `Connection refused`**: MySQL is not running in XAMPP.
- **`Access denied for user 'root'`**: set the right `spring.datasource.password` in `application.properties`.
- **Port 3306 is used by another MySQL**: stop it, or change the port in the datasource URL.
- **`Port 8080 was already in use`**: add `server.port=8081` in `application.properties`.
- **Red imports in IntelliJ**: right-click `pom.xml` > *Maven > Reload project*.
- **Wrong Java version**: File > Project Structure > Project SDK = 17 or higher.
- **Want a clean database**: drop the `campusfind` database in phpMyAdmin and restart the app.
