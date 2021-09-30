package skmall;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Warehouse_table")
public class Warehouse {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String name;
    private Integer stock;

    @PostUpdate
    public void onPostUpdate(){
        StockReduced stockReduced = new StockReduced();
        BeanUtils.copyProperties(this, stockReduced);
        stockReduced.publishAfterCommit();

        StockIncreaced stockIncreaced = new StockIncreaced();
        BeanUtils.copyProperties(this, stockIncreaced);
        stockIncreaced.publishAfterCommit();

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }




}