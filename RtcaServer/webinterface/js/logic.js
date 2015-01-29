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
                    loadRepositoryList();
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
                    loadRepositoryList();
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

function loadRepositoryList() {
    sendRequest({
        name: 'getRepositories',
        data: { sessionId: login.sessionId },
        success: function(data) {
            console.log("Get repositories. Got response: " + JSON.stringify(data));
            renderRepositoryList({ repositories: data, login: login });
        },
        error: function () {
            alert('Something went wrong when loading repository list.');
        }
    });
}

function renderRepositoryList(data) {
    $('body').html(new EJS({url: 'templates/repository_list.ejs'}).render(data));
    $('#logo').click(loadRepositoryList);
    $('#refresh').click(loadRepositoryList);
    $('#manageUsers').click(loadUserList);
    $('#createRepository').click(loadCreateRepository);
    $('.repository').click(function () {
        loadConflicts($(this).data('alias'));
    });
}


/* create repository view */

function loadCreateRepository() {
    renderCreateRepository({ login: login });
}

function renderCreateRepository(data) {
    $('body').html(new EJS({url: 'templates/create_repository.ejs'}).render(data));
    $('#logo').click(loadRepositoryList);
    $('#manageUsers').click(loadUserList);
    $('.repositoryViewButton').click(loadRepositoryList);
    $('#submitCreateRepository').click(function () {
        sendRequest({
            name: 'addRepository',
            data: { repositoryAlias: $('#repositoryAlias').val(), repositoryUrl: $('#repositoryUrl').val(), sessionId: login.sessionId },
            success: function(data) {
                console.log("Got create repository response: " + JSON.stringify(data));

                loadRepositoryList();
            },
            error: function () {
                alert('Something went wrong when logging in.');
            }
        });
    });
}


/* load users view */

function loadUserList() {
    sendRequest({
        name: 'getUsers',
        data: { sessionId: login.sessionId },
        success: function(data) {
            console.log("Get repositories. Got response: " + JSON.stringify(data));
            renderUserList({ users: data, login: login });
        },
        error: function () {
            alert('Something went wrong when loading repository list.');
        }
    });
}

function renderUserList(data) {
    $('body').html(new EJS({url: 'templates/user_view.ejs'}).render(data));
    $('#logo').click(loadRepositoryList);
    $('#refresh').click(loadUserList);
    $('#manageUsers').click(loadUserList);
    $('.repository').click(function () {
        loadConflicts($(this).data('alias'));
    });
}


/* load conflict view */

function loadConflicts(repositoryAlias) {
    sendRequest({
        name: 'getConflicts',
        data: { sessionId: login.sessionId, repositoryAlias: 'test' },
        success: function(data) {
            console.log("Get file conflicts. Got response: " + JSON.stringify(data));
            activeRepository = repositoryAlias;
    		renderConflicts({ Conflicts: data, repositoryAlias: repositoryAlias });
        },
        error: function () {
            console.log("Get file conflicts. Error.");
        }
    });
}

function renderConflicts(data) {
    $('body').html(new EJS({url: 'templates/conflicts_view.ejs'}).render(data));
    $('.fileConflict').click(function () {
        loadFileView($(this).data('filename'));
    });
    $('.repositoryViewButton').click(function () {
        loadRepositoryList();
    });
}

/* load file view */

function loadFileView(filename) {
    renderFileView({ filename: filename, repositoryAlias: activeRepository });
}

function renderFileView(data) {
    $('body').html(new EJS({url: 'templates/file_view.ejs'}).render(data));
    $('.repositoryViewButton').click(function () {
        loadRepositoryList();
    });
    $('.ConflictsViewButton').click(function () {
        loadConflicts(activeRepository);
    });
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
