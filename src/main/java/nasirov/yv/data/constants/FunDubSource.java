package nasirov.yv.data.constants;

import static java.util.Arrays.stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by nasirov.yv
 */
@RequiredArgsConstructor
public enum FunDubSource {
	ANIMEDIA("animedia"), NINEANIME("nineAnime");

	@Getter
	private final String name;

	public static FunDubSource getFunDubSourceByName(String name) {
		return stream(FunDubSource.values()).filter(x -> x.getName()
				.equals(name))
				.findFirst()
				.orElse(null);
	}
}
