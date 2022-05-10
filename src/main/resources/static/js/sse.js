/**
 * @author Nasirov Yuriy
 */
$(document).ready(function () {
  let username = $('#username').val();
  let fandubList = $('#fandubList').val();
  let watchingTitlesSize = $('#watchingTitlesSize').val();
  let eventSource = new EventSource(
      '/sse?username=' + username + '&fanDubSources=' + fandubList);
  eventSource.onmessage = function (messageFromServer) {
    let sseDto = JSON.parse(messageFromServer.data);
    let handledCount = messageFromServer.lastEventId;
    let header = $('header');
    let availableSection = $('#available-section');
    let notAvailableSection = $('#not-available-section');
    let notFoundSection = $('#not-found-section');
    switch (sseDto.eventType) {
      case 'AVAILABLE':
        if (isSectionDoNotExist(availableSection)) {
          addAfter(header, buildSection('available-section', 'Available'));
        }
        buildAvailable(sseDto.anime, fandubList);
        updateProcessingStatus(username, handledCount, watchingTitlesSize);
        break;
      case 'NOT_AVAILABLE':
        if (isSectionDoNotExist(notAvailableSection)) {
          let newNotAvailableSection = buildSection('not-available-section',
              'Not'
              + ' Available');
          if (isSectionExists(availableSection)) {
            addAfter(availableSection, newNotAvailableSection);
          } else {
            addAfter(header, newNotAvailableSection);
          }
        }
        buildNotAvailable(sseDto.anime);
        updateProcessingStatus(username, handledCount, watchingTitlesSize);
        break;
      case 'NOT_FOUND':
        if (isSectionDoNotExist(notFoundSection)) {
          let newNotFoundSection = buildSection('not-found-section', 'Not'
              + ' Found');
          if (isSectionExists(notAvailableSection)) {
            addAfter(notAvailableSection, newNotFoundSection);
          } else if (isSectionExists(availableSection)) {
            addAfter(availableSection, newNotFoundSection);
          } else {
            addAfter(header, newNotFoundSection);
          }
        }
        buildNotFound(sseDto.anime);
        updateProcessingStatus(username, handledCount, watchingTitlesSize);
        break;
      case 'ERROR':
        setStatus(sseDto.errorMessage);
        eventSource.close();
        break;
      case 'DONE':
        setStatus('Successfully completed result for ' + username);
        eventSource.close();
        break;
    }
  };

  eventSource.onerror = function () {
    setStatus('Completed result with errors for ' + username);
    eventSource.close();
  };

});

function isSectionExists(section) {
  return section[0] !== undefined;
}

function isSectionDoNotExist(section) {
  return section[0] === undefined;
}

function addAfter(topElement, afterElement) {
  topElement.after(afterElement);
}

function buildAvailable(anime, fandubList) {
  let item = buildItem();
  let animeName = anime.animeName;
  let malEpisodeNumber = anime.malEpisodeNumber;
  let img = buildImg(anime.posterUrlOnMal, animeName,
      animeName + ' episode ' + malEpisodeNumber);
  let malEpisodeOverlay = buildMalEpisodeOverlay(malEpisodeNumber);
  let overlayFullCover = buildOverlayFullCover();
  let linkHolder = buildLinkHolder();
  let fandubListArray = fandubList.split(",");
  let linksAmount = appendLinks(anime, linkHolder, fandubListArray);
  let fandubSlider = buildFandubSlider(anime);
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
  appendFandubEpisodes(anime, item, fandubListArray);
  let availableSection = $('#available-section');
  availableSection.append(item);
}

function buildNotAvailable(anime) {
  let item = buildItem();
  let img = buildImg(anime.posterUrlOnMal, anime.animeName, anime.animeName);
  let malEpisodeOverlay = buildMalEpisodeOverlay(anime.malEpisodeNumber);
  let overlayFullCover = buildOverlayFullCover();
  let link = buildLink('full_cover', anime.animeUrlOnMal);
  overlayFullCover.append(link);
  item.append(img, malEpisodeOverlay, overlayFullCover);
  let notAvailableSection = $('#not-available-section');
  notAvailableSection.append(item);
}

function buildNotFound(anime) {
  let item = buildItem();
  let img = buildImg(anime.posterUrlOnMal, anime.animeName, anime.animeName);
  let overlayFullCover = buildOverlayFullCover();
  let link = buildLink('full_cover', anime.animeUrlOnMal);
  overlayFullCover.append(link);
  item.append(img, overlayFullCover);
  let notFoundSection = $('#not-found-section');
  notFoundSection.append(item);
}

function updateProcessingStatus(username, handledCount, watchingTitlesCount) {
  handledCount++;
  setStatus('Processing result for ' + username + ' ...'
      + ' Handled ' + handledCount + ' from ' + watchingTitlesCount);
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
    let fanDubUrl = anime.fanDubUrls[fandub];
    let fanDubEpisodeName = anime.fanDubEpisodeNames[fandub];
    if (fanDubUrl.startsWith('http')) {
      let id = buildFandubEpisodeOverlayId(anime, fandub);
      let fandubEpisodeOverlay = buildFandubEpisodeOverlay(fanDubEpisodeName,
          fandub, id);
      item.append(fandubEpisodeOverlay);
    }
  }
}

function buildFandubEpisodeOverlayId(anime, fandub) {
  return anime.animeName.replace(/[^a-zA-Z\d_-]/g, '_') + '_' + fandub;
}

function buildFandubSliderId(anime) {
  return anime.animeName.replace(/[^a-zA-Z\d_-]/g, '_') + '_fandub_slider';
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

function buildLeftSliderArrow() {
  let container = $('<div class="slider_arrow is_left_arrow"></div>');
  let img = $('<img src="/img/left-arrow.png" alt="arrow left">');
  container.append(img);
  container.mousedown(function () {
    let fandubLinks = this.parentNode.getElementsByClassName(
        'link_holder')[0].children;
    let fandubLinkToDisable;
    let fandubLinkToEnable;
    for (let i = fandubLinks.length - 1; i >= 0; i--) {
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

function buildRightSliderArrow() {
  let container = $('<div class="slider_arrow is_right_arrow"></div>');
  let img = $('<img src="/img/right-arrow.png" alt="arrow right">');
  container.append(img);
  container.mousedown(function () {
    let fandubLinks = this.parentNode.getElementsByClassName(
        'link_holder')[0].children;
    let fandubLinkToDisable;
    let fandubLinkToEnable;
    for (let i = 0; i < fandubLinks.length; i++) {
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

function buildLinkHolder() {
  return $('<div class="link_holder"></div>');
}

function appendLinks(anime, linkHolder, fandubList) {
  let i;
  let linksAmount = 0;
  for (i in fandubList) {
    let fandub = fandubList[i];
    let fanDubUrl = anime.fanDubUrls[fandub];
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
    changeFandubOverlayClass(fandub, id)
  });
  outboundLink.mouseout(function () {
    changeFandubOverlayClass(fandub, id)
  });
}

function changeFandubOverlayClass(fandub, id) {
  let fandubEpisodeOverlay = $(
      '[fandub="' + fandub + '"].fandub_episode_overlay#' + id);
  fandubEpisodeOverlay.toggleClass('is_disabled');
}