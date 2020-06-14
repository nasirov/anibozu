package nasirov.yv.data.constants;

import static java.util.Arrays.stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by nasirov.yv
 */
@RequiredArgsConstructor
public enum FanDubSource {

	ANIMEDIA("animedia"), NINEANIME("nineAnime"), ANIDUB("anidub"), JISEDAI("jisedai"), ANIMEPIK("animepik");

	@Getter
	private final String name;

	public static FanDubSource getFanDubSourceByName(String name) {
		return stream(FanDubSource.values()).filter(x -> x.getName()
				.equals(name))
				.findFirst()
				.orElse(null);
	}
}
