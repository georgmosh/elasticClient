import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.*;

import java.io.IOException;

/**
 * Indexing.java
 * A class indexing a collection of JSON elements to the "documents" Lucene index.
 * Actually represents an ElasticSearch client (https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/index.html).
 * @author Georgios M. Moschovis (p3150113@aueb.gr)
 */
public class Indexing {

    /**
     * Overloaded constructor.
     */
    public Indexing() {
        CollectionDAO.SetClient(new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http"))));
        CreateDocsIndex();
    }

    /**
     * A method creating the "documents" Lucene index; to store all generated JSON elements.
     */
    public void CreateDocsIndex() {
        try {
            CreateIndexRequest request = new CreateIndexRequest("documents");
            CreateIndexResponse createIndexResponse = CollectionDAO.GetClient().indices().create(request, RequestOptions.DEFAULT);
            GetIndexRequest getRequest = new GetIndexRequest(); getRequest.indices("documents"); // Testing the completion of the "documents" index creation.
            boolean flag = createIndexResponse.isAcknowledged() && CollectionDAO.GetClient().indices().exists(getRequest, RequestOptions.DEFAULT);
            if(flag) System.out.println("Lucene index \"documents\" created successfully!");
                    else System.out.println("Error creating the \"documents\" index!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method storing all generated JSON elements to "documents" index; to retrieve them later on.
     * Uses the TransportClient for the ElasticSearch Lucene index, labeled "documents", shutting down its operation at the end of the process.
     *
     */
    public void EvaluateSearchData() {
        System.out.println("Starting analyzing the collection data!\n");
           this.AnalyzeCollectionData();
        System.out.println("Starting indexing the collection data!\n");
           this.IndexCollectionData();
    }

    /**
     * A method storing all generated JSON format content elements to "documents" index.
     */
    public void IndexCollectionData() {
        for(Integer doc = 0; doc < CollectionDAO.GetDataAnalyzed().size(); doc++) {
            try {
                String currentElement = CollectionDAO.GetDataAnalyzed().get(doc).getYValue();
                IndexRequest request = new IndexRequest("documents", "doc", doc.toString());
                request.source(currentElement, XContentType.JSON); // The JSON format context after analyzing.
                CollectionDAO.GetClient().index(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A method analyzing all indexed JSON format data to the "documents" index mentioned above. The English analyzer is really just Lucene¢s EnglishAnalyzer.
     * As a built-in language analyzers are available globally and need not be configured before being used. Lucene EnglishAnalyzer class uses following components:
     *   TOKENIZER: Standard tokenizer
     *   TOKEN FILTERS:
     *      a) Standard token filter
     *      b) English possessive filter, which removes trailings from words
     *      c) Lowercase token filter
     *      d) Stop token filter
     *      e) Keyword marker filter, which protects certain tokens from modification by stemmers
     *      f) Porter stemmer filter, which reduces words down to a base form (?stem?)
     */
    public void AnalyzeCollectionData() {
        CollectionDAO.InitializeContainerAnalyzer();
        for (int doc = 0; doc < CollectionDAO.GetDataJSON().size(); doc++) {
            try {
                /*
                 * Splitting the text to perform analysis.
                 */
                String splitter1 = "\"text\":\"", splitter2 = "\"}}", analyzedText = "", currentElement = CollectionDAO.GetDataJSON().get(doc).getYValue();
                String[] splittedElem1 = currentElement.split(splitter1), splittedElem2 = splittedElem1[1].split(splitter2);
                AnalyzeRequest request = new AnalyzeRequest();
                request.text(splittedElem2[0]);
                request.analyzer("english");

                /*
                 * Restoring the analyzed context to the JSON for indexing.
                 */
                AnalyzeResponse response = CollectionDAO.GetClient().indices().analyze(request, RequestOptions.DEFAULT);
                for(int token = 0; token < response.getTokens().size(); token++)
                        analyzedText += (response.getTokens().get(token).getTerm() + " ");
                analyzedText.trim(); // Eliminating whitespace at the end of the analyzed element.
                CollectionDAO.GetDataAnalyzed().add(
                        new Vec2<String, String>(CollectionDAO.GetDataJSON().get(doc).getTValue(), splittedElem1[0] + splitter1 + analyzedText + splitter2));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}