<#import "/spring.ftl" as spring/>

<head>
    <title>Result for ${username}</title>
    <link rel="stylesheet" type="text/css" href="<@spring.url '/css/style.css'/>"/>
    <link rel="stylesheet" type="text/css" href="<@spring.url '/css/ps2p.css'/>"/>
    <link rel="stylesheet" type="text/css" href="<@spring.url '/css/nes.min.css'/>"/>
    <link rel="shortcut icon" type="image/x-icon" href="<@spring.url '/img/favicon.ico'/>"/>
</head>
<body>

<#if newEpisodeAvailable?has_content || newEpisodeNotAvailable?has_content || matchedNotFoundAnimeOnAnimedia?has_content>
<header>
    <h1>Result for ${username}</h1>
</header>
</#if>

<#if newEpisodeAvailable?has_content>
<section class="nes-container with-title is-centered">
    <p class="title">New Episode Available</p>
    <ul>
        <#list newEpisodeAvailable as available>
            <a href="${available.finalUrl}" target="_blank"><img src="${available.posterUrl}" height="318" width="225"
                                                                 alt="${available.titleOnMAL}"
                                                                 title="${available.titleOnMAL} episode ${available.episodeNumberForWatch}"
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
            <img src="${notAvailable.posterUrl}" height="318" width="225" alt="${notAvailable.titleOnMAL}"
                 title="${notAvailable.titleOnMAL}" class="fade"/>
        </#list>
    </ul>
</section>
</#if>

<#if matchedNotFoundAnimeOnAnimedia?has_content>
<section class="nes-container with-title is-centered">
    <p class="title">Not Found on Animedia</p>
    <ul>
        <#list matchedNotFoundAnimeOnAnimedia as notFound>
            <a href="${notFound.animeUrl}" target="_blank"><img src="${notFound.posterUrl}" height="318" width="225"
                                                                alt="${notFound.title}"
                                                                title="${notFound.title}" class="fade"/></a>
        </#list>
    </ul>
</section>
</#if>

<#if errorMsg??>
<h2>${errorMsg}</h2>
</#if>

</body>