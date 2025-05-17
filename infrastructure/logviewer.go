package main

import (
	"flag"
	L "network-manager/lib"
)

func main() {

	// Define command-line flags
	host := flag.String("host", "127.0.0.1", "Host IP of the node")
	port := flag.Int("port", 1099, "Port of the node")
	name := flag.String("name", "default-node", "Name of the node")

	flag.Parse() // Parse command-line flags

	// Create a Node object using parsed flags
	node := L.Node{
		Host: *host,
		Port: *port,
		Name: *name,
	}

	err := L.StreamRemoteLogs(node)
	if err != nil {
		return
	}
}
