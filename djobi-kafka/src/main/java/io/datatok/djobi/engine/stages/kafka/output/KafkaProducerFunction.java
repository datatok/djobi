package io.datatok.djobi.engine.stages.kafka.output;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.spark.util.LongAccumulator;
import scala.Serializable;
import scala.collection.Iterator;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaProducerFunction<T> extends AbstractFunction1<Iterator<T>, BoxedUnit> implements Serializable {

    private static final long serialVersionUID = -1919222653470217466L;

    final private Properties kafkaProperties;

    final private String topic;

    final private LongAccumulator messagesSent;

    KafkaProducerFunction(final LongAccumulator messagesAccumulator, final Properties properties, final String topic) {
        super();

        this.kafkaProperties = properties;
        this.topic = topic;
        this.messagesSent = messagesAccumulator;
    }

    @Override
    public BoxedUnit apply(Iterator<T> iterator) {

        final Producer<String, String> producer = new KafkaProducer<>(kafkaProperties);

        while (iterator.hasNext()) {
            final T row = iterator.next();

            try {
                producer.send(new ProducerRecord<>(topic, row instanceof String ? (String) row : row.toString())).get();
                this.messagesSent.add(1);
            } catch(ExecutionException e) {
                e.printStackTrace();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        producer.flush();

        producer.close();

        return null;
    }
}
