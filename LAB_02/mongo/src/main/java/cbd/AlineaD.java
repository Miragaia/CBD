package cbd;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Filters;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.mongodb.client.model.Aggregates.group;

public class AlineaD {

    static MongoCollection<Document> collection;
    public static void main(String[] args) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("CBD");
            collection = database.getCollection("restaurants");

            // Contagem de localidades distintas
            int distinctLocalidades = countLocalidades(collection);
            System.out.println();
            System.out.println("Número de localidades distintas: " + distinctLocalidades);

            // Nome de restaurantes contendo 'Park' no nome
            List<String> restaurantesComParkNoNome = getRestWithNameCloserTo(collection, "Park");
            System.out.println();
            System.out.println("Nome de restaurantes contendo 'Park' no nome:");
            for (String nome : restaurantesComParkNoNome) {
                System.out.println("-> " + nome);
            }

            // Número de restaurantes por localidade
            System.out.println();
            System.out.println("Numero de restaurantes por localidade: ");
            Map<String, Integer> map = countRestByLocalidade();
            for (String entry : map.keySet()) {
                System.out.println("-> " + entry + " - " + map.get(entry));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int countLocalidades(MongoCollection<Document> collection) {
        // Utilize uma agregação para contar localidades distintas
        List<Document> pipeline = new ArrayList<>();
        pipeline.add(new Document("$group", new Document("_id", "$localidade")));
        List<Document> results = collection.aggregate(pipeline).into(new ArrayList<Document>());

        return results.size();
    }


    public static List<String> getRestWithNameCloserTo(MongoCollection<Document> collection, String name) {
        // Use uma consulta com filtro de texto para encontrar restaurantes com o nome próximo
        FindIterable<Document> restaurants = collection.find(Filters.text(name));
        List<String> restaurantesEncontrados = new ArrayList<>();
        for (Document restaurant : restaurants) {
            restaurantesEncontrados.add(restaurant.getString("nome"));
        }
        return restaurantesEncontrados;
    }
    
    public static Map<String, Integer> countRestByLocalidade(){
        Map<String, Integer> map = new HashMap<>();

        AggregateIterable<Document> documents = collection.aggregate(Arrays.asList(group("$localidade", Accumulators.sum("number_restaurants", 1))));

        for (Document doc : documents) {
            Object idObject = doc.get("_id");
            Object numRestaurantsObject = doc.get("number_restaurants");

            if (idObject != null && numRestaurantsObject != null) {
                map.put(idObject.toString(), numRestaurantsObject instanceof Integer ? (Integer) numRestaurantsObject : 0);
            }
        }

        return map;
    }
} 
