package lab1_4;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.io.*;
import java.util.*;

public class Autocomplete_B {
    public static void main(String[] args) {
        Jedis jedis = new Jedis();

        //limpar os valores anteriores
        jedis.flushAll();

        Map<String, Integer> namesPopularity = loadNamesFromCSV("LAb01/src/lab1_4/nomes-pt-2021.csv");
        if (jedis.exists("names")) {
            jedis.del("names"); // Delete the existing key
        }
        for (Map.Entry<String, Integer> entry : namesPopularity.entrySet()) {
            //System.out.println(entry.getKey() + " - " + entry.getValue());
            jedis.zadd("names", entry.getValue(), entry.getKey());
        }

        Scanner search = new Scanner(System.in);
        while (true) {
            System.out.print("Search for ('Enter' for quit): ");
            String search_name = search.nextLine();
            if (search_name.equals("")) {
                break;
            }

            Set<String> results = searchAutocomplete(jedis, search_name);

            for (String result : results) {
                System.out.println(result);
            }
        }
        search.close();
        jedis.close();
    }

    private static Map<String, Integer> loadNamesFromCSV(String filename) {
        Map<String, Integer> namesPopularity = new HashMap<>();
        try (Scanner sc = new Scanner(new File(filename))) {
            String linha;
            while (sc.hasNextLine()) {
                linha = sc.nextLine();
                //System.out.println(linha);
                String[] combined = linha.split(";");
                //System.out.println(Arrays.toString(combined));
                String name = combined[0];
                int count = Integer.parseInt(combined[1]);
                namesPopularity.put(name, count);
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
            System.exit(0);
        }
        return namesPopularity;
    }   
    

    private static Set<String> searchAutocomplete(Jedis jedis, String prefix) {
        List<Tuple> autocompleteResultsWithScores = jedis.zrevrangeWithScores("names", 0, -1);
        //System.out.println(autocompleteResultsWithScores);
        Set<String> autocompleteResults = new LinkedHashSet<>(); // LinkedHashSet to preserve order

        for (Tuple tuple : autocompleteResultsWithScores) {
            String name = tuple.getElement().toLowerCase();
            if (name.startsWith(prefix)) {
                autocompleteResults.add(name);
            }
        }
        return autocompleteResults;
    }
    
}
