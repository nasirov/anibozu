/**
 * @author Nasirov Yuriy
 */
$(document).ready(function () {
  var username = $('#username').val();
  var fandubList = $('#fandubList').val();
  var watchingTitlesSize = $('#watchingTitlesSize').val();
  var eventSource = new EventSource(
      '/sse?username=' + username + '&fanDubSources=' + fandubList);
  eventSource.onmessage = function (messageFromServer) {
    var msg = JSON.parse(messageFromServer.data);
    var handledCount = messageFromServer.lastEventId;
    var header = $('header');
    var availableSection = $('#available-section');
    var notAvailableSection = $('#not-available-section');
    var notFoundSection = $('#not-found-section');
    switch (msg.eventType) {
      case 'AVAILABLE':
        if (isSectionDoNotExist(availableSection)) {
          addAfter(header, buildSection('available-section', 'Available'));
        }
        buildAvailable(msg.anime, fandubList);
        updateStatus(username, handledCount, watchingTitlesSize);
        break;
      case 'NOT_AVAILABLE':
        if (isSectionDoNotExist(notAvailableSection)) {
          var newNotAvailableSection = buildSection('not-available-section',
              'Not'
              + ' Available');
          if (isSectionExists(availableSection)) {
            addAfter(availableSection, newNotAvailableSection);
          } else {
            addAfter(header, newNotAvailableSection);
          }
        }
        buildNotAvailable(msg.anime);
        updateStatus(username, handledCount, watchingTitlesSize);
        break;
      case 'NOT_FOUND':
        if (isSectionDoNotExist(notFoundSection)) {
          var newNotFoundSection = buildSection('not-found-section', 'Not'
              + ' Found');
          if (isSectionExists(notAvailableSection)) {
            addAfter(notAvailableSection, newNotFoundSection);
          } else if (isSectionExists(availableSection)) {
            addAfter(availableSection, newNotFoundSection);
          } else {
            addAfter(header, newNotFoundSection);
          }
        }
        buildNotFound(msg.anime);
        updateStatus(username, handledCount, watchingTitlesSize);
        break;
      case 'DONE':
        setStatusToCompleted('Successfully completed result for ' + username);
        eventSource.close();
        break;
    }
  };

  eventSource.onerror = function () {
    setStatusToCompleted('Completed result with errors for ' + username);
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
  var item = buildItem();
  var animeName = anime.animeName;
  var malEpisodeNumber = anime.malEpisodeNumber;
  var img = buildImg(anime.posterUrlOnMal, animeName,
      animeName + ' episode ' + malEpisodeNumber);
  var malEpisodeOverlay = buildMalEpisodeOverlay(malEpisodeNumber);
  var overlayFullCover = buildOverlayFullCover();
  var linkHolder = buildLinkHolder();
  var fandubListArray = fandubList.split(",");
  var linksAmount = appendLinks(anime, linkHolder, fandubListArray);
  var fandubSlider = buildFandubSlider(anime);
  if (linksAmount > 3) {
    var leftSliderArrow = buildLeftSliderArrow();
    var rightSliderArrow = buildRightSliderArrow();
    fandubSlider.append(leftSliderArrow, linkHolder, rightSliderArrow);
    overlayFullCover.append(fandubSlider);
  } else {
    fandubSlider.append(linkHolder);
    overlayFullCover.append(fandubSlider);
  }
  item.append(img, malEpisodeOverlay, overlayFullCover);
  appendFandubEpisodes(anime, item, fandubListArray);
  var availableSection = $('#available-section');
  availableSection.append(item);
}

function buildNotAvailable(anime) {
  var item = buildItem();
  var img = buildImg(anime.posterUrlOnMal, anime.animeName, anime.animeName);
  var malEpisodeOverlay = buildMalEpisodeOverlay(anime.malEpisodeNumber);
  var overlayFullCover = buildOverlayFullCover();
  var link = buildLink('full_cover', anime.animeUrlOnMal);
  overlayFullCover.append(link);
  item.append(img, malEpisodeOverlay, overlayFullCover);
  var notAvailableSection = $('#not-available-section');
  notAvailableSection.append(item);
}

function buildNotFound(anime) {
  var item = buildItem();
  var img = buildImg(anime.posterUrlOnMal, anime.animeName, anime.animeName);
  var overlayFullCover = buildOverlayFullCover();
  var link = buildLink('full_cover', anime.animeUrlOnMal);
  overlayFullCover.append(link);
  item.append(img, overlayFullCover);
  var notFoundSection = $('#not-found-section');
  notFoundSection.append(item);
}

function setStatusToCompleted(message) {
  setStatusText(message);
}

function updateStatus(username, handledCount, watchingTitlesCount) {
  handledCount++;
  setStatusText('Processing result for ' + username + ' ...'
      + ' Handled ' + handledCount + ' from ' + watchingTitlesCount);
}

function setStatusText(message) {
  $('#status').text(message);
}

function buildSection(id, text) {
  var section = $(
      '<section class="nes-container with-title is-centered"></section>')
  .attr('id', id);
  var title = $('<p class="title"></p>').text(text);
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
  var episodeOverlay = $('<div class="mal_episode_overlay"></div>');
  var episodeSpan = $('<span></span>').text(targetEpisode);
  episodeOverlay.append(episodeSpan, '<span>episode</span>');
  return episodeOverlay;
}

function appendFandubEpisodes(anime, item, fandubList) {
  var i;
  for (i in fandubList) {
    var fandub = fandubList[i];
    var fanDubUrl = anime.fanDubUrls[fandub];
    var fanDubEpisodeName = anime.fanDubEpisodeNames[fandub];
    if (fanDubUrl.startsWith('http')) {
      var id = buildFandubEpisodeOverlayId(anime, fandub);
      var fandubEpisodeOverlay = buildFandubEpisodeOverlay(fanDubEpisodeName,
          fandub, id);
      item.append(fandubEpisodeOverlay);
    }
  }
}

function buildFandubEpisodeOverlayId(anime, fandub) {
  return anime.animeName.replace(/[^a-zA-Z0-9_-]/g, '_') + '_' + fandub;
}

function buildFandubSliderId(anime) {
  return anime.animeName.replace(/[^a-zA-Z0-9_-]/g, '_') + '_fandub_slider';
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
  var container = $('<div class="slider_arrow is_left_arrow"></div>');
  var img = $('<img src="/img/left-arrow.png" alt="arrow left">');
  container.append(img);
  container.mousedown(function () {
    var fandubLinks = this.parentNode.getElementsByClassName(
        'link_holder')[0].children;
    var fandubLinkToEnable;
    var fandubLinkToDisable;
    for (var i = fandubLinks.length - 1; i >= 0; i--) {
      var currentFandubLink = $(fandubLinks[i]);
      var currentFandubLinkDisabled = currentFandubLink.hasClass('is_disabled');
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
  var container = $('<div class="slider_arrow is_right_arrow"></div>');
  var img = $('<img src="/img/right-arrow.png" alt="arrow right">');
  container.append(img);
  container.mousedown(function () {
    var fandubLinks = this.parentNode.getElementsByClassName(
        'link_holder')[0].children;
    var fandubLinkToDisable;
    var fandubLinkToEnable;
    for (var i = 0; i < fandubLinks.length; i++) {
      var currentFandubLink = $(fandubLinks[i]);
      var currentFandubLinkDisabled = currentFandubLink.hasClass('is_disabled');
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
  var i;
  var linksAmount = 0;
  for (i in fandubList) {
    var fandub = fandubList[i];
    var fanDubUrl = anime.fanDubUrls[fandub];
    if (fanDubUrl.startsWith('http')) {
      linksAmount++;
      var targetClass = 'outbound_link ' + fandub + '_background';
      if (linksAmount > 3) {
        targetClass += ' is_disabled';
      }
      var link = buildLink(targetClass, fanDubUrl);
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
  var id = buildFandubEpisodeOverlayId(anime, fandub);
  outboundLink.mouseover(function () {
    changeFandubOverlayClass(fandub, id)
  });
  outboundLink.mouseout(function () {
    changeFandubOverlayClass(fandub, id)
  });
}

function changeFandubOverlayClass(fandub, id) {
  var fandubEpisodeOverlay = $(
      '[fandub="' + fandub + '"].fandub_episode_overlay#' + id);
  fandubEpisodeOverlay.toggleClass('is_disabled');
}