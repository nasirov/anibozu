<#macro notFoundSection resultAnimeList>
    <#assign notFound>
        <#list resultAnimeList as anime>
            <#if anime.isNotFound("${animedia}") && anime.isNotFound("${nineAnime}")>
              <div class="item">
                <img src="${anime.posterUrlOnMAL}" alt="${anime.animeName}" title="${anime.animeName}"/>
                <div class="overlay full_cover">
                  <a class="full_cover" href="${anime.animeUrlOnMAL}" target="_blank"></a>
                </div>
              </div>
            </#if>
        </#list>
    </#assign>
    <#if notFound?has_content>
      <section class="nes-container with-title is-centered">
        <p class="title">Not Found</p>
          ${notFound}
      </section>
    </#if>
</#macro>