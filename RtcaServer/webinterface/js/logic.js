var activeRepository = undefined;

var conflictType = "INTER_BRANCH_CONFLICTS"

var conflictType;

var login = undefined;

var apiPrefix = '/request'






$(function () {
    renderLogin();
});


/* request */

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


/* load login view */

function renderLogin() {
    $('body').html(new EJS({url: 'templates/login.ejs'}).render());
    $('#submitLogin').click(function () {
        sendRequest({
            name: 'login',
            data: { username: $('#username').val(), password: $('#password').val() },
            success: function(data) { // request success
                console.log("Got login response: " + JSON.stringify(data));
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
                console.log("Got login response: " + JSON.stringify(data));
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


/* load repository view */

function loadOverviewView() {
    sendRequest({
        name: 'getRepositories',
        data: { sessionId: login.sessionId },
        success: function(data) {
            console.log("Get repositories. Got response: " + JSON.stringify(data));
            renderOverviewView({ repositories: data, login: login });
        },
        error: function () {
            alert('Something went wrong when loading repository list.');
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
        loadRepositoryView($(this).data('alias'));
    });
}


/* create repository view */

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
                console.log("Got create repository response: " + JSON.stringify(data));

                loadOverviewView();
            },
            error: function () {
                alert('Something went wrong when logging in.');
            }
        });
    });
}


/* load users view */

function loadUsersView() {
    sendRequest({
        name: 'getUsers',
        data: { sessionId: login.sessionId },
        success: function(data) {
            console.log("Get repositories. Got response: " + JSON.stringify(data));
            renderUsersView({ users: data, login: login });
        },
        error: function () {
            alert('Something went wrong when loading repository list.');
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
}


/* load conflict view */

function loadRepositoryView(repositoryAlias) {
    sendRequest({
        name: 'getRepositoryInformation',
        data: { sessionId: login.sessionId, repositoryAlias: repositoryAlias },
        success: function(data) {
            console.log("Get repository information. Got response: " + JSON.stringify(data));
            activeRepository = repositoryAlias;
            renderRepositoryView({ repositoryInformation: data, repositoryAlias: repositoryAlias });
        },
        error: function () {
            console.log("Get file conflicts. Error.");
        }
    });

}

function renderRepositoryView(data) {
    $('body').html(new EJS({url: 'templates/repository.ejs'}).render(data));
    $('.fileConflict').click(function () {
        loadFileView($(this).data('filename'));
    });
    $('.repositoryViewButton').click(function () {
        loadOverviewView();
    });
    $('#submitFilter').click(loadConflictsView);
}

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


/* load file view */

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
}

/* add repository */

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
