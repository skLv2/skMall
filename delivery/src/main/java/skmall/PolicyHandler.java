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
    @Autowired DeliveryRepository deliveryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrdered_PrepareShip(@Payload Ordered ordered){

        if(!ordered.validate()) return;

        System.out.println("\n\n##### listener PrepareShip : " + ordered.toJson() + "\n\n");

        Delivery delivery = new Delivery();
        delivery.setCustomerId(ordered.getCustomerId());
        delivery.setOrderId(ordered.getId());
        delivery.setProductId(ordered.getProductId());
        delivery.setStatus("주문성공");
        deliveryRepository.save(delivery);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCancelled_CancelDelivery(@Payload OrderCancelled orderCancelled){

        if(!orderCancelled.validate()) return;

        System.out.println("\n\n##### listener CancelDelivery : " + orderCancelled.toJson() + "\n\n");
        Delivery delivery = new Delivery();
        delivery.setCustomerId(orderCancelled.getCustomerId());
        delivery.setOrderId(orderCancelled.getId());
        delivery.setProductId(orderCancelled.getProductId());
        delivery.setStatus("주문취소");
        deliveryRepository.save(delivery);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}