
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
### 필수미션:호감 표시/ 사유 변경 이후 개별 호감 표시 건에 대하여 세시간 동안 수정 및 삭제 불가능 하게 구현.

- 초기 구현 아이디어
  - java의 Duration 클래스를 이용하여, modifyDate와 현 시각의 차이를 구한다.
  - 초단위로 환산하여 10800초가 넘지 않았으면, likeablePersonService에서의 canCancel, canModify 메서드에서 실패 응답을 반환한다.


- LikeablePersonService.java 에 다음과 같은 필드, 메서드 추가
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

````javascript
<script>
        document.addEventListener('DOMContentLoaded', function() {
            var canModifyTime = document.getElementById('canModifyTime');
            var modifyMsg = document.getElementById('modifyMsg');
            var targetTime = new Date(canModifyTime.textContent).getTime();
            var currentTime = new Date().getTime();
            var duration = 3 * 60 * 60 * 1000; // 3시간을 밀리초로 변환

            if (currentTime > targetTime + duration) {
                canModifyTime.style.display = 'none';
                modifyMsg.style.display = 'none';
            }

        });
    </script>
````

<img width="624" alt="image" src="https://user-images.githubusercontent.com/101499795/235405956-1c79fe34-c24f-4bcd-9d6a-b881ff77c588.png">

UI 확인이후 재 수정. 남은 시간을 표시하게 UI가 구현되어 있어서 남은 시간 실시간으로 구현 중.
- 타임리프는 서버측 템플릿 엔진이라, 클라이언트의 현재 시각을 알 수 없음. 

- 새로고침하면 표시된 시간이 변하긴 하지만 실시간 구현하려면 AJAX 통신 구현하거나 일정 시간마다 새로고침 하게 해야할 듯.


<img width="623" alt="image" src="https://user-images.githubusercontent.com/101499795/235411411-a1c032f9-5675-44f7-9e14-14327d0c5d0b.png">


클릭하면
<img width="373" alt="image" src="https://user-images.githubusercontent.com/101499795/235411494-82313f7a-c0dd-4f92-a026-efd5b2585af0.png">

경고 메시지 출력.


### 선택미션 : 네이버 로그인 구현 및 유저정보는 표시 안되게 수정.





### **[특이사항]**



- ### **참고: [Refactoring]**

 