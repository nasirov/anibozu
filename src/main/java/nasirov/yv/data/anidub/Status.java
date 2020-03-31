package nasirov.yv.data.anidub;

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
