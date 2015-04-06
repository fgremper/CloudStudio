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

##### 1. Clone the project.

```bash
git clone https://github.com/fgremper/CloudStudio.git
```

##### 2. Import into Eclipse

Import the 4 folders CSClient, CSServer, CSCommon and CSTesting as existing Eclipse projects. (Open Eclipse and go to File → Import and select Existing Projects into Workspace.)

##### 3. Build JAR

Go to File → Export → Java → JAR file.

To build the client JAR, select CSClient and CSCommon. To build the server JAR, select CSServer and CSCommon. In this step you can also set the output directory for the generated JAR file.

Click Next twice. Select "Generate the manifest file" and select the Main class (ClientMain.java or ServerMain.java).

##### 4. Run

Run the client:

```bash
java -jar CSClient.jar
```

Run the server:
```bash
java -jar CSServer.jar
```

##### Configure the client

In order to run the client JAR, you need to have a configuration file called `config.xml` in the same directory. Alternatively, you can also specify the path to a config file as the first parameter.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <username>John</username>
    <password>burgers</password>
    <serverUrl>http://cloudstudio.se.inf.ethz.ch:7330</serverUrl> <!-- don't put a dash at the end -->
    <repositories>
        <repository>
            <alias>RepositoryAliasOnCloudStudio</alias>
            <localPath>/path/to/your/local/repository</localPath>
        </repository>
    </repositories>
    <resubmitInterval>300</resubmitInterval> <!-- in seconds; if 0 it only submits once -->
</config>
```

The client provides a nice GUI to inform you of its status. At this point, however, setting up repositories still requires you to manually edit the XML file.
