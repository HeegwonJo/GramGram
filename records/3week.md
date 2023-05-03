
# 3rdWeek_HeegwonJo.md

## Title: [3Week] 조희권

### 미션 요구사항 분석 & 체크리스트

---

### 필수미션
- 네이버클라우드플랫폼을 통한 배포(도메인 없이, IP로 접속)

- 호감표시/호감사유변경 후, 개별 호감표시건에 대해서, 3시간 동안은 호감취소와 호감사유변경을 할 수 없도록 작업

### 추가미션
- 알림기능 구현


### 3주차 미션 요약

---

### **[접근 방법]**
### 필수미션: 배포
- wiken보면서 진행중. 
- 빌드 준비까지 수행했음.

### 필수미션:호감 표시/ 사유 변경 이후 개별 호감 표시 건에 대하여 세시간 동안 수정 및 삭제 불가능 하게 구현.

- 초기 구현 아이디어
  - java의 Duration 클래스를 이용하여, modifyDate와 현 시각의 차이를 구한다.
  - 초단위로 환산하여 10800초가 넘지 않았으면, likeablePersonService에서의 canCancel, canModify 메서드에서 실패 응답을 반환한다.



- LIkeablePerson, LikeablePersonService.java 에 다음과 같은 필드, 메서드 추가
```java
private Duration durationAfterModified; // 수정, 삭제 부터 흐른 시간

public long getDurationAfterModified(){
        this.durationAfterModified=Duration.between(this.getModifyDate(), LocalDateTime.now());
        return durationAfterModified.toSeconds();
        }

public boolean isModifyUnlocked(){
        return getDurationAfterModified() >= AppConfig.getLikeablePersonDurationAfterModified();
        }
```


<img width="652" alt="image" src="https://user-images.githubusercontent.com/101499795/235405554-2f1961f7-39f2-4676-ac72-1658e06934ee.png">

ui는 마지막 수정 시각으로부터 현재 시각까지 흐른 시각을 초, 분, 시간 단위로 나타내게 변경 .
수정이 가능한 시각 표시하게 구현.

```html
<span class="badge badge-info"
      th:if="${likeablePerson.getDurationAfterModified() < 60}"
      th:text="${likeablePerson.getDurationAfterModified()} + '초 전'"></span>
<span class="badge badge-info"       
      th:if="${likeablePerson.getDurationAfterModified() >= 60 and likeablePerson.getDurationAfterModified() < 3600}"
      th:text="${likeablePerson.getDurationAfterModified() div 60} + '분 전'"></span>
<span class="badge badge-info"       
      th:if="${likeablePerson.getDurationAfterModified() >= 3600}"
      th:text="${likeablePerson.getDurationAfterModified() div 3600} + '시간 전'"></span>
```

또, 수정가능 시각이 되면 아래 안내문구는 사라지게 구현. (버튼 투명도로 버튼 사용불가 표시도 풀림.)
disable을 주면 서비스단에서 잘 막아지는지 확인하기가 어려워서 우선은 클릭이 가능하게 두었음.



<img width="624" alt="image" src="https://user-images.githubusercontent.com/101499795/235405956-1c79fe34-c24f-4bcd-9d6a-b881ff77c588.png">

UI 확인이후 재 수정. 남은 시간을 표시하게 UI가 구현되어 있어서 남은 시간 실시간으로 구현 중.
- 타임리프는 서버측 템플릿 엔진이라, 클라이언트의 현재 시각을 알 수 없음. 

- 새로고침하면 표시된 시간이 변하긴 하지만 실시간 구현하려면 AJAX 통신 구현하거나 일정 시간마다 새로고침 하게 해야할 듯.


<img width="623" alt="image" src="https://user-images.githubusercontent.com/101499795/235411411-a1c032f9-5675-44f7-9e14-14327d0c5d0b.png">


클릭하면
<img width="373" alt="image" src="https://user-images.githubusercontent.com/101499795/235411494-82313f7a-c0dd-4f92-a026-efd5b2585af0.png">

경고 메시지 출력.


