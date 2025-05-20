package lib

import (
	"context"
	"fmt"
	scp "github.com/bramvdbogaerde/go-scp"
	"github.com/bramvdbogaerde/go-scp/auth"
	"golang.org/x/crypto/ssh"
	"io"
	"io/ioutil"
	"log"
	"net"
	"os"
	"os/exec"
	"os/signal"
	"strconv"
	"syscall"
	"time"
)

const (
	remoteUser = "root"
	// TODO: replace with your key
	privateKeyPath = "c:\\Users\\warre\\.ssh\\id_rsa" // Change this!
	remoteJarPath  = "/root/node.jar"
	logFilePath    = "/root/"
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

func CreateSSHClient(remoteHost string) *ssh.Client {
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

func CreateSSHSession(sshClient *ssh.Client) *ssh.Session {
	session, err := sshClient.NewSession()
	if err != nil {
		log.Fatalf("SSH session failed: %v", err)
	}
	return session
}

func RemoteExecuteCmd(cmd string, sshClient *ssh.Client) {
	// Open ssh session
	session := CreateSSHSession(sshClient)
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

func BuildJavaJar(projectDir string, isNode bool) {
	fmt.Println("Building Maven project...")

	originalDir, err1 := os.Getwd()
	// Defer returning to the original working directory
	defer func() {
		if err2 := os.Chdir(originalDir); err2 != nil {
			log.Printf("Warning: Failed to return to original directory: %v", err2)
		}
	}()

	if err1 != nil {
		log.Fatalf("Failed to get current directory: %v", err1)
	}
	// Step 1: Change working directory
	err := os.Chdir(projectDir)
	if err != nil {
		log.Fatalf("Failed to change directory: %v", err)
	}

	// Step 2: Remove the target directory if it exists
	targetDir := projectDir + "/target"
	fmt.Printf("Removing existing target directory... %s", targetDir)

	fmt.Println("Removing existing target directory...")
	err = os.RemoveAll(targetDir)
	if err != nil {
		log.Fatalf("Failed to remove target directory: %v", err)
	}

	// Step 3: Install jade.jar to local Maven repo
	if isNode {
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
         apt-get remove --purge -y maven || true &&
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

func SendJAR(localJarPath string, scpClient scp.Client) {
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

func RunJarNode(hostInfo Node, sshClient *ssh.Client) {
	cmd := fmt.Sprintf(
		"java -DREMOTE=true -DNS_PORT=%d -DNS_HTTP_PORT=%d -DSERVER_PORT=%d -DSERVER_NAME=%s -DNS_IP=%s -jar %s --server.port=%d >> %slogs_%s.log 2>&1 &",
		hostInfo.NSPort,
		hostInfo.NSHTTPPort,
		hostInfo.NPort,
		hostInfo.Name,
		hostInfo.NSIP,
		remoteJarPath,
		hostInfo.NPort+1,
		logFilePath,
		hostInfo.Name,
	)

	RemoteExecuteCmd(cmd, sshClient)
}

func RunJarNS(hostInfo Node, sshClient *ssh.Client) {
	cmd := "java " +
		"-DREMOTE=true " +
		"-DNS_PORT=" + strconv.Itoa(hostInfo.NSPort) + " " +
		"-Dserver.port=" + strconv.Itoa(hostInfo.NSHTTPPort) + " " +
		"-Dname=" + hostInfo.Name + " " +
		"-jar " + remoteJarPath + " --server.port=" + strconv.Itoa(hostInfo.NSHTTPPort) + " >> " + logFilePath + "logs_" + hostInfo.Name + ".log 2>&1 &"

	RemoteExecuteCmd(cmd, sshClient)
}

func SetupRemoteHost(hostInfo Node, build bool, install bool, jarFile string, isNode bool, run bool) {
	fullHost := hostInfo.Host + ":" + strconv.Itoa(hostInfo.Port)
	if build {
		// Create SCP client using SSH private key
		scpClient := CreateSCPClient(fullHost)
		SendJAR(jarFile, scpClient)
		defer scpClient.Close()

	} else {
		log.Println("Skipping dependency installation.")
	}

	// Manually create SSH client (separate from SCP)
	sshClient := CreateSSHClient(fullHost)
	defer sshClient.Close()

	// Call the installer if flag is true
	if install {
		InstallJavaAndMaven(sshClient)
	} else {
		log.Println("Skipping dependency installation.")
	}
	if run {
		if isNode {
			CreateDirectories(hostInfo, sshClient)
			RunJarNode(hostInfo, sshClient)
		} else {
			RunJarNS(hostInfo, sshClient)
		}
	}
}

func StreamRemoteLogs(hostInfo Node, static bool) error {
	fullHost := hostInfo.Host + ":" + strconv.Itoa(hostInfo.Port)
	sshClient := CreateSSHClient(fullHost)
	defer sshClient.Close()

	session, err := sshClient.NewSession()
	if err != nil {
		return err
	}
	defer session.Close()

	session.Stdout = os.Stdout
	session.Stderr = os.Stderr
	if static {
		return session.Run("cat " + logFilePath + "logs_" + hostInfo.Name + ".log")
	}
	return session.Run("tail -f " + logFilePath + "logs_" + hostInfo.Name + ".log")
}

func DeleteLogFile(hostInfo Node, sshClient *ssh.Client) {
	logFile := fmt.Sprintf("%slogs_%s.log", logFilePath, hostInfo.Name)
	cmd := fmt.Sprintf(`rm -f "%s"`, logFile)
	RemoteExecuteCmd(cmd, sshClient)
}

func KillRemoteJar(hostInfo Node) {
	fullHost := hostInfo.Host + ":" + strconv.Itoa(hostInfo.Port)
	// Manually create SSH client (separate from SCP)
	sshClient := CreateSSHClient(fullHost)
	defer sshClient.Close()
	fullCmd := "pkill -f " + hostInfo.Name
	RemoteExecuteCmd(fullCmd, sshClient)
	DeleteLogFile(hostInfo, sshClient)
}

func CreateDirectories(hostInfo Node, sshClient *ssh.Client) {
	cmd := fmt.Sprintf(`
		mkdir -p "local_files_%[1]s" && \
		mkdir -p "logs_%[1]s" && \
		mkdir -p "replicated_files_%[1]s"
	`, hostInfo.Name)
	RemoteExecuteCmd(cmd, sshClient)
}

func OpenSSHTunnel(sshClient *ssh.Client, port int) {

	localListener, err := net.Listen("tcp", "localhost:"+strconv.Itoa(port))
	if err != nil {
		log.Fatalf("Failed to listen on local port: %s", err)
	}
	defer localListener.Close()

	log.Println("Tunnel running: localhost → remote")

	// Handle Ctrl+C
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		sig := <-sigChan
		log.Printf("Received signal: %s. Shutting down.", sig)
		localListener.Close()
		os.Exit(0)
	}()

	// Accept and proxy connections
	for {
		localConn, err := localListener.Accept()
		if err != nil {
			log.Println("Stopped accepting connections.")
			break
		}

		remoteConn, err := sshClient.Dial("tcp", "localhost:"+strconv.Itoa(port))
		if err != nil {
			log.Printf("Remote dial error: %s", err)
			localConn.Close()
			continue
		}

		go proxy(localConn, remoteConn)
		go proxy(remoteConn, localConn)
	}
}

func proxy(src, dst net.Conn) {
	defer src.Close()
	defer dst.Close()
	io.Copy(src, dst)
}
