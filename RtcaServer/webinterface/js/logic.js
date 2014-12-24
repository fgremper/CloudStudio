$(function () {
    loadRepositoryList();
});

function loadRepositoryList() {
    $.getJSON("/pull/getRepositories").done(function(data) {
        console.log("Got response: " + data);
		renderRepositoryList({ repositories: data });
    }).fail(function () {
        console.log("Well, this didn't work! :O");
    });
}

function renderRepositoryList(data) {
	console.log(data);
    $('body').html(new EJS({url: 'templates/repository_list.ejs'}).render(data));
    $('.repository').click(function () {
        loadFileConflicts($(this).attr('data-alias'));
    });
}

function loadFileConflicts(repositoryAlias) {
    $.getJSON("/pull/getFileConflicts/" + repositoryAlias).done(function(data) {
        console.log(data);
		renderFileConflicts({ fileConflicts: data, repositoryAlias: 'testrepo' });
    }).fail(function () {
        console.log("Well, this didn't work! :O");
    });
}



function renderFileConflicts(data) {
	console.log(data);
    $('body').html(new EJS({url: 'templates/file_conflicts.ejs'}).render(data));
    /*$('.file').click(function () {
        loadFileConflicts($(this).attr('data-alias'));
    });*/
}


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
