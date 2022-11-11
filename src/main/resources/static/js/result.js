/**
 * @author Nasirov Yuriy
 */
$(document).ready(function () {
  let titles = JSON.parse($('#titles').val());
  if (titles.length > 0) {
    let i;
    let fandubListArray = $('#fandubList').val().split(',');
    for (i in titles) {
      let title = titles[i];
      if ('AVAILABLE' === title.type) {
        buildAvailable(title, fandubListArray);
      } else {
        buildNotAvailable(title);
      }
    }
  } else {
    setStatus('Sorry, something went wrong. Please try again later.');
  }
});

function getTitlesSection() {
  return $('#titles-section');
}

function buildAvailable(titleDto, fandubList) {
  let animeTitle = buildAnimeTitle();
  let nameOnMal = titleDto.nameOnMal;
  let episodeNumberOnMal = titleDto.episodeNumberOnMal;
  let animeTitlePoster = buildAnimeTitlePoster(titleDto.posterUrlOnMal,
      nameOnMal,
      nameOnMal + ' episode ' + episodeNumberOnMal);
  let malEpisodeOverlay = buildMalEpisodeOverlay(episodeNumberOnMal);
  let overlay = buildOverlay();
  let malLink = buildLink('anime-title__overlay__mal_link',
      titleDto.animeUrlOnMal);
  overlay.append(malLink);
  let linkHolder = buildLinkHolder();
  let linksAmount = appendLinks(titleDto, linkHolder, fandubList);
  let fandubSlider = buildFandubSlider(titleDto);
  if (linksAmount > LINKS_TO_SHOW_IN_SLIDER) {
    let leftSliderArrow = buildSliderArrow(SliderArrowDirection.LEFT);
    let rightSliderArrow = buildSliderArrow(SliderArrowDirection.RIGHT);
    fandubSlider.append(leftSliderArrow, linkHolder, rightSliderArrow);
    overlay.append(fandubSlider);
  } else {
    fandubSlider.append(linkHolder);
    overlay.append(fandubSlider);
  }
  animeTitle.append(animeTitlePoster, malEpisodeOverlay, overlay);
  appendFandubEpisodes(titleDto, animeTitle, fandubList);
  getTitlesSection().append(animeTitle);
}

function buildNotAvailable(titleDto) {
  let animeTitle = buildAnimeTitle();
  let animeTitlePoster = buildAnimeTitlePoster(titleDto.posterUrlOnMal,
      titleDto.nameOnMal,
      titleDto.nameOnMal);
  let overlay = buildOverlay();
  let malLink = buildLink('anime-title__overlay__mal_link',
      titleDto.animeUrlOnMal);
  overlay.append(malLink);
  animeTitle.append(animeTitlePoster, overlay);
  getTitlesSection().append(animeTitle);
}

function setStatus(message) {
  $('#status').text(message);
}

function buildAnimeTitle() {
  return $('<div class="anime-title"></div>');
}

function buildAnimeTitlePoster(src, alt, title) {
  return $('<img class="anime-title__poster" loading="lazy">')
  .attr('src', src)
  .attr('alt', alt)
  .attr('title', title);
}

function buildMalEpisodeOverlay(targetEpisode) {
  let episodeOverlay = $(
      '<div class="anime-title__mal_episode_overlay"></div>');
  let episodeSpan = $(
      '<span class="anime-title__mal_episode_overlay--block"></span>').text(
      targetEpisode);
  episodeOverlay.append(episodeSpan,
      '<span class="anime-title__mal_episode_overlay--block">episode</span>');
  return episodeOverlay;
}

function appendFandubEpisodes(titleDto, animeTitle, fandubList) {
  let i;
  for (i in fandubList) {
    let fandub = fandubList[i];
    let fanDubEpisodeName = titleDto.fandubToEpisodeName[fandub];
    if (fanDubEpisodeName !== undefined) {
      let id = buildFandubEpisodeOverlayId(titleDto, fandub);
      let fandubEpisodeOverlay = buildFandubEpisodeOverlay(fanDubEpisodeName,
          fandub, id);
      animeTitle.append(fandubEpisodeOverlay);
    }
  }
}

function buildFandubEpisodeOverlayId(titleDto, fandub) {
  return titleDto.nameOnMal.replace(/[^a-zA-Z\d_-]/g, '_') + '_' + fandub;
}

