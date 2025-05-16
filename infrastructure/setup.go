package main

import (
    "log"
    "flag"
    "os/exec"
    "path/filepath"
    L "network-manager/lib"
)



func main() {
    // Define where the node project is located
    projectDir, _ := filepath.Abs("../namenode")
    // Remote host
    remoteHost := "6dist.idlab.uantwerpen.be:2011"
    // Location JAR file
    jarFile := filepath.Join(projectDir, "target", "namenode-0.0.1-SNAPSHOT.jar")

    // Read flags
    build := flag.Bool("build", false, "Build java jar file and upload it to the remote host")
    installDeps := flag.Bool("dependencies", false, "Install Java and Maven on the remote host")

    // Parse CLI flags
    flag.Parse()

    // Call the installer if flag is true
    if *build {
        // Build the jar file locally
        L.BuildJavaJar(projectDir)
    } else {
        log.Println("Skipping JAR build installation.")
    }

    L.SetupRemoteHost(remoteHost,*build,*installDeps,jarFile)

    cmd := exec.Command("cmd", "/C", "start", "Log window", "cmd", "/K","go", "run", "logviewer.go")
    err := cmd.Start()
    if err != nil {
        log.Fatalf("Failed to open terminal: %v", err)
    }
}

