package @PACKAGE@.feature.example;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class ExampleMessageDeserializer extends ObjectMapperDeserializer<ExampleMessage> {

    public ExampleMessageDeserializer() {
        super(ExampleMessage.class);
    }
}
