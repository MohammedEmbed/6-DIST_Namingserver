package lib

import (
    "context"
    "fmt"
    "io/ioutil"
    "log"
    "os"
    "os/exec"
    "time"
    "golang.org/x/crypto/ssh"
    scp "github.com/bramvdbogaerde/go-scp"
    "github.com/bramvdbogaerde/go-scp/auth"
)
const (
    remoteUser     = "root"
    // TODO: replace with your key
    privateKeyPath = "c:\\Users\\warre\\.ssh\\id_rsa" // Change this!
    remoteJarPath  = "/root/node.jar"
    logFile="/root/serverLogs.log"
)
func CreateSCPClient(remoteHost string) scp.Client {
    log.Printf("Connecting to %s with user %s and key at %s", remoteHost, remoteUser, privateKeyPath)

    clientConfig, err := auth.PrivateKey(remoteUser, privateKeyPath, ssh.InsecureIgnoreHostKey())
    if err != nil {
        log.Fatalf("Failed to read private key: %v", err)
    }
    scpClient := scp.NewClient(remoteHost, &clientConfig)
    err = scpClient.Connect()
    if err != nil {
        log.Fatalf("SCP connection failed: %v", err)
    }
    return scpClient
    //defer scpClient.Close()
}

func CreateSSHClient(remoteHost string) *ssh.Client  {
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
    return sshClient
}

func CreateSSHSession(sshClient *ssh.Client) *ssh.Session  {
    session, err := sshClient.NewSession()
    if err != nil {
        log.Fatalf("SSH session failed: %v", err)
    }
    return session
}

func RemoteExecuteCmd(cmd string, sshClient *ssh.Client) {
    // Open ssh session
    session :=CreateSSHSession(sshClient)
    defer session.Close()
    runCmd := fmt.Sprint(cmd)
    log.Printf("Executing remote command: %s", runCmd)
    session.Stdout = os.Stdout
    session.Stderr = os.Stderr

    if err := session.Run(runCmd); err != nil {
        log.Fatalf("Remote execution failed: %v", err)
    }
    log.Println("Remote execution complete.")
}

func BuildJavaJar(projectDir string)  {
    fmt.Println("Building Maven project...")

    // Step 1: Change working directory
    err := os.Chdir(projectDir)
    if err != nil {
        log.Fatalf("Failed to change directory: %v", err)
    }

    // Step 2: Remove the target directory if it exists
    targetDir := projectDir+"/target"
    fmt.Printf("Removing existing target directory... %s", targetDir)

    fmt.Println("Removing existing target directory...")
    err = os.RemoveAll(targetDir)
    if err != nil {
        log.Fatalf("Failed to remove target directory: %v", err)
    }


    // Step 3: Install jade.jar to local Maven repo
    installCmd := exec.Command("mvn", "install:install-file",
        "-Dfile=jade/lib/jade.jar",
        "-DgroupId=com.tilab.jade",
        "-DartifactId=jade",
        "-Dversion=4.6.0",
        "-Dpackaging=jar",
    )
    installCmd.Stdout = os.Stdout
    installCmd.Stderr = os.Stderr
    fmt.Println("Installing jade.jar to local Maven repository...")
    if err := installCmd.Run(); err != nil {
        log.Fatalf("Maven install failed: %v", err)
    }

    // Step 4: Clean and package the project
    packageCmd := exec.Command("mvn", "clean", "package", "-DskipTests")
    packageCmd.Stdout = os.Stdout
    packageCmd.Stderr = os.Stderr
    fmt.Println("Packaging Maven project...")
    if err := packageCmd.Run(); err != nil {
        log.Fatalf("Maven package failed: %v", err)
    }

    fmt.Println("Build complete.")
}



func InstallJavaAndMaven(sshClient *ssh.Client) {
    // Combined shell command to install Java and Maven
    // This assumes Debian-based system (Ubuntu, etc.)
    fullCmd := `
         apt-get remove --purge -y maven &&
         apt-get update &&
         apt-get install -y openjdk-17-jdk wget &&
        wget https://dlcdn.apache.org/maven/maven-3/3.9.4/binaries/apache-maven-3.9.4-bin.tar.gz -P /tmp &&
         tar xf /tmp/apache-maven-3.9.4-bin.tar.gz -C /opt &&
         ln -sfn /opt/apache-maven-3.9.4 /opt/maven &&
        echo 'export M2_HOME=/opt/maven' |  tee /etc/profile.d/maven.sh &&
        echo 'export PATH=${M2_HOME}/bin:${PATH}' |  tee -a /etc/profile.d/maven.sh &&
         chmod +x /etc/profile.d/maven.sh &&
        source /etc/profile.d/maven.sh &&
        mvn -version
        `
    log.Println("Installing Java and Maven on remote host...")
    RemoteExecuteCmd(fullCmd, sshClient)
}

func SendJAR(localJarPath string, scpClient scp.Client){
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
}

func RunJar(sshClient *ssh.Client)  {
    //Run the JAR file in a new Command Prompt window
    envString := "SERVER_PORT="+"8082" +" "+
        "SERVER_IP="+"127.0.0.1"+" "+
        "SERVER_NAME="+"warre"+" "+
        "NS_IP="+"143.169.218.121"+" "+
        "NS_PORT="+"8090"+" "+
        "REMOTE=true"

    cmd := envString + " java -jar " + remoteJarPath +" >> "+logFile+ " 2>&1 &"
    RemoteExecuteCmd(cmd, sshClient)
}

func SetupRemoteHost(remoteHost string, build bool,install bool,jarFile string)  {
    if build {
        // Create SCP client using SSH private key
        scpClient := CreateSCPClient(remoteHost)
        SendJAR(jarFile,scpClient)
        defer scpClient.Close()

    } else {
        log.Println("Skipping dependency installation.")
    }

    // Manually create SSH client (separate from SCP)
    sshClient := CreateSSHClient(remoteHost)
    defer sshClient.Close()

    // Call the installer if flag is true
    if install {
        InstallJavaAndMaven(sshClient)
    } else {
        log.Println("Skipping dependency installation.")
    }

    RunJar(sshClient)

}

func StreamRemoteLogs() error {
    // TO DO: dont hard code host
    sshClient := CreateSSHClient("6dist.idlab.uantwerpen.be:2011")
    defer sshClient.Close()

    session, err := sshClient.NewSession()
    if err != nil {
        return err
    }
    defer session.Close()

    session.Stdout = os.Stdout
    session.Stderr = os.Stderr

    return session.Run("tail -f " + logFile)
}