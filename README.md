# HIFLY
Live Video Web Streaming Service using Drone(Application)

> [HIFLY Final](https://github.com/HIFLY-CSM/HIFLY-Final) 참조

​

PC 버전과 Raspberry PI 버전 구별없이 같은 어플리케이션을 사용한다.

​

​

## 사용 법

​
1. <https://developer.dji.com/mobile-sdk/> 사이트에 접속한다.

​
2. 회원가입을 한 후 SDK를 사용하기 위해 사이트 SDK 사용 메뉴얼에 따라 인증키를 받고 안드로이드 프로젝트에 모듈을 적용시킨다.

​
3. HIFLY 프로젝트에서는 CAMERA SDK를 사용했다.

​
4. ```HIFLYStreamingSocket.java```의 ```private final String SERVER_ADDRESS```를 Streamming Server의 IP 주소와 맞춘다.

- PC : HIFLY-JAVA-SERVER의 IP 주소

- Raspberry PI : HIFLY-JAVA-SERVER의 IP 주소

​
5.  ```MessageSocketThread.java```의 ```final String SERVER_ADDRESS```를 Message Server의 IP 주소와 맞춘다.

- PC : HIFLY-Web-View-Broadcasting의 IP 주소

- Raspberry PI : HIFLY-JAVA-SERVER의 IP 주소

​
6. 각 Message Server와 Streamming Server와 연결이 확인되면 Streamming을 시작할 수 있다


</br> 
<a href="mailto:dydtjr1994@gmail.com" target="_blank">
  <img src="https://img.shields.io/badge/E--mail-Yongseok%20choi-yellow.svg">
</a>
<a href="https://blog.naver.com/cys_star" target="_blank">
  <img src="https://img.shields.io/badge/Blog-cys__star%27s%20Blog-blue.svg">
</a>
