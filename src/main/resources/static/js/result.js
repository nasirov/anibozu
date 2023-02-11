/*
   Author: Nasirov Yuriy
*/

$(document).ready(function () {
	let titles = JSON.parse($('#titlesJson').val());
	if (titles.length > 0) {
		let i;
		let fandubMap = new Map(Object.entries(JSON.parse($('#fandubMapJson').val())));
		for (i in titles) {
			let titleDto = titles[i];
			let animeTitle = buildAnimeTitle();
			registerHoverEvents(animeTitle);
			let animeTitlePoster = buildAnimeTitlePoster(titleDto);
			let animeTitleOverlay = buildAnimeTitleOverlay();
			let animeTitleMalEpisodeOverlay = buildAnimeTitleMalEpisodeOverlay(titleDto);
			if ('AVAILABLE' === titleDto.type) {
				let animeTitleFandubSlider = buildAnimeTitleFandubSlider(titleDto, fandubMap);
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
	return $('<div class="anime-title"></div>');
}

function registerHoverEvents(animeTitle) {
	animeTitle.mouseover(function () {
		fitFandubText(this);
	});
}

function fitFandubText(e) {
	fitText(e.querySelector('.anime-title__link_holder__link--active'));
	fitText(e.querySelector('.anime-title__fandub_episode_overlay--active'));
}

function fitText(e) {
	if (e !== null) {
		textFit(e);
	}
}

function buildAnimeTitlePoster(titleDto) {
	let nameOnMal = titleDto.nameOnMal;
	return $('<img class="anime-title__poster" loading="lazy">')
	.attr('src', titleDto.posterUrlOnMal)
	.attr('alt', nameOnMal)
	.attr('title', nameOnMal + ' episode ' + titleDto.episodeNumberOnMal);
}

function buildAnimeTitleMalEpisodeOverlay(titleDto) {
	let result = $('<div class="anime-title__mal_info"></div>');
	let episode = $('<span class="anime-title__mal_info__episode"></span>').text(titleDto.episodeNumberOnMal + ' ep.');
	let malLink = buildLink('anime-title__mal_info__link mal-icon sized-icon-50', titleDto.animeUrlOnMal);
	result.append(episode);
	result.append(malLink);
	return result;
}

function appendFandubEpisodes(titleDto, animeTitleOverlay, fandubMap) {
	let firstNotActive = true;
	for (const fandub of fandubMap.keys()) {
		let fanDubEpisodeName = titleDto.fandubToEpisodeName[fandub];
		if (fanDubEpisodeName !== undefined) {
			let fandubEpisodeOverlay = buildFandubEpisodeOverlay(fanDubEpisodeName, fandub);
			if (firstNotActive) {
				fandubEpisodeOverlay.toggleClass('anime-title__fandub_episode_overlay--active');
				firstNotActive = false;
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
	let result = $('<div class="anime-title__fandub_slider"></div>');
	let animeTitleLinkHolder = buildAnimeTitleLinkHolder(titleDto, fandubMap);
	if (animeTitleLinkHolder.children().length > LINKS_TO_SHOW_IN_SLIDER) {
		let leftSliderArrow = buildSliderArrow(SliderArrowDirection.LEFT);
		let rightSliderArrow = buildSliderArrow(SliderArrowDirection.RIGHT);
		result.append(leftSliderArrow, animeTitleLinkHolder, rightSliderArrow);
	} else {
		result.append(animeTitleLinkHolder);
	}
	return result;
}

const SliderArrowDirection = {LEFT: 'left', RIGHT: 'right'};

const LINKS_TO_SHOW_IN_SLIDER = 1;

function buildSliderArrow(direction) {
	let result = $(
			'<div class="anime-title__fandub_slider__arrow' + ' anime-title__fandub_slider__arrow--' + direction + '"></div>');
	result.mousedown(function () {
		let linkHolder = this.parentNode.getElementsByClassName('anime-title__link_holder')[0];
		let fandubLinks = linkHolder.children;
		let shiftedFandubLinks = [];
		let previousFandubLink = fandubLinks[getInitPreviousFandubLinkIndex(direction, fandubLinks)];
		for (let i = getLoopCounter(direction, fandubLinks); getLoopCondition(direction, i, fandubLinks);
				i = modifyLoopCounter(direction, i)) {
			let currentFandubLink = fandubLinks[i];
			shiftedFandubLinks[i] = previousFandubLink;
			previousFandubLink = currentFandubLink;
		}
		let fandubLinkToDisableIndex;
		let fandubLinkToEnableIndex;
		if (SliderArrowDirection.LEFT === direction) {
			fandubLinkToDisableIndex = LINKS_TO_SHOW_IN_SLIDER;
			fandubLinkToEnableIndex = 0;
		} else if (SliderArrowDirection.RIGHT === direction) {
			fandubLinkToDisableIndex = shiftedFandubLinks.length - 1;
			fandubLinkToEnableIndex = LINKS_TO_SHOW_IN_SLIDER - 1;
		}
		let fandubLinkToDisable = shiftedFandubLinks[fandubLinkToDisableIndex];
		toggleLinkHolderLinkActive(fandubLinkToDisable);
		let fandubLinkToEnable = shiftedFandubLinks[fandubLinkToEnableIndex];
		toggleLinkHolderLinkActive(fandubLinkToEnable);
		linkHolder.replaceChildren(...shiftedFandubLinks);
		let animeTitle = this.parentNode.parentNode.parentNode;
		toggleFandubEpisodeOverlayActive(animeTitle, fandubLinkToDisable);
		toggleFandubEpisodeOverlayActive(animeTitle, fandubLinkToEnable);
		fitFandubText(animeTitle);
	});
	return result;
}

function getLoopCounter(direction, fandubLinks) {
	let result;
	if (SliderArrowDirection.LEFT === direction) {
		result = 0;
	} else if (SliderArrowDirection.RIGHT === direction) {
		result = fandubLinks.length - 1;
	}
	return result;
}

function getLoopCondition(direction, counter, fandubLinks) {
	let result;
	if (SliderArrowDirection.LEFT === direction) {
		result = counter < fandubLinks.length;
	} else if (SliderArrowDirection.RIGHT === direction) {
		result = counter >= 0;
	}
	return result;
}

function modifyLoopCounter(direction, counter) {
	let result;
	if (SliderArrowDirection.LEFT === direction) {
		result = counter + 1;
	} else if (SliderArrowDirection.RIGHT === direction) {
		result = counter - 1;
	}
	return result;
}

function getInitPreviousFandubLinkIndex(direction, fandubLinks) {
	let result;
	if (SliderArrowDirection.LEFT === direction) {
		result = fandubLinks.length - 1;
	} else if (SliderArrowDirection.RIGHT === direction) {
		result = 0;
	}
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
	let result = $('<div class="anime-title__link_holder"></div>');
	let firstNotActive = true;
	for (const entry of fandubMap.entries()) {
		let fandub = entry[0];
		let fanDubUrl = titleDto.fandubToUrl[fandub];
		if (fanDubUrl !== undefined) {
			let targetClass = 'anime-title__link_holder__link';
			if (firstNotActive) {
				targetClass += ' anime-title__link_holder__link--active';
				firstNotActive = false;
			}
			let fandubLink = buildLink(targetClass, fanDubUrl);
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