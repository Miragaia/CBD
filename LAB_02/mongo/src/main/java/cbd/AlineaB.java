package cbd;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;

import com.mongodb.client.FindIterable;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;


import java.util.Arrays;

import org.bson.Document;
import org.bson.conversions.Bson;


class Programa2{

    private MongoCollection<Document> collection;

     public Programa2() {
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

     public void PesquisarSemIndexs(){
        
        System.out.println("Pesquisa sem Indices");
        System.out.println();
        System.out.println("Pesquisa de resstaurantes com localidade Brooklyn e gastronomia Ice Cream, Gelato, Yogurt, Ices ou Chinese");

        long inicioSemIndex = System.nanoTime();

            Bson filtro = Filters.and(
                Filters.eq("localidade", "Brooklyn"),
                Filters.in("gastronomia", Arrays.asList("Ice Cream, Gelato, Yogurt, Ices", "Chinese"))
            );

            Bson projecao = Projections.fields(
                Projections.include("nome", "localidade", "gastronomia"),
                Projections.excludeId() 
            );

            FindIterable<Document> restaurantes = collection.find(filtro).projection(projecao);

            long fimSemIndex = System.nanoTime();
            long duracaosemIndex = fimSemIndex - inicioSemIndex;

            System.out.println("Duração nas pesquisas sem índices: " + duracaosemIndex);
    }
    

    public void PesquisarComIndexs() {
        
            try {
                collection.createIndex(Indexes.ascending("gastronomia"));
            } catch (Exception e) {
                System.err.println("Erro: Não foi possível criar um índice para gastronomia: " + e);
            }

            try {
                collection.createIndex(Indexes.ascending("localidade"));
            } catch (Exception e) {
                System.err.println("Erro: Não foi possível criar um índice para localidade: " + e);
            }

            try {
                collection.createIndex(Indexes.text(("nome")));
            } catch (Exception e) {
                System.err.println("Erro: Não foi possível criar um índice para nome: " + e);
            }

            System.out.println("Pesquisa com Indices");
            System.out.println();
            System.out.println("Pesquisa de resstaurantes com localidade Brooklyn e gastronomia Ice Cream, Gelato, Yogurt, Ices ou Chinese");

            long inicioComIndex = System.nanoTime();

            Bson filtro = Filters.and(
                Filters.eq("localidade", "Brooklyn"),
                Filters.in("gastronomia", Arrays.asList("Ice Cream, Gelato, Yogurt, Ices", "Chinese"))
            );

            Bson projecao = Projections.fields(
                Projections.include("nome", "localidade", "gastronomia"),
                Projections.excludeId() 
            );

            FindIterable<Document> restaurantes = collection.find(filtro).projection(projecao);

            long fimComIndex = System.nanoTime();
            long duracaocomIndex = fimComIndex - inicioComIndex;
            
            System.out.println("Duração nas pesquisas com índices: " + duracaocomIndex);
    }

}

public class AlineaB {
    
    public static void main( String[] args )
    {
        Programa2 programa = new Programa2();
        System.out.println();
        programa.PesquisarSemIndexs();
        System.out.println();
        programa.PesquisarComIndexs();
        System.out.println();
    }
}