<#macro isNotAvailableMacro fundubList anime>
    <#list fundubList as fundub>
        <#if anime.isNotAvailable("${fundub}")>
            ${fundub}
        </#if>
    </#list>
</#macro>
<#macro notAvailableSection resultAnimeList fundubList>
    <#assign notAvailable>
        <#list resultAnimeList as anime>
            <#outputformat "plainText">
                <#assign isNotAvailable>
                    <@isNotAvailableMacro fundubList anime/>
                </#assign>
            </#outputformat>
            <#if isNotAvailable?has_content && fundubList?size == isNotAvailable?word_list?size>
              <div class="item">
                <img src="${anime.posterUrlOnMAL}" alt="${anime.animeName}" title="${anime.animeName}">
                <div class="episode_overlay">
                  <span>${anime.episode}</span>
                  <span>episode</span>
                </div>
                <div class="overlay full_cover">
                  <a class="full_cover" href="${anime.animeUrlOnMAL}" target="_blank"></a>
                </div>
              </div>
            </#if>
        </#list>
    </#assign>
    <#if notAvailable?has_content>
      <section class="nes-container with-title is-centered">
        <p class="title">Not Available</p>
          ${notAvailable}
      </section>
    </#if>
</#macro>