# Anime Checker

[![Build Status](https://travis-ci.org/nasirov/anime-checker.svg?branch=master)](https://travis-ci.org/nasirov/anime-checker)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=nasirov_anime-checker&metric=alert_status)](https://sonarcloud.io/dashboard?id=nasirov_anime-checker)
[![Coverage Status](https://coveralls.io/repos/github/nasirov/anime-checker/badge.svg?branch=master)](https://coveralls.io/github/nasirov/anime-checker?branch=master)
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

The application analyzes an user watching titles from **[MyAnimeList](https://myanimelist.net/)** and searches for new episodes on FanDub Sites.

# Supported FanDub Sites

<table>
  <tbody>
  <tr>
    <td><a href="https://online.animedia.tv"><img src="https://www.google.com/s2/favicons?domain=https://online.animedia.tv" alt="animedia"> Animedia</a></td>
  </tr>
  <tr>
    <td><a href="https://anime.anidub.life/"><img src="https://www.google.com/s2/favicons?domain=https://anime.anidub.life/" alt="anidub"> Anidub</a></td>
  </tr>
  <tr>
    <td><a href="https://jisedai.tv/"><img src="https://www.google.com/s2/favicons?domain=https://jisedai.tv/" alt="jesidai"> Jesidai</a></td>
  </tr>
  </tbody>
</table>

# Disabled FanDub Sites

<table>
  <tbody>
  <tr>
    <td><a href="https://9anime.to"><img src="https://www.google.com/s2/favicons?domain=https://9anime.to" alt="9anime"> 9anime(Because of
     aggressive WAF policy)</a></td>
  </tr>
  </tbody>
</table>

# Try It Out On Heroku

https://anime-checker.herokuapp.com/

# Flow

![Submit form](/images/flow.gif)
*Submit a MAL username and wait a while*