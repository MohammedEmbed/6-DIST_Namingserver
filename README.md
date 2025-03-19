# Distributed Systems Project - Kaas & Wijn

## Overview
This project implements a distributed system. The Naming Server serves as a directory service that maps logical names to system resources such as IP addresses or service endpoints. It allows clients and distributed components to locate services dynamically without hardcoding network addresses.

## Features
- **Centralized Name Resolution:** Maps logical names to system resources.
- **Dynamic Registration:** Services can register and update their addresses.
- **Lookup Services:** Clients can query the Naming Server to resolve service names.
- **Fault Tolerance:** Ensures availability through replication or persistent storage.
- **Concurrency Handling:** Supports multiple simultaneous clients.

## System Architecture
The system follows a client-server model where:
- **Naming Server** maintains a registry of service names and their network addresses.
- **Services** dynamically register themselves with the Naming Server.
- **Clients** query the Naming Server to obtain the addresses of required services.

## Components
- **Naming Server:** A central directory that maintains a name-to-address mapping.
- **Service Registration Module:** Allows services to dynamically register or update their locations.
- **Lookup Module:** Handles client requests for service resolution.
- **Persistent Storage (Optional):** Ensures state retention in case of failures.

## Installation and Setup
### Prerequisites
- Java 17
- Apache Maven (for dependency management and build)

### Running the Naming Server
1. Compile the project using Maven:
   ```bash
   mvn clean install
   ```
2. Run the Naming Server:
   ```bash
   java -jar target/naming-server.jar
   ```
By default, the server runs on `localhost:5000`. Modify the configuration for custom settings.

### Registering a Service
A service can register itself with the Naming Server using an API call:
```bash
curl -X POST http://localhost:5000/register -d '{"name": "ServiceA", "address": "192.168.1.10:8080"}' -H "Content-Type: application/json"
```

### Looking Up a Service
Clients can retrieve service addresses using:
```bash
curl -X GET http://localhost:5000/lookup/ServiceA
```
The server will respond with the registered address.

## API Endpoints
| Endpoint            | Method | Description |
|---------------------|--------|-------------|
| `/register`        | POST   | Registers a new service |
| `/lookup/<name>`   | GET    | Retrieves the address of a registered service |
| `/unregister/<name>` | DELETE | Removes a service from the registry |

## Future Enhancements
- **Discovery Service:** Enable automatic service discovery within the distributed system.
- **Replication:** Implement replication mechanisms for high availability.
- **Agents:** Introduce agents to monitor and manage distributed components.
- **GUI:** Develop a graphical interface for easier service management.
- **Remote Nodes:** Support for remote distributed nodes to enhance scalability.
- **Authentication and Security:** Implement authentication mechanisms to secure service registrations and lookups.

## Contributors
- **Warros Hofmanos** (Project Lead)
- **Daanos DeKoningos**
- **Mohammedos Hamdouenos**
- **Arvos Cantos**


---
This README provides an overview of the project, its functionality, setup instructions, and API usage. Modify it based on your specific implementation details.
