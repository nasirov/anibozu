/*
   Author: Nasirov Yuriy
*/

const SLIDER_ARROW_DIRECTION = {LEFT: 'left', RIGHT: 'right'};

const GENERIC_ERROR_MESSAGE = 'Sorry, something went wrong. Please try again later.';

async function getAndRenderTitles(username) {
	try {
		const response = await fetch('https://anibozu.backend.nasirov.info/process/' + username);
		document.querySelector('title').textContent = username;
		if (response.ok) {
			const processResult = await response.json();
			const titles = processResult.titles;
			emptyMainContainer();
			if (titles.length > 0) {
				const fandubMap = new Map(Object.entries(processResult.fandubMap));
				getMainContainer().classList.toggle('titles-container');
				for (const i in titles) {
					const titleDto = titles[i];
					const animeTitle = buildAnimeTitle();
					const animeTitlePoster = buildAnimeTitlePoster(titleDto);
					const animeTitleOverlay = buildAnimeTitleOverlay();
					const animeTitleMalEpisodeOverlay = buildAnimeTitleMalEpisodeOverlay(titleDto);
					if ('AVAILABLE' === titleDto.type) {
						const animeTitleFandubSlider = buildAnimeTitleFandubSlider(titleDto, fandubMap);
						animeTitleOverlay.appendChild(animeTitleFandubSlider);
						appendFandubEpisodes(titleDto, animeTitleOverlay, fandubMap);
					}
					animeTitle.appendChild(animeTitlePoster);
					animeTitle.appendChild(animeTitleMalEpisodeOverlay);
					animeTitle.appendChild(animeTitleOverlay);
					getMainContainer().appendChild(animeTitle);
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

function buildAnimeTitle() {
	const result = document.createElement('div',);
	result.setAttribute('class', 'anime-title');
	result.addEventListener('mouseover', function () {
		fitFandubText(this);
	});
	return result;
}

function fitFandubText(element) {
	fitText(element.querySelector('.anime-title__link_holder__link--active'));
	fitText(element.querySelector('.anime-title__fandub_episode_overlay--active'));
}

function fitText(element) {
	if (element !== null) {
		textFit(element);
	}
}

function buildAnimeTitlePoster(titleDto) {
	const nameOnMal = titleDto.nameOnMal;
	const result = document.createElement('img');
	result.setAttribute('class', 'anime-title__poster');
	result.setAttribute('loading', 'lazy');
	result.setAttribute('src', titleDto.posterUrlOnMal);
	result.setAttribute('alt', nameOnMal);
	result.setAttribute('title', nameOnMal + ' episode ' + titleDto.episodeNumberOnMal);
	return result;
}

function buildAnimeTitleMalEpisodeOverlay(titleDto) {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime-title__mal_info');
	const episode = document.createElement('span');
	episode.setAttribute('class', 'anime-title__mal_info__episode');
	episode.textContent = titleDto.episodeNumberOnMal + ' ep.';
	const malLink = buildLink('anime-title__mal_info__link mal-icon sized-icon-50', titleDto.animeUrlOnMal);
	result.appendChild(episode);
	result.appendChild(malLink);
	return result;
}

function appendFandubEpisodes(titleDto, animeTitleOverlay, fandubMap) {
	let isFirstNotActive = true;
	for (const fandub of fandubMap.keys()) {
		const fanDubEpisodeName = titleDto.fandubToEpisodeName[fandub];
		if (fanDubEpisodeName !== undefined) {
			const fandubEpisodeOverlay = buildFandubEpisodeOverlay(fanDubEpisodeName, fandub);
			if (isFirstNotActive) {
				fandubEpisodeOverlay.classList.toggle('anime-title__fandub_episode_overlay--active');
				isFirstNotActive = false;
			}
			animeTitleOverlay.appendChild(fandubEpisodeOverlay);
		}
	}
}

function buildFandubEpisodeOverlay(targetEpisode, fandub) {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime-title__fandub_episode_overlay');
	result.setAttribute('fandub', fandub);
	result.textContent = targetEpisode;
	return result;
}

function buildAnimeTitleOverlay() {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime-title__overlay');
	return result;
}

function buildAnimeTitleFandubSlider(titleDto, fandubMap) {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime-title__fandub_slider');
	const animeTitleLinkHolder = buildAnimeTitleLinkHolder(titleDto, fandubMap);
	if (animeTitleLinkHolder.childElementCount > 1) {
		result.appendChild(buildSliderArrow(SLIDER_ARROW_DIRECTION.LEFT));
		result.appendChild(animeTitleLinkHolder);
		result.appendChild(buildSliderArrow(SLIDER_ARROW_DIRECTION.RIGHT));
	} else {
		result.appendChild(animeTitleLinkHolder);
	}
	return result;
}

function buildSliderArrow(direction) {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime-title__fandub_slider__arrow anime-title__fandub_slider__arrow--' + direction);
	result.addEventListener('mousedown', function () {
		const linkHolder = this.parentNode.querySelector('.anime-title__link_holder');
		const fandubLinks = linkHolder.children;
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
		linkHolder.replaceChildren(...shiftedFandubLinks);
		const animeTitle = this.parentNode.parentNode.parentNode;
		toggleFandubEpisodeOverlayActive(animeTitle, fandubLinkToDisable);
		toggleFandubEpisodeOverlayActive(animeTitle, fandubLinkToEnable);
		fitFandubText(animeTitle);
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
	result.classList.toggle('anime-title__link_holder__link--active');
	return result;
}

function toggleFandubEpisodeOverlayActive(animeTitle, fandubLink) {
	animeTitle.querySelector(
			'.anime-title__fandub_episode_overlay[fandub="' + fandubLink.getAttribute('fandub') + '"]').classList.toggle(
			'anime-title__fandub_episode_overlay--active');
}

function buildAnimeTitleLinkHolder(titleDto, fandubMap) {
	const result = document.createElement('div');
	result.setAttribute('class', 'anime-title__link_holder');
	let isFirstNotActive = true;
	for (const entry of fandubMap.entries()) {
		const fandub = entry[0];
		const fanDubUrl = titleDto.fandubToUrl[fandub];
		if (fanDubUrl !== undefined) {
			let targetClass = 'anime-title__link_holder__link';
			if (isFirstNotActive) {
				targetClass += ' anime-title__link_holder__link--active';
				isFirstNotActive = false;
			}
			const fandubLink = buildLink(targetClass, fanDubUrl);
			fandubLink.setAttribute('fandub', fandub);
			fandubLink.textContent = entry[1];
			result.appendChild(fandubLink);
		}
	}
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