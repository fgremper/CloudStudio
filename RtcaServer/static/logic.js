$(document).ready(function () {
    loadRepositories();
});

function loadRepositories() {

    $.get("http://127.0.0.1:7330/").done(function(data) {
        console.log("Got response: " + data);
    }).fail(function () {
        console.log("Well, this didn't work! :O");
    });
   
}
