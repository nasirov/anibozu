/*
   Author: Nasirov Yuriy
*/

const SLIDER_ARROW_DIRECTION = {LEFT: 'left', RIGHT: 'right'};

$(document).ready(function () {
	const titles = JSON.parse($('#titlesJson').val());
	if (titles.length > 0) {
		const fandubMap = new Map(Object.entries(JSON.parse($('#fandubMapJson').val())));
		for (const i in titles) {
			const titleDto = titles[i];
			const animeTitle = buildAnimeTitle();
			const animeTitlePoster = buildAnimeTitlePoster(titleDto);
			const animeTitleOverlay = buildAnimeTitleOverlay();
			const animeTitleMalEpisodeOverlay = buildAnimeTitleMalEpisodeOverlay(titleDto);
			if ('AVAILABLE' === titleDto.type) {
				const animeTitleFandubSlider = buildAnimeTitleFandubSlider(titleDto, fandubMap);
				animeTitleOverlay.append(animeTitleFandubSlider);
				appendFandubEpisodes(titleDto, animeTitleOverlay, fandubMap);
			}
			animeTitle.append(animeTitlePoster, animeTitleMalEpisodeOverlay, animeTitleOverlay);
			getTitlesContainer().append(animeTitle);
		}
	} else {
		getTitlesContainer().append(
				$('<h1 class="error-message"></h1>').text('Sorry, something went wrong. Please try again later.'));
	}
});

function buildAnimeTitle() {
	const result = $('<div class="anime-title"></div>');
	result.mouseover(function () {
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
	return $('<img class="anime-title__poster" loading="lazy">')
	.attr('src', titleDto.posterUrlOnMal)
	.attr('alt', nameOnMal)
	.attr('title', nameOnMal + ' episode ' + titleDto.episodeNumberOnMal);
}

function buildAnimeTitleMalEpisodeOverlay(titleDto) {
	const result = $('<div class="anime-title__mal_info"></div>');
	const episode = $('<span class="anime-title__mal_info__episode"></span>').text(titleDto.episodeNumberOnMal + ' ep.');
	const malLink = buildLink('anime-title__mal_info__link mal-icon sized-icon-50', titleDto.animeUrlOnMal);
	result.append(episode);
	result.append(malLink);
	return result;
}

function appendFandubEpisodes(titleDto, animeTitleOverlay, fandubMap) {
	let isFirstNotActive = true;
	for (const fandub of fandubMap.keys()) {
		const fanDubEpisodeName = titleDto.fandubToEpisodeName[fandub];
		if (fanDubEpisodeName !== undefined) {
			const fandubEpisodeOverlay = buildFandubEpisodeOverlay(fanDubEpisodeName, fandub);
			if (isFirstNotActive) {
				fandubEpisodeOverlay.toggleClass('anime-title__fandub_episode_overlay--active');
				isFirstNotActive = false;
			}
			animeTitleOverlay.append(fandubEpisodeOverlay);
		}
	}
}

function buildFandubEpisodeOverlay(targetEpisode, fandub) {
	return $('<div></div>').attr('class', 'anime-title__fandub_episode_overlay').attr('fandub', fandub).text(targetEpisode);
}

function buildAnimeTitleOverlay() {
	return $('<div class="anime-title__overlay"></div>');
}

function buildAnimeTitleFandubSlider(titleDto, fandubMap) {
	const result = $('<div class="anime-title__fandub_slider"></div>');
	const animeTitleLinkHolder = buildAnimeTitleLinkHolder(titleDto, fandubMap);
	if (animeTitleLinkHolder.children().length > 1) {
		const leftSliderArrow = buildSliderArrow(SLIDER_ARROW_DIRECTION.LEFT);
		const rightSliderArrow = buildSliderArrow(SLIDER_ARROW_DIRECTION.RIGHT);
		result.append(leftSliderArrow, animeTitleLinkHolder, rightSliderArrow);
	} else {
		result.append(animeTitleLinkHolder);
	}
	return result;
}

function buildSliderArrow(direction) {
	const result = $(
			'<div class="anime-title__fandub_slider__arrow' + ' anime-title__fandub_slider__arrow--' + direction + '"></div>');
	result.mousedown(function () {
		const linkHolder = this.parentNode.getElementsByClassName('anime-title__link_holder')[0];
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
	toggleLinkHolderLinkActive(result);
	return result;
}

function toggleFandubEpisodeOverlayActive(animeTitle, fandubLink) {
	$(animeTitle.querySelector(
			'.anime-title__fandub_episode_overlay[fandub="' + $(fandubLink).attr('fandub') + '"]')).toggleClass(
			'anime-title__fandub_episode_overlay--active');
}

function toggleLinkHolderLinkActive(fandubLink) {
	$(fandubLink).toggleClass('anime-title__link_holder__link--active');
}

function buildAnimeTitleLinkHolder(titleDto, fandubMap) {
	const result = $('<div class="anime-title__link_holder"></div>');
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
			fandubLink.attr('fandub', fandub);
			fandubLink.text(entry[1]);
			result.append(fandubLink);
		}
	}
	return result;
}

function buildLink(targetClass, href) {
	return $('<a target="_blank"></a>')
	.attr('class', targetClass)
	.attr('href', href);
}

function getTitlesContainer() {
	return $('.main-container');
}