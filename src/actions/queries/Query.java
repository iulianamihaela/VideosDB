package actions.queries;

import common.Constants;
import fileio.ActionInputData;
import fileio.Writer;
import main.Database;
import org.json.simple.JSONObject;

public final class Query {
    private Query() { }

    /**
     * Process a query command
     *
     * @param actionInput action input
     * @param writer      output writer
     * @return result
     */
    public static JSONObject execute(final Database database,
                                     final ActionInputData actionInput,
                                     final Writer writer) {
        return switch (actionInput.getObjectType()) {
            case Constants.ACTORS -> ActorsQuery.execute(database, actionInput, writer);
            case Constants.MOVIES -> MoviesQuery.execute(database, actionInput, writer);
            case Constants.SHOWS -> ShowsQuery.execute(database, actionInput, writer);
            case Constants.USERS -> UsersQuery.execute(database, actionInput, writer);
            default -> new JSONObject();
        };
    }
}
