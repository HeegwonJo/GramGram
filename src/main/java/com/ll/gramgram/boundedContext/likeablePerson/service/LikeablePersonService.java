package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.rq.Rq;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;
    private final Rq rq;

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {

        if (member.hasConnectedInstaMember() == false) {
            return RsData.of("F-2", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }

        if (member.getInstaMember().getUsername().equals(username)) {
            return RsData.of("F-1", "본인을 호감상대로 등록할 수 없습니다.");
        }

        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();
        InstaMember fromInstaMember = member.getInstaMember();
        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(member.getInstaMember().getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .build();

        // 너가 좋아하는 호감표시 생겼어.
        fromInstaMember.addFromLikeablePerson(likeablePerson);

        // 너를 좋아하는 호감표시 생겼어.
        toInstaMember.addToLikeablePerson(likeablePerson);

        likeablePersonRepository.save(likeablePerson); // 저장

        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

    public RsData isPresent(Member member, String username, int attractiveTypeCode) {
        Optional<LikeablePerson> toLikeAblePerson = likeablePersonRepository.findByToInstaMemberUsername(username);

        //username으로 검색한 인스타 계정이 존재하고 내가 팔로우했으며, 매력이 같을 경우에 실패.
        if (toLikeAblePerson.isPresent() && member.getInstaMember().getFromLikeablePeople().contains(toLikeAblePerson.get())) {
            if (attractiveTypeCode == toLikeAblePerson.get().getAttractiveTypeCode()) {
                return RsData.of("F-2", "(%s)은 이미 존재하는 호감상대입니다.".formatted(username));
            }
        }
        return RsData.of("S-2", "%s는 추가가능한 상대".formatted(username));
    }

    @Transactional
    public RsData<LikeablePerson> modifyAttractiveType(Member member, String username, int attractiveTypeCode) {
        Optional<LikeablePerson> toLikeAblePerson = likeablePersonRepository.findByToInstaMemberUsername(username);
        //존재하고 가지고 있고
        if (toLikeAblePerson.isPresent()) {
            LikeablePerson likeablePerson = toLikeAblePerson.get();
            int existingAttractiveTypeCode = likeablePerson.getAttractiveTypeCode();
            if (member.getInstaMember().getFromLikeablePeople().contains(likeablePerson) && attractiveTypeCode != existingAttractiveTypeCode) {
                likeablePerson.setAttractiveTypeCode(attractiveTypeCode);
                likeablePersonRepository.save(likeablePerson);
                return RsData.of("S-1", "%s 님의 호감 정보 수정 완료".formatted(username));
            }
        }
        return RsData.of("F-1", "수정 할 수 없습니다.");
    }

    public RsData ifMaxSize(Member member) {
        if (member.getInstaMember().getFromLikeablePeople().size() >= 10) {
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

    @Transactional
    public RsData delete(LikeablePerson likeablePerson) {
        String toInstaMemberUsername = likeablePerson.getToInstaMember().getUsername();
        this.likeablePersonRepository.delete(likeablePerson);

        return RsData.of("S-1", "%s 님에 대한 호감을 취소했습니다.".formatted(toInstaMemberUsername));
    }

    public RsData ableToDelete(Member actor, LikeablePerson likeablePerson) {
        if (likeablePerson == null) return RsData.of("F-1", "이미 삭제되었습니다.");

        // 수행자의 인스타계정 번호
        long actorInstaMemberId = actor.getInstaMember().getId();
        // 삭제 대상의 작성자(호감표시한 사람)의 인스타계정 번호
        long fromInstaMemberId = likeablePerson.getFromInstaMember().getId();

        if (actorInstaMemberId != fromInstaMemberId)
            return RsData.of("F-2", "권한이 없습니다.");

        return RsData.of("S-1", "삭제가능합니다.");
    }
}
