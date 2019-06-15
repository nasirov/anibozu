/**
 * Created by nasirov.yv
 */
$(document).ready(function () {
  $('#username-submit-form').on('submit', function (e) {
    var container = $('.nes-container.with-title.is-centered');
    var usernameInputField = $('#username');
    var submitButton = $('.nes-btn');
    var username = usernameInputField.val();
    if (!isElementHasReadonlyAttr(usernameInputField[0])) {
      if (checkMalUsername(username)) {
        addReadonlyAttr(usernameInputField);
        changeClasses(usernameInputField, true);
        changeClasses(submitButton, true);
        setInnerHtmlValue(submitButton[0], "Searching...");
        setDisabledAttributeValue(submitButton[0], true);
        addAndRunProgressBar(container);
      } else {
        disableEvents(e);
        changeClasses(usernameInputField, false);
        changeClasses(submitButton, false);
        removeProgressBar(container);
      }
    } else {
      disableEvents(e);
    }
  });

});
function addAndRunProgressBar(element) {
  var progressBar = '<progress class="nes-progress is-success" value="0" max="100"></progress>';
  if (element.find('.nes-progress.is-success').length === 0) {
    element.append(progressBar);
    setInterval(runProgressBar, 1000);
  }
}
function removeProgressBar(element) {
  var progressBar = '.nes-progress.is-success';
  if (!(element.find(progressBar).length === 0)) {
    element.find(progressBar).remove();
  }
}
function runProgressBar() {
  var progressBar = $('.nes-progress.is-success');
  var value = progressBar[0].value;
  if (value === 100) {
    value = 0;
  }
  value += 10;
  progressBar.attr('value', value);
}
function changeClasses(element, isInputValueValid) {
  var is_success = 'is-success';
  var is_error = 'is-error';
  var is_primary = 'is-primary';
  if (element.hasClass(is_primary)) {
    element.toggleClass(is_primary);
  }
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
  if (username === undefined || username === null) {
    return false;
  }
  return username.match(/^[\w_-]{2,16}$/) !== null;
}
function addReadonlyAttr(element) {
  element.attr('readonly', '');
}
function isElementHasReadonlyAttr(element) {
  return element.hasAttribute('readonly');
}
function disableEvents(element) {
  element.preventDefault();
  element.stopImmediatePropagation();
}
function setInnerHtmlValue(element, value) {
  element.innerHTML = value;
}
function setDisabledAttributeValue(element, value) {
  element.disabled = value;
}