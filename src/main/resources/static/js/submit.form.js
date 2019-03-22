/**
 * Created by nasirov.yv
 */
$(document).ready(function () {
    $('#username-submit-form').on('submit', function (e) {
        var inputField = $('#name_field');
        var userInput = inputField.val();
        if (checkMalUsername(userInput)) {
            changeClasses(inputField, true);
        } else {
            e.preventDefault();
            e.stopImmediatePropagation();
            changeClasses(inputField, false);
        }
    });

});
function changeClasses(inputField, isInputValueValid) {
    var is_success = 'is-success';
    var is_error = 'is-error';
    if (isInputValueValid) {
        if (!inputField.hasClass(is_success)) {
            inputField.toggleClass(is_success);
        }
        if (inputField.hasClass(is_error)) {
            inputField.toggleClass(is_error);
        }
    } else {
        if (inputField.hasClass(is_success)) {
            inputField.toggleClass(is_success);
        }
        if (!inputField.hasClass(is_error)) {
            inputField.toggleClass(is_error);
        }
    }
}
function checkMalUsername(username) {
    var minLength = 2;
    var maxLength = 16;
    if (username === undefined || username === null) {
        return false;
    }
    return username.length >= minLength && username.length <= maxLength;
}