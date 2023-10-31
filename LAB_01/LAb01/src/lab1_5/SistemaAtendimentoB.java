package lab1_5;

import redis.clients.jedis.Jedis;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;

public class SistemaAtendimentoB {

    private Jedis jedis;
    private int totalLimit = 30; // Limite total em unidades de produto por janela temporal
    private int timeslot = 3600; // Janela temporal em segundos (60 minutos)

    public SistemaAtendimentoB(int totalLimit, int timeslot) {
        jedis = new Jedis("localhost", 6379);
        // Limpar todas as chaves
        jedis.flushAll();
        this.totalLimit = totalLimit;
        this.timeslot = timeslot;
    }

    public boolean isTimeslotExpired(String username) {
        String timestampString = jedis.hget("timestamps", username);
        if (timestampString != null) {
            long timestamp = Long.parseLong(timestampString);
            long currentTime = System.currentTimeMillis() / 1000;
            return (currentTime - timestamp) >= timeslot;
        }
        return false; 
    }

    public void solicitarProduto(String username, String product, int quantity) {
        String chave = "atendimento";
        Map<String, String> usuarioProdutos = jedis.hgetAll(chave);

        // Criar um novo mapa modificável
        Map<String, String> newUsuarioProdutos = new HashMap<>(usuarioProdutos);
        System.out.println("newUsuarioProdutos: " + newUsuarioProdutos);

        long currentTime = System.currentTimeMillis() / 1000;

        if (!newUsuarioProdutos.containsKey(username)) {
            newUsuarioProdutos.put(username, product + ":" + quantity);
            
            jedis.hset("timestamps", username, String.valueOf(currentTime));
        } else {
            String produtosDoUsuario = newUsuarioProdutos.get(username);
            String[] produtos = produtosDoUsuario.split(",");
            int totalQuantity = 0;

            for (String produto : produtos) {
                String[] parts = produto.split(":");
                if (parts.length == 2) {
                    int q = Integer.parseInt(parts[1]);
                    totalQuantity += q;
                }
            }

            if (totalQuantity + quantity > totalLimit) {
                System.out.println("Limit of Products exceded by user " + username);
                throw new RuntimeException(" " + username);
            }

            produtosDoUsuario += "," + product + ":" + quantity;
            newUsuarioProdutos.put(username, produtosDoUsuario);
        }

        // Atualizar o Redis Hash com o mapa modificado
        jedis.hmset(chave, newUsuarioProdutos);
    }

    public static void main(String[] args) {
        int totalLimit = 30;
        int timeslot = 3600; // 60 minutos em segundos
        SistemaAtendimentoB sistema = new SistemaAtendimentoB(totalLimit, timeslot);
        Scanner scanner = new Scanner(System.in);

        try (PrintWriter outputFile = new PrintWriter(new FileWriter("LAb01/src/lab1_5/CBD-15b-out.txt"))) {
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

                System.out.print("Product: ");
                String product = scanner.nextLine();
                outputFile.println("User Input (Product): " + product);
                System.out.print("Quantity: ");
                int quantity = scanner.nextInt(); // Leitura da quantidade
                scanner.nextLine();
                outputFile.println("User Input (Quantity): " + quantity);

                sistema.solicitarProduto(username, product, quantity);
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
