const THEMES = {DARK: 'dark', LIGHT: 'light'};
const THEME_ATTR = 'theme';
const THEME_COOKIE = 'theme';
const GENERIC_ERROR_MESSAGE = 'Sorry, something went wrong. Please try again later.';
const TOO_MANY_REQUESTS_ERROR_MESSAGE = 'You\'ve performed too many requests. Please try again later.';
const SVG_NS = 'http://www.w3.org/2000/svg';
const CLICK_TYPE = 'click';
const KEYPRESS_EVENT_TYPE = 'keypress';
const KEY_ENTER = 'Enter';

document.addEventListener('DOMContentLoaded', () => {
	switchTheme();
	setThemeSwitcherEvents();
	setFormEvents();
});

/**
 * Theme setting priority:
 * 1. user's choice - {@link THEME_COOKIE}
 * 2. browser's default - media prefers-color-scheme
 */
function switchTheme() {
	let theme;
	if (window.localStorage) {
		theme = localStorage.getItem(THEME_COOKIE);
	}
	if (!theme && window.matchMedia) {
		theme = window.matchMedia('(prefers-color-scheme: light)').matches ? THEMES.LIGHT : THEMES.DARK;
	}
	setTheme(theme);
}

function setThemeSwitcherEvents() {
	const themeSwitcher = document.querySelector('.theme-switcher');

	[CLICK_TYPE, KEYPRESS_EVENT_TYPE].forEach(type => {
			themeSwitcher.addEventListener(type, function (e) {
				if (e.type === CLICK_TYPE || (e.type === KEYPRESS_EVENT_TYPE && e.key === KEY_ENTER)) {
					const currentTheme = document.documentElement.getAttribute(THEME_ATTR);
					const nextTheme = currentTheme === THEMES.LIGHT ? THEMES.DARK : THEMES.LIGHT;
					setTheme(nextTheme);
					if (window.localStorage) {
						localStorage.setItem(THEME_COOKIE, nextTheme);
					}
				}
			});
		}
	);
}

function setTheme(theme) {
	if (theme) {
		document.documentElement.setAttribute(THEME_ATTR, theme);
	}
}

function setFormEvents() {
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
				await renderAnimeList(username);
				input.removeAttribute('readonly');
				clearInterval(intervalId);
				setText(button, '');
				button.appendChild(icon);
			}
		});
}

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

async function renderAnimeList(username) {
	try {
		const response = await fetch(`https://api.anibozu.moe/api/v1/user/${username}/anime-list`);
		if (response.status === 429) {
			renderErrorMessage(TOO_MANY_REQUESTS_ERROR_MESSAGE);
			return;
		}

		if (response.status >= 500) {
			renderErrorMessage(GENERIC_ERROR_MESSAGE);
			return;
		}

		const animeListDto = await response.json();
		const animeList = animeListDto.animeList;
		if (!animeList || animeList.length === 0) {
			renderErrorMessage(animeListDto.errorMessage);
			return;
		}

		const items = buildDiv('items');
		for (const i in animeList) {
			const anime = animeList[i];
			const item = buildItem();
			item.appendChild(buildPoster(anime));
			const itemBadges = buildDiv('item__badges');
			itemBadges.appendChild(buildMalBadges(anime));
			item.appendChild(itemBadges);
			const episodes = anime.episodes;
			if (episodes.length > 0) {
				const stats = buildDiv('anime_stats_badges');
				addTranslationTypeStats(episodes, 'sub', stats);
				addTranslationTypeStats(episodes, 'dub', stats);
				if (stats.childNodes.length > 0) {
					itemBadges.appendChild(stats);
				}
				item.appendChild(buildDialog(item, episodes));
			}
			items.appendChild(item);
		}

		renderMessage(`${username}'s watching anime list`);
		getMainContainer().appendChild(items);
	} catch (e) {
		renderErrorMessage(GENERIC_ERROR_MESSAGE);
		console.error(e, e.stack);
	}
}

function buildDiv(targetClass) {
	const result = document.createElement('div');
	result.setAttribute('class', targetClass);
	return result;
}

