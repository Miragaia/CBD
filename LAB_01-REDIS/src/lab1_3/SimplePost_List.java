package lab1_3;
import java.util.List;
import java.util.Set;
import redis.clients.jedis.Jedis;

public class SimplePost_List {

	private Jedis jedis;
	public static String USERS = "usersList"; // Key set for users' name
	
	public SimplePost_List() {
		this.jedis = new Jedis("localhost", 6379);
	}
 
	public void saveUser(String username) {
        jedis.lpush(USERS, username);
    }

    public List<String> getUsers() {    
        return jedis.lrange(USERS, 0, -1);
    }
	
	public Set<String> getAllKeys() {
        return jedis.keys("*");
    }
 
	public static void main(String[] args) {
		SimplePost_List board = new SimplePost_List();
		//limpar os valores usando flush
		board.jedis.flushAll();

		// set some users
		String[] users = { "Ana", "Pedro", "Maria", "Luis" };
		for (String user: users) {
            board.saveUser(user);
        }

        board.getUsers().stream().forEach(System.out::println);
		board.getAllKeys().stream().forEach(System.out::println);
        
	}
}



