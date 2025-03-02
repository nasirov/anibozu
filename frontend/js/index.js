document.addEventListener('DOMContentLoaded', () => {
	document.querySelector('form').addEventListener('submit',
			async e => {
				e.preventDefault();
				const input = document.querySelector('input:valid');
				if (input) {
					const username = input.value;
					input.setAttribute('readonly', '');
					const button = document.querySelector('button');
					const icon = button.querySelector('svg');
					const intervalId = setLoadingProgress(button);
					const animeListResponse = await getAnimeList(username);
					renderResult(username, animeListResponse);
					input.removeAttribute('readonly');
					clearInterval(intervalId);
					setText(button, '');
					button.appendChild(icon);
				}
			});
});

function setLoadingProgress(element) {
	const symbols = ['-', '\\', '|', '/'];
	let counter = 0;
	setText(element, '*');
	return setInterval(() => {
		setText(element, symbols[counter]);
		counter = (counter + 1) % symbols.length;
	}, 100);
}

function setText(element, value) {
	element.textContent = value;
}