### 선택미션 : 알림 구현


- 각각의 알림 따로 만들었음
  - 새로운 호감표시, 호감 수정, 삭제, 성별 변경으로 구분. 
  - 각각의 이벤트에서 NotificationService 호출해서 알림 생성하게함.
  - 알림에 필요한 정보가 알림마다 다르므로 각각 빌더패턴으로 알림객체 생성.

읽기 전에는 readDate null이고 NotificationService에서 알림 읽어올때 모든 알림에 대해 열람일시 지정하는 메서드 추가.
```java
@Transactional
    public RsData updateReadDate(List<Notification> notifications){
        for (Notification notification: notifications){
            if (notification.getReadDate()==null){
                notification.updateReadDate();
                notificationRepository.save(notification);
            }
        }
        return RsData.of("S-1", "열람일시 수정 완료");
    }
```
### 결과

<img width="420" alt="image" src="https://user-images.githubusercontent.com/101499795/235837842-4be5c988-3240-4ade-8940-666a3474ec1f.png">

<img width="274" alt="image" src="https://user-images.githubusercontent.com/101499795/235837956-e925c670-48b3-48a2-bd78-e7d1d1d80d94.png">



### UI수정
```html
 <div th:switch="${notification.getTypeCode()}">
                            <div th:case="Liked">
                            어떤 <span class="badge badge-primary" th:text="${notification.getGenderDisplayName()}"></span>가 당신을
                                <span class="badge badge-primary" th:text="${notification.getAttractiveTypeDisplayName()}"></span> 때문에 좋아합니다.
                            </div>
                            <div th:case="Modified">
                                어떤 <span class="badge badge-primary" th:text="${notification.getGenderDisplayName()}"></span>가 당신을 좋아하는 이유를
                                <span class="badge badge-primary" th:text="${notification.getOldAttractiveTypeDisplayName()}"></span> 에서
                                <span class="badge badge-primary" th:text="${notification.getAttractiveTypeDisplayName()}"></span> 로 변경했습니다.
                            </div>
                            <div th:case="Canceled">
                                어떤 <span class="badge badge-primary" th:text="${notification.getGenderDisplayName()}"></span>가 당신을 더 이상 좋아하지 않습니다.
                            </div>
                            <div th:case="GenderChanged">
                                어떤 <span class="badge badge-primary" th:text="${notification.getOldGenderDisplayName()}"></span>가 성별을
                                <span class="badge badge-primary" th:text="${notification.getGenderDisplayName()}"></span> 로 변경하였습니다..?
                            </div>
                        </div>
```

조건문을 통해 알림의 타입별로 다른 알림 형태 표현하게 했으며 생성일을 기준으로 정렬해서 출력하게 수정.
```java
@Transactional
    public List<Notification> findByToInstaMember(InstaMember toInstaMember) {
        List<Notification> notifications = notificationRepository.findByToInstaMember(toInstaMember);
        //readDate 업데이트
        updateReadDate(notifications);

        //생성일시 역순으로 정렬
        notifications.sort(Comparator.comparing(Notification::getCreateDate).reversed());

        return notifications;
    }
```
<img width="666" alt="image" src="https://user-images.githubusercontent.com/101499795/235838053-eb6bd809-9d86-4a91-a7c7-847183c12e80.png">

### **[특이사항]**
이벤트를 생성함으로써 결합도를 낮추는 이유와 구조에 대해서는 이해가 되었지만 이벤트 리스너가 NotificationService를 호출해서
알림을 생성하게 하는 방식이 올바른 이벤트 사용법인지는 모르겠음.


- ### **참고: [Refactoring]**
- 쿨타임을 적용하면서 시간을 구하는 방식에 대해 고민하다가 Duration을 사용해서 구하고 각각의 객체들이 마지막 수정시간부터 현재까지 흐른 시각을 가지고 있게 했다.
- 실시간으로 반영되지 않고 새로고침을 할 때 적용되면서 시간이 흐르는데 쓸데없는 컬럼을 만든건 아닌지 고민이 되었음. 
 