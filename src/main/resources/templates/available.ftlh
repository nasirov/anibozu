<#macro isAvailableMacro fundubList anime>
    <#list fundubList as fundub>
        <#if anime.isAvailable("${fundub}")>
            ${fundub}
        </#if>
    </#list>
</#macro>
<#macro availableSection resultAnimeList fundubList>
    <#assign available>
        <#list resultAnimeList as anime>
            <#outputformat "plainText">
                <#assign isAvailable>
                    <@isAvailableMacro fundubList anime/>
                </#assign>
            </#outputformat>
            <#if isAvailable?has_content>
              <div class="item">
                <img src="${anime.posterUrlOnMAL}" alt="${anime.animeName}" title="${anime.animeName} episode ${anime.episode}">
                <div class="episode_overlay">
                  <span>${anime.episode}</span>
                  <span>episode</span>
                </div>
                <div class="overlay full_cover">
                  <div class="link_holder">
                      <#list isAvailable?word_list as fundub>
                        <a class="outbound_link ${fundub}_background" href="${anime.getUrlByFunDubSourceName("${fundub}")}" target="_blank"></a>
                      </#list>
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