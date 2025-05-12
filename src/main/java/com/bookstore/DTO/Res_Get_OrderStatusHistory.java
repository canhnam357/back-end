package com.bookstore.DTO;

import com.bookstore.Entity.OrderStatusHistory;
import lombok.*;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Res_Get_OrderStatusHistory {
    private String id;
    private String orderId;
    private String fromStatus;
    private String toStatus;
    private String changeBy;
    private String cause;
    private Date changeAt;

    public void convert(OrderStatusHistory ele) {
        id = ele.getId();
        orderId = ele.getOrder().getOrderId();
        fromStatus = ele.getFromStatus().name();
        toStatus = ele.getToStatus().name();
        changeBy = ele.getChangedBy().getUserId();
        cause = ele.getCause();
        changeAt = ele.getChangedAt();
    }
}
