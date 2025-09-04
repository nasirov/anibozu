package nasirov.yv.anibozu.dto.user;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

@Builder
public record Episodes(int dub, int sub, @NotNull List<Episode> list) {

	@Builder
	public record Episode(@NotNull String site, @NotNull String siteName, @NotNull String type, @NotNull String source, @NotNull String link,
												@NotNull String name) {}
}
