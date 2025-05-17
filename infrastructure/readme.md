# Cloud deployment
## Prerequisites
### Install go
Go to https://go.dev/doc/install 

## Deployment
### Install dependencies on remote hosts
The following command can be used to prepare the remote hosts. It will install all needed dependencies.
````
go run ./setup.go --dependencies
````
### Push new Java code to remote hosts and start services
The following command can be used to build the JAR-file for the nodes and naming server before starting them. The JAR-files are build locally and pushed to the remote hosts and finally started. The command assumes that the needed dependencies are already satisfied on the remote hosts.
````
go run ./setup.go --install
````
### Only start services nodes and naming server
This command only starts the services assuming the remote hosts its dependence are satisfied and the JAR-files are present.
````
go run ./setup.go
````

### Running for the first time?
````
go run ./setup.go --dependencies --build
````