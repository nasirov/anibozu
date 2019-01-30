<#import "/spring.ftl" as spring/>

<head>
<#--<title>${title}</title>-->
    <title>Anime Checker</title>
    <link rel="stylesheet" type="text/css" href="<@spring.url '/css/style.css'/>"/>
    <link rel="shortcut icon" href="<@spring.url '/img/favicon.ico'/>" type="image/x-icon">
</head>
<body>

<#if newEpisodeAvailable?has_content>
<h1>New Episode Available:</h1>
<ul>
    <#list newEpisodeAvailable as available>
        <a href="${available.finalUrl}"><img src="${available.posterUrl}" height="318" width="225"
                                             alt="${available.titleOnMAL}"
                                             title="${available.titleOnMAL} episode ${available.numberOfEpisodeForWatch}"
                                             class="fade"/></a>
    </#list>
</ul>
</#if>

<#if newEpisodeNotAvailable?has_content>
<h1>New Episode Not Available:</h1>
<ul>
    <#list newEpisodeNotAvailable as notAvailable>
        <img src="${notAvailable.posterUrl}" height="318" width="225" alt="${notAvailable.titleOnMAL}"
             title="${notAvailable.titleOnMAL}" class="fade"/>
    </#list>
</ul>
</#if>

<#if matchedNotFoundAnimeOnAnimedia?has_content>
<h1>Not Found on Animedia:</h1>
<ul>
    <#list matchedNotFoundAnimeOnAnimedia as notFound>
        <a href="${notFound.animeUrl}"><img src="${notFound.posterUrl}" height="318" width="225" alt="${notFound.title}"
                                            title="${notFound.title}" class="fade"/></a>
    </#list>
</ul>
</#if>

<#if errorMsg??>
<h1>${errorMsg}</h1>
</#if>

</body>
</html>