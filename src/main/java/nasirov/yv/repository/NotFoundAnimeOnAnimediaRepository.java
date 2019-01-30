package nasirov.yv.repository;

import nasirov.yv.serialization.UserMALTitleInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Created by Хикка on 25.01.2019.
 */
@Repository
public interface NotFoundAnimeOnAnimediaRepository extends JpaRepository<UserMALTitleInfo, Integer> {
	@Query(value = "SELECT CASE WHEN COUNT(u) > 0 THEN 'true' ELSE 'false' END FROM UserMALTitleInfo u WHERE u.title = ?1")
	boolean exitsByTitle(String title);
}
