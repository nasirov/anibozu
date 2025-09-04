const THEMES = {DARK: 'dark', LIGHT: 'light'};
const THEME_ATTR = 'theme';
const THEME_COOKIE = 'theme';
const GENERIC_ERROR_MESSAGE = 'Sorry, something went wrong. Please try again later.';
const TOO_MANY_REQUESTS_ERROR_MESSAGE = 'You\'ve performed too many requests. Please try again later.';
const SVG_NS = 'http://www.w3.org/2000/svg';
const CLICK_TYPE = 'click';
const KEYPRESS_EVENT_TYPE = 'keypress';
const KEY_ENTER = 'Enter';
const TYPE_ATTR = 'type';
const TYPE_DUB = 'dub';
const TYPE_SUB = 'sub';
const TYPE_DUB_SUB = 'dub_sub';
const FILTER_TYPE_ALL = 'all';

document.addEventListener('DOMContentLoaded', () => {
    switchTheme();
    setThemeSwitcherEvents();
    setFormEvents();
});

/**
 * Theme setting priority:
 * 1. user's choice - {@link THEME_COOKIE}
 * 2. browser's default - media prefers-color-scheme
 */
function switchTheme() {
    let theme;
    if (window.localStorage) {
        theme = localStorage.getItem(THEME_COOKIE);
    }
    if (!theme && window.matchMedia) {
        theme = window.matchMedia('(prefers-color-scheme: light)').matches ? THEMES.LIGHT : THEMES.DARK;
    }
    setTheme(theme);
}

function setThemeSwitcherEvents() {
    const themeSwitcher = document.querySelector('.theme-switcher');

    [CLICK_TYPE, KEYPRESS_EVENT_TYPE].forEach(type => {
            themeSwitcher.addEventListener(type, e => {
                e.preventDefault();
                if (e.type === CLICK_TYPE || (e.type === KEYPRESS_EVENT_TYPE && e.key === KEY_ENTER)) {
                    const currentTheme = document.documentElement.getAttribute(THEME_ATTR);
                    const nextTheme = currentTheme === THEMES.LIGHT ? THEMES.DARK : THEMES.LIGHT;
                    setTheme(nextTheme);
                    if (window.localStorage) {
                        localStorage.setItem(THEME_COOKIE, nextTheme);
                    }
                }
            });
        }
    );
}

function setTheme(theme) {
    if (theme) {
        document.documentElement.setAttribute(THEME_ATTR, theme);
    }
}

function setFormEvents() {
    document.querySelector('form').addEventListener('submit',
        async e => {
            e.preventDefault();
            const input = e.target.querySelector('input:valid');
            if (input) {
                const username = input.value;
                input.setAttribute('readonly', '');
                const button = e.target.querySelector('button');
                const icon = button.querySelector('svg');
                const intervalId = setLoadingProgress(button);
                await renderAnimeList(username);
                input.removeAttribute('readonly');
                clearInterval(intervalId);
                withText(button, '');
                button.appendChild(icon);
            }
        });
}

function setLoadingProgress(element) {
    const symbols = ['-', '\\', '|', '/'];
    let counter = 0;
    withText(element, '*');
    return setInterval(() => {
        withText(element, symbols[counter]);
        counter = (counter + 1) % symbols.length;
    }, 100);
}

async function renderAnimeList(username) {
    try {
        const response = await fetch(`https://api.anibozu.moe/api/v1/user/${username}/anime-list`);
        if (response.status === 429) {
            renderErrorMessage(TOO_MANY_REQUESTS_ERROR_MESSAGE);
            return;
        }

        if (response.status >= 500) {
            renderErrorMessage(GENERIC_ERROR_MESSAGE);
            return;
        }

        const responseDto = await response.json();
        const animeDtoList = responseDto.list;
        if (!animeDtoList || animeDtoList.length === 0) {
            renderErrorMessage(responseDto.errorMessage);
            return;
        }

        const animeList = div('anime-list');

        animeDtoList.forEach(animeDto => {
            const anime = div('anime');
            animeList.appendChild(anime);
            anime.appendChild(buildPoster(animeDto));

            const overlay = div('overlay');
            overlay.appendChild(buildMalInfo(animeDto));
            anime.appendChild(overlay);

            if (animeDto.episodes.length > 0) {
                const dialog = buildDialog(animeDto);
                anime.appendChild(dialog);

                overlay.appendChild(buildSitesInfo(animeDto, dialog));
            }
        });

        renderMessage(`${username}'s watching anime list`);
        getMainContainer().appendChild(animeList);
    } catch (e) {
        renderErrorMessage(GENERIC_ERROR_MESSAGE);
        console.error(e, e.stack);
    }
}

