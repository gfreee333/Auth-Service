package ru.bank.auth_service.config.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * <p><b>Defualt Producer Factory</b></p>
     * <p><b>Описание: настройки для Producer, для сообщений, которые мы можем потерять<br>
     * и не доставить, до конечного пользователя </b></p>
     * <p><b>Используется для:</b> Используем для не критичных сообщений</p>
     */
    @Bean
    @Primary
    public ProducerFactory<String, String> defualtProducerFactory(){
        Map<String, Object> configProducer = new HashMap<>();
        configProducer.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProducer.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProducer.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProducer.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "false");
        configProducer.put(ProducerConfig.ACKS_CONFIG, "1");
        configProducer.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProducer.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 30000);
        configProducer.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 15000);
        return new DefaultKafkaProducerFactory<>(configProducer);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, String> defualtKafkaTemplate(){
        return new KafkaTemplate<>(defualtProducerFactory());
    }

    /**
     * <p><b>Critical Producer Factory</b></p>
     * <p><b>Описание: Идемпотентный Producer для отправки важных сообщений в Kafka, <br>
     * которые мы должны доставить со 100% шансом</b></p>
     * <p><b>Используется для:</b> пароли, важные уведомления</p>
     */
    @Bean
    public ProducerFactory<String, String> criticalProducerFactory() {
        Map<String, Object> configProducer = new HashMap<>();
        configProducer.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProducer.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProducer.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProducer.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        configProducer.put(ProducerConfig.ACKS_CONFIG, "all");
        configProducer.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProducer.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        configProducer.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 60000);
        return new DefaultKafkaProducerFactory<>(configProducer);
    }

    @Bean(name = "criticalKafkaTemplate")
    public KafkaTemplate<String, String> criticalKafkaTemplate(){
        return new KafkaTemplate<>(criticalProducerFactory());
    }

}
