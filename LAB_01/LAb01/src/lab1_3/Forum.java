package lab1_3;
import redis.clients.jedis.Jedis;

public class Forum {
    public static void main(String[] args) {
        Jedis jedis = new Jedis();
        System.out.println(jedis.ping());
        System.out.println(jedis.info());
        jedis.set("chave1", "valor1");
        System.out.println("Valor da chave1: " + jedis.get("chave1"));
        //limpar os valores
        jedis.del("Clubes");
        jedis.rpush("Clubes", "Benfica");
        jedis.rpush("Clubes", "Sporting");
        jedis.rpush("Clubes", "Porto");

        System.out.println("");
        System.out.println("Elementos da lista de Clubes:");
        for (String elemento : jedis.lrange("Clubes", 0, -1)) {
            System.out.println(elemento);
        }
        jedis.close();
    }
}
