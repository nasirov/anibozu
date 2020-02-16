<#macro isNotAvailableMacro fandubList anime>
    <#list fandubList as fandub>
        <#if anime.isNotAvailable("${fandub}")>
            ${fandub}
        </#if>
    </#list>
</#macro>
<#macro notAvailableSection resultAnimeList fandubList>
    <#assign notAvailable>
        <#list resultAnimeList as anime>
            <#outputformat "plainText">
                <#assign isNotAvailable>
                    <@isNotAvailableMacro fandubList anime/>
                </#assign>
            </#outputformat>
            <#if isNotAvailable?has_content && fandubList?size == isNotAvailable?word_list?size>
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