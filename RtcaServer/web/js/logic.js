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



/* UTILITY FUNCTIONS */

// Send an API request
function sendApiRequest(requestObject) {
    requestObject.data.sessionId = login.sessionId;
    $.ajax({
        url: apiPrefix + '/' + requestObject.name,
        type: requestObject.type,
        dataType: 'json',
        contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
        data: requestObject.data,
        success: requestObject.success,
        error: requestObject.error
    });
}

// Display an error overlay
function displayError(module, errorObject) {

    if (errorObject == undefined) { errorObject = {} };
    var errorMessage = errorObject.error;
    if (errorMessage == undefined || errorMessage == "") errorMessage = "An error occured";

    $('#errorOverlayTitle').text(module + ' Error');
    $('#errorOverlayDescription').text(errorMessage);
    $('#errorOverlay').show();
    $('#errorOverlayClose').click(function () {
        $('#errorOverlay').hide();
    });

}



/* INITIALIZE */

// Document gets loaded for the first time
$(function () {

    // Do we have a session cookie?
    if (getCookie("sessionId") != null) {

        // Recreate login element from cookies
        login = {
            sessionId: getCookie("sessionId"),
            username: getCookie("username"),
            isAdmin: (getCookie("isAdmin") == "true"),
            isCreator: (getCookie("isCreator") == "true")
        };

        // Look at URL to find out what to render
        renderFromDocumentLocation();
    }
    else {
        // We're not logged in, render login view
        renderLoginView();
    }

    // Render header bar
    renderHeaderBar();

    // If the error overlay is visible and we press enter, close it
    $('body').keypress(function(e) {
        if (e.which == 13 && $('#errorOverlay').is(':visible')) {
            $('#errorOverlay').hide();
        }
    });

});

// Someone hit the history back of or forward button
window.onpopstate = function() {

    // Look at URL to find out what to render
    renderFromDocumentLocation();

}

// Set the URL to a certain path, only if it is different from the current path
function pushHistoryState(path) {

    // Don't push the state if it's the same URL as where we are now.
    // This would otherwise prevent the (history) forward button from working.
    if (document.location.pathname == webInterfacePrefix + path) return;

    // Push the history state
    history.pushState(null, "", webInterfacePrefix + path);

}

