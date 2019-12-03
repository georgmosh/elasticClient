import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CollectionDAO.java
 * A class implementing a Data Access Object of the JSON elements for the "documents" Lucene index.
 * The query documents to compute similarities are also being stored to this DAO.
 * @author Georgios M. Moschovis (p3150113@aueb.gr)
 */
public class CollectionDAO {
    /**
     * Dynamic Containers to hold the JSON collection versions.
     */
    private static List<Vec2<String, String>> JSON_data_context, JSON_data_analyzed;

    /**
     * Dynamic Containers to hold the query documents versions.
     */
    private static List<Vec2<String, String>> QUERY_data_context, QUERY_data_analyzed;

    /**
     * The elastic search client; to enable access to the Lucene indexing mechanisms.
     */
    private static RestHighLevelClient client;

    /**
     * Initialize the dynamic container to hold the JSON collection's equivalent context.
     */
    public static void InitializeContainerJSON() {
        JSON_data_context = new ArrayList<Vec2<String, String>>();
    }

    /**
     * Initialize the dynamic container to hold the JSON collection's analyzed elements.
     */
    public static void InitializeContainerAnalyzer() {
        JSON_data_analyzed = new ArrayList<Vec2<String, String>>();
    }

    /**
     * Initialize the dynamic container to hold the JSON collection's equivalent context.
     */
    public static void InitializeContainerQuery() {
        QUERY_data_context = new ArrayList<Vec2<String, String>>();
    }

    /**
     * Initialize the dynamic container to hold the JSON collection's analyzed elements.
     */
    public static void InitializeContainerQAnalyzer() {
        QUERY_data_analyzed = new ArrayList<Vec2<String, String>>();
    }

    /**
     * Getter for the dynamic container storing the JSON collection's equivalent context.
     * @return The dynamic container storing the JSON context.
     */
    public static List<Vec2<String, String>> GetDataJSON() {
        return JSON_data_context;
    }

    /**
     * Getter for the dynamic container storing the JSON collection's analyzed elements.
     * @return The dynamic container storing the analyzed context.
     */
    public static List<Vec2<String, String>> GetDataAnalyzed() {
        return JSON_data_analyzed;
    }

    /**
     * Getter for the dynamic container storing the query documents to find similar ones.
     * @return The dynamic container storing the query documents.
     */
    public static List<Vec2<String, String>> GetQueries() {
        return QUERY_data_context;
    }

    /**
     * Getter for the dynamic container storing the query documents analyzed elements.
     * @return The dynamic container storing the analyzed queries.
     */
    public static List<Vec2<String, String>> GetQueriesAnalyzed() {
        return QUERY_data_analyzed;
    }

    /**
     * Setter for the elastic search client; facilitating the Lucene services through the APIs.
     * @param c The elastic search client to set.
     */
    public static void SetClient(RestHighLevelClient c) { client = c; }

    /**
     * Getter for the elastic search client; facilitating the Lucene services through the APIs.
     * @return The preset elastic search client.
     */
    public static RestHighLevelClient GetClient() { return client; }

    /**
     * Method shutting down the elastic search client; facilitating the Lucene services.
     */
    public static void ShutClient() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
