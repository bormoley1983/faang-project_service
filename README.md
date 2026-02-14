# Project Service
Service responsible for managing projects, team members, vacancies, and related business logic.

## Quick start

Prerequisites:
- Java 21+ (JDK)
- Docker (for container runs)
- [faang-infra services](https://github.com/bormoley1983/faang-infra) running locally or accessible

Run locally:
```sh
./gradlew bootRun
```

Run tests:
```sh
./gradlew test --info
```

Build and run in Docker:
```sh
./gradlew build
docker build -t project-service .
docker run -p 8080:8080 project-service
```

## Configuration

Main config: [src/main/resources/application.yaml](src/main/resources/application.yaml)  
Test config: [src/test/resources/application-test.yaml](src/test/resources/application-test.yaml)

### Required Configuration Properties

**Database:**
- PostgreSQL connection settings (host, port, database name, credentials)

**S3 Service:**
- `services.s3.endpoint` - S3-compatible storage endpoint
- `services.s3.access-key` - Access key for S3
- `services.s3.secret-key` - Secret key for S3
- `services.s3.bucket-name` - Bucket name for file storage

**Redis:**
- Redis connection settings for pub/sub messaging and caching

### Test Configuration

For running tests, ensure the following properties are configured in `application-test.yaml`:

```yaml
services:
  s3:
    endpoint: http://localhost:9000
    access-key: test-access-key
    secret-key: test-secret-key
    bucket-name: test-bucket
```

## External Integrations

### Jira Integration

To work with Jira API, pass the following headers in your requests:
- `x-jira-username` - Jira user login
- `x-jira-password` - Jira user password or token
- `x-jira-base-url` - Base URL of Jira server

Example request using curl:
```shell
curl -X GET http://localhost:8080/your-endpoint \
     -H "x-jira-username: your_jira_login" \
     -H "x-jira-password: your_jira_password_or_token" \
     -H "x-jira-base-url: https://your-company.atlassian.net"
```

### Redis Messaging

Redis is used for pub/sub messaging patterns:
- Configuration: [RedisConfig](src/main/java/faang/school/projectservice/config/RedisConfig.java) - sets up RedisTemplate for convenient Redis operations
- Publishers and subscribers can be implemented for asynchronous event processing
- TTL-based caching is supported through Redis

**Note:** Base code structure and architecture patterns are based on [FAANG School](https://github.com/faang-school) educational project.