/*
   Author: Nasirov Yuriy
*/

document.addEventListener('DOMContentLoaded', () => {
	const usernameInput = document.querySelector('#username-form-input');
	const submitButton = document.querySelector('#username-form-button');

	submitButton.addEventListener('click',
			async e => {
				const readonlyAttribute = 'readonly';
				if (!usernameInput.hasAttribute(readonlyAttribute)) {
					const username = usernameInput.value;
					const validUsername = isValidUsername(username);
					if (validUsername) {
						usernameInput.setAttribute(readonlyAttribute, '');
						setScheduledLoadingMessages(submitButton);
						const animeListResponse = await getAnimeList(username);
						renderResult(username, animeListResponse);
					} else {
						disableEvents(e);
						toggleErrorClass(usernameInput, validUsername);
					}
				} else {
					disableEvents(e);
				}
			});

	usernameInput.addEventListener('keypress',
			e => {
				if (e.key === 'Enter') {
					disableEvents(e);
					submitButton.click();
				}
			});

	usernameInput.addEventListener('keypress', replaceIllegalChars);
	usernameInput.addEventListener('keydown', replaceIllegalChars);
	usernameInput.addEventListener('keyup', replaceIllegalChars);
	usernameInput.addEventListener('onpaste', replaceIllegalChars);
});

function replaceIllegalChars(e) {
	setTimeout(() => {
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
	setInterval(() => {
		setText(element, progressBarSymbols[counter]);
		counter = (counter + 1) % progressBarSymbols.length;
	}, 100);
}