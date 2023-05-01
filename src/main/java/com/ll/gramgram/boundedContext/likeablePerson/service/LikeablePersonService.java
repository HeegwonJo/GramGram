package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.appConfig.AppConfig;
<<<<<<< HEAD
import com.ll.gramgram.base.rq.Rq;
=======
import com.ll.gramgram.base.event.EventAfterLike;
import com.ll.gramgram.base.event.EventAfterModifyAttractiveType;
import com.ll.gramgram.base.event.EventBeforeCancelLike;
>>>>>>> main
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

<<<<<<< HEAD
import java.time.LocalDateTime;
=======
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
>>>>>>> main
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;
<<<<<<< HEAD
    private final Rq rq;

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {

        if (member.hasConnectedInstaMember() == false) {
            return RsData.of("F-2", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }
=======
    private final ApplicationEventPublisher publisher;

    @Transactional
    public RsData<LikeablePerson> like(Member actor, String username, int attractiveTypeCode) {
        RsData canLikeRsData = canLike(actor, username, attractiveTypeCode);
>>>>>>> main

        if (canLikeRsData.isFail()) return canLikeRsData;

        if (canLikeRsData.getResultCode().equals("S-2")) return modifyAttractive(actor, username, attractiveTypeCode);

        InstaMember fromInstaMember = actor.getInstaMember();
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();
        InstaMember fromInstaMember = member.getInstaMember();
        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
<<<<<<< HEAD
                .fromInstaMemberUsername(member.getInstaMember().getUsername()) // 중요하지 않음
=======
                .fromInstaMemberUsername(actor.getInstaMember().getUsername()) // 중요하지 않음
>>>>>>> main
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .build();

        // 너가 좋아하는 호감표시 생겼어.
        fromInstaMember.addFromLikeablePerson(likeablePerson);

        // 너를 좋아하는 호감표시 생겼어.
        toInstaMember.addToLikeablePerson(likeablePerson);

        likeablePersonRepository.save(likeablePerson); // 저장

        // 너가 좋아하는 호감표시 생겼어.
        fromInstaMember.addFromLikeablePerson(likeablePerson);

        // 너를 좋아하는 호감표시 생겼어.
        toInstaMember.addToLikeablePerson(likeablePerson);

        publisher.publishEvent(new EventAfterLike(this, likeablePerson));

        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

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


    //수정 메서드
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
                likeablePerson.modifyAttractiveTypeCode(attractiveTypeCode);
                likeablePersonRepository.save(likeablePerson);
                return RsData.of("S-1", "%s 님의 호감정보 수정 완료".formatted(username),likeablePerson);
            }
        }
        return RsData.of("F-1", "수정 할 수 없습니다.");
    }

    //더 추가 가능한지 검증 메서드 사이즈 10넘으면 더이상 추가 안 됨.
    public RsData ifMaxSize(Member member) {
        Long maxSize = AppConfig.getLikeablePersonFromMax();

        if (member.getInstaMember().getFromLikeablePeople().size() >= maxSize) {
            return RsData.of("F-1", "더 이상 호감상대를 추가할 수 없습니다.");
        }
        return RsData.of("S-1", "아직 추가가능");
    }

    private Optional<LikeablePerson> findByToInstaMember(String username) {
        return likeablePersonRepository.findByToInstaMemberUsername(username);
    }


    public List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId) {
        return likeablePersonRepository.findByFromInstaMemberId(fromInstaMemberId);
    }

    public Optional<LikeablePerson> findById(Long id) {
        return likeablePersonRepository.findById(id);
    }

