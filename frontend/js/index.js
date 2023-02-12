/*
   Author: Nasirov Yuriy
*/

document.addEventListener('DOMContentLoaded', function () {
	const usernameInput = document.querySelector('#username-form-input');
	document.querySelector('#username-form-button').addEventListener('click',
			function (e) {
				const readonlyAttribute = 'readonly';
				if (!usernameInput.hasAttribute(readonlyAttribute)) {
					const username = usernameInput.value;
					const validUsername = isValidUsername(username);
					if (validUsername) {
						usernameInput.setAttribute(readonlyAttribute, '');
						let submitButton = e.target;
						setScheduledLoadingMessages(submitButton);
						getAndRenderTitles(username);
					} else {
						disableEvents(e);
						toggleErrorClass(usernameInput, validUsername);
					}
				} else {
					disableEvents(e);
				}
			});

	usernameInput.addEventListener('keypress', replaceIllegalChars);
	usernameInput.addEventListener('keydown', replaceIllegalChars);
	usernameInput.addEventListener('keyup', replaceIllegalChars);
	usernameInput.addEventListener('onpaste', replaceIllegalChars);
});

function replaceIllegalChars(e) {
	setTimeout(function () {
		const usernameInput = e.target;
		const username = usernameInput.value;
		usernameInput.value = username.replace(/[^\w-_]/g, '');
		const usernameLength = username.length;
		const maxUsernameLength = 16;
		if (usernameLength >= maxUsernameLength) {
			usernameInput.value = username.substring(0, maxUsernameLength);
		}
		if (usernameLength > 0) {
			toggleErrorClass(usernameInput, isValidUsername(username));
		}
	}, 0);
}

function toggleErrorClass(element, validUsername) {
	element.classList.toggle('username-form__input--error', !validUsername);
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

function setText(element, value) {
	element.textContent = value;
}

function setScheduledLoadingMessages(element) {
	const progressBarSymbols = ['-', '\\', '|', '/'];
	let counter = 0;
	setText(element, '*');
	setInterval(function () {
		setText(element, progressBarSymbols[counter]);
		counter = (counter + 1) % progressBarSymbols.length;
	}, 100);
}