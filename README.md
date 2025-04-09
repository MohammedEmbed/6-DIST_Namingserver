# Distributed Systems Project - Kaas & Wijn

## Overview
This project is a lightweight naming server designed to manage a distributed set of nodes by mapping service names to IP addresses. The system provides RESTful endpoints to register, lookup, and remove services dynamically.

## Project Structure
src/
│── controller/
│   ├── FileController.java   # TODO: File operations (placeholder)
│   ├── NodeController.java   # REST API for managing nodes
│── model/
│   ├── Node.java             # Data model for registered nodes
│── repository/
│   ├── IpRepository.java     # Handles JSON storage of node IPs
│── service/
│   ├── NameService.java      # Hashing logic for node identification


## Installation and Setup
TO DO
## Run nodes
````
start_nodes.bat
````
## API Endpoints
| Endpoint            | Method | Description |
|---------------------|--------|-------------|
| `/register`        | POST   | Registers a new service |
| `/lookup/<name>`   | GET    | Retrieves the address of a registered service |
| `/unregister/<name>` | DELETE | Removes a service from the registry |


## Contributors
- **Warros Hofmanos** (Project Lead)
- **Daanos DeKoningos**
- **Mohammedos Hamdouenos**
- **Arvos Cantos**


