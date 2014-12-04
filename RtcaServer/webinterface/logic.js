$(document).ready(function () {
    console.log("Thanks for reading the console!");
    //addRepository("nyan", "cat");
    loadRepositories();

});

function loadRepositories() {
    $.get("/pull/getRepositories").done(function(data) {
        console.log("Got response: " + data);
    }).fail(function () {
        console.log("Well, this didn't work! :O");
    });
}

function addRepository(repositoryAlias, repositoryUrl) {
    $.ajax(
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
    );
}
