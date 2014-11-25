$(document).ready(function () {
    loadRepositories();
});

function loadRepositories() {
    console.log("Thanks for reading the console!");
    $.get("/pull").done(function(data) {
        console.log("Got response: " + data);
    }).fail(function () {
        console.log("Well, this didn't work! :O");
    });
}
