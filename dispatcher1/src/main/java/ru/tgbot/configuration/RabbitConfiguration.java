package ru.tgbot.configuration;

import lombok.AllArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tgbot.service.RabbitQueue;

@Configuration
@AllArgsConstructor
public class RabbitConfiguration {
    private final RabbitQueue rabbitQueue;
    @Bean
    public MessageConverter jsonMessageConverter() {
        return  new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue textMessageQueue() {
        return new Queue(RabbitQueue.TEXT_MESSAGE_UPDATE);
    }
    @Bean
    public Queue doctMessageQueue() {
        return new Queue(RabbitQueue.DOC_MESSAGE_UPDATE);
    }
    @Bean
    public Queue photoMessageQueue() {
        return new Queue(RabbitQueue.PHOTO_MESSAGE_UPDATE);
    }
    @Bean
    public Queue answerMessageQueue() {
        return new Queue(RabbitQueue.ANSWER_MESSAGE);
    }

}
