package cbd;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;

import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;


import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;


class SistemaC{

        private MongoCollection<Document> collection;

    public SistemaC(){
        try {
            MongoClient mongoCliente = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoCliente.getDatabase("CBD");
            collection = database.getCollection("restaurants");
        } catch (MongoException e) {
            e.printStackTrace();
            System.exit(1);
            collection=null;
        }
    }

    public void exercicio4(){

        try {
            
            Bson filtro = Filters.eq("localidade", "Bronx");

           
            long count = collection.countDocuments(filtro);

            System.out.println("Número de documentos com localidade 'Bronx': " + count);
        } catch (Exception e) {
            System.err.println("Erro ao executar a consulta: " + e);
        }

    }

    public void exercicio11(){
        try {
    
            Bson filtro = Filters.and(
                Filters.eq("localidade", "Bronx"),
                Filters.or(Filters.eq("gastronomia", "American"), Filters.eq("gastronomia", "Chinese"))
            );

            
            Bson projecao = Projections.fields(
                Projections.include("nome", "localidade", "gastronomia"),
                Projections.excludeId() 
            );

            FindIterable<Document> docs = collection.find(filtro).projection(projecao);

            
            for (Document d : docs) {
                System.out.println(d.toJson());
            }
        } catch (Exception e) {
            System.err.println("Erro ao executar a consulta: " + e);
        }

    }

    public void exercicio12(){
        try {
    
            Bson filtro = Filters.or(
                Filters.eq("localidade", "Brooklyn"),
                Filters.eq("localidade", "Queens"),
                Filters.eq("localidade", "Staten Island")
            );

            
            Bson projecao = Projections.fields(
                Projections.include("restaurant_id","nome", "localidade", "gastronomia"),
                Projections.excludeId() 
            );

            FindIterable<Document> docs = collection.find(filtro).projection(projecao);

            
            for (Document d : docs) {
                System.out.println(d.toJson());
            }
        } catch (Exception e) {
            System.err.println("Erro ao executar a consulta: " + e);
        }

    }

    public void exercicio13(){
        try {
    
            Bson filtro = Filters.not(Filters.elemMatch("grades", Filters.gt("score", 3)));

            
            Bson projecao = Projections.fields(
                Projections.include("nome","localidade", "gastronomia", "grades.score"),
                Projections.excludeId() 
            );

            FindIterable<Document> docs = collection.find(filtro).projection(projecao);

            
            for (Document d : docs) {
                System.out.println(d.toJson());
            }
        } catch (Exception e) {
            System.err.println("Erro ao executar a consulta: " + e);
        }

    }

    public void exercicio17() {
        try {
            Document projection = new Document("_id", 0)
                    .append("nome", 1)
                    .append("gastronomia", 1)
                    .append("localidade", 1);

            // Crie uma lista de critérios de ordenação
            List<Bson> sortCriteria = Arrays.asList(
                    Sorts.ascending("gastronomia"),
                    Sorts.descending("localidade")
            );

            // Realize a consulta e aplique a projeção e a ordenação
            FindIterable<Document> result = collection.find()
                    .projection(projection)
                    .sort(Sorts.orderBy(sortCriteria));

            for (Document d : result) {
                System.out.println(d.toJson());
            }
        } catch (Exception e) {
            System.err.println("Erro ao executar a consulta: " + e);
        }
    }
}



public class AlineaC {

    public static void main(String[] args) {
        
        SistemaC sistema = new SistemaC();
        System.out.println();
        System.out.println("Exercicio 4:");
        System.out.println("4. Indique o total de restaurantes localizados no Bronx.\n");
        sistema.exercicio4();
        System.out.println();
        System.out.println("Exercicio 11:");
        System.out.println("11. Liste o nome, a localidade e a gastronomia dos restaurantes que pertencem ao Bronx\n" + //
                "e cuja gastronomia é do tipo \"American\" ou \"Chinese\".\n");
        sistema.exercicio11();
        System.out.println();
        System.out.println("Exercicio 12:");
        System.out.println("Liste o restaurant_id, o nome, a localidade e a gastronomia dos restaurantes\n" + //
                "localizados em \"Staten Island\" , \"Queens\", ou \"Brooklyn\".\n");
        sistema.exercicio12();
        System.out.println();
        System.out.println("Exercicio 13:");
        System.out.println("13. Liste o nome, a localidade, o score e gastronomia dos restaurantes que alcançaram sempre pontuações inferiores ou igual a 3.\n");
        sistema.exercicio13();
        System.out.println();
        System.out.println("Exercicio 17:");
        System.out.println("17. Liste nome, gastronomia e localidade de todos os restaurantes ordenando por ordem\n" + //
                "crescente da gastronomia e, em segundo, por ordem decrescente de localidade.\n");
        sistema.exercicio17();
        System.out.println();
    }
    
}