function buildItem() {
	const result = buildDiv('item navigable');
	result.setAttribute('tabindex', '0');
	return result;
}

function buildPoster(anime) {
	const name = anime.name;
	const result = document.createElement('img');
	result.setAttribute('class', 'item__poster');
	result.setAttribute('loading', 'lazy');
	result.setAttribute('src', anime.posterUrl);
	result.setAttribute('alt', name);
	result.setAttribute('title', name);
	return result;
}

function buildMalBadges(anime) {
	const result = buildLink('mal_badges navigable', anime.malUrl);
	if (anime.name) {
		result.appendChild(buildBadge(anime.name));
	}
	result.appendChild(buildMalEpisodeBadge(anime));
	if (anime.airing) {
		result.appendChild(buildBadge('Airing'));
	}
	return result;
}

function buildMalEpisodeBadge(anime) {
	const result = buildDiv('badge navigable');
	const maxEpisodes = anime.maxEpisodes;
	const title = 'Next ep. ' + anime.nextEpisode + ' / ' + (maxEpisodes === 0 ? '?' : maxEpisodes);
	result.setAttribute('title', title);
	setText(result, title);
	return result;
}

function buildSvg(targetClass, viewBox, dValue) {
	const result = document.createElementNS(SVG_NS, 'svg');
	result.setAttribute('class', targetClass);
	result.setAttribute('viewBox', viewBox);
	const path = document.createElementNS(SVG_NS, 'path');
	path.setAttribute('d', dValue);
	result.appendChild(path);
	return result;
}

function addTranslationTypeStats(episodes, translationType, stats) {
	const matchedAmount = episodes.filter(x => x.extra.includes(translationType)).length;
	if (matchedAmount > 0) {
		let badge;

		if (translationType === 'dub') {
			badge = buildDubBadge();
		}

		if (translationType === 'sub') {
			badge = buildSubBadge();
		}

		if (badge) {
			const span = document.createElement('span');
			setText(span, matchedAmount);
			badge.appendChild(span);
			stats.appendChild(badge);
		}
	}
}

function buildDialog(item, episodes) {
	const result = document.createElement('dialog');
	const closeButton = buildDialogCloseButton();

	[CLICK_TYPE, KEYPRESS_EVENT_TYPE].forEach(type => {
		item.addEventListener(type, function (e) {
			if (e.target === item && e.key === KEY_ENTER) {
				result.showModal();
			}

			if (e.type !== CLICK_TYPE) {
				return;
			}

			if (e.target === item ||
				e.target.className === 'item__badges' ||
				e.target.className === 'anime_stats_badges' ||
				e.target.parentElement.className === 'badge' ||
				e.target.parentElement.className === 'anime_stats_badges') {
				result.showModal();
			}
		});

		closeButton.addEventListener(type, e => {
			if (e.type === CLICK_TYPE || e.key === KEY_ENTER) {
				result.close();
			}
		});
		}
	);

	result.addEventListener(CLICK_TYPE, e => {
		if (e.target === result) {
			result.close();
		}
	});

	const animeSiteBadgesList = buildAnimeSiteBadgesList(episodes);
	result.append(...animeSiteBadgesList);
	result.appendChild(closeButton);
	return result;
}

function buildAnimeSiteBadgesList(episodes) {
	const result = [];
	for (const i in episodes) {
		const episode = episodes[i];
		const badges = buildLink('anime_site_badges navigable', episode.link);

		for (const value of episode.extra) {
			if (value === 'dub') {
				badges.appendChild(buildDubBadge());
			} else if (value === 'sub') {
				badges.appendChild(buildSubBadge());
			} else {
				badges.appendChild(buildBadge(value));
			}
		}

		badges.appendChild(buildBadge(episode.name));
		result.push(badges);
	}

	return result;
}

function buildBadge(title) {
	const result = buildBadgeWithoutText(title);
	setText(result, title);
	return result;
}

function buildBadgeWithoutText(title) {
	const result = buildDiv('badge');
	result.setAttribute('title', title);
	return result;
}

