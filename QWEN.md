# QWEN.md — Greenhouse Backend Project Context

## Projektübersicht

Dies ist das **Greenhouse Backend** — ein Quarkus-basiertes Java-Projekt (Java 25) für die Steuerung und Überwachung eines Gewächshauses. Es dient als Backend für IoT-Satelliten (ESP32-Mikrocontroller), die Sensordaten erfassen und Aktoren steuern.

### Technologie-Stack
- **Framework:** Quarkus 3.37.3 mit Panache ORM (JPA)
- **Sprache:** Java 25
- **Datenbank:** PostgreSQL mit pgvector Erweiterung für AI-Embeddings
- **API:** REST API mit JAX-RS 3.x und OpenAPI/Swagger-Dokumentation (`/q/docs/`)
- **Echtzeit-Kommunikation:** WebSocket-Support (Messungen, Relay-Logs)
- **Native Compilation:** GraalVM Native Image (ARMv8-a optimiert)
- **CI/CD:** CircleCI (Quality Gate + Native Build)
- **Containerisierung:** Docker mit docker-compose für lokale Entwicklung
- **AI/LLM:** Integration von Ollama (gemma-4 Chat-Modell, bge-small-en-v1.5 Embeddings)
- **RAG:** Retrieval-Augmented Generation mit Vektorsuche
- **Telegram Bot:** Benachrichtigungen über Telegram Bot API
- **IoT-Architektur:** Satelliten (ESP32) kommunizieren via HTTP REST API

## Build und Ausführung

### Lokale Entwicklung
```bash
# Projekt bauen
mvn clean package

# Quarkus Dev-Modus (empfohlen für Entwicklung)
mvn quarkus:dev

# Native Image erstellen
mvn clean package -Pnative -Dnative.image.docker.permission=security.write

# Docker Container erstellen
docker-compose up -d
```

### Abhängigkeiten
Hauptabhängigkeiten in `pom.xml`:
- `quarkus-rest` — REST API Framework
- `quarkus-hibernate-orm-panache` — JPA ORM mit Panache-Pattern
- `quarkus-jdbc-postgresql` — PostgreSQL JDBC Treiber
- `quarkus-websockets` — WebSocket Support
- `quarkus-scheduler` — Zeitgesteuerte Aufgaben
- `quarkus-security-jpa` — Sicherheit mit JPA
- `quarkus-mutiny` — Reaktive Programmierung
- `pgvector` — Vektorsuche für AI-Embeddings
- `cron-utils` — Cron-Expression Parsing

## Code-Struktur

```
src/main/java/de/hablijack/greenhouse/
├── ai/                          # AI/LLM Subsystem (RAG, Embeddings, Chat)
│   ├── api/                     # AI REST Resources (Admin, Ai, Plant)
│   │   └── dto/                 # AI DTOs
│   ├── config/                  # AI Konfiguration
│   ├── entity/                  # AI Entities (Plant)
│   ├── lifecycle/               # AI Initializer (Startup)
│   ├── llm/                     # LLM Client & Service
│   ├── rag/
│   │   ├── entity/              # RAG Entity (PlantKnowledgeDocument)
│   │   └── service/             # RAG Services (Ingestion, Embedding, Prompt, VectorSearch)
│   ├── schedule/                # AI Scheduler (Shadow Control)
│   └── service/                 # AI Services (AiService, Analyzer, Plant, RelayPulse, Safety, SensorHistory)
├── api/                         # REST API Endpoints (JAX-RS Resources)
│   ├── auth/                    # AuthenticationResource
│   ├── database/                # StatsResource (DB Stats)
│   ├── history/                 # HistoryResource (Messverlauf)
│   ├── planting/                # PlantingTrackResource
│   ├── pojo/                    # POJOs (TelegramTextMessage, etc.)
│   ├── relay/                   # RelayResource + RelayLogSocket (WebSocket)
│   ├── satellite/               # SatelliteResource
│   ├── sensor/                  # MeasurementResource, SensorResource + MeasurementSocket (WebSocket)
│   └── user/                    # UserResource
├── entity/                      # JPA Entities (Panache ORM)
├── lifecycle/                   # Application Lifecycle Hooks
├── microprofile.health/         # Health Checks
├── schedule/                    # Zeitgesteuerte Aufgaben
├── service/                     # Core Services
└── webclient/                   # HTTP Clients (TelegramClient, SatelliteClient)
    └── pojo/                    # Client POJOs
```

