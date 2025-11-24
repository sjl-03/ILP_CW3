package com.ilp.cw3.drone_simulator.model;

public record DroneDeliveryInfo(
        DroneDetail droneDetail,
        DeliveryPath deliveryPath
) {
}
