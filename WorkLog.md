## Work log

### Tasks

#### TODO

* user mgmt.: change password
* line conflict (gonna be a lot of subtasks) (edit: started adding subtasks below)
* think about file rename heuristics
* make sure binary files don't get submitted
* make sure session expiration works properly
* find out how to find the best merge-base-file commit
* make the server fetch origin (either directly by including client features or using "standalone" plugin [make user origin?!])
* API documentation
* last fetch (stat -c %Y .git/FETCH_HEAD, http://stackoverflow.com/questions/2993902/how-do-i-check-the-date-and-time-of-the-latest-git-pull-that-was-executed)

#### Done

* database connection pool
* user mgmt.: change repository owner
* web interface: encapsulate request in a function
* password management
* user management (add repo, del repo, add user to repo, del user from repo, change repo owner, change and enforce user privileges)
* upload non-committed files from plugin
* store uncommitted/committed views in db
* new and better query for class awareness view
* send depth of parent history (for branch view)
* get familiar with java diff library
* explicitly send branch: commitid in {} and active branch and modify timestamp from client for "branch view"

('TODO' as well as 'Done' is incomplete, because I forget to write stuff down here ~~a lot~~ sometimes, sorry! Trying to do it, though!)

### Daily info

#### Tuesday 27.01.2015

Did today: Password management (hashing), adding authorization support to local plugin, work on user management.

Will do tomorrow: Deal with non-committed files, work on user management.

#### Wednesday 28.01.2015

Did today: Extended and adapted SQL structure, work on usermanagement, submitting and storing uncommitted files in a useful way, local plugin clean-up.

Will do tomorrow: Refine search/filter for file conflicts.

#### Thursday 29.01.2015

Did today: Search/filter for file conflicts, some automated test sandpit setup. A lot of SQL writing. Encountered problems with SQL outer joins.

Will do tomorrow: More search/filter, prepare for meeting, code clean-up.

#### Friday 30.01.2015

Did today: Prepare for meeting, search/filter for file conflicts.

Will do tomorrow: Environment stuff.

#### Sunday 01.02.2015

Did today: DB connection pool, made paths relative, added a server config, planned new features.

Will do tomrrow: Distribute tasks until presentation over work days and start working.

#### Monday 02.02.2015

Did today: Distance to parent commits, explicit branch info, play around with diff algorithms.

#### Tueday:

Fetch the origin
Branch view

#### Wednesday

Find merge base for commit

#### Thursday

View diff between files

#### Friday

Diff3