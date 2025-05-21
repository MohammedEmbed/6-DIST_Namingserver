package main

import (
	L "network-manager/lib"
	"strconv"
)

var tunnelServer = L.Node{
	Host:       "6dist.idlab.uantwerpen.be",
	Port:       2013,
	Name:       "NamingServer",
	NSPort:     8090,
	NSHTTPPort: 8091,
	NSIP:       "172.18.0.5",
}

func main() {

	// Finally, setup tunnel to Naming server
	sshClient := L.CreateSSHClient(tunnelServer.Host + ":" + strconv.Itoa(tunnelServer.Port))
	defer sshClient.Close()
	L.OpenSSHTunnel(sshClient, tunnelServer.NSHTTPPort)

}
