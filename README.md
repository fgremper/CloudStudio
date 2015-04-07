# CloudStudio

#### Introduction

CloudStudio delivers real-time status information about users collaborating on a project using Git and can detect conflicts early on.

#### Structure

File/folder      | Contents
---------------- | --------------------------------
`CSClient`       | Contains source files for the CloudStudio client
`CSServer`       | Contains source files for the CloudStudio server
`CSCommon`       | Contains source files shared between the CloudStudio client and server
`CSTesting`      | Contains tests to verify the correctness of CloudStudio client and server
`SQLInit.sql`    | Script to initialize MySQL database tables

#### Build and Run

##### 1. Clone the project.

```bash
git clone https://github.com/fgremper/CloudStudio.git
```

##### 2. Import into Eclipse

Import the 4 folders `CSClient`, `CSServer`, `CSCommon` and `CSTesting` as existing Eclipse projects. (Open Eclipse and go to File → Import and select Existing Projects into Workspace.)

##### 3. Build JAR

Go to File → Export → Java → Runnable JAR file.

Under _Launch configuration_, select ClientMain to build the client JAR. To build the server JAR, select ServerMain. Under _Library handling_, select _Package required libraries into generated JAR_.

Select the export destionation and click _Finish_.

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

A sample configuration file looks like this:

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
    <resubmitInterval>300</resubmitInterval> <!-- in seconds -->
</config>
```

The client provides a nice GUI to inform you of its status. At this point, however, setting up repositories still requires you to manually edit the XML file.

##### Configure the server

This is a sample server configuration:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
	<serverPort>7330</serverPort>
    <dbDriverClass>com.mysql.jdbc.Driver</dbDriverClass>
    <dbJdbcUrl>jdbc:mysql://localhost/cloudstudio</dbJdbcUrl>
    <dbUser>dbadmin</dbUser>
    <dbPassword>1234</dbPassword>
    <dbMinPoolSize>5</dbMinPoolSize>
    <dbAcquireIncrement>5</dbAcquireIncrement>
    <dbMaxPoolSize>20</dbMaxPoolSize>
    <dbMaxStatements>180</dbMaxStatements>
    <fileStorageDirectory>path/to/filestorage</fileStorageDirectory>
    <originStorageDirectory>path/to/origins</originStorageDirectory>
    <passwordSalt>GXSBML0EGjOMfqPzsznUCkK8ENP3lmOX</passwordSalt>
    <originUpdateInterval>300</originUpdateInterval>
</config>
```

Parameter name            | Description
------------------------- | --------------------------------
serverPort                | Port for the HTTP server hosting the API and the Web Interface
dbDriverClass             | JDBC driver
dbJdbcUrl                 | Database URL
dbUser                    | Database username
dbPassword                | Database password
dbMinPoolSize             | C3P0 paramater: minimum pool size
dbAcquireIncrement        | C3P0 paramater: aquire increment
dbMaxPoolSize             | C3P0 parameter: maximum pool size
dbMaxStatements           | C3P0 parameter: maximum database statements
fileStorageDirectory      | The database only stores file hashes. The file contents to the hashes are stored in this directory.
originStorageDirectory    | A clone of the remote repository is stored in this directory for all projects.
passwordSalt              | Salt for the password hash
originUpdateInterval      | How often to update remote repositories (in seconds)

Run `SQLInit.sql` to initialize the database (MySQL).

#### API Reference

See [here](https://github.com/fgremper/CloudStudio/blob/master/ApiReference.md) for the API documentation.
