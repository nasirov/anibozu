/**
 * @author Nasirov Yuriy
 */
$(document).ready(function () {
  let username = $('#username').val();
  let fandubList = $('#fandubList').val();
  let availableTitles = JSON.parse($('#availableTitles').val());
  let notAvailableTitles = JSON.parse($('#notAvailableTitles').val());
  let notFoundTitles = JSON.parse($('#notFoundTitles').val());
  let header = $('header');
  if (availableTitles.length > 0) {
    addAfter(header, buildSection('available-section', 'Available'));
    let i;
    let fandubListArray = fandubList.split(',');
    for (i in availableTitles) {
      buildAvailable(availableTitles[i], fandubListArray);
    }
  }
  if (notAvailableTitles.length > 0) {
    let availableSection = getAvailableSection();
    let newNotAvailableSection = buildSection('not-available-section',
        'Not Available');
    if (sectionExists(availableSection)) {
      addAfter(availableSection, newNotAvailableSection);
    } else {
      addAfter(header, newNotAvailableSection);
    }
    let i;
    for (i in notAvailableTitles) {
      buildNotAvailable(notAvailableTitles[i]);
    }
  }
  if (notFoundTitles.length > 0) {
    let newNotFoundSection = buildSection('not-found-section', 'Not'
        + ' Found');
    let availableSection = getAvailableSection();
    let notAvailableSection = getNotAvailableSection();
    if (sectionExists(notAvailableSection)) {
      addAfter(notAvailableSection, newNotFoundSection);
    } else if (sectionExists(availableSection)) {
      addAfter(availableSection, newNotFoundSection);
    } else {
      addAfter(header, newNotFoundSection);
    }
    let i;
    for (i in notFoundTitles) {
      buildNotFound(notFoundTitles[i]);
    }
  }
  if (sectionDoesNotExist(getAvailableSection()) && sectionDoesNotExist(
      getNotAvailableSection()) && sectionDoesNotExist(getNotFoundSection())) {
    setStatus('Sorry, something went wrong. Please try again later.');
  }
});

function getAvailableSection() {
  return $('#available-section');
}

function getNotAvailableSection() {
  return $('#not-available-section');
}

function getNotFoundSection() {
  return $('#not-found-section');
}

function sectionExists(section) {
  return section[0] !== undefined;
}

function sectionDoesNotExist(section) {
  return section[0] === undefined;
}

function addAfter(topElement, afterElement) {
  topElement.after(afterElement);
}

function buildAvailable(titleDto, fandubList) {
  let item = buildItem();
  let nameOnMal = titleDto.nameOnMal;
  let episodeNumberOnMal = titleDto.episodeNumberOnMal;
  let img = buildImg(titleDto.posterUrlOnMal, nameOnMal,
      nameOnMal + ' episode ' + episodeNumberOnMal);
  let malEpisodeOverlay = buildMalEpisodeOverlay(episodeNumberOnMal);
  let overlayFullCover = buildOverlayFullCover();
  let linkHolder = buildLinkHolder();
  let linksAmount = appendLinks(titleDto, linkHolder, fandubList);
  let fandubSlider = buildFandubSlider(titleDto);
  if (linksAmount > 3) {
    let leftSliderArrow = buildLeftSliderArrow();
    let rightSliderArrow = buildRightSliderArrow();
    fandubSlider.append(leftSliderArrow, linkHolder, rightSliderArrow);
    overlayFullCover.append(fandubSlider);
  } else {
    fandubSlider.append(linkHolder);
    overlayFullCover.append(fandubSlider);
  }
  item.append(img, malEpisodeOverlay, overlayFullCover);
  appendFandubEpisodes(titleDto, item, fandubList);
  let availableSection = getAvailableSection();
  availableSection.append(item);
}

function buildNotAvailable(titleDto) {
  let item = buildItem();
  let img = buildImg(titleDto.posterUrlOnMal, titleDto.nameOnMal,
      titleDto.nameOnMal);
  let malEpisodeOverlay = buildMalEpisodeOverlay(titleDto.episodeNumberOnMal);
  let overlayFullCover = buildOverlayFullCover();
  let link = buildLink('full_cover', titleDto.animeUrlOnMal);
  overlayFullCover.append(link);
  item.append(img, malEpisodeOverlay, overlayFullCover);
  let notAvailableSection = getNotAvailableSection();
  notAvailableSection.append(item);
}

function buildNotFound(titleDto) {
  let item = buildItem();
  let img = buildImg(titleDto.posterUrlOnMal, titleDto.nameOnMal,
      titleDto.nameOnMal);
  let overlayFullCover = buildOverlayFullCover();
  let link = buildLink('full_cover', titleDto.animeUrlOnMal);
  overlayFullCover.append(link);
  item.append(img, overlayFullCover);
  let notFoundSection = getNotFoundSection();
  notFoundSection.append(item);
}

function setStatus(message) {
  $('#status').text(message);
}

function buildSection(id, text) {
  let section = $(
      '<section class="nes-container with-title is-centered"></section>')
  .attr('id', id);
  let title = $('<p class="title"></p>').text(text);
  section.append(title);
  return section;
}

function buildItem() {
  return $('<div class="item"></div>');
}

function buildImg(src, alt, title) {
  return $('<img class="width_full height_full">')
  .attr('src', src)
  .attr('alt', alt)
  .attr('title', title);
}

