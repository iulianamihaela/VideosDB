package actions;

import actions.queries.Query;
import actions.recommendations.Recommendation;
import common.Constants;
import fileio.ActionInputData;
import fileio.Writer;
import main.Database;
import org.json.simple.JSONObject;

public final class Action {
    private Action() { }

    /**
     * Executes an action
     *
     * @param database    database
     * @param actionInput action input
     * @param writer      output writer
     * @return action result as JSONObject
     */
    public static JSONObject execute(final Database database,
                                     final ActionInputData actionInput,
                                     final Writer writer) {

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
