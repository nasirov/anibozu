/**
 * Created by nasirov.yv
 */
$(document).ready(function () {
  $('#username-submit-form').on('submit', function (e) {
    var usernameInputField = $('#username');
    var submitButton = $('.nes-btn');
    var username = usernameInputField.val();
    var fanDubCheckboxes = $('.nes-checkbox');
    if (!isElementHasReadonlyAttr(usernameInputField[0])) {
      var isValidUsername = checkMalUsername(username);
      var isAtLeastOnFanDubChecked = checkFanDubCheckboxes(fanDubCheckboxes);
      if (isValidUsername && isAtLeastOnFanDubChecked) {
        addReadonlyAttr(usernameInputField);
        changeClasses(usernameInputField, true);
        changeClasses(submitButton, true);
        setInnerHtmlValue(submitButton[0], "Searching for Watching Titles...");
        setDisabledAttributeValue(submitButton[0], true);
      } else {
        disableEvents(e);
        changeClasses(usernameInputField, isValidUsername);
        changeClasses(fanDubCheckboxes, isAtLeastOnFanDubChecked);
        changeClasses(submitButton, false);
      }
    } else {
      disableEvents(e);
    }
  });

  $('#username.nes-input').on({
    keypress: function (e) {
      replaceIllegalChars(e);
    },
    keydown: function (e) {
      replaceIllegalChars(e);
    },
    keyup: function (e) {
      replaceIllegalChars(e);
    },
    onpaste: function (e) {
      replaceIllegalChars(e);
    }
  });
});

function replaceIllegalChars(e) {
  setTimeout(function () {
    var element = $(e.target);
    element.val(element.val().replace(/[^\w-_]/g, ''));
    var usernameLength = element.val().length;
    var maxUsernameLength = 16;
    if (usernameLength >= maxUsernameLength) {
      element.val(element.val().substring(0, 16));
    }
  }, 0);
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

function checkFanDubCheckboxes(fanDubCheckboxes) {
  for (var i = 0; i < fanDubCheckboxes.length; i++) {
    var checkbox = fanDubCheckboxes[i];
    if (checkbox.checked) {
      return true;
    }
  }
  return false;
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