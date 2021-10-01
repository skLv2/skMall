package skmall;

import skmall.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired OrderRepository orderRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCancelled_UpdateStatus(@Payload DeliveryCancelled deliveryCancelled){

        if(!deliveryCancelled.validate()) return;

        System.out.println("\n\n##### listener UpdateStatus : " + deliveryCancelled.toJson() + "\n\n");

        Order order = new Order();
        order.setCustomerId(deliveryCancelled.getCustomerId());
        order.setProductId(deliveryCancelled.getProductId());
        order.setStatus("배송취소");
        orderRepository.save(order);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverShipped_UpdateStatus(@Payload Shipped shipped){

        if(!shipped.validate()) return;

        System.out.println("\n\n##### listener UpdateStatus : " + shipped.toJson() + "\n\n");
        
        Order order = new Order();
        order.setCustomerId(shipped.getCustomerId());
        order.setProductId(shipped.getProductId());
        order.setStatus("배송성공");
        orderRepository.save(order);
        
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}