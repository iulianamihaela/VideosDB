package actions.recommendations;

import common.Constants;
import fileio.ActionInputData;
import fileio.Writer;
import main.Database;
import org.json.simple.JSONObject;

public class Recommendation {
    public static JSONObject execute(final Database database,
                                     final ActionInputData actionInput,
                                     final Writer writer) {
        return switch (actionInput.getType()) {
            case Constants.STANDARD -> StandardRecommendation.execute(database,
                    actionInput,
                    writer);
            case Constants.BEST_UNSEEN -> BestUnseenRecommendation.execute(database,
                    actionInput,
                    writer);
            case Constants.POPULAR -> PopularRecommendation.execute(database,
                    actionInput,
                    writer);
            case Constants.FAVORITE -> FavoriteRecommendation.execute(database,
                    actionInput,
                    writer);
            case Constants.SEARCH -> SearchRecommendation.execute(database,
                    actionInput,
                    writer);
            default -> new JSONObject();
        };
    }
}
