/*
   Author: Nasirov Yuriy
*/

$(function () {
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
		let lightTheme = theme === Themes.LIGHT;
		switchTheme(lightTheme);
		$('#switch-theme-checkbox')[0].checked = lightTheme;
	}
}

function switchTheme(lightTheme) {
	let body = $('body');
	body.toggleClass(Themes.LIGHT, lightTheme);
	body.toggleClass(Themes.DARK, !lightTheme);
}

function configSwitchThemeCheckbox() {
	$('#switch-theme-checkbox').on({
		change: function (e) {
			let lightTheme = e.target.checked;
			switchTheme(lightTheme);
			if (window.localStorage) {
				localStorage.setItem(THEME_COOKIE, lightTheme ? Themes.LIGHT : Themes.DARK);
			}
		}
	});
}
