FROM openjdk:8-jdk-alpine

RUN mkdir -p /webapp
RUN mkdir -p /webapp/keystore
RUN mkdir -p /webapp/javadoc
RUN mkdir -p /webapp/postman
ADD postman/* /webapp/postman

WORKDIR /webapp
EXPOSE 8888

#mainnet infomation
ENV SERVICE_MODE=${SERVICE_MODE:-docker}
ENV CHAIN_ID=${CHAIN_ID:-1}
ENV NODE_URL=${NODE_URL:-http://58.227.193.175:8545}
ENV ENGINE_VERSION=${ENGINE_VERSION:-1.0.14}
ENV WORK_NODE=${WORK_NODE:-0002db0859e72aad0002}

#My keystore infomation
ENV KEYSTORE_PASSWORD=${KEYSTORE_PASSWORD:-INSERT_PASSWORD}
ENV KEYSTORE_FILENAME=${KEYSTORE_FILENAME:-keystore.json}

COPY sct21SampleProject-0.0.1-SNAPSHOT.jar .
CMD java -jar -DSERVICE_MODE=$SERVICE_MODE -DWORK_NODE=$WORK_NODE  -DCHAIN_ID=$CHAIN_ID -DNODE_URL=$NODE_URL -DENGINE_VERSION=$ENGINE_VERSION -DKEYSTORE_PASSWORD=$KEYSTORE_PASSWORD -DKEYSTORE_FILENAME=$KEYSTORE_FILENAME sct21SampleProject-0.0.1-SNAPSHOT.jar
