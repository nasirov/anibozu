/*
   Author: Nasirov Yuriy
*/

document.addEventListener('DOMContentLoaded', function () {
	setTheme();
	configSwitchThemeCheckbox();
});

const Themes = {DARK: 'dark', LIGHT: 'light'};
const THEME_COOKIE = 'theme';

/**
 * Theme setting priority:
 * 1. user's choice - {@link THEME_COOKIE}
 * 2. browser's default - media prefers-color-scheme
 */
function setTheme() {
	let theme;
	if (window.localStorage) {
		theme = localStorage.getItem(THEME_COOKIE);
	}
	if (theme == null && window.matchMedia) {
		theme = window.matchMedia('(prefers-color-scheme: light)').matches ? Themes.LIGHT : Themes.DARK;
	}
	if (theme != null) {
		const lightTheme = theme === Themes.LIGHT;
		switchTheme(lightTheme);
		document.querySelector('#switch-theme-checkbox').checked = lightTheme;
	}
}

function switchTheme(lightTheme) {
	document.documentElement.setAttribute('theme', lightTheme ? Themes.LIGHT : Themes.DARK);
}

function configSwitchThemeCheckbox() {
	document.querySelector('#switch-theme-checkbox').addEventListener('change',
			function (e) {
				const lightTheme = e.target.checked;
				switchTheme(lightTheme);
				if (window.localStorage) {
					localStorage.setItem(THEME_COOKIE, lightTheme ? Themes.LIGHT : Themes.DARK);
				}
			}
	);
}
