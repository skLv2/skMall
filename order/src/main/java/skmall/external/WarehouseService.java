package skmall.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="warehouse", url="http://warehouse:8080")
public interface WarehouseService {
    @RequestMapping(method= RequestMethod.GET, path="/warehouses/{id}")
    public Warehouse getWarehouse(@PathVariable("id") Long id);

}

