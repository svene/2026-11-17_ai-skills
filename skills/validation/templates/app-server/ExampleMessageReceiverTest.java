package @PACKAGE@.feature.example;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Drives the example consumer through a real broker (Dev Services, needs Docker). Each case sends bad message(s),
 * then a valid one that still has to be accepted: a bad message must never stop the channel. Raw String serdes,
 * so the test can send what no well-behaved producer would.
 */
@QuarkusTest
class ExampleMessageReceiverTest {

    static final String TOPIC = "example";
    static final String DLQ = "example-dlq";

    static KafkaProducer<String, String> producer;
    static String bootstrap;

    @Inject
    ExampleHandler exampleHandler;

    @BeforeAll
    static void createProducer() {
        bootstrap = ConfigProvider.getConfig().getValue("kafka.bootstrap.servers", String.class);
        producer = new KafkaProducer<>(Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class));
    }

    @AfterAll
    static void closeProducer() {
        producer.close();
    }

    @Test
    void invalid_message_is_skipped_not_dead_lettered() throws Exception {
        send("{\"name\": \"kafka-invalid\", \"count\": \"abc\"}");
        send("{\"name\": \"kafka-valid\", \"count\": \"2\"}");

        awaitAccepted("kafka-valid");
        assertThat(exampleHandler.accepted()).extracting(Example::name).doesNotContain("kafka-invalid");
        assertThat(readDlq(r -> true, 0)).extracting(ConsumerRecord::value)
            .noneMatch(value -> value != null && value.contains("kafka-invalid"));
    }

    @Test
    void undeserializable_message_goes_to_dlq_and_channel_keeps_consuming() throws Exception {
        send("{not json");
        send("{\"name\": \"kafka-after-garbage\", \"count\": 1}");

        awaitAccepted("kafka-after-garbage");
        var dead = readDlq(r -> "{not json".equals(r.value()), 1);
        assertThat(header(dead.getFirst(), "dead-letter-topic")).isEqualTo(TOPIC);
        assertThat(header(dead.getFirst(), "dead-letter-reason")).isNotBlank();
    }

    @Test
    void tombstone_goes_to_dlq_and_channel_keeps_consuming() throws Exception {
        send(null);
        send("{\"name\": \"kafka-after-tombstone\", \"count\": 1}");

        awaitAccepted("kafka-after-tombstone");
        // The dead-letter queue's ObjectMapperSerializer writes the null payload as JSON "null", not as a tombstone.
        var dead = readDlq(r -> "null payload (tombstone)".equals(header(r, "dead-letter-reason")), 1);
        assertThat(dead.getFirst().value()).isEqualTo("null");
    }

    private static void send(String value) throws Exception {
        producer.send(new ProducerRecord<>(TOPIC, value)).get();
    }

    private void awaitAccepted(String name) {
        await().atMost(30, SECONDS).untilAsserted(() ->
            assertThat(exampleHandler.accepted()).extracting(Example::name).contains(name));
    }

    /**
     * The dead-letter records that match, once at least {@code expected} do; with {@code expected} 0, everything
     * that matches within a few seconds.
     */
    private static List<ConsumerRecord<String, String>> readDlq(Predicate<ConsumerRecord<String, String>> matching, int expected) {
        try (var consumer = new KafkaConsumer<String, String>(Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
            ConsumerConfig.GROUP_ID_CONFIG, "dlq-reader-" + UUID.randomUUID(),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class))) {
            consumer.subscribe(List.of(DLQ));
            var records = new ArrayList<ConsumerRecord<String, String>>();
            long deadline = System.nanoTime() + Duration.ofSeconds(expected == 0 ? 3 : 30).toNanos();
            while (System.nanoTime() < deadline && (expected == 0 || records.size() < expected)) {
                consumer.poll(Duration.ofMillis(500)).forEach(r -> {
                    if (matching.test(r)) {
                        records.add(r);
                    }
                });
            }
            assertThat(records).hasSizeGreaterThanOrEqualTo(expected);
            return records;
        }
    }

    private static String header(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
