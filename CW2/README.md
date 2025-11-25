# Getting Started
First clean and package
```
docker buildx build --load -t ilp_cw2 .
docker image save ilp_cw2 -o ilp_submisson_image.tar
```

In addition to the physical factor of uploading the submission please check:
- That your submission contains source code and the image-file in tar format
- That your tar file can be loaded into docker

```
docker image load -i ilp_submisson_image.tar
```

- That after you loaded the tar file you can run it

```
docker run -d --publish 8080:8080 --name s2559435 ilp_cw2:latest
```
general format `docker run -d --publish 8080:8080 --name sXXXX imageId`

sXXXXX is your student id and the imageId is the result of the image loading – can be either
the image tag or the image id (a rather long unique id)
- Check in docker desktop that your image really runs (no exceptions, etc.)
- Check that you can reach the endpoints using postman, curl or any other tool
