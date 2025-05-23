# Cloud deployment
## Prerequisites
### Install go
Go to https://go.dev/doc/install 

### Set ssh private key

Change the value of privateKeyPath in lib/utils to your path to the private ssh key associated with the public ssh keys on the VM's.

## Server configuration
### Install dependencies on remote hosts
The following command can be used to prepare the remote hosts. It will install all needed dependencies.
````
go run ./configureServers.go --dependencies
````
### Push new Java code to remote hosts and start services
The following command can be used to build the JAR-file for the nodes and naming server before starting them. The JAR-files are build locally and pushed to the remote hosts and finally started. The command assumes that the needed dependencies are already satisfied on the remote hosts.
````
go run ./configureServers.go --install
````

### Running for the first time?
Install dependencies and build/copy java JARS at the same time.
````
go run ./configureServers.go --dependencies --build
````

## Server management
### Start a node
A node can be deployed to one of the VM's using the command below. It has the following options that must be specified:

- **host-port**: The port of the VM, it identifies where the node must be deployed, the IP is defaulted to 6dist.idlab.uantwerpen.be.
- **port**: The port where the node will bind to on the VM.
- **name**: The name of the node, used to identify it.
````
go run ./manageNode.go --host-port {host port} --port {port} --name {name}
````
### Stop a node
A node can be stopped using the same command to start it, but the kill option needs to be passed.

````
go run ./manageNode.go --kill --host-port {host port} --port {port} --name {name}
````

### Start naming server
As the Naming server is always deployed to the same VM for simplicity, no options needs to be passed.

````
go run ./manageNS.go
````

### Stop naming server
To stop the naming server, the options kill needs to be passed again.
````
go run ./manageNS.go --kill
````

## Tunnel
To access the naming sever from your local machine, a ssh tunnel needs to be set up to forward the traffic.
````
go run ./tunnel.go
````

## Logs
Logs of nodes can be accessed from the local machine using the command below and following options.

- **port**: Port of the VM where te node/naming server is located
- **name**: Name of the node or naming server
- **static**: Set this option if you want a snapshot of the logs. If this option is not set, the logs are streamed!
````
go run ./logviewer.go --port {port} --name {name}
````
````
go run ./logviewer.go --port {port} --name {name} --static
````