var activeRepository = undefined;
var activeBranch = undefined;
var activeFile = undefined;
var activeUser = undefined;
var activeRepositoryUsers = undefined;
var activeRepositoryBranches = undefined;
var activeCompareToBranch = undefined;

var selectedUsers = undefined;
var showUncommitted = false;
var showConflicts = false;
var selectedAdditionalBranches = undefined;

var conflictType = "INTER_BRANCH_CONFLICTS"

var conflictType;

var login = {};

var apiPrefix = '/api'





var webInterfacePrefix = "/web/";

$(function () {
    if (getCookie("sessionId") != null) {
        login = { sessionId: getCookie("sessionId"), username: getCookie("username"), isAdmin: (getCookie("isAdmin") == "true"), isCreator: (getCookie("isCreator") == "true") };
    }
    renderHeaderBar();
    renderFromURL();
});

window.onpopstate = function() {
    renderFromURL();
}

function renderFromURL() {
    var url = document.location.pathname;
    var params = url.split(/\//).splice(2);
    if (params[0] == "") renderLoginView();
    else if (params[0] == "repositories") loadRepositoryView();
}


function renderHeaderBar() {

    $('#headerBar').html('');
    $('#headerBar').append('<div class="headerBarLeft">CloudStudio</div>');
    if (login.sessionId != null) {
        $('#headerBar').append('<div class="headerBarRight" id="logout">Logout</div>');
        if (login.isAdmin) { $('#headerBar').append('<div class="headerBarRight">Manage Users</div>'); }
        $('#headerBar').append('<div class="headerBarRight">Profile</div>');
    }
    else {
        $('#headerBar').append('<div class="headerBarRight">Sign Up</div>');
        $('#headerBar').append('<div class="headerBarRight">Log In</div>');
    }


    $('#logout').click(function () {
        // Delete the login cookies
        setCookie("sessionId", login.sessionId, -1);
        setCookie("username", login.username, -1);
        setCookie("isCreator", login.isCreator, -1);
        setCookie("isAdmin", login.isAdmin, -1);

        // Delete the login variable
        login = {};

        // Render the header bar
        renderHeaderBar();

        // Render the login view
        renderLoginView();
    });

}

/* UTILITY FUNCTIONS */

function sendRequest(requestObject) {
    $.ajax({
        url: apiPrefix + '/' + requestObject.name,
        type: 'POST',
        dataType: 'json',
        contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
        data: requestObject.data,
        success: requestObject.success,
        error: requestObject.error
    });
}

function sendRequestGET(requestObject) {
    $.ajax({
        url: apiPrefix + '/' + requestObject.name,
        type: 'GET',
        dataType: 'json',
        contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
        data: requestObject.data,
        success: requestObject.success,
        error: requestObject.error
    });
}


/* LOGIN */

function renderLoginView() {
    

    $('#content').html(new EJS({url: webInterfacePrefix + 'templates/login_view.ejs'}).render());
    $('#submitLogin').click(function () {
        sendRequest({
            name: 'login',
            data: { username: $('#username').val(), password: $('#password').val() },
            success: function(data) { // request success
                console.log("Login. Success: " + JSON.stringify(data));
                login = data;


                renderHeaderBar();

                if (login.sessionId != undefined) {
                    loadRepositoryView();

                    if ($('#rememberMe').is(':checked')) {
                        setCookie("sessionId", login.sessionId, 10);
                        setCookie("username", login.username, 10);
                        setCookie("isCreator", login.isCreator, 10);
                        setCookie("isAdmin", login.isAdmin, 10);
                    }

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
    sendRequestGET({
        name: 'repositories',
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

    // set state
    history.pushState(null, "", webInterfacePrefix + "repositories");

    $('#content').html(new EJS({url: webInterfacePrefix + 'templates/repository_view.ejs'}).render(data));

    // header
    $('#headerLogo').click(loadRepositoryView);
    $('#manageUsers').click(loadUsersView);

    // navigation bar
    $('#refresh').click(loadRepositoryView);

    // content
    $('#createRepository').click(loadCreateRepositoryView);
    $('.repositoryListItem').click(function () {
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

    // set state
    history.pushState(null, "", webInterfacePrefix + "createRepository");

    $('#content').html(new EJS({url: webInterfacePrefix + 'templates/create_repository_view.ejs'}).render(data));

    // header
    $('#headerLogo').click(loadRepositoryView);
    $('#manageUsers').click(loadUsersView);

    // navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);
    
    $('#submitCreateRepository').click(function () {
        sendRequest({
            name: 'createRepository',
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

    // set state
    history.pushState(null, "", webInterfacePrefix + "users");

    $('#content').html(new EJS({url: webInterfacePrefix + 'templates/users_view.ejs'}).render(data));

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

    sendRequestGET({
        name: 'repositoryInformation',
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

    // set state
    history.pushState(null, "", webInterfacePrefix + "repositories/" + activeRepository);

    $('#content').html(new EJS({url: webInterfacePrefix + 'templates/branch_view.ejs'}).render(data));

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

    sendRequestGET({
        name: 'branchAwareness',
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
    $('#branchTable').html(new EJS({url: webInterfacePrefix + 'templates/branch_view_table.ejs'}).render(data));

    // content
    $('.branch').click(function () {
        showUncommitted = false;
        showConflicts = false;
        selectedAdditionalBranches = [];
        loadFileView(activeRepository, $(this).data('branch'));
    });
}


/* LEVEL 2: FILE AWARENESS */


function loadFileView(repositoryAlias, branch) {
    sendRequestGET({
        name: 'repositoryInformation',
        data: { sessionId: login.sessionId, repositoryAlias: repositoryAlias },
        success: function(data) {
            console.log("Get repository info. Success: " + JSON.stringify(data));

            activeBranch = branch;
            activeRepositoryUsers = data.repositoryUsers;
            activeRepositoryBranches = data.repositoryBranches;

            renderFileView({ repositoryAlias: repositoryAlias, branch: branch, repositoryUsers: data.repositoryUsers, repositoryBranches: activeRepositoryBranches, selectedUsers: selectedUsers, selectedAdditionalBranches: selectedAdditionalBranches, showUncommitted: showUncommitted, showConflicts: showConflicts });
        },
        error: function () {
            alert('Something went wrong when trying to load file level awareness data.');
        }
    });
}


function renderFileView(data) {

    // set state
    history.pushState(null, "", webInterfacePrefix + "repositories/" + activeRepository + "/" + activeBranch);

    $('#content').html(new EJS({url: webInterfacePrefix + 'templates/file_view.ejs'}).render(data));

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
    $('#conflictsFilter').change(function () {
        showConflicts = $('#conflictsFilter').is(':checked');

        loadFileViewTable(activeRepository, activeBranch);
    });
    $('select').chosen();

    loadFileViewTable(activeRepository, activeBranch);
}

function loadFileViewTable(repositoryAlias, branch) {
    sendRequestGET({
        name: 'fileAwareness',
        data: { sessionId: login.sessionId, repositoryAlias: repositoryAlias, branch: branch, showUncommitted: showUncommitted, showConflicts: showConflicts, selectedAdditionalBranches: selectedAdditionalBranches },
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
    $('#fileTable').html(new EJS({url: webInterfacePrefix + 'templates/file_view_table.ejs'}).render(data));

    // content
    $('.fileAndUser').click(function () {
        loadContentView(activeRepository, activeBranch, $(this).data('filename'), $(this).data('username'), $(this).data('comparetobranch'));
    });
}


/* LEVEL 3: LOAD CONTENT LEVEL AWARENESS */

function loadContentView(repositoryAlias, branch, filename, username, compareToBranch) {
    activeFile = filename;
    activeUser = username;
    activeCompareToBranch = compareToBranch;
    renderContentView({ sessionId: login.sessionId, repositoryAlias: repositoryAlias, branch: branch, filename: filename, username: username, showUncommitted: showUncommitted, showConflicts: showConflicts });
}


function renderContentView(data) {
    
    $('#content').html(new EJS({url: webInterfacePrefix + 'templates/content_view.ejs'}).render(data));

    // header
    $('#headerLogo').click(loadRepositoryView);
    $('#manageUsers').click(loadUsersView);

    // navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);
    $('.loadBranchView').click(function () { loadBranchView(activeRepository); });
    $('.loadFileView').click(function () { loadFileView(activeRepository, activeBranch); });
    $('.loadContentView').click(function () { loadFileView(activeRepository, activeBranch); });
    $('#refresh').click(function () { loadContentView(activeRepository, activeBranch, activeFile, activeUser, activeCompareToBranch); });

    $('#uncommittedFilter').change(function () {
        showUncommitted = $('#uncommittedFilter').is(':checked');

        loadContentViewDiff(activeRepository, activeBranch, activeFile, activeUser, activeCompareToBranch);
    });
    $('#conflictsFilter').change(function () {
        showConflicts = $('#conflictsFilter').is(':checked');

        loadContentViewDiff(activeRepository, activeBranch, activeFile, activeUser, activeCompareToBranch);
    });

    loadContentViewDiff(activeRepository, activeBranch, activeFile, activeUser, activeCompareToBranch);

}

function loadContentViewDiff(repositoryAlias, branch, filename, username, compareToBranch) {
    sendRequest({
        name: 'getContentLevelAwareness',
        data: { sessionId: login.sessionId, repositoryAlias: repositoryAlias, branch: branch, filename: filename, username: username, showUncommitted: showUncommitted, showConflicts: showConflicts, compareToBranch: compareToBranch },
        success: function(data) {
            console.log("Get content awareness. Success: " + JSON.stringify(data));
            renderContentViewDiff({ content: data.content, filename: filename, repositoryAlias: repositoryAlias, branch: branch, username: username, showUncommitted: showUncommitted, showConflicts: showConflicts });
        },
        error: function () {
            alert('Something went wrong when trying to load line level awareness data.');
        }
    });
}


function renderContentViewDiff(data) {
    if (showConflicts) {
        $('#diffTable').html(new EJS({url: webInterfacePrefix + 'templates/content_view_diff3.ejs'}).render(data));
    }
    else {
        $('#diffTable').html(new EJS({url: webInterfacePrefix + 'templates/content_view_diff.ejs'}).render(data));
    }
}











function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+d.toUTCString();
    document.cookie = cname + "=" + cvalue + "; " + expires;
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1);
        if (c.indexOf(name) == 0) return c.substring(name.length, c.length);
    }
    return null;
}