function buildDubBadge() {
	const result = buildBadgeWithoutText('dub');
	const dValue = 'M192 0C139 0 96 43 96 96l0 160c0 53 43 96 96 96s96-43 96-96l0-160c0-53-43-96-96-96zM64 216c0-13.3-10.7-24-24-24s-24 10.7-24 24l0 40c0 89.1 66.2 162.7 152 174.4l0 33.6-48 0c-13.3 0-24 10.7-24 24s10.7 24 24 24l72 0 72 0c13.3 0 24-10.7 24-24s-10.7-24-24-24l-48 0 0-33.6c85.8-11.7 152-85.3 152-174.4l0-40c0-13.3-10.7-24-24-24s-24 10.7-24 24l0 40c0 70.7-57.3 128-128 128s-128-57.3-128-128l0-40z';
	result.appendChild(buildSvg('', '0 0 384 512', dValue));
	return result;
}

function buildSubBadge() {
	const result = buildBadgeWithoutText('sub');
	const dValue = 'M0 96C0 60.7 28.7 32 64 32l448 0c35.3 0 64 28.7 64 64l0 320c0 35.3-28.7 64-64 64L64 480c-35.3 0-64-28.7-64-64L0 96zM200 208c14.2 0 27 6.1 35.8 16c8.8 9.9 24 10.7 33.9 1.9s10.7-24 1.9-33.9c-17.5-19.6-43.1-32-71.5-32c-53 0-96 43-96 96s43 96 96 96c28.4 0 54-12.4 71.5-32c8.8-9.9 8-25-1.9-33.9s-25-8-33.9 1.9c-8.8 9.9-21.6 16-35.8 16c-26.5 0-48-21.5-48-48s21.5-48 48-48zm144 48c0-26.5 21.5-48 48-48c14.2 0 27 6.1 35.8 16c8.8 9.9 24 10.7 33.9 1.9s10.7-24 1.9-33.9c-17.5-19.6-43.1-32-71.5-32c-53 0-96 43-96 96s43 96 96 96c28.4 0 54-12.4 71.5-32c8.8-9.9 8-25-1.9-33.9s-25-8-33.9 1.9c-8.8 9.9-21.6 16-35.8 16c-26.5 0-48-21.5-48-48z';
	result.appendChild(buildSvg('', '0 0 576 512', dValue));
	return result;
}

function buildDialogCloseButton() {
	const result = buildDiv('dialog__close-button navigable');
	result.setAttribute('tabindex', '0');
	const dValue = 'M183.1 137.4C170.6 124.9 150.3 124.9 137.8 137.4C125.3 149.9 125.3 170.2 137.8 182.7L275.2 320L137.9 457.4C125.4 469.9 125.4 490.2 137.9 502.7C150.4 515.2 170.7 515.2 183.2 502.7L320.5 365.3L457.9 502.6C470.4 515.1 490.7 515.1 503.2 502.6C515.7 490.1 515.7 469.8 503.2 457.3L365.8 320L503.1 182.6C515.6 170.1 515.6 149.8 503.1 137.3C490.6 124.8 470.3 124.8 457.8 137.3L320.5 274.7L183.1 137.4z';
	result.appendChild(buildSvg('icon', '0 0 640 640', dValue));
	return result;
}

function buildLink(targetClass, href) {
	const result = document.createElement('a');
	result.setAttribute('class', targetClass);
	result.setAttribute('target', '_blank');
	result.setAttribute('href', href);
	return result;
}

function getMainContainer() {
	return document.querySelector('main');
}

function renderErrorMessage(errorMessage) {
	const message = !errorMessage || errorMessage === '' ? GENERIC_ERROR_MESSAGE : errorMessage;
	renderMessage(message, 'error-message');
}

function renderMessage(message, extraClass = '') {
	const result = document.createElement('h1');
	result.setAttribute('class', 'message ' + extraClass);
	setText(result, message);
	removeMessage();
	removeItems();
	getMainContainer().appendChild(result);
}

function removeElement(selector) {
	const element = document.querySelector(selector);
	if (element) {
		element.remove();
	}
}

function removeMessage() {
	removeElement('main > h1');
}

function removeItems() {
	removeElement('main > .items');
}