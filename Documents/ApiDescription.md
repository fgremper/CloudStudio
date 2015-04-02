# CloudStudio API Reference




## Introduction

The CloudStudio API exposes an interface to access and manipulate CloudStudio resources. All CloudStudio resources are accessed and manipulated in a similar way.

Requests to the CloudStudio API have to use either the GET or POST method. GET requests are used for functions that do not change the state of the database. POST requests are used for functions that make changes to the database.

The content type of requests to the CloudStudio API must be `application/x-www-form-urlencoded`. The response has content type `application/json. This asynchronism allows to provide parameters for both GET and POST requests similarly and still retrieve comprehensive JSON objects, and is used by many widely used APIs (e.g. SoundCloud).




## /api/login

Method: POST

Log into CloudStudio with your username and password. Returns a session ID that will be required for further API calls, as well as the username and user privileges.

#### Parameters

Parameter name        | Description
--------------------- | ------------------------------------------
username              | Your username
password              | Your password

#### Example

###### Request
```bash
curl "http://cloudstudio:7330/api/login" \
  --data "username=John" \
  --data "password=burgers"
```

###### Response
```json
{  
   "sessionId": "f40309335f82e044fa04c6f267aa62fd",
   "username": "John",
   "isAdmin": false,
   "isCreator": false
}
```




## /api/repositories

Method: GET

Retrieves a list of all repositories you have access to.

#### Parameters

Parameter name        | Description
--------------------- | ------------------------------------------
sessionId             | Your session ID

#### Example

###### Request
```bash
curl "http://cloudstudio:7330/api/repositories?
  sessionId=YOUR_SESSION_ID"
```

###### Response
```json
[  
   {  
      "repositoryAlias": "BankAccountDemo",
      "repositoryDescription": "Dealing with banks and accounts.",
      "repositoryUrl": "https://github.com/foo/bankaccountdemo",
      "repositoryOwner": "John",
      "users": [
         "David",
         "Isabelle",
         "John"
      ],
   },
   {
      (...)
   }
]
```




## /api/repositoryInformation

Method: GET

Retrieves a list of user and branches for a given repository.

#### Parameters

Parameter name        | Description
--------------------- | ------------------------------------------
sessionId             | Your session ID
repositoryAlias       | Repository alias

#### Example

###### Request
```bash
curl "http://localhost:7330/api/repositoryInformation?
  sessionId=YOUR_SESSION_ID&
  repositoryAlias=BankAccountDemo"
```

###### Response
```json
{  
   "repositoryUsers": [  
      "David",
      "Isabelle",
      "John"
   ],
   "repositoryBranches": [  
      "master",
      "test_branch"
   ]
}
```







## /api/branchAwareness

Method: GET

Retrieves branch level awareness information for a repository. For every branch, the active users represent the users that have this particular branch checked out currently.

For every user in branch, a relation to the origin is given. This value can be EQUAL, AHEAD, BEHIND, FORK, LOCAL_BRANCH or NOT_CHECKED_OUT.

Relationship with origin   | Description
-------------------------- | ------------------------------------------
EQUAL                      | The latest branch commit is the same for the user and the origin.
AHEAD                      | The user has made commits and is directly ahead of the origin.
BEHIND                     | New commits have been pushed to the origin and the user is directly behind.
FORK                       | The user has made commits but new commits have been pushed to the origin in the meantime.
LOCAL_BRANCH               | This branch is a local branch for the user.
NOT_CHECKED_OUT            | This branch hasn’t been checked out by the user.

For the relationships AHEAD, BEHIND and FORK a distance specifies the shortest distance between the current commit for the client and the origin.

For every user, the "lastUpdate" field refers to the last time that the client has sent an update to CloudStudio. "lastUpdateDiff" is the elapsed time since the last update, e.g. "2h" (2 hours) or "5d" (5 days).

#### Parameters

Parameter name        | Description
--------------------- | ------------------------------------------
sessionId             | Your session ID
repositoryAlias       | Repository alias

#### Example

###### Request
```bash
curl "http://localhost:7330/api/branchAwareness?
  sessionId=YOUR_SESSION_ID&
  repositoryAlias=BankAccountDemo"
```

###### Response
```json
{  
   "branches": [  
      {  
         "branch": "master",
         "activeUsers": [  
            {  
               "username": "David",
               "lastUpdate": "2015-03-27 20:10:03.0",
               "lastUpdateDiff": "3h"
            },
            {  
               "username": "John",
               "lastUpdate": "2015-03-27 21:33:41.0",
               "lastUpdateDiff": "2h"
            }
         ],
         "users":[  
            {  
               "username": "David",
               "relationWithOrigin": "FORK",
               "distanceFromOrigin": 2
            },
            {  
               "username": "Isabelle",
               "relationWithOrigin": "EQUAL"
            },
            {  
               "username": "John",
               "relationWithOrigin": "AHEAD",
               "distanceFromOrigin": 1
            }
         ]
      },
      {
         (...)
      }
   ]
}
```



## /api/fileAwareness

Method: GET

Retrieves file level awareness information for a repository and branch. All your files in a branch are compared to every other users’ files in the same or specified branch.

For every user a conflict type is set to either NO_CONFLICT, FILE_CONFLICT, CONTENT_CONFLICT.

Conflict Type              | Description
-------------------------- | ------------------------------------------
NO_CONFLICT                | The two files being compared are identical.
FILE_CONFLICT              | The two files being compared are different.
CONTENT_CONFLICT           | After further analysing conflicting files, by doing a three-way diff with a suitable common ancestor of both files, a merge conflict occurs.

Non-existing files are treated as empty files for these purposes.

#### Parameters

Parameter name        | Description
--------------------- | ------------------------------------------
sessionId             | Your session ID
repositoryAlias       | Repository alias
branch                | Branch from which your files are compared
compareToBranch       | Branch to which files of other users your files are compared to
showUncommitted       | If true, also take into account changes that have not been locally committed yet.
showConflicts         | If true, for all files with a FILE_CONFLICT, additionally run a content conflict analysis. If false, just compare the files by their hash.

#### Example

###### Request
```bash
curl "http://cloudstudio:7330/api/fileAwareness?
  sessionId=YOUR_SESSION_ID&
  repositoryAlias=BankAccountDemo&
  branch=master&
  compareToBranch=master&
  showUncommitted=false&
  showConflicts=true"
```

###### Response
```json
{  
   "files": [  
      {  
         "filename": "README",
         "users": [  
            {  
               "username": "David",
               "type": "FILE_CONFLICT"
            },
            {  
               "username": "Isabelle",
               "type": "CONTENT_CONFLICT"
            },
            {  
               "username": "John",
               "type": "NO_CONFLICT"
            }
         ]
      },
      {  
         "filename": "src/java/Main.java",
         "users": [  
            {  
               "username": "David",
               "type":"NO_CONFLICT"
            },
            {  
               "username": "Isabelle",
               "type":"FILE_CONFLICT"
            },
            {  
               "username": "John",
               "type":"NO_CONFLICT"
            }
         ]
      }
   ]
}
```










#### Error handling

An erroneous request results in a status code 400 (Bad Request) response. The response data is a JSON object as always and contains and error message.

Example

###### Response
```json
{
   "error": "Insufficient privileges"
}
```



