/*
   Author: Nasirov Yuriy
*/

const SLIDER_ARROW_DIRECTION = {LEFT: 'left', RIGHT: 'right'};
const GENERIC_ERROR_MESSAGE = 'Sorry, something went wrong. Please try again later.';

async function getAnimeList(username) {
	let result;
	try {
		result = await (await fetch(`https://anibozu-backend.nasirov.info/api/v1/user/${username}/anime-list`)).json();
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
		getMainContainer().classList.toggle('anime-container');
		for (const i in animeList) {
			const anime = animeList[i];
			const animeItem = buildAnimeItem();
			animeItem.appendChild(buildMalEpisode(anime));
			animeItem.appendChild(buildMalPoster(anime));
			const episodes = anime.episodes;
			if (episodes.length > 0) {
				animeItem.appendChild(buildFandubSlider(buildFandubLinkHolder(episodes, animeItem)));
			}
			getMainContainer().appendChild(animeItem);
			fitFandubName(animeItem);
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

function buildAnimeItem() {
	const result = document.createElement('div',);
	result.setAttribute('class', 'anime-item');
	return result;
}

function buildMalEpisode(anime) {
	const result = buildLink('anime-item__mal_episode', anime.malUrl);
	const maxEpisodes = anime.maxEpisodes;
	result.textContent = 'Next ' + anime.nextEpisode + ' / ' + (maxEpisodes === 0 ? '?' : maxEpisodes);
	return result;
}

function buildMalPoster(anime) {
	const name = anime.name;
	const result = document.createElement('img');
	result.setAttribute('class', 'anime-item__mal_poster');
	result.setAttribute('loading', 'lazy');
	result.setAttribute('src', anime.posterUrl);
	result.setAttribute('alt', name);
	result.setAttribute('title', name);
	return result;
}

function fitFandubName(element) {
	fitText(element.querySelector('.anime-item__fandub_link_holder__link--active'));
}

function fitText(element) {
	if (element) {
		textFit(element);
	}
}

function buildFandubSlider(fandubLinkHolder) {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime-item__fandub_slider');
	result.appendChild(fandubLinkHolder);
	if (fandubLinkHolder.childElementCount > 1) {
		result.prepend(buildSliderArrow(SLIDER_ARROW_DIRECTION.LEFT));
		result.appendChild(buildSliderArrow(SLIDER_ARROW_DIRECTION.RIGHT));
	}
	return result;
}

function buildSliderArrow(direction) {
	const result = buildArrowSvg(direction);
	const mousedownEventType = 'mousedown';
	const keypressEventType = 'keypress';
	[mousedownEventType, keypressEventType].forEach(type => {
				result.addEventListener(type, function (e) {
					if (e.type === mousedownEventType || (e.type === keypressEventType && e.key === 'Enter')) {
						const fandubLinkHolder = this.parentNode.querySelector('.anime-item__fandub_link_holder');
						const fandubLinks = fandubLinkHolder.children;
						const shiftedFandubLinks = [];
						let previousFandubLink = fandubLinks[getInitPreviousFandubLinkIndex(direction, fandubLinks)];
						for (let i = getLoopCounter(direction, fandubLinks); getLoopCondition(direction, i, fandubLinks);
								i = modifyLoopCounter(direction, i)) {
							shiftedFandubLinks[i] = previousFandubLink;
							previousFandubLink = fandubLinks[i];
						}
						const fandubLinkToDisableIndex = SLIDER_ARROW_DIRECTION.LEFT === direction ? 1 : shiftedFandubLinks.length - 1;
						const fandubLinkToDisable = getFandubLink(shiftedFandubLinks, fandubLinkToDisableIndex);
						const fandubLinkToEnableIndex = 0;
						const fandubLinkToEnable = getFandubLink(shiftedFandubLinks, fandubLinkToEnableIndex);
						fandubLinkHolder.replaceChildren(...shiftedFandubLinks);
						const animeItem = this.parentNode.parentNode;
						toggleFandubEpisodeActive(animeItem, fandubLinkToDisable);
						toggleFandubEpisodeActive(animeItem, fandubLinkToEnable);
						toggleFandubTypesActive(animeItem, fandubLinkToDisable);
						toggleFandubTypesActive(animeItem, fandubLinkToEnable);
						fitFandubName(animeItem);
					}
				});
			}
	);
	return result;
}

function buildArrowSvg(direction) {
	const result = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
	result.setAttribute('class',
			'anime-item__fandub_slider__arrow anime-item__fandub_slider__arrow--' + direction);
	result.setAttribute('viewBox', '0 0 384 512');
	result.setAttribute('tabindex', '0');
	const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
	let dValue;
	if (SLIDER_ARROW_DIRECTION.LEFT === direction) {
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

function getLoopCounter(direction, fandubLinks) {
	let result;
	if (SLIDER_ARROW_DIRECTION.LEFT === direction) {
		result = 0;
	} else if (SLIDER_ARROW_DIRECTION.RIGHT === direction) {
		result = fandubLinks.length - 1;
	}
	return result;
}

function getLoopCondition(direction, counter, fandubLinks) {
	let result;
	if (SLIDER_ARROW_DIRECTION.LEFT === direction) {
		result = counter < fandubLinks.length;
	} else if (SLIDER_ARROW_DIRECTION.RIGHT === direction) {
		result = counter >= 0;
	}
	return result;
}

function modifyLoopCounter(direction, counter) {
	let result;
	if (SLIDER_ARROW_DIRECTION.LEFT === direction) {
		result = counter + 1;
	} else if (SLIDER_ARROW_DIRECTION.RIGHT === direction) {
		result = counter - 1;
	}
	return result;
}

function getInitPreviousFandubLinkIndex(direction, fandubLinks) {
	let result;
	if (SLIDER_ARROW_DIRECTION.LEFT === direction) {
		result = fandubLinks.length - 1;
	} else if (SLIDER_ARROW_DIRECTION.RIGHT === direction) {
		result = 0;
	}
	return result;
}

function getFandubLink(shiftedFandubLinks, index) {
	const result = shiftedFandubLinks[index];
	result.classList.toggle('anime-item__fandub_link_holder__link--active');
	return result;
}

function toggleFandubEpisodeActive(animeItem, fandubLink) {
	toggleActive(animeItem, fandubLink, 'anime-item__fandub_episode');
}

function toggleFandubTypesActive(animeItem, fandubLink) {
	toggleActive(animeItem, fandubLink, 'anime-item__fandub_types');
}

function toggleActive(animeItem, fandubLink, targetClass) {
	animeItem.querySelector(
			'.' + targetClass + '[fandub="' + fandubLink.getAttribute('fandub') + '"][id="'
			+ fandubLink.getAttribute('id') + '"]').classList.toggle(
			targetClass + '--active');
}

function buildFandubLinkHolder(episodes, animeItem) {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime-item__fandub_link_holder');
	for (const i in episodes) {
		const episode = episodes[i];
		const fandub = episode.fandubSource;
		const fandubTypes = buildFandubTypes(episode, fandub, i);
		animeItem.appendChild(fandubTypes);
		const fandubEpisode = buildFandubEpisode(episode.episodeName, fandub, i);
		animeItem.appendChild(fandubEpisode);
		let fandubLinkTargetClass = 'anime-item__fandub_link_holder__link';
		if (i === '0') {
			fandubLinkTargetClass += ' anime-item__fandub_link_holder__link--active';
			fandubEpisode.classList.toggle('anime-item__fandub_episode--active');
			fandubTypes.classList.toggle('anime-item__fandub_types--active');
		}
		fandubLinkTargetClass += ' navigable';
		result.appendChild(buildFandubLink(fandubLinkTargetClass, fandub, i, episode));
	}
	return result;
}

function buildFandubEpisode(targetEpisode, fandub, id) {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime-item__fandub_episode');
	result.setAttribute('fandub', fandub);
	result.setAttribute('id', id);
	result.setAttribute('title', targetEpisode);
	result.textContent = targetEpisode;
	return result;
}

function buildFandubTypes(fandubInfo, fandub, id) {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime-item__fandub_types');
	result.setAttribute('fandub', fandub);
	result.setAttribute('id', id);
	const types = fandubInfo.types;
	for (const i in types) {
		let typeElement = document.createElement('div');
		typeElement.setAttribute('class', 'anime-item__fandub_types__type');
		typeElement.textContent = types[i];
		result.appendChild(typeElement);
	}
	return result;
}

function buildFandubLink(fandubLinkTargetClass, fandub, id, episode) {
	const result = buildLink(fandubLinkTargetClass, episode.episodeUrl);
	result.setAttribute('fandub', fandub);
	result.setAttribute('id', id);
	result.setAttribute('tabindex', '0');
	result.textContent = episode.fandubSourceCanonicalName;
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