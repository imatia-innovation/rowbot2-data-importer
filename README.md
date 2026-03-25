# rowbot2-data-importer
On‑demand data ingestion service responsible for retrieving, validating and processing data from external sources.


# 🚀 Installing and Running the Project Locally

This document describes the steps to configure, and run the **Rowbot2 Data Importer** project in a local environment.

---

## 📋 Prerequisites

Before starting, ensure the following tools are installed:

- **Java 21**
- **Maven 3.9.6 or higher**

You can verify your versions with:

```bash
java -version
mvn -version
```

If you're working on Windows, it's recommended that you enable long path support to avoid issues when cloning or building the project:

```bash
git config --system core.longpaths true
```

## 📦 Project Installation

Once the repository is cloned, install the project with Maven:

```bash
mvn clean install
```

## ▶️ Running the Application

To run the application locally, use the _local_  Spring profile and specify the main application class _Rowbot2DataImporterApplication_.

#### 🟦 Option 1 — Run from the command line

```bash
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Dspring-boot.run.main-class=com.imatia.implatform.rowbot2.data.importer.boot.Rowbot2DataImporterApplication
```

#### 🟩 Option 2 — Run from your IDE (IntelliJ, Eclipse, VS Code)

1. Open the project in your IDE.
2. Run the main class:
   ````
    com.imatia.implatform.rowbot2.data.importer.boot.Rowbot2DataImporterApplication
   ````
3. Add this program argument:
    ````
   --spring.profiles.active=local
   ````

# 🌐 REST Endpoints

## 📡 Example: Sending a REST Request to `/externaldatasource`

This project exposes a REST endpoint that allows triggering an external datasource import.

### ▶️ HTTP Endpoint

**Method:** `POST`  
**URL:** `http://localhost:8085/externaldatasource`

### 📝 Request Body Example

```json
{
  "externalDatasourceId": 4,
  "resume": false,
  "tenantCallbackToken": "12345634548",
  "tenantDataSourceConnectionSettings": {
    "url": "jdbc:postgresql://localhost:5432/rowbot2_tenant_demo",
    "username": "rowbot2_tenant_demo_user",
    "password": "iLSWKIVYZgQJe85fnYJuoOCIV4DfS5Hj"
  }
}
