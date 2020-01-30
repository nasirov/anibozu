<!DOCTYPE html>
<html lang="en">
<head>
  <title>Result for ${username}</title>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/style.css')}"/>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/ps2p.css')}"/>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/nes.min.css')}"/>
  <link rel="shortcut icon" type="image/x-icon" href="${versionedUrls.getForLookupPath('/img/favicon.ico')}"/>
  <meta charset="UTF-8">
</head>
<body>

<#if newEpisodeAvailable?has_content || newEpisodeNotAvailable?has_content || notFoundAnimeOnAnimedia?has_content>
  <header>
    <h1>Result for ${username}</h1>
  </header>
</#if>

<#if newEpisodeAvailable?has_content>
  <section class="nes-container with-title is-centered">
    <p class="title">Available</p>
      <#list newEpisodeAvailable as available>
        <div class="item">
          <img src="${available.posterUrlOnMAL}" alt="${available.titleNameOnMAL}"
               title="${available.titleNameOnMAL} episode ${available.episodeNumberForWatchForFront}">
          <div class="episode_overlay">
            <span>${available.episodeNumberForWatchForFront}</span>
            <span>episode</span>
          </div>
          <div class="overlay full_cover">
            <div class="link_holder">
              <a class="outbound_link animedia_background" href="${available.finalUrlForFront}" target="_blank"></a>
            </div>
          </div>
        </div>
      </#list>
  </section>
</#if>

<#if newEpisodeNotAvailable?has_content>
  <section class="nes-container with-title is-centered">
    <p class="title">Not Available</p>
      <#list newEpisodeNotAvailable as notAvailable>
        <div class="item">
          <img src="${notAvailable.posterUrlOnMAL}" alt="${notAvailable.titleNameOnMAL}" title="${notAvailable.titleNameOnMAL}">
        </div>
      </#list>
  </section>
</#if>

<#if notFoundAnimeOnAnimedia?has_content>
  <section class="nes-container with-title is-centered">
    <p class="title">Not Found</p>
      <#list notFoundAnimeOnAnimedia as notFound>
        <div class="item">
          <img src="${notFound.posterUrl}" alt="${notFound.title}" title="${notFound.title}"/>
          <div class="overlay full_cover">
            <a class="full_cover" href="${notFound.animeUrl}" target="_blank"></a>
          </div>
        </div>
      </#list>
  </section>
</#if>

</body>
</html>