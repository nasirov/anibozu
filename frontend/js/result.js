/*
   Author: Nasirov Yuriy
*/

const SLIDER_ARROW_DIRECTION = {LEFT: 'left', RIGHT: 'right'};

const GENERIC_ERROR_MESSAGE = 'Sorry, something went wrong. Please try again later.';

async function getAndRenderAnimeItems(username) {
	try {
		const response = await fetch('https://anibozu-backend.nasirov.info/process/' + username);
		document.querySelector('title').textContent = username;
		if (response.ok) {
			const processResult = await response.json();
			const animeList = processResult.animeList;
			emptyMainContainer();
			if (animeList.length > 0) {
				getMainContainer().classList.toggle('anime-container');
				for (const i in animeList) {
					const anime = animeList[i];
					const animeItem = buildAnimeItem();
					const malEpisode = buildMalEpisode(anime);
					const malLink = buildMalLink(anime);
					malLink.appendChild(malEpisode);
					animeItem.appendChild(malLink);
					const malPoster = buildMalPoster(anime);
					animeItem.appendChild(malPoster);
					const fandubInfoList = anime.fandubInfoList;
					if (fandubInfoList.length > 0) {
						const fandubLinkHolder = buildFandubLinkHolder();
						for (const i in fandubInfoList) {
							const fandubInfo = fandubInfoList[i];
							const fandub = fandubInfo.fandubSource;
							const fandubEpisode = buildFandubEpisode(fandubInfo.episodeName, fandub);
							animeItem.appendChild(fandubEpisode);
							let fandubLinkTargetClass = 'anime-item__fandub_link_holder__link';
							if (i === '0') {
								fandubLinkTargetClass += ' anime-item__fandub_link_holder__link--active';
								fandubEpisode.classList.toggle('anime-item__fandub_episode--active');
							}
							const fandubLink = buildLink(fandubLinkTargetClass, fandubInfo.episodeUrl);
							fandubLink.setAttribute('fandub', fandub);
							fandubLink.textContent = fandubInfo.fandubSourceCanonicalName;
							fandubLinkHolder.appendChild(fandubLink);
						}
						const fandubSlider = buildFandubSlider();
						animeItem.appendChild(fandubSlider);
						fandubSlider.appendChild(fandubLinkHolder);
						if (fandubLinkHolder.childElementCount > 1) {
							fandubSlider.prepend(buildSliderArrow(SLIDER_ARROW_DIRECTION.LEFT));
							fandubSlider.appendChild(buildSliderArrow(SLIDER_ARROW_DIRECTION.RIGHT));
						}
					}
					getMainContainer().appendChild(animeItem);
					fitFandubName(animeItem);
				}
			} else {
				renderErrorMessage(processResult.errorMessage);
			}
		} else {
			renderGenericErrorMessage();
		}
	} catch (e) {
		renderGenericErrorMessage();
	}
}

function buildAnimeItem() {
	const result = document.createElement('div',);
	result.setAttribute('class', 'anime-item');
	return result;
}

function buildMalEpisode(anime) {
	let result = document.createElement('div');
	result.setAttribute('class', 'anime-item__mal_episode');
	const maxEpisodes = anime.maxEpisodes;
	result.textContent = 'Next ' + anime.nextEpisode + ' / ' + (maxEpisodes === '0' ? '?' : maxEpisodes);
	return result;
}

function buildMalLink(anime) {
	return buildLink('anime-item__mal_link', anime.malUrl);
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

function buildFandubSlider() {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime-item__fandub_slider');
	return result;
}

function buildSliderArrow(direction) {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime-item__fandub_slider__arrow anime-item__fandub_slider__arrow--' + direction);
	result.addEventListener('mousedown', function () {
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
		fitFandubName(animeItem);
	});
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
	animeItem.querySelector(
			'.anime-item__fandub_episode[fandub="' + fandubLink.getAttribute('fandub') + '"]').classList.toggle(
			'anime-item__fandub_episode--active');
}

function buildFandubLinkHolder() {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime-item__fandub_link_holder');
	return result;
}

function buildFandubEpisode(targetEpisode, fandub) {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime-item__fandub_episode');
	result.setAttribute('fandub', fandub);
	result.textContent = targetEpisode;
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

function renderGenericErrorMessage() {
	emptyMainContainer();
	renderErrorMessage(GENERIC_ERROR_MESSAGE);
}

function renderErrorMessage(errorMessage) {
	const result = document.createElement('h1');
	result.setAttribute('class', 'error-message');
	result.textContent = errorMessage === '' ? GENERIC_ERROR_MESSAGE : errorMessage;
	getMainContainer().appendChild(result);
}