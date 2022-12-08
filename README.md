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
| [![akariGroup](/images/favicons/akariGroup.png)](https://akari-anime.com/) **[Akari Group](https://akari-anime.com/)**                  |        :heavy_minus_sign:         |
| [![amber](/images/favicons/amber.png)](https://vkstudioamber.github.io/) **[Amber](https://vkstudioamber.github.io/)**                  |         :heavy_plus_sign:         |
| [![anibreeze](/images/favicons/anibreeze.png)](http://anibreeze.ru/) **[AniBreeze](http://anibreeze.ru/)**                              |        :heavy_minus_sign:         |
| [![aniMaunt](/images/favicons/aniMaunt.png)](https://animaunt.org/) **[AniMaunt](https://animaunt.org/)**                               |        :heavy_minus_sign:         |
| [![aniNyaTv](/images/favicons/aniNyaTv.png)](https://aninyatv.ru/) **[AniNya.TV](https://aninyatv.ru/)**                                |         :heavy_plus_sign:         |
| [![aniPlague](/images/favicons/aniPlague.png)](https://plaguestudios.nl/) **[AniPlague](https://plaguestudios.nl/)**                    |        :heavy_minus_sign:         |
| [![aniPlayTv](/images/favicons/aniPlayTv.png)](https://aniplay.tv/) **[AniPlay.TV](https://aniplay.tv/)**                               |         :heavy_plus_sign:         |
| [![aniRise](/images/favicons/aniRise.png)](https://anirise.com/) **[AniRise](https://anirise.com/)**                                    |         :heavy_plus_sign:         |
| [![anidub](/images/favicons/anidub.png)](https://anime.anidub.life/) **[Anidub](https://anime.anidub.life/)**                           |        :heavy_minus_sign:         |
| [![anilibria](/images/favicons/anilibria.png)](https://www.anilibria.tv/) **[Anilibria](https://www.anilibria.tv/)**                    |        :heavy_minus_sign:         |
| [![animeBest](/images/favicons/animeBest.png)](https://anime1.animebesst.org/) **[AnimeBest](https://anime1.animebesst.org/)**          |        :heavy_minus_sign:         |
| [![animeJet](/images/favicons/animeJet.png)](https://animejet.ru/) **[AnimeJet](https://animejet.ru/)**                                 |         :heavy_plus_sign:         |
| [![animedia](/images/favicons/animedia.png)](https://online.animedia.tv/) **[Animedia](https://online.animedia.tv/)**                   |         :heavy_plus_sign:         |
| [![anythingGroup](/images/favicons/anythingGroup.png)](https://a-g.site/) **[Anything Group](https://a-g.site/)**                       |         :heavy_plus_sign:         |
| [![dreamCast](/images/favicons/dreamCast.png)](https://dreamerscast.com/) **[Dream Cast](https://dreamerscast.com/)**                   |        :heavy_minus_sign:         |
| [![dubClub](/images/favicons/dubClub.png)](https://dubclub.online/) **[DubClub](https://dubclub.online/)**                              |        :heavy_minus_sign:         |
| [![freeDub](/images/favicons/freeDub.png)](https://freedubstudio.club/) **[FreeDub](https://freedubstudio.club/)**                      |        :heavy_minus_sign:         |
| [![frondaStudio](/images/favicons/frondaStudio.png)](https://frondastudio.ru/) **[Fronda Studio](https://frondastudio.ru/)**            |        :heavy_minus_sign:         |
| [![fumoDub](/images/favicons/fumoDub.png)](https://fumodub.ru/) **[FumoDub](https://fumodub.ru/)**                                      |         :heavy_plus_sign:         |
| [![haronMedia](/images/favicons/haronMedia.png)](https://haronmedia.online/) **[Haron Media](https://haronmedia.online/)**              |         :heavy_plus_sign:         |
| [![insomniaStudio](/images/favicons/insomniaStudio.png)](https://somnis.ru/) **[INSOMNIA Studio](https://somnis.ru/)**                  |         :heavy_plus_sign:         |
| [![jnUnion](/images/favicons/jnUnion.png)](https://jn-union.com/) **[J&N union](https://jn-union.com/)**                                |        :heavy_minus_sign:         |
| [![jamClub](/images/favicons/jamClub.png)](https://jam-club.org/) **[Jam Club](https://jam-club.org/)**                                 |        :heavy_minus_sign:         |
| [![jisedai](/images/favicons/jisedai.png)](https://jisedai.tv/) **[Jisedai](https://jisedai.tv/)**                                      |        :heavy_minus_sign:         |
| [![jutsu](/images/favicons/jutsu.png)](https://jut.su/) **[Jutsu](https://jut.su/)**                                                    |         :heavy_plus_sign:         |
| [![kalabsStudio](/images/favicons/kalabsStudio.png)](https://kalabs.bicbai.ru/) **[Kalabs Studio](https://kalabs.bicbai.ru/)**          |        :heavy_minus_sign:         |
| [![komnataDidi](/images/favicons/komnataDidi.png)](https://komnatadidi.com/) **[Komnata DiDi](https://komnatadidi.com/)**               |        :heavy_minus_sign:         |
| [![senuProject](/images/favicons/senuProject.png)](https://senu.pro/) **[SENU Project](https://senu.pro/)**                             |        :heavy_minus_sign:         |
| ~~![shizaProject](/images/favicons/shizaProject.png) SHIZA Project~~                                                                    |        :heavy_minus_sign:         |
| [![stepOnee](/images/favicons/stepOnee.png)](https://steponee.tv/) **[STEPonee](https://steponee.tv/)**                                 |        :heavy_minus_sign:         |
| **[Shiroi Kitsune](https://shiroikitsune.org/)**                                                                                        |        :heavy_minus_sign:         |
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