// Look at URL to find out what to render
function renderFromDocumentLocation() {

    var url = document.location.pathname;
    var params = url.split(/\//).splice(2);

    if (params[0] == "") {
        renderLoginView();
    }
    else if (params[0] == "repositories" && (params[1] == undefined || params[1] == "")) {
        loadRepositoryView();
    }
    else if (params[0] == "repositories" && !(params[1] == undefined || params[1] == "") && (params[2] == undefined || params[2] == "")) {
        selectedUsers = [];
        loadBranchView(params[1]);
    }
    else if (params[0] == "repositories" && !(params[1] == undefined || params[1] == "") && !(params[2] == undefined || params[2] == "")) {
        selectedUsers = [];
        showUncommitted = false;
        showConflicts = false;
        selectedAdditionalBranches = [];
        activeRepository = params[1];
        loadFileView(params[1], params[2]);
    }
    else {
        renderLoginView();
    }
}



/* RENDERING THE HEADER BAR */

function renderHeaderBar() {

    // Rendering the header HTML
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

    // Clicking the logout button
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



/* LOGIN */

function renderLoginView() {
    
    // Push history state
    pushHistoryState("");

    // Render template
    $('#content').html(new EJS({url: webInterfacePrefix + 'templates/login_view.ejs'}).render());

    // Register login click event
    $('#submitLogin').click(function () {
        sendApiRequest({
            name: 'login',
            type: 'POST',
            data: { username: $('#username').val(), password: $('#password').val() },
            success: function(data) {
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
                    displayError('Login', { error: 'No session ID reveived' });
                }

            },
            error: function (data) {
                displayError('Login', data.responseJSON);
            }
        });
    });

    // Submit form when we press enter while we're in one of the input fields
    $('#username, #password').keypress(function(e) {
        if (e.which == 13 && !$('#errorOverlay').is(':visible')) {
            $('#submitLogin').click();
        }
    });

    // Focus on the username input field
    $('#username').focus();
}



/* REPOSITORY OVERIEW */

function loadRepositoryView() {

    // Send api request
    sendApiRequest({
        name: 'repositories',
        type: 'GET',
        data: { sessionId: login.sessionId },
        success: function(data) {
            console.log("Load overview. Success: " + JSON.stringify(data));

            //Render
            renderRepositoryView({ login: login, repositories: data });
        },
        error: function (data) {
            displayError('Repository Overview', data.responseJSON);
        }
    });

}

function renderRepositoryView(data) {

    // Set state
    pushHistoryState("repositories");

    // Render template
    $('#content').html(new EJS({url: webInterfacePrefix + 'templates/repository_view.ejs'}).render(data));

    // Clicking on a repository
    $('.repositoryListItem').click(function () {
        selectedUsers = [];
        loadBranchView($(this).data('alias'));
    });

    /*
    // content
    $('#createRepository').click(loadCreateRepositoryView);
    $('.addUserToRepository').click(function (e) {
        var usernameToAdd = prompt('Enter user to add to repository "' + $(this).data('repositoryalias') + '":');
        if (usernameToAdd == null) return;
        sendApiRequest({
            name: 'addUserToRepository',
            type: 'POST',
            data: { repositoryAlias: $(this).data('repositoryalias'), username: usernameToAdd, sessionId: login.sessionId },
            success: function(data) {
                console.log("Add repository. Success: " + JSON.stringify(data));
                loadRepositoryView();
            },
            error: function (data) {
                alert('Something went wrong when trying to add a user to the repository.');
            }
        });
        e.stopPropagation();
    });
    $('.deleteUserFromRepository').click(function (e) {
        sendApiRequest({
            name: 'deleteUserFromRepository',
            type: 'POST',
            data: { repositoryAlias: $(this).data('repositoryalias'), username: $(this).data('username'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Delete user from repository. Success: " + JSON.stringify(data));
                loadRepositoryView();
            },
            error: function (data) {
                alert('Something went wrong when trying to delete a user from the repository.');
            }
        });
        e.stopPropagation();
    });
    $('.deleteRepository').click(function (e) {
        sendApiRequest({
            name: 'deleteRepository',
            type: 'POST',
            data: { repositoryAlias: $(this).data('repositoryalias'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Delete repository. Success: " + JSON.stringify(data));
                loadRepositoryView();
            },
            error: function (data) {
                alert('Something went wrong when trying to delete a repository.');
            }
        });
        e.stopPropagation();
    });
    $('.modifyRepositoryOwner').click(function (e) {
        var newRepositoryOwner = prompt('Enter new owner for repository "' + $(this).data('repositoryalias') + '":');
        if (newRepositoryOwner == null) return;
        sendApiRequest({
            name: 'modifyRepositoryOwner',
            type: 'POST',
            data: { repositoryAlias: $(this).data('repositoryalias'), username: newRepositoryOwner, sessionId: login.sessionId },
            success: function(data) {
                console.log("Modify repository owner. Success: " + JSON.stringify(data));
                loadRepositoryView();
            },
            error: function (data) {
                alert('Something went wrong when trying to modify the repository owner.');
            }
        });
        e.stopPropagation();
    });
    */
}



/* BRANCH LEVEL AWARENESS */

function loadBranchView(repositoryAlias) {

    // Send api request
    sendApiRequest({
        name: 'repositoryInformation',
        type: 'GET',
        data: { repositoryAlias: repositoryAlias },
        success: function(data) {
            console.log("Get repository info. Success: " + JSON.stringify(data));

            // Set our active variables
            activeRepository = repositoryAlias;
            activeRepositoryUsers = data.repositoryUsers;

            // Render branch view
            renderBranchView({ login: login, repositoryAlias: repositoryAlias, repositoryUsers: data.repositoryUsers });
        },
        error: function (data) {
            displayError('Branch Level Awareness', data.responseJSON);
        }
    });
}

function renderBranchView(data) {

    // set state
    pushHistoryState("repositories/" + activeRepository);

    $('#content').html(new EJS({url: webInterfacePrefix + 'templates/branch_view.ejs'}).render(data));

    // Navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);

    /*
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
    */

    loadBranchViewTable(activeRepository);
}


function loadBranchViewTable(repositoryAlias) {

    // Send API request
    sendApiRequest({
        name: 'branchAwareness',
        type: 'GET',
        data: { repositoryAlias: repositoryAlias },
        success: function(data) {
            console.log("Get branch awareness. Success: " + JSON.stringify(data));

            // Render table
            renderBranchViewTable({ branches: data.branches, repositoryAlias: repositoryAlias });
        },
        error: function (data) {
            displayError('Branch Level Awareness', data.responseJSON);
        }
    });

}

function renderBranchViewTable(data) {

    // Render template
    $('#branchListContainer').html(new EJS({url: webInterfacePrefix + 'templates/branch_view_table.ejs'}).render(data));

    // Click on a branch event
    $('.branchListItem').click(function () {
        showUncommitted = false;
        showConflicts = false;
        selectedAdditionalBranches = [];
        loadFileView(activeRepository, $(this).data('branch'));
    });

}



/* FILE LEVEL AWARENESS */

function loadFileView(repositoryAlias, branch) {

    // Send API request
    sendApiRequest({
        name: 'repositoryInformation',
        type: 'GET',
        data: { repositoryAlias: repositoryAlias },
        success: function(data) {
            console.log("Get repository info. Success: " + JSON.stringify(data));

            // Set active variables
            activeBranch = branch;
            activeRepositoryUsers = data.repositoryUsers;
            activeRepositoryBranches = data.repositoryBranches;

            // Render
            renderFileView({ login: login, repositoryAlias: repositoryAlias, branch: branch, repositoryUsers: data.repositoryUsers, repositoryBranches: data.repositoryBranches });
        },
        error: function (data) {
            displayError('File Level Awareness', data.responseJSON);
        }
    });
}



function renderFileView(data) {

    // Set state
    pushHistoryState("repositories/" + activeRepository + "/" + activeBranch);

    // Render template
    $('#content').html(new EJS({url: webInterfacePrefix + 'templates/file_view.ejs'}).render(data));

    // Navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);
    $('.loadBranchView').click(function () { loadBranchView(activeRepository); });

/*
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
*/

    loadFileViewTable(activeRepository, activeBranch);
}

function loadFileViewTable(repositoryAlias, branch) {
    sendApiRequest({
        name: 'fileAwareness',
        type: 'GET',
        data: { repositoryAlias: repositoryAlias, branch: branch, showUncommitted: showUncommitted, showConflicts: showConflicts, compareToBranch: branch },
        success: function(data) {
            console.log("Get file awareness. Success: " + JSON.stringify(data));

            var pathConflicts = {};



            for (var i = 0; i < data.files.length; i++) { 

                var filenameSplit = data.files[i].filename.split(/\//);
                
                for (var j = 0; j < filenameSplit.length - 1; j++) {

                    var filenamePath = filenameSplit.slice(0, j + 1).join('/');

                    if (pathConflicts[filenamePath] == undefined) pathConflicts[filenamePath] = "NO_CONFLICT";

                    for (var k = 0; k < data.files[i].users.length; k++) {
                        var conflictType = data.files[i].users[k].type;
                        if (conflictType == "CONTENT_CONFLICT") pathConflicts[filenamePath] = "CONTENT_CONFLICT";
                        else if (conflictType == "FILE_CONFLICT" && pathConflicts[filenamePath] != "CONTENT_CONFLICT") pathConflicts[filenamePath] = "FILE_CONFLICT";
                    }

                }

            }

            // Render
            renderFileViewTable({ login: login, repositoryAlias: repositoryAlias, branch: branch, files: data.files, pathConflicts: pathConflicts });
        },
        error: function (data) {
            displayError('File Level Awareness', data.responseJSON);
        }
    });
}

function renderFileViewTable(data) {


    $('#fileListContainer').html(new EJS({url: webInterfacePrefix + 'templates/file_view_table.ejs'}).render(data));

    // Clicking on a file and user
    $('.fileAndUser').click(function () {
        loadContentView(activeRepository, activeBranch, $(this).data('filename'), $(this).data('username'), $(this).data('comparetobranch'));
    });

    $('.fileFolderName').click(function () {
        $(this).parent().find('> .fileFolderContent').slideToggle();
    });

    $('.toggleFiles').click(function () {
        if ($('.fileFolderContent').find(':visible').length > 0) {
            $('.fileFolderContent').slideUp();
        }
        else {

            $('.fileFolderContent').slideDown();
        }
    });
}



/* CONTENT LEVEL AWARENESS */

function loadContentView(repositoryAlias, branch, filename, username, compareToBranch) {
    activeFile = filename;
    activeUser = username;
    activeCompareToBranch = compareToBranch;
    renderContentView({ repositoryAlias: repositoryAlias, branch: branch, filename: filename, username: username, showUncommitted: showUncommitted, showConflicts: showConflicts });
}


function renderContentView(data) {
    
    // Set state
    pushHistoryState("repositories/" + activeRepository + "/" + activeBranch + "/" + data.filename);

    $('#content').html(new EJS({url: webInterfacePrefix + 'templates/content_view.ejs'}).render(data));

    // navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);
    $('.loadBranchView').click(function () { loadBranchView(activeRepository); });
    $('.loadFileView').click(function () { loadFileView(activeRepository, activeBranch); });
    $('.loadContentView').click(function () { loadFileView(activeRepository, activeBranch); });

    /*
    $('#uncommittedFilter').change(function () {
        showUncommitted = $('#uncommittedFilter').is(':checked');

        loadContentViewDiff(activeRepository, activeBranch, activeFile, activeUser, activeCompareToBranch);
    });
    $('#conflictsFilter').change(function () {
        showConflicts = $('#conflictsFilter').is(':checked');

        loadContentViewDiff(activeRepository, activeBranch, activeFile, activeUser, activeCompareToBranch);
    });
    */

    loadContentViewDiff(activeRepository, activeBranch, activeFile, activeUser, activeCompareToBranch);

}

function loadContentViewDiff(repositoryAlias, branch, filename, theirUsername, compareToBranch) {
    sendApiRequest({
        name: 'contentAwareness',
        type: 'GET',
        data: { repositoryAlias: repositoryAlias, branch: branch, filename: filename, theirUsername: theirUsername, showUncommitted: showUncommitted, showConflicts: showConflicts, compareToBranch: compareToBranch },
        success: function(data) {
            console.log("Get content awareness. Success: " + JSON.stringify(data));
            renderContentViewDiff({ content: data.content, filename: filename, repositoryAlias: repositoryAlias, branch: branch, theirUsername: theirUsername, showUncommitted: showUncommitted, showConflicts: showConflicts });
        },
        error: function (data) {
            alert('Something went wrong when trying to load line level awareness data.');
        }
    });
}


function renderContentViewDiff(data) {
    /*if (showConflicts) {
        $('#contentDiffContainer').html(new EJS({url: webInterfacePrefix + 'templates/content_view_diff3.ejs'}).render(data));
    }
    else {*/
        $('#contentDiffContainer').html(new EJS({url: webInterfacePrefix + 'templates/content_view_diff.ejs'}).render(data));
    //}
}







/* CREATE REPOSITORY VIEW */

function loadCreateRepositoryView() {
    renderCreateRepositoryView({ login: login });
}

function renderCreateRepositoryView(data) {

    // set state
    pushHistoryState("createRepository");

    $('#content').html(new EJS({url: webInterfacePrefix + 'templates/create_repository_view.ejs'}).render(data));

    // header
    $('#headerLogo').click(loadRepositoryView);
    $('#manageUsers').click(loadUsersView);

    // navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);
    
    $('#submitCreateRepository').click(function () {
        sendApiRequest({
            name: 'createRepository',
            type: 'POST',
            data: { repositoryAlias: $('#repositoryAlias').val(), repositoryUrl: $('#repositoryUrl').val() },
            success: function(data) {
                console.log("Create repository. Success: " + JSON.stringify(data));

                loadRepositoryView();
            },
            error: function (data) {
                alert('Something went wrong when trying to create a repository.');
            }
        });
    });
}


/* USER MANAGEMENT VIEW */

/*
function loadUsersView() {
    sendApiRequest({
        name: 'users',
        type: 'GET',
        data: { },
        success: function(data) {
            console.log("Load users. Success: " + JSON.stringify(data));
            renderUsersView({ users: data, login: login });
        },
        error: function (data) {
            alert('Something went wrong when trying to load the list of users.');
        }
    });
}

function renderUsersView(data) {

    // set state
    pushHistoryState("users");

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
        sendApiRequest({
            name: 'deleteUser',
            type: 'POST',
            data: { username: $(this).data('username'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Delete user. Success: " + JSON.stringify(data));
                loadUsersView();
            },
            error: function (data) {
                alert('Something went wrong when trying to delete a user.');
            }
        });
        e.stopPropagation();
    });
    $('.makeUserAdmin').click(function (e) {
        sendApiRequest({
            name: 'makeUserAdmin',
            type: 'POST',
            data: { username: $(this).data('username'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Make user admin. Success: " + JSON.stringify(data));
                loadUsersView();
            },
            error: function (data) {
                alert('Something went wrong when trying to make a user admin.');
            }
        });
        e.stopPropagation();
    });
    $('.revokeUserAdmin').click(function (e) {
        sendApiRequest({
            name: 'revokeUserAdmin',
            type: 'POST',
            data: { username: $(this).data('username'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Revoke user admin. Success: " + JSON.stringify(data));
                loadUsersView();
            },
            error: function (data) {
                alert('Something went wrong when trying to revoke a users admin privileges.');
            }
        });
        e.stopPropagation();
    });
    $('.makeUserCreator').click(function (e) {
        sendApiRequest({
            name: 'makeUserCreator',
            type: 'POST',
            data: { username: $(this).data('username'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Make user creator. Success: " + JSON.stringify(data));
                loadUsersView();
            },
            error: function (data) {
                alert('Something went wrong when trying to give a user repository creator privileges.');
            }
        });
        e.stopPropagation();
    });
    $('.revokeUserCreator').click(function (e) {
        sendApiRequest({
            name: 'revokeUserCreator',
            type: 'POST',
            data: { username: $(this).data('username'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Revoke user creator. Success: " + JSON.stringify(data));
                loadUsersView();
            },
            error: function (data) {
                alert('Something went wrong when trying to remove a users creator privileges.');
            }
        });
        e.stopPropagation();
    });
}




*/



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

