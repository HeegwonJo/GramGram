# 1Week_HeegwonJo.md

## Title: [1Week] 조희권

### 미션 요구사항 분석 & 체크리스트

---

### 필수미션
- 호감상대 삭제

- [x] 컨트롤러,서비스 메서드 구현
- [x] 매핑 => delete/{id}로 구현
- [x] rq.redirectWithMsg로 리스트 리다이렉션 및 삭제 메시지 출력

### 추가미션
- 구글 로그인 연동
- [x] 구글 API Console에서 프로젝트 생성
- [x] 구글 OAuth2 클라이언트 설정



### 1주차 미션 요약

---

### **[접근 방법]**

- 해당 likeablePerson을 삭제 하려면 id를 기준으로 가져와야함.

- 컨트롤러에서 id를 통해서 likeablePerson 객체를 찾아서 가져오고 서비스에 전달
  서비스에서 리포지토리에 전달해서 삭제.

- 게시판 만들 때 리스트에서 질문 삭제 했던 로직 참고해서 구현.

- 이미 등록해놓은 카카오 로그인 application.yml 참고해서 구글 API Console 참고해서 구현
- 구글 등록하는 김에 네이버도 등록.

### **[특이사항]**
- 이미지 파일로 버튼을 만들려는데 올바른 경로를 입력해도 계속 엑스박스가 떴음.

  => 타임리프 문법으로 이미지 접근하는 방식 검색을 통해서 확인. 해결


- 부트스트랩에 익숙해져 있어서 css 적용하려고 클래스 적용했는데 다른 부분들 때문에 적용 안될 때 약간 당황함.


- 테일윈드와 데이지 ui에 조금 더 적응하는 기간이 필요할 듯.


- 본인의 호감상대 삭제시에 검증하는 로직이 최선인지에 대해서 고민중.
  현 방식은 호감을 가진 사람인 fromInstaMember의 인스타 계정과 현재 사용자 rq.getMember의 인스타계정 비교 방식.



# 1Week_HeegwonJo.md

## Title: [1Week] 조희권

### 미션 요구사항 분석 & 체크리스트

---

### 필수미션
- 호감표시 할 때 예외처리 케이스 3가지를 추가로 처리

### 추가미션
- 네이버 로그인 연동


### 2주차 미션 요약

---

### **[접근 방법]**

- 선택미션 : 네이버 로그인 구현 및 유저정보는 표시 안되게 수정.



<img width="496" alt="image" src="https://user-images.githubusercontent.com/101499795/231059709-b9f9494e-637e-4ee8-9653-0fc5bd52ec83.png">

- 네이버 로그인시에 넘어오는 응답 확인하고 성공 코드랑 메시지는 필요없으니까 담겨넘어온 유저정보만 맵 새로 만들어서 저장.
  
  - (나중에 이메일이랑 이름 사용할 예정 => 기존 회원과 연동 및 기존 id 있는지 검증시)

```agsl
Map<String, Object> attributes = (Map<String, Object>) oAuth2User.getAttributes().get("response");
```
- 현재 이렇게 네이버 계정에 할당되는 고유 id, email , name 이 담겨있음.

<img width="465" alt="image" src="https://user-images.githubusercontent.com/101499795/231060424-f28ee40f-e849-40e8-be41-44a5d66872d1.png">


- 기존 CustomOAuth2UserService 에서 공급자 타입이 네이버이면 잡아내는 if문 생성 해서 구글, 카카오와는 다른 방식으로 username 설정해줌

```agsl
 if (providerTypeCode.equals("NAVER")) {
            String username =providerTypeCode + "__%s".formatted(attributes.get("id"));
            Member member = memberService.whenSocialLogin(providerTypeCode, username).getData();

            return new CustomOAuth2User(member.getUsername(), member.getPassword(), member.getGrantedAuthorities());
        }
```

- **결과** : 뒤에 표시되던 email,name은 표시하지 않고 고유 id만 표시됨

<img width="679" alt="image" src="https://user-images.githubusercontent.com/101499795/231060802-7d4c6b85-6450-40c9-b69a-e97e45b0d37e.png">





### **[특이사항]**




- ### **참고: [Refactoring]**

   
