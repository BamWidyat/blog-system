FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/blog-system-0.0.1-SNAPSHOT-standalone.jar /blog-system/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/blog-system/app.jar"]
