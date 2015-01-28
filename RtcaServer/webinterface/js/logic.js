var activeRepository = undefined;

var conflictType = "INTER_BRANCH_CONFLICTS"

var conflictType;

var login = undefined;

var apiPrefix = '/pull'



$(function () {
    renderLogin();
});


/* load login view */

function renderLogin() {
    $('body').html(new EJS({url: 'templates/login.ejs'}).render());
    $('#submitLogin').click(function () {
        $.ajax({
            url: apiPrefix + '/login',
            type: 'POST',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify({ username: $('#username').val(), password: $('#password').val() }),
            success: function(data) {
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
    $('#submitNewUser').click(function () {
        $.ajax({
            url: apiPrefix + '/createUserAndLogin',
            type: 'POST',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify({ username: $('#newUsername').val(), password: $('#newPassword').val() }),
            success: function(data) {
                console.log("Got login response: " + JSON.stringify(data));
                login = data;

                if (login.sessionId != undefined) {
                    loadRepositoryList();
                }
                else {
                    alert('User create error. Maybe the user already exists or something.');
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
            $('#submitNewUser').click();
        }
    });
    $('#username').focus();
}



/* load repository view */

function loadRepositoryList() {
    $.ajax({
        url: apiPrefix + '/getRepositories',
        type: 'POST',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify({ sessionId: login.sessionId }),
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
    $('.repository').click(function () {
        loadFileConflicts($(this).data('alias'));
    });
}


/* load users view */

function loadUserList() {
    $.ajax({
        url: apiPrefix + '/getUsers',
        type: 'POST',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify({ sessionId: login.sessionId }),
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
    $('body').html(new EJS({url: 'templates/user_list.ejs'}).render(data));
    $('#logo').click(loadRepositoryList);
    $('#refresh').click(loadRepositoryList);
    $('#manageUsers').click(loadUserList);
    $('.repository').click(function () {
        loadFileConflicts($(this).data('alias'));
    });
}


/* load conflict view */

function loadFileConflicts(repositoryAlias) {
    $.getJSON(apiPrefix + '/getFileConflicts/' + repositoryAlias).done(function(data) {
        console.log("Get file conflicts. Got response: " + JSON.stringify(data));
        activeRepository = repositoryAlias;
		renderFileConflicts({ fileConflicts: data, repositoryAlias: repositoryAlias });
    }).fail(function () {
        console.log("Get file conflicts. Error.");
    });
}

function renderFileConflicts(data) {
    $('body').html(new EJS({url: 'templates/file_conflicts.ejs'}).render(data));
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
    $('.fileConflictsViewButton').click(function () {
        loadFileConflicts(activeRepository);
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