function withText(element, value) {
    element.textContent = value;
    return element;
}

function withClass(element, targetClass) {
    element.setAttribute('class', targetClass);
    return element;
}

function withTitle(element, title) {
    element.setAttribute('title', title);
    return element;
}

function div(targetClass) {
    return withClass(document.createElement('div'), targetClass);
}

function span(targetClass, title, text) {
    const result = document.createElement('span');
    withClass(result, targetClass);
    withTitle(result, title);
    return withText(result, text);
}

function button(targetClass) {
    return withClass(document.createElement('button'), targetClass);
}

function link(targetClass, href) {
    const result = document.createElement('a');
    withClass(result, targetClass);
    result.setAttribute('target', '_blank');
    result.setAttribute('href', href);
    result.setAttribute('draggable', 'false');
    return result;
}

function svg(targetClass, viewBox, dValue) {
    const result = withClass(document.createElementNS(SVG_NS, 'svg'), targetClass);
    result.setAttribute('viewBox', viewBox);
    const path = document.createElementNS(SVG_NS, 'path');
    path.setAttribute('d', dValue);
    result.appendChild(path);
    return result;
}

function buildPoster(animeDto) {
    const result = document.createElement('img');
    result.setAttribute('loading', 'lazy');
    result.setAttribute('src', animeDto.posterUrl);
    result.setAttribute('alt', animeDto.name);
    withTitle(result, animeDto.name);
    result.setAttribute('draggable', 'false');
    return result;
}

function buildMalInfo(animeDto) {
    const result = div('mal_info');

    if (animeDto.name) {
        result.appendChild(buildBadge(animeDto.name, animeDto.name));
    }

    result.appendChild(buildMalEpisode(animeDto));

    if (animeDto.airing) {
        result.appendChild(buildBadge('Airing', 'Airing'));
    }

    result.appendChild(buildMalLink(animeDto))
    return result;
}

function buildMalEpisode(animeDto) {
    const result = div('badge');
    const maxEpisodes = animeDto.maxEpisodes;
    const title = 'Next ep. ' + animeDto.nextEpisode + ' / ' + (maxEpisodes === 0 ? '?' : maxEpisodes);
    return withText(withTitle(result, title), title);
}

function buildMalLink(animeDto) {
    const result = link('badge', animeDto.malUrl);
    const title = 'Open on MyAnimeList';
    withTitle(result, title);
    result.appendChild(span('', title, 'MyAnimeList'))
    result.appendChild(buildRightArrowSvg())
    return result;
}

function buildDialog(animeDto) {
    const result = document.createElement('dialog');
    const closeButton = buildCloseButton();
    result.appendChild(closeButton);

    [CLICK_TYPE, KEYPRESS_EVENT_TYPE].forEach(type => {
            closeButton.addEventListener(type, e => {
                if (e.type === CLICK_TYPE || e.key === KEY_ENTER) {
                    result.close();
                }
            });
        }
    );

    result.addEventListener(CLICK_TYPE, e => {
        if (e.target === result) {
            result.close();
        }
    });

    addTypeFilter(animeDto, result);
    result.append(buildSites(animeDto.episodes));
    return result;
}

