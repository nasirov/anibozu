package nasirov.yv.data.fandub.anidub.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Status {

	UNKNOWN(0, "Не известно"), COMPLETED(1, "Вышел"), ONGOING(2, "Выходит"), ANNOUNCEMENT(3, "Анонс");

	@Getter
	private final Integer id;

	@Getter
	private final String name;
}
