FROM ubuntu:latest
LABEL authors="Billy"

ENTRYPOINT ["top", "-b"]