function buildFandubSliderId(titleDto) {
  return titleDto.nameOnMal.replace(/[^a-zA-Z\d_-]/g, '_') + '_fandub_slider';
}

function buildFandubEpisodeOverlay(targetEpisode, fandub, id) {
  return $('<div></div>').attr('class',
      'anime-title__fandub_episode_overlay is_disabled').attr(
      'fandub', fandub).attr('id', id).text(targetEpisode);
}

function buildOverlay() {
  return $('<div class="anime-title__overlay"></div>');
}

function buildFandubSlider(titleDto) {
  return $('<div class="anime-title__fandub_slider"></div>').attr('id',
      buildFandubSliderId(titleDto));
}

const SliderArrowDirection = {LEFT: 'left', RIGHT: 'right'};

const LINKS_TO_SHOW_IN_SLIDER = 3;

function buildSliderArrow(direction) {
  let container = $(
      '<div id="arrow ' + direction + '" '
      + 'class="anime-title__fandub_slider__arrow'
      + ' anime-title__fandub_slider__arrow--' + direction + '"></div>');
  container.mousedown(function () {
    let linkHolder = this.parentNode.getElementsByClassName('anime-title__link_holder')[0];
    let fandubLinks = linkHolder.children;
    let shiftedFandubLinks = [];
    let previousFandubLink = fandubLinks[getInitPreviousFandubLinkIndex(direction, fandubLinks)];
    for (let i = getLoopCounter(direction, fandubLinks);
        getLoopCondition(direction, i, fandubLinks);
        i = modifyLoopCounter(direction, i)) {
      let currentFandubLink = fandubLinks[i];
      shiftedFandubLinks[i] = previousFandubLink;
      previousFandubLink = currentFandubLink;
    }
    disableAndEnableFandubLinks(direction, shiftedFandubLinks);
    linkHolder.replaceChildren(...shiftedFandubLinks);
  });
  return container;
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

function disableAndEnableFandubLinks(direction, shiftedFandubLinks) {
  let fandubLinkToDisableIndex;
  let fandubLinkToEnableIndex;
  if (SliderArrowDirection.LEFT === direction) {
    fandubLinkToDisableIndex = LINKS_TO_SHOW_IN_SLIDER;
    fandubLinkToEnableIndex = 0;
  } else if (SliderArrowDirection.RIGHT === direction) {
    fandubLinkToDisableIndex = shiftedFandubLinks.length - 1;
    fandubLinkToEnableIndex = LINKS_TO_SHOW_IN_SLIDER - 1;
  }
  $(shiftedFandubLinks[fandubLinkToDisableIndex]).toggleClass('is_disabled');
  $(shiftedFandubLinks[fandubLinkToEnableIndex]).toggleClass('is_disabled');
}

function buildLinkHolder() {
  return $('<div class="anime-title__link_holder"></div>');
}

function appendLinks(titleDto, linkHolder, fandubList) {
  let i;
  let linksAmount = 0;
  for (i in fandubList) {
    let fandub = fandubList[i];
    let fanDubUrl = titleDto.fandubToUrl[fandub];
    if (fanDubUrl !== undefined) {
      linksAmount++;
      let targetClass = 'anime-title__link_holder__link anime-title__link_holder__link--'
          + fandub;
      if (linksAmount > LINKS_TO_SHOW_IN_SLIDER) {
        targetClass += ' is_disabled';
      }
      let link = buildLink(targetClass, fanDubUrl);
      link.attr('fandub', fandub);
      registerLinkEvents(link, fandub, titleDto);
      linkHolder.append(link);
    }
  }
  return linksAmount;
}

function buildLink(targetClass, href) {
  return $('<a target="_blank"></a>')
  .attr('class', targetClass)
  .attr('href', href);
}

function registerLinkEvents(link, fandub, titleDto) {
  let id = buildFandubEpisodeOverlayId(titleDto, fandub);
  link.mouseover(function () {
    changeFandubOverlayClass(fandub, id);
  });
  link.mouseout(function () {
    changeFandubOverlayClass(fandub, id);
  });
}

function changeFandubOverlayClass(fandub, id) {
  let fandubEpisodeOverlay = $(
      '[fandub="' + fandub + '"].anime-title__fandub_episode_overlay#' + id);
  fandubEpisodeOverlay.toggleClass('is_disabled');
}