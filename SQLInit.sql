# /usr/local/mysql/bin/mysql -u dbadmin cloudstudio < /Users/novocaine/Documents/masterthesis/workspace/SQLInit.sql

DROP TABLE usersessions;
DROP TABLE useraccess;
DROP TABLE files;
DROP TABLE commithistory;
DROP TABLE repositories;
DROP TABLE users;

CREATE TABLE users
(
username VARCHAR(255) NOT NULL,
passwordhash VARCHAR(255) NOT NULL,
isadmin VARCHAR(5) NOT NULL,
iscreator VARCHAR(5) NOT NULL,
PRIMARY KEY (username)
);

CREATE TABLE repositories
(
repositoryalias VARCHAR(255),
repositoryurl VARCHAR(255),
repositoryowner VARCHAR(255),
PRIMARY KEY (repositoryalias),
FOREIGN KEY (repositoryowner) REFERENCES users (username) ON DELETE CASCADE
);

CREATE TABLE usersessions
(
sessionid VARCHAR(255) NOT NULL,
username VARCHAR(255) NOT NULL,
expires TIMESTAMP NOT NULL,
PRIMARY KEY (sessionid),
FOREIGN KEY (username) REFERENCES users (username) ON DELETE CASCADE
);

CREATE TABLE useraccess
(
username VARCHAR(255) NOT NULL,
repositoryalias VARCHAR(255) NOT NULL,
FOREIGN KEY (username) REFERENCES users (username) ON DELETE CASCADE,
FOREIGN KEY (repositoryalias) REFERENCES repositories (repositoryalias) ON DELETE CASCADE
);

CREATE TABLE files
(
repositoryalias VARCHAR(255),
username VARCHAR(255),
filename VARCHAR(255),
sha VARCHAR(255),
branch VARCHAR(255),
commit VARCHAR(255),
committed VARCHAR(11),
PRIMARY KEY (repositoryalias, username, filename),
FOREIGN KEY (username) REFERENCES users (username) ON DELETE CASCADE,
FOREIGN KEY (repositoryalias) REFERENCES repositories (repositoryalias) ON DELETE CASCADE
);

CREATE TABLE commithistory
(
repositoryalias VARCHAR(255),
username VARCHAR(255),
commit VARCHAR(255),
downstreamcommit VARCHAR(255),
PRIMARY KEY (repositoryalias, username, commit),
FOREIGN KEY (username) REFERENCES users (username) ON DELETE CASCADE,
FOREIGN KEY (repositoryalias) REFERENCES repositories (repositoryalias) ON DELETE CASCADE
);

