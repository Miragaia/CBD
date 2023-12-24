package cbd;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.FindIterable;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;


class Programa {

    private MongoCollection<Document> collection;

     public Programa() {
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

    public void pesquisa(String nome){
         Document filtro = new Document("nome", nome);
        FindIterable<Document> resultados = collection.find(filtro);

        if (resultados != null) {
            MongoCursor<Document> cursor = resultados.iterator();

            while (cursor.hasNext()) {

                System.out.println();

                Document documento = cursor.next();
                System.out.println("Restaurante encontrado:");
                System.out.println("ID: " + documento.getObjectId("_id"));
                System.out.println("Nome: " + documento.getString("nome"));
                System.out.println("Gastronomia: " + documento.getString("gastronomia"));
                System.out.println("Localidade: " + documento.getString("localidade"));
                

                System.out.println();
            }

            cursor.close();
        } else {
            System.out.println("Erro: Nenhum restaurante chamado " + nome + "encontrado");
        }
    }


   public void inserir(String nome, String localidade, String gastronomia) {
    
        if (nome.isEmpty() | localidade.isEmpty() || gastronomia.isEmpty()) {
            System.out.println("Erro: Não é possível executar a ação, insira todos os dados (nome, localidade, gastronomia).");
            return; 
        }


        Bson filtroNomeGastronomiaExistente = Filters.and(
            Filters.eq("nome", nome),
            Filters.eq("gastronomia", gastronomia)
        );
        if (collection.countDocuments(filtroNomeGastronomiaExistente) > 0) {
            System.out.println("Erro: Impossível executar ação, já existe um restaurante com o mesmo nome.\n");
            return;
        }
    
        Document documento = new Document("nome", nome)
            .append("localidade", localidade)
            .append("gastronomia", gastronomia);

        
        collection.insertOne(documento);

        System.out.println("Sucesso: Restaurante inserido com sucesso!");
        System.out.println();
    }
    
    public void editar(String id, String novoNome, String novaLocalidade, String novaGastronomia) {
        
        if (id == null || id.isEmpty()) {
            System.out.println("Erro: Insira o ID do restaurante a ser editado.");
            return;
        }

        
        if (novoNome == null && novaLocalidade == null && novaGastronomia == null) {
            System.out.println("Erro: Insira os dados a serem atualizados. (nome, localidade, gastronomia)");
            return;
        }

        
        Bson restaurante_id = Filters.eq("_id", new ObjectId(id));

        
        Document updateDoc = new Document();
        updateDoc.append("nome", novoNome).append("localidade", novaLocalidade).append("novaGastronomia", novaLocalidade);
        
        UpdateResult resultado = collection.updateOne(restaurante_id, new Document("$set", updateDoc));

        if (resultado.getModifiedCount() > 0) {
            System.out.println("Sucesso: Restaurante editado com sucesso!");
            System.out.println();
        } else {
            System.out.println("Erro; Não exite um restaurante com esse ID.");
            System.out.println();
        }
    }


}


public class AlineaA 
{
    public static void main( String[] args )
    {
        Programa programa = new Programa();

        System.out.println("Pesquisa de restaurantes pelo nome:");
        programa.pesquisa("Kosher Island");
        programa.pesquisa("Riviera Caterer");
        System.out.println();
        System.out.println("Inserção de restaurante:");
        programa.inserir("Miragaia House", "Viseu", "Portuguese");
        System.out.println();
        System.out.println("Pesquisa do Restaurante inserido:");
        programa.pesquisa("Miragaia House");
        System.out.println();
        System.out.println("Tentativa de inserção de resturante sem o campo gastronomia:");
        programa.inserir("Cantina Santiago", "Aveiro","");
        System.out.println();
        System.out.println("Edicao de restaurante pelo ID:");
        programa.editar("653ce5da466e4f24161b3ada", "Vintage Burger", "Viseu", "Italian");
    }
}