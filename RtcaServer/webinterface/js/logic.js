var activeRepository = undefined;
var activeBranch = undefined;
var activeFile = undefined;
var activeUser = undefined;
var activeRepositoryUsers = undefined;
var activeRepositoryBranches = undefined;
var activeCompareToBranch = undefined;

var selectedUsers = undefined;
var showUncommitted = false;
var selectedAdditionalBranches = undefined;

var conflictType = "INTER_BRANCH_CONFLICTS"

var conflictType;

var login = undefined;

var apiPrefix = '/request'






$(function () {
    renderLoginView();
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

function renderLoginView() {
    $('body').html(new EJS({url: 'templates/login_view.ejs'}).render());
    $('#submitLogin').click(function () {
        sendRequest({
            name: 'login',
            data: { username: $('#username').val(), password: $('#password').val() },
            success: function(data) { // request success
                console.log("Login. Success: " + JSON.stringify(data));
                login = data;

                if (login.sessionId != undefined) {
                    loadRepositoryView();
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
                    loadRepositoryView();
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

function loadRepositoryView() {
    sendRequest({
        name: 'getRepositories',
        data: { sessionId: login.sessionId },
        success: function(data) {
            console.log("Load overview. Success: " + JSON.stringify(data));
            renderRepositoryView({ repositories: data, login: login });
        },
        error: function () {
            alert('Error loading list of repositories.');
        }
    });
}

function renderRepositoryView(data) {

    $('body').html(new EJS({url: 'templates/repository_view.ejs'}).render(data));

    // header
    $('#headerLogo').click(loadRepositoryView);
    $('#manageUsers').click(loadUsersView);

    // navigation bar
    $('#refresh').click(loadRepositoryView);

    // content
    $('#createRepository').click(loadCreateRepositoryView);
    $('.repository').click(function () {
        selectedUsers = [];
        loadBranchView($(this).data('alias'));
    });
    $('.addUserToRepository').click(function (e) {
        var usernameToAdd = prompt('Enter user to add to repository "' + $(this).data('repositoryalias') + '":');
        if (usernameToAdd == null) return;
        sendRequest({
            name: 'addUserToRepository',
            data: { repositoryAlias: $(this).data('repositoryalias'), username: usernameToAdd, sessionId: login.sessionId },
            success: function(data) {
                console.log("Add repository. Success: " + JSON.stringify(data));
                loadRepositoryView();
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
                loadRepositoryView();
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
                loadRepositoryView();
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
                loadRepositoryView();
            },
            error: function () {
                alert('Something went wrong when trying to modify the repository owner.');
            }
        });
        e.stopPropagation();
    });
}


/* CREATE REPOSITORY VIEW */

function loadCreateRepositoryView() {
    renderCreateRepositoryView({ login: login });
}

function renderCreateRepositoryView(data) {
    $('body').html(new EJS({url: 'templates/create_repository_view.ejs'}).render(data));

    // header
    $('#headerLogo').click(loadRepositoryView);
    $('#manageUsers').click(loadUsersView);

    // navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);
    
    $('#submitCreateRepository').click(function () {
        sendRequest({
            name: 'addRepository',
            data: { repositoryAlias: $('#repositoryAlias').val(), repositoryUrl: $('#repositoryUrl').val(), sessionId: login.sessionId },
            success: function(data) {
                console.log("Create repository. Success: " + JSON.stringify(data));

                loadRepositoryView();
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
    $('body').html(new EJS({url: 'templates/users_view.ejs'}).render(data));

    // header
    $('#headerLogo').click(loadRepositoryView);
    $('#manageUsers').click(loadUsersView);

    // navigation bar
    $('#refresh').click(loadUsersView);

    $('.repository').click(function () {
        loadRepositoryView($(this).data('alias'));
    });

    // content
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

function loadBranchView(repositoryAlias) {
    sendRequest({
        name: 'getRepositoryInformation',
        data: { sessionId: login.sessionId, repositoryAlias: repositoryAlias },
        success: function(data) {
            console.log("Get repository info. Success: " + JSON.stringify(data));

            activeRepository = repositoryAlias;
            activeRepositoryUsers = data.repositoryUsers;


            renderBranchView({ repositoryAlias: repositoryAlias, repositoryUsers: data.repositoryUsers, selectedUsers: selectedUsers });

        },
        error: function () {
            alert('Something went wrong when trying to load branch level awareness data.');
        }
    });
}


function renderBranchView(data) {
    $('body').html(new EJS({url: 'templates/branch_view.ejs'}).render(data));

    // header
    $('#headerLogo').click(loadRepositoryView);
    $('#manageUsers').click(loadUsersView);

    // navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);
    $('#refresh').click(function () { loadBranchView(activeRepository); });

    // filter
    $('#usersFilter').change(function () {
        selectedUsers = $.map($('#usersFilter option:selected'), function (o) { return o.value })

        // remove "":
        var index = selectedUsers.indexOf("");
        if (index > -1) {
            selectedUsers.splice(index, 1);
        }

        loadBranchViewTable(activeRepository);
    });
    $('select').chosen();

    loadBranchViewTable(activeRepository);
}

function loadBranchViewTable(repositoryAlias) {
    console.log('TRYING TO LOAD: ' + activeRepository);

    sendRequest({
        name: 'getBranchLevelAwareness',
        data: { sessionId: login.sessionId, repositoryAlias: repositoryAlias },
        success: function(data) {
            console.log("Get branch awareness. Success: " + JSON.stringify(data));
            renderBranchViewTable({ branches: data.branches, repositoryAlias: repositoryAlias, repositoryUsers: data.repositoryUsers, selectedUsers: selectedUsers });
        },
        error: function () {
            alert('Something went wrong when trying to load branch level awareness data.');
        }
    });

}

function renderBranchViewTable(data) {
    $('#branchTable').html(new EJS({url: 'templates/branch_view_table.ejs'}).render(data));

    // content
    $('.branch').click(function () {
        showUncommitted = false;
        selectedAdditionalBranches = [];
        loadFileView(activeRepository, $(this).data('branch'));
    });
}


/* LEVEL 2: FILE AWARENESS */


function loadFileView(repositoryAlias, branch) {
    sendRequest({
        name: 'getRepositoryInformation',
        data: { sessionId: login.sessionId, repositoryAlias: repositoryAlias },
        success: function(data) {
            console.log("Get repository info. Success: " + JSON.stringify(data));

            activeBranch = branch;
            activeRepositoryUsers = data.repositoryUsers;
            activeRepositoryBranches = data.repositoryBranches;

            renderFileView({ repositoryAlias: repositoryAlias, branch: branch, repositoryUsers: data.repositoryUsers, repositoryBranches: activeRepositoryBranches, selectedUsers: selectedUsers, selectedAdditionalBranches: selectedAdditionalBranches, showUncommitted: showUncommitted });
        },
        error: function () {
            alert('Something went wrong when trying to load file level awareness data.');
        }
    });
}


function renderFileView(data) {
    $('body').html(new EJS({url: 'templates/file_view.ejs'}).render(data));

    // header
    $('#headerLogo').click(loadRepositoryView);
    $('#manageUsers').click(loadUsersView);

    // navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);
    $('.loadBranchView').click(function () { loadBranchView(activeRepository); });
    $('#refresh').click(function () { loadFileView(activeRepository, activeBranch); });

    // filter
    $('#usersFilter').change(function () {
        selectedUsers = $.map($('#usersFilter option:selected'), function (o) { return o.value })

        // remove "":
        var index = selectedUsers.indexOf("");
        if (index > -1) {
            selectedUsers.splice(index, 1);
        }

        loadFileViewTable(activeRepository, activeBranch);
    });
    $('#branchesFilter').change(function () {
        selectedAdditionalBranches = $.map($('#branchesFilter option:selected'), function (o) { return o.value })

        // remove "":
        var index = selectedAdditionalBranches.indexOf("");
        if (index > -1) {
            selectedAdditionalBranches.splice(index, 1);
        }

        loadFileViewTable(activeRepository, activeBranch);
    });
    $('#uncommittedFilter').change(function () {
        showUncommitted = $('#uncommittedFilter').is(':checked');

        loadFileViewTable(activeRepository, activeBranch);
    });
    $('select').chosen();

    loadFileViewTable(activeRepository, activeBranch);
}

function loadFileViewTable(repositoryAlias, branch) {
    sendRequest({
        name: 'getFileLevelAwareness',
        data: { sessionId: login.sessionId, repositoryAlias: repositoryAlias, branch: branch, showUncommitted: showUncommitted, selectedAdditionalBranches: selectedAdditionalBranches },
        success: function(data) {
            console.log("Get file awareness. Success: " + JSON.stringify(data));
            renderFileViewTable({ branches: data.branches, repositoryAlias: repositoryAlias, branch: branch, selectedUsers: selectedUsers, showUncommitted: showUncommitted, selectedAdditionalBranches: selectedAdditionalBranches });
        },
        error: function () {
            alert('Something went wrong when trying to load file level awareness data.');
        }
    });
}

function renderFileViewTable(data) {
    $('#fileTable').html(new EJS({url: 'templates/file_view_table.ejs'}).render(data));

    // content
    $('.fileAndUser').click(function () {
        loadContentView(activeRepository, activeBranch, $(this).data('filename'), $(this).data('username'), $(this).data('comparetobranch'));
    });
}


/* LEVEL 3: LOAD CONTENT LEVEL AWARENESS */

function loadContentView(repositoryAlias, branch, filename, username, compareToBranch) {
    sendRequest({
        name: 'getContentLevelAwareness',
        data: { sessionId: login.sessionId, repositoryAlias: repositoryAlias, branch: branch, filename: filename, username: username, showUncommitted: showUncommitted, compareToBranch: compareToBranch },
        success: function(data) {
            console.log("Get content awareness. Success: " + JSON.stringify(data));
            activeFile = filename;
            activeUser = username;
            activeCompareToBranch = compareToBranch;
            renderContentView({ content: data.content, filename: filename, repositoryAlias: repositoryAlias, branch: branch, username: username, showUncommitted: showUncommitted });
        },
        error: function () {
            alert('Something went wrong when trying to load line level awareness data.');
        }
    });
}

function renderContentView(data) {
    
    $('body').html(new EJS({url: 'templates/content_view.ejs'}).render(data));

    // header
    $('#headerLogo').click(loadRepositoryView);
    $('#manageUsers').click(loadUsersView);

    // navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);
    $('.loadBranchView').click(function () { loadBranchView(activeRepository); });
    $('.loadFileView').click(function () { loadFileView(activeRepository, activeBranch); });
    $('.loadContentView').click(function () { loadFileView(activeRepository, activeBranch); });
    $('#refresh').click(function () { loadContentView(activeRepository, activeBranch, activeFile, activeUser, activeCompareToBranch); });

}

