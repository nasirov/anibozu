package nasirov.yv.data.anidub.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Category {

	UNKNOWN(0, "Не известно"), SERIES(1, "Сериал"), FILM(2, "Полнометражный фильм"), OVA(3, "OVA"), DORAMA(4, "Дорама");

	@Getter
	private final Integer id;

	@Getter
	private final String name;
}
