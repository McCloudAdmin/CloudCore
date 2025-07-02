# MCCloudAdmin Chat Worker

A background worker service that processes chat messages from Redis and stores them in MySQL for the MCCloudAdmin system.

## Features

- Processes chat messages from Redis queue
- Batch inserts messages into MySQL database
- Configurable batch sizes and polling intervals
- Automatic retry logic with configurable delays
- Graceful shutdown handling
- Connection pooling for both Redis and MySQL

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Redis server
- MySQL server
- MCCloudAdmin database

## Configuration

The worker uses a YAML configuration file (`config.yml`) that will be created automatically on first run with default values:

```yaml
database:
  host: localhost
  port: 3306
  database: mccloudadmin
  username: root
  password: ""
  pool_size: 10

redis:
  host: localhost
  port: 6379
  password: ""
  pool_size: 5

worker:
  batch_size: 100
  poll_interval_ms: 1000
  max_retries: 3
  retry_delay_ms: 5000
```

### Configuration Options

#### Database
- `host`: MySQL server hostname
- `port`: MySQL server port
- `database`: Database name
- `username`: Database username
- `password`: Database password
- `pool_size`: Connection pool size

#### Redis
- `host`: Redis server hostname
- `port`: Redis server port
- `password`: Redis password (leave empty if no password)
- `pool_size`: Connection pool size

#### Worker
- `batch_size`: Number of messages to process in each batch
- `poll_interval_ms`: Time to wait for new messages in milliseconds
- `max_retries`: Maximum number of retry attempts on errors
- `retry_delay_ms`: Delay between retry attempts in milliseconds

## Building

```bash
mvn clean package
```

## Running

### Using the startup script (recommended)
```bash
./start.sh
```

### Manual execution
```bash
java -jar target/mccloudadmin-worker-1.0-SNAPSHOT.jar [config.yml]
```

## Integration with MCCloudAdmin

This worker is designed to work with the MCCloudAdmin system. The main MCCloudAdmin plugins (BungeeCord/Velocity) will:

1. Capture chat messages from players
2. Queue them in Redis using the `mccloudadmin:chat:queue` key
3. The worker processes these messages and stores them in the `mccloudadmin_chatlogs` table

## Database Schema

The worker expects the following table structure:

```sql
CREATE TABLE IF NOT EXISTS mccloudadmin_chatlogs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    server VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted ENUM('true', 'false') DEFAULT 'false',
    INDEX idx_uuid (uuid),
    INDEX idx_created_at (created_at),
    INDEX idx_server (server)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

The worker will automatically create this table if it doesn't exist.

## Monitoring

The worker logs its activity to the console. Key log messages include:

- `INFO`: Successful message processing, queue sizes
- `WARNING`: Redis unavailability, partial batch failures
- `SEVERE`: Database connection errors, critical failures

## Troubleshooting

### Redis Connection Issues
- Verify Redis server is running
- Check Redis host, port, and password in config
- Ensure Redis is accessible from the worker machine

### Database Connection Issues
- Verify MySQL server is running
- Check database credentials in config
- Ensure the database exists and is accessible

### Performance Issues
- Increase `batch_size` for higher throughput
- Decrease `poll_interval_ms` for faster processing
- Adjust connection pool sizes based on load

## Deployment

For production deployment:

1. Build the project: `mvn clean package`
2. Copy the JAR file to your server
3. Create a configuration file with your production settings
4. Run the worker as a service (systemd, supervisor, etc.)
5. Monitor logs for any issues

## License

This project is part of the MCCloudAdmin system. 