# Drone simulator and visualiser

## Project Structure
There are 3 Docker images, which are loaded from `ilp_submisson_image.tar`, `drone_simulator_image.tar` and `drone_visualiser_image.tar`. All components run together through Docker and communicate via a shared Docker network.

## Getting Started
cd into the root folder of this project, where you should find this README file and  `ilp_submisson_image.tar`, `drone_simulator_image.tar`, `drone_visualiser_image.tar`.

Now run the following
```
docker image load -i ilp_submisson_image.tar
docker image load -i drone_simulator_image.tar
docker image load -i drone_visualiser_image.tar

docker network create ilpnet
docker run -d --network ilpnet --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
docker run -d --network ilpnet --name ilp-cw2 -p 8080:8080 ilp_cw2:latest
docker run -d --network ilpnet --name drone-simulator -p 8081:8081 drone_simulator:latest
docker run -d --network ilpnet --name drone-visualiser -p 8082:8082 drone_visualiser:latest
```

Open the browser and go to:
```
http://localhost:8082/
```
This will display the live map visuliser and drone status panel.

## Trigger a Simulation

Send a POST request to the Drone Simulater, e.g.:
```
curl -X POST http://localhost:8081/api/v1/simulateDeliveryPath \
  -H "Content-Type: application/json" \
  -d '[
    {
        "id": 1,
        "date": "2025-12-22",
        "time": "14:30",
        "requirements": {
            "capacity": 0.75,
            "cooling": false,
            "heating": true,
            "maxCost": 13.5
        },
        "delivery": { "lng": -3.189, "lat": 55.941 }
    },
    {
        "id": 2,
        "date": "2025-12-22",
        "time": "14:30",
        "requirements": {
            "capacity": 0.15,
            "cooling": false,
            "heating": false,
            "maxCost": 10.5
        },
        "delivery": { "lng": -3.189, "lat": 55.951 }
    },
    {
        "id": 3,
        "date": "2025-12-22",
        "time": "14:30",
        "requirements": {
            "capacity": 6,
            "cooling": false,
            "heating": false,
            "maxCost": 5.0
        },
        "delivery": { "lng": -3.183, "lat": 55.95 }
    },
    {
        "id": 4,
        "date": "2025-12-22",
        "time": "14:30",
        "requirements": {
            "capacity": 0.65,
            "cooling": false,
            "heating": true,
            "maxCost": 15.0
        },
        "delivery": { "lng": -3.213, "lat": 55.94 }
    },
    {
        "id": 5,
        "date": "2025-12-22",
        "time": "14:30",
        "requirements": {
            "capacity": 0.75,
            "cooling": false,
            "heating": true,
            "maxCost": 13.5
        },
        "delivery": { "lng": -3.2088, "lat": 55.9799 }
    },
    {
        "id": 6,
        "date": "2025-12-22",
        "time": "14:30",
        "requirements": {
            "capacity": 0.15,
            "cooling": false,
            "heating": false,
            "maxCost": 10.5
        },
        "delivery": { "lng": -3.1845, "lat": 55.9707 }
    },
    {
        "id": 7,
        "date": "2025-12-22",
        "time": "14:30",
        "requirements": {
            "capacity": 0.65,
            "cooling": false,
            "heating": true,
            "maxCost": 15.0
        },
        "delivery": { "lng": -3.1795, "lat": 55.9434 }
    },
    {
        "id": 8,
        "date": "2025-12-22",
        "time": "14:30",
        "requirements": {
            "capacity": 0.75,
            "cooling": false,
            "heating": true,
            "maxCost": 13.5
        },
        "delivery": { "lng": -3.1655, "lat": 55.9806 }
    }
]'

```

# Building Docker Images
Below is how the Docker images are built
```
docker buildx build --load -t ilp_cw2 .
docker image save ilp_cw2 -o ilp_submisson_image.tar
```
```
docker buildx build --load -t drone_simulator .
docker image save drone_simulator -o drone_simulator_image.tar
```
```
docker buildx build --load -t drone_visualiser .
docker image save drone_visualiser -o drone_visualiser_image.tar
```
