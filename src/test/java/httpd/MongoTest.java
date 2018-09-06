package httpd;

import org.junit.Ignore;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoTest {

    @Test
    @Ignore
    public void t() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase db = mongoClient.getDatabase("learn");
        MongoCollection us = db.getCollection("unicorns");
        System.out.println(us.count());
        for (Object o : us.find()) {
            System.out.println(o);
        }
    }
}
