
# 2ndWeek_HeegwonJo.md

## Title: [2Week] 조희권

### 미션 요구사항 분석 & 체크리스트

---

### 필수미션
- 호감표시 할 때 예외처리 케이스 3가지를 추가로 처리

### 추가미션
- 네이버 로그인 연동


### 2주차 미션 요약

---

### **[접근 방법]**
### 필수미션:세가지 예외처리 추가

## 1. 이미 등록된 상대를 같은 매력으로 등록하려면 안되게 구현
- 서비스에 isPresent 메서드 구현
```java
public RsData isPresent(Member member, String username, int attractiveTypeCode) {
        Optional<LikeablePerson> toLikeAblePerson = likeablePersonRepository.findByToInstaMemberUsername(username);

        //username으로 검색한 인스타 계정이 존재하고 내가 팔로우했으며, 매력이 같을 경우에 실패.
        if (toLikeAblePerson.isPresent() && member.getInstaMember().getFromLikeablePeople().contains(toLikeAblePerson.get())) {
            //조건이 길어지니 보기 힘들어서 조건문 속 조건문으로 구현했음
            if (attractiveTypeCode == toLikeAblePerson.get().getAttractiveTypeCode()) {
                return RsData.of("F-2", "(%s)은 이미 존재하는 호감상대입니다.".formatted(username));
            }
        }
        return RsData.of("S-2", "%s는 추가가능한 상대".formatted(username));
    }
```




- ###  **결과**


   <img width="450" alt="image" src="https://user-images.githubusercontent.com/101499795/231350228-ebfccebb-095f-4e0e-9f16-6ddfdfcb2830.png">

## 2.  열명 등록하면 더 이상 호감상대 추가 불가능하게 구현
- 서비스에 ifMaxSize 메서드 구현
```java
 public RsData ifMaxSize(Member member) {
        if (member.getInstaMember().getFromLikeablePeople().size() >= 10) {
            return RsData.of("F-1", "더 이상 호감상대를 추가할 수 없습니다.");
        }
        return RsData.of("S-1", "아직 추가가능");
    }
```

- AppConfig 생성하고 거기서 값 받아오게 변경
```java
public RsData ifMaxSize(Member member) {
        Long maxSize = AppConfig.getLikeablePersonFromMax();

        if (member.getInstaMember().getFromLikeablePeople().size() >= maxSize) {
            return RsData.of("F-1", "더 이상 호감상대를 추가할 수 없습니다.");
        }
        return RsData.of("S-1", "아직 추가가능");
    }

```

- 이제 application.yml에서 값만 바꿔주면 쉽게 최대 사이즈 변경할 수 있음.

- ### **결과**
<img width="999" alt="image" src="https://user-images.githubusercontent.com/101499795/231331046-09393631-bc5e-4ca6-82ca-268d33cb7273.png">

## 3. 다른 매력으로 호감정보 등록하면 수정되게 구현
- 서비스에 modifyAttractiveType 메서드 구현
```java
@Transactional
    public RsData<LikeablePerson> modifyAttractiveType(Member member, String username, int attractiveTypeCode) {
        Optional<LikeablePerson> toLikeAblePerson = likeablePersonRepository.findByToInstaMemberUsername(username);
        //존재하면 객체 가져와서 매력 내용 가져옴. 기존이랑 매개변수 넘어온거랑 다르면 바꿔주고 다시 저장.
        if (toLikeAblePerson.isPresent()) {
            //기존 객체
            LikeablePerson likeablePerson = toLikeAblePerson.get();
            //기존 매력포인트
            int existingAttractiveTypeCode = likeablePerson.getAttractiveTypeCode();

            if (member.getInstaMember().getFromLikeablePeople().contains(likeablePerson) && attractiveTypeCode != existingAttractiveTypeCode) {
                likeablePerson.setAttractiveTypeCode(attractiveTypeCode);
                likeablePersonRepository.save(likeablePerson);
                return RsData.of("S-1", "%s 님의 호감 정보 수정 완료".formatted(username),likeablePerson);
            }
        }
        return RsData.of("F-1", "수정 할 수 없습니다.");
    }
```
- 이미 호감상대 리스트에 들어가 있고 입력한 호감정보가 기존과 다르다면 수정하게 했음
- Transactional 어노테이션 붙여서 DB에 즉시 반영되게 구현.

- ### **결과**
   <img width="465" alt="image" src="https://user-images.githubusercontent.com/101499795/231350349-4af1cc39-6522-47b1-8aee-2e66b3777241.png">
## 컨트롤러
1. 이미 있는지 확인하고 수정인지 판별.
2. 수정이 아니라면 중복인지 검증
3. 리스트 사이즈 확인해서 추가 가능한지 검증
4. 검증 끝나면 등록.

