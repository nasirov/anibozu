/*
   Author: Nasirov Yuriy
*/

$(function () {
	$('#username-form').on('submit', function (e) {
		let usernameInput = $('#username-form-input');
		if (!usernameInput[0].hasAttribute('readonly')) {
			let validUsername = isValidUsername(usernameInput.val());
			if (validUsername) {
				usernameInput.attr('readonly', '');
				let submitButton = $('#username-form >button')[0];
				setScheduledLoadingMessages(submitButton);
				submitButton.disabled = true;
			} else {
				disableEvents(e);
				toggleErrorClass(usernameInput, validUsername);
			}
		} else {
			disableEvents(e);
		}
	});

	$('#username-form-input').on({
		keypress: function (e) {
			replaceIllegalChars(e);
		}, keydown: function (e) {
			replaceIllegalChars(e);
		}, keyup: function (e) {
			replaceIllegalChars(e);
		}, onpaste: function (e) {
			replaceIllegalChars(e);
		},
	});
});

function replaceIllegalChars(e) {
	setTimeout(function () {
		let element = $(e.target);
		element.val(element.val().replace(/[^\w-_]/g, ''));
		let usernameLength = element.val().length;
		let maxUsernameLength = 16;
		if (usernameLength >= maxUsernameLength) {
			element.val(element.val().substring(0, maxUsernameLength));
		}
		if (usernameLength > 0) {
			let validUsername = isValidUsername(element.val());
			toggleErrorClass(element, validUsername);
		}
	}, 0);
}

function toggleErrorClass(element, validUsername) {
	element.toggleClass('username-form__input--error', !validUsername);
}

function isValidUsername(username) {
	if (username === undefined || username === null) {
		return false;
	}
	return username.match(/^[\w_-]{2,16}$/) !== null;
}

function disableEvents(element) {
	element.preventDefault();
	element.stopImmediatePropagation();
}

function setInnerHtmlValue(element, value) {
	element.innerHTML = value;
}

function setScheduledLoadingMessages(element) {
	let progressBarSymbols = ['-', '\\', '|', '/'];
	let counter = 0;
	setInnerHtmlValue(element, '*');
	setInterval(function () {
		setInnerHtmlValue(element, progressBarSymbols[counter]);
		counter = (counter + 1) % progressBarSymbols.length;
	}, 100);
}