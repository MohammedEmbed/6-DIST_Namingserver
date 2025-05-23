package main

import (
	"flag"
	L "network-manager/lib"
)

func main() {

	// Define command-line flags
	port := flag.Int("port", 1099, "Port of the node")
	name := flag.String("name", "default-node", "Name of the node")
	static := flag.Bool("static", false, "Static")

	flag.Parse() // Parse command-line flags

	// Create a Node object using parsed flags
	node := L.Node{
		Host: "6dist.idlab.uantwerpen.be",
		Port: *port,
		Name: *name,
	}

	err := L.StreamRemoteLogs(node, *static)
	if err != nil {
		return
	}
}
