package cbd;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.Decimal128;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SistemaB {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private int totalLimit = 30; // Limite total em unidades de produto por janela temporal
    private int timeslot = 3600; // Janela temporal em segundos (60 minutos)

    public SistemaB(int totalLimit, int timeslot) {
        this.mongoClient = MongoClients.create("mongodb://localhost:27017");
        this.database = mongoClient.getDatabase("atendimento_b");
        this.totalLimit = totalLimit;
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
        return false;
    }

    public void solicitarProduto(String username, String product, int quantity) {
        MongoCollection<Document> atendimentoCollection = database.getCollection("atendimento");

        if (isTimeslotExpired(username)) {
            System.out.println("Timeslot for user " + username + " has expired.");
            return;
        }

        Document userDocument = atendimentoCollection.find(Filters.eq("username", username)).first();

        if (userDocument == null) {
            Document newUserDocument = new Document("username", username)
                    .append("products", new ArrayList<Document>());
            userDocument = newUserDocument;
        }

        List<Document> products = userDocument.get("products", List.class);

        int totalQuantity = 0;

        for (Document productDocument : products) {
            int q = productDocument.getInteger("quantity");
            totalQuantity += q;
        }

        if (totalQuantity + quantity > totalLimit) {
            System.out.println("Limit of Products exceeded for user " + username);
            return;
        }

        Document newProduct = new Document("product", product)
                .append("quantity", quantity);

        products.add(newProduct);

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
        int totalLimit = 30;
        int timeslot = 3600; // 60 minutos em segundos
        SistemaB sistema = new SistemaB(totalLimit, timeslot);
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
                System.out.print("Quantity: ");
                int quantity = scanner.nextInt(); // Leitura da quantidade
                scanner.nextLine();

                sistema.solicitarProduto(username, product, quantity);
                System.out.println("Pedido registrado com sucesso.");
            }
        } finally {
            sistema.mongoClient.close();
            scanner.close();
        }
    }
}
