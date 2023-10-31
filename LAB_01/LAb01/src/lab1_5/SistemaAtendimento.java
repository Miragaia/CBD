package lab1_5;

import redis.clients.jedis.Jedis;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;

public class SistemaAtendimento {

    private Jedis jedis;
    private int limit = 4;
    private int timeslot = 10;

    public SistemaAtendimento(int limit, int timeslot) {
        jedis = new Jedis("localhost", 6379);
        // Clear all keys
        jedis.flushAll();
        this.limit = limit;
        this.timeslot = timeslot;
    }

    public boolean isTimeslotExpired(String username) {
        String timestampString = jedis.hget("timestamps", username);
        if (timestampString != null) {
            long timestamp = Long.parseLong(timestampString);
            long currentTime = System.currentTimeMillis() / 1000;
            return (currentTime - timestamp) >= timeslot;
        }
        return false; // No previous timestamp for this user
    }

    public void solicitarProduto(String username, String product) {
        String chave = "atendimento";
        Map<String, String> usuarioProdutos = jedis.hgetAll(chave);

        // Create a new modifiable map
        Map<String, String> newUsuarioProdutos = new HashMap<>(usuarioProdutos);
        System.out.println("newUsuarioProdutos: " + newUsuarioProdutos);

        long currentTime = System.currentTimeMillis() / 1000;

        if (!newUsuarioProdutos.containsKey(username)) {
            newUsuarioProdutos.put(username, product);
            // Store the timestamp of the first entry for the user
            jedis.hset("timestamps", username, String.valueOf(currentTime));
        } else {
            String produtosDoUsuario = newUsuarioProdutos.get(username);
            String[] produtos = produtosDoUsuario.split(",");

            if (produtos.length >= limit) {
                System.out.println("Limit of Products exceded for user " + username);
                throw new RuntimeException(" " + username);
            }

            produtosDoUsuario += "," + product;
            newUsuarioProdutos.put(username, produtosDoUsuario);
        }

        // Update the Redis Hash with the modified map
        jedis.hmset(chave, newUsuarioProdutos);
    }

    public static void main(String[] args) {
        int limit = 4;
        int timeslot = 10; 
        SistemaAtendimento sistema = new SistemaAtendimento(limit, timeslot);
        Scanner scanner = new Scanner(System.in);

        try(PrintWriter outputFile = new PrintWriter(new FileWriter("LAb01/src/lab1_5/CBD-15a-out.txt"))) {
            while (true) {
                System.out.print("Username (or type 'exit' to exit the app): ");
                String username = scanner.nextLine();
                outputFile.println("User Input (Username / Exit): " + username);

                if (username.equalsIgnoreCase("exit")) {
                    System.out.println("It is all for now!");
                    outputFile.println("Machine Input: It is all for now!");
                    break;
                }

                if (sistema.isTimeslotExpired(username)) {
                    System.out.println("Timeslot for user " + username + " has expired.");
                    outputFile.println("Machine Input: Timeslot for user " + username + " has expired.");
                    continue;
                }

                System.out.print("Produto: ");
                String product = scanner.nextLine();
                outputFile.println("User Input (Product): " + product);
                

                sistema.solicitarProduto(username, product);
                System.out.println("Pedido registrado com sucesso.");
                outputFile.println("Machine Input: Pedido registrado com sucesso.");
            }
        } catch (IOException e) {
            System.err.println("Error writing to the output file");
            System.exit(1);
        } finally {
            sistema.jedis.close();
            scanner.close();
        }
    }
}