## Naming Conventions
- **Packages:** lowercase, keine Bindestriche
- **Klassen:** PascalCase, beschreibend (z.B. `SatelliteResource`, `GreenhouseService`)
- **Methoden:** camelCase, verben-basiert (z.B. `getSatellites()`, `updateMeasurement()`)
- **Konstanten:** UPPER_SNAKE_CASE
- **Variablen:** camelCase

### API-Design
- RESTful Endpoints mit JAX-RS 3.x Annotationen (`@Path`, `@GET`, `@POST`, etc.)
- Alle Resources unter `/api/rest` (außer AI Admin: `/api/rest/admin`)
- OpenAPI/Swagger Documentation für alle Endpoints (`/q/docs/`)
- Konsistente Fehlerbehandlung mit HTTP Status Codes
- JSON-Serialisierung mit Jackson
- WebSocket für Echtzeit-Daten (Messungen, Relay-Logs)

### Testing
- Unit Tests mit JUnit 5 und Mockito
- Integration Tests mit TestContainers
- REST API Tests mit RestAssured
- Code Coverage mit JaCoCo

## Wichtige Dateien und Verzeichnisse

### Konfiguration
- **`pom.xml`** — Maven Build-Konfiguration, Dependencies, Plugins
- **`src/main/resources/application.properties`** — Quarkus-Konfiguration (DB, AI, Scheduler, Telegram)
- **`.circleci/config.yml`** — CircleCI Konfiguration

### Core Business Logic
- **`src/main/java/de/hablijack/greenhouse/api/`** — 9 API Packages mit 14 REST/WebSocket Resources
- **`src/main/java/de/hablijack/greenhouse/service/`** — 3 Core Services (DatabaseStats, Satellite, Sensor)
- **`src/main/java/de/hablijack/greenhouse/entity/`** — 9 JPA Entities

### AI/LLM Subsystem
- **`src/main/java/de/hablijack/greenhouse/ai/`** — Vollständiges AI-Subsystem
  - `rag/service/` — RAG-Pipeline (DocumentIngestion, Embedding, PromptEnrichment, VectorSearch)
  - `llm/` — LLM Client und Service
  - `service/` — AI Business Logic (GreenhouseAnalyzer, PlantService, RelayPulseService, etc.)
  - `schedule/` — AI Shadow Control Scheduler
- Embedding-Generierung mit `bge-small-en-v1.5` via Ollama
- Vektorsuche mit pgvector Extension

### Scheduled Tasks
- **`src/main/java/de/hablijack/greenhouse/schedule/`** — Zeitgesteuerte Aufgaben:
  - Kamera-Snapshots
  - Bereinigungs-Jobs
  - Lüftersteuerung (mit Telegram-Benachrichtigung)
  - Gesundheitschecks
  - Messdatenerfassung
  - Wassersteuerung (mit Telegram-Benachrichtigung)
- **`src/main/java/de/hablijack/greenhouse/ai/schedule/`** — AI Shadow Control Scheduler

### CI/CD Pipeline
- **`.circleci/config.yml`** — CircleCI Konfiguration
  - Quality Gate Phase: Build, Tests, Code Quality (SpotBugs, PMD, Checkstyle)
  - Native Build Phase: GraalVM Native Image Compilation
  - Docker Image Build und Push

## Architektur-Details

### Datenmodell (9 JPA Entities)

| Entity | Beschreibung |
|--------|-------------|
| `Satellite` | ESP32-Geräte mit eindeutiger ID, Standort, Status |
| `Sensor` | Sensortypen an Satelliten |
| `Measurement` | Sensordaten (Temperatur, Luftfeuchtigkeit, Bodenfeuchtigkeit, Licht) |
| `Relay` | Aktoren (Lüfter, Bewässerung, etc.) |
| `RelayLog` | Protokollierung von Relay-Aktionen |
| `CameraPicture` | Kamera-Snapshots vom Greenhouse |
| `ConditionTrigger` | Schwellwert-basierte Trigger für automatische Steuerung |
| `TimeTrigger` | Zeitgesteuerte Trigger für automatische Steuerung |
| `User` | Benutzer für Authentifizierung und Berechtigungen |

