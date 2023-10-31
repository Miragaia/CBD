package lab1_3;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;

public class SimplePost_HashMap {
    private Jedis jedis;
	public static String USERS = "usersHashMap"; // Key set for users' name
	
	public SimplePost_HashMap() {
		this.jedis = new Jedis("localhost", 6379);
	}
 
	public void saveUser(Map<String,String> username) {
		jedis.hmset(USERS, username);
	}

	public Map<String,String> getUser() {
		return jedis.hgetAll(USERS);
	}       
	
	public Set<String> getAllKeys() {
		return jedis.keys("*");
	}
 
	public static void main(String[] args) {
		SimplePost_HashMap board = new SimplePost_HashMap();
        
		//limpar os valores usando flush
		board.jedis.flushAll();

        // set some users
        Map<String, String> userMap = new HashMap<>();
        userMap.put("key1", "Panela");
        userMap.put("key2", "Garfo");
        userMap.put("key3", "Copo");
        userMap.put("key4", "Tijela");
        
        board.saveUser(userMap);
        board.getAllKeys().stream().forEach(System.out::println);

        // Retrieve and print user information
        Map<String, String> users = board.getUser();
        for (Map.Entry<String, String> entry : users.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
		
	}
}
