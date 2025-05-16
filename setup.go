package main

import (
    "context"
    "fmt"
    "io/ioutil"
    "log"
    "os"
    "time"

    "golang.org/x/crypto/ssh"

    scp "github.com/bramvdbogaerde/go-scp"
    "github.com/bramvdbogaerde/go-scp/auth"
)

const (
    remoteUser     = "remoteuser"
    remoteHost     = "192.168.1.101:22"
    privateKeyPath = "/home/youruser/.ssh/id_rsa" // Change this!
    localJarPath   = "my-agents.jar"
    remoteJarPath  = "/home/remoteuser/my-agents.jar"
)

func main() {
    // Create SCP client using SSH private key
    clientConfig, err := auth.PrivateKey(remoteUser, privateKeyPath, ssh.InsecureIgnoreHostKey())
    if err != nil {
        log.Fatalf("Failed to read private key: %v", err)
    }

    scpClient := scp.NewClient(remoteHost, &clientConfig)
    err = scpClient.Connect()
    if err != nil {
        log.Fatalf("SCP connection failed: %v", err)
    }
    defer scpClient.Close()

    // Open the local jar file
    file, err := os.Open(localJarPath)
    if err != nil {
        log.Fatalf("Failed to open JAR file: %v", err)
    }
    defer file.Close()

    log.Println("Uploading JAR...")
    err = scpClient.CopyFile(context.Background(), file, remoteJarPath, "0755")
    if err != nil {
        log.Fatalf("SCP file upload failed: %v", err)
    }
    log.Println("Upload complete.")

    // Manually create SSH client (separate from SCP)
    privateKey, err := ioutil.ReadFile(privateKeyPath)
    if err != nil {
        log.Fatalf("Failed to read private key file: %v", err)
    }

    signer, err := ssh.ParsePrivateKey(privateKey)
    if err != nil {
        log.Fatalf("Failed to parse private key: %v", err)
    }

    sshConfig := &ssh.ClientConfig{
        User: remoteUser,
        Auth: []ssh.AuthMethod{
            ssh.PublicKeys(signer),
        },
        HostKeyCallback: ssh.InsecureIgnoreHostKey(),
        Timeout:         10 * time.Second,
    }

    sshClient, err := ssh.Dial("tcp", remoteHost, sshConfig)
    if err != nil {
        log.Fatalf("SSH dial failed: %v", err)
    }
    defer sshClient.Close()

    session, err := sshClient.NewSession()
    if err != nil {
        log.Fatalf("SSH session failed: %v", err)
    }
    defer session.Close()

    runCmd := fmt.Sprintf("java -jar %s", remoteJarPath)
    log.Printf("Executing remote command: %s", runCmd)
    session.Stdout = os.Stdout
    session.Stderr = os.Stderr

    if err := session.Run(runCmd); err != nil {
        log.Fatalf("Remote execution failed: %v", err)
    }

    log.Println("Remote execution complete.")
}
