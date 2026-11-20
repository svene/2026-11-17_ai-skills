package org.acme.shop.feature.order;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Copied in by the skill test after the run. Each case sends a bad message, then a valid one that must still be
 * placed: a bad message never stops the channel. Invalid orders are skipped, garbage lands on "orders-dlq".
 */
@QuarkusTest
class OrderConsumerCheckTest {

    static KafkaProducer<String, String> producer;
    static String bootstrap;

    @Inject
    OrderHandler orderHandler;

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
    void valid_order_is_placed() throws Exception {
        send("{\"product\": \"check-valid\", \"quantity\": 2}");
        awaitPlaced("check-valid");
    }

    @Test
    void invalid_order_is_skipped_not_dead_lettered() throws Exception {
        send("{\"product\": \"check-invalid\", \"quantity\": 0}");
        send("{\"product\": \"check-after-invalid\", \"quantity\": 1}");

        awaitPlaced("check-after-invalid");
        assertThat(orderHandler.placed()).extracting(Order::product).doesNotContain("check-invalid");
        assertThat(readDlq(0)).noneMatch(value -> value != null && value.contains("check-invalid"));
    }

    @Test
    void garbage_goes_to_dlq_and_channel_keeps_consuming() throws Exception {
        send("{check garbage");
        send("{\"product\": \"check-after-garbage\", \"quantity\": 1}");

        awaitPlaced("check-after-garbage");
        assertThat(readDlq(1)).contains("{check garbage");
    }

    private static void send(String value) throws Exception {
        producer.send(new ProducerRecord<>("orders", value)).get();
    }

    private void awaitPlaced(String product) {
        await().atMost(30, SECONDS).untilAsserted(() ->
            assertThat(orderHandler.placed()).extracting(Order::product).contains(product));
    }

    /** The values on "orders-dlq": once {@code expected} have arrived, or after a few seconds when 0. */
    private static List<String> readDlq(int expected) {
        try (var consumer = new KafkaConsumer<String, String>(Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
            ConsumerConfig.GROUP_ID_CONFIG, "check-dlq-" + UUID.randomUUID(),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class))) {
            consumer.subscribe(List.of("orders-dlq"));
            var values = new ArrayList<String>();
            long deadline = System.nanoTime() + Duration.ofSeconds(expected == 0 ? 3 : 30).toNanos();
            while (System.nanoTime() < deadline && (expected == 0 || values.size() < expected)) {
                consumer.poll(Duration.ofMillis(500)).forEach(r -> values.add(r.value()));
            }
            assertThat(values).hasSizeGreaterThanOrEqualTo(expected);
            return values;
        }
    }
}
