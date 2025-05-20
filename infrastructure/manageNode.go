package main

import (
	"flag"
	L "network-manager/lib"
	"path/filepath"
)

var AddNamingServer = L.Node{
	Host:       "6dist.idlab.uantwerpen.be",
	Port:       2013,
	Name:       "NamingServer",
	NSPort:     8090,
	NSHTTPPort: 8091,
	NSIP:       "172.18.0.5",
}

func main() {
	// Define where the node project is located: on local host
	projectDir, _ := filepath.Abs("../namenode")
	// Location JAR file: the same for all nodes
	jarFile := filepath.Join(projectDir, "target", "namenode-0.0.1-SNAPSHOT.jar")

	// Read flags
	Nport := flag.Int("port", 0, "Port")
	port := flag.Int("host-port", 0, "Host port")
	name := flag.String("name", "", "Name")
	kill := flag.Bool("kill", false, "Kill")

	// Parse CLI flags
	flag.Parse()

	var node = L.Node{
		Host:       "6dist.idlab.uantwerpen.be",
		Port:       *port,
		Name:       *name,
		NPort:      *Nport,
		NSIP:       AddNamingServer.NSIP,
		NSPort:     AddNamingServer.NSPort,
		NSHTTPPort: AddNamingServer.NSHTTPPort,
	}
	if *kill {
		L.KillRemoteJar(node)
	} else {
		L.SetupRemoteHost(node, false, false, jarFile, true)
	}

}