### AI Entities (2 zusätzliche Entities)
- `Plant` — Pflanzeninformationen im AI-Subsystem
- `PlantKnowledgeDocument` — RAG-Dokumente mit Vektor-Embeddings

### AI/LLM Subsystem
Das Projekt nutzt Ollama als lokales LLM-Server:
- **Chat-Modell:** `gemma-4-e2b-it-q4_k_m.gguf` für natürliche Sprachinteraktionen
- **Embedding-Modell:** `bge-small-en-v1.5` für Text-Embeddings (384 Dimensionen)
- **RAG-Pipeline:**
  1. Dokumente werden in Embeddings konvertiert
  2. Embeddings werden in pgvector gespeichert
  3. Bei Abfragen wird Ähnlichkeitssuche durchgeführt
  4. Kontext wird an LLM für Antwortgenerierung übergeben
- **Shadow AI:** Experimentelles Feature für AI-gesteuerte Relay-Pulse (deaktivierbar)

### Satelliten-Kommunikation
ESP32-Satelliten kommunizieren via HTTP REST API:
- **Datenübertragung:** Sensormessungen an Backend senden
- **Befehlsentgegennahme:** Steuerbefehle vom Backend empfangen
- **Statusberichte:** Gesundheitsstatus und Batterielevel melden
- **Bildübertragung:** Kamera-Snapshots vom Greenhouse

### Telegram Bot Integration
Das System sendet Benachrichtigungen über die Telegram Bot API:
- **Lüftersteuerung:** FanControlScheduler sendet Notifications bei Lüfter-Ereignissen
- **Wassersteuerung:** WaterControlScheduler sendet Notifications bei Wasser-Ereignissen
- **AI Shadow Control:** AI-gesteuerte Relay-Pulse werden via Telegram kommuniziert
- **Client:** `TelegramClient` (REST Client Interface) für `sendMessage` und `sendPhoto`

### WebSocket-Kommunikation
- **MeasurementSocket:** Echtzeit-Messungsdaten (pfadparameter-basiert)
- **RelayLogSocket:** Echtzeit-Relay-Log-Einträge (pfadparameter-basiert)
- **Konfiguration:** `quarkus.websocket.dispatch-to-worker=true`

### Scheduled Tasks
Das Quarkus-Scheduler-Framework zeitgesteuerte Aufgaben:
- **Cron-basiert:** Konfigurierbar in `application.properties`
- **Reaktiv:** Nutzung von SmallRye Mutiny für asynchrone Verarbeitung
- **Fehlertolerant:** Retry-Logik und Error Handling

## Konfiguration

### application.properties (wichtige Eigenschaften)
```properties
# Datenbank
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=${DB_URL}
quarkus.datasource.username=${DB_USERNAME}
quarkus.datasource.password=${DB_PASSWORD}
quarkus.datasource.jdbc.max-size=16
quarkus.hibernate-orm.database.generation=update
quarkus.hibernate-orm.database.generation.create-schemas=true

# CORS (wichtig für Frontend-Entwicklung)
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:3000,https://garden.dedyn.io,http://zeus:5500,http://192.168.178.162:5500/
quarkus.http.cors.headers=accept, authorization, content-type, x-requested-with
quarkus.http.cors.methods=GET,POST,PUT

# WebSocket
quarkus.websocket.dispatch-to-worker=true

# Swagger/OpenAPI
quarkus.swagger-ui.always-include=true

# Telegram Bot
telegram.bot.token=SETMEATOKEN
telegram.bot.chatid=SETMEACHATID

# AI / LLM Configuration (Prefix: ai.llm.*)
ai.llm.base-url=http://192.168.178.84:11434
ai.llm.timeout=${AI_LLM_TIMEOUT:180000}
ai.llm.max-retries=${AI_LLM_MAX_RETRIES:3}
ai.llm.chat-model=gemma-4-e2b-it-q4_k_m.gguf
ai.llm.embedding-model=bge-small-en-v1.5
ai.llm.embedding-dimension=384
ai.llm.embedding-base-url=${AI_LLM_EMBEDDING_BASE_URL:${ai.llm.base-url}}

# RAG
ai.rag.enabled=true
ai.rag.max-documents=5
ai.rag.similarity-threshold=0.5

# Shadow AI (experimentell)
ai.shadow.enabled=false

# Satellite Client
quarkus.rest-client."de.hablijack.greenhouse.webclient.SatelliteClient".url=http://satellite.org
```

