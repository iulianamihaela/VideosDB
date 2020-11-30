package actor;

import fileio.ActorInputData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Actor {
  private String name;
  private String careerDescription;
  private HashMap<ActorsAwards, Integer> awards;
  private ArrayList<String> filmography;

  public Actor(
      final String name,
      final String careerDescription,
      final Map<ActorsAwards, Integer> awards,
      final ArrayList<String> filmography) {
    this.name = name;
    this.careerDescription = careerDescription;
    this.awards = new HashMap<>(awards);
    this.filmography = new ArrayList<>(filmography);
  }

  public Actor(final ActorInputData actorInput) {
    this(
        actorInput.getName(),
        actorInput.getCareerDescription(),
        actorInput.getAwards(),
        actorInput.getFilmography());
  }

  /**
   * Retrieves actor's awards <Award, Count>
   * @return awards map
   */
  public HashMap<ActorsAwards, Integer> getAwards() {
    return awards;
  }

  /**
   * Retrieves the number of actor's awards
   * @return number of awards
   */
  public Integer getAwardsCount() {
    return awards.values().stream().mapToInt(Integer::intValue).sum();
  }

  /**
   * Retrieve's actor's name
   * @return actor's name
   */
  public String getName() {
    return name;
  }
}