function buildMalEpisodeOverlay(targetEpisode) {
  let episodeOverlay = $('<div class="mal_episode_overlay"></div>');
  let episodeSpan = $('<span></span>').text(targetEpisode);
  episodeOverlay.append(episodeSpan, '<span>episode</span>');
  return episodeOverlay;
}

function appendFandubEpisodes(anime, item, fandubList) {
  let i;
  for (i in fandubList) {
    let fandub = fandubList[i];
    let fanDubUrl = anime.fandubToUrl[fandub];
    let fanDubEpisodeName = anime.fandubToEpisodeName[fandub];
    if (fanDubUrl.startsWith('http')) {
      let id = buildFandubEpisodeOverlayId(anime, fandub);
      let fandubEpisodeOverlay = buildFandubEpisodeOverlay(fanDubEpisodeName,
          fandub, id);
      item.append(fandubEpisodeOverlay);
    }
  }
}

function buildFandubEpisodeOverlayId(anime, fandub) {
  return anime.nameOnMal.replace(/[^a-zA-Z\d_-]/g, '_') + '_' + fandub;
}

function buildFandubSliderId(anime) {
  return anime.nameOnMal.replace(/[^a-zA-Z\d_-]/g, '_') + '_fandub_slider';
}

function buildFandubEpisodeOverlay(targetEpisode, fandub, id) {
  return $('<div></div>').attr('class',
      'fandub_episode_overlay is_disabled').attr(
      'fandub', fandub).attr('id', id).text(targetEpisode);
}

function buildOverlayFullCover() {
  return $('<div class="overlay full_cover"></div>');
}

function buildFandubSlider(anime) {
  return $('<div class="fandub_slider"></div>').attr('id',
      buildFandubSliderId(anime));
}

const SliderArrowDirection = {LEFT: 'left', RIGHT: 'right'};

function buildLeftSliderArrow() {
  return buildSliderArrow(SliderArrowDirection.LEFT);
}

function buildRightSliderArrow() {
  return buildSliderArrow(SliderArrowDirection.RIGHT);
}

function buildSliderArrow(direction) {
  let container = $(
      '<div id="arrow ' + direction + '" class="slider_arrow is_' + direction
      + '_arrow"></div>');
  container.mousedown(function () {
    let fandubLinks = this.parentNode.getElementsByClassName(
        'link_holder')[0].children;
    let fandubLinkToDisable;
    let fandubLinkToEnable;
    for (let i = getLoopCounter(direction, fandubLinks);
        getLoopCondition(direction, i, fandubLinks);
        i = modifyLoopCounter(direction, i)) {
      let currentFandubLink = $(fandubLinks[i]);
      let currentFandubLinkDisabled = currentFandubLink.hasClass('is_disabled');
      if (fandubLinkToDisable === undefined && !currentFandubLinkDisabled) {
        fandubLinkToDisable = currentFandubLink;
      } else if (fandubLinkToDisable !== undefined
          && currentFandubLinkDisabled) {
        fandubLinkToEnable = currentFandubLink;
        fandubLinkToDisable.toggleClass('is_disabled');
        fandubLinkToEnable.toggleClass('is_disabled');
        break;
      }
    }
  });
  return container;
}

function getLoopCounter(direction, fandubLinks) {
  let result;
  switch (direction) {
    case SliderArrowDirection.LEFT:
      result = fandubLinks.length - 1;
      break;
    case SliderArrowDirection.RIGHT:
      result = 0;
      break;
  }
  return result;
}

function getLoopCondition(direction, counter, fandubLinks) {
  let result;
  switch (direction) {
    case SliderArrowDirection.LEFT:
      result = counter >= 0;
      break;
    case SliderArrowDirection.RIGHT:
      result = counter < fandubLinks.length;
      break;
  }
  return result;
}

function modifyLoopCounter(direction, counter) {
  let result;
  switch (direction) {
    case SliderArrowDirection.LEFT:
      result = counter - 1;
      break;
    case SliderArrowDirection.RIGHT:
      result = counter + 1;
      break;
  }
  return result;
}

function buildLinkHolder() {
  return $('<div class="link_holder"></div>');
}

function appendLinks(anime, linkHolder, fandubList) {
  let i;
  let linksAmount = 0;
  for (i in fandubList) {
    let fandub = fandubList[i];
    let fanDubUrl = anime.fandubToUrl[fandub];
    if (fanDubUrl.startsWith('http')) {
      linksAmount++;
      let targetClass = 'outbound_link ' + fandub + '_background';
      if (linksAmount > 3) {
        targetClass += ' is_disabled';
      }
      let link = buildLink(targetClass, fanDubUrl);
      link.attr('fandub', fandub);
      registerOutboundLinkEvents(link, fandub, anime);
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

function registerOutboundLinkEvents(outboundLink, fandub, anime) {
  let id = buildFandubEpisodeOverlayId(anime, fandub);
  outboundLink.mouseover(function () {
    changeFandubOverlayClass(fandub, id);
  });
  outboundLink.mouseout(function () {
    changeFandubOverlayClass(fandub, id);
  });
}

function changeFandubOverlayClass(fandub, id) {
  let fandubEpisodeOverlay = $(
      '[fandub="' + fandub + '"].fandub_episode_overlay#' + id);
  fandubEpisodeOverlay.toggleClass('is_disabled');
}