const THEMES = {DARK: 'dark', LIGHT: 'light'};
const THEME_ATTR = 'theme';
const THEME_COOKIE = 'theme';
const DIRECTION = {LEFT: 'left', RIGHT: 'right'};
const GENERIC_ERROR_MESSAGE = 'Sorry, something went wrong. Please try again later.';
const TOO_MANY_REQUESTS_ERROR_MESSAGE = 'You\'ve performed too many requests. Please try again later.';
const SVG_NS = 'http://www.w3.org/2000/svg';

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
	const clickType = 'click';
	const keypressEventType = 'keypress';
	[clickType, keypressEventType].forEach(type => {
				themeSwitcher.addEventListener(type, function (e) {
					if (e.type === clickType || (e.type === keypressEventType && e.key === 'Enter')) {
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
				const animeSiteBadgesList = buildAnimeSiteBadgesList(episodes);
				itemBadges.append(...animeSiteBadgesList);
				const slider = buildSlider(episodes[0]);
				if (episodes.length > 1) {
					slider.prepend(buildSliderArrow(DIRECTION.LEFT));
					slider.appendChild(buildSliderArrow(DIRECTION.RIGHT));
				}
				item.appendChild(slider);
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
	const result = buildDiv('item');
	result.setAttribute('data-id', '1');
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
	const result = buildDiv('mal_badges');
	result.appendChild(buildMalEpisodeBadge(anime));
	if (anime.airing) {
		result.appendChild(buildBadge('Airing'));
	}
	return result;
}

function buildMalEpisodeBadge(anime) {
	const result = buildLink('badge navigable', anime.malUrl);
	const maxEpisodes = anime.maxEpisodes;
	const title = 'Ep. ' + anime.nextEpisode + ' / ' + (maxEpisodes === 0 ? '?' : maxEpisodes);
	result.setAttribute('title', title);
	setText(result, title);
	return result;
}

function buildSlider(episode) {
	const result = buildDiv('slider');
	result.appendChild(buildAnimeLink(episode));
	return result;
}

function buildSliderArrow(direction) {
	const result = buildArrowSvg(direction);
	const clickType = 'click';
	const keypressEventType = 'keypress';
	[clickType, keypressEventType].forEach(type => {
				result.addEventListener(type, function (e) {
					if (e.type === clickType || (e.type === keypressEventType && e.key === 'Enter')) {
						const item = this.parentNode.parentNode;
						const currentDataId = item.getAttribute('data-id');
						const nextDataId = getNextDataId(direction, currentDataId, item);
						toggleAnimeSiteBadgesActive(item, currentDataId);
						item.setAttribute('data-id', nextDataId);
						toggleAnimeSiteBadgesActive(item, nextDataId);
						setNextAnimeLink(item, nextDataId);
					}
				});
			}
	);
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

function buildArrowSvg(direction) {
	let dValue;
	if (DIRECTION.LEFT === direction) {
		dValue = 'M41.4 233.4c-12.5 12.5-12.5 32.8 0 45.3l160 160c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L109.3 256 246.6 118.6c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0l-160 160z';
	} else {
		dValue = 'M278.6 233.4c12.5 12.5 12.5 32.8 0 45.3l-160 160c-12.5 12.5-32.8 12.5-45.3 0s-12.5-32.8 0-45.3L210.7 256 73.4 118.6c-12.5-12.5-12.5-32.8 0-45.3s32.8-12.5 45.3 0l160 160z';
	}
	const result = buildSvg('slider__arrow slider__arrow--' + direction, '0 0 320 512', dValue);
	result.setAttribute('tabindex', '0');
	return result;
}

function buildPlayIconSvg() {
	const dValue = 'M73 39c-14.8-9.1-33.4-9.4-48.5-.9S0 62.6 0 80L0 432c0 17.4 9.4 33.4 24.5 41.9s33.7 8.1 48.5-.9L361 297c14.3-8.7 23-24.2 23-41s-8.7-32.2-23-41L73 39z';
	return buildSvg('play_icon', '0 0 384 512', dValue);
}

function getNextDataId(direction, currentId, item) {
	const firstId = 1;
	const lastId = item.querySelectorAll('div[id]').length;
	let result;
	currentId = Number.parseInt(currentId);
	if (DIRECTION.LEFT === direction) {
		result = currentId - 1;
		if (result < firstId) {
			result = lastId;
		}
	} else {
		result = currentId + 1;
		if (result > lastId) {
			result = firstId;
		}
	}
	return result;
}

function toggleAnimeSiteBadgesActive(item, id) {
	item.querySelector('div[id="' + id + '"]').classList.toggle('anime_site_badges--active');
}

function setNextAnimeLink(item, dataId) {
	const nextAnimeLink = item.querySelector('div[id="' + dataId + '"]').getAttribute('anime-link');
	item.querySelector('div.slider >a').setAttribute('href', nextAnimeLink);
}

function buildAnimeSiteBadgesList(episodes) {
	const result = [];
	for (const i in episodes) {
		const episode = episodes[i];
		const id = Number.parseInt(i) + 1;
		const badges = buildAnimeSiteBadges(episode, id);
		badges.appendChild(buildBadge(episode.name));
		badges.appendChild(buildBadge('Link ' + id + ' / ' + episodes.length));
		if (i === '0') {
			badges.classList.toggle('anime_site_badges--active');
		}
		result.push(badges);
	}
	return result;
}

function buildAnimeSiteBadges(episode, id) {
	const result = buildDiv('anime_site_badges');
	result.setAttribute('id', id);
	result.setAttribute('anime-link', episode.link);
	for (const value of episode.extra) {
		result.appendChild(buildBadge(value));
	}
	return result;
}

function buildBadge(title) {
	const result = buildDiv('badge');
	result.setAttribute('title', title);
	setText(result, title);
	return result;
}

function buildAnimeLink(episode) {
	const result = buildLink('anime_link', episode.link);
	result.setAttribute('tabindex', '0');
	result.appendChild(buildPlayIconSvg());
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
	renderMessage(!errorMessage || errorMessage === '' ? GENERIC_ERROR_MESSAGE : errorMessage);
}

function renderMessage(message) {
	const result = document.createElement('h1');
	result.setAttribute('class', 'message');
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