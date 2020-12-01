package actions;

import actions.queries.Query;
import actions.recommendations.Recommendation;
import common.Constants;
import fileio.ActionInputData;
import fileio.Writer;
import main.Database;
import org.json.simple.JSONObject;

public class Action {
    public static JSONObject execute(Database database,
                                     ActionInputData actionInput,
                                     Writer writer) {

        return switch (actionInput.getActionType()) {
            case Constants.COMMAND -> Command.execute(database,
                    actionInput,
                    writer);
            case Constants.QUERY -> Query.execute(database,
                    actionInput,
                    writer);
            case Constants.RECOMMENDATION -> Recommendation.execute(database,
                    actionInput,
                    writer);
            default -> new JSONObject();
        };
    }
}
