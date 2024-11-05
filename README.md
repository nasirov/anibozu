<p align="center">
  <img width="325" height="215" alt="anibozu-logo" src="https://raw.githubusercontent.com/nasirov/anibozu/main/frontend/img/logo.png"> <br>
  <a href="https://anibozu.moe/">Anibozu</a> is a free service that provides links to the next episodes on 
  <a href="https://github.com/nasirov/anibozu#supported-anime-sites">the anime sites</a> <br>
  for your watching anime from <a href="https://myanimelist.net/">MyAnimeList</a>. <br>
</p>

### Status

[![Backend](https://github.com/nasirov/anibozu/actions/workflows/backend-on_push.yaml/badge.svg)](https://github.com/nasirov/anibozu/actions/workflows/backend-on_push.yaml)
[![Frontend](https://github.com/nasirov/anibozu/actions/workflows/frontend-on_push.yaml/badge.svg)](https://github.com/nasirov/anibozu/actions/workflows/frontend-on_push.yaml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=nasirov_anibozu&metric=alert_status)](https://sonarcloud.io/dashboard?id=nasirov_anibozu)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=nasirov_anibozu&metric=coverage)](https://sonarcloud.io/dashboard?id=nasirov_anibozu)
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

### Supported anime sites

| Site            | Language | Type       |                                          Link                                           | Direct links to episodes |        Dub        |        Sub         |
|:----------------|:--------:|:-----------|:---------------------------------------------------------------------------------------:|:------------------------:|:-----------------:|:------------------:|
| Dream Cast      |   :ru:   | Original   |       [![dream_cast](/images/favicons/dream_cast.png)](https://dreamerscast.com/)       |    :heavy_minus_sign:    | :heavy_plus_sign: | :heavy_minus_sign: |
| Gogoanime       |   :uk:   | Aggregator |          [![gogo_anime](/images/favicons/gogo_anime.png)](https://anitaku.bz/)          |    :heavy_plus_sign:     | :heavy_plus_sign: | :heavy_plus_sign:  |
| HiAnime         |   :uk:   | Aggregator |            [![hi_anime](/images/favicons/hi_anime.png)](https://hianime.to/)            |    :heavy_plus_sign:     | :heavy_plus_sign: | :heavy_plus_sign:  |
| MAL-To-Kodik    |   :ru:   | Aggregator |            [:link:](https://github.com/mal-to-kodik/mal-to-kodik.github.io)             |    :heavy_plus_sign:     | :heavy_plus_sign: | :heavy_plus_sign:  |
| SovetRomantica  |   :ru:   | Original   | [![sovet_romantica](/images/favicons/sovet_romantica.png)](https://sovetromantica.com/) |    :heavy_plus_sign:     | :heavy_plus_sign: | :heavy_plus_sign:  |

### Who would use it?

Folks that don't stick to one anime site and are lazy enough to check their watching anime for new episodes on the anime sites manually.

<img alt="sw_obi_wan" src="https://raw.githubusercontent.com/nasirov/anibozu/main/images/extra/sw_obi_wan.gif">

### Limitations

- Depends on MAL availability
- Handles at max 50 watching anime per user with a public anime list
- Anime mapping is not 100% accurate and updates once a day