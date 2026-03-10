package com.clubportal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TimeSlotUpsertRequest {

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @JsonAlias({"max_capacity", "maxCapacity", "capacity", "max"})
    private Integer maxCapacity;

    @JsonAlias({"price", "slotPrice", "fee", "amount"})
    private BigDecimal price;

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
