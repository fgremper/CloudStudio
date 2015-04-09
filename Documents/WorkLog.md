## Work log

### Tasks

#### TODO


>> clean up the code and do comments
>> changed errors to 400 (from 500) still everything OK (cause 500 is deprecated)
>> write api document
write tests (not done yet)
>> test for no common ancestor


> fix sql case sensitiviity
> fix active branch is null
> fix a bug with empty files where the diff3 output isn't in order
> fix ejs bracket bug
> fix db pool -> seems to work now... maybe i had a leak somewhere (pretty sure I did)

> add the remaining interface functionality, clean up interface
>> UPDATE LAST BRANCH ORIGIN UPDATE VIEW!!!!!!!!! -> put it into repository information lastOriginUpdate


>> * hide unused titles branch view
>>* build with maven
>>* no more absolute paths
>>* documentation on how to setup (README)
>>* 2 more settings: enableoriginupdater, create admin account
>>* favicon (@home)


>>* change password
>> * first run create admin account
>>* welcome after signup!

>>* run tests without server running (short circuit without http handler)

* go through webinterface and look through
* clean up intendation and look
* error msgs for sho

* who is responsible for package conflicts!

* add 3 new parameters to doc and debug that config read works
* changepw, setrepositoryinformation to api ref


click around and test :) all satisfactory? :>
gui explanation at the bottom (or question mark somewhere) -> external help

TODO sunday

send to martin


#### Done

(started doing this official task list somewhere halfway through the project...)
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
* make the server fetch origin (either directly by including client features or using "standalone" plugin [make user origin?!])
* send more branch level information
* find out where all users in a branch stand in relation to the origin
* pick one of two diff libraries and build side by side diff
* regularily fetch origin, general working with origin
* branch view
* see if the instability thing over long runtime still exists since changes to the db connection pool etc.
* remake a lot of the gui so it looks nice
* prepare presentation
* fix some bugs that crash over time (origin updater, uncommitted files from the local plugin)
* verify correctness of file compare sql when comparing to different branch
* package as jar, test everything with a repo on github
* send and show the active branch directly in the branch []
* have a new database table to store where the current 'clone' is -> possibly a number that goes up!
* update into the right folder!
* gui "conflict" box
* three way side by side info
* count conflicts with less computing power
* do three way diff
* package three way diff info in json object
* display the 3 way diff in client
* have a [conflict] checkbox in the line level
* client: comment the code more
* have a switch for showing line conflicts on a file level (we get the information from the server already? just now pipe all conflicts through the countconflicts mechanic)
* log4j for logging
* display file conflicts in file view as red
* clean up code in client
* api description first draft
* line conflict (gonna be a lot of subtasks) (edit: started adding subtasks below)
* reimplement that active branch thing with the new db syntax
* GUI for the client
* Client: When is the last time that CloudStudio accepted the change
* Better error handling (client, webinterface, server)
* Logout
* Display active branch
* Show only repositories you have permission to see
* Set session ID as cookie, stay logged in
* Icons
* Webinterface: history, forward/back buttons
* General webinterface overhaul
* Webinterface: Titles for where you're at
* Clean up code in the webinterface
* Webinterface: Folder/package view
* Webinterface: error overlay
* Fix the json string detection for local plugin /localState
* Remove old origin fetches regularily!
* Restyle the content diff
* Redesign icons
* Have a setup system that setups something nice that you can show to people and test
* Not /web/ but /
* Webinterface: bring back the filters
* Webinterface: severity selector
* Webinterface: branch view: last change of user!
* store last webinterface update
* make sure session expiration works properly -> 30 days
* dont rewrite files in the filestorage folder
* still problems with fetching all branches in origin?
* test branch commit level
* test file level
* test content level stuff
* Webinterface: create repository
* Webinterface: New user
* Webinterface: user management
* conflict view for uncommitted changes
* conflict view without common ancestor -> empty file


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

#### Tuesday 03.02.2015

Did today: Automated fetching of origin, work on branch view.

#### Wednesday 04.02.2015

Did today: Work on branch view.

#### Thursday 05.02.2015

Did today: Begin work on line awareness view, diff between files/lines.

#### Friday 06.02.2015

Did today: Work on file line view.

#### Monday 09.02.2015

Did today: Make sure everthing works in branch/file view.

#### Tuesday 10.02.2015

Did today: Basically remade content diff with a new library.

#### Wednesday 11.02.2015

Did today: Make sure things run smoothly overall. Everything looks fabulous in the GUI :)

#### Thursday 12.02.2015

Did today: Make sure things run smoothly overall. GUI. Select users and additional branch.

#### Friday 13.02.2015

Did today: Prepare presentation. Fix the origin updater problem over time. Fix the uncommitted files submit crash bug. Get an eiffel project for the demo.

#### Sunday 15.02.2015

Did today: Back to single connections for now, the c3p0 connection pool still has some over-time bug. Check if file sql is correct with diff branch and no 'where'. Test running from JAR. Test a bit.

#### Monday 16.02.2015

Did today: Prepare and demonstration and test run with Martin and Christian.

#### Tuesday 17.02.2015

Did today: Demo.

#### Wednesday 18.02.2014

Did today: Three-way merging for conflict detection setup. Stuff like cloning "increments" in separate directories. New db column for that also.

#### Saturday 21.02.2014

Did today: Three-way merging for conflict detection setup. JGit in the server.

#### Sunday 22.02.2014

Did today: More three-way conflict stuff.

#### Tuesday

Did today: Found problems with the jlibdiff implementation for the 3-way merge that I'm using, so I'm looking for something else.

#### Wednesday, Thursday

Feeling sick.

#### Saturday 28.02.2014

Did today: Researching about three-way merging a lot. Most probably going to incorporate the command-line diff3. Nothing good out there for Java and implementing something is probably error-prone and outside of the scope of this work.

