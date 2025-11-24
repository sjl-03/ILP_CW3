# Drone simulator and visualiser

Run RabbitMQ in Docker 
```
docker run -d --hostname rabbit --name rabbitmq \
  -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

After cloning the repo, first locate the index file inside Drone Visualiser.

To view live update, go to
```
http://localhost:8082/
```

Then run both the Drone Visualiser and Simulator. 

Live info of the simulated drone should be visible in the browser. 
