# 4thWeek_HeegwonJo.md

## Title: [4Week] 조희권

### 미션 요구사항 분석 & 체크리스트

---

### 필수미션
- 네이버클라우드플랫폼을 통한 배포(도메인 적용)

- 내가 받은 호감 목록 필터링 기능 구현

### 추가미션
- 내가 받은 호감 목록 정렬 기능 구현


### 4주차 미션 요약

---

### **[접근 방법]**
### 필수미션: 배포
- 도메인 [joyfulgwon.site]
- 도메인 구매 및 npm 인증서 적용까지 수행.
- 코드에 수정 할 부분이 있어서 재배포 했는데 502 bad gateway 떠서 조치중.

<img width="1129" alt="image" src="https://github.com/HeegwonJo/Mission_HeegwonJo/assets/101499795/bf825d80-ec7a-42c1-bfd4-ff5ed2d4a320">


### 필수미션:내가 받은 호감 목록 성별 필터링 구현.

- 구현되어있는 ui 확인하고 기능 구현.


### LikeabePersonController.java
```java
// 인스타인증을 했는지 체크
        if (instaMember != null) {
            // 해당 인스타회원이 좋아하는 사람들 목록
            List<LikeablePerson> likeablePeople;
            if (gender == null || gender.isBlank()) { // gender가 선택되지 않은 경우, 빈칸인 경우
                likeablePeople = instaMember.getToLikeablePeople();
            } else { // gender가 선택된 경우
                likeablePeople = instaMember.getToLikeablePeople().stream()
                        .filter(likeablePerson -> likeablePerson.getFromInstaMember().getGender().equals(gender))
                        .collect(Collectors.toList());
            }
            if (attractiveTypeCode != null) { //호감 사유가 선택된 경우
                likeablePeople = likeablePeople.stream()
                        .filter(likeablePerson -> likeablePerson.getAttractiveTypeCode() == (attractiveTypeCode))
                        .collect(Collectors.toList());
            }
```

gender 가 null이 아닌 경우에 스트림으로 필터링해서 나타내게 구현했는데
남성, 혹은 여성 선택 후에 다시 전체 누르면 gender="" 로 나오는 걸 디버깅 과정중 발견.

비어있는 경우에 전체 목록 나타내게 구현.

마찬가지로 attractiveTypeCode 넘어올 경우에 필터링 되어서 나타나게 구현.



### 선택미션 : 정렬 구현
### LikeabePersonController.java
```java
   if (sortCode != null) {
                switch (sortCode) {
                    case 2: //날짜순
                        likeablePeople = likeablePeople.stream()
                                .sorted(Comparator.comparing(BaseEntity::getCreateDate))
                                .collect(Collectors.toList());
                        break;
                    case 3:// 인기순
                        likeablePeople = likeablePeople.stream()
                                .sorted(Comparator.comparing(a -> a.getFromInstaMember().getToLikeablePeople().size(), Comparator.reverseOrder()))
                                .collect(Collectors.toList());
                        break;

                    case 4:// 인기 역순
                        likeablePeople = likeablePeople.stream()
                                .sorted(Comparator.comparing(a -> a.getFromInstaMember().getToLikeablePeople().size()))
                                .collect(Collectors.toList());
                        break;
                    case 5: // 성별순 여성 먼저.
                        likeablePeople = likeablePeople.stream()
                                .sorted(Comparator.comparing(a -> a.getFromInstaMember().getGender(),Comparator.reverseOrder()))
                                .sorted(Comparator.comparing(LikeablePerson::getCreateDate).reversed())
                                .collect(Collectors.toList());
                        break;
                    case 6: //호감 사유 순
                        likeablePeople = likeablePeople.stream()
                                .sorted(Comparator.comparingInt(LikeablePerson::getAttractiveTypeCode))
                                .sorted(Comparator.comparing(LikeablePerson::getCreateDate).reversed())
                                .collect(Collectors.toList());
                        break;
                    case 1: // 최신순 (기본값)
                    default:
                        likeablePeople = likeablePeople.stream()
                                .sorted(Comparator.comparing(LikeablePerson::getCreateDate).reversed())
                                .collect(Collectors.toList());

                }
            }
```
람다식 comparator로 정렬하는건 해봤지만 스트림을 이용해본 적은 없어서 좀 해맸음. 
각각의 케이스에 맞는 정렬 방식을 적용해서 구현.


### 결과




### **[특이사항]**
- 도커 사용법이 익숙치 않아서 많이 해매고 어려움을 겪었음.
- ### **참고: [Refactoring]**
- 정렬 기능 확인을 하기 위해서는 많은 테스트 데이터를 만들고 테스트 코드를 작성해서 확인하는게 좋은데 테스트 코드 작성 없이 몇개의 테스트 케이스만 추가해서 잘 작동되는지 확인해봤음. 추가로 올바르게 정렬 되는지 확인해야 할 필요 있음.