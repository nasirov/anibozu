<#macro isNotFoundMacro fundubList anime>
    <#list fundubList as fundub>
        <#if anime.isNotFound("${fundub}")>
            ${fundub}
        </#if>
    </#list>
</#macro>
<#macro notFoundSection resultAnimeList fundubList>
    <#assign notFound>
        <#list resultAnimeList as anime>
            <#outputformat "plainText">
                <#assign isNotFound>
                    <@isNotFoundMacro fundubList anime/>
                </#assign>
            </#outputformat>
            <#if isNotFound?has_content && fundubList?size == isNotFound?word_list?size>
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