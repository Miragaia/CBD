package cbd;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SistemaA {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private int limit = 4;
    private int timeslot = 10;

    public SistemaA(int limit, int timeslot) {
        this.mongoClient = MongoClients.create("mongodb://localhost:27017");
        this.database = mongoClient.getDatabase("atendimento");
        this.limit = limit;
        this.timeslot = timeslot;
    }

    public boolean isTimeslotExpired(String username) {
        MongoCollection<Document> timestampsCollection = database.getCollection("timestamps");
        Document timestampDocument = timestampsCollection.find(Filters.eq("username", username)).first();

        if (timestampDocument != null) {
            long timestamp = timestampDocument.getLong("timestamp");
            long currentTime = System.currentTimeMillis() / 1000;
            return (currentTime - timestamp) >= timeslot;
        }
        return false; // No previous timestamp for this user
    }

    public void solicitarProduto(String username, String product) {
        MongoCollection<Document> atendimentoCollection = database.getCollection("atendimento");

        if (isTimeslotExpired(username)) {
            System.out.println("Timeslot for user " + username + " has expired.");
            return;
        }

        Document userDocument = atendimentoCollection.find(Filters.eq("username", username)).first();

        if (userDocument == null) {
            Document newUserDocument = new Document("username", username)
                    .append("products", new ArrayList<String>());
            userDocument = newUserDocument;
        }

        List<String> products = userDocument.get("products", List.class);

        if (products.size() >= limit) {
            System.out.println("Limit of Products exceeded for user " + username);
            return;
        }

        products.add(product);

        atendimentoCollection.replaceOne(Filters.eq("username", username), userDocument);

        // Store the timestamp if it doesn't exist
        MongoCollection<Document> timestampsCollection = database.getCollection("timestamps");
        Document timestampDocument = timestampsCollection.find(Filters.eq("username", username)).first();
        long currentTime = System.currentTimeMillis() / 1000;

        if (timestampDocument == null) {
            timestampDocument = new Document("username", username)
                    .append("timestamp", currentTime);
            timestampsCollection.insertOne(timestampDocument);
        }
    }

    public static void main(String[] args) {
        int limit = 4;
        int timeslot = 10;
        SistemaA sistema = new SistemaA(limit, timeslot);
        Scanner scanner = new Scanner(System.in);

        try {
            while (true) {
                System.out.print("Username (or type 'exit' to exit the app): ");
                String username = scanner.nextLine();

                if (username.equalsIgnoreCase("exit")) {
                    System.out.println("It is all for now!");
                    break;
                }

                System.out.print("Product: ");
                String product = scanner.nextLine();

                sistema.solicitarProduto(username, product);
                System.out.println("Pedido registrado com sucesso.");
            }
        } finally {
            sistema.mongoClient.close();
            scanner.close();
        }
    }
}

