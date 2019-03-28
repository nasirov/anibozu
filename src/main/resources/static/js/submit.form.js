/**
 * Created by nasirov.yv
 */
$(document).ready(function () {
  $('#username-submit-form').on('submit', function (e) {
    var container = $('.nes-container.with-title.is-centered');
    var inputField = $('#name_field');
    var submitButton = $('.nes-btn');
    var userInput = inputField.val();
    if (checkMalUsername(userInput)) {
      changeClasses(inputField, true);
      changeClasses(submitButton, true);
      addAndRunProgressBar(container);
    } else {
      e.preventDefault();
      e.stopImmediatePropagation();
      changeClasses(inputField, false);
      changeClasses(submitButton, false);
      removeProgressBar(container);
    }
  });

});
function addAndRunProgressBar(element) {
  var progressBar = '<progress class="nes-progress is-success" value="0" max="100"></progress>';
  if (element.find(".nes-progress.is-success").length === 0) {
    element.append(progressBar);
    setInterval(runProgressBar, 1000);
  }
}
function removeProgressBar(element) {
  var progressBar = ".nes-progress.is-success";
  if (!(element.find(progressBar).length === 0)) {
    element.find(progressBar).remove();
  }
}
function runProgressBar() {
  var progressBar = $(".nes-progress.is-success");
  var value = progressBar[0].value;
  if (value === 100) {
    value = 0;
  }
  value += 10;
  progressBar.attr("value", value);
}
function changeClasses(element, isInputValueValid) {
  var is_success = 'is-success';
  var is_error = 'is-error';
  if (isInputValueValid) {
    if (!element.hasClass(is_success)) {
      element.toggleClass(is_success);
    }
    if (element.hasClass(is_error)) {
      element.toggleClass(is_error);
    }
  } else {
    if (element.hasClass(is_success)) {
      element.toggleClass(is_success);
    }
    if (!element.hasClass(is_error)) {
      element.toggleClass(is_error);
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