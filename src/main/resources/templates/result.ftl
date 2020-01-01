<!DOCTYPE html>
<html lang="en">
<head>
  <title>Result for ${username}</title>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/style.css')}"/>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/ps2p.css')}"/>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/nes.min.css')}"/>
  <link rel="shortcut icon" type="image/x-icon" href="${versionedUrls.getForLookupPath('/img/favicon.ico')}"/>
</head>
<body>

<#if newEpisodeAvailable?has_content || newEpisodeNotAvailable?has_content || notFoundAnimeOnAnimedia?has_content>
<header>
  <h1>Result for ${username}</h1>
</header>
</#if>

<#if newEpisodeAvailable?has_content>
<section class="nes-container with-title is-centered">
  <p class="title">New Episode Available</p>
  <ul>
    <#list newEpisodeAvailable as available>
      <a href="${available.finalUrlForFront}" target="_blank"><img src="${available.posterUrlOnMAL}" height="318" width="225"
                                                                   alt="${available.titleNameOnMAL}"
                                                                   title="${available.titleNameOnMAL} episode ${available.episodeNumberForWatchForFront}"
                                                                   class="fade"/></a>
    </#list>
  </ul>
</section>
</#if>

<#if newEpisodeNotAvailable?has_content>
<section class="nes-container with-title is-centered">
  <p class="title">New Episode Not Available</p>
  <ul>
    <#list newEpisodeNotAvailable as notAvailable>
      <img src="${notAvailable.posterUrlOnMAL}" height="318" width="225" alt="${notAvailable.titleNameOnMAL}"
           title="${notAvailable.titleNameOnMAL}" class="fade"/>
    </#list>
  </ul>
</section>
</#if>

<#if notFoundAnimeOnAnimedia?has_content>
<section class="nes-container with-title is-centered">
  <p class="title">Not Found on Animedia</p>
  <ul>
      <#list notFoundAnimeOnAnimedia as notFound>
      <a href="${notFound.animeUrl}" target="_blank"><img src="${notFound.posterUrl}" height="318" width="225"
                                                          alt="${notFound.title}"
                                                          title="${notFound.title}" class="fade"/></a>
    </#list>
  </ul>
</section>
</#if>

</body>
</html>