package ru.tgbot.configuration;

import static ru.tgbot.service.RabbitQueue.ANSWER_MESSAGE;
import static ru.tgbot.service.RabbitQueue.DOC_MESSAGE_UPDATE;
import static ru.tgbot.service.RabbitQueue.PHOTO_MESSAGE_UPDATE;
import static ru.tgbot.service.RabbitQueue.TEXT_MESSAGE_UPDATE;

import lombok.AllArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class RabbitConfiguration {
    @Bean
    public MessageConverter jsonMessageConverter() {
        return  new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue textMessageQueue() {
        return new Queue(TEXT_MESSAGE_UPDATE);
    }
    @Bean
    public Queue doctMessageQueue() {
        return new Queue(DOC_MESSAGE_UPDATE);
    }
    @Bean
    public Queue photoMessageQueue() {
        return new Queue(PHOTO_MESSAGE_UPDATE);
    }
    @Bean
    public Queue answerMessageQueue() {
        return new Queue(ANSWER_MESSAGE);
    }

}
