package main

import (
	"flag"
	L "network-manager/lib"
	"path/filepath"
	"sync"
)

var nserver = L.Node{
	Host:       "6dist.idlab.uantwerpen.be",
	Port:       2013,
	Name:       "NamingServer",
	NSPort:     8090,
	NSHTTPPort: 8091,
	NSIP:       "172.18.0.5",
}
var servers = []L.Node{
	{
		Host:       "6dist.idlab.uantwerpen.be",
		Port:       2011,
		Name:       "Warre",
		NPort:      8010,
		NSIP:       nserver.NSIP,
		NSPort:     nserver.NSPort,
		NSHTTPPort: nserver.NSHTTPPort,
	},
	{
		Host:       "6dist.idlab.uantwerpen.be",
		Port:       2012,
		Name:       "Arvo",
		NPort:      8011,
		NSIP:       nserver.NSIP,
		NSPort:     nserver.NSPort,
		NSHTTPPort: nserver.NSHTTPPort,
	},
	{
		Host:       "6dist.idlab.uantwerpen.be",
		Port:       2014,
		Name:       "Daan",
		NPort:      8011,
		NSIP:       nserver.NSIP,
		NSPort:     nserver.NSPort,
		NSHTTPPort: nserver.NSHTTPPort,
	},
	{
		Host:       "6dist.idlab.uantwerpen.be",
		Port:       2015,
		Name:       "Mohammed",
		NPort:      8011,
		NSIP:       nserver.NSIP,
		NSPort:     nserver.NSPort,
		NSHTTPPort: nserver.NSHTTPPort,
	},
}

func main() {
	build := flag.Bool("build", true, "Build java jar file and upload it to the remote host")
	installDeps := flag.Bool("dependencies", true, "Install Java and Maven on the remote host")

	// Define where the node project is located: on local host
	projectDir, _ := filepath.Abs("../namenode")
	projectDirServer, _ := filepath.Abs("../nameserver")
	// Location JAR file: the same for all nodes
	jarFile := filepath.Join(projectDir, "target", "namenode-0.0.1-SNAPSHOT.jar")
	jarFileServer := filepath.Join(projectDirServer, "target", "Namingserver-0.0.1-SNAPSHOT.jar")

	// Parse CLI flags
	flag.Parse()

	if *build {
		// Build the jar file locally
		L.BuildJavaJar(projectDir, true)
		L.BuildJavaJar(projectDirServer, false)
	}

	L.SetupRemoteHost(nserver, *build, *installDeps, jarFileServer, false, false)

	// Configure all nodes
	var wg sync.WaitGroup

	for _, node := range servers {
		wg.Add(1)
		go func(n L.Node) {
			defer wg.Done()
			L.SetupRemoteHost(n, *build, *installDeps, jarFile, true, false)
		}(node) // pass node as argument to avoid loop variable capture issue
	}
	wg.Wait()
}