<<<<<<< HEAD
    //삭제
    @Transactional
    public RsData delete(LikeablePerson likeablePerson) {
        String toInstaMemberUsername = likeablePerson.getToInstaMember().getUsername();
        this.likeablePersonRepository.delete(likeablePerson);

        return RsData.of("S-1", "%s 님에 대한 호감을 취소했습니다.".formatted(toInstaMemberUsername));
    }

    //삭제 권한 확인
    public RsData ableToDelete(Member actor, LikeablePerson likeablePerson) {
=======
    @Transactional
    public RsData cancel(LikeablePerson likeablePerson) {
        publisher.publishEvent(new EventBeforeCancelLike(this, likeablePerson));

        // 너가 생성한 좋아요가 사라졌어.
        likeablePerson.getFromInstaMember().removeFromLikeablePerson(likeablePerson);

        // 너가 받은 좋아요가 사라졌어.
        likeablePerson.getToInstaMember().removeToLikeablePerson(likeablePerson);

        likeablePersonRepository.delete(likeablePerson);

        String likeCanceledUsername = likeablePerson.getToInstaMember().getUsername();
        return RsData.of("S-1", "%s님에 대한 호감을 취소하였습니다.".formatted(likeCanceledUsername));
    }

    public RsData canCancel(Member actor, LikeablePerson likeablePerson) {
>>>>>>> main
        if (likeablePerson == null) return RsData.of("F-1", "이미 삭제되었습니다.");

        // 수행자의 인스타계정 번호
        long actorInstaMemberId = actor.getInstaMember().getId();
        // 삭제 대상의 작성자(호감표시한 사람)의 인스타계정 번호
        long fromInstaMemberId = likeablePerson.getFromInstaMember().getId();

        if (actorInstaMemberId != fromInstaMemberId)
            return RsData.of("F-2", "권한이 없습니다.");

<<<<<<< HEAD
        return RsData.of("S-1", "삭제가능합니다.");
    }
=======
        long likeablePersonDurationAfterModified = AppConfig.getLikeablePersonDurationAfterModified();

        if(likeablePerson.getDurationAfterModified()<=likeablePersonDurationAfterModified){
            return RsData.of("F-3", "마지막 수정이후 3시간이 경과하지 않았습니다.");
        }
        return RsData.of("S-1", "삭제가능합니다.");
    }

    private RsData canLike(Member actor, String username, int attractiveTypeCode) {
        if (!actor.hasConnectedInstaMember()) {
            return RsData.of("F-1", "먼저 본인의 인스타그램 아이디를 입력해주세요.");
        }

        InstaMember fromInstaMember = actor.getInstaMember();

        if (fromInstaMember.getUsername().equals(username)) {
            return RsData.of("F-2", "본인을 호감상대로 등록할 수 없습니다.");
        }

        // 액터가 생성한 `좋아요` 들 가져오기
        List<LikeablePerson> fromLikeablePeople = fromInstaMember.getFromLikeablePeople();

        // 그 중에서 좋아하는 상대가 username 인 녀석이 혹시 있는지 체크
        LikeablePerson fromLikeablePerson = fromLikeablePeople
                .stream()
                .filter(e -> e.getToInstaMember().getUsername().equals(username))
                .findFirst()
                .orElse(null);

        if (fromLikeablePerson != null && fromLikeablePerson.getAttractiveTypeCode() == attractiveTypeCode) {
            return RsData.of("F-3", "이미 %s님에 대해서 호감표시를 했습니다.".formatted(username));
        }

        long likeablePersonFromMax = AppConfig.getLikeablePersonFromMax();

        if (fromLikeablePerson != null) {
            return RsData.of("S-2", "%s님에 대해서 호감표시가 가능합니다.".formatted(username));
        }

        if (fromLikeablePeople.size() >= likeablePersonFromMax) {
            return RsData.of("F-4", "최대 %d명에 대해서만 호감표시가 가능합니다.".formatted(likeablePersonFromMax));
        }

        return RsData.of("S-1", "%s님에 대해서 호감표시가 가능합니다.".formatted(username));
    }

    public Optional<LikeablePerson> findByFromInstaMember_usernameAndToInstaMember_username(String fromInstaMemberUsername, String toInstaMemberUsername) {
        return likeablePersonRepository.findByFromInstaMember_usernameAndToInstaMember_username(fromInstaMemberUsername, toInstaMemberUsername);
    }

    @Transactional
    public RsData<LikeablePerson> modifyAttractive(Member actor, Long id, int attractiveTypeCode) {
        Optional<LikeablePerson> likeablePersonOptional = findById(id);

        if (likeablePersonOptional.isEmpty()) {
            return RsData.of("F-1", "존재하지 않는 호감표시입니다.");
        }

        LikeablePerson likeablePerson = likeablePersonOptional.get();

        return modifyAttractive(actor, likeablePerson, attractiveTypeCode);
    }

    private RsData<LikeablePerson> modifyAttractive(Member actor, LikeablePerson likeablePerson, int attractiveTypeCode) {
        RsData canModifyRsData = canModifyLike(actor, likeablePerson);

        if (canModifyRsData.isFail()) {
            return canModifyRsData;
        }

        String oldAttractiveTypeDisplayName = likeablePerson.getAttractiveTypeDisplayName();
        String username = likeablePerson.getToInstaMember().getUsername();

        modifyAttractionTypeCode(likeablePerson, attractiveTypeCode);

        String newAttractiveTypeDisplayName = likeablePerson.getAttractiveTypeDisplayName();

        return RsData.of("S-3", "%s님에 대한 호감사유를 %s에서 %s(으)로 변경합니다.".formatted(username, oldAttractiveTypeDisplayName, newAttractiveTypeDisplayName), likeablePerson);
    }

    private RsData<LikeablePerson> modifyAttractive(Member actor, String username, int attractiveTypeCode) {
        // 액터가 생성한 `좋아요` 들 가져오기
        List<LikeablePerson> fromLikeablePeople = actor.getInstaMember().getFromLikeablePeople();

        LikeablePerson fromLikeablePerson = fromLikeablePeople
                .stream()
                .filter(e -> e.getToInstaMember().getUsername().equals(username))
                .findFirst()
                .orElse(null);

        if (fromLikeablePerson == null) {
            return RsData.of("F-7", "호감표시를 하지 않았습니다.");
        }

        return modifyAttractive(actor, fromLikeablePerson, attractiveTypeCode);
    }

    private void modifyAttractionTypeCode(LikeablePerson likeablePerson, int attractiveTypeCode) {
        int oldAttractiveTypeCode = likeablePerson.getAttractiveTypeCode();
        RsData rsData = likeablePerson.updateAttractionTypeCode(attractiveTypeCode);

        if (rsData.isSuccess()) {
            publisher.publishEvent(new EventAfterModifyAttractiveType(this, likeablePerson, oldAttractiveTypeCode, attractiveTypeCode));
        }
    }

    public RsData canModifyLike(Member actor, LikeablePerson likeablePerson) {
        if (!actor.hasConnectedInstaMember()) {
            return RsData.of("F-1", "먼저 본인의 인스타그램 아이디를 입력해주세요.");
        }

        InstaMember fromInstaMember = actor.getInstaMember();

        if (!Objects.equals(likeablePerson.getFromInstaMember().getId(), fromInstaMember.getId())) {
            return RsData.of("F-2", "해당 호감표시를 취소할 권한이 없습니다.");
        }

        long likeablePersonDurationAfterModified = AppConfig.getLikeablePersonDurationAfterModified();

        if(likeablePerson.getDurationAfterModified()<=likeablePersonDurationAfterModified){
            return RsData.of("F-3", "마지막 수정이후 3시간이 지나지 않았습니다.");
        }

        return RsData.of("S-1", "호감표시취소가 가능합니다.");
    }

>>>>>>> main
}



