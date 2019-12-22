package nasirov.yv.service;

import java.util.Set;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;

/**
 * Created by nasirov.yv
 */
public interface AnimediaServiceI {

	Set<AnimediaSearchListTitle> getAnimediaSearchListFromAnimedia();

	Set<AnimediaSearchListTitle> getAnimediaSearchListFromGitHub();
}
