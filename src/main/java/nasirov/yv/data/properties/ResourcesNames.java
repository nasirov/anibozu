package nasirov.yv.data.properties;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Created by nasirov.yv
 */

@ConfigurationProperties(prefix = "application.resources")
@Configuration
@Validated
@Data
public class ResourcesNames {

	@NotBlank
	private String multiSeasonsAnimeUrls;

	@NotBlank
	private String singleSeasonsAnimeUrls;

	@NotBlank
	private String announcementsUrls;

	@NotBlank
	private String applicationLogo;

	@NotBlank
	private String tempFolder;

	@NotBlank
	private String tempRawReferences;

	@NotBlank
	private String tempNewTitlesInAnimediaSearchList;

	@NotBlank
	private String tempRemovedTitlesFromAnimediaSearchList;

	@NotBlank
	private String tempDuplicatedUrlsInAnimediaSearchList;

	@NotBlank
	private String tempSingleSeasonTitlesWithCyrillicKeywordsInAnimediaSearchList;

	@NotBlank
	private String tempReferencesWithInvalidMALTitleName;

	@NotBlank
	private String tempSearchTitlesWithInvalidMALTitleName;

}
