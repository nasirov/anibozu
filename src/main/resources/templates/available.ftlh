<#macro availableSection resultAnimeList>
    <#assign available>
        <#list resultAnimeList as anime>
            <#if anime.isAvailable("${animedia}") || anime.isAvailable("${nineAnime}")>
              <div class="item">
                <img src="${anime.posterUrlOnMAL}" alt="${anime.animeName}" title="${anime.animeName} episode ${anime.episode}">
                <div class="episode_overlay">
                  <span>${anime.episode}</span>
                  <span>episode</span>
                </div>
                <div class="overlay full_cover">
                  <div class="link_holder">
                      <#if anime.isAvailable("${animedia}")>
                        <a class="outbound_link animedia_background" href="${anime.getUrlByFunDubSourceName("${animedia}")}" target="_blank"></a>
                      </#if>
                      <#if anime.isAvailable("${nineAnime}")>
                        <a class="outbound_link nineAnime_background" href="${anime.getUrlByFunDubSourceName("${nineAnime}")}" target="_blank"></a>
                      </#if>
                  </div>
                </div>
              </div>
            </#if>
        </#list>
    </#assign>
    <#if available?has_content>
      <section class="nes-container with-title is-centered">
        <p class="title">Available</p>
          ${available}
      </section>
    </#if>
</#macro>