package com.bookstore.DTO;

import com.bookstore.Entity.OrderStatusHistory;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.ZonedDateTime;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private ZonedDateTime changeAt;

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
