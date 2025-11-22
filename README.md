# Drone simulator and visualiser

Run RabbitMQ in Docker 
```
docker run -d --hostname rabbit --name rabbitmq \
  -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

After cloning the repo, first locate the index file inside Drone Visualiser.

Paste the absolute path of the index file into the browser, for example
```
file:///Users/shenjunlu/Edinburgh_Materials/UoE_Courseworks/Y3S1_ILP/CW3/drone_visualiser/src/main/resources/static/index.html
```

Then run both the Drone Visualiser and Simulator. 

Live info of the simulated drone should be visible in the browser. 
