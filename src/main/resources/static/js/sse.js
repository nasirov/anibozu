/**
 * Created by nasirov.yv
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
  var img = buildImg(anime.posterUrlOnMal, anime.animeName,
      anime.animeName + ' episode ' + anime.episode);
  var episodeOverlay = buildEpisodeOverlay(anime.episode);
  var overlayFullCover = buildOverlayFullCover();
  var linkHolder = buildLinkHolder();
  appendLinks(anime, linkHolder, fandubList.split(","));
  overlayFullCover.append(linkHolder);
  item.append(img, episodeOverlay, overlayFullCover);
  var availableSection = $('#available-section');
  availableSection.append(item);
}

function buildNotAvailable(anime) {
  var item = buildItem();
  var img = buildImg(anime.posterUrlOnMal, anime.animeName, anime.animeName);
  var episodeOverlay = buildEpisodeOverlay(anime.episode);
  var overlayFullCover = buildOverlayFullCover();
  var link = buildLink('full_cover', anime.animeUrlOnMal);
  overlayFullCover.append(link);
  item.append(img, episodeOverlay, overlayFullCover);
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

function buildEpisodeOverlay(targetEpisode) {
  var episodeOverlay = $('<div class="episode_overlay"></div>');
  var episodeSpan = $('<span></span>').text(targetEpisode);
  episodeOverlay.append(episodeSpan, '<span>episode</span>');
  return episodeOverlay;
}

function buildOverlayFullCover() {
  return $('<div class="overlay full_cover"></div>');
}

function buildLinkHolder() {
  return $('<div class="link_holder"></div>');
}

function appendLinks(anime, linkHolder, fandubList) {
  var i;
  for (i in fandubList) {
    var fandub = fandubList[i];
    var fanDubUrl = anime.fanDubUrls[fandub];
    if (fanDubUrl.startsWith('http')) {
      var link = buildLink('outbound_link ' + fandub + '_background',
          fanDubUrl);
      linkHolder.append(link);
    }
  }
}

function buildLink(targetClass, href) {
  return $('<a target="_blank"></a>')
  .attr('class', targetClass)
  .attr('href', href);
}