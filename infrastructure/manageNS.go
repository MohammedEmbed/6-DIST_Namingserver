package main

import (
	"flag"
	"log"
	L "network-manager/lib"
	"path/filepath"
)

var server = L.Node{
	Host:       "6dist.idlab.uantwerpen.be",
	Port:       2013,
	Name:       "NamingServer",
	NSPort:     8090,
	NSHTTPPort: 8091,
	NSIP:       "172.18.0.5",
}

func main() {
	kill := flag.Bool("kill", false, "Kill")

	// Parse CLI flags
	flag.Parse()

	// Define where the node project is located: on local host
	projectDirServer, _ := filepath.Abs("../nameserver")
	// Location JAR file: the same for all nodes
	jarFileServer := filepath.Join(projectDirServer, "target", "Namingserver-0.0.1-SNAPSHOT.jar")

	if *kill {
		L.KillRemoteJar(server)
	} else {
		L.SetupRemoteHost(server, false, false, jarFileServer, false, true)
		log.Println("Skipping JAR build installation.")
	}

}
