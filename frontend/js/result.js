/*
   Author: Nasirov Yuriy
*/

const DIRECTION = {LEFT: 'left', RIGHT: 'right'};
const GENERIC_ERROR_MESSAGE = 'Sorry, something went wrong. Please try again later.';

async function getAnimeList(username) {
	let result;
	try {
		result = await (await fetch(`https://api.anibozu.moe/api/v1/user/${username}/anime-list`)).json();
	} catch (e) {
		console.error(e, e.stack);
	}
	return result;
}

function renderResult(username, animeListResponse) {
	try {
		setTitle(username);
		emptyMainContainer();
		if (!animeListResponse) {
			renderErrorMessage(GENERIC_ERROR_MESSAGE);
		}
		const animeList = animeListResponse.animeList;
		if (!animeList || animeList.length === 0) {
			renderErrorMessage(animeListResponse.errorMessage);
		}
		const mainContainer = getMainContainer();
		mainContainer.classList.toggle('items-container');
		for (const i in animeList) {
			const anime = animeList[i];
			const item = buildItem();
			item.appendChild(buildMalEpisode(anime));
			item.appendChild(buildMalPoster(anime));
			const episodes = anime.episodes;
			if (episodes.length > 0) {
				const animeSiteInfoList = buildAnimeSiteInfoList(episodes);
				item.append(...animeSiteInfoList);
				const slider = buildSlider(episodes[0]);
				if (episodes.length > 1) {
					slider.prepend(buildSliderArrow(DIRECTION.LEFT));
					slider.appendChild(buildSliderArrow(DIRECTION.RIGHT));
				}
				item.appendChild(slider);
			}
			mainContainer.appendChild(item);
		}
	} catch (e) {
		emptyMainContainer();
		renderErrorMessage(GENERIC_ERROR_MESSAGE);
		console.error(e, e.stack);
	}
}

function setTitle(username) {
	document.querySelector('title').textContent = `${username}'s watching anime list`;
}

function buildItem() {
	const result = document.createElement('div');
	result.setAttribute('class', 'item');
	result.setAttribute('data-id', '1');
	return result;
}

function buildMalEpisode(anime) {
	const result = buildLink('mal_episode', anime.malUrl);
	const maxEpisodes = anime.maxEpisodes;
	result.textContent = 'Ep. ' + anime.nextEpisode + ' / ' + (maxEpisodes === 0 ? '?' : maxEpisodes);
	return result;
}

function buildMalPoster(anime) {
	const name = anime.name;
	const result = document.createElement('img');
	result.setAttribute('class', 'mal_poster');
	result.setAttribute('loading', 'lazy');
	result.setAttribute('src', anime.posterUrl);
	result.setAttribute('alt', name);
	result.setAttribute('title', name);
	return result;
}

function buildSlider(episode) {
	const result = document.createElement('div');
	result.setAttribute('class', 'slider');
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
						const firstId = 1;
						const lastId = item.querySelectorAll('div[id]').length;
						const nextDataId = getNextDataId(direction, currentDataId, firstId, lastId);
						toggleAnimeSiteInfoActive(item, currentDataId);
						item.setAttribute('data-id', nextDataId);
						toggleAnimeSiteInfoActive(item, nextDataId);
						setNextAnimeLink(item, nextDataId);
					}
				});
			}
	);
	return result;
}

function buildArrowSvg(direction) {
	const result = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
	result.setAttribute('class', 'slider__arrow slider__arrow--' + direction);
	result.setAttribute('viewBox', '0 0 384 512');
	result.setAttribute('tabindex', '0');
	const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
	let dValue;
	if (DIRECTION.LEFT === direction) {
		dValue = 'M380.6 81.7c7.9 15.8 1.5 35-14.3 42.9L103.6 256 366.3 387.4c15.8 7.9 22.2 27.1 14.3 42.9s-27.1 22.2-42.9'
				+ ' 14.3l-320-160C6.8 279.2 0 268.1 0 256s6.8-23.2 17.7-28.6l320-160c15.8-7.9 35-1.5 42.9 14.3z';
	} else {
		dValue = 'M3.4 81.7c-7.9 15.8-1.5 35 14.3 42.9L280.5 256 17.7 387.4C1.9 395.3-4.5 414.5 3.4 430.3s27.1 22.2 42.9'
				+ ' 14.3l320-160c10.8-5.4 17.7-16.5 17.7-28.6s-6.8-23.2-17.7-28.6l-320-160c-15.8-7.9-35-1.5-42.9 14.3z';
	}
	path.setAttribute('d', dValue);
	result.appendChild(path);
	return result;
}

function getNextDataId(direction, currentId, firstId, lastId) {
	let result;
	currentId = Number.parseInt(currentId);
	if (DIRECTION.LEFT === direction) {
		result = currentId - 1;
		if (result < firstId) {
			result = firstId;
		}
	} else {
		result = currentId + 1;
		if (result > lastId) {
			result = lastId;
		}
	}
	return result;
}

function toggleAnimeSiteInfoActive(item, id) {
	item.querySelector('div[id="' + id + '"]').classList.toggle('anime_site_info--active');
}

function setNextAnimeLink(item, dataId) {
	const nextAnimeLink = item.querySelector('div[id="' + dataId + '"]').getAttribute('anime-link');
	item.querySelector('div.slider >a').setAttribute('href', nextAnimeLink);
}

function buildAnimeSiteInfoList(episodes) {
	const result = [];
	for (const i in episodes) {
		const episode = episodes[i];
		const id = Number.parseInt(i) + 1;
		const animeSiteInfo = buildAnimeSiteInfo(episode, id);
		animeSiteInfo.appendChild(buildAnimeSiteInfoItem(episode.name));
		animeSiteInfo.appendChild(buildAnimeSiteInfoItem('Link ' + id + ' / ' + episodes.length));
		if (i === '0') {
			animeSiteInfo.classList.toggle('anime_site_info--active');
		}
		result.push(animeSiteInfo);
	}
	return result;
}

function buildAnimeSiteInfo(episode, id) {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime_site_info');
	result.setAttribute('id', id);
	result.setAttribute('anime-link', episode.link);
	for (const value of episode.extra) {
		result.appendChild(buildAnimeSiteInfoItem(value));
	}
	return result;
}

function buildAnimeSiteInfoItem(value) {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime_site_info__item');
	result.setAttribute('title', value);
	result.textContent = value;
	return result;
}

function buildAnimeLink(episode) {
	const result = buildLink('anime_link navigable', episode.link);
	result.setAttribute('tabindex', '0');
	result.textContent = 'Watch';
	return result;
}

function buildLink(targetClass, href) {
	let result = document.createElement('a');
	result.setAttribute('class', targetClass);
	result.setAttribute('target', '_blank');
	result.setAttribute('href', href);
	return result;
}

function getMainContainer() {
	return document.querySelector('.main-container');
}

function emptyMainContainer() {
	getMainContainer().innerHTML = '';
}

function renderErrorMessage(errorMessage) {
	const result = document.createElement('h1');
	result.setAttribute('class', 'error-message');
	result.textContent = !errorMessage || errorMessage === '' ? GENERIC_ERROR_MESSAGE : errorMessage;
	getMainContainer().appendChild(result);
}