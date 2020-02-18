< Sct21 docker 컨테이너 셋팅방법 >

1. docker images 받기 ( 둘중 선택 ) 뒤에 tag는 engine버전이다. -> docker push 

   : 특정버전 받는방법

   - symverse/symverse_sct21_sample:latest -> docker pull symverse/symverse_sct20_sample:1.0.14

   : 최신버전 받는 방법

   - symverse/symverse_sct21_sample:latest -> docker pull symverse/symverse_sct20_sample:latest

     

2. docker 실행 

   - sudo docker run -e KEYSTORE_PASSWORD=0000 -d -p ${my want port}:8888 --name sct21_sample_springboot symverse/symverse_sct21_sample

     

3. 지갑 키스토에서 서버 키스토어로 변경하는 작업 

   - http://${my ip}:{my want port}/keystore/convert 접속!!

   - 지갑 키스토를 넣고 변환버튼을 누르면 서버 키스토어 형태로 변환해 준다.

     

4. 본인의 keystore 컨테이너로 전송 

   - docker cp /home/sct21Sample/keystore.json {my_container_id}:/webapp/keystore/keystore.json

     

5. 컨테이너 재실행 -> docker restart {my_container_id}

   

6. 제대로 올라갔는지 log 확인 -> docker logs {my_container_id}