function buildSitesInfo(animeDto, dialog) {
    const result = div('sites_info');

    addTranslationTypeInfo(animeDto, TYPE_DUB, result);
    addTranslationTypeInfo(animeDto, TYPE_SUB, result);

    const openDialogButton = withText(button('badge'), 'Pick a Site');
    result.appendChild(openDialogButton);

    [CLICK_TYPE, KEYPRESS_EVENT_TYPE].forEach(type => {
            openDialogButton.addEventListener(type, e => {
                if (e.type === CLICK_TYPE || e.key === KEY_ENTER) {
                    dialog.showModal();
                }
            });
        }
    );

    return result;
}

function addTranslationTypeInfo(animeDto, translationType, sitesInfo) {
    const matchedAmount = animeDto[translationType];
    if (matchedAmount > 0) {
        let badge;
        let title;

        if (translationType === TYPE_DUB) {
            badge = buildDub();
            title = TYPE_DUB;
        }

        if (translationType === TYPE_SUB) {
            badge = buildSub();
            title = TYPE_SUB;
        }

        if (badge && title) {
            badge.appendChild(span('', title, matchedAmount));
            sitesInfo.appendChild(badge);
        }
    }
}

function addTypeFilter(animeDto, dialog) {
    const dubPresents = animeDto.dub > 0;
    const subPresents = animeDto.sub > 0;

    if (animeDto.episodes.length > 1 && dubPresents && subPresents) {
        const typeFilter = withClass(document.createElement('select'), 'filter');

        typeFilter.addEventListener('change', () => {
            const hideClass = 'hidden';
            dialog.querySelectorAll('.site').forEach(site => {
                if (typeFilter.value === FILTER_TYPE_ALL) {
                    site.classList.remove(hideClass);
                } else {
                    const type = site.getAttribute(TYPE_ATTR);
                    if (type && type.includes(typeFilter.value)) {
                        site.classList.remove(hideClass);
                    } else {
                        site.classList.add(hideClass);
                    }
                }
            });
        });

        addOption(typeFilter, FILTER_TYPE_ALL, 'All Types');

        if (dubPresents) {
            addOption(typeFilter, TYPE_DUB, 'Dub');
        }

        if (subPresents) {
            addOption(typeFilter, TYPE_SUB, 'Sub');
        }

        dialog.appendChild(typeFilter);
    }
}

function addOption(filter, value, text) {
    const result = document.createElement('option');
    result.setAttribute('value', value)
    filter.appendChild(withText(result, text));
}

function buildSites(episodes) {
    const result = div('sites');

    for (const i in episodes) {
        const episode = episodes[i];
        const site = div('site');

        const title = div('title');
        title.appendChild(withText(document.createElement('h2'), episode.siteName));
        site.appendChild(title);

        site.appendChild(document.createElement('hr'));

        const types = div('types');

        if (episode.type === TYPE_DUB) {
            types.appendChild(buildDubSvg());
        } else if (episode.type === TYPE_SUB) {
            types.appendChild(buildSubSvg());
        } else if (episode.type === TYPE_DUB_SUB) {
            types.appendChild(buildDubSvg());
            types.appendChild(buildSubSvg());
        }

        if (types.childNodes.length > 0) {
            title.appendChild(types);
            site.setAttribute(TYPE_ATTR, episode.type);
        }

        addInfo(episode.source, site, 'Source:');
        addInfo(episode.name, site, 'Episode:');

        site.appendChild(withText(link('', episode.link), 'Open'));
        result.appendChild(site);
    }

    return result;
}

function addInfo(data, site, descriptionText) {
    if (data) {
        const info = div('info');
        const description = span('description', '', descriptionText);
        const value = span('value', data, data);
        info.appendChild(description);
        info.appendChild(value);
        site.appendChild(info);
    }
}

function buildBadge(title, text = '') {
    return withText(withTitle(div('badge'), title), text);
}

