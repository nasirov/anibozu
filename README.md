# Anime Checker

[![Tests, sonar and deploy](https://github.com/nasirov/anime-checker/actions/workflows/on_push_wf.yaml/badge.svg?branch=master&event=push)](https://github.com/nasirov/anime-checker/actions/workflows/on_push_wf.yaml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=nasirov_anime-checker&metric=alert_status)](https://sonarcloud.io/dashboard?id=nasirov_anime-checker)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=nasirov_anime-checker&metric=coverage)](https://sonarcloud.io/dashboard?id=nasirov_anime-checker)
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

The application analyzes a user's watching titles list from **[MyAnimeList](https://myanimelist.net/)** and searches for next episodes on the
supported Fandub Sites.

| Supported Fandub Sites                                                                                                                  | Provides direct links to episodes |
|:----------------------------------------------------------------------------------------------------------------------------------------|:---------------------------------:|
| [![9anime](/images/favicons/9anime.png)](https://9anime.id/) **[9anime](https://9anime.id/)**                                           |         :heavy_plus_sign:         |
| [![aniFilm](/images/favicons/aniFilm.png)](https://www.anifilm.tv/) **[AniFilm](https://www.anifilm.tv/)**                              |        :heavy_minus_sign:         |
| [![aniMaunt](/images/favicons/aniMaunt.png)](https://animaunt.org/) **[AniMaunt](https://animaunt.org/)**                               |        :heavy_minus_sign:         |
| [![anidub](/images/favicons/anidub.png)](https://anidub.com/) **[Anidub](https://anidub.com/)**                                         |        :heavy_minus_sign:         |
| [![anilibria](/images/favicons/anilibria.png)](https://www.anilibria.tv/) **[Anilibria](https://www.anilibria.tv/)**                    |        :heavy_minus_sign:         |
| [![animedia](/images/favicons/animedia.png)](https://online.animedia.tv/) **[Animedia](https://online.animedia.tv/)**                   |         :heavy_plus_sign:         |
| [![anythingGroup](/images/favicons/anythingGroup.png)](https://a-g.site/) **[Anything Group](https://a-g.site/)**                       |         :heavy_plus_sign:         |
| [![dreamCast](/images/favicons/dreamCast.png)](https://dreamerscast.com/) **[Dream Cast](https://dreamerscast.com/)**                   |        :heavy_minus_sign:         |
| [![freeDub](/images/favicons/freeDub.png)](https://freedubstudio.club/) **[FreeDub](https://freedubstudio.club/)**                      |        :heavy_minus_sign:         |
| **[Kodik](https://mal-to-kodik.github.io/)**                                                                                            |        :heavy_minus_sign:         |
| [![senuProject](/images/favicons/senuProject.png)](https://senu.pro/) **[SENU Project](https://senu.pro/)**                             |        :heavy_minus_sign:         |
| [![sovetRomantica](/images/favicons/sovetRomantica.png)](https://sovetromantica.com/) **[SovetRomantica](https://sovetromantica.com/)** |         :heavy_plus_sign:         |

> :memo: **Note:**
>
> If a Fandub site doesn't provide direct links to episodes, you will have to choose it manually by name in the site's embedded video player.
> You can see target episodes names by hovering Fandub sites logos.

# Try It Out

![Flow](/images/flow.gif)

1. Open **[Anime Checker](https://anime-checker.nasirov.info/)** via browser with ***enabled JavaScript***
2. Enter your **[MyAnimeList](https://myanimelist.net/)** username. ***Your anime list must be public!***
3. Submit the form
4. Wait for a result