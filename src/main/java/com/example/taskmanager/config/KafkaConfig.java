package com.example.taskmanager.config;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.kafka.MessageDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfig {

    private String groupId;
    private String servers;
    private String sessionTimeout;
    private String maxPartitionFetchBytes;
    private String maxPollRecords;
    private String maxPollIntervalsMs;
    private String clientTopic;

    @Bean
    public ConsumerFactory<String, TaskDto> consumerListenerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers); //сервер
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); // консьюмер группа
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // кто будет десериализировать ключ
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MessageDeserializer.class); // кто будет десериализировать value
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.taskmanager.dto.TaskDto"); //во что маппим? где это взять
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

    ConcurrentKafkaListenerContainerFactory<String, TaskDto> kafkaListenerContainerFactory(@Qualifier("consumerListenerFactory") ConsumerFactory<String, TaskDto> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, TaskDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
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
        return null;
    }
}
