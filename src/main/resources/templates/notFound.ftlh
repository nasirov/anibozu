<#macro isNotFoundMacro fandubList anime>
    <#list fandubList as fandub>
        <#if anime.isNotFound("${fandub}")>
            ${fandub}
        </#if>
    </#list>
</#macro>
<#macro notFoundSection resultAnimeList fandubList>
    <#assign notFound>
        <#list resultAnimeList as anime>
            <#outputformat "plainText">
                <#assign isNotFound>
                    <@isNotFoundMacro fandubList anime/>
                </#assign>
            </#outputformat>
            <#if isNotFound?has_content && fandubList?size == isNotFound?word_list?size>
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