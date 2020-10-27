# Anime Checker

[![Build Status](https://travis-ci.com/nasirov/anime-checker.svg?branch=master)](https://travis-ci.com/nasirov/anime-checker)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=nasirov_anime-checker&metric=alert_status)](https://sonarcloud.io/dashboard?id=nasirov_anime-checker)
[![Coverage Status](https://coveralls.io/repos/github/nasirov/anime-checker/badge.svg?branch=master)](https://coveralls.io/github/nasirov/anime-checker?branch=master)
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

The application analyzes an user watching titles list from **[MyAnimeList](https://myanimelist.net/)** and searches for new episodes on FanDub Sites.

<table>
  <tbody>
  <tr>
    <th>Supported FanDub Sites</th>
    <th>Link Contains New Episode</th>
  </tr>
  <tr>
    <td><a href="https://www12.9anime.to/"><img src="https://www.google.com/s2/favicons?domain=https://www12.9anime.to/" alt="9anime"> 9Anime</a></td>
    <td>:heavy_plus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://anime.anidub.life/"><img src="https://www.google.com/s2/favicons?domain=https://anime.anidub.life/" alt="anidub"> Anidub</a></td>
    <td>:heavy_minus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://www.anilibria.tv/"><img src="https://www.google.com/s2/favicons?domain=https://www.anilibria.tv/" alt="anilibria"> Anilibria</a></td>
    <td>:heavy_minus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://animepik.org/"><img src="https://www.google.com/s2/favicons?domain=https://animepik.org/" alt="animepik"> AnimePiK</a></td>
    <td>:heavy_minus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://online.animedia.tv"><img src="https://www.google.com/s2/favicons?domain=https://online.animedia.tv" alt="animedia"> Animedia</a></td>
    <td>:heavy_plus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://jisedai.tv/"><img src="https://www.google.com/s2/favicons?domain=https://jisedai.tv/" alt="jesidai"> Jesidai</a></td>
    <td>:heavy_minus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://jut.su/"><img src="https://www.google.com/s2/favicons?domain=https://jut.su/" alt="jutsu"> Jutsu</a></td>
    <td>:heavy_plus_sign:</td>
  </tr>
  <tr>
    <td><a href="http://shiza-project.com/"><img src="https://www.google.com/s2/favicons?domain=http://shiza-project.com/" alt="shizaProject"> ShizaProject</a></td>
    <td>:heavy_plus_sign:</td>
  </tr>
  <tr>
    <td><a href="https://sovetromantica.com/"><img src="https://www.google.com/s2/favicons?domain=https://sovetromantica.com/" alt="sovetRomantica"> SovetRomantica</a></td>
    <td>:heavy_plus_sign:</td>
  </tr>
  </tbody>
</table>

**There are two types of result links:**

- a link that contains a new episode
- a link to a whole title page which contains a new episode but needs to be chosen manually

# Try It Out

![Flow](/images/flow.gif)

1. Open **[Anime Checker](https://anime-checker.herokuapp.com/)**
2. Enter an username from **[MyAnimeList](https://myanimelist.net/)**
3. Choose at least one FanDub site
4. Submit form

- You will receive your anime check result in real time title by title
- Titles from "Available" section have links to FanDub sites
- Titles from "Not Available" and "Not Found" sections have links to MyAnimeList site