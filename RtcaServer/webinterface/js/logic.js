var activeRepository = undefined;

var conflictType = "INTER_BRANCH_CONFLICTS"



$(function () {
    loadRepositoryList();
});

/* load repository view */

function loadRepositoryList() {
    $('#titleText').html('Repositories')
    $.getJSON("/pull/getRepositories").done(function(data) {
        console.log("Get repositories. Got response: " + JSON.stringify(data));
		renderRepositoryList({ repositories: data });
    }).fail(function () {
        console.log("Get repositories. Error.");
    });
}

function renderRepositoryList(data) {
    $('body').html(new EJS({url: 'templates/repository_list.ejs'}).render(data));
    $('.repository').click(function () {
        loadFileConflicts($(this).data('alias'));
    });
}

/* load conflict view */

function loadFileConflicts(repositoryAlias) {
    $.getJSON("/pull/getFileConflicts/" + repositoryAlias).done(function(data) {
        console.log("Get file conflicts. Got response: " + JSON.stringify(data));
        activeRepository = repositoryAlias;
		renderFileConflicts({ fileConflicts: data, repositoryAlias: repositoryAlias });
    }).fail(function () {
        console.log("Get file conflicts. Error.");
    });
}

function renderFileConflicts(data) {
	console.log(data);
    $('body').html(new EJS({url: 'templates/file_conflicts.ejs'}).render(data));
    $('.repositoryViewButton').click(function () {
        loadRepositoryList();
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