### Hibernate DDL-auto vs. Migration Tools
- **Aktuell:** `quarkus.hibernate-orm.database.generation=update` — Hibernate aktualisiert das Schema automatisch
- **Keine Flyway/Liquibase im Einsatz** (wird aber als zukünftige Option diskutiert)
- **Vorteil:** Schnelle Entwicklung, automatische Schema-Aktualisierung
- **Nachteil:** Keine versionierten Migrationen, Risiko bei Datenbank-Updates in Production

### Umgebungsvariablen
- `DB_USERNAME` — PostgreSQL Benutzername
- `DB_PASSWORD` — PostgreSQL Passwort
- `DB_URL` — JDBC-Verbindungszeichenfolge
- `AI_LLM_TIMEOUT` — LLM Timeout in ms (Default: 180000)
- `AI_LLM_MAX_RETRIES` — Max. Retry-Anzahl (Default: 3)
- `AI_LLM_EMBEDDING_BASE_URL` — Embedding-spezifische Base URL (Fallback: ai.llm.base-url)

## API-Endpunkte

Alle Endpoints sind über Swagger/OpenAPI dokumentiert: `http://localhost:8080/q/docs/`

### Core API (`/api/rest`)
| Resource | Package | Beschreibung |
|----------|---------|-------------|
| `AuthenticationResource` | `api.auth` | Authentifizierung |
| `StatsResource` | `api.database` | Datenbank-Statistiken |
| `HistoryResource` | `api.history` | Messverlauf abfragen |
| `PlantingTrackResource` | `api.planting` | Pflanzenschutz-Tracks |
| `RelayResource` | `api.relay` | Relay-Steuerung (Aktoren) |
| `SatelliteResource` | `api.satellite` | Satelliten-CRUD |
| `MeasurementResource` | `api.sensor` | Messungsdaten senden/abrufen |
| `SensorResource` | `api.sensor` | Sensoren verwalten (Achtung: Tippfehler im Dateinamen) |
| `UserResource` | `api.user` | Benutzer-Management |

### AI API (`/api/rest` und `/api/rest/admin`)
| Resource | Base Path | Beschreibung |
|----------|-----------|-------------|
| `AiResource` | `/api/rest` | AI-Chat und Abfragen |
| `PlantResource` | `/api/rest` | Pflanzen-Management |
| `AdminResource` | `/api/rest/admin` | AI-Admin-Funktionen |

### WebSocket Endpoints
| Socket | Beschreibung |
|--------|-------------|
| `MeasurementSocket` | Echtzeit-Messungsdaten |
| `RelayLogSocket` | Echtzeit-Relay-Logs |

### Gesamte Übersicht
- **14 REST/WebSocket Resources** in **9 API Packages**
- Alle Core-Endpoints unter `/api/rest`
- AI Admin-Endpoints unter `/api/rest/admin`
- Vollständige Dokumentation: `http://localhost:8080/q/docs/`

## Häufige Aufgaben für den Agent

### Neue API-Endpoints hinzufügen
1. Neue Klasse im passenden Package unter `api/` erstellen (z.B. `api/myfeature/MyResource.java`)
2. JAX-RS Annotationen verwenden (`@Path`, `@GET`, etc.)
3. OpenAPI Documentation mit `@Operation`, `@Response`
4. Service-Layer für Business Logic aufrufen
5. Tests in `src/test/java/` schreiben

