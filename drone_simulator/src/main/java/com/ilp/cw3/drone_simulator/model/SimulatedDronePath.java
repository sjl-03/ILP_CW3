package com.ilp.cw3.drone_simulator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class SimulatedDronePath {
    private final String droneId;
    private final List<SimulatedDelivery> simulatedDeliveries;
    private int deliveryIndex = 0;

    public SimulatedDronePath(String droneId,
                              List<SimulatedDelivery> simulatedDeliveries) {
        this.droneId = droneId;
        this.simulatedDeliveries = simulatedDeliveries;
    }

    private SimulatedDelivery nextDelivery() {
        deliveryIndex++;
        if (deliveryIndex >= simulatedDeliveries.size()) {
            return null;
        }
        else {
            return simulatedDeliveries.get(deliveryIndex);
        }
    }

    public Position nextPosition() {
        SimulatedDelivery delivery =
                simulatedDeliveries.get(deliveryIndex);

        Position nextPosition = delivery.nextPosition();

        if (nextPosition == null) {
            // finished current delivery
            SimulatedDelivery nextDelivery = nextDelivery();

            if (nextDelivery == null) {
                // finished all deliveries
                return null;
            }
            else {
                // next delivery

                // skip the first one since it's not a move
                nextDelivery.nextPosition();
                return nextDelivery.nextPosition();
            }
        }
        else {
            return nextPosition;
        }
    }

    public int getCurrentDeliveryId(){
        SimulatedDelivery delivery =
                simulatedDeliveries.get(deliveryIndex);
        return delivery.getDeliveryId();
    }
}
