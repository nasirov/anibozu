document.addEventListener('DOMContentLoaded', () => {
	document.querySelector('#username-form').addEventListener('submit',
			async e => {
				e.preventDefault();
				await submitUsernameForm();
			});
});

async function submitUsernameForm() {
	const usernameInput = document.querySelector('#username-form-input:valid');
	if (usernameInput) {
		const username = usernameInput.value;
		usernameInput.setAttribute('readonly', '');
		setLoadingProgress();
		const animeListResponse = await getAnimeList(username);
		renderResult(username, animeListResponse);
	}
}

function getUsernameFormButton() {
	return document.querySelector('#username-form-button');
}

function setLoadingProgress() {
	const usernameFormButton = getUsernameFormButton();
	const progressBarSymbols = ['-', '\\', '|', '/'];
	let counter = 0;
	setText(usernameFormButton, '*');
	setInterval(() => {
		setText(usernameFormButton, progressBarSymbols[counter]);
		counter = (counter + 1) % progressBarSymbols.length;
	}, 100);
}

function setText(element, value) {
	element.textContent = value;
}