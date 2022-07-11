<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Anime Checker</title>
  <link rel="stylesheet" type="text/css" href="/css/center-style.css"/>
  <link rel="stylesheet" type="text/css" href="/css/style.css"/>
  <link rel="stylesheet" type="text/css" href="/css/nes.min.css"/>
  <link rel="stylesheet" type="text/css" href="/css/ps2p.css"/>
  <link rel="shortcut icon" type="image/x-icon" href="/img/favicon.ico"/>
</head>

<body>
<div id="container">
  <div class="inner">
    <h1 class="is_centered">Anime Checker</h1>
    <section class="nes-container is-centered">
      <div class="nes-field">
        <form id="username-submit-form" action="/result" method="get">
          <input type="text" id="username" name="username" value="" class="nes-input" placeholder="Enter MAL username..."
                 title="Please enter a valid mal username between 2 and 16 characters(latin letters, numbers, underscores and dashes only)" autofocus>
          <div class="nes-field">Choose At Least One Fandub Site</div>
          <table class="width_full font_x-small is_text_left">
            <tbody>
            <tr>
              <td>
                <label>
                  <input id="nineAnime-checkbox" type="checkbox" class="nes-checkbox" name="fandubSources" value="${nineAnime}"/>
                  <span>9Anime</span>
                </label>
              </td>
              <td>
                <label>
                  <input id="anidub-checkbox" type="checkbox" class="nes-checkbox" name="fandubSources" value="${anidub}"/>
                  <span>Anidub</span>
                </label>
              </td>
              <td>
                <label>
                  <input id="anilibria-checkbox" type="checkbox" class="nes-checkbox" name="fandubSources" value="${anilibria}"/>
                  <span>Anilibria</span>
                </label>
              </td>
            </tr>
            <tr>
              <td>
                <label>
                  <input id="animepik-checkbox" type="checkbox" class="nes-checkbox" name="fandubSources" value="${animepik}"/>
                  <span>AnimePiK</span>
                </label>
              </td>
              <td>
                <label>
                  <input id="animedia-checkbox" type="checkbox" class="nes-checkbox" name="fandubSources" value="${animedia}"/>
                  <span>Animedia</span>
                </label>
              </td>
              <td>
                <label>
                  <input id="jamClub-checkbox" type="checkbox" class="nes-checkbox" name="fandubSources" value="${jamClub}"/>
                  <span>JamClub</span>
                </label>
              </td>
            </tr>
            <tr>
              <td>
                <label>
                  <input id="jisedai-checkbox" type="checkbox" class="nes-checkbox" name="fandubSources" value="${jisedai}"/>
                  <span>Jisedai</span>
                </label>
              </td>
              <td>
                <label>
                  <input id="jutsu-checkbox" type="checkbox" class="nes-checkbox" name="fandubSources" value="${jutsu}"/>
                  <span>Jutsu</span>
                </label>
              </td>
              <td>
                <label>
                  <input id="shizaProject-checkbox" type="checkbox" class="nes-checkbox" name="fandubSources" value="${shizaProject}"/>
                  <span>ShizaProject</span>
                </label>
              </td>
            </tr>
            <tr>
              <td>
                <label>
                  <input id="sovetRomantica-checkbox" type="checkbox" class="nes-checkbox" name="fandubSources" value="${sovetRomantica}"/>
                  <span>SovetRomantica</span>
                </label>
              </td>
              <td>
                <label>
                  <input id="anythingGroup-checkbox" type="checkbox" class="nes-checkbox" name="fandubSources" value="${anythingGroup}"/>
                  <span>Anything Group</span>
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
<script src="/js/jquery.min.js"></script>
<script src="/js/index.js"></script>
</html>