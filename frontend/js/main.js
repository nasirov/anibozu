const Themes = {DARK: 'dark', LIGHT: 'light'};
const THEME_ATTR = 'theme';
const THEME_COOKIE = 'theme';

document.addEventListener('DOMContentLoaded', () => {
	switchTheme();
	configureSwitchContainerEvents();
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
	if (theme == null && window.matchMedia) {
		theme = window.matchMedia('(prefers-color-scheme: light)').matches ? Themes.LIGHT : Themes.DARK;
	}
	if (theme != null) {
		setTheme(theme);
	}
}

function configureSwitchContainerEvents() {
	const switchContainer = document.querySelector('.switch-container');
	const clickType = 'click';
	const keypressEventType = 'keypress';
	[clickType, keypressEventType].forEach(type => {
				switchContainer.addEventListener(type, function (e) {
					if (e.type === clickType || (e.type === keypressEventType && e.key === 'Enter')) {
						const currentTheme = document.documentElement.getAttribute(THEME_ATTR);
						const nextTheme = currentTheme === Themes.LIGHT ? Themes.DARK : Themes.LIGHT;
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
	document.documentElement.setAttribute(THEME_ATTR, theme);
}
