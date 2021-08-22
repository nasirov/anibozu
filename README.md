# Anime Checker

[![Build Status](https://travis-ci.com/nasirov/anime-checker.svg?branch=master)](https://travis-ci.com/nasirov/anime-checker)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=nasirov_anime-checker&metric=alert_status)](https://sonarcloud.io/dashboard?id=nasirov_anime-checker)
[![Coverage Status](https://coveralls.io/repos/github/nasirov/anime-checker/badge.svg?branch=master)](https://coveralls.io/github/nasirov/anime-checker?branch=master)
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

The application analyzes a user watching titles list from **[MyAnimeList](https://myanimelist.net/)** and searches for new episodes on FanDub Sites.

| Supported FanDub Sites | Link Contains New Episode |
| :---                   |           :---:           |
| [![9anime](/images/favicons/9anime.png)](https://www13.9anime.to/)**[9Anime](https://www13.9anime.to/)**  | :heavy_plus_sign: |
| [![anidub](/images/favicons/anidub.png)](https://anime.anidub.life/) **[Anidub](https://anime.anidub.life/)**  | :heavy_minus_sign: |
| [![anilibria](/images/favicons/anilibria.png)](https://www.anilibria.tv/) **[Anilibria](https://www.anilibria.tv/)**  | :heavy_minus_sign: |
| [![animepik](/images/favicons/animepik.png)](https://animepik.org/) **[AnimePiK](https://animepik.org/)**  | :heavy_minus_sign: |
| [![animedia](/images/favicons/animedia.png)](https://online.animedia.tv/) **[Animedia](https://online.animedia.tv/)**  | :heavy_plus_sign: |
| [![jamClub](/images/favicons/jamClub.png)](https://jamclub.cc/) **[JamClub](https://jamclub.cc/)**  | :heavy_minus_sign: |
| [![jisedai](/images/favicons/jisedai.png)](https://jisedai.tv/) **[Jisedai](https://jisedai.tv/)**  | :heavy_minus_sign: |
| [![jutsu](/images/favicons/jutsu.png)](https://jut.su/) **[Jutsu](https://jut.su/)**  | :heavy_plus_sign: |
| [![shizaProject](/images/favicons/shizaProject.png)](https://shiza-project.com/) **[ShizaProject](https://shiza-project.com/)**  | :heavy_minus_sign: |
| [![sovetRomantica](/images/favicons/sovetRomantica.png)](https://sovetromantica.com/) **[SovetRomantica](https://sovetromantica.com/)**  | :heavy_plus_sign: |

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