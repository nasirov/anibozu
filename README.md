# Anime Checker

[![Build Status](https://travis-ci.org/nasirov/anime-checker.svg?branch=master)](https://travis-ci.org/nasirov/anime-checker)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=nasirov_anime-checker&metric=alert_status)](https://sonarcloud.io/dashboard?id=nasirov_anime-checker)
[![Coverage Status](https://coveralls.io/repos/github/nasirov/anime-checker/badge.svg?branch=master)](https://coveralls.io/github/nasirov/anime-checker?branch=master)
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

The application analyzes user watching titles from **[MyAnimeList](https://myanimelist.net/)** and searches for new episodes on **[Animedia](https://online.animedia.tv/)**

# Docker
#### Image
- `docker pull nasirov/anime-checker:latest`
#### Run
- Install Docker and Docker-Compose
- Get **[docker-compose.yml](https://github.com/nasirov/anime-checker/blob/master/docker-compose.yml)** and run `docker-compose up`
- or just `docker run -p 8080:8080 nasirov/anime-checker:latest`
- Access http://localhost:8080/ if *nix or http://${docker-machine ip}:8080/ if windows
- Submit username from **[My Anime List](https://myanimelist.net/)** and wait for a while

# Note
- Keep in mind that most of the data is creates in runtime and the application check result is directly depends on mal and animedia ping and the amount of your watching titles
- For example, test case with 352 multiseasons titles handles about 2 minutes
- After first full check your data puts in the cache for 30 min
- Your title will update if it updates on mal or animedia in cached period
- The application supports only default anime list view on mal

# Screenshots

![Index](/images/index.jpg)
*Start page*

![Invalid Input](/images/invalidInput.jpg)
*MAL username must be between 2 and 16 characters*

![Submit form](/images/validInputAndDataProcessing.gif)
*Submit MAL username and wait a while*

![Result View](/images/resultViewPt1.jpg)
*Posters from "New Episode Available" section are provides links to new episodes on **[Animedia](https://online.animedia.tv/)***

![Result View](/images/resultViewPt2.jpg)
*Posters from "Not Found on Animedia" section are provides links to title page on **[MyAnimeList](https://myanimelist.net/)***