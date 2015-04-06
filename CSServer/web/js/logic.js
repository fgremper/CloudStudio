/*var navigation.repositoryAlias = undefined;
var navigation.branch = undefined;
var activeFile = undefined;
var activeUser = undefined;
var navigation.repositoryAliasUsers = undefined;
var navigation.repositoryAliasBranches = undefined;
var activeCompareToBranch = undefined;

var selectedUsers = undefined;
var showUncommitted = false;
var showConflicts = false;
var selectedAdditionalBranches = undefined;
var conflictType = "INTER_BRANCH_CONFLICTS"
*/


/* VARIABLES */

// Constants
var API_PREFIX = '/api/'
var WEB_INTERFACE_PREFIX = "/";

// Global objects
var login = {};
var selection = {};
var navigation = {};




selection.selectedUsers = [];
selection.showConflicts = true;
selection.showUncommitted = false;
selection.severityFilter = 'ALL';




/* UTILITY FUNCTIONS */

// Send an API request
function sendApiRequest(requestObject) {
    requestObject.data.sessionId = login.sessionId;
    $.ajax({
        url: API_PREFIX + requestObject.name,
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

    }

    // Look at URL to find out what to render
    renderFromDocumentLocation();

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
    if (document.location.pathname == WEB_INTERFACE_PREFIX + path) return;

    // Push the history state
    history.pushState(null, "", WEB_INTERFACE_PREFIX + path);

}

// Look at URL to find out what to render
function renderFromDocumentLocation() {

    var url = document.location.pathname;
    var params = url.split(/\//).splice(1);

    if (params[0] == "") {
        if (login.sessionId == null) {
            renderLoginView();
        }
        else {
            loadRepositoryView();
        }

    }
    else if (params[0] == "signup" && (params[1] == undefined || params[1] == "")) {
        renderSignUpView();
    }
    else if (params[0] == "create" && (params[1] == undefined || params[1] == "")) {
        loadCreateRepositoryView();
    }
    else if (params[0] == "repositories" && (params[1] == undefined || params[1] == "")) {
        loadRepositoryView();
    }
    else if (params[0] == "repositories" && !(params[1] == undefined || params[1] == "") && (params[2] == undefined || params[2] == "")) {
        loadBranchView(params[1]);
    }
    else if (params[0] == "repositories" && !(params[1] == undefined || params[1] == "") && !(params[2] == undefined || params[2] == "")) {
        selection.compareToBranch = params[2];
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
    $('#headerBar').append('<div id="headerLogo" class="headerBarLeft">CloudStudio</div>');
    if (login.sessionId != null) {
        $('#headerBar').append('<div class="headerBarRight" id="logout">Logout</div>');
        $('#headerBar').append('<div class="headerBarRight" id="profile">Profile</div>');
        if (login.isAdmin) { $('#headerBar').append('<div id="headerManageUsers" class="headerBarRight">Manage Users</div>'); }
    }
    else {
        $('#headerBar').append('<div id="headerSignUp" class="headerBarRight">Sign Up</div>');
        $('#headerBar').append('<div id="headerLogIn" class="headerBarRight">Log In</div>');
    }

    // Clicking the logout button
    $('#logout').click(function () {
        // Delete the login cookies
        setCookie("sessionId", null, -1);
        setCookie("username", null, -1);
        setCookie("isCreator", null, -1);
        setCookie("isAdmin", null, -1);

        // Delete the login variable
        login = {};

        // Render the header bar
        renderHeaderBar();

        // Render the login view
        renderLoginView();
    });

    $('#headerLogo').click(function () {

        if (login.sessionId == null) {
            renderLoginView();
        }
        else {
            loadRepositoryView();
        }
    });

    $('#headerSignUp').click(function () {
        renderSignUpView();
    });

    $('#headerLogIn').click(function () {
        renderLoginView();
    });


    $('#headerManageUsers').click(function () {
        loadUsersView();
    });

    $('#profile').click(function () {
        loadProfileView();
    });
}



/* LOGIN */

function renderLoginView() {
    
    // Push history state
    pushHistoryState("");

    // Render template
    $('#content').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/login_view.ejs'}).render());

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
                        setCookie("sessionId", login.sessionId, 30);
                        setCookie("username", login.username, 30);
                        setCookie("isCreator", login.isCreator, 30);
                        setCookie("isAdmin", login.isAdmin, 30);
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



/* SIGN UP */

function renderSignUpView() {
    
    // Push history state
    pushHistoryState("signup");

    // Render template
    $('#content').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/signup_view.ejs'}).render());

    // Register login click event
    $('#submitSignup').click(function () {
        sendApiRequest({
            name: 'createUser',
            type: 'POST',
            data: { username: $('#username').val(), password: $('#password').val() },
            success: function(data) {
                console.log("Sign Up. Success: " + JSON.stringify(data));

                alert("Sign up success!");

                renderLoginView();

            },
            error: function (data) {
                displayError('Sign Up', data.responseJSON);
            }
        });
    });

    // Submit form when we press enter while we're in one of the input fields
    $('#username, #password').keypress(function(e) {
        if (e.which == 13 && !$('#errorOverlay').is(':visible')) {
            $('#submitSignup').click();
        }
    });

    // Focus on the username input field
    $('#username').focus();

}



/* SIGN UP */

function renderCreateRepository() {
    
    // Push history state
    pushHistoryState("signup");

    // Render template
    $('#content').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/signup_view.ejs'}).render());

    // Register login click event
    $('#submitSignup').click(function () {
        sendApiRequest({
            name: 'createUser',
            type: 'POST',
            data: { username: $('#username').val(), password: $('#password').val() },
            success: function(data) {
                console.log("Sign Up. Success: " + JSON.stringify(data));

                alert("Sign up success!");

                renderLoginView();

            },
            error: function (data) {
                displayError('Sign Up', data.responseJSON);
            }
        });
    });

    // Submit form when we press enter while we're in one of the input fields
    $('#username, #password').keypress(function(e) {
        if (e.which == 13 && !$('#errorOverlay').is(':visible')) {
            $('#submitSignup').click();
        }
    });

    // Focus on the username input field
    $('#username').focus();

}




function loadProfileView() {
    renderProfileView({ login: login });
}

function renderProfileView(data) {
    
    // Push history state
    pushHistoryState("profile");

    // Render template
    $('#content').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/profile_view.ejs'}).render(data));


}




/* REPOSITORY OVERIEW */

function loadRepositoryView() {

    // Send API request
    sendApiRequest({
        name: 'repositories',
        type: 'GET',
        data: { sessionId: login.sessionId },
        success: function(data) {
            console.log("Load overview. Success: " + JSON.stringify(data));

            //Render
            renderRepositoryView({ login: login, repositories: data.repositories });
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
    $('#content').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/repository_view.ejs'}).render(data));

    // Clicking on a repository
    $('.repositoryListItem').click(function () {
        selection.selectedUsers = [];
        loadBranchView($(this).data('alias'));
    });

    $('#createRepository').click(loadCreateRepositoryView);

    $('.editRepository').click(function (e) {
        loadEditRepositoryView($(this).parent().parent().data('alias'));
        e.stopPropagation();
    });

    /*
    // content
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





function loadEditRepositoryView(repositoryAlias) {

    // Send API request
    sendApiRequest({
        name: 'repositoryInformation',
        type: 'GET',
        data: { sessionId: login.sessionId, repositoryAlias: repositoryAlias },
        success: function(data) {
            console.log("Load edit repository. Success: " + JSON.stringify(data));

            //Render
            renderEditRepositoryView({ login: login, repositoryAlias: data.repositoryAlias, repositoryDescription: data.repositoryDescription, repositoryUrl: data.repositoryUrl, repositoryUsers: data.repositoryUsers, repositoryOwner: data.repositoryOwner });
        },
        error: function (data) {
            displayError('Edit Repository', data.responseJSON);
        }
    });

}

function renderEditRepositoryView(data) {

    // set state
    pushHistoryState("edit");

    $('#content').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/edit_repository_view.ejs'}).render(data));

    // navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);
    
    $('#submitEditRepository').click(function () {
        sendApiRequest({
            name: 'setRepositoryInformation',
            type: 'POST',
            data: { repositoryAlias: $('#repositoryAlias').val(), repositoryDescription: $('#repositoryDescription').val(), repositoryUrl: $('#repositoryUrl').val() },
            success: function(data) {
                console.log("Update repository. Success: " + JSON.stringify(data));

                loadEditRepositoryView($('#repositoryAlias').val());
            },
            error: function (data) {
                alert('Something went wrong when trying to edit a repository.');
            }
        });
    });

    $('#addUserToRepository').click(function (e) {
        sendApiRequest({
            name: 'addUserToRepository',
            type: 'POST',
            data: { repositoryAlias: $('#repositoryAlias').val(), username: $('#newUser').val(), sessionId: login.sessionId },
            success: function(data) {
                console.log("Add repository. Success: " + JSON.stringify(data));
                loadEditRepositoryView($('#repositoryAlias').val());
            },
            error: function (data) {
                alert('Something went wrong when trying to add a user to the repository.');
            }
        });
        e.stopPropagation();
    });

    $('.deleteUserFromRepository').click(function (e) {
        sendApiRequest({
            name: 'removeUserFromRepository',
            type: 'POST',
            data: { repositoryAlias: $('#repositoryAlias').val(), username: $(this).data('username'), sessionId: login.sessionId },
            success: function(data) {
                console.log("Delete user from repository. Success: " + JSON.stringify(data));
                loadEditRepositoryView($('#repositoryAlias').val());
            },
            error: function (data) {
                alert('Something went wrong when trying to delete a user from the repository.');
            }
        });
        e.stopPropagation();
    });

    $('#deleteRepository').click(function (e) {
        sendApiRequest({
            name: 'deleteRepository',
            type: 'POST',
            data: { repositoryAlias: $('#repositoryAlias').val(), sessionId: login.sessionId },
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
    $('#modifyRepositoryOwner').click(function (e) {
        sendApiRequest({
            name: 'modifyRepositoryOwner',
            type: 'POST',
            data: { repositoryAlias: $('#repositoryAlias').val(), username: $('#newOwner').val(), sessionId: login.sessionId },
            success: function(data) {
                console.log("Modify repository owner. Success: " + JSON.stringify(data));
                loadEditRepositoryView($('#repositoryAlias').val());
            },
            error: function (data) {
                alert('Something went wrong when trying to modify the repository owner.');
            }
        });
        e.stopPropagation();
    });
}




/* BRANCH LEVEL AWARENESS */

function loadBranchView(repositoryAlias) {

    // Send API request
    sendApiRequest({
        name: 'repositoryInformation',
        type: 'GET',
        data: { repositoryAlias: repositoryAlias },
        success: function(data) {
            console.log("Get repository info. Success: " + JSON.stringify(data));

            // Set our active variables
            navigation.repositoryAlias = repositoryAlias;

            // Render branch view
            renderBranchView({ login: login, repositoryAlias: repositoryAlias, repositoryUsers: data.repositoryUsers, selectedUsers: selection.selectedUsers, lastOriginUpdateDiff: data.lastOriginUpdateDiff });
        },
        error: function (data) {
            displayError('Branch Level Awareness', data.responseJSON);
        }
    });

}

function renderBranchView(data) {

    // Set state
    pushHistoryState("repositories/" + navigation.repositoryAlias);

    $('#content').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/branch_view.ejs'}).render(data));

    // Navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);

    // filter
    $('#usersFilter').change(function () {
        selection.selectedUsers = $.map($('#usersFilter option:selected'), function (o) { return o.value })

        // remove "":
        var index = selection.selectedUsers.indexOf("");
        if (index > -1) {
            selection.selectedUsers.splice(index, 1);
        }

        loadBranchViewTable(navigation.repositoryAlias);
    });

    $('select').chosen();

    loadBranchViewTable(navigation.repositoryAlias);

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
            renderBranchViewTable({ branches: data.branches, repositoryAlias: repositoryAlias, selectedUsers: selection.selectedUsers });
        },
        error: function (data) {
            displayError('Branch Level Awareness', data.responseJSON);
        }
    });

}

function renderBranchViewTable(data) {

    // Render template
    $('#branchListContainer').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/branch_view_table.ejs'}).render(data));

    // Click on a branch event
    $('.branchListItem').click(function () {
        selection.compareToBranch = $(this).data('branch');
        loadFileView(navigation.repositoryAlias, $(this).data('branch'));
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
            navigation.repositoryAlias = repositoryAlias;
            navigation.branch = branch;

            // Render
            renderFileView({ login: login, repositoryAlias: repositoryAlias, branch: branch, repositoryUsers: data.repositoryUsers, selectedUsers: selection.selectedUsers, repositoryBranches: data.repositoryBranches, compareToBranch: selection.compareToBranch, showUncommitted: selection.showUncommitted, showConflicts: selection.showConflicts, severityFilter: selection.severityFilter });
        },
        error: function (data) {
            displayError('File Level Awareness', data.responseJSON);
        }
    });
}



function renderFileView(data) {

    // Set state
    pushHistoryState("repositories/" + navigation.repositoryAlias + "/" + navigation.branch);

    // Render template
    $('#content').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/file_view.ejs'}).render(data));

    // Navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);
    $('.loadBranchView').click(function () { loadBranchView(navigation.repositoryAlias); });

    // filter
    $('#usersFilter').change(function () {
        selection.selectedUsers = $.map($('#usersFilter option:selected'), function (o) { return o.value })

        // remove "":
        var index = selection.selectedUsers.indexOf("");
        if (index > -1) {
            selection.selectedUsers.splice(index, 1);
        }

         loadFileViewTable(navigation.repositoryAlias, navigation.branch);
    });
    $('#compareToBranchFilter').change(function () {
        selection.compareToBranch = $('#compareToBranchFilter').val();

        loadFileViewTable(navigation.repositoryAlias, navigation.branch);
    });
    
    $('#uncommittedFilter').change(function () {
        selection.showUncommitted = $('#uncommittedFilter').is(':checked');

        loadFileViewTable(navigation.repositoryAlias, navigation.branch);
    });
    $('#conflictsFilter').change(function () {
        selection.showConflicts = $('#conflictsFilter').is(':checked');

        loadFileViewTable(navigation.repositoryAlias, navigation.branch);
    });
    $('#severityFilter').change(function () {
        selection.severityFilter = $('#severityFilter').val();

        loadFileViewTable(navigation.repositoryAlias, navigation.branch);
    });
    
    
    $('select').chosen();


    loadFileViewTable(navigation.repositoryAlias, navigation.branch);
}

function loadFileViewTable(repositoryAlias, branch) {

    sendApiRequest({
        name: 'fileAwareness',
        type: 'GET',
        data: { repositoryAlias: repositoryAlias, branch: branch, showUncommitted: selection.showUncommitted, showConflicts: selection.showConflicts, compareToBranch: selection.compareToBranch },
        success: function(data) {
            console.log("Get file awareness. Success: " + JSON.stringify(data));


            if (selection.selectedUsers.length > 0) {
                // filter users

                for (var i = 0; i < data.files.length; i++) { 
                    for (var k = data.files[i].users.length - 1; k >= 0; k--) {
                        if (selection.selectedUsers.indexOf(data.files[i].users[k].username) < 0) data.files[i].users.splice(k, 1);
                    }
                }
            }


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
            renderFileViewTable({ login: login, repositoryAlias: repositoryAlias, branch: branch, files: data.files, pathConflicts: pathConflicts, severityFilter: selection.severityFilter });
        },
        error: function (data) {
            displayError('File Level Awareness', data.responseJSON);
        }
    });
}

function renderFileViewTable(data) {


    $('#fileListContainer').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/file_view_table.ejs'}).render(data));

    // Clicking on a file and user
    $('.fileAndUser').click(function () {
        loadContentView(navigation.repositoryAlias, navigation.branch, $(this).data('filename'), $(this).data('username'), $(this).data('comparetobranch'));
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
    navigation.filename = filename;
    navigation.username = username;
    selection.compareToBranch = compareToBranch;
    renderContentView({ repositoryAlias: repositoryAlias, branch: branch, filename: filename, username: username, showUncommitted: selection.showUncommitted, showConflicts: selection.showConflicts });
}


function renderContentView(data) {
    
    // Set state
    pushHistoryState("repositories/" + navigation.repositoryAlias + "/" + navigation.branch + "/" + data.filename);

    $('#content').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/content_view.ejs'}).render(data));

    // navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);
    $('.loadBranchView').click(function () { loadBranchView(navigation.repositoryAlias); });
    $('.loadFileView').click(function () { loadFileView(navigation.repositoryAlias, navigation.branch); });
    $('.loadContentView').click(function () { loadFileView(navigation.repositoryAlias, navigation.branch); });

    
    $('#uncommittedFilter').change(function () {
        selection.showUncommitted = $('#uncommittedFilter').is(':checked');

        loadContentViewDiff(navigation.repositoryAlias, navigation.branch, navigation.filename, navigation.username, selection.compareToBranch);
    });
    $('#conflictsFilter').change(function () {
        selection.showConflicts = $('#conflictsFilter').is(':checked');

        loadContentViewDiff(navigation.repositoryAlias, navigation.branch, navigation.filename, navigation.username, selection.compareToBranch);
    });
    

    loadContentViewDiff(navigation.repositoryAlias, navigation.branch, navigation.filename, navigation.username, selection.compareToBranch);

}

function loadContentViewDiff(repositoryAlias, branch, filename, theirUsername, compareToBranch) {
    if (selection.showConflicts) {
        sendApiRequest({
            name: 'contentConflicts',
            type: 'GET',
            data: { repositoryAlias: repositoryAlias, branch: branch, filename: filename, theirUsername: theirUsername, showUncommitted: selection.showUncommitted, compareToBranch: selection.compareToBranch },
            success: function(data) {
                console.log("Get content awareness. Success: " + JSON.stringify(data));
                renderContentViewDiff3({ content: data.content, filename: filename, repositoryAlias: repositoryAlias, branch: branch, theirUsername: theirUsername });
            },
            error: function (data) {
                alert('Something went wrong when trying to load line level awareness data.');
            }
        });
    }
    else {
        sendApiRequest({
            name: 'contentAwareness',
            type: 'GET',
            data: { repositoryAlias: repositoryAlias, branch: branch, filename: filename, theirUsername: theirUsername, showUncommitted: selection.showUncommitted, compareToBranch: selection.compareToBranch },
            success: function(data) {
                console.log("Get content awareness. Success: " + JSON.stringify(data));
                renderContentViewDiff({ content: data.content, filename: filename, repositoryAlias: repositoryAlias, branch: branch, theirUsername: theirUsername });
            },
            error: function (data) {
                alert('Something went wrong when trying to load line level awareness data.');
            }
        });
    }
}


function renderContentViewDiff(data) {
        $('#contentDiffContainer').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/content_view_diff.ejs'}).render(data));
}

function renderContentViewDiff3(data) {
        $('#contentDiffContainer').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/content_view_diff3.ejs'}).render(data));
}







/* CREATE REPOSITORY VIEW */

function loadCreateRepositoryView() {
    renderCreateRepositoryView({ login: login });
}

function renderCreateRepositoryView(data) {

    // set state
    pushHistoryState("create");

    $('#content').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/create_repository_view.ejs'}).render(data));

    // navigation bar
    $('.loadRepositoryView').click(loadRepositoryView);
    
    $('#submitCreateRepository').click(function () {
        sendApiRequest({
            name: 'createRepository',
            type: 'POST',
            data: { repositoryAlias: $('#repositoryAlias').val(), repositoryUrl: $('#repositoryUrl').val(), repositoryDescription: $('#repositoryDescription').val() },
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


function loadUsersView() {
    sendApiRequest({
        name: 'users',
        type: 'GET',
        data: { },
        success: function(data) {
            console.log("Load users. Success: " + JSON.stringify(data));
            renderUsersView({ users: data.users, login: login });
        },
        error: function (data) {
            alert('Something went wrong when trying to load the list of users.');
        }
    });
}

function renderUsersView(data) {

    // set state
    pushHistoryState("users");

    $('#content').html(new EJS({url: WEB_INTERFACE_PREFIX + 'templates/users_view.ejs'}).render(data));

    // navigation bar
    $('.loadRepositoryView').click(function () {
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
            name: 'giveAdminPrivileges',
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
            name: 'revokeAdminPrivileges',
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
            name: 'giveCreatorPrivileges',
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
            name: 'revokeCreatorPrivileges',
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






function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+d.toUTCString();
    document.cookie = cname + "=" + cvalue + "; " + expires + "; path=/";
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