function buildDubSvg() {
    return svg('', '0 0 384 512', 'M192 0C139 0 96 43 96 96l0 160c0 53 43 96 96 96s96-43 96-96l0-160c0-53-43-96-96-96zM64 216c0-13.3-10.7-24-24-24s-24 10.7-24 24l0 40c0 89.1 66.2 162.7 152 174.4l0 33.6-48 0c-13.3 0-24 10.7-24 24s10.7 24 24 24l72 0 72 0c13.3 0 24-10.7 24-24s-10.7-24-24-24l-48 0 0-33.6c85.8-11.7 152-85.3 152-174.4l0-40c0-13.3-10.7-24-24-24s-24 10.7-24 24l0 40c0 70.7-57.3 128-128 128s-128-57.3-128-128l0-40z');
}

function buildSubSvg() {
    return svg('', '0 0 576 512', 'M0 96C0 60.7 28.7 32 64 32l448 0c35.3 0 64 28.7 64 64l0 320c0 35.3-28.7 64-64 64L64 480c-35.3 0-64-28.7-64-64L0 96zM200 208c14.2 0 27 6.1 35.8 16c8.8 9.9 24 10.7 33.9 1.9s10.7-24 1.9-33.9c-17.5-19.6-43.1-32-71.5-32c-53 0-96 43-96 96s43 96 96 96c28.4 0 54-12.4 71.5-32c8.8-9.9 8-25-1.9-33.9s-25-8-33.9 1.9c-8.8 9.9-21.6 16-35.8 16c-26.5 0-48-21.5-48-48s21.5-48 48-48zm144 48c0-26.5 21.5-48 48-48c14.2 0 27 6.1 35.8 16c8.8 9.9 24 10.7 33.9 1.9s10.7-24 1.9-33.9c-17.5-19.6-43.1-32-71.5-32c-53 0-96 43-96 96s43 96 96 96c28.4 0 54-12.4 71.5-32c8.8-9.9 8-25-1.9-33.9s-25-8-33.9 1.9c-8.8 9.9-21.6 16-35.8 16c-26.5 0-48-21.5-48-48z');
}

function buildRightArrowSvg() {
    return svg('', '0 0 640 640', 'M566.6 342.6C579.1 330.1 579.1 309.8 566.6 297.3L406.6 137.3C394.1 124.8 373.8 124.8 361.3 137.3C348.8 149.8 348.8 170.1 361.3 182.6L466.7 288L96 288C78.3 288 64 302.3 64 320C64 337.7 78.3 352 96 352L466.7 352L361.3 457.4C348.8 469.9 348.8 490.2 361.3 502.7C373.8 515.2 394.1 515.2 406.6 502.7L566.6 342.7z');
}

function buildDub() {
    const result = buildBadge(TYPE_DUB);
    result.appendChild(buildDubSvg());
    return result;
}

function buildSub() {
    const result = buildBadge(TYPE_SUB);
    result.appendChild(buildSubSvg());
    return result;
}

function buildCloseButton() {
    const result = button('close');
    const dValue = 'M183.1 137.4C170.6 124.9 150.3 124.9 137.8 137.4C125.3 149.9 125.3 170.2 137.8 182.7L275.2 320L137.9 457.4C125.4 469.9 125.4 490.2 137.9 502.7C150.4 515.2 170.7 515.2 183.2 502.7L320.5 365.3L457.9 502.6C470.4 515.1 490.7 515.1 503.2 502.6C515.7 490.1 515.7 469.8 503.2 457.3L365.8 320L503.1 182.6C515.6 170.1 515.6 149.8 503.1 137.3C490.6 124.8 470.3 124.8 457.8 137.3L320.5 274.7L183.1 137.4z';
    result.appendChild(svg('icon', '0 0 640 640', dValue));
    return result;
}

function getMainContainer() {
    return document.querySelector('main');
}

function renderErrorMessage(errorMessage) {
    const message = !errorMessage || errorMessage === '' ? GENERIC_ERROR_MESSAGE : errorMessage;
    renderMessage(message, 'error-message');
}

function renderMessage(message, extraClass = '') {
    const result = document.createElement('h1');
    withClass(result, 'message ' + extraClass);
    withText(result, message);
    removeElement('main > h1');
    removeElement('main > .anime-list');
    getMainContainer().appendChild(result);
}

function removeElement(selector) {
    const element = document.querySelector(selector);
    if (element) {
        element.remove();
    }
}