### Neue JPA-Entities hinzufügen
1. Entity-Klasse in `entity/` erstellen (oder `ai/entity/` für AI-Entities)
2. `PanacheEntity` erweitern
3. Beziehungen mit `@OneToMany`, `@ManyToOne` definieren
4. Schema-Update durch Hibernate DDL-auto (oder Migration hinzufügen)

### Scheduled Tasks hinzufügen
1. Methode in `schedule/` Package erstellen (oder `ai/schedule/` für AI-Tasks)
2. `@Scheduled` Annotation mit Cron-Expression verwenden
3. Error Handling und Logging implementieren
4. In `application.properties` konfigurieren falls nötig

### Telegram-Benachrichtigungen hinzufügen
1. `TelegramClient` injizieren (REST Client Interface)
2. `telegram.bot.token` und `telegram.bot.chatid` in Konfiguration setzen
3. `sendMessage()` oder `sendPhoto()` aufrufen
4. In Scheduler oder Service integrieren

### AI/LLM Features erweitern
1. Embedding-Logik in `ai/rag/service/` anpassen
2. Neue Modelle in `application.properties` konfigurieren (Prefix: `ai.llm.*`)
3. Vektorsuche mit pgvector optimieren
4. RAG-Pipeline testen mit realen Daten

## Wichtige Hinweise

### Sicherheit
- Datenbankzugriff über parametrisierte Queries (SQL-Injection Prävention)
- Input-Validierung auf allen API-Endpoints
- Sensible Daten in Umgebungsvariablen speichern (nicht hardcoded)
- JPA-basierte Authentifizierung (`quarkus-security-jpa`)

### Performance
- Panache ORM für effiziente Datenbankoperationen
- Mutiny für asynchrone, nicht-blockierende Verarbeitung
- Connection Pooling für PostgreSQL (max-size=16)
- Embedding-Caching wo sinnvoll
- WebSocket dispatch to worker für Echtzeit-Daten

### Wartbarkeit
- Klare Trennung von API, Service und Entity Layer
- Konsistente Fehlerbehandlung
- Umfassende Logging
- Dokumentation in OpenAPI/Swagger

### Known Issues / Technical Debt
- **Tippfehler:** `SensorResouce.java` (sollte `SensorResource.java` sein)
- **Keine DB-Migrationstools:** Hibernate DDL-auto (`update`) statt Flyway/Liquibase — keine versionierten Migrationen
- **Hardcoded IPs:** `ai.llm.base-url=http://192.168.178.84:11434` sollte über Umgebungsvariablen konfiguriert werden

### Native Image
- GraalVM Native Image Support aktiviert
- Reflection Configuration für JSON Serialisierung
- DNS Resolver für Container-Umgebungen
- ARMv8-a Optimierung für Embedded Hardware

## Troubleshooting

### Häufige Probleme
1. **PostgreSQL Verbindung:** Prüfe `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
2. **Ollama nicht erreichbar:** Prüfe `ai.llm.base-url`, Docker Compose Services, Netzwerk-Konfiguration
3. **Native Build fehlschlägt:** Prüfe GraalVM Installation, Memory Limits
4. **Tests fehlschlagen:** Prüfe TestContainers, Port-Konflikte
5. **CORS-Fehler:** Prüfe `quarkus.http.cors.origins` — muss Origin des Frontends enthalten
6. **Telegram-Benachrichtigungen funktionieren nicht:** Prüfe `telegram.bot.token` und `telegram.bot.chatid`

### Debugging
- Quarkus Dev-Modus: `mvn quarkus:dev` (Hot Reload, Live Debugging)
- SQL Logging: `quarkus.datasource.jdbc.logging.enabled=true`
- OpenAPI UI: `http://localhost:8080/q/docs/`
- Quarkus Health Checks: `http://localhost:8080/q/health`

---

**Projekt-Status:** Dieses Backend ist Teil einer IoT-Architektur für Smart Greenhouse Automation. Alle Änderungen sollten die Integration mit ESP32-Satelliten, AI/LLM-Funktionalität und Telegram-Benachrichtigungen berücksichtigen.
