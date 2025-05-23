# Distributed Systems Project - Kaas & Wijn

## Overview
This system implements a distributed file replication network with a central naming server and a ring-based node architecture. The naming server maintains metadata and coordinates node activity, while the nodes form a logical ring to replicate files among each other efficiently. A user-friendly GUI is included for automated node deployment and real-time monitoring, streamlining setup and management of the entire network.
## Project Structure

project-root/ \
│── infrastructure/ \
│── namenode/ \
│── nameserver/ \
│── NetworkManager/ 

The project contains the following sub-folders:

- infrastructure: Contains go scripts for server management
- namenode: contains the code for the nodes
- nameserver: contains the code for the Naming server
- NetworkManager: contians the code for the GUI, using the scripts of infrastructure


## Installation and Setup
Please checkout the [readme](infrastructure/readme.md) in infrastructure to configure the servers and start a tunnel.

## Running
Start the NetworkManager to start/stop the Naming server and add/remove nodes to the network.
## Contributors
- **Warre Hofmans**
- **Daan DeKoning**
- **Mohammed Hamdouen**
- **Arvo Cant**


