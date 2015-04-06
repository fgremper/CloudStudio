# CloudStudio

#### Introduction

CloudStudio delivers real-time status information about users collaborating on a project using Git and can detect conflicts early on.

#### Structure

File/folder      | Contents
---------------- | --------------------------------
CSClient         | Contains source files for the CloudStudio client
CSServer         | Contains source files for the CloudStudio server
CSCommon         | Contains source files files shared between the CloudStudio client and server
CSTesting        | Contains tests to verify the correctness of CloudStudio client and server
SQLInit.sql      | Script to initialize MySQL database tables

#### Build

###### 1. Clone the project.

```bash
git clone https://github.com/fgremper/CloudStudio.git
```

###### 2. Import into eclipse

Import the 4 folders CSClient, CSServer, CSCommon and CSTesting as existing Eclipse projects. (Open Eclipse and go to File → Import and select Existing Projects into Workspace.)

###### 3. Build JAR

Go to File → Export → Java → JAR file.

To build the client JAR, select CSClient and CSCommon. To build the server JAR, select CSServer and CSCommon. In this step you can also set the output directory for the generated JAR file.

Click Next twice. Select "Generate the manifest file" and select the Main class (ClientMain.java or ServerMain.java).

###### 4. Setup config file

Put a config.xml (client) or serverConfig.xml (server) into the same directory. You can find a sample config in the repository under CSClient/config.xml or CSServer/serverConfig.xml.

Make changes to the config file as needed.

###### 5. Run

Run the client:

```bash
java -jar CSClient.jar
```

Run the server:
```bash
java -jar CSServer.jar
```

