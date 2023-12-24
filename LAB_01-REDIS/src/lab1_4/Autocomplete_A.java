package lab1_4;
import redis.clients.jedis.Jedis;
import java.io.*;
import java.util.*;



public class Autocomplete_A {
    public static void main(String[] args) {
        Jedis jedis = new Jedis();
        try (Scanner sc = new Scanner(new File("LAb01/src/lab1_4/names.txt")) ;
        
            PrintWriter outputFile = new PrintWriter(new FileWriter("LAb01/src/lab1_4/CBD-14a-out.txt"))) {
            while (sc.hasNext()) {
                String name = sc.next();
                jedis.sadd("names", name);
            }
            sc.close();

            Scanner search = new Scanner(System.in);
            while (true) {
                System.out.print("Search for ('Enter' for quit):");
                String search_name = search.nextLine();
                if(search_name.equals("")){
                    break;
                }

                outputFile.println("User Input: " + search_name);

                Set<String> results = searchAutocomplete(jedis, search_name);
                for (String result : results) {
                    System.out.println(result);
                    outputFile.println("Autocomplete Result: " + result);
                    outputFile.println();
                }

            }
            search.close();
            jedis.close();

        } catch (FileNotFoundException e) {
            System.err.println("File not found");
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Error writing to the output file");
            System.exit(1);
        }
    }

    private static Set<String> searchAutocomplete(Jedis jedis, String prefix){
        Set<String> autocompleteResults = new TreeSet<>();

        Set<String> names = jedis.smembers("names");

        for (String name : names) {
            if (name.startsWith(prefix)) {
                autocompleteResults.add(name);
            }
        }

        return autocompleteResults;
    }
}
