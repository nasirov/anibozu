# Anime Checker

[![Build Status](https://travis-ci.com/nasirov/anime-checker.svg?branch=master)](https://travis-ci.com/nasirov/anime-checker)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=nasirov_anime-checker&metric=alert_status)](https://sonarcloud.io/dashboard?id=nasirov_anime-checker)
[![Coverage Status](https://coveralls.io/repos/github/nasirov/anime-checker/badge.svg?branch=master)](https://coveralls.io/github/nasirov/anime-checker?branch=master)
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

The application analyzes a user watching titles list from **[MyAnimeList](https://myanimelist.net/)** and searches for new episodes on FanDub Sites.

<table>
  <tbody>
  <tr>
    <th>Supported FanDub Sites</th>
    <th>Link Contains New Episode</th>
  </tr>
  <tr>
    <td><a href="https://www13.9anime.to/"><img src="/images/favicons/9anime.png" alt="9anime"> 9Anime</a></td>
    <td>:heavy_plus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://anime.anidub.life/"><img src="/images/favicons/anidub.png" alt="anidub"> Anidub</a></td>
    <td>:heavy_minus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://www.anilibria.tv/"><img src="/images/favicons/anilibria.png" alt="anilibria"> Anilibria</a></td>
    <td>:heavy_minus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://animepik.org/"><img src="/images/favicons/animepik.png" alt="animepik"> AnimePiK</a></td>
    <td>:heavy_minus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://online.animedia.tv"><img src="/images/favicons/animedia.png" alt="animedia"> Animedia</a></td>
    <td>:heavy_plus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://jisedai.tv/"><img src="/images/favicons/jisedai.png" alt="Jisedai"> Jisedai</a></td>
    <td>:heavy_minus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://jut.su/"><img src="/images/favicons/jutsu.png" alt="jutsu"> Jutsu</a></td>
    <td>:heavy_plus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://shiza-project.com/"><img src="/images/favicons/shizaProject.png" alt="shizaProject"> ShizaProject</a></td>
    <td>:heavy_minus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://sovetromantica.com/"><img src="/images/favicons/sovetRomantica.png" alt="sovetRomantica"> SovetRomantica</a></td>
    <td>:heavy_plus_sign:</td>
  </tr>
  </tbody>
</table>

**There are two types of result links:**

- a link that contains a new episode
- a link to a whole title page which contains a new episode but needs to be chosen manually

# Try It Out

![Flow](/images/flow.gif)

1. Open **[Anime Checker](https://anime-checker.herokuapp.com/)** via an **[EventSource compatible browser](https://developer.mozilla.org/en-US/docs/Web/API/EventSource#Browser_compatibility)**  with enabled JavaScript
2. Enter a username from **[MyAnimeList](https://myanimelist.net/)**
3. Choose at least one FanDub site
4. Submit form

- You will receive your anime check result in real time title by title
- On a FanDub link hover you will see a hint with a new episode name. This feature is useful for a Fandub site which link doesn't contain an episode and you have to choose it manually in an embedded player
- Titles from "Available" section have links to FanDub sites
- Titles from "Not Available" and "Not Found" sections have links to MyAnimeList site