```java
@PreAuthorize("isAuthenticated()")
    @PostMapping("/add")
    public String add(@Valid AddForm addForm) {
        //존재여부 확인
        RsData isPresentRsData = likeablePersonService.isPresent(rq.getMember(),addForm.getUsername(),addForm.getAttractiveTypeCode());
        //리스트 사이즈 확인
        RsData ifMaxSizeRsData = likeablePersonService.ifMaxSize(rq.getMember());
        //수정여부 확인
        RsData modifyRsData = likeablePersonService.modifyAttractiveType(rq.getMember(),addForm.getUsername(),addForm.getAttractiveTypeCode());
        //수정이 성공하면 리스트 출력
        if(modifyRsData.isSuccess()){
            return rq.redirectWithMsg("/likeablePerson/list",modifyRsData);
        }
        //이미 존재하는 지 검증.
        if(isPresentRsData.isFail()){
            return rq.historyBack(isPresentRsData);
        }
        //리스트 사이즈가 10이면 더이상 추가 안됨
        if(ifMaxSizeRsData.isFail()){
            return rq.historyBack(ifMaxSizeRsData);
        }
        //모든 검증이 끝나면 등록 시도
        RsData<LikeablePerson> createRsData = likeablePersonService.like(rq.getMember(), addForm.getUsername(), addForm.getAttractiveTypeCode());
        if (createRsData.isFail()) {
            return rq.historyBack(createRsData);
        }

        return rq.redirectWithMsg("/likeablePerson/list", createRsData);
    }
```









### 선택미션 : 네이버 로그인 구현 및 유저정보는 표시 안되게 수정.



<img width="496" alt="image" src="https://user-images.githubusercontent.com/101499795/231059709-b9f9494e-637e-4ee8-9653-0fc5bd52ec83.png">

- 네이버 로그인시에 넘어오는 응답 확인하고 성공 코드랑 메시지는 필요없으니까 담겨넘어온 유저정보만 맵 새로 만들어서 저장.
  
  - (나중에 이메일이랑 이름 사용할 예정 => 기존 회원과 연동 및 기존 id 있는지 검증시)

```java
Map<String, Object> attributes = (Map<String, Object>) oAuth2User.getAttributes().get("response");
```
- 현재 이렇게 네이버 계정에 할당되는 고유 id, email , name 이 담겨있음.

<img width="465" alt="image" src="https://user-images.githubusercontent.com/101499795/231060424-f28ee40f-e849-40e8-be41-44a5d66872d1.png">


- 기존 CustomOAuth2UserService 에서 공급자 타입이 네이버이면 잡아내는 if문 생성 해서 구글, 카카오와는 다른 방식으로 username 설정해줌

```java
 if (providerTypeCode.equals("NAVER")) {
            String username =providerTypeCode + "__%s".formatted(attributes.get("id"));
            Member member = memberService.whenSocialLogin(providerTypeCode, username).getData();

            return new CustomOAuth2User(member.getUsername(), member.getPassword(), member.getGrantedAuthorities());
        }
```

- **결과** : 뒤에 표시되던 email,name은 표시하지 않고 고유 id만 표시됨

<img width="679" alt="image" src="https://user-images.githubusercontent.com/101499795/231060802-7d4c6b85-6450-40c9-b69a-e97e45b0d37e.png">





### **[특이사항]**

- 현 프로그램에서 likeablePerson 객체를 생성할 때 빌더 패턴을 이용해서 객체를 생성했는데 Builder로 수정하려고 하니까
새로운 객체가 생성되어 저장되고 수정이 되질 않았음. attractiveTypeCode 필드에만 @SETTER 달아주고 수정했는데 좋은 방법은 아닌듯 함.
```java
likeablePerson.setAttractiveTypeCode(attractiveTypeCode);
```

- isPresent 메서드에서 이미 리스트에 존재하는지 확인을 하기 떄문에 수정도 가능하다고 생각해서 수정이나 실패를 하나의 
메서드에서 반환하게 만들려고 했는데 기능을 나누어 놓으려고 modify를 별도로 만들어서 중복 코드가 발생함.
    - 무조건 중복을 줄이는 것이 좋을지 기능을 분할하는게 좋을지에 대해 나의 기준이 명확하지 않은 점이 아쉬움.


- ### **참고: [Refactoring]**

   - 현재 컨트롤러에서 각각의 RsData를 따로 받아서 순차적으로 검증하고 있는데 이 모든 검증과정을 하나의 결과로 받을 수 있도록
논리 게이트로 회로 설계하듯이 설계해보면 재미있고 컨트롤러 코드를 간결하게 줄일 수 있을 것 같음.
  - 네이버 로그인 하면 고유 id가 너무 길어서 불편함