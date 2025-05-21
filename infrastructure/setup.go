package main

import (
	"flag"
	"log"
	L "network-manager/lib"
	"os/exec"
	"path/filepath"
	"strconv"
	"sync"
)

var NamingServer = L.Node{
	Host:       "6dist.idlab.uantwerpen.be",
	Port:       2013,
	Name:       "NamingServer",
	NSPort:     8090,
	NSHTTPPort: 8091,
	NSIP:       "172.18.0.5",
}
var nodes = []L.Node{
	//{
	//	Host:       "6dist.idlab.uantwerpen.be",
	//	Port:       2011,
	//	Name:       "Warre",
	//	NPort:      8010,
	//	NSIP:       NamingServer.NSIP,
	//	NSPort:     NamingServer.NSPort,
	//	NSHTTPPort: NamingServer.NSHTTPPort,
	//},
	//{
	//	Host:       "6dist.idlab.uantwerpen.be",
	//	Port:       2012,
	//	Name:       "Arvo",
	//	NPort:      8011,
	//	NSIP:       NamingServer.NSIP,
	//	NSPort:     NamingServer.NSPort,
	//	NSHTTPPort: NamingServer.NSHTTPPort,
	//},
	//{
	//    Host:   "6dist.idlab.uantwerpen.be",
	//    Port:   2011,
	//    Name:   "Warre",
	//    NSIP:   "127.0.0.1",
	//    NSPort: 8090,
	//},
}

func main() {
	// Define where the node project is located: on local host
	projectDir, _ := filepath.Abs("../namenode")
	projectDirServer, _ := filepath.Abs("../nameserver")
	// Location JAR file: the same for all nodes
	jarFile := filepath.Join(projectDir, "target", "namenode-0.0.1-SNAPSHOT.jar")
	jarFileServer := filepath.Join(projectDirServer, "target", "Namingserver-0.0.1-SNAPSHOT.jar")

	// Read flags
	build := flag.Bool("build", false, "Build java jar file and upload it to the remote host")
	installDeps := flag.Bool("dependencies", false, "Install Java and Maven on the remote host")
	killAll := flag.Bool("killall", false, "Stop all remove nodes")

	// Parse CLI flags
	flag.Parse()

	// Call the installer if flag is true
	if *build {
		// Build the jar file locally
		L.BuildJavaJar(projectDir, true)
		L.BuildJavaJar(projectDirServer, false)
	} else {
		log.Println("Skipping JAR build installation.")
	}

	if *killAll {
		L.KillRemoteJar(NamingServer)
	} else {
		// Setup Naming server
		L.SetupRemoteHost(NamingServer, *build, *installDeps, jarFileServer, false, true)
		StartLogging(NamingServer)

	}

	// Configure all nodes
	var wg sync.WaitGroup

	for _, node := range nodes {
		wg.Add(1)
		go func(n L.Node) {
			defer wg.Done()
			if *killAll {
				L.KillRemoteJar(n)
			} else {
				L.SetupRemoteHost(n, *build, *installDeps, jarFile, true, true)
				StartLogging(n)
			}

		}(node) // pass node as argument to avoid loop variable capture issue
	}
	wg.Wait()

	if !*killAll {
		// Finally, setup tunnel to Naming server
		sshClient := L.CreateSSHClient(NamingServer.Host + ":" + strconv.Itoa(NamingServer.Port))
		defer sshClient.Close()
		L.OpenSSHTunnel(sshClient, NamingServer.NSHTTPPort)
	}

}

func StartLogging(hostInfo L.Node) {
	cmd := exec.Command("cmd", "/C", "start", hostInfo.Name+" - Logs", "cmd", "/K",
		"go", "run", "logviewer.go",
		"--port="+strconv.Itoa(hostInfo.Port),
		"--name="+hostInfo.Name,
	)
	err := cmd.Start()
	if err != nil {
		log.Fatalf("Failed to open terminal: %v", err)
	}
}
