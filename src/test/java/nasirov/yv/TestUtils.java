package nasirov.yv;

import static nasirov.yv.AbstractTest.FAIRY_TAIL_ROOT_URL;
import static nasirov.yv.AbstractTest.SAO_ROOT_URL;
import static nasirov.yv.AbstractTest.TITANS_ROOT_URL;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import nasirov.yv.data.constants.BaseConstants;

/**
 * Created by nasirov.yv
 */
public class TestUtils {

	public static List<String> getEpisodesRange(String min, String max) {
		List<String> result = new LinkedList<>();
		for (int i = Integer.parseInt(min); i <= Integer.parseInt(max); i++) {
			result.add(String.valueOf(i));
		}
		return result;
	}

	public static <T extends Collection> T getMultiSeasonsReferencesList(Class<T> collection, boolean updated)
			throws IllegalAccessException, InstantiationException {
		T refs = collection.newInstance();
		AnimediaMALTitleReferences fairyTail1 = AnimediaMALTitleReferences.builder().url(FAIRY_TAIL_ROOT_URL).dataList("1").firstEpisode("1")
				.titleOnMAL("fairy tail").build();
		AnimediaMALTitleReferences fairyTail2 = AnimediaMALTitleReferences.builder().url(FAIRY_TAIL_ROOT_URL).dataList("2").firstEpisode("176")
				.titleOnMAL("fairy tail (2014)").build();
		AnimediaMALTitleReferences fairyTail3 = AnimediaMALTitleReferences.builder().url(FAIRY_TAIL_ROOT_URL).dataList("3").firstEpisode("278")
				.titleOnMAL("fairy tail: final series").build();
		AnimediaMALTitleReferences fairyTail7 = AnimediaMALTitleReferences.builder().url(FAIRY_TAIL_ROOT_URL).dataList("7").firstEpisode("1")
				.titleOnMAL("fairy tail ova").build();
		AnimediaMALTitleReferences sao1 = AnimediaMALTitleReferences.builder().url(SAO_ROOT_URL).dataList("1").firstEpisode("1")
				.titleOnMAL("sword art online").build();
		AnimediaMALTitleReferences sao2 = AnimediaMALTitleReferences.builder().url(SAO_ROOT_URL).dataList("2").firstEpisode("1")
				.titleOnMAL("sword art online ii").build();
		AnimediaMALTitleReferences sao3 = AnimediaMALTitleReferences.builder().url(SAO_ROOT_URL).dataList("3").firstEpisode("1")
				.titleOnMAL("sword art online: alicization").build();
		AnimediaMALTitleReferences sao7 = AnimediaMALTitleReferences.builder().url(SAO_ROOT_URL).dataList("7").firstEpisode("1")
				.titleOnMAL("sword art online: extra edition").build();
		AnimediaMALTitleReferences none = AnimediaMALTitleReferences.builder().url("anime/velikiy-uchitel-onidzuka").dataList("1").firstEpisode("1")
				.titleOnMAL(BaseConstants.NOT_FOUND_ON_MAL).build();
		AnimediaMALTitleReferences onePunch7 = AnimediaMALTitleReferences.builder().url("anime/vanpanchmen").dataList("7").firstEpisode("1")
				.titleOnMAL("one punch man specials").minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("6")
				.minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("6").currentMax("6").build();
		AnimediaMALTitleReferences onePunch7_2 = AnimediaMALTitleReferences.builder().url("anime/vanpanchmen").dataList("7").firstEpisode("7")
				.titleOnMAL("one punch man: road to hero").minConcretizedEpisodeOnAnimedia("7").maxConcretizedEpisodeOnAnimedia("7")
				.minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("1").currentMax("7").build();
		AnimediaMALTitleReferences titans3ConcretizedAndOngoing = AnimediaMALTitleReferences.builder().url(TITANS_ROOT_URL).dataList("3")
				.firstEpisode("13").titleOnMAL("shingeki no kyojin season 3 part 2").minConcretizedEpisodeOnAnimedia("13")
				.maxConcretizedEpisodeOnAnimedia("22").minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("10").build();
		if (updated) {
			fairyTail1.setCurrentMax("175");
			fairyTail2.setCurrentMax("277");
			fairyTail3.setCurrentMax("294");
			fairyTail7.setCurrentMax("4");
			fairyTail1.setFirstEpisode("1");
			fairyTail2.setFirstEpisode("176");
			fairyTail3.setFirstEpisode("278");
			fairyTail7.setFirstEpisode("1");
			fairyTail1.setMinConcretizedEpisodeOnAnimedia("1");
			fairyTail2.setMinConcretizedEpisodeOnAnimedia("176");
			fairyTail3.setMinConcretizedEpisodeOnAnimedia("278");
			fairyTail7.setMinConcretizedEpisodeOnAnimedia("1");
			fairyTail1.setMaxConcretizedEpisodeOnAnimedia("175");
			fairyTail2.setMaxConcretizedEpisodeOnAnimedia("277");
			fairyTail3.setMaxConcretizedEpisodeOnAnimedia("xxx");
			fairyTail7.setMaxConcretizedEpisodeOnAnimedia("4");
			fairyTail1.setEpisodesRange(getEpisodesRange("1", "175"));
			fairyTail2.setEpisodesRange(getEpisodesRange("176", "277"));
			fairyTail3.setEpisodesRange(getEpisodesRange("278", "294"));
			fairyTail7.setEpisodesRange(getEpisodesRange("1", "4"));
			sao1.setCurrentMax("25");
			sao2.setCurrentMax("24");
			sao3.setCurrentMax("16");
			sao7.setCurrentMax("1");
			sao1.setFirstEpisode("1");
			sao2.setFirstEpisode("1");
			sao3.setFirstEpisode("1");
			sao7.setFirstEpisode("1");
			sao1.setMinConcretizedEpisodeOnAnimedia("1");
			sao2.setMinConcretizedEpisodeOnAnimedia("1");
			sao3.setMinConcretizedEpisodeOnAnimedia("1");
			sao7.setMinConcretizedEpisodeOnAnimedia("1");
			sao1.setMaxConcretizedEpisodeOnAnimedia("25");
			sao2.setMaxConcretizedEpisodeOnAnimedia("24");
			sao3.setMaxConcretizedEpisodeOnAnimedia("24");
			sao7.setMaxConcretizedEpisodeOnAnimedia("1");
			sao1.setEpisodesRange(getEpisodesRange("1", "25"));
			sao2.setEpisodesRange(getEpisodesRange("1", "24"));
			sao3.setEpisodesRange(getEpisodesRange("1", "16"));
			sao7.setEpisodesRange(getEpisodesRange("1", "1"));
			titans3ConcretizedAndOngoing.setCurrentMax("13");
		}
		refs.add(fairyTail1);
		refs.add(fairyTail2);
		refs.add(fairyTail3);
		refs.add(fairyTail7);
		refs.add(sao1);
		refs.add(sao2);
		refs.add(sao3);
		refs.add(sao7);
		refs.add(none);
		refs.add(onePunch7);
		refs.add(onePunch7_2);
		refs.add(titans3ConcretizedAndOngoing);
		return refs;
	}

}
