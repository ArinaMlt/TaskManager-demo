package com.example.taskmanager.config;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskStatusDto;
import com.example.taskmanager.kafka.KafkaTaskStatusProducer;
import com.example.taskmanager.kafka.MessageDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfigTaskStatus {

    @Value("t1-demo")
    private String groupId;
    @Value("localhost:9092")
    private String servers;
    @Value("${t1.kafka.session.timeout.ms:15000}")
    private String sessionTimeout;
    @Value("${t1.kafka.max.partition.fetch.bytes:300000}")
    private String maxPartitionFetchBytes;
    @Value("${t1.kafka.max.poll.records:1}")
    private String maxPollRecords;
    @Value("${t1.kafka.max.poll.interval.ms:3000}")
    private String maxPollIntervalsMs;
    @Value("task_status_default")
    private String taskTopic;

    @Bean
    public ConsumerFactory<String, TaskStatusDto> taskStatusDtoConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers); //сервер
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "_taskDto"); // консьюмер группа
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // кто будет десериализировать ключ
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MessageDeserializer.class); // кто будет десериализировать value
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.taskmanager.dto.TaskStatusDto"); //во что маппим? где это взять
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // если маппим во что-то вне пакета - кафка не сделает этого
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // заголовки (конфиг проперти для использования заголовков)
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes); // максимальный размер сообщения, если не усложиться будет частично доставлено
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords); // сколько сообщений прочитать за один раз и коммит одного оффсета
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalsMs); // время сколько консьюмер может получать ответ от кафки и работать, если упал - считается что умер, в консюмер группе перебалансируется
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE); // консьюмер будет ли автоматически подтверждать смещение после обработки сообщения, выкл - делаем сами
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // начать с раннего сообщения
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, MessageDeserializer.class.getName()); // для ошибок свой десериализатор
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, MessageDeserializer.class); // для ошибок свой десериализатор

        DefaultKafkaConsumerFactory factory = new DefaultKafkaConsumerFactory<String, TaskDto>(props);
        factory.setKeyDeserializer(new StringDeserializer());

        return factory;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, TaskStatusDto> taskStatusDtoKafkaListenerContainerFactory(@Qualifier("taskStatusDtoConsumerFactory") ConsumerFactory<String, TaskStatusDto> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, TaskStatusDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factoryBuilder(consumerFactory, factory);
        return factory;
    }

    private <T> void factoryBuilder(ConsumerFactory<String, T> consumerFactory, ConcurrentKafkaListenerContainerFactory<String, T> factory) {
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(5000);
        factory.getContainerProperties().setMicrometerEnabled(true);
        factory.setCommonErrorHandler(errorHandler());
    }

    // для повторной попытки прочитать сообщения
    private CommonErrorHandler errorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(new FixedBackOff(1000, 3));
        handler.addNotRetryableExceptions(IllegalStateException.class);
        handler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.error("RetryListeners message = {}, offset = {} deliveryAttempt = {}",
                    ex.getMessage(), record.offset(), deliveryAttempt);
        });
        return handler;
    }

    @Bean("taskStatus")
    public KafkaTemplate<String, TaskStatusDto> kafkaTemplate(ProducerFactory<String, TaskStatusDto> producerPatFactory) {
        return new KafkaTemplate<>(producerPatFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "t1.kafka.producer.enable",
            havingValue = "true",
            matchIfMissing = true)
    public KafkaTaskStatusProducer producerClientTaskStatus(@Qualifier("taskStatus") KafkaTemplate template) {
        template.setDefaultTopic(taskTopic);
        return new KafkaTaskStatusProducer(template);
    }

    @Bean
    public ProducerFactory<String, TaskStatusDto> producerClientFactoryTaskStatus() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        return new DefaultKafkaProducerFactory<>(props);
    }
}
