<#import "/spring.ftl" as spring/>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Anime Checker</title>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/center-style.css')}"/>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/style.css')}"/>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/nes.min.css')}"/>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/ps2p.css')}"/>
  <link rel="shortcut icon" type="image/x-icon" href="${versionedUrls.getForLookupPath('/img/favicon.ico')}"/>
</head>

<body>
<div id="container">
  <div class="inner">
    <h1 class="is_centered">Anime Checker</h1>
    <section class="nes-container is-centered">
      <div class="nes-field">
        <form id="username-submit-form" action="/result" method="get">
            <@spring.bind "userInputDto"/>
            <@spring.formInput "userInputDto.username",'class="nes-input"
                 placeholder="Enter MAL username..."
                 title="Please enter a valid mal username between 2 and 16 characters(latin letters, numbers, underscores and dashes only)"
                 autofocus'/>
          <div class="nes-field">Choose At Least One FanDub Site</div>
          <table class="width_full font_x-small is_left">
            <tbody>
            <tr>
              <td>
                <label>
                    <@spring.bind "userInputDto.fanDubSources"/>
                  <input id="nineAnime-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" value="${nineAnime}"/>
                  <span>9Anime</span>
                </label>
              </td>
              <td>
                <label>
                    <@spring.bind "userInputDto.fanDubSources"/>
                  <input id="anidub-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" value="${anidub}"/>
                  <span>Anidub</span>
                </label>
              </td>
              <td>
                <label>
                    <@spring.bind "userInputDto.fanDubSources"/>
                  <input id="anilibria-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" value="${anilibria}"/>
                  <span>Anilibria</span>
                </label>
              </td>
            </tr>
            <tr>
              <td>
                <label>
                    <@spring.bind "userInputDto.fanDubSources"/>
                  <input id="animepik-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" value="${animepik}"/>
                  <span>AnimePiK</span>
                </label>
              </td>
              <td>
                <label>
                    <@spring.bind "userInputDto.fanDubSources"/>
                  <input id="animedia-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" value="${animedia}"/>
                  <span>Animedia</span>
                </label>
              </td>
              <td>
                <label>
                    <@spring.bind "userInputDto.fanDubSources"/>
                  <input id="jisedai-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" value="${jisedai}"/>
                  <span>Jisedai</span>
                </label>
              </td>
            </tr>
            <tr>
              <td>
                <label>
                    <@spring.bind "userInputDto.fanDubSources"/>
                  <input id="jutsu-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" value="${jutsu}"/>
                  <span>Jutsu</span>
                </label>
              </td>
              <td>
                <label>
                    <@spring.bind "userInputDto.fanDubSources"/>
                  <input id="shizaProject-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" value="${shizaProject}"/>
                  <span>ShizaProject</span>
                </label>
              </td>
              <td>
                <label>
                    <@spring.bind "userInputDto.fanDubSources"/>
                  <input id="sovetRomantica-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" value="${sovetRomantica}"/>
                  <span>SovetRomantica</span>
                </label>
              </td>
            </tr>
            </tbody>
          </table>
          <button type="submit" class="nes-btn is-primary width_full">Search For New Episodes</button>
        </form>
      </div>
    </section>
  </div>
</div>
</body>
<script src="${versionedUrls.getForLookupPath('/js/jquery.min.js')}"></script>
<script src="${versionedUrls.getForLookupPath('/js/submit.form.js')}"></script>
</html>