package skmall;

import skmall.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired WarehouseRepository warehouseRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverShipped_ReduceStock(@Payload Shipped shipped){

        if(!shipped.validate()) return;

        System.out.println("\n\n##### listener ReduceStock : " + shipped.toJson() + "\n\n");

        Warehouse warehouse = new Warehouse();
        warehouse.setProductId(shipped.getProductId());
        warehouse.setStock(0);
        warehouseRepository.save(warehouse);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCancelled_IncreaseStock(@Payload DeliveryCancelled deliveryCancelled){

        if(!deliveryCancelled.validate()) return;

        System.out.println("\n\n##### listener IncreaseStock : " + deliveryCancelled.toJson() + "\n\n");

        Warehouse warehouse = new Warehouse();
        warehouse.setProductId(deliveryCancelled.getProductId());
        warehouse.setStock(1);
        warehouseRepository.save(warehouse);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}
}