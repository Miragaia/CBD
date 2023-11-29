package cbd.lab3.ex3;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Row;

public class App 
{
    static Session session;

    public static void main( String[] args )
    {
        try {

            String serverIP = "127.0.0.1";
            String keyspace = "sistemavideos";

            Cluster cluster = Cluster.builder().addContactPoints(serverIP).build();

            session = cluster.connect(keyspace);

            System.out.println();
            System.out.println("Connected to Cassandra");
            System.out.println("========================================");
            System.err.println();
            System.out.println("Alinea A");
            AlineaA();
            System.out.println("========================================");
            System.err.println();
            System.out.println("Alinea B");
            AlineaB();
            System.out.println("========================================");

            if (session != null) {
                session.close();
            }
            cluster.close();
            

        } catch (Exception e) {
            System.err.println("Error connecting to Cassandra");
            System.exit(1);
        }
    }

    public static void AlineaA() {
        System.out.println();
        System.out.println("Inserting data");
        insertion();
        System.out.println();
        System.out.println("Selecting data from insertion:");
        System.out.println();
        search1();
        System.out.println();
        System.out.println("Updating data from insertion");
        update();
        System.out.println();
        System.out.println("Selecting data from update:");
        search2();
        System.out.println();
    }

    private static void AlineaB() {
        search1B();
    }

    public static void insertion() {
        session.execute("INSERT INTO videos (video_id, author_id, name, description, tags, upload_time) VALUES (938b2942-9ffd-47eb-a241-a92db9f40de7, b5c01858-1e56-48cc-99ba-024954d1a0c3, 'Miragaia Top 1 Video', 'Crossbar Challenge' , {'football', 'sports'} , '2023-11-29')");
        session.execute("INSERT INTO users (user_id, username, name, email, reg_time) VALUES (b5c01858-1e56-48cc-99ba-024954d1a0c3, 'miragaia10', 'Miragaia', 'miguelmiragaia@ua.pt', '2023-11-29')");
        session.execute("INSERT INTO comments (comment_id, video_id, author_id, comment_time, comment) VALUES (e1b442c2-62ea-42fc-905d-c9064ab0164b, 938b2942-9ffd-47eb-a241-a92db9f40de7, b5c01858-1e56-48cc-99ba-024954d1a0c3, '2023-11-29', 'Top 1 Video')");
    }

    public static void search1() {
        System.out.println("Video com id 938b2942-9ffd-47eb-a241-a92db9f40de7:");
        for (Row row : session.execute("SELECT * FROM videos WHERE video_id = 938b2942-9ffd-47eb-a241-a92db9f40de7")) {
            System.out.println(row.toString());
        }
        System.out.println();
        System.out.println("Utilizador com id b5c01858-1e56-48cc-99ba-024954d1a0c3:");
        for (Row row : session.execute("SELECT * FROM users WHERE user_id = b5c01858-1e56-48cc-99ba-024954d1a0c3")) {
            System.out.println(row.toString());
        }
        System.out.println();
        System.out.println("Comentario com id e1b442c2-62ea-42fc-905d-c9064ab0164b:");
        for (Row row : session.execute("SELECT * FROM comments WHERE comment_id = e1b442c2-62ea-42fc-905d-c9064ab0164b")) {
            System.out.println(row.toString());
        }
    }

    public static void update() {
        session.execute("UPDATE videos SET name = 'Miragaia Worst Video' WHERE video_id = 938b2942-9ffd-47eb-a241-a92db9f40de7");
        session.execute("UPDATE users SET name = 'Miguel' WHERE user_id = b5c01858-1e56-48cc-99ba-024954d1a0c3");
        session.execute("UPDATE comments SET comment = 'Top 1 Worst Videos' WHERE comment_id = e1b442c2-62ea-42fc-905d-c9064ab0164a");
    }

    public static void search2() {
        System.out.println("Video com id 938b2942-9ffd-47eb-a241-a92db9f40de7 (Name Update):");
        for (Row row : session.execute("SELECT * FROM videos WHERE video_id = 938b2942-9ffd-47eb-a241-a92db9f40de7")) {
            System.out.println(row.toString());
        }
        System.out.println();
        System.out.println("Utilizador com id b5c01858-1e56-48cc-99ba-024954d1a0c3 (Name Update):");
        for (Row row : session.execute("SELECT * FROM users WHERE user_id = b5c01858-1e56-48cc-99ba-024954d1a0c3")) {
            System.out.println(row.toString());
        }
        System.out.println();
        System.out.println("Comentario com id e1b442c2-62ea-42fc-905d-c9064ab0164b (Comment Update):");
        for (Row row : session.execute("SELECT * FROM comments WHERE comment_id = e1b442c2-62ea-42fc-905d-c9064ab0164b")) {
            System.out.println(row.toString());
        }
    }

    public static void search1B() {
        System.out.println();
        System.out.println("2. Lista das tags de determinado vídeo");
        for (Row r : session.execute("select tags from videos where video_id=8d87e4dd-a6b0-4328-a81d-fb1d64a90e47;")) {
            System.out.println(r.toString());
        }
        
        System.out.println();
        System.out.println("4. Os últimos 5 eventos de determinado vídeo realizados por um utilizador");
        for (Row r : session.execute("select * from events where user_id= 66205b1d-8634-4e8a-8d4a-c8736d359b6b and video_id = 8d87e4dd-a6b0-4328-a81d-fb1d64a90e47 ORDER BY event_timestamp limit 5;")) {
            System.out.println(r.toString());
        }

        System.out.println();
        System.out.println("7. Todos os seguidores (followers) de determinado vídeo");
        for (Row r : session.execute("select * from video_followers where video_id = 8d87e4dd-a6b0-4328-a81d-fb1d64a90e47;")) {
            System.out.println(r.toString());
        }

        System.out.println();
        System.out.println("12. Lista de videos e o seu rating medio associado");
        for (Row r : session.execute("SELECT video_id, avg_rating(user_ratings) AS avg_rating\n" + //
                "    FROM ratings\n" + //
                "    GROUP BY video_id")) {
            System.out.println(r.toString());
        }
    }
}
