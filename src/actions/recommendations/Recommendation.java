package actions.recommendations;

import common.Constants;
import fileio.ActionInputData;
import fileio.Writer;
import main.Database;
import org.json.simple.JSONObject;

public final class Recommendation {
    private Recommendation() { }

    /**
     * Process a recommendation
     *
     * @param database    database
     * @param actionInput action input data
     * @param writer      output writer
     * @return recommendation result as JSONObject
     */
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
