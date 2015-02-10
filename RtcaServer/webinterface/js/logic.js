var activeRepository = undefined;
var activeBranch = undefined;


var showUncommitted = false;

var conflictType = "INTER_BRANCH_CONFLICTS"

var conflictType;

var login = undefined;

var apiPrefix = '/request'






$(function () {
    renderLogin();
});


/* UTILITY FUNCTIONS */

function sendRequest(requestObject) {
    $.ajax({
        url: apiPrefix + '/' + requestObject.name,
        type: 'POST',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify(requestObject.data),
        success: requestObject.success,
        error: requestObject.error
    });
}


/* LOGIN */

function renderLogin() {
    $('body').html(new EJS({url: 'templates/login.ejs'}).render());
    $('#submitLogin').click(function () {
        sendRequest({
            name: 'login',
            data: { username: $('#username').val(), password: $('#password').val() },
            success: function(data) { // request success
                console.log("Login. Success: " + JSON.stringify(data));
                login = data;

                if (login.sessionId != undefined) {
                    loadOverviewView();
                }
                else {
                    alert('Login error. Wrong username/password probably.');
                }
            },
            error: function () {
                alert('Something went wrong when logging in.');
            }
        });
    });
    $('#submitCreateUserAndLogin').click(function () {
        sendRequest({
            name: 'createUserAndLogin',
            data: { username: $('#newUsername').val(), password: $('#newPassword').val() },
            success: function(data) { // request success
                console.log("Login. Success: " + JSON.stringify(data));
                login = data;

                if (login.sessionId != undefined) {
                    loadOverviewView();
                }
                else {
                    alert('Login error. Wrong username/password probably.');
                }
            },
            error: function () {
                alert('Something went wrong when logging in.');
            }
        });
    });
    $('#username, #password').keypress(function(e) {
        if (e.which == 13) {
            $('#submitLogin').click();
        }
    });
    $('#newUsername, #newPassword').keypress(function(e) {
        if (e.which == 13) {
            $('#submitCreateUserAndLogin').click();
        }
    });
    $('#username').focus();
}


/* OVERVIEW: LIST OF ALL REPOSITORIES */

function loadOverviewView() {
    sendRequest({
        name: 'getRepositories',
        data: { sessionId: login.sessionId },
        success: function(data) {
            console.log("Load overview. Success: " + JSON.stringify(data));
            renderOverviewView({ repositories: data, login: login });
        },
        error: function () {
            alert('Error loading list of repositories.');
        }
    });
}

