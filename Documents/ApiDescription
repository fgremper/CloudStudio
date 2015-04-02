# CloudStudio API Reference

## Introduction

The CloudStudio API exposes an interface to access and manipulate CloudStudio resources. All CloudStudio resources are accessed and manipulated in a similar way.

Requests to the CloudStudio API have to use either the GET or POST method. GET requests are used for functions that do not change the state of the database. POST requests are used for functions that make changes to the database.

The content type of requests to the CloudStudio API must be "application/x-www-form-urlencoded". The response has content type "application/json". This asynchronism allows to provide parameters for both GET and POST requests similarly and still retrieve comprehensive JSON objects, and is used by many widely used APIs (e.g. SoundCloud).

## /api/login

Method: POST

Log into CloudStudio with your username and password. Returns a session ID that will be required for further API calls, as well as the username and user privileges.

### Parameters

Parameter name | Description
-------------- | -----------
username       | Your username
password       | Your password

### Example

```
curl "http://cloudstudio:7330/api/login" \
  --data "username=John" \
  --data "password=burgers"
```
```
{  
   "sessionId": "f40309335f82e044fa04c6f267aa62fd",
   "username": "John",
   "isAdmin": false,
   "isCreator": false
}
```

























/api/repositories

Method: GET

Retrieves a list of all repositories you have access to.

Parameters



Example




















/api/repositoryInformation

Method: GET

Retrieves a list of user and branches for a given repository.

Parameters



Example























/api/branchAwareness

Method: GET

Retrieves branch level awareness information for a repository. For every branch, the active users represent the users that have this particular branch checked out currently.

For every user in branch, a relation to the origin is given. This value can be EQUAL, AHEAD, BEHIND, FORK, LOCAL_BRANCH or NOT_CHECKED_OUT.



For the relationships AHEAD, BEHIND and FORK a distance specifies the shortest distance between the current commit for the client and the origin.

For every user, the “lastUpdate” field refers to the last time that the client has sent an update to CloudStudio. “lastUpdateDiff” is the elapsed time since the last update, e.g. “2h” (2 hours) or “5d” (5 days).

Parameters




















Example




















/api/fileAwareness

Method: GET

Retrieves file level awareness information for a repository and branch. All your files in a branch are compared to every other users’ files in the same or specified branch.

For every user a conflict type is set to either NO_CONFLICT, FILE_CONFLICT, CONTENT_CONFLICT.



Non-existing files are treated as empty files for these purposes.


Parameters










Example














Error handling

An erroneous request results in a status code 400 (Bad Request) response. The response data is a JSON object as always and contains and error message.

Example






