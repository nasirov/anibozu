<!DOCTYPE html>
<html lang="en">
<head>
<#if errorMsg??>
  <title>${errorMsg}</title>
<#else>
  <title>Error</title>
</#if>
  <meta charset="UTF-8"/>
  <meta name="description" content="Get new episodes for your watching titles from MyAnimeList">
  <meta name="keywords"
        content="anime, mal, myanimelist, fandub, 9anime, anifilm, animaunt, anidub, anilibria, animedia, anything group, dream cast, freedub, kodik, senu project, sovetromantica">
  <meta name="robots" content="index, follow">
  <meta http-equiv="cache-control" content="no-cache"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <meta name="color-scheme" content="dark light"/>
  <meta property="og:type" content="website">
  <meta property="og:url" content="https://anime-checker.nasirov.info/"/>
  <meta property="og:title" content="Anime Checker">
  <meta property="og:description" content="Get new episodes for your watching titles from MyAnimeList">
  <link rel="shortcut icon" type="image/x-icon" href="/img/favicon.ico"/>
  <link rel="stylesheet" type="text/css" href="/css/fonts.css"/>
  <link rel="stylesheet" type="text/css" href="/css/main.css"/>
  <script type="application/javascript" src="/js/jquery.js"></script>
  <script type="application/javascript" src="/js/main.js"></script>
</head>
<body class="dark">
<div class="main-grid">
  <nav class="nav-bar">
    <div class="nav-bar__container">
      <a href="/" class="home-icon sized-icon-50"></a>
      <a
          href="https://myanimelist.net/"
          class="mal-icon sized-icon-50"
          target="_blank"
      ></a>
      <div class="switch-container">
        <span class="moon-icon sized-icon-50"></span>
        <label class="theme_switcher" for="switch-theme-checkbox">
          <input
              type="checkbox"
              id="switch-theme-checkbox"
              class="theme_switcher__checkbox"
          />
          <span class="theme_switcher__slider"></span>
        </label>
        <span class="sun-icon sized-icon-50"></span>
      </div>
    </div>
  </nav>

  <main class="main-container">
      <#if errorMsg??>
        <h1 class="error-message">${errorMsg}</h1>
      <#else>
        <h1 class="error-message">Sorry, something went wrong. Please try again later.</h1>
      </#if>
  </main>

  <footer class="footer">
    <div class="footer__container">
      <a
          href="https://github.com/nasirov/anime-checker"
          class="github-icon sized-icon-50"
          target="_blank"
      ></a>
      <a
          href="https://www.linkedin.com/in/yuriy-nasirov/"
          class="linkedin-icon sized-icon-50"
          target="_blank"
      ></a>
      <a
          href="https://myanimelist.net/profile/yasamuro"
          class="mal-icon sized-icon-50"
          target="_blank"
      ></a>
    </div>
  </footer>
</div>
</body>
</html>