function renderOverviewView(data) {
    $('body').html(new EJS({url: 'templates/overview.ejs'}).render(data));
    $('#logo').click(loadOverviewView);
    $('#refresh').click(loadOverviewView);
    $('#manageUsers').click(loadUsersView);
    $('#createRepository').click(loadCreateRepository);
    $('.repository').click(function () {
        loadBranchLevelAwarenessView($(this).data('alias'));
    });
    
    $('.addUserToRepository').click(function (e) {
        var usernameToAdd = prompt('Enter user to add to repository "' + $(this).data('repositoryalias') + '":');
        if (usernameToAdd == null) return;
        sendRequest({
            name: 'addUserToRepository',
            data: { repositoryAlias: $(this).data('repositoryalias'), username: usernameToAdd, sessionId: login.sessionId },
            success: function(data) {
                console.log("Add repository. Success: " + JSON.stringify(data));
                loadOverviewView();
            },
            error: function () {
                alert('Something went wrong when trying to add a user to the repository.');
            }
        });
        e.stopPropagation();
    });
    $('.deleteUserFromRepository').click(function (e) {
        sendRequest({
            name: 'deleteUserFromRepository',
            data: { repositoryAlias: $(this).data('repositoryalias'), username: $(this).data('username'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Delete user from repository. Success: " + JSON.stringify(data));
                loadOverviewView();
            },
            error: function () {
                alert('Something went wrong when trying to delete a user from the repository.');
            }
        });
        e.stopPropagation();
    });
    $('.deleteRepository').click(function (e) {
        sendRequest({
            name: 'deleteRepository',
            data: { repositoryAlias: $(this).data('repositoryalias'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Delete repository. Success: " + JSON.stringify(data));
                loadOverviewView();
            },
            error: function () {
                alert('Something went wrong when trying to delete a repository.');
            }
        });
        e.stopPropagation();
    });
    $('.modifyRepositoryOwner').click(function (e) {
        var newRepositoryOwner = prompt('Enter new owner for repository "' + $(this).data('repositoryalias') + '":');
        if (newRepositoryOwner == null) return;
        sendRequest({
            name: 'modifyRepositoryOwner',
            data: { repositoryAlias: $(this).data('repositoryalias'), username: newRepositoryOwner, sessionId: login.sessionId },
            success: function(data) {
                console.log("Modify repository owner. Success: " + JSON.stringify(data));
                loadOverviewView();
            },
            error: function () {
                alert('Something went wrong when trying to modify the repository owner.');
            }
        });
        e.stopPropagation();
    });
}


/* CREATE REPOSITORY VIEW */

function loadCreateRepository() {
    renderCreateRepository({ login: login });
}

function renderCreateRepository(data) {
    $('body').html(new EJS({url: 'templates/create_repository.ejs'}).render(data));
    $('#logo').click(loadOverviewView);
    $('#manageUsers').click(loadUsersView);
    $('.repositoryViewButton').click(loadOverviewView);
    $('#submitCreateRepository').click(function () {
        sendRequest({
            name: 'addRepository',
            data: { repositoryAlias: $('#repositoryAlias').val(), repositoryUrl: $('#repositoryUrl').val(), sessionId: login.sessionId },
            success: function(data) {
                console.log("Create repository. Success: " + JSON.stringify(data));

                loadOverviewView();
            },
            error: function () {
                alert('Something went wrong when trying to create a repository.');
            }
        });
    });
}


/* USER MANAGEMENT VIEW */

function loadUsersView() {
    sendRequest({
        name: 'getUsers',
        data: { sessionId: login.sessionId },
        success: function(data) {
            console.log("Load users. Success: " + JSON.stringify(data));
            renderUsersView({ users: data, login: login });
        },
        error: function () {
            alert('Something went wrong when trying to load the list of users.');
        }
    });
}

function renderUsersView(data) {
    $('body').html(new EJS({url: 'templates/users.ejs'}).render(data));
    $('#logo').click(loadOverviewView);
    $('#refresh').click(loadUsersView);
    $('#manageUsers').click(loadUsersView);
    $('.repository').click(function () {
        loadRepositoryView($(this).data('alias'));
    });

    $('.deleteUser').click(function (e) {
        sendRequest({
            name: 'deleteUser',
            data: { username: $(this).data('username'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Delete user. Success: " + JSON.stringify(data));
                loadUsersView();
            },
            error: function () {
                alert('Something went wrong when trying to delete a user.');
            }
        });
        e.stopPropagation();
    });
    $('.makeUserAdmin').click(function (e) {
        sendRequest({
            name: 'makeUserAdmin',
            data: { username: $(this).data('username'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Make user admin. Success: " + JSON.stringify(data));
                loadUsersView();
            },
            error: function () {
                alert('Something went wrong when trying to make a user admin.');
            }
        });
        e.stopPropagation();
    });
    $('.revokeUserAdmin').click(function (e) {
        sendRequest({
            name: 'revokeUserAdmin',
            data: { username: $(this).data('username'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Revoke user admin. Success: " + JSON.stringify(data));
                loadUsersView();
            },
            error: function () {
                alert('Something went wrong when trying to revoke a users admin privileges.');
            }
        });
        e.stopPropagation();
    });
    $('.makeUserCreator').click(function (e) {
        sendRequest({
            name: 'makeUserCreator',
            data: { username: $(this).data('username'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Make user creator. Success: " + JSON.stringify(data));
                loadUsersView();
            },
            error: function () {
                alert('Something went wrong when trying to give a user repository creator privileges.');
            }
        });
        e.stopPropagation();
    });
    $('.revokeUserCreator').click(function (e) {
        sendRequest({
            name: 'revokeUserCreator',
            data: { username: $(this).data('username'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Revoke user creator. Success: " + JSON.stringify(data));
                loadUsersView();
            },
            error: function () {
                alert('Something went wrong when trying to remove a users creator privileges.');
            }
        });
        e.stopPropagation();
    });
}

/* LEVEL 1: BRANCH AWARENESS */

function loadBranchLevelAwarenessView(repositoryAlias) {
    sendRequest({
        name: 'getBranchLevelAwareness',
        data: { sessionId: login.sessionId, repositoryAlias: repositoryAlias },
        success: function(data) {
            console.log("Get branch awareness. Success: " + JSON.stringify(data));
            activeRepository = repositoryAlias;
            renderBranchLevelAwarenessView({ branches: data.branches, repositoryAlias: repositoryAlias });
        },
        error: function () {
            alert('Something went wrong when trying to load branch level awareness data.');
        }
    });
}


function renderBranchLevelAwarenessView(data) {
    $('body').html(new EJS({url: 'templates/branchawareness.ejs'}).render(data));
    $('#logo').click(loadOverviewView);
    $('.branch').click(function () {
        showUncommitted = false;
        loadFileLevelAwarenessView(activeRepository, $(this).data('branch'));
    });
    $('.repositoryViewButton').click(function () {
        loadOverviewView();
    });
    $('#refresh').click(function () {
        loadBranchLevelAwarenessView(activeRepository);
    });
}


/* LEVEL 2: FILE AWARENESS */

function loadFileLevelAwarenessView(repositoryAlias, branch) {
    sendRequest({
        name: 'getFileLevelAwareness',
        data: { sessionId: login.sessionId, repositoryAlias: repositoryAlias, branch: branch, showUncommitted: showUncommitted },
        success: function(data) {
            console.log("Get file awareness. Success: " + JSON.stringify(data));
            activeBranch = branch;
            renderFileLevelAwarenessView({ files: data.files, repositoryAlias: repositoryAlias, branch: branch, showUncommitted: showUncommitted });
        },
        error: function () {
            alert('Something went wrong when trying to load file level awareness data.');
        }
    });

}

function renderFileLevelAwarenessView(data) {
    $('body').html(new EJS({url: 'templates/fileawareness.ejs'}).render(data));


    $('#logo').click(loadOverviewView);
    $('.repositoryViewButton').click(function () {
        loadOverviewView();
    });
    $('.branchViewButton').click(function () {
        loadBranchLevelAwarenessView(activeRepository);
    });
    $('#submitFilter').click(function () {
        showUncommitted = $('#filterShowUncommitted').prop('checked');
        loadFileLevelAwarenessView(activeRepository, activeBranch);
    });
    $('#refresh').click(function () {
        loadFileLevelAwarenessView(activeRepository, activeBranch);
    });

    $('.fileAndUser').click(function () {
        loadContentLevelAwareness(activeRepository, activeBranch, $(this).data('filename'), $(this).data('username'));
    });

    /*
    $('.file').click(function () {
        loadFileView($(this).data('filename'));
    });
    $('#submitFilter').click(loadConflictsView);
    $('.file').click(function () {
        loadLineLevelAwareness(activeRepository, activeBranch, $(this).data('filename'), undefined);
    });
    */
}


/* LEVEL 3: LOAD CONTENT LEVEL AWARENESS */

function loadContentLevelAwareness(repositoryAlias, branch, filename, username) {
console.log({ sessionId: login.sessionId, repositoryAlias: repositoryAlias, branch: branch, filename: filename, username: username });
    sendRequest({
        name: 'getContentLevelAwareness',
        data: { sessionId: login.sessionId, repositoryAlias: repositoryAlias, branch: branch, filename: filename, username: username },
        success: function(data) {
            console.log("Get content awareness. Success: " + JSON.stringify(data));
            // renderLineLevelAwarenessView({ files: data.files, repositoryAlias: repositoryAlias, branch: branch });
        },
        error: function () {
            alert('Something went wrong when trying to load line level awareness data.');
        }
    });
}

function renderLineLevelAwarenessView(data) {
    
}

/*
function loadConflictsView() {
    sendRequest({
        name: 'getConflicts',
        data: { sessionId: login.sessionId, repositoryAlias: activeRepository, branch: $('#filterBranch').val(), viewUncommitted: $('#filterViewUncommitted').prop('checked'), users: $('#filterUsers option:selected').map(function() {return this.value;}).get(), filterUsers: $('#filterUsersBool').prop('checked') },
        success: function(data) {
            console.log("Get file conflicts. Got response: " + JSON.stringify(data));
            renderConflictsView({ conflicts: data.conflicts });
        },
        error: function () {
            console.log("Get file conflicts. Error.");
        }
    });
}


function renderConflictsView(data) {
    $('#conflicts').html(new EJS({url: 'templates/conflicts.ejs'}).render(data));
}
*/

/* load file view */

/*
function loadFileView(filename) {
    //renderFileView({ filename: filename, repositoryAlias: activeRepository });
}

function renderFileView(data) {
    /*
    $('body').html(new EJS({url: 'templates/file_view.ejs'}).render(data));
    $('.repositoryViewButton').click(function () {
        loadOverviewView();
    });
    $('.ConflictsViewButton').click(function () {
        loadConflictsView(activeRepository);
    });
    */
//}


/* add repository */

/*
function addRepository(repositoryAlias, repositoryUrl) {
    $.ajax({
        url: "/pull/addRepository",
        type: "POST",
        contentType: "application/json",
        data: {repositoryAlias: repositoryAlias, repositoryUrl: repositoryUrl},
        success: function(data) {
            console.log("Got response: " + data);
        },
        error: function () {
            console.log("Well, this didn't work! :O");
        }
    });
}

*/
