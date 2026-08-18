# ProjectConvert

ProjectConvert is a Spring Boot + MPXJ converter.

## What it does

- Reads project files with MPXJ `UniversalProjectReader`.
- Writes Primavera P6 XER.
- Writes Microsoft Project XML (MSPDI).
- Does **not** write MPP because MPXJ supports MPP as a read-only format.
- Preserves multiple projects when the source contains multiple schedules and the target is XER.
- For MSPDI output, the first project is exported because MSPDI is a single-project format.

## Requirements

- Java 17 or newer
- Maven 3.9 or newer

## Run

```bash
mvn spring-boot:run
```

Then open:

```text
http://localhost:8080
```

## Build a JAR

```bash
mvn clean package
java -jar target/projectconvert-1.0.0.jar
```

## API

`POST /api/convert`

Multipart form fields:

- `file`: source project file
- `target`: `xer` or `mspdi`

The response is the converted file as an attachment.
