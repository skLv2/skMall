package skmall;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long productId;
    private Integer qty;
    private String status;
    private Long customerId;

    @PostPersist
    public void onPostPersist(){
        
        Ordered ordered = new Ordered();
        BeanUtils.copyProperties(this, ordered);
        ordered.publishAfterCommit();        

    }
    @PostRemove
    public void onPostRemove(){
        OrderCancelled orderCancelled = new OrderCancelled();
        BeanUtils.copyProperties(this, orderCancelled);
        orderCancelled.publishAfterCommit();

    }
    @PrePersist
    public void onPrePersist(){
        
        // Get request from Warehouse
        /*skmall.external.Warehouse warehouse =
            OrderApplication.applicationContext.getBean(skmall.external.WarehouseService.class).getWarehouse(productId);
        
        if(warehouse.getStock() > 0 ){
            Ordered ordered = new Ordered();
            ordered.setStatus("OrderSuccessed");
            BeanUtils.copyProperties(this, ordered);
            ordered.publishAfterCommit();
        } */

    }
    @PreRemove
    public void onPreRemove(){
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

}