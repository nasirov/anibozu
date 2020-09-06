<#import "/spring.ftl" as spring/>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Anime Checker</title>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/center-style.css')}"/>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/nes.min.css')}"/>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/ps2p.css')}"/>
  <link rel="shortcut icon" type="image/x-icon" href="${versionedUrls.getForLookupPath('/img/favicon.ico')}"/>
</head>

<body>
<div id="container">
  <div class="inner">
    <section class="nes-container with-title is-centered">
      <p class="title">Anime Checker</p>
      <div class="nes-field">
        <form id="username-submit-form" action="/result/" method="post">
            <@spring.bind "malUser"/>
            <@spring.formInput "malUser.username",'class="nes-input"
                 placeholder="Enter MAL username..."
                 title="Please enter a valid mal username between 2 and 16 characters(latin letters, numbers, underscores and dashes only)"
                 autofocus'/>
          <label>
              <@spring.bind "malUser.fanDubSources"/>
            <input id="animedia-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" checked="checked" value="${animedia}"/>
            <span>Animedia</span>
          </label>
          <label>
              <@spring.bind "malUser.fanDubSources"/>
            <input id="animedia-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" checked="checked" value="${anidub}"/>
            <span>Anidub</span>
          </label>
          <label>
              <@spring.bind "malUser.fanDubSources"/>
            <input id="jisedai-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" checked="checked" value="${jisedai}"/>
            <span>Jisedai</span>
          </label>
          <label>
              <@spring.bind "malUser.fanDubSources"/>
            <input id="animepik-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" checked="checked" value="${animepik}"/>
            <span>AnimePiK</span>
          </label>
          <label>
              <@spring.bind "malUser.fanDubSources"/>
            <input id="anilibria-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" checked="checked" value="${anilibria}"/>
            <span>Anilibria</span>
          </label>
          <label>
              <@spring.bind "malUser.fanDubSources"/>
            <input id="jutsu-checkbox" type="checkbox" class="nes-checkbox" name="fanDubSources" checked="checked" value="${jutsu}"/>
            <span>Jutsu</span>
          </label>
          <button type="submit" class="nes-btn is-primary">Search for new episodes</button>
        </form>
      </div>
    </section>
  </div>
</div>
</body>
<script src="${versionedUrls.getForLookupPath('/js/jquery.min.js')}"></script>
<script src="${versionedUrls.getForLookupPath('/js/submit.form.js')}"></script>
</html>